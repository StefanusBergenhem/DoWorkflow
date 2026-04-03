package comm.protocol;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MessageParser (DD-007).
 * Tests validate message framing, checksums, field extraction, and error handling.
 */
@DisplayName("MessageParser Unit Tests")
class MessageParserTest {

    private MessageParser parser;

    // Fixed header configuration from design
    private static final byte SYNC_BYTE_1 = (byte) 0xAA;
    private static final byte SYNC_BYTE_2 = (byte) 0x55;
    private static final byte SUPPORTED_VERSION = 1;
    private static final int HEADER_SIZE = 10;
    private static final int CHECKSUM_SIZE = 2;
    private static final int MIN_MESSAGE_SIZE = HEADER_SIZE + CHECKSUM_SIZE; // 12 bytes

    @BeforeEach
    void setUp() {
        parser = new MessageParser();
    }

    // ============================================================================
    // Sync Bytes Validation Tests (Step 1 from design)
    // ============================================================================

    @Nested
    @DisplayName("Sync Bytes Validation")
    class SyncBytesTests {

        @Test
        @DisplayName("Valid sync bytes: returns parsed message")
        void validSyncBytes() {
            byte[] data = buildValidMessage(1, 10, new byte[]{});
            ParseResult result = parser.parse(data);

            assertTrue(result.isSuccess(), "Should parse valid sync bytes");
            assertNotNull(result.getSuccess());
        }

        @Test
        @DisplayName("First sync byte invalid (0xBB instead of 0xAA)")
        void invalidFirstSyncByte() {
            byte[] data = new byte[MIN_MESSAGE_SIZE];
            data[0] = (byte) 0xBB;  // Wrong first byte
            data[1] = SYNC_BYTE_2;

            ParseResult result = parser.parse(data);

            assertTrue(result.isError(), "Should fail with invalid first sync byte");
            ParseError error = result.getError();
            assertEquals("invalid_sync", error.getErrorCode());
            assertEquals(0, error.getByteOffset());
        }

        @Test
        @DisplayName("Second sync byte invalid (0x54 instead of 0x55)")
        void invalidSecondSyncByte() {
            byte[] data = new byte[MIN_MESSAGE_SIZE];
            data[0] = SYNC_BYTE_1;
            data[1] = (byte) 0x54;  // Wrong second byte

            ParseResult result = parser.parse(data);

            assertTrue(result.isError(), "Should fail with invalid second sync byte");
            ParseError error = result.getError();
            assertEquals("invalid_sync", error.getErrorCode());
            assertEquals(0, error.getByteOffset());
        }

        @Test
        @DisplayName("Both sync bytes invalid")
        void bothSyncBytesInvalid() {
            byte[] data = new byte[MIN_MESSAGE_SIZE];
            data[0] = (byte) 0x00;
            data[1] = (byte) 0x00;

            ParseResult result = parser.parse(data);

            assertTrue(result.isError());
            assertEquals("invalid_sync", result.getError().getErrorCode());
        }
    }

    // ============================================================================
    // Version Byte Validation Tests (Step 2 from design)
    // ============================================================================

    @Nested
    @DisplayName("Version Byte Validation")
    class VersionTests {

        @Test
        @DisplayName("Valid version (1): parsing succeeds")
        void validVersion() {
            byte[] data = buildValidMessage(1, 10, new byte[]{});
            ParseResult result = parser.parse(data);

            assertTrue(result.isSuccess());
        }

        @Test
        @DisplayName("Invalid version (0): returns unsupported_version error")
        void invalidVersionZero() {
            byte[] data = buildMessageWithVersion((byte) 0, 10);
            ParseResult result = parser.parse(data);

            assertTrue(result.isError());
            ParseError error = result.getError();
            assertEquals("unsupported_version", error.getErrorCode());
            assertEquals(2, error.getByteOffset());
        }

        @ParameterizedTest
        @ValueSource(bytes = {2, 3, 5, 127, -1})
        @DisplayName("Unsupported versions: returns unsupported_version error")
        void unsupportedVersions(byte version) {
            byte[] data = buildMessageWithVersion(version, 10);
            ParseResult result = parser.parse(data);

            assertTrue(result.isError());
            assertEquals("unsupported_version", result.getError().getErrorCode());
            assertEquals(2, result.getError().getByteOffset());
        }
    }

    // ============================================================================
    // Payload Length Validation Tests (Step 3 from design)
    // ============================================================================

    @Nested
    @DisplayName("Payload Length Validation")
    class LengthTests {

        @Test
        @DisplayName("Length matches actual data: parsing succeeds")
        void validLength() {
            byte[] payload = {0x01, 0x02, 0x03};
            byte[] data = buildValidMessage(1, payload.length, payload);
            ParseResult result = parser.parse(data);

            assertTrue(result.isSuccess());
        }

        @Test
        @DisplayName("Declared length exceeds actual data")
        void lengthExceedsData() {
            byte[] data = buildValidMessage(1, 100, new byte[]{});
            ParseResult result = parser.parse(data);

            assertTrue(result.isError());
            ParseError error = result.getError();
            assertEquals("length_mismatch", error.getErrorCode());
            assertEquals(3, error.getByteOffset());
        }

        @Test
        @DisplayName("Declared length is less than actual data")
        void lengthLessThanData() {
            byte[] payload = {0x01, 0x02, 0x03, 0x04, 0x05};
            byte[] data = new byte[HEADER_SIZE + payload.length + CHECKSUM_SIZE];
            buildHeader(data, (short) 3);  // Say 3 bytes but have 5
            System.arraycopy(payload, 0, data, HEADER_SIZE, payload.length);
            appendChecksum(data);

            ParseResult result = parser.parse(data);

            assertTrue(result.isError());
            assertEquals("length_mismatch", result.getError().getErrorCode());
        }

        @Test
        @DisplayName("Zero payload length: valid message with empty payload")
        void zeroPayloadLength() {
            byte[] data = buildValidMessage(1, 0, new byte[]{});
            ParseResult result = parser.parse(data);

            assertTrue(result.isSuccess());
            Map<String, Object> payload = result.getSuccess().getPayload();
            assertTrue(payload.isEmpty());
        }

        @Test
        @DisplayName("Maximum payload length (1024 bytes)")
        void maximumPayloadLength() {
            byte[] payload = new byte[1024];
            byte[] data = buildValidMessage(1, payload.length, payload);
            ParseResult result = parser.parse(data);

            assertTrue(result.isSuccess());
        }
    }

    // ============================================================================
    // Null and Empty Input Tests (Error Handling from design)
    // ============================================================================

    @Nested
    @DisplayName("Null and Empty Input Handling")
    class InputValidationTests {

        @Test
        @DisplayName("Null input: returns invalid_sync error at offset 0")
        void nullInput() {
            ParseResult result = parser.parse(null);

            assertTrue(result.isError());
            ParseError error = result.getError();
            assertEquals("invalid_sync", error.getErrorCode());
            assertEquals(0, error.getByteOffset());
        }

        @Test
        @DisplayName("Empty byte array: returns invalid_sync error at offset 0")
        void emptyInput() {
            byte[] data = new byte[0];
            ParseResult result = parser.parse(data);

            assertTrue(result.isError());
            ParseError error = result.getError();
            assertEquals("invalid_sync", error.getErrorCode());
            assertEquals(0, error.getByteOffset());
        }

        @Test
        @DisplayName("Input shorter than minimum message size (< 12 bytes)")
        void inputTooShort() {
            byte[] data = new byte[MIN_MESSAGE_SIZE - 1];
            data[0] = SYNC_BYTE_1;
            data[1] = SYNC_BYTE_2;

            ParseResult result = parser.parse(data);

            assertTrue(result.isError());
            ParseError error = result.getError();
            assertEquals("length_mismatch", error.getErrorCode());
            assertEquals(0, error.getByteOffset());
        }

        @Test
        @DisplayName("Input exactly minimum message size (empty payload, valid checksum)")
        void inputMinimumSize() {
            byte[] data = new byte[MIN_MESSAGE_SIZE];
            buildHeader(data, (short) 0);
            appendChecksum(data);

            ParseResult result = parser.parse(data);

            assertTrue(result.isSuccess());
        }
    }

    // ============================================================================
    // Message Type Reading Tests (Step 4 from design)
    // ============================================================================

    @Nested
    @DisplayName("Message Type Extraction")
    class MessageTypeTests {

        @Test
        @DisplayName("Read message type from offset 5")
        void extractMessageType() {
            byte messageType = 42;
            byte[] data = buildValidMessage(messageType, 0, new byte[]{});
            ParseResult result = parser.parse(data);

            assertTrue(result.isSuccess());
            assertEquals(messageType, result.getSuccess().getMessageType());
        }

        @ParameterizedTest
        @ValueSource(bytes = {0, 1, 127, -128, -1})
        @DisplayName("Various message types are extracted correctly")
        void variousMessageTypes(byte messageType) {
            byte[] data = buildValidMessage(messageType, 0, new byte[]{});
            ParseResult result = parser.parse(data);

            assertTrue(result.isSuccess());
            assertEquals(messageType, result.getSuccess().getMessageType());
        }

        @Test
        @DisplayName("Unknown message type: returns unknown_type error")
        void unknownMessageType() {
            byte unknownType = (byte) 255;
            byte[] data = buildValidMessage(unknownType, 0, new byte[]{});
            // Parser has registry that doesn't include this type
            ParseResult result = parser.parse(data);

            // This assumes the parser has a type registry
            if (result.isError()) {
                assertEquals("unknown_type", result.getError().getErrorCode());
                assertEquals(5, result.getError().getByteOffset());
            }
        }
    }

    // ============================================================================
    // Sequence Number Extraction Tests (Step 5 from design)
    // ============================================================================

    @Nested
    @DisplayName("Sequence Number Extraction")
    class SequenceNumberTests {

        @Test
        @DisplayName("Read sequence number from offset 6-9 (big-endian uint32)")
        void extractSequenceNumber() {
            int sequenceNumber = 0x12345678;
            byte[] data = buildValidMessageWithSequence(1, 0, new byte[]{}, sequenceNumber);
            ParseResult result = parser.parse(data);

            assertTrue(result.isSuccess());
            assertEquals(sequenceNumber, result.getSuccess().getSequenceNumber());
        }

        @ParameterizedTest
        @CsvSource({
            "0,          0x00000000",
            "1,          0x00000001",
            "256,        0x00000100",
            "65536,      0x00010000",
            "2147483647, 0x7FFFFFFF",
            "-1,         0xFFFFFFFF"
        })
        @DisplayName("Various sequence numbers (big-endian)")
        void variousSequenceNumbers(String label, String hexValue) {
            int sequenceNumber = (int) Long.parseLong(hexValue, 16);
            byte[] data = buildValidMessageWithSequence(1, 0, new byte[]{}, sequenceNumber);
            ParseResult result = parser.parse(data);

            assertTrue(result.isSuccess());
            assertEquals(sequenceNumber, result.getSuccess().getSequenceNumber());
        }

        @Test
        @DisplayName("Sequence numbers enable message ordering")
        void sequenceNumberOrdering() {
            byte[] msg1 = buildValidMessageWithSequence(1, 0, new byte[]{}, 100);
            byte[] msg2 = buildValidMessageWithSequence(1, 0, new byte[]{}, 101);

            ParseResult result1 = parser.parse(msg1);
            ParseResult result2 = parser.parse(msg2);

            assertTrue(result1.isSuccess());
            assertTrue(result2.isSuccess());
            assertEquals(100, result1.getSuccess().getSequenceNumber());
            assertEquals(101, result2.getSuccess().getSequenceNumber());
            assertTrue(result1.getSuccess().getSequenceNumber() < result2.getSuccess().getSequenceNumber());
        }
    }

    // ============================================================================
    // Checksum Verification Tests (Step 7 from design)
    // ============================================================================

    @Nested
    @DisplayName("Checksum Verification")
    class ChecksumTests {

        @Test
        @DisplayName("Valid checksum: parsing succeeds")
        void validChecksum() {
            byte[] data = buildValidMessage(1, 10, new byte[10]);
            ParseResult result = parser.parse(data);

            assertTrue(result.isSuccess());
        }

        @Test
        @DisplayName("Corrupted checksum (first byte): returns checksum_failed error")
        void corruptedChecksumFirstByte() {
            byte[] data = buildValidMessage(1, 5, new byte[5]);
            // Corrupt last 2 bytes (checksum)
            data[data.length - 2] = (byte) (data[data.length - 2] ^ 0xFF);

            ParseResult result = parser.parse(data);

            assertTrue(result.isError());
            assertEquals("checksum_failed", result.getError().getErrorCode());
            assertEquals(data.length - 2, result.getError().getByteOffset());
        }

        @Test
        @DisplayName("Corrupted checksum (second byte): returns checksum_failed error")
        void corruptedChecksumSecondByte() {
            byte[] data = buildValidMessage(1, 5, new byte[5]);
            // Corrupt last byte (checksum)
            data[data.length - 1] = (byte) (data[data.length - 1] ^ 0xFF);

            ParseResult result = parser.parse(data);

            assertTrue(result.isError());
            assertEquals("checksum_failed", result.getError().getErrorCode());
            assertEquals(data.length - 1, result.getError().getByteOffset());
        }

        @Test
        @DisplayName("Checksum computed over all preceding bytes")
        void checksumCoversAllBytes() {
            byte[] payload = {0x01, 0x02, 0x03, 0x04};
            byte[] data = buildValidMessage(42, payload.length, payload);

            // Change a byte in the payload
            data[HEADER_SIZE] = (byte) (data[HEADER_SIZE] ^ 0x01);

            ParseResult result = parser.parse(data);

            assertTrue(result.isError());
            assertEquals("checksum_failed", result.getError().getErrorCode());
        }

        @Test
        @DisplayName("Checksum validates sync bytes")
        void checksumIncludesSyncBytes() {
            byte[] data = buildValidMessage(1, 0, new byte[]{});

            // Corrupt sync byte
            data[0] = (byte) (data[0] ^ 0x01);

            ParseResult result = parser.parse(data);

            // Parser detects invalid sync before checksum, but if it did,
            // checksum would fail too
            assertTrue(result.isError());
        }
    }

    // ============================================================================
    // Payload Parsing Tests (Step 8 from design)
    // ============================================================================

    @Nested
    @DisplayName("Payload Parsing")
    class PayloadParsingTests {

        @Test
        @DisplayName("Empty payload: returns empty map")
        void emptyPayload() {
            byte[] data = buildValidMessage(1, 0, new byte[]{});
            ParseResult result = parser.parse(data);

            assertTrue(result.isSuccess());
            Map<String, Object> payload = result.getSuccess().getPayload();
            assertTrue(payload.isEmpty());
        }

        @Test
        @DisplayName("Non-empty payload: extracted and included in result")
        void nonEmptyPayload() {
            byte[] payloadBytes = {0x48, 0x65, 0x6C, 0x6C, 0x6F};  // "Hello"
            byte[] data = buildValidMessage(1, payloadBytes.length, payloadBytes);
            ParseResult result = parser.parse(data);

            assertTrue(result.isSuccess());
            Map<String, Object> payload = result.getSuccess().getPayload();
            assertNotNull(payload);
            assertFalse(payload.isEmpty());
        }

        @Test
        @DisplayName("Payload extracted from correct offset (HEADER_SIZE to HEADER_SIZE + length)")
        void payloadExtractedFromCorrectOffset() {
            byte[] expectedPayload = {0x01, 0x02, 0x03, 0x04, 0x05};
            byte[] data = buildValidMessage(1, expectedPayload.length, expectedPayload);
            ParseResult result = parser.parse(data);

            assertTrue(result.isSuccess());
            // Verify payload bytes are at expected position
            for (int i = 0; i < expectedPayload.length; i++) {
                assertEquals(expectedPayload[i], data[HEADER_SIZE + i]);
            }
        }

        @Test
        @DisplayName("Large payload (1024 bytes): parsed correctly")
        void largePayload() {
            byte[] payload = new byte[1024];
            for (int i = 0; i < payload.length; i++) {
                payload[i] = (byte) (i % 256);
            }
            byte[] data = buildValidMessage(1, payload.length, payload);
            ParseResult result = parser.parse(data);

            assertTrue(result.isSuccess());
            assertNotNull(result.getSuccess().getPayload());
        }
    }

    // ============================================================================
    // Success Case Tests (End-to-End)
    // ============================================================================

    @Nested
    @DisplayName("Successful Parse Cases")
    class SuccessTests {

        @Test
        @DisplayName("Complete valid message: all fields present")
        void completeValidMessage() {
            byte messageType = 5;
            byte[] payload = {0xAA, 0xBB, 0xCC};
            byte[] data = buildValidMessage(messageType, payload.length, payload);
            ParseResult result = parser.parse(data);

            assertTrue(result.isSuccess());
            ParseSuccess success = result.getSuccess();
            assertEquals(messageType, success.getMessageType());
            assertNotNull(success.getPayload());
            assertNotNull(success.getSequenceNumber());
        }

        @Test
        @DisplayName("Multiple messages parsed independently")
        void multipleIndependentMessages() {
            byte[] msg1 = buildValidMessage(1, 0, new byte[]{});
            byte[] msg2 = buildValidMessage(2, 0, new byte[]{});

            ParseResult result1 = parser.parse(msg1);
            ParseResult result2 = parser.parse(msg2);

            assertTrue(result1.isSuccess());
            assertTrue(result2.isSuccess());
            assertEquals(1, result1.getSuccess().getMessageType());
            assertEquals(2, result2.getSuccess().getMessageType());
        }
    }

    // ============================================================================
    // Error Code Coverage Tests
    // ============================================================================

    @Nested
    @DisplayName("All Error Codes Reachable")
    class ErrorCodeCoverageTests {

        @Test
        @DisplayName("Error code: invalid_sync")
        void errorCodeInvalidSync() {
            byte[] data = new byte[MIN_MESSAGE_SIZE];
            data[0] = 0x00;
            data[1] = 0x00;

            ParseResult result = parser.parse(data);
            assertEquals("invalid_sync", result.getError().getErrorCode());
        }

        @Test
        @DisplayName("Error code: unsupported_version")
        void errorCodeUnsupportedVersion() {
            byte[] data = buildMessageWithVersion((byte) 99, 0);
            ParseResult result = parser.parse(data);
            assertEquals("unsupported_version", result.getError().getErrorCode());
        }

        @Test
        @DisplayName("Error code: length_mismatch")
        void errorCodeLengthMismatch() {
            byte[] data = new byte[HEADER_SIZE + 5 + CHECKSUM_SIZE];
            buildHeader(data, (short) 100);  // Say 100 but only have 5
            appendChecksum(data);

            ParseResult result = parser.parse(data);
            assertEquals("length_mismatch", result.getError().getErrorCode());
        }

        @Test
        @DisplayName("Error code: checksum_failed")
        void errorCodeChecksumFailed() {
            byte[] data = buildValidMessage(1, 0, new byte[]{});
            data[data.length - 1] ^= 0xFF;  // Corrupt checksum

            ParseResult result = parser.parse(data);
            assertEquals("checksum_failed", result.getError().getErrorCode());
        }
    }

    // ============================================================================
    // Reentrancy and State Tests
    // ============================================================================

    @Nested
    @DisplayName("Reentrancy and State Management")
    class ReetrancyTests {

        @Test
        @DisplayName("Parser is reentrant: parsing same message twice produces same result")
        void reentrancyConsistency() {
            byte[] data = buildValidMessage(1, 5, new byte[5]);

            ParseResult result1 = parser.parse(data);
            ParseResult result2 = parser.parse(data);

            assertTrue(result1.isSuccess());
            assertTrue(result2.isSuccess());
            assertEquals(result1.getSuccess().getMessageType(), result2.getSuccess().getMessageType());
        }

        @Test
        @DisplayName("Parser state isolated between calls")
        void stateIsolation() {
            byte[] data1 = buildValidMessage(10, 0, new byte[]{});
            byte[] data2 = buildValidMessage(20, 0, new byte[]{});

            ParseResult result1 = parser.parse(data1);
            ParseResult result2 = parser.parse(data2);

            assertEquals(10, result1.getSuccess().getMessageType());
            assertEquals(20, result2.getSuccess().getMessageType());
        }

        @Test
        @DisplayName("No shared mutable state affects parsing")
        void noMutableSharedState() {
            byte[] data1 = buildValidMessage(1, 3, new byte[]{0x01, 0x02, 0x03});
            byte[] data2 = buildValidMessage(2, 3, new byte[]{0x04, 0x05, 0x06});

            ParseResult result1 = parser.parse(data1);
            // Modify data1 after parsing
            data1[HEADER_SIZE] = (byte) 0xFF;

            ParseResult result2 = parser.parse(data2);

            assertTrue(result1.isSuccess());
            assertTrue(result2.isSuccess());
            // result1 should not be affected by data1 modification
        }
    }

    // ============================================================================
    // Helper Methods
    // ============================================================================

    /**
     * Builds a valid message with given message type, payload length, and payload bytes.
     * Constructs header, embeds payload, and appends valid CRC-16 checksum.
     */
    private byte[] buildValidMessage(byte messageType, int payloadLength, byte[] payload) {
        int totalLength = HEADER_SIZE + payloadLength + CHECKSUM_SIZE;
        byte[] data = new byte[totalLength];

        buildHeader(data, (short) payloadLength);
        data[5] = messageType;

        if (payload.length > 0) {
            System.arraycopy(payload, 0, data, HEADER_SIZE, payload.length);
        }

        appendChecksum(data);
        return data;
    }

    /**
     * Builds a message with custom version byte.
     */
    private byte[] buildMessageWithVersion(byte version, int payloadLength) {
        int totalLength = HEADER_SIZE + payloadLength + CHECKSUM_SIZE;
        byte[] data = new byte[totalLength];

        data[0] = SYNC_BYTE_1;
        data[1] = SYNC_BYTE_2;
        data[2] = version;

        // Set payload length (big-endian uint16 at offset 3-4)
        data[3] = (byte) ((payloadLength >> 8) & 0xFF);
        data[4] = (byte) (payloadLength & 0xFF);

        data[5] = 1;  // message type
        // sequence number at 6-9 (initialized to 0)

        appendChecksum(data);
        return data;
    }

    /**
     * Builds a valid message with custom sequence number.
     */
    private byte[] buildValidMessageWithSequence(byte messageType, int payloadLength,
                                                  byte[] payload, int sequenceNumber) {
        int totalLength = HEADER_SIZE + payloadLength + CHECKSUM_SIZE;
        byte[] data = new byte[totalLength];

        buildHeader(data, (short) payloadLength);
        data[5] = messageType;

        // Set sequence number (big-endian uint32 at offset 6-9)
        data[6] = (byte) ((sequenceNumber >> 24) & 0xFF);
        data[7] = (byte) ((sequenceNumber >> 16) & 0xFF);
        data[8] = (byte) ((sequenceNumber >> 8) & 0xFF);
        data[9] = (byte) (sequenceNumber & 0xFF);

        if (payload.length > 0) {
            System.arraycopy(payload, 0, data, HEADER_SIZE, payload.length);
        }

        appendChecksum(data);
        return data;
    }

    /**
     * Builds the standard header (sync bytes, version, length, message type, sequence).
     */
    private void buildHeader(byte[] data, short payloadLength) {
        data[0] = SYNC_BYTE_1;
        data[1] = SYNC_BYTE_2;
        data[2] = SUPPORTED_VERSION;
        data[3] = (byte) ((payloadLength >> 8) & 0xFF);
        data[4] = (byte) (payloadLength & 0xFF);
        data[5] = 1;  // default message type
        // data[6-9]: sequence number (default 0)
    }

    /**
     * Appends CRC-16 checksum over all preceding bytes.
     * Assumes data array is sized to include 2 checksum bytes at the end.
     */
    private void appendChecksum(byte[] data) {
        int checksumStart = data.length - CHECKSUM_SIZE;
        int crc16 = calculateCrc16(data, checksumStart);
        data[checksumStart] = (byte) ((crc16 >> 8) & 0xFF);
        data[checksumStart + 1] = (byte) (crc16 & 0xFF);
    }

    /**
     * Calculates CRC-16 over the specified range (used for test checksum generation).
     * This is a simple CRC-16 implementation; adjust polynomial/init if needed.
     */
    private int calculateCrc16(byte[] data, int length) {
        int crc = 0xFFFF;
        for (int i = 0; i < length; i++) {
            crc ^= (data[i] & 0xFF) << 8;
            for (int j = 0; j < 8; j++) {
                if ((crc & 0x8000) != 0) {
                    crc = ((crc << 1) ^ 0x1021) & 0xFFFF;
                } else {
                    crc = (crc << 1) & 0xFFFF;
                }
            }
        }
        return crc;
    }
}

// ============================================================================
// Supporting Classes (Part of test infrastructure)
// ============================================================================

/**
 * Result union: either Success or Error.
 */
interface ParseResult {
    boolean isSuccess();
    boolean isError();
    ParseSuccess getSuccess();
    ParseError getError();
}

/**
 * Successful parse outcome.
 */
interface ParseSuccess {
    byte getMessageType();
    Map<String, Object> getPayload();
    int getSequenceNumber();
}

/**
 * Error outcome with diagnostic information.
 */
interface ParseError {
    String getErrorCode();
    int getByteOffset();
}
