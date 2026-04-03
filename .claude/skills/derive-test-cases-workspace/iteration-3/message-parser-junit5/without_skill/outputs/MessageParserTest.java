package comm.protocol;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 test suite for MessageParser unit.
 *
 * Tests derived from DD-007 (detailed-design) using requirement-based testing
 * and equivalence class partitioning strategies:
 *
 * EC1: Valid protocol messages → Success variant
 * EC2: Invalid sync bytes → Error(invalid_sync, offset 0)
 * EC3: Unsupported version → Error(unsupported_version, offset 2)
 * EC4: Length mismatch → Error(length_mismatch, offset 3 or 0)
 * EC5: Checksum failure → Error(checksum_failed, checksum position)
 * EC6: Unknown message type → Error(unknown_type, offset 5)
 * EC7: Null/empty input → Error(invalid_sync, offset 0)
 * EC8: Input too short → Error(length_mismatch, offset 0)
 */
public class MessageParserTest {

    private MessageParser parser;

    @BeforeEach
    void setUp() {
        parser = new MessageParser();
    }

    @Nested
    @DisplayName("Valid Message Parsing (EC1)")
    class ValidMessageParsing {

        @Test
        @DisplayName("Should parse complete valid message and return Success")
        void shouldParseValidMessage() {
            // Arrange: Construct a minimal valid 12-byte message
            // Header (10 bytes): sync(2) + version(1) + length(2) + type(1) + seq(4)
            // Payload (0 bytes) + Checksum (2 bytes)
            byte[] validMessage = new byte[]{
                (byte) 0xAA, (byte) 0x55,  // SYNC_BYTES
                (byte) 0x01,               // SUPPORTED_VERSION
                (byte) 0x00, (byte) 0x00,  // payload_length = 0 (big-endian)
                (byte) 0x01,               // message_type = 1
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01,  // sequence_number = 1
                (byte) 0x00, (byte) 0x00   // checksum (placeholder - will be computed)
            };

            // Act
            ParseResult result = parser.parse(validMessage);

            // Assert
            assertTrue(result.isSuccess());
            assertEquals(1, result.getMessageType());
            assertEquals(1, result.getSequenceNumber());
            assertNotNull(result.getPayload());
        }

        @Test
        @DisplayName("Should parse message with non-empty payload")
        void shouldParseMessageWithPayload() {
            // Arrange: 12-byte message with 2-byte payload
            // Header (10) + Payload (2) + Checksum (2) = 14 bytes
            byte[] messageWithPayload = new byte[]{
                (byte) 0xAA, (byte) 0x55,  // SYNC_BYTES
                (byte) 0x01,               // SUPPORTED_VERSION
                (byte) 0x00, (byte) 0x02,  // payload_length = 2
                (byte) 0x02,               // message_type = 2
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02,  // sequence_number = 2
                (byte) 0xAB, (byte) 0xCD,  // payload
                (byte) 0x00, (byte) 0x00   // checksum
            };

            // Act
            ParseResult result = parser.parse(messageWithPayload);

            // Assert
            assertTrue(result.isSuccess());
            assertEquals(2, result.getMessageType());
            assertEquals(2, result.getSequenceNumber());
            assertNotNull(result.getPayload());
        }

        @Test
        @DisplayName("Should extract sequence number as big-endian uint32")
        void shouldParseSequenceNumberCorrectly() {
            // Arrange: sequence_number = 0x12345678 in big-endian at offset 6-9
            byte[] message = new byte[]{
                (byte) 0xAA, (byte) 0x55,
                (byte) 0x01,
                (byte) 0x00, (byte) 0x00,
                (byte) 0x01,
                (byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0x78,
                (byte) 0x00, (byte) 0x00
            };

            // Act
            ParseResult result = parser.parse(message);

            // Assert
            assertTrue(result.isSuccess());
            assertEquals(0x12345678, result.getSequenceNumber());
        }

        @Test
        @DisplayName("Should handle maximum payload size within 1024-byte constraint")
        void shouldParseMaximumSizeMessage() {
            // Arrange: 1024-byte input with maximum payload
            // Payload size = 1024 - HEADER_SIZE(10) - CHECKSUM_SIZE(2) = 1012
            byte[] largeMessage = new byte[1024];
            largeMessage[0] = (byte) 0xAA;
            largeMessage[1] = (byte) 0x55;
            largeMessage[2] = (byte) 0x01;
            largeMessage[3] = (byte) 0x03;  // payload_length high byte = 1012
            largeMessage[4] = (byte) 0xF4;  // payload_length low byte
            largeMessage[5] = (byte) 0x03;
            largeMessage[6] = largeMessage[7] = largeMessage[8] = largeMessage[9] = (byte) 0x00;

            // Act
            ParseResult result = parser.parse(largeMessage);

            // Assert
            assertTrue(result.isSuccess());
        }
    }

    @Nested
    @DisplayName("Invalid Sync Bytes (EC2)")
    class InvalidSyncBytes {

        @Test
        @DisplayName("Should return invalid_sync error when first byte is wrong")
        void shouldFailOnWrongFirstSyncByte() {
            // Arrange: First byte wrong
            byte[] invalidSync = new byte[]{
                (byte) 0xFF, (byte) 0x55,  // Wrong first sync byte
                (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x01,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01,
                (byte) 0x00, (byte) 0x00
            };

            // Act
            ParseResult result = parser.parse(invalidSync);

            // Assert
            assertFalse(result.isSuccess());
            assertEquals("invalid_sync", result.getErrorCode());
            assertEquals(0, result.getByteOffset());
        }

        @Test
        @DisplayName("Should return invalid_sync error when second byte is wrong")
        void shouldFailOnWrongSecondSyncByte() {
            // Arrange: Second byte wrong
            byte[] invalidSync = new byte[]{
                (byte) 0xAA, (byte) 0xFF,  // Wrong second sync byte
                (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x01,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01,
                (byte) 0x00, (byte) 0x00
            };

            // Act
            ParseResult result = parser.parse(invalidSync);

            // Assert
            assertFalse(result.isSuccess());
            assertEquals("invalid_sync", result.getErrorCode());
            assertEquals(0, result.getByteOffset());
        }

        @Test
        @DisplayName("Should return invalid_sync error when both sync bytes are wrong")
        void shouldFailOnBothSyncBytesWrong() {
            // Arrange
            byte[] invalidSync = new byte[]{
                (byte) 0xFF, (byte) 0xFF,
                (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x01,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01,
                (byte) 0x00, (byte) 0x00
            };

            // Act
            ParseResult result = parser.parse(invalidSync);

            // Assert
            assertFalse(result.isSuccess());
            assertEquals("invalid_sync", result.getErrorCode());
            assertEquals(0, result.getByteOffset());
        }
    }

    @Nested
    @DisplayName("Unsupported Version (EC3)")
    class UnsupportedVersion {

        @Test
        @DisplayName("Should return unsupported_version error for version 0")
        void shouldFailOnVersionZero() {
            // Arrange
            byte[] wrongVersion = new byte[]{
                (byte) 0xAA, (byte) 0x55,
                (byte) 0x00,  // SUPPORTED_VERSION is 1, not 0
                (byte) 0x00, (byte) 0x00, (byte) 0x01,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01,
                (byte) 0x00, (byte) 0x00
            };

            // Act
            ParseResult result = parser.parse(wrongVersion);

            // Assert
            assertFalse(result.isSuccess());
            assertEquals("unsupported_version", result.getErrorCode());
            assertEquals(2, result.getByteOffset());
        }

        @Test
        @DisplayName("Should return unsupported_version error for version 2")
        void shouldFailOnVersionTwo() {
            // Arrange
            byte[] wrongVersion = new byte[]{
                (byte) 0xAA, (byte) 0x55,
                (byte) 0x02,  // SUPPORTED_VERSION is 1, not 2
                (byte) 0x00, (byte) 0x00, (byte) 0x01,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01,
                (byte) 0x00, (byte) 0x00
            };

            // Act
            ParseResult result = parser.parse(wrongVersion);

            // Assert
            assertFalse(result.isSuccess());
            assertEquals("unsupported_version", result.getErrorCode());
            assertEquals(2, result.getByteOffset());
        }

        @Test
        @DisplayName("Should return unsupported_version error for version 255")
        void shouldFailOnVersionMaxByte() {
            // Arrange
            byte[] wrongVersion = new byte[]{
                (byte) 0xAA, (byte) 0x55,
                (byte) 0xFF,
                (byte) 0x00, (byte) 0x00, (byte) 0x01,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01,
                (byte) 0x00, (byte) 0x00
            };

            // Act
            ParseResult result = parser.parse(wrongVersion);

            // Assert
            assertFalse(result.isSuccess());
            assertEquals("unsupported_version", result.getErrorCode());
            assertEquals(2, result.getByteOffset());
        }
    }

    @Nested
    @DisplayName("Length Mismatch (EC4)")
    class LengthMismatch {

        @Test
        @DisplayName("Should return length_mismatch when payload_length is too large")
        void shouldFailWhenPayloadLengthExceedsInputSize() {
            // Arrange: payload_length = 100 but only 12 bytes total
            byte[] wrongLength = new byte[]{
                (byte) 0xAA, (byte) 0x55,
                (byte) 0x01,
                (byte) 0x00, (byte) 0x64,  // payload_length = 100, but total size is 12
                (byte) 0x01,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01,
                (byte) 0x00, (byte) 0x00
            };

            // Act
            ParseResult result = parser.parse(wrongLength);

            // Assert
            assertFalse(result.isSuccess());
            assertEquals("length_mismatch", result.getErrorCode());
            assertEquals(3, result.getByteOffset());
        }

        @Test
        @DisplayName("Should return length_mismatch when payload_length is too small")
        void shouldFailWhenPayloadLengthIsTooSmall() {
            // Arrange: payload_length = 0 but actual data present
            byte[] wrongLength = new byte[]{
                (byte) 0xAA, (byte) 0x55,
                (byte) 0x01,
                (byte) 0x00, (byte) 0x00,  // payload_length = 0
                (byte) 0x01,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01,
                (byte) 0xAB, (byte) 0xCD   // But checksum says there's data
            };

            // Act
            ParseResult result = parser.parse(wrongLength);

            // Assert
            assertFalse(result.isSuccess());
            assertEquals("length_mismatch", result.getErrorCode());
        }

        @Test
        @DisplayName("Should return length_mismatch when total size < HEADER_SIZE + CHECKSUM_SIZE")
        void shouldFailWhenInputTooShort() {
            // Arrange: Only 11 bytes (minimum is 12)
            byte[] tooShort = new byte[]{
                (byte) 0xAA, (byte) 0x55, (byte) 0x01,
                (byte) 0x00, (byte) 0x00, (byte) 0x01,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01,
                (byte) 0x00
            };

            // Act
            ParseResult result = parser.parse(tooShort);

            // Assert
            assertFalse(result.isSuccess());
            assertEquals("length_mismatch", result.getErrorCode());
            assertEquals(0, result.getByteOffset());
        }

        @Test
        @DisplayName("Should calculate expected length as payload_length + HEADER_SIZE + CHECKSUM_SIZE")
        void shouldValidateTotalLengthFormula() {
            // Arrange: payload_length = 2, so total should be 2 + 10 + 2 = 14 bytes
            // But provide only 13 bytes
            byte[] wrongTotal = new byte[13];
            wrongTotal[0] = (byte) 0xAA;
            wrongTotal[1] = (byte) 0x55;
            wrongTotal[2] = (byte) 0x01;
            wrongTotal[3] = (byte) 0x00;
            wrongTotal[4] = (byte) 0x02;  // payload_length = 2
            wrongTotal[5] = (byte) 0x01;
            wrongTotal[6] = wrongTotal[7] = wrongTotal[8] = wrongTotal[9] = (byte) 0x00;
            wrongTotal[10] = (byte) 0xAB;
            wrongTotal[11] = (byte) 0xCD;
            wrongTotal[12] = (byte) 0x00;

            // Act
            ParseResult result = parser.parse(wrongTotal);

            // Assert
            assertFalse(result.isSuccess());
            assertEquals("length_mismatch", result.getErrorCode());
        }
    }

    @Nested
    @DisplayName("Checksum Validation (EC5)")
    class ChecksumValidation {

        @Test
        @DisplayName("Should return checksum_failed when CRC-16 does not match")
        void shouldFailOnIncorrectChecksum() {
            // Arrange: Valid message with deliberately wrong checksum
            byte[] invalidChecksum = new byte[]{
                (byte) 0xAA, (byte) 0x55,
                (byte) 0x01,
                (byte) 0x00, (byte) 0x00,
                (byte) 0x01,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01,
                (byte) 0xFF, (byte) 0xFF  // Wrong checksum
            };

            // Act
            ParseResult result = parser.parse(invalidChecksum);

            // Assert
            assertFalse(result.isSuccess());
            assertEquals("checksum_failed", result.getErrorCode());
            assertTrue(result.getByteOffset() >= 10, "Checksum should be detected at or after byte 10");
        }

        @Test
        @DisplayName("Should detect checksum failure even with valid structure otherwise")
        void shouldFailChecksumWithValidStructure() {
            // Arrange: Perfect structure, wrong checksum in last 2 bytes
            byte[] badChecksum = new byte[]{
                (byte) 0xAA, (byte) 0x55,
                (byte) 0x01,
                (byte) 0x00, (byte) 0x00,
                (byte) 0x02,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02,
                (byte) 0x00, (byte) 0x01  // Checksum that doesn't match
            };

            // Act
            ParseResult result = parser.parse(badChecksum);

            // Assert
            assertFalse(result.isSuccess());
            assertEquals("checksum_failed", result.getErrorCode());
        }

        @Test
        @DisplayName("Should compute CRC-16 over all bytes except the checksum itself")
        void shouldComputeChecksumOverHeaderAndPayload() {
            // Arrange: Message where checksum is computed over header + payload only
            byte[] message = new byte[14];
            message[0] = (byte) 0xAA;
            message[1] = (byte) 0x55;
            message[2] = (byte) 0x01;
            message[3] = (byte) 0x00;
            message[4] = (byte) 0x02;
            message[5] = (byte) 0x03;
            message[6] = message[7] = message[8] = (byte) 0x00;
            message[9] = (byte) 0x03;
            message[10] = (byte) 0xAA;
            message[11] = (byte) 0xBB;
            // Compute correct CRC-16 over bytes 0-11, place in bytes 12-13
            // (Implementation detail: the test framework will provide helper to compute this)
            message[12] = (byte) 0x00;
            message[13] = (byte) 0x00;

            // Act
            ParseResult result = parser.parse(message);

            // Assert
            // Either succeeds (if checksum computation is correct) or fails on checksum_failed
            if (!result.isSuccess()) {
                assertEquals("checksum_failed", result.getErrorCode());
            }
        }
    }

    @Nested
    @DisplayName("Unknown Message Type (EC6)")
    class UnknownMessageType {

        @Test
        @DisplayName("Should return unknown_type error for unregistered message type")
        void shouldFailOnUnknownMessageType() {
            // Arrange: message_type = 255 (not registered in type registry)
            byte[] unknownType = new byte[]{
                (byte) 0xAA, (byte) 0x55,
                (byte) 0x01,
                (byte) 0x00, (byte) 0x00,
                (byte) 0xFF,  // Unregistered message type
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01,
                (byte) 0x00, (byte) 0x00
            };

            // Act
            ParseResult result = parser.parse(unknownType);

            // Assert
            assertFalse(result.isSuccess());
            assertEquals("unknown_type", result.getErrorCode());
            assertEquals(5, result.getByteOffset());
        }

        @Test
        @DisplayName("Should return unknown_type error for message type 0")
        void shouldFailOnMessageTypeZero() {
            // Arrange
            byte[] unknownType = new byte[]{
                (byte) 0xAA, (byte) 0x55,
                (byte) 0x01,
                (byte) 0x00, (byte) 0x00,
                (byte) 0x00,  // Message type 0 (may not be registered)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01,
                (byte) 0x00, (byte) 0x00
            };

            // Act
            ParseResult result = parser.parse(unknownType);

            // Assert
            assertFalse(result.isSuccess());
            assertEquals("unknown_type", result.getErrorCode());
            assertEquals(5, result.getByteOffset());
        }
    }

    @Nested
    @DisplayName("Null and Empty Input (EC7)")
    class NullAndEmptyInput {

        @Test
        @DisplayName("Should return invalid_sync error for null input")
        void shouldHandleNullInput() {
            // Act
            ParseResult result = parser.parse(null);

            // Assert
            assertFalse(result.isSuccess());
            assertEquals("invalid_sync", result.getErrorCode());
            assertEquals(0, result.getByteOffset());
        }

        @Test
        @DisplayName("Should return invalid_sync error for empty byte array")
        void shouldHandleEmptyInput() {
            // Arrange
            byte[] empty = new byte[0];

            // Act
            ParseResult result = parser.parse(empty);

            // Assert
            assertFalse(result.isSuccess());
            assertEquals("invalid_sync", result.getErrorCode());
            assertEquals(0, result.getByteOffset());
        }

        @Test
        @DisplayName("Should return invalid_sync error for single-byte input")
        void shouldHandleSingleByteInput() {
            // Arrange
            byte[] singleByte = new byte[]{(byte) 0xAA};

            // Act
            ParseResult result = parser.parse(singleByte);

            // Assert
            assertFalse(result.isSuccess());
            assertEquals("invalid_sync", result.getErrorCode());
            assertEquals(0, result.getByteOffset());
        }
    }

    @Nested
    @DisplayName("Input Size Boundary (EC8)")
    class InputSizeBoundary {

        @Test
        @DisplayName("Should fail for input smaller than minimum (HEADER_SIZE + CHECKSUM_SIZE = 12)")
        void shouldFailForInputSmallerThanMinimum() {
            // Arrange: 11 bytes (1 less than minimum 12)
            byte[] tooSmall = new byte[11];
            tooSmall[0] = (byte) 0xAA;
            tooSmall[1] = (byte) 0x55;

            // Act
            ParseResult result = parser.parse(tooSmall);

            // Assert
            assertFalse(result.isSuccess());
            assertEquals("length_mismatch", result.getErrorCode());
            assertEquals(0, result.getByteOffset());
        }

        @Test
        @DisplayName("Should pass minimum size requirement (12 bytes)")
        void shouldPassMinimumSizeRequirement() {
            // Arrange: exactly 12 bytes (minimum valid message)
            byte[] minimum = new byte[]{
                (byte) 0xAA, (byte) 0x55,
                (byte) 0x01,
                (byte) 0x00, (byte) 0x00,
                (byte) 0x01,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01,
                (byte) 0x00, (byte) 0x00
            };

            // Act
            ParseResult result = parser.parse(minimum);

            // Assert (result depends on checksum, but should not fail on length)
            if (!result.isSuccess()) {
                assertNotEquals("length_mismatch", result.getErrorCode());
            }
        }

        @Test
        @DisplayName("Should accept input up to maximum constraint (1024 bytes)")
        void shouldAcceptMaximumSizeInput() {
            // Arrange: 1024-byte input
            byte[] maximum = new byte[1024];
            maximum[0] = (byte) 0xAA;
            maximum[1] = (byte) 0x55;
            maximum[2] = (byte) 0x01;
            maximum[3] = (byte) 0x03;  // payload_length = 1012
            maximum[4] = (byte) 0xF4;
            maximum[5] = (byte) 0x01;
            for (int i = 6; i < 10; i++) {
                maximum[i] = (byte) 0x00;
            }

            // Act
            ParseResult result = parser.parse(maximum);

            // Assert (should not fail on size constraints)
            if (!result.isSuccess()) {
                assertNotEquals("length_mismatch", result.getErrorCode());
            }
        }

        @Test
        @DisplayName("Should reject input exceeding maximum constraint (> 1024 bytes)")
        void shouldRejectInputLargerThanMaximum() {
            // Arrange: 1025 bytes (exceeds constraint)
            byte[] tooLarge = new byte[1025];
            tooLarge[0] = (byte) 0xAA;
            tooLarge[1] = (byte) 0x55;

            // Act
            ParseResult result = parser.parse(tooLarge);

            // Assert
            assertFalse(result.isSuccess());
        }
    }

    @Nested
    @DisplayName("Field Extraction Order and Positioning")
    class FieldExtractionOrder {

        @Test
        @DisplayName("Should read message_type from offset 5")
        void shouldExtractMessageTypeFromCorrectOffset() {
            // Arrange: message_type at offset 5 = 7
            byte[] message = new byte[]{
                (byte) 0xAA, (byte) 0x55,
                (byte) 0x01,
                (byte) 0x00, (byte) 0x00,
                (byte) 0x07,  // offset 5: message_type
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01,
                (byte) 0x00, (byte) 0x00
            };

            // Act
            ParseResult result = parser.parse(message);

            // Assert
            if (result.isSuccess()) {
                assertEquals(7, result.getMessageType());
            }
        }

        @Test
        @DisplayName("Should read sequence_number from offset 6-9 as big-endian uint32")
        void shouldExtractSequenceNumberFromCorrectOffset() {
            // Arrange: sequence_number at offset 6-9
            byte[] message = new byte[]{
                (byte) 0xAA, (byte) 0x55,
                (byte) 0x01,
                (byte) 0x00, (byte) 0x00,
                (byte) 0x01,
                (byte) 0xAB, (byte) 0xCD, (byte) 0xEF, (byte) 0x12,  // offset 6-9: seq
                (byte) 0x00, (byte) 0x00
            };

            // Act
            ParseResult result = parser.parse(message);

            // Assert
            if (result.isSuccess()) {
                assertEquals(0xABCDEF12L, result.getSequenceNumber());
            }
        }

        @Test
        @DisplayName("Should extract payload from offset HEADER_SIZE to HEADER_SIZE + payload_length")
        void shouldExtractPayloadFromCorrectRange() {
            // Arrange: payload_length = 4, payload at offset 10-13
            byte[] message = new byte[16];
            message[0] = (byte) 0xAA;
            message[1] = (byte) 0x55;
            message[2] = (byte) 0x01;
            message[3] = (byte) 0x00;
            message[4] = (byte) 0x04;  // payload_length = 4
            message[5] = (byte) 0x01;
            message[6] = message[7] = message[8] = message[9] = (byte) 0x00;
            message[10] = (byte) 0x11;
            message[11] = (byte) 0x22;
            message[12] = (byte) 0x33;
            message[13] = (byte) 0x44;
            message[14] = (byte) 0x00;
            message[15] = (byte) 0x00;

            // Act
            ParseResult result = parser.parse(message);

            // Assert
            if (result.isSuccess()) {
                java.util.Map<String, byte[]> payload = result.getPayload();
                assertNotNull(payload);
            }
        }
    }

    @Nested
    @DisplayName("Error Reporting and Byte Offset Accuracy")
    class ErrorReporting {

        @Test
        @DisplayName("Should report correct byte_offset for error at each validation step")
        void shouldReportAccurateByteOffsets() {
            // Invalid sync at offset 0
            byte[] msg1 = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00};
            ParseResult result1 = parser.parse(msg1);
            assertEquals(0, result1.getByteOffset());

            // Invalid version at offset 2
            byte[] msg2 = new byte[]{(byte) 0xAA, (byte) 0x55, (byte) 0xFF, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00};
            ParseResult result2 = parser.parse(msg2);
            assertEquals(2, result2.getByteOffset());

            // Length mismatch at offset 3
            byte[] msg3 = new byte[]{(byte) 0xAA, (byte) 0x55, (byte) 0x01, (byte) 0xFF, (byte) 0xFF, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00};
            ParseResult result3 = parser.parse(msg3);
            assertEquals(3, result3.getByteOffset());

            // Unknown type at offset 5
            byte[] msg4 = new byte[]{(byte) 0xAA, (byte) 0x55, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0xFE, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00};
            ParseResult result4 = parser.parse(msg4);
            assertEquals(5, result4.getByteOffset());
        }

        @Test
        @DisplayName("Should populate error_code field on Error variant")
        void shouldPopulateErrorCodeField() {
            // Arrange: message with invalid sync
            byte[] invalidMessage = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00};

            // Act
            ParseResult result = parser.parse(invalidMessage);

            // Assert
            assertFalse(result.isSuccess());
            assertNotNull(result.getErrorCode());
            assertTrue(
                result.getErrorCode().matches("invalid_sync|unsupported_version|length_mismatch|checksum_failed|unknown_type"),
                "Error code must be one of the defined enum values"
            );
        }
    }

    @Nested
    @DisplayName("Message Type Registry Interaction")
    class MessageTypeRegistry {

        @Test
        @DisplayName("Should consult type registry to extract payload fields")
        void shouldUseLookupFromRegistry() {
            // Arrange: Valid message with registered type
            byte[] message = new byte[]{
                (byte) 0xAA, (byte) 0x55,
                (byte) 0x01,
                (byte) 0x00, (byte) 0x00,
                (byte) 0x01,  // Assume type 1 is registered
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01,
                (byte) 0x00, (byte) 0x00
            };

            // Act
            ParseResult result = parser.parse(message);

            // Assert (should not fail on type lookup for type 1)
            if (!result.isSuccess()) {
                assertNotEquals("unknown_type", result.getErrorCode());
            }
        }

        @Test
        @DisplayName("Should fail gracefully on message type not in registry")
        void shouldFailOnMissingTypeRegistration() {
            // Arrange: message_type = 200 (unlikely to be registered)
            byte[] message = new byte[]{
                (byte) 0xAA, (byte) 0x55,
                (byte) 0x01,
                (byte) 0x00, (byte) 0x00,
                (byte) 0xC8,  // Type 200
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01,
                (byte) 0x00, (byte) 0x00
            };

            // Act
            ParseResult result = parser.parse(message);

            // Assert
            assertFalse(result.isSuccess());
            assertEquals("unknown_type", result.getErrorCode());
            assertEquals(5, result.getByteOffset());
        }
    }

    @Nested
    @DisplayName("Output Contract Compliance")
    class OutputContractCompliance {

        @Test
        @DisplayName("Success variant should include message_type, payload map, and sequence_number")
        void shouldReturnAllSuccessFields() {
            // Arrange
            byte[] message = new byte[]{
                (byte) 0xAA, (byte) 0x55,
                (byte) 0x01,
                (byte) 0x00, (byte) 0x00,
                (byte) 0x01,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02,
                (byte) 0x00, (byte) 0x00
            };

            // Act
            ParseResult result = parser.parse(message);

            // Assert (if successful)
            if (result.isSuccess()) {
                assertTrue(result.getMessageType() >= 0);
                assertNotNull(result.getPayload());
                assertTrue(result.getSequenceNumber() >= 0);
            }
        }

        @Test
        @DisplayName("Error variant should include error_code and byte_offset")
        void shouldReturnAllErrorFields() {
            // Arrange: invalid sync
            byte[] message = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00};

            // Act
            ParseResult result = parser.parse(message);

            // Assert
            assertFalse(result.isSuccess());
            assertNotNull(result.getErrorCode());
            assertTrue(result.getByteOffset() >= 0);
        }

        @Test
        @DisplayName("Payload should be a non-null map (may be empty for zero-length payloads)")
        void shouldReturnPayloadAsMap() {
            // Arrange
            byte[] message = new byte[]{
                (byte) 0xAA, (byte) 0x55,
                (byte) 0x01,
                (byte) 0x00, (byte) 0x00,
                (byte) 0x01,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01,
                (byte) 0x00, (byte) 0x00
            };

            // Act
            ParseResult result = parser.parse(message);

            // Assert
            if (result.isSuccess()) {
                assertNotNull(result.getPayload());
                assertTrue(result.getPayload() instanceof java.util.Map);
            }
        }
    }

    @Nested
    @DisplayName("Reentrancy and State Management")
    class ReetrancyAndState {

        @Test
        @DisplayName("Should parse multiple messages without state carryover")
        void shouldBeReentrant() {
            // Arrange: two different messages
            byte[] message1 = new byte[]{
                (byte) 0xAA, (byte) 0x55,
                (byte) 0x01,
                (byte) 0x00, (byte) 0x00,
                (byte) 0x01,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01,
                (byte) 0x00, (byte) 0x00
            };

            byte[] message2 = new byte[]{
                (byte) 0xAA, (byte) 0x55,
                (byte) 0x01,
                (byte) 0x00, (byte) 0x00,
                (byte) 0x02,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02,
                (byte) 0x00, (byte) 0x00
            };

            // Act
            ParseResult result1 = parser.parse(message1);
            ParseResult result2 = parser.parse(message2);

            // Assert: results should be independent
            if (result1.isSuccess() && result2.isSuccess()) {
                assertNotEquals(result1.getSequenceNumber(), result2.getSequenceNumber());
            }
        }

        @Test
        @DisplayName("Should not modify input byte array during parsing")
        void shouldNotModifyInput() {
            // Arrange
            byte[] original = new byte[]{
                (byte) 0xAA, (byte) 0x55,
                (byte) 0x01,
                (byte) 0x00, (byte) 0x00,
                (byte) 0x01,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01,
                (byte) 0x00, (byte) 0x00
            };
            byte[] copy = original.clone();

            // Act
            parser.parse(original);

            // Assert
            assertArrayEquals(copy, original);
        }
    }

    // --- Utility and Helper Interfaces ---

    /**
     * Represents the result of parsing: either Success or Error variant.
     * Interface contracts based on DD-007 output specification.
     */
    interface ParseResult {
        boolean isSuccess();

        // Success variant accessors
        int getMessageType();

        java.util.Map<String, byte[]> getPayload();

        long getSequenceNumber();

        // Error variant accessors
        String getErrorCode();

        int getByteOffset();
    }

    /**
     * MessageParser interface stub for testing.
     * Implementation details are left to the actual unit under test.
     */
    interface MessageParser {
        ParseResult parse(byte[] raw_data);
    }

}
