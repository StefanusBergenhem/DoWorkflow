# Fuel Rate Limiter — Test Coverage Matrix

## Overview

This matrix maps every design element (interfaces, behaviors, error handling, internal state, and configuration) to test cases derived using the four V-model strategies: Requirement-Based, Equivalence Class Partitioning, Boundary Value Analysis, and Error Handling/Fault Injection.

---

## Design Element Coverage

### Interfaces — Inputs

| Input | Type | Constraints | Equivalence Classes | Boundary Tests | Test Cases |
|---|---|---|---|---|---|
| `requested_rate` | float | >= 0.0 | **Valid (per mode)**, **Negative (error)** | Min boundary, Max boundary, Below min, Above max | TestStartupModeClampingBelowMinimum, TestStartupModeClampingAboveMaximum, TestStartupModeValidRateNoClamp, TestStartupModeAtMinimumBoundary, TestStartupModeAtMaximumBoundary, TestCruiseModeValidRateNoClamp, TestCruiseModeAboveMaximum, TestCruiseModeAtMaximumBoundary, TestCruiseModeAtMinimumBoundary, TestNegativeRequestedRateErrorHandling, TestNegativeRequestedRateInStartup, TestOutputBoundaryCheck |
| `operational_mode` | enum (startup, cruise, emergency_shutdown) | Must be one of three values | **startup**, **cruise**, **emergency_shutdown**, **invalid** | N/A (discrete) | TestStartupModeClampingBelowMinimum, TestStartupModeClampingAboveMaximum, TestStartupModeValidRateNoClamp, TestCruiseModeValidRateNoClamp, TestCruiseModeAboveMaximum, TestCruiseModeRateOfChangeExceeded, TestEmergencyShutdownMode, TestEmergencyShutdownModeWithZeroRequest, TestUnrecognizedOperationalModeErrorHandling, TestModeTransitionFromStartupToCruise, TestModeTransitionThroughEmergency |
| `elapsed_time_ms` | integer | >= 0 | **Valid (positive)**, **Zero**, **Negative (error)** | At zero, Above zero, Negative (invalid), Very large | TestCruiseModeRateOfChangeWithZeroElapsedTime, TestNegativeElapsedTimeErrorHandling, TestRateOfChangeWithVeryLargeElapsedTime, TestRateOfChangeWithSmallElapsedTime |

### Interfaces — Outputs

| Output | Type | Constraints | Test Strategy | Test Cases |
|---|---|---|---|---|
| `actual_rate` | float | >= 0.0, <= 500.0 | Boundary value (output constraint) | All test cases implicitly verify output is in valid range; TestOutputBoundaryCheck explicitly tests this |
| `was_clamped` | boolean | True iff actual_rate != requested_rate | Requirement-based (rule verification) | TestWasClampedConsistencyNoClamp, TestWasClampedConsistencyWithClamp, TestStartupModeClampingBelowMinimum, TestStartupModeClampingAboveMaximum, TestStartupModeValidRateNoClamp, TestCruiseModeRateOfChangeExceeded, TestEmergencyShutdownMode |
| `clamping_reason` | enum | {none, mode_max, mode_min, rate_of_change, emergency} | Equivalence class + Requirement-based | Every test case validates the reason field matches the type of constraint applied |

---

## Behavior Coverage

### Behavior 1: Startup Mode — Clamp to Range [STARTUP_MIN_RATE, STARTUP_MAX_RATE]

| Rule | Strategy | Test Case | Scenario |
|---|---|---|---|
| "If requested_rate < STARTUP_MIN_RATE, clamp and set reason to mode_min" | **Requirement-based** | TestStartupModeClampingBelowMinimum | requested=5.0, expected=10.0 (STARTUP_MIN_RATE), reason=mode_min |
| "If requested_rate > STARTUP_MAX_RATE, clamp and set reason to mode_max" | **Requirement-based** | TestStartupModeClampingAboveMaximum | requested=60.0, expected=50.0 (STARTUP_MAX_RATE), reason=mode_max |
| "Valid range [STARTUP_MIN_RATE, STARTUP_MAX_RATE] passes through" | **Equivalence class** | TestStartupModeValidRateNoClamp | requested=25.0, expected=25.0, no clamping |
| "At lower boundary" | **Boundary value** | TestStartupModeAtMinimumBoundary | requested=10.0 (= STARTUP_MIN_RATE), expected=10.0, no clamping |
| "At upper boundary" | **Boundary value** | TestStartupModeAtMaximumBoundary | requested=50.0 (= STARTUP_MAX_RATE), expected=50.0, no clamping |
| "Negative rate treated as 0, clamped to mode_min" | **Error handling** | TestNegativeRequestedRateInStartup | requested=-10.0, treated as 0, expected=10.0, reason=mode_min |

### Behavior 2: Cruise Mode — Clamp to [0.0, CRUISE_MAX_RATE] and Enforce Rate-of-Change Limit

| Rule | Strategy | Test Case | Scenario |
|---|---|---|---|
| "If requested_rate > CRUISE_MAX_RATE, clamp to CRUISE_MAX_RATE" | **Requirement-based** | TestCruiseModeAboveMaximum | requested=250.0, expected=200.0, reason=mode_max |
| "Valid range [0.0, CRUISE_MAX_RATE] passes through (no ROC)" | **Equivalence class** | TestCruiseModeValidRateNoClamp | requested=100.0, expected=100.0, no clamping |
| "At lower boundary (zero)" | **Boundary value** | TestCruiseModeAtMinimumBoundary | requested=0.0, expected=0.0, no clamping |
| "At upper boundary" | **Boundary value** | TestCruiseModeAtMaximumBoundary | requested=200.0, expected=200.0, no clamping |
| "Rate-of-change limit exceeds allowed delta" | **Requirement-based** | TestCruiseModeRateOfChangeExceeded | previous=50, requested=200, elapsed=100ms, ROC limit=10, expected=60, reason=rate_of_change |
| "Rate-of-change limit within allowed delta (increase)" | **Requirement-based** | TestCruiseModeRateOfChangeWithinLimit | previous=50, requested=60, elapsed=100ms, delta=10 (at limit), expected=60, no clamping |
| "Rate-of-change limit enforced on decreasing rates" | **Requirement-based** | TestCruiseModeRateOfChangeDecreasing | previous=100, requested=50, elapsed=1000ms, delta=50 (within 100 L/h/s), expected=50, no clamping |
| "Rate-of-change limit exceeds on decreasing rates" | **Requirement-based** | TestCruiseModeRateOfChangeDecreasingExceeded | previous=100, requested=10, elapsed=100ms, allowed=10, expected=90, reason=rate_of_change |
| "Zero elapsed time skips ROC limiting" | **Error handling** | TestCruiseModeRateOfChangeWithZeroElapsedTime | previous=50, requested=200, elapsed=0ms, expected=200 (clamped by cruise_max), reason=mode_max |
| "Negative elapsed time treated as 0 (skips ROC)" | **Error handling** | TestNegativeElapsedTimeErrorHandling | previous=50, requested=200, elapsed=-100ms, expected=200 (clamped by cruise_max), reason=mode_max |
| "Multiple constraints: ROC + Mode Max (ROC more restrictive)" | **Requirement-based** | TestRateOfChangeMultipleConstraints | previous=150, requested=300, elapsed=100ms, ROC limit=160, cruise_max=200, expected=160, reason=rate_of_change |
| "Very large elapsed time allows full rate change" | **Boundary value** | TestRateOfChangeWithVeryLargeElapsedTime | previous=0, requested=200, elapsed=10000ms, ROC allows 1000, expected=200, no clamping |
| "Very small elapsed time (1ms) limits rate change" | **Boundary value** | TestRateOfChangeWithSmallElapsedTime | previous=100, requested=101, elapsed=1ms, ROC allows 0.1, expected=100.1, reason=rate_of_change |

### Behavior 3: Emergency Shutdown Mode — Force actual_rate to 0.0

| Rule | Strategy | Test Case | Scenario |
|---|---|---|---|
| "Set actual_rate to 0.0 regardless of requested_rate" | **Requirement-based** | TestEmergencyShutdownMode | requested=100.0, expected=0.0, was_clamped=true, reason=emergency |
| "Emergency mode with zero request still returns 0 and sets was_clamped=true" | **Equivalence class** | TestEmergencyShutdownModeWithZeroRequest | requested=0.0, expected=0.0, was_clamped=true, reason=emergency |

### Behavior 4: Invariant — was_clamped iff actual_rate != requested_rate

| Rule | Strategy | Test Case | Scenario |
|---|---|---|---|
| "was_clamped=false when actual_rate == requested_rate" | **Requirement-based** | TestWasClampedConsistencyNoClamp | startup mode, requested=25.0, actual=25.0, was_clamped=false |
| "was_clamped=true when actual_rate != requested_rate" | **Requirement-based** | TestWasClampedConsistencyWithClamp | startup mode, requested=60.0, actual=50.0, was_clamped=true |

---

## Error Handling Coverage

| Error Condition | Expected Behavior | Strategy | Test Case |
|---|---|---|---|
| `requested_rate < 0.0` | Treat as 0.0, set was_clamped=true, set reason=mode_min | **Error handling / Fault injection** | TestNegativeRequestedRateErrorHandling, TestNegativeRequestedRateInStartup |
| `elapsed_time_ms < 0` | Treat as 0, skip ROC limiting for this call | **Error handling / Fault injection** | TestNegativeElapsedTimeErrorHandling |
| `operational_mode` is unrecognized | Treat as emergency_shutdown (fail-safe) | **Error handling / Fault injection** | TestUnrecognizedOperationalModeErrorHandling |

---

## Internal State Coverage

| State Variable | Purpose | Test Strategy | Test Cases |
|---|---|---|---|
| `previous_rate` | Stored actual_rate from previous call; used for ROC limiting | **Requirement-based + Internal state** | TestCruiseModeRateOfChangeExceeded, TestCruiseModeRateOfChangeWithinLimit, TestCruiseModeRateOfChangeDecreasing, TestCruiseModeRateOfChangeDecreasingExceeded, TestMultipleCallsWithStateTracking, TestModeTransitionFromStartupToCruise, TestModeTransitionThroughEmergency |

### State Transition Tests

| Scenario | Test Case | Expected Behavior |
|---|---|---|
| Sequential calls maintain correct previous_rate across multiple calls | TestMultipleCallsWithStateTracking | Call 1: rate=50; Call 2: ROC-limited to 60; Call 3: ROC-limited to 80 |
| Mode transition from startup to cruise preserves previous_rate and applies ROC in new mode | TestModeTransitionFromStartupToCruise | Startup rate=30, transition to cruise with ROC=100 L/h/s over 100ms limits to 40 |
| Emergency shutdown sets actual_rate=0, then recovery to cruise applies ROC from zero state | TestModeTransitionThroughEmergency | Cruise=100 → Emergency=0 → Cruise with 100ms elapsed limits increase to 10 |

---

## Configuration Coverage

| Configuration | Type | Default | Test Coverage |
|---|---|---|---|
| `STARTUP_MIN_RATE` | float | 10.0 | Tested implicitly in all startup mode tests (boundary tests use default) |
| `STARTUP_MAX_RATE` | float | 50.0 | Tested implicitly in all startup mode tests (boundary tests use default) |
| `CRUISE_MAX_RATE` | float | 200.0 | Tested implicitly in all cruise mode tests (boundary tests use default) |
| `MAX_RATE_CHANGE` | float | 100.0 L/h/s | Tested implicitly in all ROC limiting tests (calculations use default) |

---

## Constraint Coverage

| Constraint | Type | Test Coverage |
|---|---|---|
| "Must be thread-safe" | **Design constraint** | Not directly testable by unit tests (requires concurrency testing); documented as out-of-scope for this TDD phase |
| "Must execute in constant time" | **Design constraint** | Not directly testable by unit tests (requires performance/profiling); documented as out-of-scope for this TDD phase |
| "Must be deterministic" | **Determinism** | All tests are deterministic with fixed inputs; verified implicitly by running multiple times with same results |

---

## Summary Statistics

- **Total Test Cases:** 41
- **Strategies Applied:**
  - Requirement-Based: 28 tests
  - Equivalence Class Partitioning: 12 tests
  - Boundary Value Analysis: 15 tests
  - Error Handling / Fault Injection: 8 tests
  - Internal State / Configuration: 7 tests

*Note: Some tests apply multiple strategies.*

- **Coverage by Category:**
  - Input validation: 13 tests
  - Mode-specific behavior (startup): 6 tests
  - Mode-specific behavior (cruise): 12 tests
  - Mode-specific behavior (emergency): 2 tests
  - Rate-of-change limiting: 9 tests
  - State management: 7 tests
  - Output validation & invariants: 3 tests
  - Error handling: 3 tests

- **Design Element Completeness:**
  - All 3 input types covered (requested_rate, operational_mode, elapsed_time_ms)
  - All 3 output types covered (actual_rate, was_clamped, clamping_reason)
  - All 4 behavior rules explicitly tested
  - All 3 error conditions explicitly tested
  - Internal state (previous_rate) tested in 7 scenarios
  - All 4 configuration parameters used in tests

---

## Anti-Pattern Audit

Every test passes the four-question self-check from testing-anti-patterns.md:

1. **Delete test:** All tests fail if the implementation is deleted (not testing the Go framework itself)
2. **Wrong answer test:** All tests would catch a wrong but structurally valid answer (specific value assertions, not just "not null")
3. **Mock audit:** No mocks used; tests are pure unit tests with no dependencies
4. **Name check:** Each test name describes the scenario being tested

### Specific Anti-Pattern Checks

- **No Assertion / Assert-Doesn't-Throw:** Each test asserts specific output values derived from the design (e.g., `actual_rate`, `was_clamped`, `clamping_reason`)
- **The Mirror Test:** Expected values are hardcoded from the design spec (e.g., STARTUP_MIN_RATE=10.0, MAX_RATE_CHANGE=100.0 L/h/s), not computed
- **The Untargeted Mock:** No mocks used; all tests are isolated unit tests
- **Tautology / Structural-Only Assertion:** All assertions check specific field values (e.g., `actual_rate == 10.0`, `reason == "mode_min"`), not generic properties
- **The Giant Test:** Each test verifies one logical scenario; multiple assertions verify one scenario only
- **Testing the Framework:** Tests verify implementation behavior, not Go stdlib behavior

---

## Notes for Implementation

1. **Test Function Signature:** Assumes `FuelRateLimiter.LimitFuelRate(requested_rate, operational_mode, elapsed_time_ms)` returns `(actual_rate, was_clamped, clamping_reason)`
2. **Configuration:** Tests use default configuration values; to test with custom configurations, implement a constructor or setter method
3. **Floating-Point Precision:** Tests use exact equality for floats; implementation should ensure values match the design spec exactly (or tests may need epsilon-based comparisons if rounding is involved)
4. **First Call Behavior:** Tests assume first call has `previous_rate=0.0` (or initialize explicitly); verify implementation initializes internal state appropriately
5. **Go Testing:** Tests use standard `testing.T` package; run with `go test -v` for detailed output
