# Coverage Matrix: MessageParser (DD-007)

Derived following the 5-step process from the `derive-test-cases` skill.
Four derivation strategies applied against the detailed design artifact.

---

## Strategy 1: Requirement-Based Testing (from `behavior` section)

| Design Element | Test Case(s) | Strategy |
|---|---|---|
| behavior[0]: Check sync bytes (0xAA, 0x55) | `rejectsWrongFirstSyncByte`, `rejectsWrongSecondSyncByte`, `rejectsBothSyncBytesWrong` | req-based |
| behavior[1]: Version byte must equal SUPPORTED_VERSION | `rejectsUnsupportedVersion`, `rejectsVersionZero` | req-based |
| behavior[2]: Payload length matches actual data | `rejectsDeclaredLengthExceedsActualData`, `rejectsDeclaredLengthLessThanActualData` | req-based |
| behavior[3]: Read message_type at offset 5 | `parsesCorrectMessageType` | req-based |
| behavior[4]: Read sequence_number at offset 6-9 (big-endian uint32) | `parsesCorrectSequenceNumber` | req-based |
| behavior[5]: Extract payload bytes | `parsesPayloadIntoTypedFieldMap`, `parsesZeroLengthPayloadIntoEmptyMap` | req-based |
| behavior[6]: CRC-16 checksum verification | `rejectsCorruptedChecksum`, `rejectsZeroedChecksum` | req-based |
| behavior[7]: Type registry lookup | `rejectsUnknownMessageType` | req-based |
| behavior[8]: Parse payload into typed fields | `parsesPayloadIntoTypedFieldMap` | req-based |
| behavior[9]: Return Success with all fields | `returnsAllSuccessFields` | req-based |

## Strategy 2: Equivalence Class Partitioning (from `interfaces`)

| Input / Class | Test Case(s) | Strategy |
|---|---|---|
| raw_data: valid, typical length (20-byte payload) | `validNominalMessage` | equiv-class |
| raw_data: null | `invalidNullInput` | equiv-class |
| raw_data: empty (length 0) | `invalidEmptyInput` | equiv-class |
| raw_data: all-zero bytes of valid length | `invalidAllZeroBytes` | equiv-class |
| raw_data: valid with zero-length payload | `parsesZeroLengthPayloadIntoEmptyMap` | equiv-class |
| raw_data: valid with maximum payload (1012 bytes) | `atMaximumConstraint1024Bytes` | equiv-class |
| message_type: registered type (1) | `parsesCorrectMessageType`, `validNominalMessage` | equiv-class |
| message_type: unregistered type (0xFF) | `rejectsUnknownMessageType` | equiv-class |
| version: supported (1) | all success-path tests | equiv-class |
| version: unsupported (99) | `rejectsUnsupportedVersion` | equiv-class |
| version: zero | `rejectsVersionZero` | equiv-class |

## Strategy 3: Boundary Value Analysis (from `interfaces` constraints)

| Boundary | Test Case(s) | Strategy |
|---|---|---|
| raw_data length: 3 bytes (below min constraint 4) | `belowMinimumConstraint3Bytes` | boundary |
| raw_data length: 4 bytes (at min constraint, < min valid) | `atMinimumConstraint4Bytes` | boundary |
| raw_data length: 11 bytes (just below min valid 12) | `justBelowMinimumValidMessage11Bytes` | boundary |
| raw_data length: 12 bytes (min valid: HEADER + CHECKSUM) | `atMinimumValidMessageSize12Bytes` | boundary |
| raw_data length: 1024 bytes (at max constraint) | `atMaximumConstraint1024Bytes` | boundary |
| raw_data length: 1025 bytes (above max constraint) | `aboveMaximumConstraint1025Bytes` | boundary |
| sequence_number: 0 (min uint32) | `sequenceNumberZero` | boundary |
| sequence_number: 1 (just above min) | `sequenceNumberOne` | boundary |
| sequence_number: 0xFFFFFFFF (max uint32) | `sequenceNumberMaxUint32` | boundary |
| message_type: 0 (min byte, unregistered) | `messageTypeZeroUnregistered` | boundary |
| message_type: 255 (max byte, unregistered) | `messageType255Unregistered` | boundary |

## Strategy 4: Error Handling and Fault Injection

| Error Condition | Test Case(s) | Strategy |
|---|---|---|
| error_handling[0]: null input | `nullInputReturnsInvalidSync` | error |
| error_handling[0]: empty input | `emptyInputReturnsInvalidSync` | error |
| error_handling[1]: length < HEADER_SIZE + CHECKSUM_SIZE | `tooShortInputReturnsLengthMismatch` | error |
| Fault: single bit flip in payload | `singleBitFlipInPayloadCorruptsChecksum` | fault-inject |
| Fault: single bit flip in header | `singleBitFlipInHeaderCorruptsChecksum` | fault-inject |
| Fault: truncated message (missing checksum) | `truncatedMessageMissingChecksumBytes` | fault-inject |
| Fault: all-0xFF payload bytes | `payloadAllOxFFParsesSuccessfully` | fault-inject |
| Precedence: sync before version | `syncCheckedBeforeVersion` | error-precedence |
| Precedence: version before length | `versionCheckedBeforeLength` | error-precedence |
| Precedence: length before checksum | `lengthCheckedBeforeChecksum` | error-precedence |

---

## Handoff Checklist (Step 5)

- [x] Every test would fail if the implementation were deleted
- [x] No test duplicates implementation logic to compute expected values — all assertion values are hardcoded from the design spec (CRC helper is used ONLY for constructing valid test data, never in assertions)
- [x] No test uses "assert does not throw" as its only assertion — every test has explicit assertEquals/assertTrue on specific output fields
- [x] No mocks used — MessageParser has no external dependencies per the design (stateless, reentrant, no I/O)
- [x] Test names describe scenarios, not method names (e.g., `rejectsWrongFirstSyncByte`, not `testParse`)
- [x] Coverage matrix accounts for all 10 behavior steps, both error_handling entries, and all interface boundaries
- [x] Tests are syntactically valid JUnit 5 Java

## Anti-Pattern Check (per `references/testing-anti-patterns.md`)

| Anti-Pattern | Status | Notes |
|---|---|---|
| #1 "Doesn't Throw" | Clear | Every test asserts specific output values |
| #2 Mirror Test | Clear | CRC helper builds test data only; expected values are hardcoded from design |
| #3 Untargeted Mock | N/A | No mocks needed — unit has no external dependencies |
| #4 Tautology Test | Clear | No `assertNotNull`-only tests; all tests check specific field values |
| #5 Happy-Path-Only | Clear | 21 error/boundary tests vs 15 success-path tests |
| #6 Giant Test | Clear | One logical concept per test; largest test has 4 assertions for one scenario |
| #7 Tests the Framework | Clear | All assertions target MessageParser behavior, not Java/JUnit |
| #8 Assertion-Free | Clear | Every test has at least one targeted assertion |

---

## Summary

| Category | Count |
|---|---|
| **Total test methods** | **37** |
| Requirement-based (Strategy 1) | 15 |
| Equivalence class (Strategy 2) | 4 |
| Boundary value (Strategy 3) | 11 |
| Error handling / fault injection (Strategy 4) | 7 |
| Error precedence (cross-cutting) | 3 |
