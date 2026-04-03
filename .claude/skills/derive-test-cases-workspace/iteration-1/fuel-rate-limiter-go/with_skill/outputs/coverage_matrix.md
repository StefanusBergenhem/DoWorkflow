# Coverage Matrix — Fuel Rate Limiter (DD-001)

## Strategy 1: Requirement-Based (behavior section)

| Design Element | Test Case(s) | Verified |
|---|---|---|
| behavior[0]: startup mode clamp to [MIN, MAX] | `TestStartupMode_RequestBelowMin_ClampsToMin`, `TestStartupMode_RequestAboveMax_ClampsToMax`, `TestStartupMode_RequestWithinRange_NoClamping` | Yes |
| behavior[0]: startup clamping_reason = mode_min | `TestStartupMode_RequestBelowMin_ClampsToMin` | Yes |
| behavior[0]: startup clamping_reason = mode_max | `TestStartupMode_RequestAboveMax_ClampsToMax` | Yes |
| behavior[1]: cruise mode clamp to [0, CRUISE_MAX] | `TestCruiseMode_RequestAboveMax_ClampsToMax`, `TestCruiseMode_RequestWithinMax_NoClamping` | Yes |
| behavior[1]: cruise rate-of-change limit (increase) | `TestCruiseMode_RateOfChangeLimitsIncrease` | Yes |
| behavior[1]: cruise rate-of-change limit (decrease) | `TestCruiseMode_RateOfChangeLimitsDecrease` | Yes |
| behavior[1]: cruise rate-of-change exactly at limit | `TestCruiseMode_RateOfChangeExactlyAtLimit_NoClamping` | Yes |
| behavior[1]: cruise clamping_reason = rate_of_change | `TestCruiseMode_RateOfChangeLimitsIncrease`, `TestCruiseMode_RateOfChangeLimitsDecrease` | Yes |
| behavior[2]: emergency_shutdown forces 0.0 | `TestEmergencyShutdown_ForcesZeroRate` | Yes |
| behavior[2]: emergency always sets was_clamped = true | `TestEmergencyShutdown_AlreadyZero_StillClamped` | Yes |
| behavior[2]: emergency clamping_reason = emergency | `TestEmergencyShutdown_ForcesZeroRate`, `TestEmergencyShutdown_AlreadyZero_StillClamped` | Yes |
| behavior[3]: was_clamped iff actual != requested | `TestWasClampedFalse_WhenRateUnchanged`, `TestWasClampedTrue_WhenRateChanged` | Yes |

## Strategy 2: Equivalence Class Partitioning (interfaces)

| Input / Class | Test Case(s) | Verified |
|---|---|---|
| requested_rate: zero in cruise | `TestRequestedRate_Zero_CruiseMode` | Yes |
| requested_rate: zero in startup | `TestRequestedRate_Zero_StartupMode_ClampsToMin` | Yes |
| requested_rate: valid mid-range | `TestStartupMode_RequestWithinRange_NoClamping`, `TestCruiseMode_RequestWithinMax_NoClamping` | Yes |
| requested_rate: above mode max | `TestStartupMode_RequestAboveMax_ClampsToMax`, `TestCruiseMode_RequestAboveMax_ClampsToMax` | Yes |
| requested_rate: negative (invalid) | `TestErrorHandling_NegativeRequestedRate_TreatedAsZero`, `TestErrorHandling_NegativeRequestedRate_CruiseMode` | Yes |
| operational_mode: startup | `TestStartupMode_*` (3 tests) | Yes |
| operational_mode: cruise | `TestCruiseMode_*` (6 tests) | Yes |
| operational_mode: emergency_shutdown | `TestEmergencyShutdown_*` (2 tests) | Yes |
| operational_mode: unrecognized (invalid) | `TestErrorHandling_UnrecognizedMode_TreatedAsEmergencyShutdown`, `TestErrorHandling_EmptyStringMode_TreatedAsEmergencyShutdown` | Yes |
| elapsed_time_ms: zero | `TestElapsedTimeZero_CruiseMode_NoRateOfChangeApplied`, `TestElapsedTimeZero_CruiseMode_RateChangeBlockedEntirely` | Yes |
| elapsed_time_ms: positive | All cruise mode tests with elapsed > 0 | Yes |
| elapsed_time_ms: negative (invalid) | `TestErrorHandling_NegativeElapsedTime_SkipsRateOfChange` | Yes |

## Strategy 3: Boundary Value Analysis (interfaces + configuration)

| Boundary | Test Case(s) | Verified |
|---|---|---|
| requested_rate at STARTUP_MIN_RATE (10.0) | `TestRequestedRate_AtStartupMinBoundary` | Yes |
| requested_rate just below STARTUP_MIN_RATE (9.999) | `TestRequestedRate_JustBelowStartupMin` | Yes |
| requested_rate at STARTUP_MAX_RATE (50.0) | `TestRequestedRate_AtStartupMaxBoundary` | Yes |
| requested_rate just above STARTUP_MAX_RATE (50.001) | `TestRequestedRate_JustAboveStartupMax` | Yes |
| requested_rate at CRUISE_MAX_RATE (200.0) | `TestRequestedRate_AtCruiseMaxBoundary` | Yes |
| requested_rate just above CRUISE_MAX_RATE (200.001) | `TestRequestedRate_JustAboveCruiseMax` | Yes |
| rate-of-change exactly at limit | `TestCruiseMode_RateOfChangeExactlyAtLimit_NoClamping` | Yes |
| rate-of-change just above limit | `TestCruiseMode_RateOfChangeJustAboveLimit` | Yes |
| output actual_rate at 500.0 cap | `TestOutputRate_NeverExceeds500` | Yes |
| requested_rate = NaN | `TestRequestedRate_NaN_TreatedAsInvalid` | Yes |
| requested_rate = +Inf | `TestRequestedRate_PositiveInf_ClampsToMax` | Yes |

## Strategy 4: Error Handling / Fault Injection

| Error Condition | Test Case(s) | Verified |
|---|---|---|
| error_handling[0]: negative requested_rate | `TestErrorHandling_NegativeRequestedRate_TreatedAsZero`, `TestErrorHandling_NegativeRequestedRate_CruiseMode` | Yes |
| error_handling[1]: negative elapsed_time_ms | `TestErrorHandling_NegativeElapsedTime_SkipsRateOfChange` | Yes |
| error_handling[2]: unrecognized mode | `TestErrorHandling_UnrecognizedMode_TreatedAsEmergencyShutdown`, `TestErrorHandling_EmptyStringMode_TreatedAsEmergencyShutdown` | Yes |

## Stateful Behavior (internal_state: previous_rate)

| State Scenario | Test Case(s) | Verified |
|---|---|---|
| previous_rate accumulates across calls | `TestStateful_PreviousRateUpdatedAfterEachCall` | Yes |
| state carries across mode transitions | `TestStateful_ModeTransitionStartupToCruise` | Yes |
| emergency resets previous_rate to 0 | `TestStateful_EmergencyResetsToZero` | Yes |
| rapid deceleration (symmetric ROC) | `TestStateful_RapidDecelerationInCruise` | Yes |

## Configuration Variants

| Config Variant | Test Case(s) | Verified |
|---|---|---|
| Custom startup range [20, 80] | `TestConfig_CustomStartupRange` | Yes |
| Custom cruise max (100) | `TestConfig_CustomCruiseMax` | Yes |
| Custom max rate change (20 L/hr/s) | `TestConfig_CustomMaxRateChange` | Yes |

## Summary

| Strategy | Test Count |
|---|---|
| Requirement-based | 12 |
| Equivalence class | 4 |
| Boundary value | 10 |
| Error handling | 5 |
| Stateful behavior | 4 |
| Configuration | 3 |
| **Total** | **38** |

## Handoff Checklist

- [x] Every test would fail if the implementation were deleted (all call `Limit()` and assert specific outputs)
- [x] No test duplicates implementation logic to compute expected values (all expected values are hand-calculated from design constants)
- [x] No test uses "assert does not throw" as its only assertion
- [x] No mocks used (pure unit, no external dependencies)
- [x] Test names describe scenarios, not method names
- [x] Coverage matrix accounts for all design elements (3 behavior modes + 1 invariant + 3 error conditions + 3 inputs + 1 state variable + 4 config params)
- [x] Tests are syntactically valid Go using the standard `testing` package
