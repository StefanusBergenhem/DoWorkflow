"""
Test suite for FuelRateLimiter -- derived from detailed design DD-001.

Derivation strategies used:
  1. Requirement-based (from behavior section)
  2. Equivalence class partitioning (from interfaces)
  3. Boundary value analysis (from interface constraints + configuration)
  4. Error handling / fault injection (from error_handling section)

All expected values are hardcoded from the design specification.
No test recomputes expected values using implementation logic (avoids Mirror Test).
"""

import pytest

from fuel_rate_limiter import FuelRateLimiter


# ---------------------------------------------------------------------------
# Fixtures
# ---------------------------------------------------------------------------

@pytest.fixture
def limiter():
    """Default-configured FuelRateLimiter (design defaults)."""
    return FuelRateLimiter(
        startup_min_rate=10.0,
        startup_max_rate=50.0,
        cruise_max_rate=200.0,
        max_rate_change=100.0,
    )


@pytest.fixture
def custom_limiter():
    """FuelRateLimiter with non-default configuration for config-variant tests."""
    return FuelRateLimiter(
        startup_min_rate=20.0,
        startup_max_rate=80.0,
        cruise_max_rate=300.0,
        max_rate_change=50.0,
    )


# ===========================================================================
# Strategy 1: Requirement-Based Tests (from behavior section)
# ===========================================================================


class TestStartupModeBehavior:
    """behavior[0]: operational_mode is startup -- clamp to [STARTUP_MIN, STARTUP_MAX]."""

    def test_startup_clamps_rate_below_minimum_to_minimum(self, limiter):
        # Arrange: rate 5.0 below STARTUP_MIN_RATE (10.0)
        # Act
        result = limiter.limit(requested_rate=5.0, operational_mode="startup", elapsed_time_ms=100)
        # Assert: clamped up to minimum
        assert result.actual_rate == 10.0
        assert result.was_clamped is True
        assert result.clamping_reason == "mode_min"

    def test_startup_clamps_rate_above_maximum_to_maximum(self, limiter):
        # Arrange: rate 75.0 above STARTUP_MAX_RATE (50.0)
        result = limiter.limit(requested_rate=75.0, operational_mode="startup", elapsed_time_ms=100)
        assert result.actual_rate == 50.0
        assert result.was_clamped is True
        assert result.clamping_reason == "mode_max"

    def test_startup_passes_rate_within_bounds_unchanged(self, limiter):
        # Arrange: rate 30.0 within [10.0, 50.0]
        result = limiter.limit(requested_rate=30.0, operational_mode="startup", elapsed_time_ms=100)
        assert result.actual_rate == 30.0
        assert result.was_clamped is False
        assert result.clamping_reason == "none"


class TestCruiseModeBehavior:
    """behavior[1]: operational_mode is cruise -- clamp to [0, CRUISE_MAX] + rate-of-change."""

    def test_cruise_clamps_rate_above_max(self, limiter):
        # Arrange: rate 250.0 above CRUISE_MAX_RATE (200.0)
        result = limiter.limit(requested_rate=250.0, operational_mode="cruise", elapsed_time_ms=1000)
        assert result.actual_rate == 200.0
        assert result.was_clamped is True
        assert result.clamping_reason == "mode_max"

    def test_cruise_passes_rate_within_bounds(self, limiter):
        # Arrange: rate 100.0, within [0, 200], within rate-of-change from 0.0
        result = limiter.limit(requested_rate=100.0, operational_mode="cruise", elapsed_time_ms=1000)
        assert result.actual_rate == 100.0
        assert result.was_clamped is False
        assert result.clamping_reason == "none"

    def test_cruise_allows_zero_rate(self, limiter):
        # Arrange: zero rate, previous_rate=0.0, no change needed
        result = limiter.limit(requested_rate=0.0, operational_mode="cruise", elapsed_time_ms=1000)
        assert result.actual_rate == 0.0
        assert result.was_clamped is False
        assert result.clamping_reason == "none"

    def test_cruise_rate_of_change_limits_large_increase(self, limiter):
        # Arrange: previous_rate = 0.0 (default), requesting 150.0
        # elapsed = 1000ms = 1s, max delta = 100.0 * 1.0 = 100.0
        # Act
        result = limiter.limit(requested_rate=150.0, operational_mode="cruise", elapsed_time_ms=1000)
        # Assert: clamped to 0.0 + 100.0 = 100.0
        assert result.actual_rate == 100.0
        assert result.was_clamped is True
        assert result.clamping_reason == "rate_of_change"

    def test_cruise_rate_of_change_limits_large_decrease(self, limiter):
        # Arrange: set previous_rate to 200.0
        limiter.limit(requested_rate=200.0, operational_mode="cruise", elapsed_time_ms=10000)
        # Act: request 0.0, elapsed 1s, max delta = 100.0
        result = limiter.limit(requested_rate=0.0, operational_mode="cruise", elapsed_time_ms=1000)
        # Assert: 200.0 - 100.0 = 100.0
        assert result.actual_rate == 100.0
        assert result.was_clamped is True
        assert result.clamping_reason == "rate_of_change"

    def test_cruise_rate_of_change_within_limit_not_clamped(self, limiter):
        # Arrange: previous_rate = 0.0, request 50.0, elapsed 1s
        # delta = 50.0, max delta = 100.0 -- within limit
        result = limiter.limit(requested_rate=50.0, operational_mode="cruise", elapsed_time_ms=1000)
        assert result.actual_rate == 50.0
        assert result.was_clamped is False
        assert result.clamping_reason == "none"

    def test_cruise_rate_of_change_with_short_elapsed_time(self, limiter):
        # Arrange: previous_rate = 0.0, request 50.0, elapsed 100ms
        # max delta = 100.0 * 0.1 = 10.0
        result = limiter.limit(requested_rate=50.0, operational_mode="cruise", elapsed_time_ms=100)
        assert result.actual_rate == 10.0
        assert result.was_clamped is True
        assert result.clamping_reason == "rate_of_change"

    def test_cruise_mode_max_and_rate_of_change_both_apply(self, limiter):
        # Arrange: request 500.0 exceeds cruise max (200) AND rate-of-change (100)
        # Design: first clamp to [0, CRUISE_MAX], then apply rate-of-change
        # After cruise clamp: 200.0, then rate-of-change: delta 200 > 100, clamp to 100.0
        result = limiter.limit(requested_rate=500.0, operational_mode="cruise", elapsed_time_ms=1000)
        assert result.actual_rate == 100.0
        assert result.was_clamped is True
        assert result.clamping_reason == "rate_of_change"


class TestEmergencyShutdownBehavior:
    """behavior[2]: operational_mode is emergency_shutdown."""

    def test_emergency_shutdown_forces_zero_rate(self, limiter):
        result = limiter.limit(requested_rate=100.0, operational_mode="emergency_shutdown", elapsed_time_ms=100)
        assert result.actual_rate == 0.0
        assert result.was_clamped is True
        assert result.clamping_reason == "emergency"

    def test_emergency_shutdown_with_zero_request_still_reports_clamped(self, limiter):
        # Design explicitly says "Set was_clamped to true" -- unconditional in emergency
        result = limiter.limit(requested_rate=0.0, operational_mode="emergency_shutdown", elapsed_time_ms=100)
        assert result.actual_rate == 0.0
        assert result.was_clamped is True
        assert result.clamping_reason == "emergency"

    def test_emergency_shutdown_ignores_very_high_rate(self, limiter):
        result = limiter.limit(requested_rate=999.0, operational_mode="emergency_shutdown", elapsed_time_ms=100)
        assert result.actual_rate == 0.0
        assert result.was_clamped is True
        assert result.clamping_reason == "emergency"


class TestWasClampedFlag:
    """behavior[3]: was_clamped is true if and only if actual_rate != requested_rate."""

    def test_was_clamped_true_when_actual_differs_from_requested(self, limiter):
        result = limiter.limit(requested_rate=5.0, operational_mode="startup", elapsed_time_ms=100)
        assert result.actual_rate != 5.0
        assert result.was_clamped is True

    def test_was_clamped_false_when_actual_equals_requested(self, limiter):
        result = limiter.limit(requested_rate=30.0, operational_mode="startup", elapsed_time_ms=100)
        assert result.actual_rate == 30.0
        assert result.was_clamped is False


# ===========================================================================
# Strategy 2: Equivalence Class Partitioning (from interfaces)
# ===========================================================================


class TestRequestedRateEquivalenceClasses:
    """Equivalence classes for the requested_rate input (float >= 0.0)."""

    def test_zero_rate_in_cruise(self, limiter):
        # Class: zero (minimum valid value)
        result = limiter.limit(requested_rate=0.0, operational_mode="cruise", elapsed_time_ms=1000)
        assert result.actual_rate == 0.0

    def test_nominal_valid_rate_in_cruise(self, limiter):
        # Class: typical mid-range value
        result = limiter.limit(requested_rate=100.0, operational_mode="cruise", elapsed_time_ms=10000)
        assert result.actual_rate == 100.0

    def test_high_rate_exceeding_mode_max(self, limiter):
        # Class: above mode maximum (triggers clamping)
        result = limiter.limit(requested_rate=500.0, operational_mode="cruise", elapsed_time_ms=10000)
        assert result.actual_rate == 200.0
        assert result.was_clamped is True


class TestOperationalModeEquivalenceClasses:
    """One test per enum value for operational_mode."""

    def test_startup_mode_nominal(self, limiter):
        result = limiter.limit(requested_rate=30.0, operational_mode="startup", elapsed_time_ms=100)
        assert result.actual_rate == 30.0
        assert result.clamping_reason == "none"

    def test_cruise_mode_nominal(self, limiter):
        result = limiter.limit(requested_rate=100.0, operational_mode="cruise", elapsed_time_ms=10000)
        assert result.actual_rate == 100.0
        assert result.clamping_reason == "none"

    def test_emergency_shutdown_mode_nominal(self, limiter):
        result = limiter.limit(requested_rate=100.0, operational_mode="emergency_shutdown", elapsed_time_ms=100)
        assert result.actual_rate == 0.0
        assert result.clamping_reason == "emergency"


class TestElapsedTimeEquivalenceClasses:
    """Equivalence classes for elapsed_time_ms input (integer >= 0)."""

    def test_zero_elapsed_prevents_any_rate_change(self, limiter):
        # Class: zero -- max delta = 0, previous_rate = 0.0 so stuck at 0.0
        result = limiter.limit(requested_rate=50.0, operational_mode="cruise", elapsed_time_ms=0)
        assert result.actual_rate == 0.0
        assert result.was_clamped is True
        assert result.clamping_reason == "rate_of_change"

    def test_typical_elapsed_time(self, limiter):
        # Class: typical value (500ms)
        result = limiter.limit(requested_rate=50.0, operational_mode="cruise", elapsed_time_ms=500)
        # max delta = 100.0 * 0.5 = 50.0, request delta = 50.0 -- exactly at limit
        assert result.actual_rate == 50.0

    def test_large_elapsed_allows_full_change(self, limiter):
        # Class: large value -- rate-of-change effectively unconstrained
        result = limiter.limit(requested_rate=150.0, operational_mode="cruise", elapsed_time_ms=10000)
        assert result.actual_rate == 150.0
        assert result.was_clamped is False


# ===========================================================================
# Strategy 3: Boundary Value Analysis (from interface constraints)
# ===========================================================================


class TestStartupRateBoundaries:
    """Boundaries for startup mode: [STARTUP_MIN_RATE, STARTUP_MAX_RATE] = [10.0, 50.0]."""

    def test_just_below_startup_min(self, limiter):
        # 9.99 < 10.0 -- should clamp to min
        result = limiter.limit(requested_rate=9.99, operational_mode="startup", elapsed_time_ms=100)
        assert result.actual_rate == 10.0
        assert result.was_clamped is True
        assert result.clamping_reason == "mode_min"

    def test_at_startup_min(self, limiter):
        # Exactly 10.0 -- no clamping
        result = limiter.limit(requested_rate=10.0, operational_mode="startup", elapsed_time_ms=100)
        assert result.actual_rate == 10.0
        assert result.was_clamped is False
        assert result.clamping_reason == "none"

    def test_nominal_within_startup_range(self, limiter):
        # 30.0 -- mid-range, no clamping
        result = limiter.limit(requested_rate=30.0, operational_mode="startup", elapsed_time_ms=100)
        assert result.actual_rate == 30.0
        assert result.was_clamped is False

    def test_at_startup_max(self, limiter):
        # Exactly 50.0 -- no clamping
        result = limiter.limit(requested_rate=50.0, operational_mode="startup", elapsed_time_ms=100)
        assert result.actual_rate == 50.0
        assert result.was_clamped is False
        assert result.clamping_reason == "none"

    def test_just_above_startup_max(self, limiter):
        # 50.01 > 50.0 -- should clamp to max
        result = limiter.limit(requested_rate=50.01, operational_mode="startup", elapsed_time_ms=100)
        assert result.actual_rate == 50.0
        assert result.was_clamped is True
        assert result.clamping_reason == "mode_max"


class TestCruiseRateBoundaries:
    """Boundaries for cruise mode: [0.0, CRUISE_MAX_RATE] = [0.0, 200.0]."""

    def test_at_cruise_min_zero(self, limiter):
        result = limiter.limit(requested_rate=0.0, operational_mode="cruise", elapsed_time_ms=1000)
        assert result.actual_rate == 0.0
        assert result.was_clamped is False

    def test_just_below_cruise_max(self, limiter):
        # 199.99 < 200.0 -- should pass
        result = limiter.limit(requested_rate=199.99, operational_mode="cruise", elapsed_time_ms=10000)
        assert result.actual_rate == 199.99
        assert result.was_clamped is False

    def test_at_cruise_max(self, limiter):
        # Exactly 200.0 -- should pass
        result = limiter.limit(requested_rate=200.0, operational_mode="cruise", elapsed_time_ms=10000)
        assert result.actual_rate == 200.0
        assert result.was_clamped is False

    def test_just_above_cruise_max(self, limiter):
        # 200.01 > 200.0 -- should clamp
        result = limiter.limit(requested_rate=200.01, operational_mode="cruise", elapsed_time_ms=10000)
        assert result.actual_rate == 200.0
        assert result.was_clamped is True
        assert result.clamping_reason == "mode_max"


class TestRateOfChangeBoundaries:
    """Boundaries for the rate-of-change limit in cruise mode."""

    def test_rate_change_exactly_at_limit_not_clamped(self, limiter):
        # previous=0.0, request 100.0 with 1s: delta=100.0, max delta=100.0 -- at limit
        result = limiter.limit(requested_rate=100.0, operational_mode="cruise", elapsed_time_ms=1000)
        assert result.actual_rate == 100.0
        assert result.was_clamped is False
        assert result.clamping_reason == "none"

    def test_rate_change_just_above_limit_is_clamped(self, limiter):
        # previous=0.0, request 100.01 with 1s: delta=100.01 > 100.0
        result = limiter.limit(requested_rate=100.01, operational_mode="cruise", elapsed_time_ms=1000)
        assert result.actual_rate == 100.0
        assert result.was_clamped is True
        assert result.clamping_reason == "rate_of_change"

    def test_rate_change_zero_elapsed_prevents_any_change(self, limiter):
        # Set previous to 100.0, then try to change with 0 elapsed
        limiter.limit(requested_rate=100.0, operational_mode="cruise", elapsed_time_ms=10000)
        result = limiter.limit(requested_rate=150.0, operational_mode="cruise", elapsed_time_ms=0)
        # max delta = 100.0 * 0 = 0.0 -- no change allowed
        assert result.actual_rate == 100.0
        assert result.was_clamped is True
        assert result.clamping_reason == "rate_of_change"

    def test_rate_change_at_one_ms_allows_tiny_delta(self, limiter):
        # previous=0.0, request 50.0 with 1ms: max delta = 100.0 * 0.001 = 0.1
        result = limiter.limit(requested_rate=50.0, operational_mode="cruise", elapsed_time_ms=1)
        assert result.actual_rate == pytest.approx(0.1, abs=0.01)
        assert result.was_clamped is True
        assert result.clamping_reason == "rate_of_change"


class TestOutputBoundaries:
    """Output constraint: actual_rate >= 0.0, <= 500.0."""

    def test_output_never_below_zero(self, limiter):
        # Request 0.0 in cruise -- output stays at 0.0
        result = limiter.limit(requested_rate=0.0, operational_mode="cruise", elapsed_time_ms=1000)
        assert result.actual_rate >= 0.0

    def test_output_capped_at_500_even_with_high_cruise_max(self):
        # Configure cruise max above the output ceiling
        big_limiter = FuelRateLimiter(
            startup_min_rate=10.0,
            startup_max_rate=50.0,
            cruise_max_rate=600.0,
            max_rate_change=1000.0,
        )
        result = big_limiter.limit(requested_rate=550.0, operational_mode="cruise", elapsed_time_ms=1000)
        # Design says output <= 500.0
        assert result.actual_rate <= 500.0


# ===========================================================================
# Strategy 4: Error Handling / Fault Injection (from error_handling)
# ===========================================================================


class TestErrorHandling:
    """Tests for each error_handling entry in the design."""

    def test_negative_requested_rate_treated_as_zero_in_startup(self, limiter):
        # error_handling[0]: treat as 0.0, clamped, reason mode_min
        # Then startup behavior clamps 0.0 up to STARTUP_MIN_RATE = 10.0
        result = limiter.limit(requested_rate=-10.0, operational_mode="startup", elapsed_time_ms=100)
        assert result.actual_rate == 10.0
        assert result.was_clamped is True
        assert result.clamping_reason == "mode_min"

    def test_negative_requested_rate_treated_as_zero_in_cruise(self, limiter):
        # error_handling[0]: treat as 0.0 in cruise mode
        result = limiter.limit(requested_rate=-5.0, operational_mode="cruise", elapsed_time_ms=1000)
        assert result.actual_rate == 0.0
        assert result.was_clamped is True
        assert result.clamping_reason == "mode_min"

    def test_negative_elapsed_time_skips_rate_of_change_limiting(self, limiter):
        # error_handling[1]: treat as 0, skip rate-of-change for this call
        # Set previous to 50.0 first
        limiter.limit(requested_rate=50.0, operational_mode="cruise", elapsed_time_ms=1000)
        # Large jump that would normally be rate-of-change limited
        result = limiter.limit(requested_rate=200.0, operational_mode="cruise", elapsed_time_ms=-100)
        # Without rate-of-change: 200.0 within cruise max, so passes
        assert result.actual_rate == 200.0
        assert result.was_clamped is False
        assert result.clamping_reason == "none"

    def test_unrecognized_mode_treated_as_emergency_shutdown(self, limiter):
        # error_handling[2]: fail-safe to emergency_shutdown
        result = limiter.limit(requested_rate=100.0, operational_mode="invalid_mode", elapsed_time_ms=100)
        assert result.actual_rate == 0.0
        assert result.was_clamped is True
        assert result.clamping_reason == "emergency"

    def test_empty_string_mode_treated_as_emergency_shutdown(self, limiter):
        # Another unrecognized mode variant
        result = limiter.limit(requested_rate=100.0, operational_mode="", elapsed_time_ms=100)
        assert result.actual_rate == 0.0
        assert result.was_clamped is True
        assert result.clamping_reason == "emergency"

    def test_none_mode_treated_as_emergency_shutdown(self, limiter):
        # Fault injection: None as mode value
        result = limiter.limit(requested_rate=100.0, operational_mode=None, elapsed_time_ms=100)
        assert result.actual_rate == 0.0
        assert result.was_clamped is True
        assert result.clamping_reason == "emergency"


# ===========================================================================
# Stateful Behavior Tests (from internal_state: previous_rate)
# ===========================================================================


class TestStatefulBehavior:
    """Tests verifying previous_rate state tracking across calls."""

    def test_initial_previous_rate_is_zero(self, limiter):
        # First call: previous_rate = 0.0, request 100.0, 1s
        # delta=100.0, max delta=100.0 -- exactly at limit, not clamped
        result = limiter.limit(requested_rate=100.0, operational_mode="cruise", elapsed_time_ms=1000)
        assert result.actual_rate == 100.0
        assert result.was_clamped is False

    def test_previous_rate_updated_to_clamped_value_not_requested(self, limiter):
        # Call 1: previous=0.0, request 80.0, 1s -> actual=80.0 (delta 80 < 100)
        result1 = limiter.limit(requested_rate=80.0, operational_mode="cruise", elapsed_time_ms=1000)
        assert result1.actual_rate == 80.0
        # Call 2: previous=80.0, request 200.0, 1s -> max delta=100, so actual=180.0
        result2 = limiter.limit(requested_rate=200.0, operational_mode="cruise", elapsed_time_ms=1000)
        assert result2.actual_rate == 180.0
        assert result2.was_clamped is True
        assert result2.clamping_reason == "rate_of_change"
        # Call 3: previous=180.0 (the clamped value), request 200.0, 500ms
        # max delta = 100.0 * 0.5 = 50.0, so actual = 180 + 20 = 200.0 (within limit)
        result3 = limiter.limit(requested_rate=200.0, operational_mode="cruise", elapsed_time_ms=500)
        assert result3.actual_rate == 200.0
        assert result3.was_clamped is False

    def test_mode_switch_startup_to_cruise_preserves_previous_rate(self, limiter):
        # Startup: actual=30.0
        limiter.limit(requested_rate=30.0, operational_mode="startup", elapsed_time_ms=100)
        # Switch to cruise: previous=30.0, request 200.0, 1s -> max delta=100 -> 130.0
        result = limiter.limit(requested_rate=200.0, operational_mode="cruise", elapsed_time_ms=1000)
        assert result.actual_rate == 130.0
        assert result.was_clamped is True
        assert result.clamping_reason == "rate_of_change"

    def test_emergency_shutdown_resets_previous_rate_to_zero(self, limiter):
        # Set rate to 100.0
        limiter.limit(requested_rate=100.0, operational_mode="cruise", elapsed_time_ms=10000)
        # Emergency sets actual to 0.0
        limiter.limit(requested_rate=100.0, operational_mode="emergency_shutdown", elapsed_time_ms=100)
        # Back to cruise: previous=0.0, request 150.0, 1s -> max delta=100 -> 100.0
        result = limiter.limit(requested_rate=150.0, operational_mode="cruise", elapsed_time_ms=1000)
        assert result.actual_rate == 100.0
        assert result.was_clamped is True

    def test_startup_no_change_request_after_emergency(self, limiter):
        # Emergency sets previous to 0.0
        limiter.limit(requested_rate=100.0, operational_mode="emergency_shutdown", elapsed_time_ms=100)
        # Startup: request 30.0 -- within [10, 50], no rate-of-change in startup mode
        result = limiter.limit(requested_rate=30.0, operational_mode="startup", elapsed_time_ms=100)
        assert result.actual_rate == 30.0


# ===========================================================================
# Configuration Variant Tests (from configuration section)
# ===========================================================================


class TestConfigurationVariants:
    """Tests with non-default config to verify all four config params are respected."""

    def test_custom_startup_min_rate(self, custom_limiter):
        # STARTUP_MIN_RATE = 20.0 (instead of default 10.0)
        result = custom_limiter.limit(requested_rate=15.0, operational_mode="startup", elapsed_time_ms=100)
        assert result.actual_rate == 20.0
        assert result.was_clamped is True
        assert result.clamping_reason == "mode_min"

    def test_custom_startup_max_rate(self, custom_limiter):
        # STARTUP_MAX_RATE = 80.0 (instead of default 50.0)
        result = custom_limiter.limit(requested_rate=90.0, operational_mode="startup", elapsed_time_ms=100)
        assert result.actual_rate == 80.0
        assert result.was_clamped is True
        assert result.clamping_reason == "mode_max"

    def test_custom_startup_max_rate_allows_higher_values(self, custom_limiter):
        # 60.0 is within custom [20, 80] but would exceed default max of 50.0
        result = custom_limiter.limit(requested_rate=60.0, operational_mode="startup", elapsed_time_ms=100)
        assert result.actual_rate == 60.0
        assert result.was_clamped is False

    def test_custom_cruise_max_rate(self, custom_limiter):
        # CRUISE_MAX_RATE = 300.0 (instead of default 200.0)
        # 250.0 would exceed default but passes custom config
        result = custom_limiter.limit(requested_rate=250.0, operational_mode="cruise", elapsed_time_ms=10000)
        assert result.actual_rate == 250.0
        assert result.was_clamped is False

    def test_custom_max_rate_change(self, custom_limiter):
        # MAX_RATE_CHANGE = 50.0 (instead of default 100.0) -- tighter limit
        # previous=0.0, request 100.0, 1s -> max delta = 50.0 -> actual = 50.0
        result = custom_limiter.limit(requested_rate=100.0, operational_mode="cruise", elapsed_time_ms=1000)
        assert result.actual_rate == 50.0
        assert result.was_clamped is True
        assert result.clamping_reason == "rate_of_change"
