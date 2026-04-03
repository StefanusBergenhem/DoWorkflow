package com.commprotocol;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for MessageParser — derived from detailed design DD-007.
 *
 * Derivation strategies applied:
 *   1. Requirement-based: one test per behavior step and on_failure branch
 *   2. Equivalence class partitioning: valid/invalid classes for raw_data
 *   3. Boundary value analysis: raw_data length boundaries (4, 12, 1024),
 *      sequence_number (0, 0xFFFFFFFF), message_type (0, 255)
 *   4. Error handling / fault injection: null, empty, too-short, corrupted data,
 *      error precedence verification
 *
 * Configuration constants (from design):
 *   SYNC_BYTES       = {0xAA, 0x55}
 *   SUPPORTED_VERSION = 1
 *   HEADER_SIZE      = 10
 *   CHECKSUM_SIZE    = 2
 *   Minimum valid message = HEADER_SIZE + CHECKSUM_SIZE = 12 bytes
 *
 * Header layout:
 *   [0]    0xAA         sync byte 1
 *   [1]    0x55         sync byte 2
 *   [2]    version
 *   [3-4]  payload length (big-endian uint16)
 *   [5]    message type
 *   [6-9]  sequence number (big-endian uint32)
 *   [10 .. 10+payloadLen-1]  payload bytes
 *   [last 2 bytes]           CRC-16 checksum over all preceding bytes
 *
 * Note on CRC helper: The test helper computeCrc16() is used ONLY to construct
 * valid test data. It is never used in assertions — all expected values in
 * assertions come from the design specification, not from recomputation. This
 * avoids the "mirror test" anti-pattern.
 */
class MessageParserTest {

    private MessageParser parser;

    @BeforeEach
    void setUp() {
        parser = new MessageParser();
    }

    // -----------------------------------------------------------------------
    // Helper: builds a valid raw message byte array with correct CRC.
    // Used to construct test data; the CRC helper is NOT used in assertions.
    // -----------------------------------------------------------------------

    private byte[] buildValidMessage(int messageType, long sequenceNumber, byte[] payload) {
        int payloadLen = (payload == null) ? 0 : payload.length;
        int totalLen = 10 + payloadLen + 2; // HEADER_SIZE + payload + CHECKSUM_SIZE
        byte[] msg = new byte[totalLen];

        // Sync bytes
        msg[0] = (byte) 0xAA;
        msg[1] = (byte) 0x55;

        // Version
        msg[2] = 1; // SUPPORTED_VERSION

        // Payload length (big-endian uint16)
        msg[3] = (byte) ((payloadLen >> 8) & 0xFF);
        msg[4] = (byte) (payloadLen & 0xFF);

        // Message type
        msg[5] = (byte) (messageType & 0xFF);

        // Sequence number (big-endian uint32)
        msg[6] = (byte) ((sequenceNumber >> 24) & 0xFF);
        msg[7] = (byte) ((sequenceNumber >> 16) & 0xFF);
        msg[8] = (byte) ((sequenceNumber >> 8) & 0xFF);
        msg[9] = (byte) (sequenceNumber & 0xFF);

        // Payload
        if (payload != null) {
            System.arraycopy(payload, 0, msg, 10, payloadLen);
        }

        // CRC-16 over all preceding bytes (indices 0 .. totalLen-3)
        int crc = computeCrc16(msg, 0, totalLen - 2);
        msg[totalLen - 2] = (byte) ((crc >> 8) & 0xFF);
        msg[totalLen - 1] = (byte) (crc & 0xFF);

        return msg;
    }

    /**
     * CRC-16/CCITT-FALSE (poly 0x1021, init 0xFFFF).
     * Used ONLY for building test data, never for computing expected assertion values.
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

    // =======================================================================
    // Strategy 1: Requirement-Based Tests (from behavior section)
    // =======================================================================

    @Nested
    @DisplayName("Behavior: Sync byte validation (step 1)")
    class SyncByteValidation {

        @Test
        @DisplayName("rejects message with wrong first sync byte")
        void rejectsWrongFirstSyncByte() {
            // Arrange: valid message but first sync byte is 0xBB instead of 0xAA
            byte[] msg = buildValidMessage(1, 100, new byte[]{0x01, 0x02});
            msg[0] = (byte) 0xBB;

            // Act
            ParseResult result = parser.parse(msg);

            // Assert: per design step 1 on_failure — invalid_sync at offset 0
            assertTrue(result.isError());
            assertEquals("invalid_sync", result.getErrorCode());
            assertEquals(0, result.getByteOffset());
        }

        @Test
        @DisplayName("rejects message with wrong second sync byte")
        void rejectsWrongSecondSyncByte() {
            // Arrange
            byte[] msg = buildValidMessage(1, 100, new byte[]{0x01, 0x02});
            msg[1] = (byte) 0x00;

            // Act
            ParseResult result = parser.parse(msg);

            // Assert
            assertTrue(result.isError());
            assertEquals("invalid_sync", result.getErrorCode());
            assertEquals(0, result.getByteOffset());
        }

        @Test
        @DisplayName("rejects message with both sync bytes wrong")
        void rejectsBothSyncBytesWrong() {
            // Arrange
            byte[] msg = buildValidMessage(1, 100, new byte[]{0x01});
            msg[0] = (byte) 0x00;
            msg[1] = (byte) 0x00;

            // Act
            ParseResult result = parser.parse(msg);

            // Assert
            assertTrue(result.isError());
            assertEquals("invalid_sync", result.getErrorCode());
            assertEquals(0, result.getByteOffset());
        }
    }

    @Nested
    @DisplayName("Behavior: Version validation (step 2)")
    class VersionValidation {

        @Test
        @DisplayName("rejects unsupported protocol version")
        void rejectsUnsupportedVersion() {
            // Arrange: valid message but version byte set to 99
            byte[] msg = buildValidMessage(1, 100, new byte[]{0x01});
            msg[2] = 99;

            // Act
            ParseResult result = parser.parse(msg);

            // Assert: per design step 2 on_failure — unsupported_version at offset 2
            assertTrue(result.isError());
            assertEquals("unsupported_version", result.getErrorCode());
            assertEquals(2, result.getByteOffset());
        }

        @Test
        @DisplayName("rejects version zero")
        void rejectsVersionZero() {
            // Arrange
            byte[] msg = buildValidMessage(1, 100, new byte[]{0x01});
            msg[2] = 0;

            // Act
            ParseResult result = parser.parse(msg);

            // Assert
            assertTrue(result.isError());
            assertEquals("unsupported_version", result.getErrorCode());
            assertEquals(2, result.getByteOffset());
        }
    }

    @Nested
    @DisplayName("Behavior: Payload length validation (step 3)")
    class PayloadLengthValidation {

        @Test
        @DisplayName("rejects message when declared payload length exceeds actual data")
        void rejectsDeclaredLengthExceedsActualData() {
            // Arrange: build valid 12-byte message (0-byte payload) but set
            // payload length field to 100 — actual data is way too short
            byte[] msg = buildValidMessage(1, 100, new byte[0]);
            msg[3] = 0;
            msg[4] = 100; // claims 100 bytes payload but total is only 12

            // Act
            ParseResult result = parser.parse(msg);

            // Assert: per design step 3 on_failure — length_mismatch at offset 3
            assertTrue(result.isError());
            assertEquals("length_mismatch", result.getErrorCode());
            assertEquals(3, result.getByteOffset());
        }

        @Test
        @DisplayName("rejects message when declared payload length is less than actual data")
        void rejectsDeclaredLengthLessThanActualData() {
            // Arrange: build message with 4 payload bytes but set length field to 1
            byte[] msg = buildValidMessage(1, 100, new byte[]{0x01, 0x02, 0x03, 0x04});
            msg[3] = 0;
            msg[4] = 1; // claims 1 byte payload but message has 4

            // Act
            ParseResult result = parser.parse(msg);

            // Assert
            assertTrue(result.isError());
            assertEquals("length_mismatch", result.getErrorCode());
            assertEquals(3, result.getByteOffset());
        }
    }

    @Nested
    @DisplayName("Behavior: Checksum validation (step 7)")
    class ChecksumValidation {

        @Test
        @DisplayName("rejects message with corrupted checksum")
        void rejectsCorruptedChecksum() {
            // Arrange: build valid message then flip all bits in checksum
            byte[] msg = buildValidMessage(1, 100, new byte[]{0x01, 0x02, 0x03});
            int checksumPos = msg.length - 2;
            msg[checksumPos] = (byte) ~msg[checksumPos];
            msg[checksumPos + 1] = (byte) ~msg[checksumPos + 1];

            // Act
            ParseResult result = parser.parse(msg);

            // Assert: per design step 7 on_failure — checksum_failed at checksum position
            assertTrue(result.isError());
            assertEquals("checksum_failed", result.getErrorCode());
            assertEquals(checksumPos, result.getByteOffset());
        }

        @Test
        @DisplayName("rejects message with zeroed checksum")
        void rejectsZeroedChecksum() {
            // Arrange
            byte[] msg = buildValidMessage(1, 100, new byte[]{0x01});
            int checksumPos = msg.length - 2;
            msg[checksumPos] = 0x00;
            msg[checksumPos + 1] = 0x00;

            // Act
            ParseResult result = parser.parse(msg);

            // Assert
            assertTrue(result.isError());
            assertEquals("checksum_failed", result.getErrorCode());
            assertEquals(checksumPos, result.getByteOffset());
        }
    }

    @Nested
    @DisplayName("Behavior: Type registry lookup (step 8)")
    class TypeRegistryLookup {

        @Test
        @DisplayName("rejects message with unknown message type")
        void rejectsUnknownMessageType() {
            // Arrange: build valid message with type ID 0xFF — not in registry
            byte[] msg = buildValidMessage(0xFF, 100, new byte[]{0x01});

            // Act
            ParseResult result = parser.parse(msg);

            // Assert: per design step 8 on_failure — unknown_type at offset 5
            assertTrue(result.isError());
            assertEquals("unknown_type", result.getErrorCode());
            assertEquals(5, result.getByteOffset());
        }
    }

    @Nested
    @DisplayName("Behavior: Successful parse (steps 4-6, 9-10)")
    class SuccessfulParse {

        @Test
        @DisplayName("parses valid message and returns correct message type")
        void parsesCorrectMessageType() {
            // Arrange: message type 1
            byte[] msg = buildValidMessage(1, 42, new byte[]{0x10, 0x20});

            // Act
            ParseResult result = parser.parse(msg);

            // Assert: design step 4 — message_type read from offset 5
            assertTrue(result.isSuccess());
            assertEquals(1, result.getMessageType());
        }

        @Test
        @DisplayName("parses valid message and returns correct sequence number")
        void parsesCorrectSequenceNumber() {
            // Arrange: sequence number = 0x12345678 = 305419896
            byte[] msg = buildValidMessage(1, 0x12345678L, new byte[]{0x10});

            // Act
            ParseResult result = parser.parse(msg);

            // Assert: design step 5 — big-endian uint32 at offset 6-9
            assertTrue(result.isSuccess());
            assertEquals(0x12345678L, result.getSequenceNumber());
        }

        @Test
        @DisplayName("parses payload bytes into typed field map per type registry")
        void parsesPayloadIntoTypedFieldMap() {
            // Arrange: message type 1 with payload {0x00, 0x0A} — per type registry
            // for type 1, this should decode to a field with value 10 (big-endian uint16).
            // The specific field name depends on the type registry definition for type 1.
            byte[] payload = new byte[]{0x00, 0x0A};
            byte[] msg = buildValidMessage(1, 1, payload);

            // Act
            ParseResult result = parser.parse(msg);

            // Assert: design steps 8-9 — payload parsed into map with typed fields
            assertTrue(result.isSuccess());
            Map<String, Object> payloadMap = result.getPayload();
            assertNotNull(payloadMap);
            // Verify the payload was actually parsed — the map must contain at least
            // the field(s) defined in the type registry for message type 1.
            // Using containsValue(10) because the design says fields are typed, and
            // 0x000A as uint16 = 10. The exact key depends on the type registry.
            assertFalse(payloadMap.isEmpty(),
                    "Payload map should contain parsed fields per type registry for type 1");
            assertTrue(payloadMap.containsValue(10),
                    "Payload should contain uint16 value 10 decoded from bytes {0x00, 0x0A}");
        }

        @Test
        @DisplayName("parses message with zero-length payload into empty map")
        void parsesZeroLengthPayloadIntoEmptyMap() {
            // Arrange: valid message with empty payload
            byte[] msg = buildValidMessage(1, 1, new byte[0]);

            // Act
            ParseResult result = parser.parse(msg);

            // Assert: design step 10 — returns Success with empty payload map
            assertTrue(result.isSuccess());
            assertNotNull(result.getPayload());
            assertTrue(result.getPayload().isEmpty(),
                    "Zero-length payload should produce empty map");
        }

        @Test
        @DisplayName("returns all three success fields together")
        void returnsAllSuccessFields() {
            // Arrange: known message type 1, sequence 999, 2-byte payload
            byte[] msg = buildValidMessage(1, 999, new byte[]{0x00, 0x05});

            // Act
            ParseResult result = parser.parse(msg);

            // Assert: design step 10 — Success with message_type, payload, sequence_number
            assertTrue(result.isSuccess());
            assertEquals(1, result.getMessageType());
            assertEquals(999, result.getSequenceNumber());
            assertNotNull(result.getPayload());
        }
    }

    // =======================================================================
    // Strategy 2: Equivalence Class Partitioning (from interfaces)
    // =======================================================================

    @Nested
    @DisplayName("Equivalence classes for raw_data input")
    class RawDataEquivalenceClasses {

        @Test
        @DisplayName("valid: nominal message with typical payload size")
        void validNominalMessage() {
            // Representative of "valid, typical length" equivalence class
            byte[] payload = new byte[20];
            for (int i = 0; i < payload.length; i++) {
                payload[i] = (byte) i;
            }
            byte[] msg = buildValidMessage(1, 500, payload);

            // Act
            ParseResult result = parser.parse(msg);

            // Assert
            assertTrue(result.isSuccess());
            assertEquals(1, result.getMessageType());
            assertEquals(500, result.getSequenceNumber());
        }

        @Test
        @DisplayName("invalid: null raw_data returns invalid_sync error")
        void invalidNullInput() {
            // Act
            ParseResult result = parser.parse(null);

            // Assert: per error_handling[0] — null/empty -> invalid_sync at offset 0
            assertTrue(result.isError());
            assertEquals("invalid_sync", result.getErrorCode());
            assertEquals(0, result.getByteOffset());
        }

        @Test
        @DisplayName("invalid: empty byte array returns invalid_sync error")
        void invalidEmptyInput() {
            // Act
            ParseResult result = parser.parse(new byte[0]);

            // Assert: per error_handling[0]
            assertTrue(result.isError());
            assertEquals("invalid_sync", result.getErrorCode());
            assertEquals(0, result.getByteOffset());
        }

        @Test
        @DisplayName("invalid: all-zero bytes of valid length fail at sync check")
        void invalidAllZeroBytes() {
            // Arrange: 12 zero bytes — sync bytes will be 0x00, not 0xAA/0x55
            byte[] msg = new byte[12];

            // Act
            ParseResult result = parser.parse(msg);

            // Assert: fails at sync byte check
            assertTrue(result.isError());
            assertEquals("invalid_sync", result.getErrorCode());
            assertEquals(0, result.getByteOffset());
        }
    }

    // =======================================================================
    // Strategy 3: Boundary Value Analysis (from interfaces constraints)
    // =======================================================================

    @Nested
    @DisplayName("Boundary values for raw_data length")
    class RawDataLengthBoundaries {

        @Test
        @DisplayName("below minimum constraint: 3 bytes (< 4)")
        void belowMinimumConstraint3Bytes() {
            // Arrange: 3 bytes — below the interface constraint minimum of 4
            byte[] msg = new byte[]{(byte) 0xAA, (byte) 0x55, 0x01};

            // Act
            ParseResult result = parser.parse(msg);

            // Assert: too short for valid message -> length_mismatch at offset 0
            assertTrue(result.isError());
            assertEquals("length_mismatch", result.getErrorCode());
            assertEquals(0, result.getByteOffset());
        }

        @Test
        @DisplayName("at minimum constraint: 4 bytes (valid per constraint, but < min valid msg)")
        void atMinimumConstraint4Bytes() {
            // Arrange: 4 bytes with valid sync — design says minimum valid is
            // HEADER_SIZE(10) + CHECKSUM_SIZE(2) = 12, so 4 bytes is too short
            byte[] msg = new byte[]{(byte) 0xAA, (byte) 0x55, 0x01, 0x00};

            // Act
            ParseResult result = parser.parse(msg);

            // Assert: per error_handling[1] — length_mismatch at offset 0
            assertTrue(result.isError());
            assertEquals("length_mismatch", result.getErrorCode());
            assertEquals(0, result.getByteOffset());
        }

        @Test
        @DisplayName("just below minimum valid message: 11 bytes")
        void justBelowMinimumValidMessage11Bytes() {
            // Arrange: 11 bytes — one short of min valid (HEADER_SIZE + CHECKSUM_SIZE = 12)
            byte[] msg = new byte[11];
            msg[0] = (byte) 0xAA;
            msg[1] = (byte) 0x55;
            msg[2] = 1;

            // Act
            ParseResult result = parser.parse(msg);

            // Assert: per error_handling[1]
            assertTrue(result.isError());
            assertEquals("length_mismatch", result.getErrorCode());
            assertEquals(0, result.getByteOffset());
        }

        @Test
        @DisplayName("at minimum valid message size: 12 bytes (0-byte payload)")
        void atMinimumValidMessageSize12Bytes() {
            // Arrange: exactly 12 bytes — smallest valid message with 0-byte payload
            byte[] msg = buildValidMessage(1, 0, new byte[0]);
            assertEquals(12, msg.length, "Sanity: helper should produce 12-byte message");

            // Act
            ParseResult result = parser.parse(msg);

            // Assert: should parse successfully
            assertTrue(result.isSuccess());
            assertEquals(1, result.getMessageType());
            assertEquals(0, result.getSequenceNumber());
        }

        @Test
        @DisplayName("at maximum constraint: 1024 bytes")
        void atMaximumConstraint1024Bytes() {
            // Arrange: 1024 bytes total -> payload = 1024 - 10 - 2 = 1012 bytes
            byte[] payload = new byte[1012];
            for (int i = 0; i < payload.length; i++) {
                payload[i] = (byte) (i & 0xFF);
            }
            byte[] msg = buildValidMessage(1, 999, payload);
            assertEquals(1024, msg.length, "Sanity: helper should produce 1024-byte message");

            // Act
            ParseResult result = parser.parse(msg);

            // Assert
            assertTrue(result.isSuccess());
            assertEquals(1, result.getMessageType());
            assertEquals(999, result.getSequenceNumber());
        }

        @Test
        @DisplayName("above maximum constraint: 1025 bytes rejected as length_mismatch")
        void aboveMaximumConstraint1025Bytes() {
            // Arrange: 1025 bytes total -> payload = 1013 bytes, exceeds constraint max 1024
            byte[] payload = new byte[1013];
            byte[] msg = buildValidMessage(1, 1, payload);
            assertEquals(1025, msg.length, "Sanity: helper should produce 1025-byte message");

            // Act
            ParseResult result = parser.parse(msg);

            // Assert: exceeds constraint "length 4..1024" -> length_mismatch
            assertTrue(result.isError());
            assertEquals("length_mismatch", result.getErrorCode());
        }
    }

    @Nested
    @DisplayName("Boundary values for sequence number")
    class SequenceNumberBoundaries {

        @Test
        @DisplayName("sequence number zero (minimum uint32)")
        void sequenceNumberZero() {
            // Arrange
            byte[] msg = buildValidMessage(1, 0, new byte[]{0x01});

            // Act
            ParseResult result = parser.parse(msg);

            // Assert
            assertTrue(result.isSuccess());
            assertEquals(0, result.getSequenceNumber());
        }

        @Test
        @DisplayName("sequence number 0xFFFFFFFF (maximum uint32 = 4294967295)")
        void sequenceNumberMaxUint32() {
            // Arrange
            byte[] msg = buildValidMessage(1, 0xFFFFFFFFL, new byte[]{0x01});

            // Act
            ParseResult result = parser.parse(msg);

            // Assert
            assertTrue(result.isSuccess());
            assertEquals(0xFFFFFFFFL, result.getSequenceNumber());
        }

        @Test
        @DisplayName("sequence number 1 (just above minimum)")
        void sequenceNumberOne() {
            // Arrange
            byte[] msg = buildValidMessage(1, 1, new byte[]{0x01});

            // Act
            ParseResult result = parser.parse(msg);

            // Assert
            assertTrue(result.isSuccess());
            assertEquals(1, result.getSequenceNumber());
        }
    }

    @Nested
    @DisplayName("Boundary values for message type")
    class MessageTypeBoundaries {

        @Test
        @DisplayName("message type zero rejects as unknown_type if not registered")
        void messageTypeZeroUnregistered() {
            // Arrange: type 0 — design does not list it as a registered type
            // The design says unregistered types return unknown_type at offset 5
            byte[] msg = buildValidMessage(0, 1, new byte[]{0x01});

            // Act
            ParseResult result = parser.parse(msg);

            // Assert: per design step 8 on_failure — unknown_type at offset 5
            assertTrue(result.isError());
            assertEquals("unknown_type", result.getErrorCode());
            assertEquals(5, result.getByteOffset());
        }

        @Test
        @DisplayName("message type 255 rejects as unknown_type if not registered")
        void messageType255Unregistered() {
            // Arrange: type 255 (max single byte) — not a registered type
            byte[] msg = buildValidMessage(255, 1, new byte[]{0x01});

            // Act
            ParseResult result = parser.parse(msg);

            // Assert: per design step 8 on_failure — unknown_type at offset 5
            assertTrue(result.isError());
            assertEquals("unknown_type", result.getErrorCode());
            assertEquals(5, result.getByteOffset());
        }
    }

    // =======================================================================
    // Strategy 4: Error Handling and Fault Injection
    // =======================================================================

    @Nested
    @DisplayName("Error handling: explicit conditions from design")
    class ExplicitErrorHandling {

        @Test
        @DisplayName("null input returns invalid_sync error at offset 0")
        void nullInputReturnsInvalidSync() {
            // Per error_handling[0]: "raw_data is null or empty"
            ParseResult result = parser.parse(null);

            assertTrue(result.isError());
            assertEquals("invalid_sync", result.getErrorCode());
            assertEquals(0, result.getByteOffset());
        }

        @Test
        @DisplayName("empty input returns invalid_sync error at offset 0")
        void emptyInputReturnsInvalidSync() {
            // Per error_handling[0]: "raw_data is null or empty"
            ParseResult result = parser.parse(new byte[0]);

            assertTrue(result.isError());
            assertEquals("invalid_sync", result.getErrorCode());
            assertEquals(0, result.getByteOffset());
        }

        @Test
        @DisplayName("input shorter than minimum valid size returns length_mismatch at offset 0")
        void tooShortInputReturnsLengthMismatch() {
            // Per error_handling[1]: length < HEADER_SIZE + CHECKSUM_SIZE
            // Using 8 bytes with valid sync and version but too short overall
            byte[] msg = new byte[]{
                    (byte) 0xAA, (byte) 0x55, 0x01,
                    0x00, 0x00, 0x01, 0x00, 0x00
            };

            ParseResult result = parser.parse(msg);

            assertTrue(result.isError());
            assertEquals("length_mismatch", result.getErrorCode());
            assertEquals(0, result.getByteOffset());
        }
    }

    @Nested
    @DisplayName("Fault injection: data corruption scenarios")
    class FaultInjection {

        @Test
        @DisplayName("single bit flip in payload corrupts checksum")
        void singleBitFlipInPayloadCorruptsChecksum() {
            // Arrange: valid message, then flip one bit in payload
            byte[] payload = new byte[]{0x01, 0x02, 0x03, 0x04};
            byte[] msg = buildValidMessage(1, 100, payload);
            msg[10] ^= 0x01; // flip LSB of first payload byte

            // Act
            ParseResult result = parser.parse(msg);

            // Assert: CRC should no longer match
            assertTrue(result.isError());
            assertEquals("checksum_failed", result.getErrorCode());
        }

        @Test
        @DisplayName("single bit flip in header sequence number corrupts checksum")
        void singleBitFlipInHeaderCorruptsChecksum() {
            // Arrange: valid message, then flip a bit in the sequence number field
            byte[] msg = buildValidMessage(1, 100, new byte[]{0x01});
            msg[8] ^= 0x80; // flip MSB of third sequence number byte

            // Act
            ParseResult result = parser.parse(msg);

            // Assert: CRC covers header, so checksum should fail
            assertTrue(result.isError());
            assertEquals("checksum_failed", result.getErrorCode());
        }

        @Test
        @DisplayName("truncated message missing checksum bytes triggers length_mismatch")
        void truncatedMessageMissingChecksumBytes() {
            // Arrange: build valid message then remove last 2 bytes (checksum)
            byte[] fullMsg = buildValidMessage(1, 100, new byte[]{0x01, 0x02});
            byte[] truncated = new byte[fullMsg.length - 2];
            System.arraycopy(fullMsg, 0, truncated, 0, truncated.length);
            // The payload length field still claims the original payload length,
            // so total declared size won't match actual byte count

            // Act
            ParseResult result = parser.parse(truncated);

            // Assert: declared length won't match actual length
            assertTrue(result.isError());
            assertEquals("length_mismatch", result.getErrorCode());
        }

        @Test
        @DisplayName("payload filled with all 0xFF bytes parses successfully")
        void payloadAllOxFFParsesSuccessfully() {
            // Arrange: edge byte values — all payload bytes are 0xFF
            byte[] payload = new byte[10];
            for (int i = 0; i < payload.length; i++) {
                payload[i] = (byte) 0xFF;
            }
            byte[] msg = buildValidMessage(1, 1, payload);

            // Act
            ParseResult result = parser.parse(msg);

            // Assert: structurally valid message should parse successfully
            assertTrue(result.isSuccess());
            assertEquals(1, result.getMessageType());
            assertEquals(1, result.getSequenceNumber());
        }
    }

    // =======================================================================
    // Error precedence: verify sequential validation order from design
    // =======================================================================

    @Nested
    @DisplayName("Error precedence: earlier validation steps take priority")
    class ErrorPrecedence {

        @Test
        @DisplayName("bad sync bytes detected before version check")
        void syncCheckedBeforeVersion() {
            // Arrange: bad sync AND bad version
            byte[] msg = buildValidMessage(1, 1, new byte[]{0x01});
            msg[0] = 0x00; // bad sync
            msg[2] = 99;   // bad version

            // Act
            ParseResult result = parser.parse(msg);

            // Assert: step 1 (sync) runs before step 2 (version) — sync error reported
            assertTrue(result.isError());
            assertEquals("invalid_sync", result.getErrorCode());
            assertEquals(0, result.getByteOffset());
        }

        @Test
        @DisplayName("version check happens before length check")
        void versionCheckedBeforeLength() {
            // Arrange: good sync, bad version, bad length
            byte[] msg = buildValidMessage(1, 1, new byte[]{0x01});
            msg[2] = 99;       // bad version
            msg[3] = 0x7F;     // bad length field
            msg[4] = (byte) 0xFF;

            // Act
            ParseResult result = parser.parse(msg);

            // Assert: step 2 (version) runs before step 3 (length) — version error reported
            assertTrue(result.isError());
            assertEquals("unsupported_version", result.getErrorCode());
            assertEquals(2, result.getByteOffset());
        }

        @Test
        @DisplayName("length check happens before checksum check")
        void lengthCheckedBeforeChecksum() {
            // Arrange: good sync, good version, bad length (claims more than available)
            byte[] msg = buildValidMessage(1, 1, new byte[]{0x01});
            msg[3] = 0;
            msg[4] = (byte) 200; // claims 200 bytes payload but total is only 13

            // Act
            ParseResult result = parser.parse(msg);

            // Assert: step 3 (length) runs before step 7 (checksum) — length error reported
            assertTrue(result.isError());
            assertEquals("length_mismatch", result.getErrorCode());
            assertEquals(3, result.getByteOffset());
        }
    }
}
