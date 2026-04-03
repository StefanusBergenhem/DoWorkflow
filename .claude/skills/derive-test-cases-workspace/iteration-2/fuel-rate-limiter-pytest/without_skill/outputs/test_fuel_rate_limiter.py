"""
Comprehensive pytest test cases for FuelRateLimiter unit.

Based on detailed design DD-001:
- Limits fuel flow rate based on operational mode and safety constraints
- Enforces mode-specific bounds and rate-of-change limits
- Reports clamping events and reasons
"""

import pytest
from enum import Enum


class OperationalMode(Enum):
    """Operational modes for fuel rate limiting."""
    STARTUP = "startup"
    CRUISE = "cruise"
    EMERGENCY_SHUTDOWN = "emergency_shutdown"


class ClampingReason(Enum):
    """Reasons why fuel rate was clamped."""
    NONE = "none"
    MODE_MAX = "mode_max"
    MODE_MIN = "mode_min"
    RATE_OF_CHANGE = "rate_of_change"
    EMERGENCY = "emergency"


class FuelRateLimiter:
    """
    Limits fuel flow rate based on operational mode and safety constraints.
    """

    def __init__(
        self,
        startup_min_rate=10.0,
        startup_max_rate=50.0,
        cruise_max_rate=200.0,
        max_rate_change=100.0,
    ):
        """
        Initialize FuelRateLimiter with configuration parameters.

        Args:
            startup_min_rate: Minimum fuel rate during startup (liters/hour)
            startup_max_rate: Maximum fuel rate during startup (liters/hour)
            cruise_max_rate: Maximum fuel rate during cruise (liters/hour)
            max_rate_change: Maximum rate change per second (liters/hour/second)
        """
        self.startup_min_rate = startup_min_rate
        self.startup_max_rate = startup_max_rate
        self.cruise_max_rate = cruise_max_rate
        self.max_rate_change = max_rate_change
        self.previous_rate = 0.0

    def limit_rate(self, requested_rate, operational_mode, elapsed_time_ms):
        """
        Limit fuel rate based on operational mode and safety constraints.

        Args:
            requested_rate: Desired fuel rate (liters/hour), >= 0.0
            operational_mode: Current engine mode (OperationalMode enum)
            elapsed_time_ms: Time since last call (milliseconds), >= 0

        Returns:
            Tuple of (actual_rate, was_clamped, clamping_reason)
            - actual_rate: The rate after applying all limits (float)
            - was_clamped: True if actual_rate differs from requested_rate (bool)
            - clamping_reason: Why clamping was applied (ClampingReason enum)
        """
        # Handle negative requested_rate
        if requested_rate < 0.0:
            actual_rate = 0.0
            was_clamped = True
            clamping_reason = ClampingReason.MODE_MIN
            self.previous_rate = actual_rate
            return actual_rate, was_clamped, clamping_reason

        # Handle invalid operational_mode (fail-safe to emergency_shutdown)
        if not isinstance(operational_mode, OperationalMode):
            operational_mode = OperationalMode.EMERGENCY_SHUTDOWN

        # Emergency shutdown: always return 0.0
        if operational_mode == OperationalMode.EMERGENCY_SHUTDOWN:
            actual_rate = 0.0
            was_clamped = True if requested_rate != 0.0 else False
            clamping_reason = (
                ClampingReason.EMERGENCY if was_clamped else ClampingReason.NONE
            )
            self.previous_rate = actual_rate
            return actual_rate, was_clamped, clamping_reason

        # Startup mode: clamp to [STARTUP_MIN_RATE, STARTUP_MAX_RATE]
        if operational_mode == OperationalMode.STARTUP:
            if requested_rate < self.startup_min_rate:
                actual_rate = self.startup_min_rate
                was_clamped = True
                clamping_reason = ClampingReason.MODE_MIN
            elif requested_rate > self.startup_max_rate:
                actual_rate = self.startup_max_rate
                was_clamped = True
                clamping_reason = ClampingReason.MODE_MAX
            else:
                actual_rate = requested_rate
                was_clamped = False
                clamping_reason = ClampingReason.NONE
            self.previous_rate = actual_rate
            return actual_rate, was_clamped, clamping_reason

        # Cruise mode: clamp to [0.0, CRUISE_MAX_RATE] and apply rate-of-change limit
        if operational_mode == OperationalMode.CRUISE:
            # First, clamp to mode bounds
            if requested_rate > self.cruise_max_rate:
                actual_rate = self.cruise_max_rate
                was_clamped = True
                clamping_reason = ClampingReason.MODE_MAX
            else:
                actual_rate = requested_rate
                was_clamped = False
                clamping_reason = ClampingReason.NONE

            # Apply rate-of-change limit (handle negative elapsed_time_ms by treating as 0)
            if elapsed_time_ms < 0:
                elapsed_time_ms = 0

            if elapsed_time_ms > 0:
                max_change = self.max_rate_change * (elapsed_time_ms / 1000.0)
                rate_diff = actual_rate - self.previous_rate
                if abs(rate_diff) > max_change:
                    # Clamp to allowed change
                    if rate_diff > 0:
                        actual_rate = self.previous_rate + max_change
                    else:
                        actual_rate = self.previous_rate - max_change
                    was_clamped = True
                    clamping_reason = ClampingReason.RATE_OF_CHANGE

            self.previous_rate = actual_rate
            return actual_rate, was_clamped, clamping_reason


# ============================================================================
# TEST FIXTURES
# ============================================================================


@pytest.fixture
def limiter():
    """Create a FuelRateLimiter with default configuration."""
    return FuelRateLimiter(
        startup_min_rate=10.0,
        startup_max_rate=50.0,
        cruise_max_rate=200.0,
        max_rate_change=100.0,
    )


@pytest.fixture
def limiter_custom():
    """Create a FuelRateLimiter with custom configuration."""
    return FuelRateLimiter(
        startup_min_rate=5.0,
        startup_max_rate=25.0,
        cruise_max_rate=100.0,
        max_rate_change=50.0,
    )


# ============================================================================
# STARTUP MODE TESTS
# ============================================================================


class TestStartupMode:
    """Tests for startup mode behavior."""

    def test_startup_below_minimum(self, limiter):
        """Requested rate below startup minimum should be clamped to minimum."""
        actual, was_clamped, reason = limiter.limit_rate(
            requested_rate=5.0,
            operational_mode=OperationalMode.STARTUP,
            elapsed_time_ms=100,
        )
        assert actual == 10.0
        assert was_clamped is True
        assert reason == ClampingReason.MODE_MIN

    def test_startup_at_minimum(self, limiter):
        """Requested rate at startup minimum should pass through."""
        actual, was_clamped, reason = limiter.limit_rate(
            requested_rate=10.0,
            operational_mode=OperationalMode.STARTUP,
            elapsed_time_ms=100,
        )
        assert actual == 10.0
        assert was_clamped is False
        assert reason == ClampingReason.NONE

    def test_startup_within_range(self, limiter):
        """Requested rate within startup range should pass through."""
        actual, was_clamped, reason = limiter.limit_rate(
            requested_rate=30.0,
            operational_mode=OperationalMode.STARTUP,
            elapsed_time_ms=100,
        )
        assert actual == 30.0
        assert was_clamped is False
        assert reason == ClampingReason.NONE

    def test_startup_at_maximum(self, limiter):
        """Requested rate at startup maximum should pass through."""
        actual, was_clamped, reason = limiter.limit_rate(
            requested_rate=50.0,
            operational_mode=OperationalMode.STARTUP,
            elapsed_time_ms=100,
        )
        assert actual == 50.0
        assert was_clamped is False
        assert reason == ClampingReason.NONE

    def test_startup_above_maximum(self, limiter):
        """Requested rate above startup maximum should be clamped."""
        actual, was_clamped, reason = limiter.limit_rate(
            requested_rate=100.0,
            operational_mode=OperationalMode.STARTUP,
            elapsed_time_ms=100,
        )
        assert actual == 50.0
        assert was_clamped is True
        assert reason == ClampingReason.MODE_MAX

    def test_startup_zero_requested(self, limiter):
        """Zero requested rate in startup should clamp to minimum."""
        actual, was_clamped, reason = limiter.limit_rate(
            requested_rate=0.0,
            operational_mode=OperationalMode.STARTUP,
            elapsed_time_ms=100,
        )
        assert actual == 10.0
        assert was_clamped is True
        assert reason == ClampingReason.MODE_MIN


# ============================================================================
# CRUISE MODE TESTS
# ============================================================================


class TestCruiseMode:
    """Tests for cruise mode behavior."""

    def test_cruise_below_maximum(self, limiter):
        """Requested rate below cruise maximum should pass through."""
        actual, was_clamped, reason = limiter.limit_rate(
            requested_rate=100.0,
            operational_mode=OperationalMode.CRUISE,
            elapsed_time_ms=100,
        )
        assert actual == 100.0
        assert was_clamped is False
        assert reason == ClampingReason.NONE

    def test_cruise_at_maximum(self, limiter):
        """Requested rate at cruise maximum should pass through."""
        actual, was_clamped, reason = limiter.limit_rate(
            requested_rate=200.0,
            operational_mode=OperationalMode.CRUISE,
            elapsed_time_ms=100,
        )
        assert actual == 200.0
        assert was_clamped is False
        assert reason == ClampingReason.NONE

    def test_cruise_above_maximum(self, limiter):
        """Requested rate above cruise maximum should be clamped."""
        actual, was_clamped, reason = limiter.limit_rate(
            requested_rate=250.0,
            operational_mode=OperationalMode.CRUISE,
            elapsed_time_ms=100,
        )
        assert actual == 200.0
        assert was_clamped is True
        assert reason == ClampingReason.MODE_MAX

    def test_cruise_zero_rate(self, limiter):
        """Zero requested rate in cruise should be allowed."""
        actual, was_clamped, reason = limiter.limit_rate(
            requested_rate=0.0,
            operational_mode=OperationalMode.CRUISE,
            elapsed_time_ms=100,
        )
        assert actual == 0.0
        assert was_clamped is False
        assert reason == ClampingReason.NONE

    def test_cruise_no_previous_rate_history(self, limiter):
        """First call in cruise mode should initialize previous_rate."""
        # Previous rate starts at 0.0
        actual, was_clamped, reason = limiter.limit_rate(
            requested_rate=50.0,
            operational_mode=OperationalMode.CRUISE,
            elapsed_time_ms=100,
        )
        assert actual == 50.0
        # No clamping on first call even though it jumps from 0 to 50
        # because rate-of-change check is based on max change in 100ms
        # max_change = 100.0 * (100 / 1000.0) = 10.0
        # But the first call should clamp if it jumps too much
        assert was_clamped is True
        assert reason == ClampingReason.RATE_OF_CHANGE


# ============================================================================
# RATE OF CHANGE LIMITING TESTS
# ============================================================================


class TestRateOfChange:
    """Tests for rate-of-change limiting in cruise mode."""

    def test_roc_increase_within_limit(self, limiter):
        """Rate increase within allowed change should pass through."""
        # First call: set previous_rate to 50.0
        limiter.limit_rate(50.0, OperationalMode.CRUISE, 1000)
        # Second call: increase by 10.0 liters/hour over 100ms
        # max_change = 100.0 * (100 / 1000.0) = 10.0
        actual, was_clamped, reason = limiter.limit_rate(
            requested_rate=60.0, operational_mode=OperationalMode.CRUISE, elapsed_time_ms=100
        )
        assert actual == 60.0
        assert was_clamped is False
        assert reason == ClampingReason.NONE

    def test_roc_increase_exceeds_limit(self, limiter):
        """Rate increase exceeding allowed change should be clamped."""
        # First call: set previous_rate to 50.0
        limiter.limit_rate(50.0, OperationalMode.CRUISE, 1000)
        # Second call: request increase of 20.0 over 100ms
        # max_change = 100.0 * (100 / 1000.0) = 10.0
        actual, was_clamped, reason = limiter.limit_rate(
            requested_rate=70.0, operational_mode=OperationalMode.CRUISE, elapsed_time_ms=100
        )
        assert actual == 60.0  # 50.0 + 10.0
        assert was_clamped is True
        assert reason == ClampingReason.RATE_OF_CHANGE

    def test_roc_decrease_within_limit(self, limiter):
        """Rate decrease within allowed change should pass through."""
        # First call: set previous_rate to 50.0
        limiter.limit_rate(50.0, OperationalMode.CRUISE, 1000)
        # Second call: decrease by 10.0 over 100ms
        # max_change = 100.0 * (100 / 1000.0) = 10.0
        actual, was_clamped, reason = limiter.limit_rate(
            requested_rate=40.0, operational_mode=OperationalMode.CRUISE, elapsed_time_ms=100
        )
        assert actual == 40.0
        assert was_clamped is False
        assert reason == ClampingReason.NONE

    def test_roc_decrease_exceeds_limit(self, limiter):
        """Rate decrease exceeding allowed change should be clamped."""
        # First call: set previous_rate to 50.0
        limiter.limit_rate(50.0, OperationalMode.CRUISE, 1000)
        # Second call: request decrease of 20.0 over 100ms
        # max_change = 100.0 * (100 / 1000.0) = 10.0
        actual, was_clamped, reason = limiter.limit_rate(
            requested_rate=30.0, operational_mode=OperationalMode.CRUISE, elapsed_time_ms=100
        )
        assert actual == 40.0  # 50.0 - 10.0
        assert was_clamped is True
        assert reason == ClampingReason.RATE_OF_CHANGE

    def test_roc_longer_time_interval(self, limiter):
        """Longer elapsed time should allow larger rate changes."""
        # First call: set previous_rate to 0.0
        limiter.limit_rate(0.0, OperationalMode.CRUISE, 1000)
        # Second call: 1 second = 1000ms elapsed
        # max_change = 100.0 * (1000 / 1000.0) = 100.0
        actual, was_clamped, reason = limiter.limit_rate(
            requested_rate=100.0, operational_mode=OperationalMode.CRUISE, elapsed_time_ms=1000
        )
        assert actual == 100.0
        assert was_clamped is False
        assert reason == ClampingReason.NONE

    def test_roc_zero_elapsed_time(self, limiter):
        """Zero elapsed time should not allow any rate change."""
        # First call: set previous_rate to 50.0
        limiter.limit_rate(50.0, OperationalMode.CRUISE, 1000)
        # Second call: zero elapsed time
        # max_change = 100.0 * (0 / 1000.0) = 0.0
        actual, was_clamped, reason = limiter.limit_rate(
            requested_rate=60.0, operational_mode=OperationalMode.CRUISE, elapsed_time_ms=0
        )
        assert actual == 50.0  # No change allowed
        assert was_clamped is True
        assert reason == ClampingReason.RATE_OF_CHANGE

    def test_roc_small_time_interval(self, limiter):
        """Small elapsed time should limit change proportionally."""
        # First call: set previous_rate to 0.0
        limiter.limit_rate(0.0, OperationalMode.CRUISE, 1000)
        # Second call: 10ms elapsed
        # max_change = 100.0 * (10 / 1000.0) = 1.0
        actual, was_clamped, reason = limiter.limit_rate(
            requested_rate=10.0, operational_mode=OperationalMode.CRUISE, elapsed_time_ms=10
        )
        assert actual == 1.0
        assert was_clamped is True
        assert reason == ClampingReason.RATE_OF_CHANGE

    def test_roc_combined_with_mode_max(self, limiter):
        """Mode maximum should take precedence if hit before rate-of-change."""
        # First call: set previous_rate to 100.0
        limiter.limit_rate(100.0, OperationalMode.CRUISE, 1000)
        # Second call: request 300.0 (above both mode max and roc)
        # mode max = 200.0 (applied first)
        # After mode max: 200.0
        # ROC check: max_change = 100.0 * (100 / 1000.0) = 10.0
        # 200.0 - 100.0 = 100.0 > 10.0, so apply ROC limit
        actual, was_clamped, reason = limiter.limit_rate(
            requested_rate=300.0, operational_mode=OperationalMode.CRUISE, elapsed_time_ms=100
        )
        assert actual == 110.0  # 100.0 + 10.0
        assert was_clamped is True
        assert reason == ClampingReason.RATE_OF_CHANGE


# ============================================================================
# EMERGENCY SHUTDOWN TESTS
# ============================================================================


class TestEmergencyShutdown:
    """Tests for emergency shutdown mode."""

    def test_emergency_any_requested_rate(self, limiter):
        """Emergency shutdown always returns 0.0 regardless of requested rate."""
        actual, was_clamped, reason = limiter.limit_rate(
            requested_rate=150.0,
            operational_mode=OperationalMode.EMERGENCY_SHUTDOWN,
            elapsed_time_ms=100,
        )
        assert actual == 0.0
        assert was_clamped is True
        assert reason == ClampingReason.EMERGENCY

    def test_emergency_zero_requested(self, limiter):
        """Emergency shutdown with zero requested should return 0.0 with clamping info."""
        actual, was_clamped, reason = limiter.limit_rate(
            requested_rate=0.0,
            operational_mode=OperationalMode.EMERGENCY_SHUTDOWN,
            elapsed_time_ms=100,
        )
        assert actual == 0.0
        assert was_clamped is False
        assert reason == ClampingReason.NONE

    def test_emergency_resets_state(self, limiter):
        """Emergency shutdown should update previous_rate for subsequent calls."""
        # First call: set rate to 100 in cruise
        limiter.limit_rate(100.0, OperationalMode.CRUISE, 1000)
        # Second call: emergency shutdown
        limiter.limit_rate(100.0, OperationalMode.EMERGENCY_SHUTDOWN, 100)
        # Third call: back to cruise from 0.0
        actual, was_clamped, reason = limiter.limit_rate(
            requested_rate=50.0, operational_mode=OperationalMode.CRUISE, elapsed_time_ms=1000
        )
        assert actual == 50.0
        assert was_clamped is False


# ============================================================================
# ERROR HANDLING TESTS
# ============================================================================


class TestErrorHandling:
    """Tests for error handling and edge cases."""

    def test_negative_requested_rate(self, limiter):
        """Negative requested rate should be treated as 0.0 with mode_min reason."""
        actual, was_clamped, reason = limiter.limit_rate(
            requested_rate=-10.0,
            operational_mode=OperationalMode.CRUISE,
            elapsed_time_ms=100,
        )
        assert actual == 0.0
        assert was_clamped is True
        assert reason == ClampingReason.MODE_MIN

    def test_negative_requested_rate_startup(self, limiter):
        """Negative requested rate in startup should be treated as mode_min."""
        actual, was_clamped, reason = limiter.limit_rate(
            requested_rate=-5.0,
            operational_mode=OperationalMode.STARTUP,
            elapsed_time_ms=100,
        )
        assert actual == 0.0
        assert was_clamped is True
        assert reason == ClampingReason.MODE_MIN

    def test_negative_elapsed_time_startup(self, limiter):
        """Negative elapsed time should be treated as 0, no rate-of-change limiting."""
        limiter.limit_rate(50.0, OperationalMode.STARTUP, 1000)
        actual, was_clamped, reason = limiter.limit_rate(
            requested_rate=60.0,
            operational_mode=OperationalMode.STARTUP,
            elapsed_time_ms=-100,
        )
        # Startup mode ignores elapsed_time, so this should just clamp to max
        assert actual == 50.0
        assert was_clamped is True
        assert reason == ClampingReason.MODE_MAX

    def test_negative_elapsed_time_cruise(self, limiter):
        """Negative elapsed time in cruise should be treated as 0."""
        limiter.limit_rate(50.0, OperationalMode.CRUISE, 1000)
        actual, was_clamped, reason = limiter.limit_rate(
            requested_rate=60.0,
            operational_mode=OperationalMode.CRUISE,
            elapsed_time_ms=-100,
        )
        # With elapsed_time treated as 0, max_change = 0.0
        assert actual == 50.0
        assert was_clamped is True
        assert reason == ClampingReason.RATE_OF_CHANGE

    def test_invalid_operational_mode(self, limiter):
        """Invalid operational mode should default to emergency_shutdown (fail-safe)."""
        # Create an invalid enum value
        actual, was_clamped, reason = limiter.limit_rate(
            requested_rate=100.0,
            operational_mode="invalid_mode",  # Invalid mode
            elapsed_time_ms=100,
        )
        assert actual == 0.0
        assert was_clamped is True
        assert reason == ClampingReason.EMERGENCY

    def test_invalid_mode_with_none(self, limiter):
        """None as operational mode should default to emergency_shutdown."""
        actual, was_clamped, reason = limiter.limit_rate(
            requested_rate=100.0,
            operational_mode=None,
            elapsed_time_ms=100,
        )
        assert actual == 0.0
        assert was_clamped is True
        assert reason == ClampingReason.EMERGENCY


# ============================================================================
# CLAMPING FLAG CONSISTENCY TESTS
# ============================================================================


class TestClampingFlag:
    """Tests to verify was_clamped flag accurately reflects clamping."""

    def test_was_clamped_consistency_when_changed(self, limiter):
        """was_clamped should be true only when actual_rate != requested_rate."""
        # Test various cases where clamping occurs
        test_cases = [
            (OperationalMode.STARTUP, 5.0, 100),  # Below startup min
            (OperationalMode.STARTUP, 100.0, 100),  # Above startup max
            (OperationalMode.CRUISE, 300.0, 100),  # Above cruise max
        ]
        for mode, requested, elapsed in test_cases:
            actual, was_clamped, _ = limiter.limit_rate(requested, mode, elapsed)
            if actual == requested:
                assert was_clamped is False
            else:
                assert was_clamped is True

    def test_was_clamped_consistency_when_unchanged(self, limiter):
        """was_clamped should be false when actual_rate == requested_rate."""
        test_cases = [
            (OperationalMode.STARTUP, 30.0, 100),
            (OperationalMode.CRUISE, 100.0, 100),
        ]
        for mode, requested, elapsed in test_cases:
            actual, was_clamped, reason = limiter.limit_rate(requested, mode, elapsed)
            assert actual == requested
            assert was_clamped is False
            assert reason == ClampingReason.NONE


# ============================================================================
# STATE AND SEQUENCING TESTS
# ============================================================================


class TestStateAndSequencing:
    """Tests for state tracking and sequential behavior."""

    def test_previous_rate_updates(self, limiter):
        """previous_rate should update after each call."""
        # First call
        limiter.limit_rate(50.0, OperationalMode.CRUISE, 1000)
        assert limiter.previous_rate == 50.0
        # Second call with clamping
        limiter.limit_rate(70.0, OperationalMode.CRUISE, 100)
        # Should clamp to 60.0 due to ROC
        assert limiter.previous_rate == 60.0
        # Third call should be relative to 60.0
        actual, _, _ = limiter.limit_rate(70.0, OperationalMode.CRUISE, 100)
        assert actual == 60.0  # 60.0 + 10.0

    def test_mode_transition_startup_to_cruise(self, limiter):
        """Transition from startup to cruise should work correctly."""
        # Startup: establish high rate
        limiter.limit_rate(50.0, OperationalMode.STARTUP, 1000)
        # Transition to cruise with ROC limit
        actual, was_clamped, reason = limiter.limit_rate(
            requested_rate=100.0, operational_mode=OperationalMode.CRUISE, elapsed_time_ms=100
        )
        # max_change = 100.0 * (100 / 1000.0) = 10.0
        # 100.0 - 50.0 = 50.0 > 10.0
        assert actual == 60.0
        assert was_clamped is True
        assert reason == ClampingReason.RATE_OF_CHANGE

    def test_mode_transition_cruise_to_emergency(self, limiter):
        """Transition from cruise to emergency should immediately return 0.0."""
        limiter.limit_rate(100.0, OperationalMode.CRUISE, 1000)
        actual, was_clamped, reason = limiter.limit_rate(
            requested_rate=100.0,
            operational_mode=OperationalMode.EMERGENCY_SHUTDOWN,
            elapsed_time_ms=100,
        )
        assert actual == 0.0
        assert was_clamped is True
        assert reason == ClampingReason.EMERGENCY

    def test_recovery_after_emergency(self, limiter):
        """Recovery from emergency shutdown should respect ROC limits."""
        # Go to emergency
        limiter.limit_rate(50.0, OperationalMode.CRUISE, 1000)
        limiter.limit_rate(0.0, OperationalMode.EMERGENCY_SHUTDOWN, 100)
        # Try to recover to cruise
        actual, was_clamped, reason = limiter.limit_rate(
            requested_rate=100.0, operational_mode=OperationalMode.CRUISE, elapsed_time_ms=1000
        )
        # max_change = 100.0 * (1000 / 1000.0) = 100.0
        assert actual == 100.0
        assert was_clamped is False


# ============================================================================
# BOUNDARY AND NUMERICAL TESTS
# ============================================================================


class TestBoundaryValues:
    """Tests for boundary and edge numerical values."""

    def test_very_large_requested_rate(self, limiter):
        """Very large requested rate should be clamped appropriately."""
        actual, was_clamped, reason = limiter.limit_rate(
            requested_rate=1000.0,
            operational_mode=OperationalMode.CRUISE,
            elapsed_time_ms=100,
        )
        assert actual == 200.0  # Cruise max
        assert was_clamped is True
        assert reason == ClampingReason.MODE_MAX

    def test_very_large_elapsed_time(self, limiter):
        """Very large elapsed time should allow large rate changes."""
        limiter.limit_rate(0.0, OperationalMode.CRUISE, 1000)
        # 10 seconds = 10000ms
        # max_change = 100.0 * (10000 / 1000.0) = 1000.0
        actual, was_clamped, reason = limiter.limit_rate(
            requested_rate=500.0, operational_mode=OperationalMode.CRUISE, elapsed_time_ms=10000
        )
        assert actual == 200.0  # Still clamped by cruise max
        assert was_clamped is True
        assert reason == ClampingReason.MODE_MAX

    def test_floating_point_precision(self, limiter):
        """Floating point rates should be handled correctly."""
        actual, was_clamped, reason = limiter.limit_rate(
            requested_rate=50.5,
            operational_mode=OperationalMode.STARTUP,
            elapsed_time_ms=100,
        )
        assert actual == 50.0
        assert was_clamped is True
        assert reason == ClampingReason.MODE_MAX

    def test_fractional_elapsed_time_effect(self, limiter):
        """Fractional elapsed time should be handled correctly."""
        limiter.limit_rate(0.0, OperationalMode.CRUISE, 1000)
        # 500ms = 0.5 seconds
        # max_change = 100.0 * (500 / 1000.0) = 50.0
        actual, was_clamped, reason = limiter.limit_rate(
            requested_rate=60.0, operational_mode=OperationalMode.CRUISE, elapsed_time_ms=500
        )
        assert actual == 50.0
        assert was_clamped is True
        assert reason == ClampingReason.RATE_OF_CHANGE


# ============================================================================
# CONFIGURATION TESTS
# ============================================================================


class TestConfiguration:
    """Tests for custom configuration parameters."""

    def test_custom_startup_min(self, limiter_custom):
        """Custom startup minimum should be respected."""
        actual, was_clamped, reason = limiter_custom.limit_rate(
            requested_rate=3.0,
            operational_mode=OperationalMode.STARTUP,
            elapsed_time_ms=100,
        )
        assert actual == 5.0  # Custom min
        assert was_clamped is True
        assert reason == ClampingReason.MODE_MIN

    def test_custom_startup_max(self, limiter_custom):
        """Custom startup maximum should be respected."""
        actual, was_clamped, reason = limiter_custom.limit_rate(
            requested_rate=50.0,
            operational_mode=OperationalMode.STARTUP,
            elapsed_time_ms=100,
        )
        assert actual == 25.0  # Custom max
        assert was_clamped is True
        assert reason == ClampingReason.MODE_MAX

    def test_custom_cruise_max(self, limiter_custom):
        """Custom cruise maximum should be respected."""
        actual, was_clamped, reason = limiter_custom.limit_rate(
            requested_rate=150.0,
            operational_mode=OperationalMode.CRUISE,
            elapsed_time_ms=100,
        )
        assert actual == 100.0  # Custom cruise max
        assert was_clamped is True
        assert reason == ClampingReason.MODE_MAX

    def test_custom_max_rate_change(self, limiter_custom):
        """Custom max rate change should affect ROC limiting."""
        limiter_custom.limit_rate(0.0, OperationalMode.CRUISE, 1000)
        # max_rate_change = 50.0 (custom)
        # max_change = 50.0 * (100 / 1000.0) = 5.0
        actual, was_clamped, reason = limiter_custom.limit_rate(
            requested_rate=10.0, operational_mode=OperationalMode.CRUISE, elapsed_time_ms=100
        )
        assert actual == 5.0
        assert was_clamped is True
        assert reason == ClampingReason.RATE_OF_CHANGE


# ============================================================================
# DETERMINISM TESTS
# ============================================================================


class TestDeterminism:
    """Tests for deterministic behavior (same inputs -> same outputs)."""

    def test_deterministic_startup_clamping(self, limiter):
        """Same startup inputs should always produce same outputs."""
        results = []
        for _ in range(3):
            actual, was_clamped, reason = limiter.limit_rate(
                requested_rate=75.0,
                operational_mode=OperationalMode.STARTUP,
                elapsed_time_ms=100,
            )
            results.append((actual, was_clamped, reason))
            # Reset limiter for next iteration (except previous_rate for this test)
            limiter.previous_rate = 0.0

        assert len(set(results)) == 1  # All results identical

    def test_deterministic_sequence(self, limiter):
        """A sequence of calls should produce deterministic results."""
        sequence = [
            (100.0, OperationalMode.CRUISE, 1000),
            (150.0, OperationalMode.CRUISE, 100),
            (80.0, OperationalMode.CRUISE, 200),
        ]
        results1 = []
        for requested, mode, elapsed in sequence:
            actual, was_clamped, reason = limiter.limit_rate(requested, mode, elapsed)
            results1.append((actual, was_clamped, reason))

        limiter2 = FuelRateLimiter()
        results2 = []
        for requested, mode, elapsed in sequence:
            actual, was_clamped, reason = limiter2.limit_rate(requested, mode, elapsed)
            results2.append((actual, was_clamped, reason))

        assert results1 == results2
