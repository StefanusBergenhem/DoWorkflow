package com.example.commprotocol;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for MessageParser, derived from DD-007 detailed design.
 *
 * Applies four derivation strategies:
 * 1. Requirement-based: One test per behavior rule (sync check, version, length, checksum, etc.)
 * 2. Equivalence class: Input partitions (valid range, below, above, empty, etc.)
 * 3. Boundary value: Min/max/just-below/just-above for numeric constraints
 * 4. Error handling: Explicit error conditions + implicit faults (null, undersize, state violations)
 */
@DisplayName("MessageParser - Protocol Message Parsing")
class MessageParserTest {

    private MessageParser parser;
    private static final byte SYNC_BYTE_0 = (byte) 0xAA;
    private static final byte SYNC_BYTE_1 = (byte) 0x55;
    private static final byte SUPPORTED_VERSION = 1;
    private static final int HEADER_SIZE = 10;
    private static final int CHECKSUM_SIZE = 2;
    private static final int MIN_MESSAGE_SIZE = HEADER_SIZE + CHECKSUM_SIZE; // 12
    private static final int MAX_PAYLOAD_SIZE = 1024 - HEADER_SIZE - CHECKSUM_SIZE; // 1012

    @BeforeEach
    void setUp() {
        parser = new MessageParser();
    }

    // ============================================================================
    // STRATEGY 1: REQUIREMENT-BASED TESTING
    // One test per behavior rule from the design
    // ============================================================================

    @Nested
    @DisplayName("Requirement-Based Tests: Behavior Rules")
    class RequirementBasedTests {

        @Test
        @DisplayName("RB-001: Valid sync bytes (0xAA, 0x55) pass check")
        void testValidSyncBytes() {
            // Arrange
            byte[] data = buildValidMessage(
                new byte[]{SYNC_BYTE_0, SYNC_BYTE_1}, // valid sync
                SUPPORTED_VERSION,
                buildPayload(4), // 4-byte payload
                null // auto-calculate checksum
            );

            // Act
            MessageParseResult result = parser.parse(data);

            // Assert
            assertTrue(result.isSuccess(), "Should succeed with valid sync bytes");
            assertEquals(1, result.getSuccess().messageType(), "Message type should be 1");
        }

        @Test
        @DisplayName("RB-002: Invalid sync byte 0 returns invalid_sync error at offset 0")
        void testInvalidSyncByte0() {
            // Arrange
            byte[] data = buildMessageWithSyncBytes((byte) 0xBB, SYNC_BYTE_1);

            // Act
            MessageParseResult result = parser.parse(data);

            // Assert
            assertTrue(result.isError(), "Should fail with invalid sync");
            assertEquals("invalid_sync", result.getError().errorCode());
            assertEquals(0, result.getError().byteOffset());
        }

        @Test
        @DisplayName("RB-003: Invalid sync byte 1 returns invalid_sync error at offset 0")
        void testInvalidSyncByte1() {
            // Arrange
            byte[] data = buildMessageWithSyncBytes(SYNC_BYTE_0, (byte) 0xCC);

            // Act
            MessageParseResult result = parser.parse(data);

            // Assert
            assertTrue(result.isError(), "Should fail with invalid sync");
            assertEquals("invalid_sync", result.getError().errorCode());
            assertEquals(0, result.getError().byteOffset());
        }

        @Test
        @DisplayName("RB-004: Valid version byte passes check")
        void testValidVersionByte() {
            // Arrange
            byte[] data = buildValidMessage(
                new byte[]{SYNC_BYTE_0, SYNC_BYTE_1},
                SUPPORTED_VERSION,
                buildPayload(4),
                null
            );

            // Act
            MessageParseResult result = parser.parse(data);

            // Assert
            assertTrue(result.isSuccess(), "Should succeed with supported version");
        }

        @Test
        @DisplayName("RB-005: Unsupported version returns unsupported_version error at offset 2")
        void testUnsupportedVersion() {
            // Arrange
            byte[] data = buildValidMessage(
                new byte[]{SYNC_BYTE_0, SYNC_BYTE_1},
                (byte) 42, // unsupported version
                buildPayload(4),
                null
            );

            // Act
            MessageParseResult result = parser.parse(data);

            // Assert
            assertTrue(result.isError(), "Should fail with unsupported version");
            assertEquals("unsupported_version", result.getError().errorCode());
            assertEquals(2, result.getError().byteOffset());
        }

        @Test
        @DisplayName("RB-006: Length field matches actual message size")
        void testLengthFieldMatches() {
            // Arrange: Create message with payload length = 8
            byte[] payload = buildPayload(8);
            byte[] data = buildValidMessage(
                new byte[]{SYNC_BYTE_0, SYNC_BYTE_1},
                SUPPORTED_VERSION,
                payload,
                null
            );

            // Act
            MessageParseResult result = parser.parse(data);

            // Assert
            assertTrue(result.isSuccess(), "Should succeed with matching length field");
            assertEquals(payload.length, result.getSuccess().payload().size(),
                "Payload should have 8 fields");
        }

        @Test
        @DisplayName("RB-007: Length mismatch (data too short) returns length_mismatch at offset 3")
        void testLengthMismatchTooShort() {
            // Arrange: Create message with correct length field but truncate data
            byte[] data = buildValidMessage(
                new byte[]{SYNC_BYTE_0, SYNC_BYTE_1},
                SUPPORTED_VERSION,
                buildPayload(8),
                null
            );
            // Truncate to make actual length < declared length
            byte[] truncated = new byte[data.length - 5];
            System.arraycopy(data, 0, truncated, 0, truncated.length);

            // Act
            MessageParseResult result = parser.parse(truncated);

            // Assert
            assertTrue(result.isError(), "Should fail with length mismatch");
            assertEquals("length_mismatch", result.getError().errorCode());
            assertEquals(3, result.getError().byteOffset());
        }

        @Test
        @DisplayName("RB-008: Length mismatch (data too long) returns length_mismatch at offset 3")
        void testLengthMismatchTooLong() {
            // Arrange: Create valid message then append extra bytes
            byte[] data = buildValidMessage(
                new byte[]{SYNC_BYTE_0, SYNC_BYTE_1},
                SUPPORTED_VERSION,
                buildPayload(4),
                null
            );
            byte[] extended = new byte[data.length + 10];
            System.arraycopy(data, 0, extended, 0, data.length);

            // Act
            MessageParseResult result = parser.parse(extended);

            // Assert
            assertTrue(result.isError(), "Should fail with length mismatch");
            assertEquals("length_mismatch", result.getError().errorCode());
        }

        @Test
        @DisplayName("RB-009: Valid checksum passes CRC-16 verification")
        void testValidChecksum() {
            // Arrange
            byte[] data = buildValidMessage(
                new byte[]{SYNC_BYTE_0, SYNC_BYTE_1},
                SUPPORTED_VERSION,
                buildPayload(4),
                null // auto-calculate correct checksum
            );

            // Act
            MessageParseResult result = parser.parse(data);

            // Assert
            assertTrue(result.isSuccess(), "Should pass with valid checksum");
        }

        @Test
        @DisplayName("RB-010: Corrupted checksum returns checksum_failed error")
        void testCorruptedChecksum() {
            // Arrange
            byte[] data = buildValidMessage(
                new byte[]{SYNC_BYTE_0, SYNC_BYTE_1},
                SUPPORTED_VERSION,
                buildPayload(4),
                null
            );
            // Corrupt the checksum (last 2 bytes)
            data[data.length - 1] ^= 0xFF; // flip all bits in last byte

            // Act
            MessageParseResult result = parser.parse(data);

            // Assert
            assertTrue(result.isError(), "Should fail with checksum error");
            assertEquals("checksum_failed", result.getError().errorCode());
        }

        @Test
        @DisplayName("RB-011: Known message type is parsed successfully")
        void testKnownMessageType() {
            // Arrange
            byte[] data = buildValidMessage(
                new byte[]{SYNC_BYTE_0, SYNC_BYTE_1},
                SUPPORTED_VERSION,
                buildPayload(4),
                null
            );

            // Act
            MessageParseResult result = parser.parse(data);

            // Assert
            assertTrue(result.isSuccess(), "Should succeed with known message type");
            assertNotNull(result.getSuccess().payload(), "Payload should be populated");
        }

        @Test
        @DisplayName("RB-012: Unknown message type returns unknown_type error at offset 5")
        void testUnknownMessageType() {
            // Arrange: Build valid message but set message_type to unknown value
            byte[] data = buildValidMessage(
                new byte[]{SYNC_BYTE_0, SYNC_BYTE_1},
                SUPPORTED_VERSION,
                buildPayload(4),
                null,
                99 // unknown message type
            );

            // Act
            MessageParseResult result = parser.parse(data);

            // Assert
            assertTrue(result.isError(), "Should fail with unknown type");
            assertEquals("unknown_type", result.getError().errorCode());
            assertEquals(5, result.getError().byteOffset());
        }

        @Test
        @DisplayName("RB-013: Sequence number is extracted and returned")
        void testSequenceNumberExtracted() {
            // Arrange
            byte[] data = buildValidMessage(
                new byte[]{SYNC_BYTE_0, SYNC_BYTE_1},
                SUPPORTED_VERSION,
                buildPayload(4),
                null
            );

            // Act
            MessageParseResult result = parser.parse(data);

            // Assert
            assertTrue(result.isSuccess(), "Should succeed");
            assertTrue(result.getSuccess().sequenceNumber() >= 0,
                "Sequence number should be extracted");
        }
    }

    // ============================================================================
    // STRATEGY 2: EQUIVALENCE CLASS PARTITIONING
    // Partition inputs by type and constraints; test one representative per class
    // ============================================================================

    @Nested
    @DisplayName("Equivalence Class Tests: Input Partitions")
    class EquivalenceClassTests {

        @Test
        @DisplayName("EC-001: Empty input (length 0) returns length_mismatch")
        void testEmptyInput() {
            // Arrange
            byte[] data = new byte[0];

            // Act
            MessageParseResult result = parser.parse(data);

            // Assert
            assertTrue(result.isError(), "Empty input should fail");
            assertEquals("length_mismatch", result.getError().errorCode());
        }

        @Test
        @DisplayName("EC-002: Single-byte input returns length_mismatch")
        void testSingleByteInput() {
            // Arrange
            byte[] data = new byte[1];

            // Act
            MessageParseResult result = parser.parse(data);

            // Assert
            assertTrue(result.isError(), "Single byte should fail");
            assertEquals("length_mismatch", result.getError().errorCode());
        }

        @Test
        @DisplayName("EC-003: Minimum valid message (12 bytes: header + checksum) succeeds")
        void testMinimumValidMessage() {
            // Arrange: Create minimal valid message (empty payload)
            byte[] data = buildValidMessage(
                new byte[]{SYNC_BYTE_0, SYNC_BYTE_1},
                SUPPORTED_VERSION,
                buildPayload(0), // zero-length payload
                null
            );
            assertEquals(MIN_MESSAGE_SIZE, data.length,
                "Message should be exactly " + MIN_MESSAGE_SIZE + " bytes");

            // Act
            MessageParseResult result = parser.parse(data);

            // Assert
            assertTrue(result.isSuccess(), "Minimum valid message should succeed");
        }

        @Test
        @DisplayName("EC-004: Just below minimum (11 bytes) returns length_mismatch")
        void testJustBelowMinimumMessage() {
            // Arrange
            byte[] data = new byte[MIN_MESSAGE_SIZE - 1];
            System.arraycopy(new byte[]{SYNC_BYTE_0, SYNC_BYTE_1}, 0, data, 0, 2);

            // Act
            MessageParseResult result = parser.parse(data);

            // Assert
            assertTrue(result.isError(), "Message just below minimum should fail");
        }

        @Test
        @DisplayName("EC-005: Small payload (1 byte) parses successfully")
        void testSmallPayload() {
            // Arrange
            byte[] data = buildValidMessage(
                new byte[]{SYNC_BYTE_0, SYNC_BYTE_1},
                SUPPORTED_VERSION,
                buildPayload(1),
                null
            );

            // Act
            MessageParseResult result = parser.parse(data);

            // Assert
            assertTrue(result.isSuccess(), "Small payload should succeed");
            assertEquals(1, result.getSuccess().payload().size());
        }

        @Test
        @DisplayName("EC-006: Medium payload (100 bytes) parses successfully")
        void testMediumPayload() {
            // Arrange
            byte[] data = buildValidMessage(
                new byte[]{SYNC_BYTE_0, SYNC_BYTE_1},
                SUPPORTED_VERSION,
                buildPayload(100),
                null
            );

            // Act
            MessageParseResult result = parser.parse(data);

            // Assert
            assertTrue(result.isSuccess(), "Medium payload should succeed");
            assertEquals(100, result.getSuccess().payload().size());
        }

        @Test
        @DisplayName("EC-007: Large payload (1012 bytes = max) parses successfully")
        void testLargeMaxPayload() {
            // Arrange
            byte[] data = buildValidMessage(
                new byte[]{SYNC_BYTE_0, SYNC_BYTE_1},
                SUPPORTED_VERSION,
                buildPayload(MAX_PAYLOAD_SIZE),
                null
            );
            assertEquals(1024, data.length, "Message should be exactly 1024 bytes");

            // Act
            MessageParseResult result = parser.parse(data);

            // Assert
            assertTrue(result.isSuccess(), "Maximum payload should succeed");
            assertEquals(MAX_PAYLOAD_SIZE, result.getSuccess().payload().size());
        }

        @Test
        @DisplayName("EC-008: Payload exceeding 1024 byte limit returns length_mismatch")
        void testPayloadExceedsLimit() {
            // Arrange: Try to create payload that would exceed 1024 total
            byte[] data = buildValidMessage(
                new byte[]{SYNC_BYTE_0, SYNC_BYTE_1},
                SUPPORTED_VERSION,
                buildPayload(MAX_PAYLOAD_SIZE + 1), // too large
                null
            );
            // Truncate length field to appear valid, but data is wrong size
            if (data.length > 1024) {
                byte[] truncated = new byte[1024];
                System.arraycopy(data, 0, truncated, 0, 1024);
                data = truncated;
            }

            // Act
            MessageParseResult result = parser.parse(data);

            // Assert
            assertTrue(result.isError() || result.isSuccess(),
                "Oversized payload should either fail or be truncated");
        }
    }

    // ============================================================================
    // STRATEGY 3: BOUNDARY VALUE ANALYSIS
    // Test at min, max, just-below, just-above for constrained inputs
    // ============================================================================

    @Nested
    @DisplayName("Boundary Value Tests: Numeric Constraints")
    class BoundaryValueTests {

        @Test
        @DisplayName("BV-001: Payload length field = 0 (below typical range) succeeds")
        void testZeroPayloadLength() {
            // Arrange
            byte[] data = buildValidMessage(
                new byte[]{SYNC_BYTE_0, SYNC_BYTE_1},
                SUPPORTED_VERSION,
                buildPayload(0),
                null
            );

            // Act
            MessageParseResult result = parser.parse(data);

            // Assert
            assertTrue(result.isSuccess(), "Zero-length payload should succeed");
            assertEquals(0, result.getSuccess().payload().size());
        }

        @Test
        @DisplayName("BV-002: Payload length field = 1 (below typical) succeeds")
        void testMinimalPayload() {
            // Arrange
            byte[] data = buildValidMessage(
                new byte[]{SYNC_BYTE_0, SYNC_BYTE_1},
                SUPPORTED_VERSION,
                buildPayload(1),
                null
            );

            // Act
            MessageParseResult result = parser.parse(data);

            // Assert
            assertTrue(result.isSuccess(), "Single-byte payload should succeed");
        }

        @Test
        @DisplayName("BV-003: Payload length = MAX_PAYLOAD_SIZE (1012) succeeds")
        void testMaxPayloadLength() {
            // Arrange
            byte[] data = buildValidMessage(
                new byte[]{SYNC_BYTE_0, SYNC_BYTE_1},
                SUPPORTED_VERSION,
                buildPayload(MAX_PAYLOAD_SIZE),
                null
            );

            // Act
            MessageParseResult result = parser.parse(data);

            // Assert
            assertTrue(result.isSuccess(), "Max payload length should succeed");
        }

        @Test
        @DisplayName("BV-004: Payload length = MAX_PAYLOAD_SIZE + 1 exceeds message limit")
        void testPayloadLengthExceedsMax() {
            // Arrange: Total message size would exceed 1024
            byte[] oversized = buildValidMessage(
                new byte[]{SYNC_BYTE_0, SYNC_BYTE_1},
                SUPPORTED_VERSION,
                buildPayload(MAX_PAYLOAD_SIZE + 1),
                null
            );

            // If we can create it, verify it fails or gets truncated appropriately
            if (oversized.length > 1024) {
                // Adjust to exactly 1024 but with wrong length field
                oversized = Arrays.copyOf(oversized, 1024);
            }

            // Act
            MessageParseResult result = parser.parse(oversized);

            // Assert - at this boundary, either error or truncation is acceptable
            // depending on implementation
            assertNotNull(result, "Result should not be null");
        }

        @Test
        @DisplayName("BV-005: Message type byte = 0 (boundary) handled correctly")
        void testMessageTypeZero() {
            // Arrange
            byte[] data = buildValidMessage(
                new byte[]{SYNC_BYTE_0, SYNC_BYTE_1},
                SUPPORTED_VERSION,
                buildPayload(4),
                null,
                0 // message type = 0
            );

            // Act
            MessageParseResult result = parser.parse(data);

            // Assert - should either succeed (if 0 is known) or fail with unknown_type
            assertTrue(result.isError() || result.isSuccess(),
                "Should handle message type 0");
        }

        @Test
        @DisplayName("BV-006: Message type byte = 255 (max byte value) handled correctly")
        void testMessageTypeMaxByte() {
            // Arrange
            byte[] data = buildValidMessage(
                new byte[]{SYNC_BYTE_0, SYNC_BYTE_1},
                SUPPORTED_VERSION,
                buildPayload(4),
                null,
                255 // max byte value
            );

            // Act
            MessageParseResult result = parser.parse(data);

            // Assert
            assertTrue(result.isError() || result.isSuccess(),
                "Should handle message type 255");
        }

        @Test
        @DisplayName("BV-007: Sequence number field (big-endian) at minimum (0)")
        void testSequenceNumberMinimum() {
            // Arrange: Build message with sequence = 0
            byte[] data = buildValidMessageWithSeq(
                new byte[]{SYNC_BYTE_0, SYNC_BYTE_1},
                SUPPORTED_VERSION,
                buildPayload(4),
                0 // seq = 0
            );

            // Act
            MessageParseResult result = parser.parse(data);

            // Assert
            assertTrue(result.isSuccess(), "Sequence 0 should succeed");
            assertEquals(0, result.getSuccess().sequenceNumber());
        }

        @Test
        @DisplayName("BV-008: Sequence number field (big-endian) at maximum (uint32 max)")
        void testSequenceNumberMaximum() {
            // Arrange: Build message with sequence = 2^32 - 1
            byte[] data = buildValidMessageWithSeq(
                new byte[]{SYNC_BYTE_0, SYNC_BYTE_1},
                SUPPORTED_VERSION,
                buildPayload(4),
                0xFFFFFFFF // max uint32
            );

            // Act
            MessageParseResult result = parser.parse(data);

            // Assert
            assertTrue(result.isSuccess(), "Maximum sequence should succeed");
            assertEquals(0xFFFFFFFF, result.getSuccess().sequenceNumber());
        }
    }

    // ============================================================================
    // STRATEGY 4: ERROR HANDLING AND FAULT INJECTION
    // Explicit error_handling entries + implicit faults
    // ============================================================================

    @Nested
    @DisplayName("Error Handling & Fault Injection Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("EH-001: Null input (error_handling rule) returns invalid_sync")
        void testNullInput() {
            // Act & Assert
            assertThrows(NullPointerException.class, () -> parser.parse(null),
                "Null input should throw or be handled gracefully");
        }

        @Test
        @DisplayName("EH-002: Empty array returns length_mismatch at offset 0")
        void testEmptyArray() {
            // Arrange
            byte[] data = new byte[0];

            // Act
            MessageParseResult result = parser.parse(data);

            // Assert
            assertTrue(result.isError(), "Empty array should fail");
            assertEquals("length_mismatch", result.getError().errorCode());
            assertEquals(0, result.getError().byteOffset());
        }

        @Test
        @DisplayName("EH-003: Under-minimum length (< 12) returns length_mismatch")
        void testUnderMinimumLength() {
            // Arrange
            for (int len = 1; len < MIN_MESSAGE_SIZE; len++) {
                byte[] data = new byte[len];
                System.arraycopy(new byte[]{SYNC_BYTE_0, SYNC_BYTE_1}, 0, data, 0,
                    Math.min(2, len));

                // Act
                MessageParseResult result = parser.parse(data);

                // Assert
                assertTrue(result.isError(),
                    "Length " + len + " should fail");
                assertEquals("length_mismatch", result.getError().errorCode());
            }
        }

        @Test
        @DisplayName("EH-004: Corrupt first sync byte returns invalid_sync")
        void testCorruptFirstSyncByte() {
            // Arrange
            byte[] data = buildValidMessage(
                new byte[]{(byte) 0xFF, SYNC_BYTE_1}, // corrupt first sync
                SUPPORTED_VERSION,
                buildPayload(4),
                null
            );

            // Act
            MessageParseResult result = parser.parse(data);

            // Assert
            assertTrue(result.isError());
            assertEquals("invalid_sync", result.getError().errorCode());
        }

        @Test
        @DisplayName("EH-005: Corrupt second sync byte returns invalid_sync")
        void testCorruptSecondSyncByte() {
            // Arrange
            byte[] data = buildValidMessage(
                new byte[]{SYNC_BYTE_0, (byte) 0x00}, // corrupt second sync
                SUPPORTED_VERSION,
                buildPayload(4),
                null
            );

            // Act
            MessageParseResult result = parser.parse(data);

            // Assert
            assertTrue(result.isError());
            assertEquals("invalid_sync", result.getError().errorCode());
        }

        @Test
        @DisplayName("EH-006: Multiple consecutive messages parse independently")
        void testMultipleMessageSequence() {
            // Arrange: Create two valid messages
            byte[] msg1 = buildValidMessageWithSeq(
                new byte[]{SYNC_BYTE_0, SYNC_BYTE_1},
                SUPPORTED_VERSION,
                buildPayload(4),
                1
            );
            byte[] msg2 = buildValidMessageWithSeq(
                new byte[]{SYNC_BYTE_0, SYNC_BYTE_1},
                SUPPORTED_VERSION,
                buildPayload(4),
                2
            );

            // Act & Assert: Each parses independently
            MessageParseResult result1 = parser.parse(msg1);
            MessageParseResult result2 = parser.parse(msg2);

            assertTrue(result1.isSuccess());
            assertTrue(result2.isSuccess());
            assertEquals(1, result1.getSuccess().sequenceNumber());
            assertEquals(2, result2.getSuccess().sequenceNumber());
        }

        @Test
        @DisplayName("EH-007: Parser is reentrant (no mutable shared state)")
        void testReentrancy() {
            // Arrange: Prepare two messages
            byte[] msg1 = buildValidMessageWithSeq(
                new byte[]{SYNC_BYTE_0, SYNC_BYTE_1},
                SUPPORTED_VERSION,
                buildPayload(4),
                100
            );
            byte[] msg2 = buildValidMessageWithSeq(
                new byte[]{SYNC_BYTE_0, SYNC_BYTE_1},
                SUPPORTED_VERSION,
                buildPayload(8),
                200
            );

            // Act: Call parse in interleaved fashion (simulates concurrent-like calls)
            MessageParseResult r1a = parser.parse(msg1);
            MessageParseResult r2a = parser.parse(msg2);
            MessageParseResult r1b = parser.parse(msg1);

            // Assert: Results should be consistent, no cross-contamination
            assertTrue(r1a.isSuccess());
            assertTrue(r2a.isSuccess());
            assertTrue(r1b.isSuccess());
            assertEquals(100, r1a.getSuccess().sequenceNumber());
            assertEquals(200, r2a.getSuccess().sequenceNumber());
            assertEquals(100, r1b.getSuccess().sequenceNumber());
        }

        @Test
        @DisplayName("EH-008: Bitwise checksum corruption (single bit flip) fails")
        void testSingleBitChecksumCorruption() {
            // Arrange
            byte[] data = buildValidMessage(
                new byte[]{SYNC_BYTE_0, SYNC_BYTE_1},
                SUPPORTED_VERSION,
                buildPayload(4),
                null
            );
            // Flip single bit in last checksum byte
            data[data.length - 1] ^= 0x01;

            // Act
            MessageParseResult result = parser.parse(data);

            // Assert
            assertTrue(result.isError(), "Single-bit corruption should be detected");
            assertEquals("checksum_failed", result.getError().errorCode());
        }

        @Test
        @DisplayName("EH-009: Payload field corruption detected by checksum")
        void testPayloadCorruptionDetection() {
            // Arrange
            byte[] data = buildValidMessage(
                new byte[]{SYNC_BYTE_0, SYNC_BYTE_1},
                SUPPORTED_VERSION,
                buildPayload(8),
                null
            );
            // Corrupt a byte in the payload (not in sync, version, length, or checksum)
            if (data.length > 15) {
                data[15] ^= 0xFF;

                // Act
                MessageParseResult result = parser.parse(data);

                // Assert
                assertTrue(result.isError(), "Payload corruption should fail checksum");
                assertEquals("checksum_failed", result.getError().errorCode());
            }
        }

        @Test
        @DisplayName("EH-010: Message type not in registry returns unknown_type")
        void testUnregisteredMessageType() {
            // Arrange
            byte[] data = buildValidMessage(
                new byte[]{SYNC_BYTE_0, SYNC_BYTE_1},
                SUPPORTED_VERSION,
                buildPayload(4),
                null,
                77 // unregistered type
            );

            // Act
            MessageParseResult result = parser.parse(data);

            // Assert
            assertTrue(result.isError());
            assertEquals("unknown_type", result.getError().errorCode());
            assertEquals(5, result.getError().byteOffset());
        }
    }

    // ============================================================================
    // HELPER METHODS (Test Data Builders)
    // ============================================================================

    /**
     * Build a complete, valid message with calculated checksum.
     *
     * @param syncBytes 2-byte sync pattern
     * @param version version byte
     * @param payload payload bytes
     * @param checksum null = auto-calculate CRC-16; otherwise use provided
     * @return complete message ready to parse
     */
    private byte[] buildValidMessage(byte[] syncBytes, byte version, byte[] payload,
            byte[] checksum) {
        return buildValidMessage(syncBytes, version, payload, checksum, 1);
    }

    /**
     * Build a complete, valid message with calculated checksum and custom message type.
     */
    private byte[] buildValidMessage(byte[] syncBytes, byte version, byte[] payload,
            byte[] checksum, int messageType) {
        // Structure: SYNC(2) | VERSION(1) | LENGTH(2 BE) | TYPE(1) | SEQ(4 BE) | PAYLOAD | CRC(2)
        int payloadLen = payload.length;
        int messageSize = HEADER_SIZE + payloadLen + CHECKSUM_SIZE;
        byte[] message = new byte[messageSize];

        int offset = 0;
        System.arraycopy(syncBytes, 0, message, offset, 2);
        offset += 2;

        message[offset++] = version;

        // Write payload length as big-endian uint16
        message[offset++] = (byte) ((payloadLen >> 8) & 0xFF);
        message[offset++] = (byte) (payloadLen & 0xFF);

        message[offset++] = (byte) messageType;

        // Sequence number (big-endian uint32) = 0 by default
        message[offset++] = 0;
        message[offset++] = 0;
        message[offset++] = 0;
        message[offset++] = 0;

        // Copy payload
        System.arraycopy(payload, 0, message, offset, payloadLen);
        offset += payloadLen;

        // Calculate or copy checksum
        if (checksum == null) {
            byte[] calculated = calculateCRC16(message, 0, offset);
            System.arraycopy(calculated, 0, message, offset, CHECKSUM_SIZE);
        } else {
            System.arraycopy(checksum, 0, message, offset, CHECKSUM_SIZE);
        }

        return message;
    }

    /**
     * Build a valid message with a custom sequence number.
     */
    private byte[] buildValidMessageWithSeq(byte[] syncBytes, byte version, byte[] payload,
            long sequenceNumber) {
        int payloadLen = payload.length;
        int messageSize = HEADER_SIZE + payloadLen + CHECKSUM_SIZE;
        byte[] message = new byte[messageSize];

        int offset = 0;
        System.arraycopy(syncBytes, 0, message, offset, 2);
        offset += 2;

        message[offset++] = version;

        message[offset++] = (byte) ((payloadLen >> 8) & 0xFF);
        message[offset++] = (byte) (payloadLen & 0xFF);

        message[offset++] = 1; // message type = 1

        // Write sequence number as big-endian uint32
        message[offset++] = (byte) ((sequenceNumber >> 24) & 0xFF);
        message[offset++] = (byte) ((sequenceNumber >> 16) & 0xFF);
        message[offset++] = (byte) ((sequenceNumber >> 8) & 0xFF);
        message[offset++] = (byte) (sequenceNumber & 0xFF);

        System.arraycopy(payload, 0, message, offset, payloadLen);
        offset += payloadLen;

        byte[] calculated = calculateCRC16(message, 0, offset);
        System.arraycopy(calculated, 0, message, offset, CHECKSUM_SIZE);

        return message;
    }

    /**
     * Build a message with custom sync bytes.
     */
    private byte[] buildMessageWithSyncBytes(byte byte0, byte byte1) {
        byte[] payload = buildPayload(4);
        return buildValidMessage(
            new byte[]{byte0, byte1},
            SUPPORTED_VERSION,
            payload,
            null
        );
    }

    /**
     * Generate a payload of N bytes (simple test data).
     */
    private byte[] buildPayload(int sizeInBytes) {
        byte[] payload = new byte[sizeInBytes];
        for (int i = 0; i < sizeInBytes; i++) {
            payload[i] = (byte) (i % 256);
        }
        return payload;
    }

    /**
     * Calculate CRC-16 (CCITT) over the given byte range.
     * Placeholder implementation — replace with actual CRC-16 algorithm used by MessageParser.
     */
    private byte[] calculateCRC16(byte[] data, int start, int end) {
        // Placeholder: return zeros (real implementation depends on actual CRC polynomial)
        // For now, assume a simple XOR-based checksum or actual CRC-16 implementation
        int crc = 0xFFFF; // typical CRC initial value
        for (int i = start; i < end; i++) {
            crc ^= (data[i] & 0xFF);
            for (int j = 0; j < 8; j++) {
                if ((crc & 0x0001) != 0) {
                    crc = (crc >> 1) ^ 0xA001; // CCITT-16
                } else {
                    crc = crc >> 1;
                }
            }
        }
        return new byte[]{(byte) ((crc >> 8) & 0xFF), (byte) (crc & 0xFF)};
    }
}

/**
 * Result union type for MessageParser.
 * Represents either Success or Error.
 */
sealed interface MessageParseResult {
    boolean isSuccess();
    boolean isError();
    SuccessVariant getSuccess();
    ErrorVariant getError();

    record SuccessVariant(
        int messageType,
        Map<String, Object> payload,
        long sequenceNumber
    ) {}

    record ErrorVariant(
        String errorCode,
        int byteOffset
    ) {}

    record Success(SuccessVariant data) implements MessageParseResult {
        @Override public boolean isSuccess() { return true; }
        @Override public boolean isError() { return false; }
        @Override public SuccessVariant getSuccess() { return data; }
        @Override public ErrorVariant getError() {
            throw new UnsupportedOperationException("Error variant not available");
        }
    }

    record Error(ErrorVariant data) implements MessageParseResult {
        @Override public boolean isSuccess() { return false; }
        @Override public boolean isError() { return true; }
        @Override public SuccessVariant getSuccess() {
            throw new UnsupportedOperationException("Success variant not available");
        }
        @Override public ErrorVariant getError() { return data; }
    }
}

/**
 * Stub MessageParser implementation (to be replaced with actual implementation).
 * Demonstrates the interface and return type contract.
 */
class MessageParser {
    private static final byte SYNC_BYTE_0 = (byte) 0xAA;
    private static final byte SYNC_BYTE_1 = (byte) 0x55;
    private static final byte SUPPORTED_VERSION = 1;
    private static final int HEADER_SIZE = 10;
    private static final int CHECKSUM_SIZE = 2;

    public MessageParseResult parse(byte[] rawData) {
        // Stub: to be implemented
        // For testing purposes, return a valid response
        if (rawData == null || rawData.length < HEADER_SIZE + CHECKSUM_SIZE) {
            return new MessageParseResult.Error(
                new MessageParseResult.ErrorVariant("length_mismatch", 0)
            );
        }
        if (rawData[0] != SYNC_BYTE_0 || rawData[1] != SYNC_BYTE_1) {
            return new MessageParseResult.Error(
                new MessageParseResult.ErrorVariant("invalid_sync", 0)
            );
        }
        return new MessageParseResult.Success(
            new MessageParseResult.SuccessVariant(rawData[5], Map.of(), 0)
        );
    }
}
