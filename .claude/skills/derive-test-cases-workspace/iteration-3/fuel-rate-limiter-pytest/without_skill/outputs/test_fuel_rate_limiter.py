"""
Test suite for FuelRateLimiter unit.

Tests cover:
- Normal operation in each operational mode
- Boundary conditions (min/max rates)
- Clamping behavior and reporting
- Rate-of-change limiting in cruise mode
- Error handling (negative inputs, invalid modes)
- State tracking (previous_rate)
"""

import pytest
from enum import Enum


class OperationalMode(Enum):
    STARTUP = "startup"
    CRUISE = "cruise"
    EMERGENCY_SHUTDOWN = "emergency_shutdown"


class ClampingReason(Enum):
    NONE = "none"
    MODE_MAX = "mode_max"
    MODE_MIN = "mode_min"
    RATE_OF_CHANGE = "rate_of_change"
    EMERGENCY = "emergency"


class FuelRateLimiter:
    """Limits fuel flow rate based on operational mode and safety constraints."""

    # Configuration constants
    STARTUP_MIN_RATE = 10.0
    STARTUP_MAX_RATE = 50.0
    CRUISE_MAX_RATE = 200.0
    MAX_RATE_CHANGE = 100.0  # liters/hour per second

    def __init__(self):
        self.previous_rate = 0.0

    def limit_rate(self, requested_rate, operational_mode, elapsed_time_ms):
        """
        Limit fuel flow rate based on operational mode and constraints.

        Args:
            requested_rate: Desired fuel rate (liters/hour, >= 0.0)
            operational_mode: Current engine mode (startup, cruise, emergency_shutdown)
            elapsed_time_ms: Time since last call (milliseconds, >= 0)

        Returns:
            Tuple of (actual_rate, was_clamped, clamping_reason)
        """
        # Validate and normalize inputs
        if requested_rate < 0.0:
            requested_rate = 0.0
            was_clamped = True
            clamping_reason = ClampingReason.MODE_MIN
            actual_rate = 0.0
            self.previous_rate = actual_rate
            return actual_rate, was_clamped, clamping_reason

        if elapsed_time_ms < 0:
            elapsed_time_ms = 0

        # Handle invalid mode as fail-safe
        if operational_mode not in [OperationalMode.STARTUP, OperationalMode.CRUISE, OperationalMode.EMERGENCY_SHUTDOWN]:
            operational_mode = OperationalMode.EMERGENCY_SHUTDOWN

        # Mode-specific logic
        if operational_mode == OperationalMode.EMERGENCY_SHUTDOWN:
            actual_rate = 0.0
            was_clamped = True
            clamping_reason = ClampingReason.EMERGENCY

        elif operational_mode == OperationalMode.STARTUP:
            # Clamp to startup range
            if requested_rate < self.STARTUP_MIN_RATE:
                actual_rate = self.STARTUP_MIN_RATE
                was_clamped = True
                clamping_reason = ClampingReason.MODE_MIN
            elif requested_rate > self.STARTUP_MAX_RATE:
                actual_rate = self.STARTUP_MAX_RATE
                was_clamped = True
                clamping_reason = ClampingReason.MODE_MAX
            else:
                actual_rate = requested_rate
                was_clamped = False
                clamping_reason = ClampingReason.NONE

        elif operational_mode == OperationalMode.CRUISE:
            # Clamp to cruise max
            if requested_rate > self.CRUISE_MAX_RATE:
                clamped_by_max = self.CRUISE_MAX_RATE
            else:
                clamped_by_max = requested_rate

            # Apply rate-of-change limit
            elapsed_time_s = elapsed_time_ms / 1000.0
            max_change = self.MAX_RATE_CHANGE * elapsed_time_s
            rate_diff = clamped_by_max - self.previous_rate

            if abs(rate_diff) > max_change:
                if rate_diff > 0:
                    actual_rate = self.previous_rate + max_change
                else:
                    actual_rate = self.previous_rate - max_change
                was_clamped = True
                clamping_reason = ClampingReason.RATE_OF_CHANGE
            else:
                actual_rate = clamped_by_max
                was_clamped = (requested_rate != actual_rate)
                clamping_reason = ClampingReason.MODE_MAX if was_clamped else ClampingReason.NONE

        # Update state
        self.previous_rate = actual_rate

        return actual_rate, was_clamped, clamping_reason


# ============================================================================
# TESTS
# ============================================================================

class TestFuelRateLimiterStartupMode:
    """Tests for startup mode behavior."""

    def test_startup_within_bounds_no_clamp(self):
        limiter = FuelRateLimiter()
        actual, was_clamped, reason = limiter.limit_rate(
            requested_rate=30.0,
            operational_mode=OperationalMode.STARTUP,
            elapsed_time_ms=100
        )
        assert actual == 30.0
        assert was_clamped is False
        assert reason == ClampingReason.NONE

    def test_startup_below_minimum_clamps_to_min(self):
        limiter = FuelRateLimiter()
        actual, was_clamped, reason = limiter.limit_rate(
            requested_rate=5.0,
            operational_mode=OperationalMode.STARTUP,
            elapsed_time_ms=100
        )
        assert actual == 10.0  # STARTUP_MIN_RATE
        assert was_clamped is True
        assert reason == ClampingReason.MODE_MIN

    def test_startup_above_maximum_clamps_to_max(self):
        limiter = FuelRateLimiter()
        actual, was_clamped, reason = limiter.limit_rate(
            requested_rate=75.0,
            operational_mode=OperationalMode.STARTUP,
            elapsed_time_ms=100
        )
        assert actual == 50.0  # STARTUP_MAX_RATE
        assert was_clamped is True
        assert reason == ClampingReason.MODE_MAX

    def test_startup_at_minimum_boundary(self):
        limiter = FuelRateLimiter()
        actual, was_clamped, reason = limiter.limit_rate(
            requested_rate=10.0,
            operational_mode=OperationalMode.STARTUP,
            elapsed_time_ms=100
        )
        assert actual == 10.0
        assert was_clamped is False
        assert reason == ClampingReason.NONE

    def test_startup_at_maximum_boundary(self):
        limiter = FuelRateLimiter()
        actual, was_clamped, reason = limiter.limit_rate(
            requested_rate=50.0,
            operational_mode=OperationalMode.STARTUP,
            elapsed_time_ms=100
        )
        assert actual == 50.0
        assert was_clamped is False
        assert reason == ClampingReason.NONE


class TestFuelRateLimiterCruiseMode:
    """Tests for cruise mode behavior."""

    def test_cruise_within_bounds_no_clamp(self):
        limiter = FuelRateLimiter()
        actual, was_clamped, reason = limiter.limit_rate(
            requested_rate=100.0,
            operational_mode=OperationalMode.CRUISE,
            elapsed_time_ms=100
        )
        assert actual == 100.0
        assert was_clamped is False
        assert reason == ClampingReason.NONE

    def test_cruise_above_maximum_clamps_to_max(self):
        limiter = FuelRateLimiter()
        actual, was_clamped, reason = limiter.limit_rate(
            requested_rate=250.0,
            operational_mode=OperationalMode.CRUISE,
            elapsed_time_ms=100
        )
        assert actual == 200.0  # CRUISE_MAX_RATE
        assert was_clamped is True
        assert reason == ClampingReason.MODE_MAX

    def test_cruise_zero_rate_allowed(self):
        limiter = FuelRateLimiter()
        actual, was_clamped, reason = limiter.limit_rate(
            requested_rate=0.0,
            operational_mode=OperationalMode.CRUISE,
            elapsed_time_ms=100
        )
        assert actual == 0.0
        assert was_clamped is False
        assert reason == ClampingReason.NONE

    def test_cruise_at_maximum_boundary(self):
        limiter = FuelRateLimiter()
        actual, was_clamped, reason = limiter.limit_rate(
            requested_rate=200.0,
            operational_mode=OperationalMode.CRUISE,
            elapsed_time_ms=100
        )
        assert actual == 200.0
        assert was_clamped is False
        assert reason == ClampingReason.NONE


class TestFuelRateLimiterRateOfChange:
    """Tests for rate-of-change limiting in cruise mode."""

    def test_rate_of_change_increase_within_limit(self):
        limiter = FuelRateLimiter()
        # Set initial rate
        limiter.limit_rate(50.0, OperationalMode.CRUISE, 100)
        # Increase by 10 L/h, well within limit of 10 L/s * 0.1s = 1 L/h
        actual, was_clamped, reason = limiter.limit_rate(
            requested_rate=60.0,
            operational_mode=OperationalMode.CRUISE,
            elapsed_time_ms=100
        )
        assert actual == 55.0  # previous_rate=50 + max_change=5
        assert was_clamped is True
        assert reason == ClampingReason.RATE_OF_CHANGE

    def test_rate_of_change_slow_increase_allowed(self):
        limiter = FuelRateLimiter()
        # Set initial rate
        limiter.limit_rate(50.0, OperationalMode.CRUISE, 100)
        # Small increase within rate-of-change limit
        actual, was_clamped, reason = limiter.limit_rate(
            requested_rate=52.0,
            operational_mode=OperationalMode.CRUISE,
            elapsed_time_ms=100
        )
        assert actual == 52.0
        assert was_clamped is False
        assert reason == ClampingReason.NONE

    def test_rate_of_change_decrease_within_limit(self):
        limiter = FuelRateLimiter()
        # Set initial rate
        limiter.limit_rate(100.0, OperationalMode.CRUISE, 100)
        # Decrease by 10 L/h, well within limit
        actual, was_clamped, reason = limiter.limit_rate(
            requested_rate=80.0,
            operational_mode=OperationalMode.CRUISE,
            elapsed_time_ms=100
        )
        assert actual == 95.0  # previous_rate=100 - max_change=5
        assert was_clamped is True
        assert reason == ClampingReason.RATE_OF_CHANGE

    def test_rate_of_change_zero_elapsed_time(self):
        limiter = FuelRateLimiter()
        # Set initial rate
        limiter.limit_rate(50.0, OperationalMode.CRUISE, 100)
        # Zero elapsed time means max_change=0, so rate cannot change
        actual, was_clamped, reason = limiter.limit_rate(
            requested_rate=60.0,
            operational_mode=OperationalMode.CRUISE,
            elapsed_time_ms=0
        )
        assert actual == 50.0
        assert was_clamped is True
        assert reason == ClampingReason.RATE_OF_CHANGE

    def test_rate_of_change_large_time_delta_allows_large_change(self):
        limiter = FuelRateLimiter()
        # Set initial rate
        limiter.limit_rate(50.0, OperationalMode.CRUISE, 100)
        # 10 seconds elapsed time: max_change = 100 L/h/s * 10s = 1000 L/h
        actual, was_clamped, reason = limiter.limit_rate(
            requested_rate=200.0,
            operational_mode=OperationalMode.CRUISE,
            elapsed_time_ms=10000
        )
        assert actual == 200.0
        assert was_clamped is False
        assert reason == ClampingReason.NONE

    def test_rate_of_change_respects_cruise_max(self):
        limiter = FuelRateLimiter()
        # Set initial rate
        limiter.limit_rate(150.0, OperationalMode.CRUISE, 100)
        # Request increase to 250, but cruise max is 200
        # 100ms: max_change = 100 * 0.1 = 10, so clamped to 150+10=160
        # Then compare: requested=250 exceeds cruise_max=200, so clamped to 200
        # But 200 > 150+10=160, so rate-of-change limit applies first
        actual, was_clamped, reason = limiter.limit_rate(
            requested_rate=250.0,
            operational_mode=OperationalMode.CRUISE,
            elapsed_time_ms=100
        )
        assert actual == 160.0
        assert was_clamped is True
        assert reason == ClampingReason.RATE_OF_CHANGE


class TestFuelRateLimiterEmergencyShutdown:
    """Tests for emergency shutdown mode."""

    def test_emergency_shutdown_forces_zero_rate(self):
        limiter = FuelRateLimiter()
        actual, was_clamped, reason = limiter.limit_rate(
            requested_rate=100.0,
            operational_mode=OperationalMode.EMERGENCY_SHUTDOWN,
            elapsed_time_ms=100
        )
        assert actual == 0.0
        assert was_clamped is True
        assert reason == ClampingReason.EMERGENCY

    def test_emergency_shutdown_from_zero_rate(self):
        limiter = FuelRateLimiter()
        actual, was_clamped, reason = limiter.limit_rate(
            requested_rate=0.0,
            operational_mode=OperationalMode.EMERGENCY_SHUTDOWN,
            elapsed_time_ms=100
        )
        assert actual == 0.0
        assert was_clamped is False  # 0.0 == 0.0
        assert reason == ClampingReason.EMERGENCY

    def test_emergency_shutdown_with_high_requested_rate(self):
        limiter = FuelRateLimiter()
        actual, was_clamped, reason = limiter.limit_rate(
            requested_rate=500.0,
            operational_mode=OperationalMode.EMERGENCY_SHUTDOWN,
            elapsed_time_ms=1000
        )
        assert actual == 0.0
        assert was_clamped is True
        assert reason == ClampingReason.EMERGENCY


class TestFuelRateLimiterErrorHandling:
    """Tests for error handling and edge cases."""

    def test_negative_requested_rate_treated_as_zero(self):
        limiter = FuelRateLimiter()
        actual, was_clamped, reason = limiter.limit_rate(
            requested_rate=-10.0,
            operational_mode=OperationalMode.CRUISE,
            elapsed_time_ms=100
        )
        assert actual == 0.0
        assert was_clamped is True
        assert reason == ClampingReason.MODE_MIN

    def test_negative_elapsed_time_treated_as_zero(self):
        limiter = FuelRateLimiter()
        limiter.limit_rate(50.0, OperationalMode.CRUISE, 100)
        # Negative elapsed time treated as 0, so no rate change allowed
        actual, was_clamped, reason = limiter.limit_rate(
            requested_rate=60.0,
            operational_mode=OperationalMode.CRUISE,
            elapsed_time_ms=-100
        )
        assert actual == 50.0
        assert was_clamped is True
        assert reason == ClampingReason.RATE_OF_CHANGE

    def test_invalid_operational_mode_treated_as_emergency(self):
        limiter = FuelRateLimiter()
        # Use an invalid mode (represented as None or invalid value)
        actual, was_clamped, reason = limiter.limit_rate(
            requested_rate=100.0,
            operational_mode="invalid_mode",  # Invalid
            elapsed_time_ms=100
        )
        assert actual == 0.0
        assert was_clamped is True
        assert reason == ClampingReason.EMERGENCY


class TestFuelRateLimiterStateTracking:
    """Tests for state tracking (previous_rate)."""

    def test_previous_rate_updated_after_call(self):
        limiter = FuelRateLimiter()
        actual1, _, _ = limiter.limit_rate(50.0, OperationalMode.CRUISE, 100)
        assert limiter.previous_rate == 50.0

        actual2, _, _ = limiter.limit_rate(75.0, OperationalMode.CRUISE, 100)
        # Rate change from 50 to 75 is 25, limit is 100*0.1=10, so clamped to 60
        assert limiter.previous_rate == 60.0

    def test_state_persists_across_mode_transitions(self):
        limiter = FuelRateLimiter()
        # Cruise mode at 100 L/h
        limiter.limit_rate(100.0, OperationalMode.CRUISE, 100)
        assert limiter.previous_rate == 100.0

        # Switch to startup (previous_rate should still be used for next cruise call)
        limiter.limit_rate(30.0, OperationalMode.STARTUP, 100)
        # Startup doesn't track rate-of-change, but state is preserved
        assert limiter.previous_rate == 30.0

        # Return to cruise from 30 L/h
        actual, _, _ = limiter.limit_rate(80.0, OperationalMode.CRUISE, 100)
        # Rate change from 30 to 80 is 50, limit is 10, so clamped to 40
        assert actual == 40.0
        assert limiter.previous_rate == 40.0

    def test_state_updated_on_emergency_shutdown(self):
        limiter = FuelRateLimiter()
        limiter.limit_rate(100.0, OperationalMode.CRUISE, 100)
        assert limiter.previous_rate == 100.0

        limiter.limit_rate(150.0, OperationalMode.EMERGENCY_SHUTDOWN, 100)
        assert limiter.previous_rate == 0.0


class TestFuelRateLimiterInvariant:
    """Tests for the invariant: was_clamped iff actual_rate != requested_rate."""

    def test_invariant_startup_no_clamp(self):
        limiter = FuelRateLimiter()
        actual, was_clamped, _ = limiter.limit_rate(30.0, OperationalMode.STARTUP, 100)
        assert (was_clamped is True) == (actual != 30.0)
        assert was_clamped is False

    def test_invariant_startup_clamp_min(self):
        limiter = FuelRateLimiter()
        actual, was_clamped, _ = limiter.limit_rate(5.0, OperationalMode.STARTUP, 100)
        assert (was_clamped is True) == (actual != 5.0)
        assert was_clamped is True

    def test_invariant_startup_clamp_max(self):
        limiter = FuelRateLimiter()
        actual, was_clamped, _ = limiter.limit_rate(75.0, OperationalMode.STARTUP, 100)
        assert (was_clamped is True) == (actual != 75.0)
        assert was_clamped is True

    def test_invariant_cruise_no_clamp(self):
        limiter = FuelRateLimiter()
        actual, was_clamped, _ = limiter.limit_rate(100.0, OperationalMode.CRUISE, 100)
        assert (was_clamped is True) == (actual != 100.0)
        assert was_clamped is False

    def test_invariant_cruise_clamp_max(self):
        limiter = FuelRateLimiter()
        actual, was_clamped, _ = limiter.limit_rate(250.0, OperationalMode.CRUISE, 100)
        assert (was_clamped is True) == (actual != 250.0)
        assert was_clamped is True

    def test_invariant_cruise_clamp_roc(self):
        limiter = FuelRateLimiter()
        limiter.limit_rate(50.0, OperationalMode.CRUISE, 100)
        actual, was_clamped, _ = limiter.limit_rate(70.0, OperationalMode.CRUISE, 100)
        assert (was_clamped is True) == (actual != 70.0)
        assert was_clamped is True

    def test_invariant_emergency(self):
        limiter = FuelRateLimiter()
        actual, was_clamped, _ = limiter.limit_rate(100.0, OperationalMode.EMERGENCY_SHUTDOWN, 100)
        assert (was_clamped is True) == (actual != 100.0)
        assert was_clamped is True


class TestFuelRateLimiterOutputRanges:
    """Tests that outputs stay within valid ranges."""

    def test_actual_rate_never_negative(self):
        limiter = FuelRateLimiter()
        test_cases = [
            (-50.0, OperationalMode.CRUISE, 100),
            (0.0, OperationalMode.CRUISE, 100),
            (100.0, OperationalMode.EMERGENCY_SHUTDOWN, 100),
        ]
        for requested, mode, elapsed in test_cases:
            actual, _, _ = limiter.limit_rate(requested, mode, elapsed)
            assert actual >= 0.0

    def test_actual_rate_never_exceeds_max(self):
        limiter = FuelRateLimiter()
        test_cases = [
            (250.0, OperationalMode.CRUISE, 100),
            (500.0, OperationalMode.CRUISE, 1000),
            (1000.0, OperationalMode.CRUISE, 10000),
        ]
        for requested, mode, elapsed in test_cases:
            actual, _, _ = limiter.limit_rate(requested, mode, elapsed)
            assert actual <= 500.0  # Design doc max output


class TestFuelRateLimiterDeterminism:
    """Tests that the limiter is deterministic."""

    def test_same_inputs_produce_same_outputs(self):
        limiter1 = FuelRateLimiter()
        limiter2 = FuelRateLimiter()

        inputs = [
            (50.0, OperationalMode.STARTUP, 100),
            (100.0, OperationalMode.CRUISE, 100),
            (150.0, OperationalMode.CRUISE, 200),
            (0.0, OperationalMode.EMERGENCY_SHUTDOWN, 100),
        ]

        for requested, mode, elapsed in inputs:
            result1 = limiter1.limit_rate(requested, mode, elapsed)
            result2 = limiter2.limit_rate(requested, mode, elapsed)
            assert result1 == result2
