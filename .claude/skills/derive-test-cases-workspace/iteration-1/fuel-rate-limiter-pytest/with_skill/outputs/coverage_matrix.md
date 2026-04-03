# Coverage Matrix -- FuelRateLimiter (DD-001)

## Strategy 1: Requirement-Based (from `behavior` section)

| Design Element | Test Case(s) | Strategy |
|---|---|---|
| behavior[0]: startup clamps below min | `test_startup_clamps_rate_below_minimum_to_minimum` | req-based |
| behavior[0]: startup clamps above max | `test_startup_clamps_rate_above_maximum_to_maximum` | req-based |
| behavior[0]: startup passes within bounds | `test_startup_passes_rate_within_bounds_unchanged` | req-based |
| behavior[1]: cruise clamps above max | `test_cruise_clamps_rate_above_max` | req-based |
| behavior[1]: cruise passes within bounds | `test_cruise_passes_rate_within_bounds` | req-based |
| behavior[1]: cruise allows zero | `test_cruise_allows_zero_rate` | req-based |
| behavior[1]: rate-of-change limits increase | `test_cruise_rate_of_change_limits_large_increase` | req-based |
| behavior[1]: rate-of-change limits decrease | `test_cruise_rate_of_change_limits_large_decrease` | req-based |
| behavior[1]: rate-of-change within limit | `test_cruise_rate_of_change_within_limit_not_clamped` | req-based |
| behavior[1]: rate-of-change short elapsed | `test_cruise_rate_of_change_with_short_elapsed_time` | req-based |
| behavior[1]: mode max + rate-of-change combo | `test_cruise_mode_max_and_rate_of_change_both_apply` | req-based |
| behavior[2]: emergency forces zero | `test_emergency_shutdown_forces_zero_rate` | req-based |
| behavior[2]: emergency with zero request | `test_emergency_shutdown_with_zero_request_still_reports_clamped` | req-based |
| behavior[2]: emergency with high rate | `test_emergency_shutdown_ignores_very_high_rate` | req-based |
| behavior[3]: was_clamped true when differs | `test_was_clamped_true_when_actual_differs_from_requested` | req-based |
| behavior[3]: was_clamped false when equal | `test_was_clamped_false_when_actual_equals_requested` | req-based |

## Strategy 2: Equivalence Class Partitioning (from `interfaces`)

| Design Element | Test Case(s) | Strategy |
|---|---|---|
| input requested_rate: zero | `test_zero_rate_in_cruise` | equiv-class |
| input requested_rate: nominal valid | `test_nominal_valid_rate_in_cruise` | equiv-class |
| input requested_rate: above mode max | `test_high_rate_exceeding_mode_max` | equiv-class |
| input operational_mode: startup | `test_startup_mode_nominal` | equiv-class |
| input operational_mode: cruise | `test_cruise_mode_nominal` | equiv-class |
| input operational_mode: emergency_shutdown | `test_emergency_shutdown_mode_nominal` | equiv-class |
| input elapsed_time_ms: zero | `test_zero_elapsed_prevents_any_rate_change` | equiv-class |
| input elapsed_time_ms: typical | `test_typical_elapsed_time` | equiv-class |
| input elapsed_time_ms: large | `test_large_elapsed_allows_full_change` | equiv-class |

## Strategy 3: Boundary Value Analysis (from constraints)

| Design Element | Test Case(s) | Strategy |
|---|---|---|
| startup rate: just below min (9.99) | `test_just_below_startup_min` | boundary |
| startup rate: at min (10.0) | `test_at_startup_min` | boundary |
| startup rate: nominal (30.0) | `test_nominal_within_startup_range` | boundary |
| startup rate: at max (50.0) | `test_at_startup_max` | boundary |
| startup rate: just above max (50.01) | `test_just_above_startup_max` | boundary |
| cruise rate: at min (0.0) | `test_at_cruise_min_zero` | boundary |
| cruise rate: just below max (199.99) | `test_just_below_cruise_max` | boundary |
| cruise rate: at max (200.0) | `test_at_cruise_max` | boundary |
| cruise rate: just above max (200.01) | `test_just_above_cruise_max` | boundary |
| rate-of-change: exactly at limit | `test_rate_change_exactly_at_limit_not_clamped` | boundary |
| rate-of-change: just above limit | `test_rate_change_just_above_limit_is_clamped` | boundary |
| rate-of-change: zero elapsed | `test_rate_change_zero_elapsed_prevents_any_change` | boundary |
| rate-of-change: 1ms elapsed (tiny delta) | `test_rate_change_at_one_ms_allows_tiny_delta` | boundary |
| output: never below zero | `test_output_never_below_zero` | boundary |
| output: capped at 500.0 | `test_output_capped_at_500_even_with_high_cruise_max` | boundary |

## Strategy 4: Error Handling / Fault Injection (from `error_handling`)

| Design Element | Test Case(s) | Strategy |
|---|---|---|
| error[0]: negative rate in startup | `test_negative_requested_rate_treated_as_zero_in_startup` | error |
| error[0]: negative rate in cruise | `test_negative_requested_rate_treated_as_zero_in_cruise` | error |
| error[1]: negative elapsed_time_ms | `test_negative_elapsed_time_skips_rate_of_change_limiting` | error |
| error[2]: unrecognized mode string | `test_unrecognized_mode_treated_as_emergency_shutdown` | error |
| error[2]: empty string mode | `test_empty_string_mode_treated_as_emergency_shutdown` | error |
| fault injection: None mode | `test_none_mode_treated_as_emergency_shutdown` | error |

## Internal State (from `internal_state`)

| Design Element | Test Case(s) | Strategy |
|---|---|---|
| state: initial previous_rate is 0.0 | `test_initial_previous_rate_is_zero` | stateful |
| state: previous_rate tracks clamped value | `test_previous_rate_updated_to_clamped_value_not_requested` | stateful |
| state: mode switch preserves previous_rate | `test_mode_switch_startup_to_cruise_preserves_previous_rate` | stateful |
| state: emergency resets previous_rate to 0 | `test_emergency_shutdown_resets_previous_rate_to_zero` | stateful |
| state: startup after emergency | `test_startup_no_change_request_after_emergency` | stateful |

## Configuration Variants (from `configuration`)

| Design Element | Test Case(s) | Strategy |
|---|---|---|
| config: STARTUP_MIN_RATE override | `test_custom_startup_min_rate` | config |
| config: STARTUP_MAX_RATE override (clamp) | `test_custom_startup_max_rate` | config |
| config: STARTUP_MAX_RATE override (pass) | `test_custom_startup_max_rate_allows_higher_values` | config |
| config: CRUISE_MAX_RATE override | `test_custom_cruise_max_rate` | config |
| config: MAX_RATE_CHANGE override | `test_custom_max_rate_change` | config |

## Handoff Checklist

- [x] Every test would fail if the implementation were deleted
- [x] No test duplicates implementation logic to compute expected values
- [x] No test uses "assert does not throw" as its only assertion
- [x] No mocks used (unit has no external dependencies per design)
- [x] Test names describe scenarios, not method names
- [x] Coverage matrix accounts for all design elements
- [x] Tests are syntactically valid Python/pytest

## Coverage Verification (Step 4)

1. **Every behavior rule tested?** Yes -- behavior[0] (startup): 3 tests, behavior[1] (cruise): 8 tests, behavior[2] (emergency): 3 tests, behavior[3] (was_clamped): 2 tests.
2. **Every error condition tested?** Yes -- all 3 error_handling entries covered, plus fault injection for None mode.
3. **Every input covered by equiv-class + boundary?** Yes -- requested_rate (3 equiv + 9 boundary), operational_mode (3 equiv), elapsed_time_ms (3 equiv + 4 boundary).
4. **Stateful transitions tested?** Yes -- 5 tests covering init, update, mode switch, emergency reset.
5. **Configuration variants tested?** Yes -- all 4 config params with 5 tests.

## Summary

| Strategy | Test Count |
|---|---|
| Requirement-based | 16 |
| Equivalence class | 9 |
| Boundary value | 15 |
| Error handling | 6 |
| Stateful behavior | 5 |
| Configuration variants | 5 |
| **Total** | **56** |
