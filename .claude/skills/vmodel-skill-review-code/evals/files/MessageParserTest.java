package com.example.comm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.ByteBuffer;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for MessageParser — derived from design DD-007.
 */
class MessageParserTest {

    private MessageParser parser;
    private StubMessageTypeRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new StubMessageTypeRegistry();
        registry.register(1, new SimpleFieldDefinition("value", 4));
        parser = new MessageParser(registry);
    }

    // --- E1: null or empty input ---

    @Test
    void nullInput_returnsInvalidSyncError() {
        ParseResult result = parser.parse(null);
        assertError(result, ErrorCode.INVALID_SYNC, 0);
    }

    @Test
    void emptyInput_returnsInvalidSyncError() {
        ParseResult result = parser.parse(new byte[0]);
        assertError(result, ErrorCode.INVALID_SYNC, 0);
    }

    // --- E2: too short for header + checksum ---

    @Test
    void inputShorterThanMinimum_returnsLengthMismatch() {
        byte[] data = new byte[]{(byte) 0xAA, (byte) 0x55, 0x01};
        ParseResult result = parser.parse(data);
        assertError(result, ErrorCode.LENGTH_MISMATCH, 0);
    }

    // --- B1: sync byte validation ---

    @Test
    void wrongFirstSyncByte_returnsInvalidSync() {
        byte[] data = buildValidMessage(1, new byte[]{0, 0, 0, 1});
        data[0] = 0x00;
        ParseResult result = parser.parse(data);
        assertError(result, ErrorCode.INVALID_SYNC, 0);
    }

    @Test
    void wrongSecondSyncByte_returnsInvalidSync() {
        byte[] data = buildValidMessage(1, new byte[]{0, 0, 0, 1});
        data[1] = 0x00;
        ParseResult result = parser.parse(data);
        assertError(result, ErrorCode.INVALID_SYNC, 0);
    }

    // --- B2: version validation ---

    @Test
    void unsupportedVersion_returnsVersionError() {
        byte[] data = buildValidMessage(1, new byte[]{0, 0, 0, 1});
        data[2] = 99;
        ParseResult result = parser.parse(data);
        assertError(result, ErrorCode.UNSUPPORTED_VERSION, 2);
    }

    // --- B3: length validation ---

    @Test
    void payloadLengthMismatch_returnsLengthError() {
        byte[] data = buildValidMessage(1, new byte[]{0, 0, 0, 1});
        // Corrupt payload length field to claim more bytes than present
        data[3] = 0;
        data[4] = 99;
        ParseResult result = parser.parse(data);
        assertError(result, ErrorCode.LENGTH_MISMATCH, 3);
    }

    // --- B7: checksum validation ---

    @Test
    void invalidChecksum_returnsChecksumError() {
        byte[] data = buildValidMessage(1, new byte[]{0, 0, 0, 1});
        // Corrupt checksum bytes
        data[data.length - 1] = (byte) 0xFF;
        data[data.length - 2] = (byte) 0xFF;
        ParseResult result = parser.parse(data);
        assertError(result, ErrorCode.CHECKSUM_FAILED, 14);
    }

    @Test
    void validChecksum_parsesSuccessfully() {
        byte[] payload = new byte[]{0, 0, 0, 42};
        byte[] data = buildValidMessage(1, payload);
        ParseResult result = parser.parse(data);
        assertSuccess(result, 1);
    }

    // --- B8: unknown message type ---

    @Test
    void unknownMessageType_returnsUnknownTypeError() {
        byte[] data = buildValidMessage(99, new byte[]{0, 0, 0, 1});
        ParseResult result = parser.parse(data);
        assertError(result, ErrorCode.UNKNOWN_TYPE, 5);
    }

    // --- B10: successful parse ---

    @Test
    void validMessage_returnsSuccessWithParsedFields() {
        byte[] payload = new byte[]{0, 0, 1, 0};
        byte[] data = buildValidMessage(1, payload);
        // Set sequence number to 7
        data[6] = 0; data[7] = 0; data[8] = 0; data[9] = 7;
        recomputeChecksum(data);

        ParseResult result = parser.parse(data);
        assertTrue(result instanceof ParseResult.Success);
        ParseResult.Success success = (ParseResult.Success) result;
        assertEquals(1, success.messageType());
        assertEquals(7, success.sequenceNumber());
        assertNotNull(success.payload());
    }

    // --- Boundary: minimum valid message (4-byte payload) ---

    @Test
    void minimumLengthMessage_parsesSuccessfully() {
        byte[] payload = new byte[]{0, 0, 0, 0};
        byte[] data = buildValidMessage(1, payload);
        ParseResult result = parser.parse(data);
        assertSuccess(result, 1);
    }

    // --- Helpers ---

    private byte[] buildValidMessage(int messageType, byte[] payload) {
        int totalLength = 10 + payload.length + 2;
        byte[] data = new byte[totalLength];

        // Sync bytes
        data[0] = (byte) 0xAA;
        data[1] = (byte) 0x55;
        // Version
        data[2] = 1;
        // Payload length (big-endian uint16)
        data[3] = (byte) ((payload.length >> 8) & 0xFF);
        data[4] = (byte) (payload.length & 0xFF);
        // Message type
        data[5] = (byte) messageType;
        // Sequence number (default 0)
        data[6] = 0; data[7] = 0; data[8] = 0; data[9] = 0;
        // Payload
        System.arraycopy(payload, 0, data, 10, payload.length);
        // Checksum
        recomputeChecksum(data);
        return data;
    }

    private void recomputeChecksum(byte[] data) {
        int checksumOffset = data.length - 2;
        int crc = 0xFFFF;
        for (int i = 0; i < checksumOffset; i++) {
            crc ^= (data[i] & 0xFF);
            for (int j = 0; j < 8; j++) {
                if ((crc & 1) != 0) {
                    crc = (crc >>> 1) ^ 0xA001;
                } else {
                    crc >>>= 1;
                }
            }
        }
        data[checksumOffset] = (byte) ((crc >> 8) & 0xFF);
        data[checksumOffset + 1] = (byte) (crc & 0xFF);
    }

    private void assertError(ParseResult result, ErrorCode expectedCode, int expectedOffset) {
        assertInstanceOf(ParseResult.Error.class, result);
        ParseResult.Error error = (ParseResult.Error) result;
        assertEquals(expectedCode, error.errorCode());
        assertEquals(expectedOffset, error.byteOffset());
    }

    private void assertSuccess(ParseResult result, int expectedType) {
        assertInstanceOf(ParseResult.Success.class, result);
        ParseResult.Success success = (ParseResult.Success) result;
        assertEquals(expectedType, success.messageType());
    }
}
