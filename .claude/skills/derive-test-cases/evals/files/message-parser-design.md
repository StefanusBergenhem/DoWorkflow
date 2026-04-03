---
artifact_id: DD-007
artifact_type: detailed-design
version: 1.0.0
status: approved
component: comm-protocol
unit_id: MessageParser
layer: 2
trace_from: [ARCH-012]
---

# Protocol Message Parser

## Purpose

Parses raw byte arrays into structured protocol messages. Validates message framing, checksums, and field types. Returns either a parsed message or a structured error indicating what went wrong and where.

## Interfaces

### Inputs

| ID | Name | Type | Unit | Constraints | Description |
|----|------|------|------|-------------|-------------|
| I1 | raw_data | byte_array | — | length 4..1024 | Raw bytes received from the communication channel |

### Outputs

| ID | Name | Type | Unit | Constraints | Description |
|----|------|------|------|-------------|-------------|
| O1 | result | union | — | — | Either Success or Error (see below) |
| O1a | result.Success.message_type | integer | — | — | Message type ID from header |
| O1b | result.Success.payload | map | — | — | Key-value pairs of parsed fields |
| O1c | result.Success.sequence_number | integer | — | — | Message sequence number for ordering |
| O1d | result.Error.error_code | enum | — | invalid_sync, unsupported_version, length_mismatch, checksum_failed, unknown_type | What went wrong |
| O1e | result.Error.byte_offset | integer | — | >= 0 | Position in raw_data where error was detected |

## Behavior

| ID | Condition | Result |
|----|-----------|--------|
| B1 | First two bytes of raw_data | Check against SYNC_BYTES (0xAA, 0x55). If mismatch: return Error(invalid_sync, offset 0) |
| B2 | After B1 succeeds | Read version byte at offset 2. If != SUPPORTED_VERSION: return Error(unsupported_version, offset 2) |
| B3 | After B2 succeeds | Read payload_length at offset 3-4 (big-endian uint16). If raw_data.length != payload_length + HEADER_SIZE + CHECKSUM_SIZE: return Error(length_mismatch, offset 3) |
| B4 | After B3 succeeds | Read message_type at offset 5 |
| B5 | After B4 succeeds | Read sequence_number at offset 6-9 (big-endian uint32) |
| B6 | After B5 succeeds | Extract payload bytes from offset HEADER_SIZE to HEADER_SIZE + payload_length |
| B7 | After B6 succeeds | Read last 2 bytes as checksum. Compute CRC-16 over all preceding bytes. If mismatch: return Error(checksum_failed, offset at checksum position) |
| B8 | After B7 succeeds | Look up message_type in type registry. If not found: return Error(unknown_type, offset 5) |
| B9 | After B8 succeeds | Parse payload bytes into typed fields per registry definition |
| B10 | After B9 succeeds | Return Success(message_type, parsed payload map, sequence_number) |

## Error Handling

| ID | Condition | Response |
|----|-----------|----------|
| E1 | raw_data is null or empty | Return Error(invalid_sync, byte_offset 0) |
| E2 | raw_data.length < HEADER_SIZE + CHECKSUM_SIZE | Return Error(length_mismatch, byte_offset 0) |

## Configuration

| Name | Type | Default | Description |
|------|------|---------|-------------|
| SYNC_BYTES | byte_array | [0xAA, 0x55] | Expected sync pattern at start of every message |
| SUPPORTED_VERSION | integer | 1 | Protocol version this parser handles |
| HEADER_SIZE | integer | 10 | Fixed header size in bytes |
| CHECKSUM_SIZE | integer | 2 | CRC-16 checksum size in bytes |

## Constraints

- Must not allocate memory proportional to input size (parse in-place where possible)
- Must be reentrant — no mutable shared state
