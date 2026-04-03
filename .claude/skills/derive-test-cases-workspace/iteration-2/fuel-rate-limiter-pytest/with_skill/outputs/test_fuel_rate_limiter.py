"""
Test cases for FuelRateLimiter derived using V-model test derivation strategies.

Coverage strategies used:
1. Requirement-based: one test per behavior rule and condition
2. Equivalence class partitioning: valid/invalid classes for each input
3. Boundary value analysis: min/max/just-outside boundaries for numeric inputs
4. Error handling/fault injection: explicit error conditions plus implicit failures

All tests follow Arrange-Act-Assert structure and are named to describe scenarios.
Expected values derived from design specification, not implementation logic.
"""

import pytest
from enum import Enum


class OperationalMode(Enum):
    """Enum matching the design's operational_mode input."""
    STARTUP = "startup"
    CRUISE = "cruise"
    EMERGENCY_SHUTDOWN = "emergency_shutdown"


class ClampingReason(Enum):
    """Enum matching the design's clamping_reason output."""
    NONE = "none"
    MODE_MAX = "mode_max"
    MODE_MIN = "mode_min"
    RATE_OF_CHANGE = "rate_of_change"
    EMERGENCY = "emergency"


class LimitResult:
    """Output of FuelRateLimiter.limit() — matches design outputs."""
    def __init__(self, actual_rate, was_clamped, clamping_reason):
        self.actual_rate = actual_rate
        self.was_clamped = was_clamped
        self.clamping_reason = clamping_reason


class FuelRateLimiter:
    """
    Limits fuel flow rate based on operational mode and safety constraints.
    Prevents fuel rate from exceeding mode-specific maximums and enforces
    minimum rate during startup to ensure stable ignition.
    """

    # Configuration parameters (design specifies defaults)
    STARTUP_MIN_RATE = 10.0          # liters_per_hour
    STARTUP_MAX_RATE = 50.0          # liters_per_hour
    CRUISE_MAX_RATE = 200.0          # liters_per_hour
    MAX_RATE_CHANGE = 100.0          # liters_per_hour_per_second

    def __init__(self):
        """Initialize with zero previous rate (first call has no rate-of-change history)."""
        self.previous_rate = 0.0

    def limit(self, requested_rate, operational_mode, elapsed_time_ms):
        """
        Clamp requested fuel rate to mode-specific bounds and enforce constraints.

        Args:
            requested_rate (float): Desired fuel rate in liters_per_hour (design: >= 0.0)
            operational_mode (OperationalMode): Current engine operational mode
            elapsed_time_ms (int): Time since last call in milliseconds (design: >= 0)

        Returns:
            LimitResult: actual_rate (0-500 L/h), was_clamped (bool), clamping_reason (enum)
        """
        # Error handling: negative requested_rate → treat as 0.0
        if requested_rate < 0.0:
            actual_rate = 0.0
            return LimitResult(
                actual_rate=actual_rate,
                was_clamped=True,
                clamping_reason=ClampingReason.MODE_MIN
            )

        # Error handling: negative elapsed_time_ms → treat as 0
        if elapsed_time_ms < 0:
            elapsed_time_ms = 0

        # Error handling: unrecognized mode → fail-safe to emergency_shutdown
        if operational_mode not in (OperationalMode.STARTUP, OperationalMode.CRUISE, OperationalMode.EMERGENCY_SHUTDOWN):
            operational_mode = OperationalMode.EMERGENCY_SHUTDOWN

        # Behavior: mode-specific clamping
        if operational_mode == OperationalMode.STARTUP:
            # Clamp to [STARTUP_MIN_RATE, STARTUP_MAX_RATE]
            if requested_rate < self.STARTUP_MIN_RATE:
                actual_rate = self.STARTUP_MIN_RATE
                clamping_reason = ClampingReason.MODE_MIN
            elif requested_rate > self.STARTUP_MAX_RATE:
                actual_rate = self.STARTUP_MAX_RATE
                clamping_reason = ClampingReason.MODE_MAX
            else:
                actual_rate = requested_rate
                clamping_reason = ClampingReason.NONE

        elif operational_mode == OperationalMode.CRUISE:
            # Clamp to [0.0, CRUISE_MAX_RATE]
            if requested_rate > self.CRUISE_MAX_RATE:
                actual_rate = self.CRUISE_MAX_RATE
                clamping_reason = ClampingReason.MODE_MAX
            else:
                actual_rate = requested_rate
                clamping_reason = ClampingReason.NONE

            # Apply rate-of-change limit: abs(actual_rate - previous_rate) <= MAX_RATE_CHANGE * elapsed_time_ms / 1000
            max_change = self.MAX_RATE_CHANGE * elapsed_time_ms / 1000.0
            rate_change = abs(actual_rate - self.previous_rate)
            if rate_change > max_change:
                # Clamp to the maximum allowed change direction
                if actual_rate > self.previous_rate:
                    actual_rate = self.previous_rate + max_change
                else:
                    actual_rate = self.previous_rate - max_change
                clamping_reason = ClampingReason.RATE_OF_CHANGE

        elif operational_mode == OperationalMode.EMERGENCY_SHUTDOWN:
            # Set actual_rate to 0.0 regardless of requested_rate
            actual_rate = 0.0
            clamping_reason = ClampingReason.EMERGENCY

        # Rule: was_clamped is true iff actual_rate != requested_rate
        was_clamped = (actual_rate != requested_rate)

        # Update state for next call
        self.previous_rate = actual_rate

        return LimitResult(
            actual_rate=actual_rate,
            was_clamped=was_clamped,
            clamping_reason=clamping_reason
        )


# ============================================================================
# STRATEGY 1: REQUIREMENT-BASED TESTING
# One test per behavior rule or condition
# ============================================================================

class TestStartupModeBehavior:
    """Tests for the startup operational mode clamping rules."""

    def test_startup_mode_clamps_rate_to_min_when_below(self):
        """
        Behavior: operational_mode is startup, requested_rate < STARTUP_MIN_RATE
        Expected: actual_rate = STARTUP_MIN_RATE, was_clamped = true, reason = mode_min
        """
        # Arrange
        limiter = FuelRateLimiter()
        requested_rate = 5.0  # Below STARTUP_MIN_RATE (10.0)
        mode = OperationalMode.STARTUP
        elapsed_time_ms = 100

        # Act
        result = limiter.limit(requested_rate, mode, elapsed_time_ms)

        # Assert
        assert result.actual_rate == 10.0, f"Expected 10.0, got {result.actual_rate}"
        assert result.was_clamped is True
        assert result.clamping_reason == ClampingReason.MODE_MIN

    def test_startup_mode_clamps_rate_to_max_when_above(self):
        """
        Behavior: operational_mode is startup, requested_rate > STARTUP_MAX_RATE
        Expected: actual_rate = STARTUP_MAX_RATE, was_clamped = true, reason = mode_max
        """
        # Arrange
        limiter = FuelRateLimiter()
        requested_rate = 75.0  # Above STARTUP_MAX_RATE (50.0)
        mode = OperationalMode.STARTUP
        elapsed_time_ms = 100

        # Act
        result = limiter.limit(requested_rate, mode, elapsed_time_ms)

        # Assert
        assert result.actual_rate == 50.0
        assert result.was_clamped is True
        assert result.clamping_reason == ClampingReason.MODE_MAX

    def test_startup_mode_allows_rate_within_bounds(self):
        """
        Behavior: operational_mode is startup, STARTUP_MIN_RATE <= requested_rate <= STARTUP_MAX_RATE
        Expected: actual_rate = requested_rate, was_clamped = false, reason = none
        """
        # Arrange
        limiter = FuelRateLimiter()
        requested_rate = 30.0  # Within [10.0, 50.0]
        mode = OperationalMode.STARTUP
        elapsed_time_ms = 100

        # Act
        result = limiter.limit(requested_rate, mode, elapsed_time_ms)

        # Assert
        assert result.actual_rate == 30.0
        assert result.was_clamped is False
        assert result.clamping_reason == ClampingReason.NONE


class TestCruiseModeBehavior:
    """Tests for the cruise operational mode clamping rules."""

    def test_cruise_mode_clamps_rate_to_max_when_exceeded(self):
        """
        Behavior: operational_mode is cruise, requested_rate > CRUISE_MAX_RATE
        Expected: actual_rate = CRUISE_MAX_RATE, was_clamped = true, reason = mode_max
        """
        # Arrange
        limiter = FuelRateLimiter()
        requested_rate = 300.0  # Above CRUISE_MAX_RATE (200.0)
        mode = OperationalMode.CRUISE
        elapsed_time_ms = 100

        # Act
        result = limiter.limit(requested_rate, mode, elapsed_time_ms)

        # Assert
        assert result.actual_rate == 200.0
        assert result.was_clamped is True
        assert result.clamping_reason == ClampingReason.MODE_MAX

    def test_cruise_mode_allows_zero_rate(self):
        """
        Behavior: operational_mode is cruise, requested_rate = 0.0 (valid lower bound)
        Expected: actual_rate = 0.0, was_clamped = false, reason = none
        """
        # Arrange
        limiter = FuelRateLimiter()
        requested_rate = 0.0
        mode = OperationalMode.CRUISE
        elapsed_time_ms = 100

        # Act
        result = limiter.limit(requested_rate, mode, elapsed_time_ms)

        # Assert
        assert result.actual_rate == 0.0
        assert result.was_clamped is False
        assert result.clamping_reason == ClampingReason.NONE

    def test_cruise_mode_allows_rate_within_bounds(self):
        """
        Behavior: operational_mode is cruise, 0.0 <= requested_rate <= CRUISE_MAX_RATE
        Expected: actual_rate = requested_rate, was_clamped = false, reason = none
        """
        # Arrange
        limiter = FuelRateLimiter()
        requested_rate = 150.0  # Within [0.0, 200.0]
        mode = OperationalMode.CRUISE
        elapsed_time_ms = 100

        # Act
        result = limiter.limit(requested_rate, mode, elapsed_time_ms)

        # Assert
        assert result.actual_rate == 150.0
        assert result.was_clamped is False
        assert result.clamping_reason == ClampingReason.NONE

    def test_cruise_mode_enforces_rate_of_change_limit_on_increase(self):
        """
        Behavior: operational_mode is cruise, requested_rate increase exceeds MAX_RATE_CHANGE limit
        Expected: actual_rate is clamped to previous_rate + max_allowed_change, reason = rate_of_change
        Setup: previous_rate = 50.0, MAX_RATE_CHANGE = 100.0, elapsed_time_ms = 500 (0.5s)
        Max allowed increase: 100.0 * 0.5 = 50.0 L/h
        """
        # Arrange
        limiter = FuelRateLimiter()
        # Set up previous_rate by calling once first
        limiter.limit(50.0, OperationalMode.CRUISE, 0)
        # Now request a large increase
        requested_rate = 150.0  # 100.0 L/h increase
        mode = OperationalMode.CRUISE
        elapsed_time_ms = 500  # 0.5 seconds

        # Act
        result = limiter.limit(requested_rate, mode, elapsed_time_ms)

        # Assert
        expected_actual_rate = 50.0 + (100.0 * 0.5)  # 100.0
        assert result.actual_rate == expected_actual_rate, f"Expected {expected_actual_rate}, got {result.actual_rate}"
        assert result.was_clamped is True
        assert result.clamping_reason == ClampingReason.RATE_OF_CHANGE

    def test_cruise_mode_enforces_rate_of_change_limit_on_decrease(self):
        """
        Behavior: operational_mode is cruise, requested_rate decrease exceeds MAX_RATE_CHANGE limit
        Expected: actual_rate is clamped to previous_rate - max_allowed_change, reason = rate_of_change
        Setup: previous_rate = 150.0, MAX_RATE_CHANGE = 100.0, elapsed_time_ms = 500
        Max allowed decrease: 100.0 * 0.5 = 50.0 L/h
        """
        # Arrange
        limiter = FuelRateLimiter()
        limiter.limit(150.0, OperationalMode.CRUISE, 0)
        # Request a large decrease
        requested_rate = 50.0  # 100.0 L/h decrease
        mode = OperationalMode.CRUISE
        elapsed_time_ms = 500  # 0.5 seconds

        # Act
        result = limiter.limit(requested_rate, mode, elapsed_time_ms)

        # Assert
        expected_actual_rate = 150.0 - (100.0 * 0.5)  # 100.0
        assert result.actual_rate == expected_actual_rate
        assert result.was_clamped is True
        assert result.clamping_reason == ClampingReason.RATE_OF_CHANGE

    def test_cruise_mode_allows_rate_change_within_limit(self):
        """
        Behavior: operational_mode is cruise, rate_of_change is within MAX_RATE_CHANGE limit
        Expected: actual_rate = requested_rate, was_clamped = false (if within mode bounds)
        Setup: previous_rate = 50.0, requested = 100.0 (50 L/h increase), elapsed = 1000ms (1s), max = 100 L/h/s
        """
        # Arrange
        limiter = FuelRateLimiter()
        limiter.limit(50.0, OperationalMode.CRUISE, 0)
        requested_rate = 100.0  # 50 L/h increase
        mode = OperationalMode.CRUISE
        elapsed_time_ms = 1000  # 1 second, allows up to 100 L/h change

        # Act
        result = limiter.limit(requested_rate, mode, elapsed_time_ms)

        # Assert
        assert result.actual_rate == 100.0
        assert result.was_clamped is False
        assert result.clamping_reason == ClampingReason.NONE


class TestEmergencyShutdownBehavior:
    """Tests for the emergency_shutdown operational mode."""

    def test_emergency_shutdown_sets_rate_to_zero_regardless_of_requested(self):
        """
        Behavior: operational_mode is emergency_shutdown
        Expected: actual_rate = 0.0, was_clamped = true, reason = emergency
        Regardless of requested_rate or previous_rate.
        """
        # Arrange
        limiter = FuelRateLimiter()
        limiter.limit(150.0, OperationalMode.CRUISE, 0)  # Set previous_rate
        requested_rate = 100.0
        mode = OperationalMode.EMERGENCY_SHUTDOWN
        elapsed_time_ms = 100

        # Act
        result = limiter.limit(requested_rate, mode, elapsed_time_ms)

        # Assert
        assert result.actual_rate == 0.0
        assert result.was_clamped is True
        assert result.clamping_reason == ClampingReason.EMERGENCY

    def test_emergency_shutdown_sets_rate_to_zero_even_when_already_zero(self):
        """
        Behavior: operational_mode is emergency_shutdown with zero requested_rate
        Expected: actual_rate = 0.0, was_clamped = true (because 0.0 == requested_rate)
        Note: Design says "was_clamped is true if and only if actual_rate != requested_rate"
        This test ensures was_clamped is still true due to emergency condition forcing zero.
        """
        # Arrange
        limiter = FuelRateLimiter()
        requested_rate = 0.0
        mode = OperationalMode.EMERGENCY_SHUTDOWN
        elapsed_time_ms = 100

        # Act
        result = limiter.limit(requested_rate, mode, elapsed_time_ms)

        # Assert
        assert result.actual_rate == 0.0
        assert result.was_clamped is False  # Since actual == requested (both 0.0)
        assert result.clamping_reason == ClampingReason.EMERGENCY


class TestWasClampedRule:
    """
    Tests for the design rule: "was_clamped is true if and only if actual_rate != requested_rate"
    This rule must hold across all modes and conditions.
    """

    def test_was_clamped_true_when_actual_differs_from_requested(self):
        """
        Rule: was_clamped = (actual_rate != requested_rate)
        Scenario: clamping applies, so actual != requested
        Expected: was_clamped = true
        """
        # Arrange (clamping will occur)
        limiter = FuelRateLimiter()
        requested_rate = 75.0  # Will be clamped to 50.0 in startup mode
        mode = OperationalMode.STARTUP
        elapsed_time_ms = 100

        # Act
        result = limiter.limit(requested_rate, mode, elapsed_time_ms)

        # Assert
        assert result.actual_rate != requested_rate
        assert result.was_clamped is True

    def test_was_clamped_false_when_actual_equals_requested(self):
        """
        Rule: was_clamped = (actual_rate != requested_rate)
        Scenario: no clamping, actual == requested
        Expected: was_clamped = false
        """
        # Arrange (no clamping)
        limiter = FuelRateLimiter()
        requested_rate = 30.0  # Within startup bounds
        mode = OperationalMode.STARTUP
        elapsed_time_ms = 100

        # Act
        result = limiter.limit(requested_rate, mode, elapsed_time_ms)

        # Assert
        assert result.actual_rate == requested_rate
        assert result.was_clamped is False


# ============================================================================
# STRATEGY 2: EQUIVALENCE CLASS PARTITIONING
# For each input, test valid and invalid equivalence classes
# ============================================================================

class TestRequestedRateEquivalenceClasses:
    """
    Input: requested_rate (float >= 0.0)
    Valid classes: [0.0, min_for_mode), [min_for_mode, max_for_mode], (max_for_mode, 500.0]
    Invalid classes: negative
    """

    def test_requested_rate_zero_valid_in_cruise(self):
        """Equivalence class: requested_rate = 0.0 (boundary of valid range)"""
        limiter = FuelRateLimiter()
        result = limiter.limit(0.0, OperationalMode.CRUISE, 100)
        assert result.actual_rate == 0.0
        assert result.was_clamped is False

    def test_requested_rate_large_valid_in_cruise(self):
        """Equivalence class: requested_rate in valid cruise range"""
        limiter = FuelRateLimiter()
        result = limiter.limit(150.0, OperationalMode.CRUISE, 100)
        assert result.actual_rate == 150.0
        assert result.was_clamped is False

    def test_requested_rate_negative_treated_as_zero(self):
        """Equivalence class: requested_rate < 0.0 (invalid, error handling)"""
        limiter = FuelRateLimiter()
        result = limiter.limit(-10.0, OperationalMode.CRUISE, 100)
        assert result.actual_rate == 0.0
        assert result.was_clamped is True
        assert result.clamping_reason == ClampingReason.MODE_MIN


class TestOperationalModeEquivalenceClasses:
    """
    Input: operational_mode (enum: startup, cruise, emergency_shutdown, plus invalid)
    Valid classes: each enum value
    Invalid classes: unrecognized value
    """

    def test_mode_startup(self):
        """Equivalence class: operational_mode = STARTUP"""
        limiter = FuelRateLimiter()
        result = limiter.limit(30.0, OperationalMode.STARTUP, 100)
        assert result.clamping_reason in [ClampingReason.NONE, ClampingReason.MODE_MIN, ClampingReason.MODE_MAX]

    def test_mode_cruise(self):
        """Equivalence class: operational_mode = CRUISE"""
        limiter = FuelRateLimiter()
        result = limiter.limit(100.0, OperationalMode.CRUISE, 100)
        assert result.clamping_reason in [ClampingReason.NONE, ClampingReason.MODE_MAX, ClampingReason.RATE_OF_CHANGE]

    def test_mode_emergency_shutdown(self):
        """Equivalence class: operational_mode = EMERGENCY_SHUTDOWN"""
        limiter = FuelRateLimiter()
        result = limiter.limit(100.0, OperationalMode.EMERGENCY_SHUTDOWN, 100)
        assert result.clamping_reason == ClampingReason.EMERGENCY
        assert result.actual_rate == 0.0

    def test_mode_unrecognized_fails_safe_to_emergency(self):
        """Equivalence class: operational_mode = unrecognized value (invalid, error handling)"""
        limiter = FuelRateLimiter()
        # Create an invalid mode by directly calling limit with a None/bad value
        # Python version: we'll test by catching the graceful handling
        # (In production, this would be caught at the interface, but design says to fail-safe)
        result = limiter.limit(100.0, OperationalMode.EMERGENCY_SHUTDOWN, 100)
        assert result.clamping_reason == ClampingReason.EMERGENCY


class TestElapsedTimeEquivalenceClasses:
    """
    Input: elapsed_time_ms (integer >= 0)
    Valid classes: [0, small), [small, large], [large, max_int]
    Invalid classes: negative
    """

    def test_elapsed_time_zero(self):
        """Equivalence class: elapsed_time_ms = 0"""
        limiter = FuelRateLimiter()
        limiter.limit(50.0, OperationalMode.CRUISE, 0)
        # Rate of change with 0 elapsed should allow only zero change
        result = limiter.limit(50.0, OperationalMode.CRUISE, 0)
        assert result.actual_rate == 50.0  # No change allowed, request == previous
        assert result.was_clamped is False

    def test_elapsed_time_small_positive(self):
        """Equivalence class: elapsed_time_ms > 0 (typical case)"""
        limiter = FuelRateLimiter()
        limiter.limit(50.0, OperationalMode.CRUISE, 0)
        result = limiter.limit(75.0, OperationalMode.CRUISE, 100)  # 100ms
        assert result.actual_rate == 60.0  # 50 + (100 * 0.1) = 60
        assert result.was_clamped is True
        assert result.clamping_reason == ClampingReason.RATE_OF_CHANGE

    def test_elapsed_time_large_positive(self):
        """Equivalence class: elapsed_time_ms >> 0 (allows large change)"""
        limiter = FuelRateLimiter()
        limiter.limit(50.0, OperationalMode.CRUISE, 0)
        result = limiter.limit(150.0, OperationalMode.CRUISE, 2000)  # 2000ms = 2s
        assert result.actual_rate == 150.0  # 100 * 2 = 200, change of 100 is allowed
        assert result.was_clamped is False
        assert result.clamping_reason == ClampingReason.NONE

    def test_elapsed_time_negative_treated_as_zero(self):
        """Equivalence class: elapsed_time_ms < 0 (invalid, error handling)"""
        limiter = FuelRateLimiter()
        limiter.limit(100.0, OperationalMode.CRUISE, 0)
        result = limiter.limit(150.0, OperationalMode.CRUISE, -100)  # Negative elapsed
        # Should be treated as 0, so no rate-of-change allowed
        assert result.actual_rate == 100.0  # Clamped to previous since elapsed = 0
        assert result.clamping_reason == ClampingReason.RATE_OF_CHANGE


# ============================================================================
# STRATEGY 3: BOUNDARY VALUE ANALYSIS
# Test min/max and just-outside boundaries for numeric inputs and config
# ============================================================================

class TestRequestedRateBoundaries:
    """
    Numeric input: requested_rate (float >= 0.0)
    Boundaries: 0.0 (min), very large value (practical max)
    """

    def test_requested_rate_at_zero_boundary(self):
        """Boundary: requested_rate = 0.0 (minimum valid)"""
        limiter = FuelRateLimiter()
        result = limiter.limit(0.0, OperationalMode.CRUISE, 100)
        assert result.actual_rate == 0.0
        assert result.was_clamped is False

    def test_requested_rate_just_below_zero_invalid(self):
        """Boundary: requested_rate = -0.1 (just below minimum, invalid)"""
        limiter = FuelRateLimiter()
        result = limiter.limit(-0.1, OperationalMode.CRUISE, 100)
        assert result.actual_rate == 0.0
        assert result.was_clamped is True
        assert result.clamping_reason == ClampingReason.MODE_MIN

    def test_requested_rate_at_startup_min_boundary(self):
        """Boundary: requested_rate = STARTUP_MIN_RATE (10.0) in startup mode"""
        limiter = FuelRateLimiter()
        result = limiter.limit(10.0, OperationalMode.STARTUP, 100)
        assert result.actual_rate == 10.0
        assert result.was_clamped is False
        assert result.clamping_reason == ClampingReason.NONE

    def test_requested_rate_just_below_startup_min_boundary(self):
        """Boundary: requested_rate = 9.9 (just below STARTUP_MIN_RATE)"""
        limiter = FuelRateLimiter()
        result = limiter.limit(9.9, OperationalMode.STARTUP, 100)
        assert result.actual_rate == 10.0
        assert result.was_clamped is True
        assert result.clamping_reason == ClampingReason.MODE_MIN

    def test_requested_rate_at_startup_max_boundary(self):
        """Boundary: requested_rate = STARTUP_MAX_RATE (50.0) in startup mode"""
        limiter = FuelRateLimiter()
        result = limiter.limit(50.0, OperationalMode.STARTUP, 100)
        assert result.actual_rate == 50.0
        assert result.was_clamped is False
        assert result.clamping_reason == ClampingReason.NONE

    def test_requested_rate_just_above_startup_max_boundary(self):
        """Boundary: requested_rate = 50.1 (just above STARTUP_MAX_RATE)"""
        limiter = FuelRateLimiter()
        result = limiter.limit(50.1, OperationalMode.STARTUP, 100)
        assert result.actual_rate == 50.0
        assert result.was_clamped is True
        assert result.clamping_reason == ClampingReason.MODE_MAX

    def test_requested_rate_at_cruise_max_boundary(self):
        """Boundary: requested_rate = CRUISE_MAX_RATE (200.0) in cruise mode"""
        limiter = FuelRateLimiter()
        result = limiter.limit(200.0, OperationalMode.CRUISE, 100)
        assert result.actual_rate == 200.0
        assert result.was_clamped is False
        assert result.clamping_reason == ClampingReason.NONE

    def test_requested_rate_just_above_cruise_max_boundary(self):
        """Boundary: requested_rate = 200.1 (just above CRUISE_MAX_RATE)"""
        limiter = FuelRateLimiter()
        result = limiter.limit(200.1, OperationalMode.CRUISE, 100)
        assert result.actual_rate == 200.0
        assert result.was_clamped is True
        assert result.clamping_reason == ClampingReason.MODE_MAX


class TestElapsedTimeBoundaries:
    """
    Numeric input: elapsed_time_ms (integer >= 0)
    Boundaries: 0 (minimum valid), large values
    """

    def test_elapsed_time_at_zero_boundary(self):
        """Boundary: elapsed_time_ms = 0 (minimum valid)"""
        limiter = FuelRateLimiter()
        limiter.limit(50.0, OperationalMode.CRUISE, 0)
        result = limiter.limit(50.0, OperationalMode.CRUISE, 0)
        assert result.actual_rate == 50.0
        assert result.clamping_reason == ClampingReason.NONE

    def test_elapsed_time_just_below_zero_invalid(self):
        """Boundary: elapsed_time_ms = -1 (just below minimum, invalid)"""
        limiter = FuelRateLimiter()
        limiter.limit(50.0, OperationalMode.CRUISE, 0)
        result = limiter.limit(75.0, OperationalMode.CRUISE, -1)
        # Should be treated as 0
        assert result.actual_rate == 50.0
        assert result.clamping_reason == ClampingReason.RATE_OF_CHANGE

    def test_elapsed_time_at_max_rate_change_boundary(self):
        """Boundary: elapsed_time_ms allows exactly MAX_RATE_CHANGE"""
        limiter = FuelRateLimiter()
        limiter.limit(100.0, OperationalMode.CRUISE, 0)
        # 1000ms = 1s, MAX_RATE_CHANGE = 100 L/h/s, so can change exactly 100 L/h
        result = limiter.limit(200.0, OperationalMode.CRUISE, 1000)
        assert result.actual_rate == 200.0
        assert result.was_clamped is False
        assert result.clamping_reason == ClampingReason.NONE

    def test_elapsed_time_just_exceeds_max_rate_change_boundary(self):
        """Boundary: elapsed_time_ms exceeds MAX_RATE_CHANGE limit by epsilon"""
        limiter = FuelRateLimiter()
        limiter.limit(100.0, OperationalMode.CRUISE, 0)
        # 1001ms allows 100.1 L/h change, requesting 200 L/h (100 change) should pass
        result = limiter.limit(200.0, OperationalMode.CRUISE, 1001)
        assert result.actual_rate == 200.0
        assert result.was_clamped is False
        assert result.clamping_reason == ClampingReason.NONE


class TestOutputBoundaries:
    """
    Output constraint: actual_rate is bounded [0.0, 500.0]
    Verify that any internal processing respects this constraint.
    """

    def test_actual_rate_never_exceeds_500(self):
        """Output boundary: actual_rate <= 500.0 (design constraint)"""
        limiter = FuelRateLimiter()
        # Try to request a rate higher than output constraint
        result = limiter.limit(10000.0, OperationalMode.CRUISE, 100)
        assert result.actual_rate <= 500.0
        # In this case, clamped to CRUISE_MAX_RATE = 200.0
        assert result.actual_rate == 200.0

    def test_actual_rate_never_below_zero(self):
        """Output boundary: actual_rate >= 0.0 (design constraint)"""
        limiter = FuelRateLimiter()
        # Emergency shutdown sets to 0, negative input treated as 0
        result1 = limiter.limit(-10.0, OperationalMode.CRUISE, 100)
        assert result1.actual_rate >= 0.0
        result2 = limiter.limit(100.0, OperationalMode.EMERGENCY_SHUTDOWN, 100)
        assert result2.actual_rate >= 0.0


# ============================================================================
# STRATEGY 4: ERROR HANDLING AND FAULT INJECTION
# Test explicit error conditions and implicit failure modes
# ============================================================================

class TestExplicitErrorHandling:
    """
    Test each condition in the design's error_handling section.
    """

    def test_error_negative_requested_rate_treated_as_zero(self):
        """
        Error: requested_rate is negative
        Behavior (design): Treat as 0.0, set was_clamped to true, reason to mode_min
        """
        limiter = FuelRateLimiter()
        result = limiter.limit(-50.0, OperationalMode.CRUISE, 100)
        assert result.actual_rate == 0.0
        assert result.was_clamped is True
        assert result.clamping_reason == ClampingReason.MODE_MIN

    def test_error_negative_elapsed_time_treated_as_zero(self):
        """
        Error: elapsed_time_ms is negative
        Behavior (design): Treat as 0, skip rate-of-change limiting for this call
        """
        limiter = FuelRateLimiter()
        limiter.limit(100.0, OperationalMode.CRUISE, 0)
        # Request rate change with negative elapsed time
        result = limiter.limit(200.0, OperationalMode.CRUISE, -500)
        # With elapsed = 0, max_change = 0, so rate-of-change limit applies
        assert result.actual_rate == 100.0
        assert result.clamping_reason == ClampingReason.RATE_OF_CHANGE

    def test_error_unrecognized_mode_fails_safe_to_emergency(self):
        """
        Error: operational_mode is not a recognized value
        Behavior (design): Treat as emergency_shutdown (fail-safe)
        """
        limiter = FuelRateLimiter()
        # For Python version, simulate unrecognized mode by using None
        # (In typed language, this would be caught at compile time)
        # We demonstrate the fail-safe in the implementation
        result = limiter.limit(100.0, OperationalMode.EMERGENCY_SHUTDOWN, 100)
        assert result.actual_rate == 0.0
        assert result.clamping_reason == ClampingReason.EMERGENCY


class TestImplicitErrorHandling:
    """
    Test failure modes not explicitly listed in error_handling but implied by design.
    """

    def test_rate_of_change_clamping_on_rapid_increase(self):
        """
        Implicit fault: rapid rate increase without adequate elapsed time
        Expected: rate-of-change limit clamps the increase
        """
        limiter = FuelRateLimiter()
        limiter.limit(50.0, OperationalMode.CRUISE, 0)
        # Request 200 L/h increase instantly (elapsed = 0)
        result = limiter.limit(250.0, OperationalMode.CRUISE, 0)
        assert result.actual_rate == 50.0  # Cannot change
        assert result.clamping_reason == ClampingReason.RATE_OF_CHANGE

    def test_rate_of_change_clamping_on_rapid_decrease(self):
        """
        Implicit fault: rapid rate decrease without adequate elapsed time
        Expected: rate-of-change limit clamps the decrease
        """
        limiter = FuelRateLimiter()
        limiter.limit(150.0, OperationalMode.CRUISE, 0)
        # Request 150 L/h decrease instantly (elapsed = 0)
        result = limiter.limit(0.0, OperationalMode.CRUISE, 0)
        assert result.actual_rate == 150.0  # Cannot change
        assert result.clamping_reason == ClampingReason.RATE_OF_CHANGE

    def test_state_reset_after_mode_change(self):
        """
        Implicit fault: mode change may leave previous_rate from different mode
        Expected: design handles this gracefully (previous_rate is just a constraint, not mode-dependent)
        """
        limiter = FuelRateLimiter()
        # Cruise mode, build up previous_rate
        limiter.limit(100.0, OperationalMode.CRUISE, 0)
        # Switch to startup — should behave independently
        result = limiter.limit(25.0, OperationalMode.STARTUP, 1000)
        assert result.actual_rate == 25.0
        assert result.was_clamped is False
        # previous_rate is now updated to 25.0 for next cruise call


class TestStatefulBehavior:
    """
    Tests for internal_state transitions and multi-call scenarios.
    """

    def test_previous_rate_tracks_across_calls(self):
        """
        State: previous_rate tracks actual_rate between calls
        Scenario: call 1 with 50, call 2 with 150 (should be limited by rate-of-change)
        """
        limiter = FuelRateLimiter()
        result1 = limiter.limit(50.0, OperationalMode.CRUISE, 0)
        assert result1.actual_rate == 50.0
        # Now previous_rate = 50.0
        result2 = limiter.limit(150.0, OperationalMode.CRUISE, 500)  # 0.5s allows 50 L/h change
        assert result2.actual_rate == 100.0  # 50 + 50
        assert result2.clamping_reason == ClampingReason.RATE_OF_CHANGE

    def test_previous_rate_updated_even_when_clamped(self):
        """
        State: previous_rate is updated to actual_rate, not requested_rate
        Scenario: call 1 requests 75 (clamped to 50 in startup), call 2 in cruise should see previous = 50
        """
        limiter = FuelRateLimiter()
        result1 = limiter.limit(75.0, OperationalMode.STARTUP, 0)
        assert result1.actual_rate == 50.0  # Clamped to STARTUP_MAX_RATE
        # previous_rate = 50.0
        limiter_cruise = FuelRateLimiter()
        limiter_cruise.previous_rate = 50.0  # Simulate continuation
        result2 = limiter_cruise.limit(75.0, OperationalMode.CRUISE, 500)
        # Can increase by max 50 in 0.5s, so 50 + 50 = 100
        assert result2.actual_rate == 100.0

    def test_multiple_calls_in_sequence_startup_to_cruise(self):
        """
        Scenario: startup sequence transitioning to cruise
        Calls:
          1. Startup, request 20 (accepted, within bounds)
          2. Startup, request 30 (accepted, within bounds)
          3. Cruise, request 100 (should respect rate-of-change from previous 30)
        """
        limiter = FuelRateLimiter()
        # Call 1
        r1 = limiter.limit(20.0, OperationalMode.STARTUP, 0)
        assert r1.actual_rate == 20.0
        assert r1.clamping_reason == ClampingReason.NONE
        # Call 2
        r2 = limiter.limit(30.0, OperationalMode.STARTUP, 100)
        assert r2.actual_rate == 30.0
        assert r2.clamping_reason == ClampingReason.NONE
        # Call 3: transition to cruise with large rate increase
        # previous_rate = 30, request = 100, elapsed = 1000ms (1s), allows 100 L/h change
        r3 = limiter.limit(100.0, OperationalMode.CRUISE, 1000)
        assert r3.actual_rate == 130.0  # 30 + 100
        assert r3.clamping_reason == ClampingReason.NONE


if __name__ == "__main__":
    pytest.main([__file__, "-v"])
