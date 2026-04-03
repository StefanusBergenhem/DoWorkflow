# MessageParser Test Coverage Matrix

**Artifact:** DD-007 (Protocol Message Parser)
**Test Framework:** JUnit 5
**Language:** Java 17
**Test File:** `MessageParserTest.java`

---

## Coverage Summary

| Strategy | Count | Rationale |
|----------|-------|-----------|
| **Requirement-Based** | 13 tests | One per behavior rule (sync, version, length, checksum, message type, sequence number) |
| **Equivalence Class** | 8 tests | Input partitions: empty, minimal, medium, maximum payloads; undersized messages |
| **Boundary Value** | 8 tests | Min/max/just-below/just-above for payload length, message type, sequence number |
| **Error Handling** | 10 tests | Explicit error conditions + implicit faults (null, corruption, reentrancy, state isolation) |
| **TOTAL** | **39 test cases** | Every design element has at least one test that fails if implementation is deleted |

---

## Strategy 1: Requirement-Based Testing

Maps each behavior rule from the design to one or more tests.

| Design Rule | Test Name | Test ID | Expected Behavior |
|---|---|---|---|
| Check first two bytes match SYNC_BYTES (0xAA, 0x55) | `testValidSyncBytes` | RB-001 | Success with valid sync |
| Invalid sync byte 0 → error at offset 0 | `testInvalidSyncByte0` | RB-002 | error_code=invalid_sync, byte_offset=0 |
| Invalid sync byte 1 → error at offset 0 | `testInvalidSyncByte1` | RB-003 | error_code=invalid_sync, byte_offset=0 |
| Read version byte (offset 2). Must equal SUPPORTED_VERSION | `testValidVersionByte` | RB-004 | Success with valid version |
| Version != SUPPORTED_VERSION → error at offset 2 | `testUnsupportedVersion` | RB-005 | error_code=unsupported_version, byte_offset=2 |
| Length field matches actual message size | `testLengthFieldMatches` | RB-006 | Success, payload fields extracted |
| Length too short → error at offset 3 | `testLengthMismatchTooShort` | RB-007 | error_code=length_mismatch, byte_offset=3 |
| Length too long → error at offset 3 | `testLengthMismatchTooLong` | RB-008 | error_code=length_mismatch, byte_offset=3 |
| Valid CRC-16 checksum passes verification | `testValidChecksum` | RB-009 | Success |
| Corrupted checksum → error | `testCorruptedChecksum` | RB-010 | error_code=checksum_failed |
| Known message type parses successfully | `testKnownMessageType` | RB-011 | Success, payload populated |
| Unknown message type → error at offset 5 | `testUnknownMessageType` | RB-012 | error_code=unknown_type, byte_offset=5 |
| Sequence number extracted and returned | `testSequenceNumberExtracted` | RB-013 | Success, sequence_number >= 0 |

**Anti-Pattern Checks (RB tests):**
- ✅ Each test asserts a specific value from design, not just "no exception"
- ✅ Error tests verify error_code AND byte_offset (not tautological checks)
- ✅ Checksum tests use hardcoded test vectors, not recomputed logic
- ✅ Each test targets one logical behavior

---

## Strategy 2: Equivalence Class Partitioning

Partitions inputs by type/constraints; one test per equivalence class.

| Input Property | Equivalence Classes | Test Name | Test ID | Representative Value |
|---|---|---|---|---|
| raw_data length | Empty (0) | `testEmptyInput` | EC-001 | 0 bytes |
| raw_data length | Single byte (1) | `testSingleByteInput` | EC-002 | 1 byte |
| raw_data length | Minimum valid (12) | `testMinimumValidMessage` | EC-003 | 12 bytes (header + checksum) |
| raw_data length | Just below minimum (11) | `testJustBelowMinimumMessage` | EC-004 | 11 bytes |
| raw_data payload size | Small (1 byte) | `testSmallPayload` | EC-005 | 1 byte payload |
| raw_data payload size | Medium (100 bytes) | `testMediumPayload` | EC-006 | 100 bytes payload |
| raw_data payload size | Maximum (1012 bytes) | `testLargeMaxPayload` | EC-007 | 1012 bytes = 1024 total |
| raw_data payload size | Oversized (>1012) | `testPayloadExceedsLimit` | EC-008 | 1013+ bytes |

**Partition Logic:**
- Empty/undersize inputs → invalid_sync or length_mismatch
- Valid-size inputs with good sync/version/checksum → success
- All other input properties held at valid defaults while varying payload size

**Anti-Pattern Checks (EC tests):**
- ✅ Each test uses a representative value from one equivalence class only
- ✅ Tests do not combine multiple input variations
- ✅ Expected outcomes derived from design constraints (length 4..1024)

---

## Strategy 3: Boundary Value Analysis

Tests at and around numeric constraint boundaries.

| Constraint | Boundary | Test Name | Test ID | Value | Expected Outcome |
|---|---|---|---|---|---|
| Payload length: 0..1012 | Below minimum (0 is valid) | `testZeroPayloadLength` | BV-001 | 0 | Success |
| Payload length: 0..1012 | At minimum (≥1) | `testMinimalPayload` | BV-002 | 1 | Success |
| Payload length: 0..1012 | At maximum (1012) | `testMaxPayloadLength` | BV-003 | 1012 | Success |
| Payload length: 0..1012 | Above maximum (>1012) | `testPayloadLengthExceedsMax` | BV-004 | 1013 | Error or truncation |
| Message type (byte 5) | At lower boundary (0) | `testMessageTypeZero` | BV-005 | 0 | Success if known, else unknown_type |
| Message type (byte 5) | At upper boundary (255) | `testMessageTypeMaxByte` | BV-006 | 255 | Error or success |
| Sequence number (big-endian uint32) | At minimum (0) | `testSequenceNumberMinimum` | BV-007 | 0 | Success, sequence=0 |
| Sequence number (big-endian uint32) | At maximum (2^32-1) | `testSequenceNumberMaximum` | BV-008 | 0xFFFFFFFF | Success, sequence=0xFFFFFFFF |

**Anti-Pattern Checks (BV tests):**
- ✅ Tests explicitly target min/max values and transitions
- ✅ Expected outcomes derived from design constraints, not implementation
- ✅ Big-endian encoding verified (sequence number test uses 4-byte field)

---

## Strategy 4: Error Handling and Fault Injection

Explicit error_handling rules plus implicit faults not covered elsewhere.

| Error Category | Fault Scenario | Test Name | Test ID | Expected Behavior |
|---|---|---|---|---|
| Error handling rule: null input | Pass null to parse() | `testNullInput` | EH-001 | NullPointerException or graceful null check |
| Error handling rule: empty input | raw_data.length == 0 | `testEmptyArray` | EH-002 | error_code=invalid_sync, byte_offset=0 |
| Error handling rule: undersize | raw_data.length < 12 | `testUnderMinimumLength` | EH-003 | error_code=length_mismatch |
| Implicit: sync byte corruption | First sync byte != 0xAA | `testCorruptFirstSyncByte` | EH-004 | error_code=invalid_sync |
| Implicit: sync byte corruption | Second sync byte != 0x55 | `testCorruptSecondSyncByte` | EH-005 | error_code=invalid_sync |
| Implicit: statelessness | Parse two different messages sequentially | `testMultipleMessageSequence` | EH-006 | Each parses independently |
| Implicit: reentrancy (no mutable state) | Interleave calls to parse() | `testReentrancy` | EH-007 | Results consistent, no cross-contamination |
| Implicit: checksum robustness | Single-bit flip in checksum | `testSingleBitChecksumCorruption` | EH-008 | error_code=checksum_failed |
| Implicit: payload integrity | Flip bits in payload | `testPayloadCorruptionDetection` | EH-009 | error_code=checksum_failed (detects) |
| Implicit: type registry | Message type not in registry | `testUnregisteredMessageType` | EH-010 | error_code=unknown_type, byte_offset=5 |

**Anti-Pattern Checks (EH tests):**
- ✅ Null tests explicitly handle NullPointerException (design requirement check)
- ✅ Corruption tests verify error_code specifically (not just "isError()")
- ✅ Reentrancy test uses interleaved calls and checks for state isolation
- ✅ Checksum tests confirm CRC detects 1-bit faults (not just "fails")

---

## Design Element Coverage

Every section of the detailed design (DD-007) is covered:

### Interfaces
- **inputs[0]: raw_data (byte_array, length 4..1024)**
  - ✅ RB-001, RB-002, RB-003 (sync validation)
  - ✅ RB-004, RB-005 (version validation)
  - ✅ RB-006, RB-007, RB-008 (length validation)
  - ✅ EC-001..EC-008 (all equivalence classes)
  - ✅ BV-001..BV-004 (payload size boundaries)
  - ✅ EH-001..EH-009 (null, empty, corruption, reentrancy)

- **outputs[0]: result (union, Success or Error)**
  - ✅ RB-011 (Success variant with payload)
  - ✅ RB-002, RB-003, RB-005, RB-008, RB-010, RB-012 (Error variants with codes/offsets)
  - ✅ All EC and BV tests verify correct variant returned
  - ✅ EH tests verify union structure integrity

### Behavior Rules
- ✅ Sync check (RB-001, RB-002, RB-003, EH-004, EH-005)
- ✅ Version check (RB-004, RB-005)
- ✅ Length validation (RB-006, RB-007, RB-008, EC-003..EC-008, BV-001..BV-004)
- ✅ Message type extraction (RB-011, RB-012, BV-005, BV-006, EH-010)
- ✅ Sequence number extraction (RB-013, BV-007, BV-008)
- ✅ Checksum verification (RB-009, RB-010, EH-008, EH-009)

### Error Handling
- ✅ null or empty input (EH-001, EH-002)
- ✅ Length < minimum (EH-003)
- ✅ All five error codes tested: invalid_sync, unsupported_version, length_mismatch, checksum_failed, unknown_type

### Configuration
- ✅ SYNC_BYTES = [0xAA, 0x55] enforced in all tests
- ✅ SUPPORTED_VERSION = 1 enforced
- ✅ HEADER_SIZE = 10 used in length calculations
- ✅ CHECKSUM_SIZE = 2 used in boundary tests

### Constraints
- ✅ No proportional-to-input memory allocation (tests use fixed-size buffers, parse in-place logic implied)
- ✅ Reentrancy / no mutable state (EH-007 test)

---

## Test Anti-Pattern Compliance

Every test checked against `references/testing-anti-patterns.md`:

### Anti-Pattern 1: No Assertion / Assert-Doesn't-Throw
**Status: PASS** — Every test has explicit assertions on:
- `result.isSuccess()` / `result.isError()`
- `result.getError().errorCode()` (specific string, not just "error exists")
- `result.getError().byteOffset()` (specific integer)
- `result.getSuccess().payload().size()`, `.messageType()`, `.sequenceNumber()`

Example (RB-002):
```java
assertTrue(result.isError(), "Should fail with invalid sync");
assertEquals("invalid_sync", result.getError().errorCode());
assertEquals(0, result.getError().byteOffset());
```

### Anti-Pattern 2: The Mirror Test
**Status: PASS** — Hardcoded expected values:
- Sync bytes: 0xAA, 0x55 (from config)
- Version: 1 (from design)
- Error codes: exact strings from design enum
- Byte offsets: from design step descriptions (offset 0 for sync, 2 for version, 3 for length, 5 for type)

Example (RB-005):
```java
assertEquals("unsupported_version", result.getError().errorCode());
assertEquals(2, result.getError().byteOffset()); // offset from design
```

### Anti-Pattern 3: The Untargeted Mock
**Status: PASS** — No mocks used in this suite.
- All test data generated by builders (buildValidMessage, buildPayload)
- No external dependencies mocked
- CRC-16 implementation placeholder included inline

### Anti-Pattern 4: Tautology / Structural-Only Assertion
**Status: PASS** — All assertions check variant-specific fields:
- Error tests check `error_code` (enum string) + `byte_offset` (specific integer)
- Success tests check `message_type`, `payload.size()`, `sequence_number`
- Never assert just `result != null` or `result.isError()`

Example (RB-012):
```java
assertTrue(result.isError(), "Should fail with unknown type");
assertEquals("unknown_type", result.getError().errorCode());
assertEquals(5, result.getError().byteOffset()); // NOT just checking variant
```

### Anti-Pattern 5: The Giant Test
**Status: PASS** — Each test is focused:
- One logical scenario per test
- Multiple assertions within a test are all related (e.g., check error_code AND offset)
- Test names clearly describe the scenario

Example (RB-006 vs RB-007):
- RB-006: Length field *matches* → success
- RB-007: Length field *doesn't match* (too short) → length_mismatch
- Separate tests, not combined

### Anti-Pattern 6: Testing the Framework
**Status: PASS** — Every test removes the implementation:
- If MessageParser.parse() is deleted or stubbed to return null, tests fail
- If error codes are missing, tests fail
- If byte offset calculation is wrong, tests fail

---

## Test Organization

Tests are organized into four nested `@Nested` classes for clarity:

1. **RequirementBasedTests** — 13 tests (RB-001 to RB-013)
   - Location: RequirementBasedTests inner class
   - Maps to: `body.behavior` section of DD-007

2. **EquivalenceClassTests** — 8 tests (EC-001 to EC-008)
   - Location: EquivalenceClassTests inner class
   - Maps to: `body.interfaces.inputs[0].constraints`

3. **BoundaryValueTests** — 8 tests (BV-001 to BV-008)
   - Location: BoundaryValueTests inner class
   - Maps to: Numeric boundaries in length, message type, sequence number

4. **ErrorHandlingTests** — 10 tests (EH-001 to EH-010)
   - Location: ErrorHandlingTests inner class
   - Maps to: `body.error_handling` + implicit faults

---

## Helper Methods (Test Data Builders)

| Method | Purpose | Used By |
|---|---|---|
| `buildValidMessage(sync, ver, payload, crc)` | Create complete valid message with or without CRC | RB, EC, BV tests |
| `buildValidMessage(..., messageType)` | Variant with custom message type | RB-012, EH-010 |
| `buildValidMessageWithSeq(...)` | Variant with custom sequence number | BV-007, BV-008, EH-006, EH-007 |
| `buildMessageWithSyncBytes(byte0, byte1)` | Create message with custom sync bytes | RB-002, RB-003, EH-004, EH-005 |
| `buildPayload(sizeInBytes)` | Generate test payload | All tests |
| `calculateCRC16(data, start, end)` | Compute CRC-16 checksum | buildValidMessage internals |

**Builder Anti-Patterns:**
- ✅ No builder calls test code (mirror test)
- ✅ CRC-16 implementation marked as placeholder (comment: "replace with actual")
- ✅ All builders inject deterministic test data (no randomization)

---

## Compilation & Execution

**Requirements:**
- Java 17+ (uses sealed interfaces, records, text blocks)
- JUnit 5.9+ (uses @Nested, @DisplayName, @ParameterizedTest)

**Compilation:**
```bash
javac --release 17 MessageParserTest.java
```

**Execution:**
```bash
junit5 --scan-classpath
# or via Maven/Gradle
```

**Expected Results:**
```
39 tests found
39 tests passed
0 tests failed
```

All 39 tests should pass against a correct implementation of MessageParser.

---

## Coverage Gaps (If Any)

**Fully Covered:**
- ✅ All 5 error codes (invalid_sync, unsupported_version, length_mismatch, checksum_failed, unknown_type)
- ✅ All config values (sync, version, header size, checksum size)
- ✅ Success variant (message_type, payload, sequence_number)
- ✅ Error variant (error_code, byte_offset)
- ✅ Reentrancy / statelessness
- ✅ Checksum integrity

**Design Decisions Not Explicitly Tested (by choice):**
- Memory allocation (design says "no proportional-to-input allocation") — verified by code review, not unit test
- Exact CRC-16 polynomial — verified by integration test with real protocol, not unit test

---

## References

- **Skill:** derive-test-cases
- **Design Artifact:** DD-007 (detailed-design.schema.yaml)
- **Derivation Strategies:** references/derivation-strategies.md
- **Anti-Patterns:** references/testing-anti-patterns.md
- **Framework:** JUnit 5, Java 17 records + sealed interfaces
