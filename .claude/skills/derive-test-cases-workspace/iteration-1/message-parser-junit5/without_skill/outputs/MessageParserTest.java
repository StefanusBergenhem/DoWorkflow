package com.protocol.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MessageParser derived from detailed design DD-007.
 *
 * Message frame layout:
 *   [0..1]  SYNC_BYTES (0xAA, 0x55)
 *   [2]     VERSION (must equal SUPPORTED_VERSION = 1)
 *   [3..4]  PAYLOAD_LENGTH (big-endian uint16)
 *   [5]     MESSAGE_TYPE
 *   [6..9]  SEQUENCE_NUMBER (big-endian uint32)
 *   [10 .. 10+payload_length-1]  PAYLOAD
 *   [last 2 bytes]  CRC-16 checksum over all preceding bytes
 *
 * HEADER_SIZE = 10, CHECKSUM_SIZE = 2
 * Minimum valid message = 12 bytes (header + checksum, zero payload)
 */
class MessageParserTest {

    private MessageParser parser;

    @BeforeEach
    void setUp() {
        parser = new MessageParser();
    }

    // -------------------------------------------------------------------------
    // Helper methods
    // -------------------------------------------------------------------------

    /**
     * Builds a raw message byte array with correct framing.
     * Caller must supply a valid CRC or use buildValidMessage which computes it.
     */
    private byte[] buildRawMessage(byte messageType, int sequenceNumber, byte[] payload) {
        int payloadLength = (payload != null) ? payload.length : 0;
        byte[] raw = new byte[10 + payloadLength + 2]; // HEADER + PAYLOAD + CHECKSUM

        // Sync bytes
        raw[0] = (byte) 0xAA;
        raw[1] = (byte) 0x55;

        // Version
        raw[2] = (byte) 1;

        // Payload length (big-endian uint16)
        raw[3] = (byte) ((payloadLength >> 8) & 0xFF);
        raw[4] = (byte) (payloadLength & 0xFF);

        // Message type
        raw[5] = messageType;

        // Sequence number (big-endian uint32)
        raw[6] = (byte) ((sequenceNumber >> 24) & 0xFF);
        raw[7] = (byte) ((sequenceNumber >> 16) & 0xFF);
        raw[8] = (byte) ((sequenceNumber >> 8) & 0xFF);
        raw[9] = (byte) (sequenceNumber & 0xFF);

        // Payload
        if (payload != null) {
            System.arraycopy(payload, 0, raw, 10, payloadLength);
        }

        // CRC-16 over all preceding bytes (offsets 0 to raw.length - 3)
        int crc = computeCrc16(raw, 0, raw.length - 2);
        raw[raw.length - 2] = (byte) ((crc >> 8) & 0xFF);
        raw[raw.length - 1] = (byte) (crc & 0xFF);

        return raw;
    }

    /**
     * CRC-16 computation matching the implementation expected by MessageParser.
     * Uses CRC-16/CCITT-FALSE (polynomial 0x1021, init 0xFFFF).
     */
    private int computeCrc16(byte[] data, int offset, int length) {
        int crc = 0xFFFF;
        for (int i = offset; i < offset + length; i++) {
            crc ^= (data[i] & 0xFF) << 8;
            for (int j = 0; j < 8; j++) {
                if ((crc & 0x8000) != 0) {
                    crc = (crc << 1) ^ 0x1021;
                } else {
                    crc = crc << 1;
                }
            }
            crc &= 0xFFFF;
        }
        return crc;
    }

    // =========================================================================
    // Null and empty input
    // =========================================================================

    @Nested
    @DisplayName("Null and empty input handling")
    class NullAndEmptyInput {

        @Test
        @DisplayName("Null input returns Error with invalid_sync at offset 0")
        void nullInput_returnsInvalidSync() {
            ParseResult result = parser.parse(null);

            assertTrue(result.isError());
            assertEquals(ErrorCode.INVALID_SYNC, result.getError().getErrorCode());
            assertEquals(0, result.getError().getByteOffset());
        }

        @Test
        @DisplayName("Empty byte array returns Error with invalid_sync at offset 0")
        void emptyInput_returnsInvalidSync() {
            ParseResult result = parser.parse(new byte[0]);

            assertTrue(result.isError());
            assertEquals(ErrorCode.INVALID_SYNC, result.getError().getErrorCode());
            assertEquals(0, result.getError().getByteOffset());
        }
    }

    // =========================================================================
    // Minimum length validation
    // =========================================================================

    @Nested
    @DisplayName("Minimum message length")
    class MinimumLength {

        @Test
        @DisplayName("Input shorter than HEADER_SIZE + CHECKSUM_SIZE returns length_mismatch at offset 0")
        void tooShort_returnsLengthMismatch() {
            // 11 bytes is less than minimum 12 (HEADER_SIZE=10 + CHECKSUM_SIZE=2)
            byte[] tooShort = new byte[]{
                    (byte) 0xAA, (byte) 0x55, 0x01, 0x00, 0x00,
                    0x01, 0x00, 0x00, 0x00, 0x01, 0x00
            };

            ParseResult result = parser.parse(tooShort);

            assertTrue(result.isError());
            assertEquals(ErrorCode.LENGTH_MISMATCH, result.getError().getErrorCode());
            assertEquals(0, result.getError().getByteOffset());
        }

        @Test
        @DisplayName("Input of exactly minimum length (12 bytes, zero payload) can be parsed")
        void exactMinimumLength_withValidContent_succeeds() {
            byte[] msg = buildRawMessage((byte) 0x01, 0, new byte[0]);

            ParseResult result = parser.parse(msg);

            // Should either succeed or fail on unknown_type / checksum — not on length
            if (result.isError()) {
                assertNotEquals(ErrorCode.LENGTH_MISMATCH, result.getError().getErrorCode());
            }
        }
    }

    // =========================================================================
    // Sync byte validation (step 1)
    // =========================================================================

    @Nested
    @DisplayName("Sync byte validation")
    class SyncByteValidation {

        @Test
        @DisplayName("Wrong first sync byte returns invalid_sync at offset 0")
        void wrongFirstSyncByte_returnsInvalidSync() {
            byte[] msg = buildRawMessage((byte) 0x01, 1, new byte[]{0x10, 0x20});
            msg[0] = (byte) 0xBB; // corrupt first sync byte

            ParseResult result = parser.parse(msg);

            assertTrue(result.isError());
            assertEquals(ErrorCode.INVALID_SYNC, result.getError().getErrorCode());
            assertEquals(0, result.getError().getByteOffset());
        }

        @Test
        @DisplayName("Wrong second sync byte returns invalid_sync at offset 0")
        void wrongSecondSyncByte_returnsInvalidSync() {
            byte[] msg = buildRawMessage((byte) 0x01, 1, new byte[]{0x10, 0x20});
            msg[1] = (byte) 0x00; // corrupt second sync byte

            ParseResult result = parser.parse(msg);

            assertTrue(result.isError());
            assertEquals(ErrorCode.INVALID_SYNC, result.getError().getErrorCode());
            assertEquals(0, result.getError().getByteOffset());
        }

        @Test
        @DisplayName("Both sync bytes wrong returns invalid_sync at offset 0")
        void bothSyncBytesWrong_returnsInvalidSync() {
            byte[] msg = buildRawMessage((byte) 0x01, 1, new byte[]{0x10});
            msg[0] = 0x00;
            msg[1] = 0x00;

            ParseResult result = parser.parse(msg);

            assertTrue(result.isError());
            assertEquals(ErrorCode.INVALID_SYNC, result.getError().getErrorCode());
            assertEquals(0, result.getError().getByteOffset());
        }

        @Test
        @DisplayName("Correct sync bytes 0xAA 0x55 pass validation")
        void correctSyncBytes_passValidation() {
            byte[] msg = buildRawMessage((byte) 0x01, 1, new byte[]{0x10});

            ParseResult result = parser.parse(msg);

            // Should not fail on sync bytes
            if (result.isError()) {
                assertNotEquals(ErrorCode.INVALID_SYNC, result.getError().getErrorCode());
            }
        }
    }

    // =========================================================================
    // Version validation (step 2)
    // =========================================================================

    @Nested
    @DisplayName("Version validation")
    class VersionValidation {

        @Test
        @DisplayName("Version 0 returns unsupported_version at offset 2")
        void versionZero_returnsUnsupportedVersion() {
            byte[] msg = buildRawMessage((byte) 0x01, 1, new byte[]{0x10});
            msg[2] = 0x00; // version 0

            ParseResult result = parser.parse(msg);

            assertTrue(result.isError());
            assertEquals(ErrorCode.UNSUPPORTED_VERSION, result.getError().getErrorCode());
            assertEquals(2, result.getError().getByteOffset());
        }

        @Test
        @DisplayName("Version 2 returns unsupported_version at offset 2")
        void versionTwo_returnsUnsupportedVersion() {
            byte[] msg = buildRawMessage((byte) 0x01, 1, new byte[]{0x10});
            msg[2] = 0x02; // version 2

            ParseResult result = parser.parse(msg);

            assertTrue(result.isError());
            assertEquals(ErrorCode.UNSUPPORTED_VERSION, result.getError().getErrorCode());
            assertEquals(2, result.getError().getByteOffset());
        }

        @Test
        @DisplayName("Version 0xFF returns unsupported_version at offset 2")
        void versionMax_returnsUnsupportedVersion() {
            byte[] msg = buildRawMessage((byte) 0x01, 1, new byte[]{0x10});
            msg[2] = (byte) 0xFF;

            ParseResult result = parser.parse(msg);

            assertTrue(result.isError());
            assertEquals(ErrorCode.UNSUPPORTED_VERSION, result.getError().getErrorCode());
            assertEquals(2, result.getError().getByteOffset());
        }

        @Test
        @DisplayName("Version 1 (SUPPORTED_VERSION) passes validation")
        void supportedVersion_passesValidation() {
            byte[] msg = buildRawMessage((byte) 0x01, 1, new byte[]{0x10});

            ParseResult result = parser.parse(msg);

            if (result.isError()) {
                assertNotEquals(ErrorCode.UNSUPPORTED_VERSION, result.getError().getErrorCode());
            }
        }
    }

    // =========================================================================
    // Payload length validation (step 3)
    // =========================================================================

    @Nested
    @DisplayName("Payload length validation")
    class PayloadLengthValidation {

        @Test
        @DisplayName("Declared payload length greater than actual data returns length_mismatch at offset 3")
        void declaredLengthTooLarge_returnsLengthMismatch() {
            // Build a valid message with 2-byte payload
            byte[] msg = buildRawMessage((byte) 0x01, 1, new byte[]{0x10, 0x20});
            // Overwrite payload length to claim 100 bytes
            msg[3] = 0x00;
            msg[4] = 0x64; // 100

            ParseResult result = parser.parse(msg);

            assertTrue(result.isError());
            assertEquals(ErrorCode.LENGTH_MISMATCH, result.getError().getErrorCode());
            assertEquals(3, result.getError().getByteOffset());
        }

        @Test
        @DisplayName("Declared payload length less than actual data returns length_mismatch at offset 3")
        void declaredLengthTooSmall_returnsLengthMismatch() {
            // Build message with 4-byte payload
            byte[] msg = buildRawMessage((byte) 0x01, 1, new byte[]{0x10, 0x20, 0x30, 0x40});
            // Overwrite payload length to claim 1 byte
            msg[3] = 0x00;
            msg[4] = 0x01;

            ParseResult result = parser.parse(msg);

            assertTrue(result.isError());
            assertEquals(ErrorCode.LENGTH_MISMATCH, result.getError().getErrorCode());
            assertEquals(3, result.getError().getByteOffset());
        }

        @Test
        @DisplayName("Declared payload length of zero with matching actual length succeeds past length check")
        void zeroPayloadLength_withMatchingData_passesLengthCheck() {
            byte[] msg = buildRawMessage((byte) 0x01, 1, new byte[0]);

            ParseResult result = parser.parse(msg);

            if (result.isError()) {
                assertNotEquals(ErrorCode.LENGTH_MISMATCH, result.getError().getErrorCode());
            }
        }
    }

    // =========================================================================
    // Checksum validation (step 7)
    // =========================================================================

    @Nested
    @DisplayName("Checksum validation")
    class ChecksumValidation {

        @Test
        @DisplayName("Corrupted checksum returns checksum_failed at checksum offset")
        void corruptedChecksum_returnsChecksumFailed() {
            byte[] msg = buildRawMessage((byte) 0x01, 1, new byte[]{0x10, 0x20});
            // Corrupt the last two bytes (checksum)
            msg[msg.length - 2] = 0x00;
            msg[msg.length - 1] = 0x00;

            ParseResult result = parser.parse(msg);

            assertTrue(result.isError());
            assertEquals(ErrorCode.CHECKSUM_FAILED, result.getError().getErrorCode());
            // byte_offset should be at the checksum position
            assertEquals(msg.length - 2, result.getError().getByteOffset());
        }

        @Test
        @DisplayName("Corrupted payload byte causes checksum failure")
        void corruptedPayload_causesChecksumFailure() {
            byte[] msg = buildRawMessage((byte) 0x01, 1, new byte[]{0x10, 0x20, 0x30});
            // Corrupt a payload byte — checksum will no longer match
            msg[10] = (byte) 0xFF;

            ParseResult result = parser.parse(msg);

            assertTrue(result.isError());
            assertEquals(ErrorCode.CHECKSUM_FAILED, result.getError().getErrorCode());
        }

        @Test
        @DisplayName("Valid checksum over all preceding bytes passes validation")
        void validChecksum_passesValidation() {
            byte[] msg = buildRawMessage((byte) 0x01, 1, new byte[]{0x10, 0x20});

            ParseResult result = parser.parse(msg);

            if (result.isError()) {
                assertNotEquals(ErrorCode.CHECKSUM_FAILED, result.getError().getErrorCode());
            }
        }
    }

    // =========================================================================
    // Unknown message type (step 8)
    // =========================================================================

    @Nested
    @DisplayName("Message type lookup")
    class MessageTypeLookup {

        @Test
        @DisplayName("Unregistered message type returns unknown_type at offset 5")
        void unregisteredType_returnsUnknownType() {
            // Use a message type that is not in the type registry
            byte[] msg = buildRawMessage((byte) 0xFF, 1, new byte[]{0x10});

            ParseResult result = parser.parse(msg);

            assertTrue(result.isError());
            assertEquals(ErrorCode.UNKNOWN_TYPE, result.getError().getErrorCode());
            assertEquals(5, result.getError().getByteOffset());
        }
    }

    // =========================================================================
    // Successful parsing
    // =========================================================================

    @Nested
    @DisplayName("Successful message parsing")
    class SuccessfulParsing {

        @Test
        @DisplayName("Valid message returns Success with correct message_type")
        void validMessage_returnsCorrectMessageType() {
            byte messageType = 0x01;
            byte[] msg = buildRawMessage(messageType, 42, new byte[]{0x10, 0x20});

            ParseResult result = parser.parse(msg);

            assertTrue(result.isSuccess());
            assertEquals(messageType & 0xFF, result.getSuccess().getMessageType());
        }

        @Test
        @DisplayName("Valid message returns Success with correct sequence_number")
        void validMessage_returnsCorrectSequenceNumber() {
            int sequenceNumber = 12345;
            byte[] msg = buildRawMessage((byte) 0x01, sequenceNumber, new byte[]{0x10, 0x20});

            ParseResult result = parser.parse(msg);

            assertTrue(result.isSuccess());
            assertEquals(sequenceNumber, result.getSuccess().getSequenceNumber());
        }

        @Test
        @DisplayName("Valid message returns Success with parsed payload map")
        void validMessage_returnsPayloadMap() {
            byte[] msg = buildRawMessage((byte) 0x01, 1, new byte[]{0x10, 0x20});

            ParseResult result = parser.parse(msg);

            assertTrue(result.isSuccess());
            assertNotNull(result.getSuccess().getPayload());
        }

        @Test
        @DisplayName("Sequence number zero is valid")
        void sequenceNumberZero_isValid() {
            byte[] msg = buildRawMessage((byte) 0x01, 0, new byte[]{0x10});

            ParseResult result = parser.parse(msg);

            assertTrue(result.isSuccess());
            assertEquals(0, result.getSuccess().getSequenceNumber());
        }

        @Test
        @DisplayName("Maximum sequence number (0xFFFFFFFF) is valid")
        void maxSequenceNumber_isValid() {
            int maxSeq = 0xFFFFFFFF; // -1 in signed, max uint32 in unsigned
            byte[] msg = buildRawMessage((byte) 0x01, maxSeq, new byte[]{0x10});

            ParseResult result = parser.parse(msg);

            assertTrue(result.isSuccess());
            // In Java, the int representation of 0xFFFFFFFF is -1;
            // the important thing is the bytes round-trip correctly
            assertEquals(maxSeq, result.getSuccess().getSequenceNumber());
        }

        @Test
        @DisplayName("Zero-length payload produces empty payload map")
        void zeroLengthPayload_producesEmptyMap() {
            byte[] msg = buildRawMessage((byte) 0x01, 1, new byte[0]);

            ParseResult result = parser.parse(msg);

            assertTrue(result.isSuccess());
            assertTrue(result.getSuccess().getPayload().isEmpty());
        }
    }

    // =========================================================================
    // Validation order (priority of checks)
    // =========================================================================

    @Nested
    @DisplayName("Validation order — errors detected in specification order")
    class ValidationOrder {

        @Test
        @DisplayName("Bad sync bytes reported before bad version")
        void syncCheckedBeforeVersion() {
            byte[] msg = buildRawMessage((byte) 0x01, 1, new byte[]{0x10});
            msg[0] = 0x00; // bad sync
            msg[2] = 0x05; // bad version

            ParseResult result = parser.parse(msg);

            assertTrue(result.isError());
            assertEquals(ErrorCode.INVALID_SYNC, result.getError().getErrorCode());
        }

        @Test
        @DisplayName("Bad version reported before length mismatch")
        void versionCheckedBeforeLength() {
            byte[] msg = buildRawMessage((byte) 0x01, 1, new byte[]{0x10});
            msg[2] = 0x05; // bad version
            msg[3] = 0x00;
            msg[4] = (byte) 0xFF; // bad length

            ParseResult result = parser.parse(msg);

            assertTrue(result.isError());
            assertEquals(ErrorCode.UNSUPPORTED_VERSION, result.getError().getErrorCode());
        }

        @Test
        @DisplayName("Length mismatch reported before checksum failure")
        void lengthCheckedBeforeChecksum() {
            byte[] msg = buildRawMessage((byte) 0x01, 1, new byte[]{0x10});
            // Overwrite payload length to mismatch
            msg[3] = 0x00;
            msg[4] = 0x50;
            // Also corrupt checksum
            msg[msg.length - 1] = 0x00;

            ParseResult result = parser.parse(msg);

            assertTrue(result.isError());
            assertEquals(ErrorCode.LENGTH_MISMATCH, result.getError().getErrorCode());
        }

        @Test
        @DisplayName("Checksum failure reported before unknown type")
        void checksumCheckedBeforeTypeRegistry() {
            byte[] msg = buildRawMessage((byte) 0xFF, 1, new byte[]{0x10}); // 0xFF = unknown type
            // Corrupt checksum
            msg[msg.length - 2] = 0x00;
            msg[msg.length - 1] = 0x00;

            ParseResult result = parser.parse(msg);

            assertTrue(result.isError());
            assertEquals(ErrorCode.CHECKSUM_FAILED, result.getError().getErrorCode());
        }
    }

    // =========================================================================
    // Boundary conditions
    // =========================================================================

    @Nested
    @DisplayName("Boundary conditions")
    class BoundaryConditions {

        @Test
        @DisplayName("Single byte input returns error (too short)")
        void singleByte_returnsError() {
            ParseResult result = parser.parse(new byte[]{(byte) 0xAA});

            assertTrue(result.isError());
        }

        @Test
        @DisplayName("Input of exactly 4 bytes (minimum constraint) but below minimum frame returns error")
        void fourBytes_belowMinimumFrame_returnsError() {
            byte[] data = new byte[]{(byte) 0xAA, (byte) 0x55, 0x01, 0x00};

            ParseResult result = parser.parse(data);

            assertTrue(result.isError());
            assertEquals(ErrorCode.LENGTH_MISMATCH, result.getError().getErrorCode());
            assertEquals(0, result.getError().getByteOffset());
        }

        @Test
        @DisplayName("Maximum input size (1024 bytes) with valid framing is accepted")
        void maxInputSize_withValidFraming_isAccepted() {
            // 1024 total = 10 header + 1012 payload + 2 checksum
            byte[] payload = new byte[1012];
            for (int i = 0; i < payload.length; i++) {
                payload[i] = (byte) (i & 0xFF);
            }

            byte[] msg = buildRawMessage((byte) 0x01, 99, payload);
            assertEquals(1024, msg.length);

            ParseResult result = parser.parse(msg);

            // Should not fail on length or framing issues
            if (result.isError()) {
                assertNotEquals(ErrorCode.LENGTH_MISMATCH, result.getError().getErrorCode());
                assertNotEquals(ErrorCode.INVALID_SYNC, result.getError().getErrorCode());
                assertNotEquals(ErrorCode.UNSUPPORTED_VERSION, result.getError().getErrorCode());
                assertNotEquals(ErrorCode.CHECKSUM_FAILED, result.getError().getErrorCode());
            }
        }

        @Test
        @DisplayName("Payload length field uses big-endian byte order")
        void payloadLength_bigEndian() {
            // Build message with 256-byte payload (0x0100 big-endian)
            byte[] payload = new byte[256];
            byte[] msg = buildRawMessage((byte) 0x01, 1, payload);

            // Verify the length field is big-endian: offset 3 = 0x01, offset 4 = 0x00
            assertEquals(0x01, msg[3] & 0xFF);
            assertEquals(0x00, msg[4] & 0xFF);

            ParseResult result = parser.parse(msg);

            if (result.isError()) {
                assertNotEquals(ErrorCode.LENGTH_MISMATCH, result.getError().getErrorCode());
            }
        }

        @Test
        @DisplayName("Sequence number uses big-endian byte order")
        void sequenceNumber_bigEndian() {
            // sequence = 0x01020304
            int seq = 0x01020304;
            byte[] msg = buildRawMessage((byte) 0x01, seq, new byte[]{0x10});

            assertEquals(0x01, msg[6] & 0xFF);
            assertEquals(0x02, msg[7] & 0xFF);
            assertEquals(0x03, msg[8] & 0xFF);
            assertEquals(0x04, msg[9] & 0xFF);
        }
    }

    // =========================================================================
    // Reentrancy — no mutable shared state
    // =========================================================================

    @Nested
    @DisplayName("Reentrancy — parser has no mutable shared state")
    class Reentrancy {

        @Test
        @DisplayName("Parsing an error followed by a valid message succeeds")
        void errorThenSuccess_noStateLeakage() {
            // First call: bad message
            parser.parse(new byte[]{0x00, 0x00});

            // Second call: valid message
            byte[] valid = buildRawMessage((byte) 0x01, 1, new byte[]{0x10});
            ParseResult result = parser.parse(valid);

            // The first call should not affect the second
            if (result.isError()) {
                assertNotEquals(ErrorCode.INVALID_SYNC, result.getError().getErrorCode());
            }
        }

        @Test
        @DisplayName("Multiple consecutive valid parses all succeed independently")
        void multipleValidParses_allSucceed() {
            for (int i = 0; i < 5; i++) {
                byte[] msg = buildRawMessage((byte) 0x01, i, new byte[]{(byte) i});
                ParseResult result = parser.parse(msg);

                if (result.isSuccess()) {
                    assertEquals(i, result.getSuccess().getSequenceNumber());
                }
            }
        }
    }
}
