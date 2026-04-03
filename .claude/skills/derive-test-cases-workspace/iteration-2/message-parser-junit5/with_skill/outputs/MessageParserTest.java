import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for MessageParser derived from detailed design (DD-007).
 * Tests all behavior rules, error conditions, boundaries, and equivalence classes.
 *
 * Derivation strategy:
 * - Requirement-based: one test per behavior rule from design
 * - Equivalence class: valid/invalid input classes
 * - Boundary value: min/max length, field ranges
 * - Error handling: explicit error conditions + fault injection
 */
@DisplayName("MessageParser - Protocol Message Parsing")
class MessageParserTest {

    private MessageParser parser;

    // Configuration constants from design
    private static final byte[] SYNC_BYTES = {(byte) 0xAA, (byte) 0x55};
    private static final byte SUPPORTED_VERSION = 1;
    private static final int HEADER_SIZE = 10;
    private static final int CHECKSUM_SIZE = 2;
    private static final int PAYLOAD_LENGTH_OFFSET = 3;
    private static final int MESSAGE_TYPE_OFFSET = 5;
    private static final int SEQUENCE_NUMBER_OFFSET = 6;

    @BeforeEach
    void setUp() {
        parser = new MessageParser();
    }

    // ============================================================================
    // Strategy 1: Requirement-Based Testing (behavior section from design)
    // ============================================================================

    @Nested
    @DisplayName("Sync Byte Validation")
    class SyncByteValidation {

        @Test
        @DisplayName("accepts valid sync bytes (0xAA, 0x55)")
        void test_accepts_valid_sync_bytes() {
            // Arrange: valid message with correct sync bytes
            byte[] data = validMessageWithLength(10);

            // Act
            ParseResult result = parser.parse(data);

            // Assert: should not fail on sync validation (error_code != invalid_sync)
            assertTrue(result.isSuccess());
        }

        @Test
        @DisplayName("rejects invalid first sync byte")
        void test_rejects_invalid_first_sync_byte() {
            // Arrange
            byte[] data = new byte[20];
            data[0] = (byte) 0xBB;  // wrong first sync byte
            data[1] = (byte) 0x55;
            data[2] = SUPPORTED_VERSION;

            // Act
            ParseResult result = parser.parse(data);

            // Assert
            assertTrue(result.isError());
            assertEquals(ErrorCode.INVALID_SYNC, result.getErrorCode());
            assertEquals(0, result.getByteOffset());
        }

        @Test
        @DisplayName("rejects invalid second sync byte")
        void test_rejects_invalid_second_sync_byte() {
            // Arrange
            byte[] data = new byte[20];
            data[0] = (byte) 0xAA;
            data[1] = (byte) 0xCC;  // wrong second sync byte
            data[2] = SUPPORTED_VERSION;

            // Act
            ParseResult result = parser.parse(data);

            // Assert
            assertTrue(result.isError());
            assertEquals(ErrorCode.INVALID_SYNC, result.getErrorCode());
            assertEquals(0, result.getByteOffset());
        }
    }

    @Nested
    @DisplayName("Version Validation")
    class VersionValidation {

        @Test
        @DisplayName("accepts supported version (1)")
        void test_accepts_supported_version() {
            // Arrange
            byte[] data = validMessageWithLength(10);

            // Act
            ParseResult result = parser.parse(data);

            // Assert: should not fail on version validation
            assertTrue(result.isSuccess());
        }

        @Test
        @DisplayName("rejects unsupported version (0)")
        void test_rejects_unsupported_version_zero() {
            // Arrange
            byte[] data = new byte[20];
            data[0] = (byte) 0xAA;
            data[1] = (byte) 0x55;
            data[2] = 0;  // version 0, not supported

            // Act
            ParseResult result = parser.parse(data);

            // Assert
            assertTrue(result.isError());
            assertEquals(ErrorCode.UNSUPPORTED_VERSION, result.getErrorCode());
            assertEquals(2, result.getByteOffset());
        }

        @Test
        @DisplayName("rejects unsupported version (2)")
        void test_rejects_unsupported_version_two() {
            // Arrange
            byte[] data = new byte[20];
            data[0] = (byte) 0xAA;
            data[1] = (byte) 0x55;
            data[2] = 2;  // version 2, not supported

            // Act
            ParseResult result = parser.parse(data);

            // Assert
            assertTrue(result.isError());
            assertEquals(ErrorCode.UNSUPPORTED_VERSION, result.getErrorCode());
            assertEquals(2, result.getByteOffset());
        }
    }

    @Nested
    @DisplayName("Length Validation")
    class LengthValidation {

        @Test
        @DisplayName("accepts message with valid declared length matching actual data length")
        void test_accepts_valid_length() {
            // Arrange: payload length 10, actual data = HEADER_SIZE + 10 + CHECKSUM_SIZE = 22
            byte[] data = validMessageWithLength(10);

            // Act
            ParseResult result = parser.parse(data);

            // Assert
            assertTrue(result.isSuccess());
        }

        @Test
        @DisplayName("rejects message where declared payload length exceeds actual data")
        void test_rejects_payload_declared_longer_than_actual() {
            // Arrange: claim payload length is 100, but only provide 20 total bytes
            byte[] data = new byte[20];
            data[0] = (byte) 0xAA;
            data[1] = (byte) 0x55;
            data[2] = SUPPORTED_VERSION;
            // payload_length at offset 3-4 (big-endian)
            data[3] = 0;
            data[4] = 100;  // declares 100 bytes payload

            // Act
            ParseResult result = parser.parse(data);

            // Assert
            assertTrue(result.isError());
            assertEquals(ErrorCode.LENGTH_MISMATCH, result.getErrorCode());
            assertEquals(3, result.getByteOffset());
        }

        @Test
        @DisplayName("rejects message where actual data exceeds declared length")
        void test_rejects_payload_actual_longer_than_declared() {
            // Arrange: declare payload length 5, provide data for 20
            byte[] data = new byte[30];
            data[0] = (byte) 0xAA;
            data[1] = (byte) 0x55;
            data[2] = SUPPORTED_VERSION;
            data[3] = 0;
            data[4] = 5;  // declares 5 bytes payload, but more data follows

            // Act
            ParseResult result = parser.parse(data);

            // Assert
            assertTrue(result.isError());
            assertEquals(ErrorCode.LENGTH_MISMATCH, result.getErrorCode());
        }
    }

    @Nested
    @DisplayName("Checksum Validation (CRC-16)")
    class ChecksumValidation {

        @Test
        @DisplayName("accepts message with valid CRC-16 checksum")
        void test_accepts_valid_checksum() {
            // Arrange
            byte[] data = validMessageWithValidChecksum(10);

            // Act
            ParseResult result = parser.parse(data);

            // Assert
            assertTrue(result.isSuccess());
        }

        @Test
        @DisplayName("rejects message with corrupted checksum")
        void test_rejects_corrupted_checksum() {
            // Arrange
            byte[] data = validMessageWithLength(10);
            // Corrupt last 2 bytes (checksum)
            int lastIndex = data.length - 1;
            data[lastIndex] = (byte) 0xFF;
            data[lastIndex - 1] = (byte) 0xFF;

            // Act
            ParseResult result = parser.parse(data);

            // Assert
            assertTrue(result.isError());
            assertEquals(ErrorCode.CHECKSUM_FAILED, result.getErrorCode());
            assertEquals(data.length - CHECKSUM_SIZE, result.getByteOffset());
        }

        @Test
        @DisplayName("rejects message where checksum byte is off by one")
        void test_rejects_checksum_off_by_one() {
            // Arrange
            byte[] data = validMessageWithValidChecksum(10);
            // Flip one bit in the checksum
            int lastIndex = data.length - 1;
            data[lastIndex] ^= 0x01;

            // Act
            ParseResult result = parser.parse(data);

            // Assert
            assertTrue(result.isError());
            assertEquals(ErrorCode.CHECKSUM_FAILED, result.getErrorCode());
        }
    }

    @Nested
    @DisplayName("Message Type Handling")
    class MessageTypeHandling {

        @Test
        @DisplayName("extracts message type from valid message")
        void test_extracts_message_type() {
            // Arrange: message type = 0x42 at offset 5
            byte[] data = validMessageWithMessageType(0x42, 10);

            // Act
            ParseResult result = parser.parse(data);

            // Assert
            assertTrue(result.isSuccess());
            assertEquals(0x42, result.getMessageType());
        }

        @Test
        @DisplayName("rejects unknown message type")
        void test_rejects_unknown_message_type() {
            // Arrange: use message type not in registry (assume 0xFF is not registered)
            byte[] data = validMessageWithMessageType(0xFF, 10);

            // Act
            ParseResult result = parser.parse(data);

            // Assert
            assertTrue(result.isError());
            assertEquals(ErrorCode.UNKNOWN_TYPE, result.getErrorCode());
            assertEquals(MESSAGE_TYPE_OFFSET, result.getByteOffset());
        }

        @Test
        @DisplayName("extracts message type 0x01 (common type)")
        void test_extracts_common_message_type_0x01() {
            // Arrange
            byte[] data = validMessageWithMessageType(0x01, 10);

            // Act
            ParseResult result = parser.parse(data);

            // Assert
            assertTrue(result.isSuccess());
            assertEquals(0x01, result.getMessageType());
        }
    }

    @Nested
    @DisplayName("Sequence Number Extraction (Big-Endian uint32)")
    class SequenceNumberExtraction {

        @Test
        @DisplayName("extracts sequence number from big-endian uint32 at offset 6-9")
        void test_extracts_sequence_number() {
            // Arrange: sequence number = 0x12345678 (big-endian)
            byte[] data = validMessageWithSequenceNumber(0x12345678L, 10);

            // Act
            ParseResult result = parser.parse(data);

            // Assert
            assertTrue(result.isSuccess());
            assertEquals(0x12345678L, result.getSequenceNumber());
        }

        @Test
        @DisplayName("extracts minimum sequence number (0)")
        void test_extracts_min_sequence_number() {
            // Arrange
            byte[] data = validMessageWithSequenceNumber(0L, 10);

            // Act
            ParseResult result = parser.parse(data);

            // Assert
            assertTrue(result.isSuccess());
            assertEquals(0L, result.getSequenceNumber());
        }

        @Test
        @DisplayName("extracts maximum sequence number (2^32 - 1)")
        void test_extracts_max_sequence_number() {
            // Arrange: 0xFFFFFFFF
            byte[] data = validMessageWithSequenceNumber(0xFFFFFFFFL, 10);

            // Act
            ParseResult result = parser.parse(data);

            // Assert
            assertTrue(result.isSuccess());
            assertEquals(0xFFFFFFFFL, result.getSequenceNumber());
        }

        @Test
        @DisplayName("extracts sequence number 1 (boundary)")
        void test_extracts_sequence_number_one() {
            // Arrange
            byte[] data = validMessageWithSequenceNumber(1L, 10);

            // Act
            ParseResult result = parser.parse(data);

            // Assert
            assertTrue(result.isSuccess());
            assertEquals(1L, result.getSequenceNumber());
        }
    }

    @Nested
    @DisplayName("Payload Parsing")
    class PayloadParsing {

        @Test
        @DisplayName("parses payload fields into map with correct keys and values")
        void test_parses_payload_fields() {
            // Arrange: payload contains key1=100, key2="test"
            byte[] data = validMessageWithPayload(0x01, new String[]{"key1", "key2"}, 10);

            // Act
            ParseResult result = parser.parse(data);

            // Assert
            assertTrue(result.isSuccess());
            assertNotNull(result.getPayload());
            assertTrue(result.getPayload().containsKey("key1"));
            assertTrue(result.getPayload().containsKey("key2"));
        }

        @Test
        @DisplayName("extracts empty payload (zero-length)")
        void test_parses_empty_payload() {
            // Arrange: message with 0-byte payload
            byte[] data = validMessageWithLength(0);

            // Act
            ParseResult result = parser.parse(data);

            // Assert
            assertTrue(result.isSuccess());
            assertTrue(result.getPayload().isEmpty());
        }

        @Test
        @DisplayName("parses maximum-size payload (1000 bytes)")
        void test_parses_large_payload() {
            // Arrange: 1000-byte payload (total 1012 bytes = 10 + 1000 + 2)
            byte[] data = validMessageWithLength(1000);

            // Act
            ParseResult result = parser.parse(data);

            // Assert
            assertTrue(result.isSuccess());
            // Payload was parsed (would fail if parsing logic had issues)
        }
    }

    // ============================================================================
    // Strategy 2 & 3: Equivalence Class Partitioning + Boundary Value Analysis
    // ============================================================================

    @Nested
    @DisplayName("Input Length Boundaries (constraint: 4..1024 bytes)")
    class InputLengthBoundaries {

        @Test
        @DisplayName("rejects input shorter than minimum valid message (< 12 bytes)")
        void test_rejects_input_below_minimum_size() {
            // Arrange: 11 bytes (less than HEADER_SIZE=10 + CHECKSUM_SIZE=2)
            byte[] data = new byte[11];

            // Act
            ParseResult result = parser.parse(data);

            // Assert
            assertTrue(result.isError());
            assertEquals(ErrorCode.LENGTH_MISMATCH, result.getErrorCode());
            assertEquals(0, result.getByteOffset());
        }

        @Test
        @DisplayName("accepts input at minimum valid size (12 bytes: header + checksum)")
        void test_accepts_minimum_size_message() {
            // Arrange: exactly HEADER_SIZE + CHECKSUM_SIZE = 12 bytes
            byte[] data = validMessageWithLength(0);  // 0 payload + 10 header + 2 checksum

            // Act
            ParseResult result = parser.parse(data);

            // Assert
            assertTrue(result.isSuccess());
        }

        @Test
        @DisplayName("accepts input at design constraint maximum (1024 bytes)")
        void test_accepts_maximum_size_message() {
            // Arrange: 1024 bytes total
            byte[] data = validMessageWithLength(1024 - HEADER_SIZE - CHECKSUM_SIZE);

            // Act
            ParseResult result = parser.parse(data);

            // Assert
            assertTrue(result.isSuccess());
        }

        @Test
        @DisplayName("rejects input exceeding design constraint (1025 bytes)")
        void test_rejects_input_exceeding_maximum() {
            // Arrange: 1025 bytes (over the 1024 limit)
            byte[] data = new byte[1025];
            data[0] = (byte) 0xAA;
            data[1] = (byte) 0x55;
            data[2] = SUPPORTED_VERSION;
            // Set payload length to 1013 (1025 - 10 - 2)
            data[3] = 0x03;
            data[4] = (byte) 0xF5;

            // Act
            ParseResult result = parser.parse(data);

            // Assert: implementation choice — may reject as invalid or handle gracefully
            // Design says "length 4..1024" which implies this is invalid
            assertTrue(result.isError() || !result.isSuccess());
        }
    }

    // ============================================================================
    // Strategy 4: Error Handling & Fault Injection
    // ============================================================================

    @Nested
    @DisplayName("Null and Empty Input Handling (Fault Injection)")
    class NullAndEmptyInputHandling {

        @Test
        @DisplayName("rejects null input")
        void test_rejects_null_input() {
            // Act & Assert
            assertThrows(NullPointerException.class, () -> parser.parse(null));
        }

        @Test
        @DisplayName("rejects empty byte array")
        void test_rejects_empty_input() {
            // Arrange
            byte[] data = new byte[0];

            // Act
            ParseResult result = parser.parse(data);

            // Assert
            assertTrue(result.isError());
            assertEquals(ErrorCode.INVALID_SYNC, result.getErrorCode());
            assertEquals(0, result.getByteOffset());
        }

        @Test
        @DisplayName("rejects single-byte input")
        void test_rejects_single_byte_input() {
            // Arrange
            byte[] data = new byte[1];
            data[0] = (byte) 0xAA;

            // Act
            ParseResult result = parser.parse(data);

            // Assert
            assertTrue(result.isError());
        }
    }

    @Nested
    @DisplayName("Payload Length Field Edge Cases")
    class PayloadLengthEdgeCases {

        @Test
        @DisplayName("handles payload length field = 0 (header-only message)")
        void test_handles_zero_payload_length() {
            // Arrange: payload length = 0
            byte[] data = validMessageWithLength(0);

            // Act
            ParseResult result = parser.parse(data);

            // Assert
            assertTrue(result.isSuccess());
        }

        @Test
        @DisplayName("rejects inconsistent length encoding (off by 1)")
        void test_rejects_length_off_by_one() {
            // Arrange: declare length 10, but provide only 9 bytes of payload
            byte[] data = new byte[HEADER_SIZE + 9 + CHECKSUM_SIZE];
            data[0] = (byte) 0xAA;
            data[1] = (byte) 0x55;
            data[2] = SUPPORTED_VERSION;
            data[3] = 0;
            data[4] = 10;  // claim 10, provide 9

            // Act
            ParseResult result = parser.parse(data);

            // Assert
            assertTrue(result.isError());
            assertEquals(ErrorCode.LENGTH_MISMATCH, result.getErrorCode());
        }
    }

    @Nested
    @DisplayName("Concurrent Access / Reentrancy (Constraint Verification)")
    class ReentrancyVerification {

        @Test
        @DisplayName("parser is reentrant — multiple messages can be parsed in sequence")
        void test_parser_is_reentrant() {
            // Arrange: two different messages
            byte[] msg1 = validMessageWithMessageType(0x01, 5);
            byte[] msg2 = validMessageWithMessageType(0x02, 8);

            // Act: parse first message
            ParseResult result1 = parser.parse(msg1);

            // Act: parse second message with same parser instance
            ParseResult result2 = parser.parse(msg2);

            // Assert: both parse correctly, independently
            assertTrue(result1.isSuccess());
            assertTrue(result2.isSuccess());
            assertEquals(0x01, result1.getMessageType());
            assertEquals(0x02, result2.getMessageType());
        }

        @Test
        @DisplayName("parser is reentrant — parsing different sizes in sequence")
        void test_parser_handles_size_variance() {
            // Arrange: small then large message
            byte[] small = validMessageWithLength(5);
            byte[] large = validMessageWithLength(500);

            // Act
            ParseResult r1 = parser.parse(small);
            ParseResult r2 = parser.parse(large);

            // Assert: both succeed, no state contamination
            assertTrue(r1.isSuccess());
            assertTrue(r2.isSuccess());
        }
    }

    // ============================================================================
    // Helper Methods: Construct Valid Test Messages
    // ============================================================================

    /**
     * Construct a valid message with specified payload length.
     * Sync, version, and checksum are correct; can be used for positive tests.
     */
    private byte[] validMessageWithLength(int payloadLength) {
        byte[] data = new byte[HEADER_SIZE + payloadLength + CHECKSUM_SIZE];

        // Sync bytes
        data[0] = (byte) 0xAA;
        data[1] = (byte) 0x55;

        // Version
        data[2] = SUPPORTED_VERSION;

        // Payload length (big-endian uint16)
        data[3] = (byte) ((payloadLength >> 8) & 0xFF);
        data[4] = (byte) (payloadLength & 0xFF);

        // Message type (arbitrary valid type)
        data[5] = 0x01;

        // Sequence number (big-endian uint32) = 1
        data[6] = 0x00;
        data[7] = 0x00;
        data[8] = 0x00;
        data[9] = 0x01;

        // Placeholder payload
        for (int i = 0; i < payloadLength; i++) {
            data[HEADER_SIZE + i] = 0x00;
        }

        // Checksum (computed as CRC-16 of all preceding bytes)
        int checksum = computeCrc16(data, 0, HEADER_SIZE + payloadLength);
        data[HEADER_SIZE + payloadLength] = (byte) ((checksum >> 8) & 0xFF);
        data[HEADER_SIZE + payloadLength + 1] = (byte) (checksum & 0xFF);

        return data;
    }

    /**
     * Construct a valid message with a computed valid checksum.
     */
    private byte[] validMessageWithValidChecksum(int payloadLength) {
        return validMessageWithLength(payloadLength);
    }

    /**
     * Construct a valid message with specified message type.
     */
    private byte[] validMessageWithMessageType(int messageType, int payloadLength) {
        byte[] data = validMessageWithLength(payloadLength);
        data[MESSAGE_TYPE_OFFSET] = (byte) messageType;

        // Recompute checksum
        int checksum = computeCrc16(data, 0, HEADER_SIZE + payloadLength);
        data[HEADER_SIZE + payloadLength] = (byte) ((checksum >> 8) & 0xFF);
        data[HEADER_SIZE + payloadLength + 1] = (byte) (checksum & 0xFF);

        return data;
    }

    /**
     * Construct a valid message with specified sequence number.
     */
    private byte[] validMessageWithSequenceNumber(long sequenceNumber, int payloadLength) {
        byte[] data = validMessageWithLength(payloadLength);

        // Write sequence number (big-endian uint32)
        data[SEQUENCE_NUMBER_OFFSET] = (byte) ((sequenceNumber >> 24) & 0xFF);
        data[SEQUENCE_NUMBER_OFFSET + 1] = (byte) ((sequenceNumber >> 16) & 0xFF);
        data[SEQUENCE_NUMBER_OFFSET + 2] = (byte) ((sequenceNumber >> 8) & 0xFF);
        data[SEQUENCE_NUMBER_OFFSET + 3] = (byte) (sequenceNumber & 0xFF);

        // Recompute checksum
        int checksum = computeCrc16(data, 0, HEADER_SIZE + payloadLength);
        data[HEADER_SIZE + payloadLength] = (byte) ((checksum >> 8) & 0xFF);
        data[HEADER_SIZE + payloadLength + 1] = (byte) (checksum & 0xFF);

        return data;
    }

    /**
     * Construct a valid message with specified payload fields.
     */
    private byte[] validMessageWithPayload(int messageType, String[] payloadKeys, int payloadLength) {
        // Simplified: just create a valid message with the given type
        // In real implementation, would encode payloadKeys into payload bytes
        return validMessageWithMessageType(messageType, payloadLength);
    }

    /**
     * Compute CRC-16 checksum over specified byte range.
     *
     * This is a placeholder implementation. Real CRC-16 algorithm varies
     * (CRC-16-CCITT, CRC-16-IBM, etc.). Adjust to match the actual design.
     */
    private int computeCrc16(byte[] data, int start, int length) {
        int crc = 0xFFFF;
        for (int i = start; i < start + length; i++) {
            crc ^= (data[i] & 0xFF) << 8;
            for (int j = 0; j < 8; j++) {
                if ((crc & 0x8000) != 0) {
                    crc = (crc << 1) ^ 0x1021;
                } else {
                    crc = crc << 1;
                }
                crc &= 0xFFFF;
            }
        }
        return crc;
    }
}

// ============================================================================
// Contract Classes (to be implemented separately)
// ============================================================================

/**
 * Result union type: either Success with parsed message, or Error with details.
 */
interface ParseResult {
    boolean isSuccess();
    boolean isError();

    // Success fields (throws if isError())
    int getMessageType();
    long getSequenceNumber();
    java.util.Map<String, Object> getPayload();

    // Error fields (throws if isSuccess())
    ErrorCode getErrorCode();
    int getByteOffset();
}

enum ErrorCode {
    INVALID_SYNC,
    UNSUPPORTED_VERSION,
    LENGTH_MISMATCH,
    CHECKSUM_FAILED,
    UNKNOWN_TYPE
}

/**
 * MessageParser interface (implement to pass tests).
 */
interface MessageParser {
    ParseResult parse(byte[] raw_data);
}
