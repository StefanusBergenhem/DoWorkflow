package com.example.comm;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.HashMap;

/**
 * Parses raw byte arrays into structured protocol messages.
 * Design: DD-007 (Protocol Message Parser)
 */
public class MessageParser {

    private static final byte[] SYNC_BYTES = {(byte) 0xAA, (byte) 0x55};
    private static final int SUPPORTED_VERSION = 1;
    private static final int HEADER_SIZE = 10;
    private static final int CHECKSUM_SIZE = 2;

    private final MessageTypeRegistry registry;

    public MessageParser(MessageTypeRegistry registry) {
        this.registry = registry;
    }

    // DEFECT 1: God method — this parse method does everything in one place (>50 lines,
    // high cyclomatic complexity). The design's sequential steps B1-B10 should each be
    // their own method.
    public ParseResult parse(byte[] rawData) {
        // E1: null or empty
        if (rawData == null || rawData.length == 0) {
            return new ParseResult.Error(ErrorCode.INVALID_SYNC, 0);
        }

        // DEFECT 2: Missing E2 handling — design says raw_data.length < HEADER_SIZE + CHECKSUM_SIZE
        // should return Error(length_mismatch, 0). This code skips that check entirely and
        // will throw ArrayIndexOutOfBoundsException instead.

        // B1: Check sync bytes
        if (rawData[0] != SYNC_BYTES[0] || rawData[1] != SYNC_BYTES[1]) {
            return new ParseResult.Error(ErrorCode.INVALID_SYNC, 0);
        }

        // B2: Check version
        int version = rawData[2] & 0xFF;
        if (version != SUPPORTED_VERSION) {
            return new ParseResult.Error(ErrorCode.UNSUPPORTED_VERSION, 2);
        }

        // B3: Check length
        int payloadLength = ((rawData[3] & 0xFF) << 8) | (rawData[4] & 0xFF);
        if (rawData.length != payloadLength + HEADER_SIZE + CHECKSUM_SIZE) {
            return new ParseResult.Error(ErrorCode.LENGTH_MISMATCH, 3);
        }

        // B4: Read message type
        int messageType = rawData[5] & 0xFF;

        // B5: Read sequence number
        ByteBuffer seqBuf = ByteBuffer.wrap(rawData, 6, 4);
        int sequenceNumber = seqBuf.getInt();

        // B6: Extract payload
        byte[] payload = new byte[payloadLength];
        System.arraycopy(rawData, HEADER_SIZE, payload, 0, payloadLength);

        // B7: Verify checksum
        int checksumOffset = HEADER_SIZE + payloadLength;
        int expectedChecksum = ((rawData[checksumOffset] & 0xFF) << 8) | (rawData[checksumOffset + 1] & 0xFF);
        int actualChecksum = computeCrc16(rawData, 0, checksumOffset);
        if (actualChecksum != expectedChecksum) {
            return new ParseResult.Error(ErrorCode.CHECKSUM_FAILED, checksumOffset);
        }

        // B8: Look up message type
        MessageTypeDefinition typeDef = registry.lookup(messageType);
        if (typeDef == null) {
            // DEFECT 3: Returns null instead of Error result. Design says
            // "return Error(unknown_type, offset 5)" but this returns null.
            return null;
        }

        // B9: Parse payload fields
        Map<String, Object> parsedFields = typeDef.parsePayload(payload);

        // B10: Return success
        return new ParseResult.Success(messageType, parsedFields, sequenceNumber);
    }

    // DEFECT 4: Gold plating — this method is not in the design. The design specifies
    // only a parse() method. This is functionality beyond what was specified.
    public String formatMessage(int messageType, Map<String, Object> fields) {
        StringBuilder sb = new StringBuilder();
        sb.append("MSG[type=").append(messageType).append("] {");
        for (Map.Entry<String, Object> entry : fields.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append(", ");
        }
        sb.append("}");
        return sb.toString();
    }

    private int computeCrc16(byte[] data, int offset, int length) {
        int crc = 0xFFFF;
        for (int i = offset; i < offset + length; i++) {
            crc ^= (data[i] & 0xFF);
            for (int j = 0; j < 8; j++) {
                if ((crc & 1) != 0) {
                    crc = (crc >>> 1) ^ 0xA001;
                } else {
                    crc >>>= 1;
                }
            }
        }
        return crc;
    }
}
