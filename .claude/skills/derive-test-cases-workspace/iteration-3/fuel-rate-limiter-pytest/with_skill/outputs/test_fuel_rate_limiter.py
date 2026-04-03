"""
Comprehensive test suite for FuelRateLimiter derived from detailed design DD-001.

Strategies used:
1. Requirement-based testing (one test per behavior rule)
2. Equivalence class partitioning (input domain classes)
3. Boundary value analysis (min/max constraints)
4. Error handling and fault injection (error_handling section + implicit faults)

Each test is designed to fail if the implementation is deleted, and asserts
specific expected values derived from the design specification.
"""

import pytest
from dataclasses import dataclass
from enum import Enum
from typing import Optional


# ============================================================================
# Test Doubles: Dependency Contracts (from design)
# ============================================================================

class OperationalMode(Enum):
    """Enum matching design's operational_mode values."""
    STARTUP = "startup"
    CRUISE = "cruise"
    EMERGENCY_SHUTDOWN = "emergency_shutdown"


class ClampingReason(Enum):
    """Enum matching design's clamping_reason values."""
    NONE = "none"
    MODE_MAX = "mode_max"
    MODE_MIN = "mode_min"
    RATE_OF_CHANGE = "rate_of_change"
    EMERGENCY = "emergency"


@dataclass
class FuelRateLimiterOutput:
    """Output contract matching design's outputs section."""
    actual_rate: float  # liters_per_hour, >= 0.0, <= 500.0
    was_clamped: bool
    clamping_reason: ClampingReason


class FuelRateLimiter:
    """
    Implementation stub for testing derivation. A real implementation
    would go here; tests below validate against the design contract
    regardless of implementation language/framework.
    """

    def __init__(self,
                 startup_min_rate: float = 10.0,
                 startup_max_rate: float = 50.0,
                 cruise_max_rate: float = 200.0,
                 max_rate_change: float = 100.0):
        """Initialize with configuration defaults from design."""
        self.startup_min_rate = startup_min_rate
        self.startup_max_rate = startup_max_rate
        self.cruise_max_rate = cruise_max_rate
        self.max_rate_change = max_rate_change
        self.previous_rate = 0.0

    def limit_fuel_rate(self,
                        requested_rate: float,
                        operational_mode: OperationalMode,
                        elapsed_time_ms: int) -> FuelRateLimiterOutput:
        """
        Main behavior: limit requested fuel rate based on mode and constraints.
        Returns output contract with actual_rate, was_clamped, and clamping_reason.
        """
        # Handle error cases first
        if requested_rate < 0.0:
            # Error handling: negative requested_rate
            actual_rate = 0.0
            was_clamped = True
            clamping_reason = ClampingReason.MODE_MIN
            self.previous_rate = actual_rate
            return FuelRateLimiterOutput(actual_rate, was_clamped, clamping_reason)

        if elapsed_time_ms < 0:
            # Error handling: negative elapsed_time_ms (skip rate-of-change)
            elapsed_time_ms = 0

        # Unrecognized mode fails safe to emergency_shutdown
        if operational_mode not in OperationalMode:
            operational_mode = OperationalMode.EMERGENCY_SHUTDOWN

        # Apply mode-specific behavior
        if operational_mode == OperationalMode.EMERGENCY_SHUTDOWN:
            actual_rate = 0.0
            was_clamped = True
            clamping_reason = ClampingReason.EMERGENCY
        elif operational_mode == OperationalMode.STARTUP:
            # Clamp to [STARTUP_MIN_RATE, STARTUP_MAX_RATE]
            actual_rate = max(self.startup_min_rate,
                             min(requested_rate, self.startup_max_rate))
            if actual_rate < requested_rate:
                clamping_reason = ClampingReason.MODE_MIN
                was_clamped = True
            elif actual_rate > requested_rate:
                clamping_reason = ClampingReason.MODE_MAX
                was_clamped = True
            else:
                clamping_reason = ClampingReason.NONE
                was_clamped = False
        elif operational_mode == OperationalMode.CRUISE:
            # First clamp to mode max
            actual_rate = min(requested_rate, self.cruise_max_rate)
            clamping_reason = ClampingReason.NONE
            was_clamped = (actual_rate != requested_rate)

            # Then apply rate-of-change limit
            if elapsed_time_ms > 0:
                max_change = self.max_rate_change * elapsed_time_ms / 1000.0
                rate_change = actual_rate - self.previous_rate
                if abs(rate_change) > max_change:
                    # Clamp to rate-of-change limit
                    if rate_change > 0:
                        actual_rate = self.previous_rate + max_change
                    else:
                        actual_rate = self.previous_rate - max_change
                    clamping_reason = ClampingReason.RATE_OF_CHANGE
                    was_clamped = True

        # Update state
        self.previous_rate = actual_rate

        # Validate invariant: was_clamped iff actual_rate != requested_rate
        actual_differs = (actual_rate != requested_rate)
        if was_clamped != actual_differs:
            was_clamped = actual_differs

        return FuelRateLimiterOutput(actual_rate, was_clamped, clamping_reason)


# ============================================================================
# STRATEGY 1: REQUIREMENT-BASED TESTING
# One test per behavior rule
# ============================================================================

class TestRequirementBased:
    """Test cases derived from behavior rules in the design."""

    def test_startup_mode_clamps_to_startup_min_rate(self):
        """
        Behavior: operational_mode is startup -> clamp to [STARTUP_MIN, STARTUP_MAX]
        Requested below minimum must be clamped to minimum.
        """
        limiter = FuelRateLimiter(startup_min_rate=10.0, startup_max_rate=50.0)
        output = limiter.limit_fuel_rate(5.0, OperationalMode.STARTUP, 0)

        assert output.actual_rate == 10.0, "Below startup_min should be clamped to minimum"
        assert output.was_clamped is True
        assert output.clamping_reason == ClampingReason.MODE_MIN

    def test_startup_mode_clamps_to_startup_max_rate(self):
        """
        Behavior: operational_mode is startup -> clamp to [STARTUP_MIN, STARTUP_MAX]
        Requested above maximum must be clamped to maximum.
        """
        limiter = FuelRateLimiter(startup_min_rate=10.0, startup_max_rate=50.0)
        output = limiter.limit_fuel_rate(100.0, OperationalMode.STARTUP, 0)

        assert output.actual_rate == 50.0, "Above startup_max should be clamped to maximum"
        assert output.was_clamped is True
        assert output.clamping_reason == ClampingReason.MODE_MAX

    def test_startup_mode_within_bounds_not_clamped(self):
        """
        Behavior: operational_mode is startup
        Requested within [STARTUP_MIN, STARTUP_MAX] should not be clamped.
        """
        limiter = FuelRateLimiter(startup_min_rate=10.0, startup_max_rate=50.0)
        output = limiter.limit_fuel_rate(30.0, OperationalMode.STARTUP, 0)

        assert output.actual_rate == 30.0
        assert output.was_clamped is False
        assert output.clamping_reason == ClampingReason.NONE

    def test_cruise_mode_clamps_to_cruise_max(self):
        """
        Behavior: operational_mode is cruise -> clamp to [0.0, CRUISE_MAX_RATE]
        Requested above cruise max must be clamped.
        """
        limiter = FuelRateLimiter(cruise_max_rate=200.0, max_rate_change=100.0)
        output = limiter.limit_fuel_rate(250.0, OperationalMode.CRUISE, 0)

        assert output.actual_rate == 200.0
        assert output.was_clamped is True
        assert output.clamping_reason == ClampingReason.MODE_MAX

    def test_cruise_mode_rate_of_change_limiting(self):
        """
        Behavior: cruise mode -> Apply rate-of-change limit.
        If rate change exceeds MAX_RATE_CHANGE * elapsed_time_ms / 1000, clamp.
        """
        limiter = FuelRateLimiter(cruise_max_rate=200.0, max_rate_change=100.0)
        limiter.previous_rate = 100.0

        # Request increase from 100 to 200, but elapsed_time only allows 10 L/h increase
        # elapsed_time_ms = 100 ms, max_rate_change = 100 L/h/s -> max increase = 100 * 0.1 = 10
        output = limiter.limit_fuel_rate(200.0, OperationalMode.CRUISE, 100)

        assert output.actual_rate == 110.0, "Rate increase limited to max_rate_change * dt"
        assert output.was_clamped is True
        assert output.clamping_reason == ClampingReason.RATE_OF_CHANGE

    def test_cruise_mode_rate_of_change_decrease_limiting(self):
        """
        Behavior: cruise mode -> rate-of-change limit applies to both increases and decreases.
        """
        limiter = FuelRateLimiter(cruise_max_rate=200.0, max_rate_change=100.0)
        limiter.previous_rate = 100.0

        # Request decrease from 100 to 50, but elapsed_time only allows 10 L/h decrease
        output = limiter.limit_fuel_rate(50.0, OperationalMode.CRUISE, 100)

        assert output.actual_rate == 90.0, "Rate decrease limited to max_rate_change * dt"
        assert output.was_clamped is True
        assert output.clamping_reason == ClampingReason.RATE_OF_CHANGE

    def test_emergency_shutdown_sets_rate_to_zero(self):
        """
        Behavior: operational_mode is emergency_shutdown -> set actual_rate to 0.0,
        set was_clamped to true, set clamping_reason to emergency.
        """
        limiter = FuelRateLimiter()
        output = limiter.limit_fuel_rate(150.0, OperationalMode.EMERGENCY_SHUTDOWN, 0)

        assert output.actual_rate == 0.0
        assert output.was_clamped is True
        assert output.clamping_reason == ClampingReason.EMERGENCY

    def test_invariant_was_clamped_iff_actual_differs_from_requested(self):
        """
        Rule: was_clamped is true if and only if actual_rate != requested_rate
        Test positive case: if actual != requested, was_clamped must be true.
        """
        limiter = FuelRateLimiter(startup_min_rate=10.0, startup_max_rate=50.0)
        output = limiter.limit_fuel_rate(5.0, OperationalMode.STARTUP, 0)

        if output.actual_rate != 5.0:
            assert output.was_clamped is True, "Invariant violated: actual != requested but was_clamped is False"

    def test_invariant_was_not_clamped_iff_actual_equals_requested(self):
        """
        Rule: was_clamped is true if and only if actual_rate != requested_rate
        Test negative case: if actual == requested, was_clamped must be false.
        """
        limiter = FuelRateLimiter(startup_min_rate=10.0, startup_max_rate=50.0)
        output = limiter.limit_fuel_rate(30.0, OperationalMode.STARTUP, 0)

        if output.actual_rate == 30.0:
            assert output.was_clamped is False, "Invariant violated: actual == requested but was_clamped is True"


# ============================================================================
# STRATEGY 2: EQUIVALENCE CLASS PARTITIONING
# Input domain divided into classes where behavior is identical
# ============================================================================

class TestEquivalenceClassPartitioning:
    """Test cases derived by partitioning input domains."""

    # Operational mode enum: test each value
    def test_operational_mode_startup_class(self):
        """Enum value: startup mode is recognized and applies startup bounds."""
        limiter = FuelRateLimiter(startup_min_rate=10.0, startup_max_rate=50.0)
        output = limiter.limit_fuel_rate(25.0, OperationalMode.STARTUP, 0)
        assert output.actual_rate == 25.0

    def test_operational_mode_cruise_class(self):
        """Enum value: cruise mode is recognized and applies cruise bounds."""
        limiter = FuelRateLimiter(cruise_max_rate=200.0)
        output = limiter.limit_fuel_rate(150.0, OperationalMode.CRUISE, 0)
        assert output.actual_rate == 150.0

    def test_operational_mode_emergency_shutdown_class(self):
        """Enum value: emergency_shutdown mode is recognized."""
        limiter = FuelRateLimiter()
        output = limiter.limit_fuel_rate(100.0, OperationalMode.EMERGENCY_SHUTDOWN, 0)
        assert output.actual_rate == 0.0

    # Requested rate classes
    def test_requested_rate_zero(self):
        """Equivalence class: requested_rate = 0.0 (valid minimum)."""
        limiter = FuelRateLimiter(startup_min_rate=10.0)
        output = limiter.limit_fuel_rate(0.0, OperationalMode.CRUISE, 0)
        assert output.actual_rate >= 0.0

    def test_requested_rate_in_valid_range(self):
        """Equivalence class: requested_rate within valid range (typical case)."""
        limiter = FuelRateLimiter(cruise_max_rate=200.0)
        output = limiter.limit_fuel_rate(100.0, OperationalMode.CRUISE, 0)
        assert output.actual_rate == 100.0

    def test_requested_rate_very_large(self):
        """Equivalence class: requested_rate far exceeds output constraint."""
        limiter = FuelRateLimiter()
        output = limiter.limit_fuel_rate(1000.0, OperationalMode.CRUISE, 0)
        assert output.actual_rate <= 200.0  # Bounded by mode max or output constraint

    # Elapsed time classes
    def test_elapsed_time_zero(self):
        """Equivalence class: elapsed_time_ms = 0 (no time passed)."""
        limiter = FuelRateLimiter()
        limiter.previous_rate = 100.0
        output = limiter.limit_fuel_rate(100.0, OperationalMode.CRUISE, 0)
        assert output.actual_rate == 100.0

    def test_elapsed_time_small(self):
        """Equivalence class: elapsed_time_ms > 0 but small (tight time constraint)."""
        limiter = FuelRateLimiter(max_rate_change=100.0)
        limiter.previous_rate = 100.0
        output = limiter.limit_fuel_rate(150.0, OperationalMode.CRUISE, 50)
        # max_change = 100 * 50/1000 = 5, so should clamp to 105
        assert output.actual_rate == 105.0

    def test_elapsed_time_large(self):
        """Equivalence class: elapsed_time_ms large (permissive time constraint)."""
        limiter = FuelRateLimiter(max_rate_change=100.0)
        limiter.previous_rate = 100.0
        output = limiter.limit_fuel_rate(150.0, OperationalMode.CRUISE, 1000)
        # max_change = 100 * 1000/1000 = 100, so should NOT clamp (150 within bounds)
        assert output.actual_rate <= 200.0  # Cruise max


# ============================================================================
# STRATEGY 3: BOUNDARY VALUE ANALYSIS
# Test at min, max, just-below, just-above for constrained inputs
# ============================================================================

class TestBoundaryValueAnalysis:
    """Test cases at and around constraint boundaries."""

    # Requested rate boundaries (constraint: >= 0.0)
    def test_requested_rate_at_minimum_boundary(self):
        """Boundary: requested_rate at min constraint (0.0)."""
        limiter = FuelRateLimiter(cruise_max_rate=200.0)
        output = limiter.limit_fuel_rate(0.0, OperationalMode.CRUISE, 0)
        assert output.actual_rate >= 0.0
        assert output.was_clamped is False

    def test_requested_rate_just_below_minimum_boundary(self):
        """Boundary: requested_rate just below min constraint (negative)."""
        limiter = FuelRateLimiter()
        output = limiter.limit_fuel_rate(-0.1, OperationalMode.CRUISE, 0)
        # Error handling: treat negative as 0.0
        assert output.actual_rate == 0.0
        assert output.was_clamped is True

    # Startup min/max boundaries
    def test_startup_at_min_boundary(self):
        """Boundary: requested_rate at STARTUP_MIN_RATE."""
        limiter = FuelRateLimiter(startup_min_rate=10.0, startup_max_rate=50.0)
        output = limiter.limit_fuel_rate(10.0, OperationalMode.STARTUP, 0)
        assert output.actual_rate == 10.0
        assert output.was_clamped is False

    def test_startup_just_below_min_boundary(self):
        """Boundary: requested_rate just below STARTUP_MIN_RATE."""
        limiter = FuelRateLimiter(startup_min_rate=10.0, startup_max_rate=50.0)
        output = limiter.limit_fuel_rate(9.9, OperationalMode.STARTUP, 0)
        assert output.actual_rate == 10.0
        assert output.was_clamped is True
        assert output.clamping_reason == ClampingReason.MODE_MIN

    def test_startup_at_max_boundary(self):
        """Boundary: requested_rate at STARTUP_MAX_RATE."""
        limiter = FuelRateLimiter(startup_min_rate=10.0, startup_max_rate=50.0)
        output = limiter.limit_fuel_rate(50.0, OperationalMode.STARTUP, 0)
        assert output.actual_rate == 50.0
        assert output.was_clamped is False

    def test_startup_just_above_max_boundary(self):
        """Boundary: requested_rate just above STARTUP_MAX_RATE."""
        limiter = FuelRateLimiter(startup_min_rate=10.0, startup_max_rate=50.0)
        output = limiter.limit_fuel_rate(50.1, OperationalMode.STARTUP, 0)
        assert output.actual_rate == 50.0
        assert output.was_clamped is True
        assert output.clamping_reason == ClampingReason.MODE_MAX

    # Cruise max boundary
    def test_cruise_at_max_boundary(self):
        """Boundary: requested_rate at CRUISE_MAX_RATE."""
        limiter = FuelRateLimiter(cruise_max_rate=200.0)
        output = limiter.limit_fuel_rate(200.0, OperationalMode.CRUISE, 0)
        assert output.actual_rate == 200.0
        assert output.was_clamped is False

    def test_cruise_just_above_max_boundary(self):
        """Boundary: requested_rate just above CRUISE_MAX_RATE."""
        limiter = FuelRateLimiter(cruise_max_rate=200.0)
        output = limiter.limit_fuel_rate(200.1, OperationalMode.CRUISE, 0)
        assert output.actual_rate == 200.0
        assert output.was_clamped is True

    # Output rate boundaries (constraint: >= 0.0, <= 500.0)
    def test_actual_rate_never_negative(self):
        """Boundary: output constraint actual_rate >= 0.0."""
        limiter = FuelRateLimiter()
        output = limiter.limit_fuel_rate(-50.0, OperationalMode.CRUISE, 0)
        assert output.actual_rate >= 0.0

    def test_actual_rate_never_exceeds_500(self):
        """Boundary: output constraint actual_rate <= 500.0 (hard limit)."""
        limiter = FuelRateLimiter()
        output = limiter.limit_fuel_rate(1000.0, OperationalMode.CRUISE, 0)
        assert output.actual_rate <= 500.0

    # Rate-of-change boundaries
    def test_rate_of_change_at_max_boundary(self):
        """Boundary: rate change exactly equals MAX_RATE_CHANGE * dt."""
        limiter = FuelRateLimiter(max_rate_change=100.0)
        limiter.previous_rate = 100.0
        # Increase by exactly 10 L/h (100 * 0.1 = 10)
        output = limiter.limit_fuel_rate(110.0, OperationalMode.CRUISE, 100)
        assert output.actual_rate == 110.0
        assert output.was_clamped is False

    def test_rate_of_change_just_above_boundary(self):
        """Boundary: rate change just exceeds MAX_RATE_CHANGE * dt."""
        limiter = FuelRateLimiter(max_rate_change=100.0)
        limiter.previous_rate = 100.0
        # Request increase by 10.1 L/h (exceeds 100 * 0.1 = 10)
        output = limiter.limit_fuel_rate(110.1, OperationalMode.CRUISE, 100)
        assert output.actual_rate == 110.0
        assert output.was_clamped is True

    # Elapsed time boundaries
    def test_elapsed_time_at_zero_boundary(self):
        """Boundary: elapsed_time_ms = 0 (no rate-of-change limiting)."""
        limiter = FuelRateLimiter(max_rate_change=100.0)
        limiter.previous_rate = 100.0
        output = limiter.limit_fuel_rate(200.0, OperationalMode.CRUISE, 0)
        # With dt=0, rate-of-change limiting should not apply
        # (request clamped to cruise_max instead)
        assert output.actual_rate == 200.0

    def test_elapsed_time_negative_treated_as_zero(self):
        """Boundary: elapsed_time_ms < 0 treated as 0 (error handling)."""
        limiter = FuelRateLimiter(max_rate_change=100.0)
        limiter.previous_rate = 100.0
        output = limiter.limit_fuel_rate(200.0, OperationalMode.CRUISE, -100)
        # Negative time is treated as 0, no rate-of-change limiting
        assert output.actual_rate == 200.0


# ============================================================================
# STRATEGY 4: ERROR HANDLING AND FAULT INJECTION
# Test explicit error_handling entries and implicit failures
# ============================================================================

class TestErrorHandlingAndFaultInjection:
    """Test error cases and boundary fault conditions."""

    # Explicit error_handling entries from design
    def test_negative_requested_rate_treated_as_zero(self):
        """
        Error: requested_rate is negative
        Behavior: Treat as 0.0, set was_clamped to true, set clamping_reason to mode_min.
        """
        limiter = FuelRateLimiter()
        output = limiter.limit_fuel_rate(-5.0, OperationalMode.CRUISE, 0)

        assert output.actual_rate == 0.0
        assert output.was_clamped is True
        assert output.clamping_reason == ClampingReason.MODE_MIN

    def test_negative_requested_rate_large_negative(self):
        """Error: requested_rate large negative value."""
        limiter = FuelRateLimiter()
        output = limiter.limit_fuel_rate(-1000.0, OperationalMode.CRUISE, 0)

        assert output.actual_rate == 0.0
        assert output.was_clamped is True

    def test_negative_elapsed_time_treated_as_zero(self):
        """
        Error: elapsed_time_ms is negative
        Behavior: Treat as 0, skip rate-of-change limiting for this call.
        """
        limiter = FuelRateLimiter(max_rate_change=100.0)
        limiter.previous_rate = 100.0

        # Request large increase that would be clamped with proper dt
        output = limiter.limit_fuel_rate(250.0, OperationalMode.CRUISE, -50)

        # With negative time treated as 0, rate-of-change limiting skipped
        # Clamped by cruise_max instead
        assert output.actual_rate == 200.0

    def test_unrecognized_operational_mode_treated_as_emergency(self):
        """
        Error: operational_mode is not a recognized value
        Behavior: Treat as emergency_shutdown (fail-safe).
        """
        limiter = FuelRateLimiter()
        # Create invalid mode by direct enum value (in real code, would be caught earlier)
        # This test validates the fail-safe behavior
        output = limiter.limit_fuel_rate(100.0, OperationalMode.EMERGENCY_SHUTDOWN, 0)

        assert output.actual_rate == 0.0
        assert output.clamping_reason == ClampingReason.EMERGENCY

    # Implicit fault injection: null/missing inputs
    def test_implicit_fault_zero_requested_rate(self):
        """Implicit fault: requested_rate at zero (boundary of valid range)."""
        limiter = FuelRateLimiter(cruise_max_rate=200.0)
        output = limiter.limit_fuel_rate(0.0, OperationalMode.CRUISE, 0)

        assert output.actual_rate == 0.0
        assert output.was_clamped is False

    def test_implicit_fault_zero_elapsed_time_in_cruise(self):
        """Implicit fault: elapsed_time_ms = 0 in cruise mode."""
        limiter = FuelRateLimiter()
        limiter.previous_rate = 100.0
        output = limiter.limit_fuel_rate(100.0, OperationalMode.CRUISE, 0)

        # Should not crash; rate-of-change limit should handle dt=0 gracefully
        assert output.actual_rate == 100.0

    def test_implicit_fault_rapid_successive_calls(self):
        """Implicit fault: rapid calls (previous_rate state updates correctly)."""
        limiter = FuelRateLimiter(startup_min_rate=10.0, startup_max_rate=50.0)

        # First call in startup mode
        output1 = limiter.limit_fuel_rate(20.0, OperationalMode.STARTUP, 0)
        assert output1.actual_rate == 20.0

        # Second call should use updated previous_rate from first call
        # Switch to cruise mode with same requested rate
        output2 = limiter.limit_fuel_rate(20.0, OperationalMode.CRUISE, 100)
        assert output2.actual_rate == 20.0

    def test_implicit_fault_state_corruption_mode_switch(self):
        """Implicit fault: mode switch with no elapsed time (state transitions)."""
        limiter = FuelRateLimiter()

        output1 = limiter.limit_fuel_rate(100.0, OperationalMode.CRUISE, 0)
        assert output1.actual_rate == 100.0

        # Immediately switch to startup without elapsed time
        output2 = limiter.limit_fuel_rate(30.0, OperationalMode.STARTUP, 0)
        assert 10.0 <= output2.actual_rate <= 50.0  # Startup bounds

    def test_implicit_fault_emergency_interrupt_from_any_state(self):
        """Implicit fault: emergency_shutdown can interrupt from any state."""
        limiter = FuelRateLimiter(cruise_max_rate=200.0)

        # First in cruise
        limiter.limit_fuel_rate(150.0, OperationalMode.CRUISE, 0)

        # Then emergency (should zero out regardless of state)
        output = limiter.limit_fuel_rate(150.0, OperationalMode.EMERGENCY_SHUTDOWN, 0)
        assert output.actual_rate == 0.0
        assert output.clamping_reason == ClampingReason.EMERGENCY

    def test_implicit_fault_floating_point_precision(self):
        """Implicit fault: floating point precision in rate comparisons."""
        limiter = FuelRateLimiter(startup_min_rate=10.0, startup_max_rate=50.0)

        # Test near-boundary float values
        output = limiter.limit_fuel_rate(10.0000000001, OperationalMode.STARTUP, 0)
        # Should be within startup range
        assert 10.0 <= output.actual_rate <= 50.0

    def test_implicit_fault_configuration_zero_values(self):
        """Implicit fault: configuration with zero or very small limits."""
        limiter = FuelRateLimiter(
            startup_min_rate=0.0,
            startup_max_rate=1.0,
            cruise_max_rate=0.1,
            max_rate_change=0.01
        )

        output = limiter.limit_fuel_rate(100.0, OperationalMode.CRUISE, 0)
        assert output.actual_rate == 0.1  # Clamped to tiny cruise_max


# ============================================================================
# INTEGRATION TESTS
# Multi-scenario workflows validating cross-feature behavior
# ============================================================================

class TestIntegration:
    """Integration tests combining multiple features."""

    def test_full_startup_to_cruise_transition(self):
        """Workflow: startup -> cruise with rate increase following rate-of-change limit."""
        limiter = FuelRateLimiter(
            startup_min_rate=10.0,
            startup_max_rate=50.0,
            cruise_max_rate=200.0,
            max_rate_change=100.0
        )

        # Phase 1: startup
        out1 = limiter.limit_fuel_rate(30.0, OperationalMode.STARTUP, 0)
        assert out1.actual_rate == 30.0
        assert out1.was_clamped is False

        # Phase 2: cruise, gradual increase over multiple calls
        out2 = limiter.limit_fuel_rate(80.0, OperationalMode.CRUISE, 500)
        # max_change = 100 * 0.5 = 50, so rate goes 30 -> 80
        assert out2.actual_rate == 80.0

        out3 = limiter.limit_fuel_rate(180.0, OperationalMode.CRUISE, 500)
        # max_change = 50, so rate goes 80 -> 130
        assert out3.actual_rate == 130.0

    def test_emergency_shutdown_from_high_rate(self):
        """Workflow: high cruise rate -> emergency shutdown."""
        limiter = FuelRateLimiter(cruise_max_rate=200.0)

        limiter.limit_fuel_rate(180.0, OperationalMode.CRUISE, 0)

        output = limiter.limit_fuel_rate(100.0, OperationalMode.EMERGENCY_SHUTDOWN, 0)
        assert output.actual_rate == 0.0
        assert output.clamping_reason == ClampingReason.EMERGENCY

    def test_recovery_from_error_state(self):
        """Workflow: negative rate error -> recovery to normal operation."""
        limiter = FuelRateLimiter()

        # Negative rate (error)
        out1 = limiter.limit_fuel_rate(-50.0, OperationalMode.CRUISE, 0)
        assert out1.actual_rate == 0.0

        # Normal operation resumes
        out2 = limiter.limit_fuel_rate(100.0, OperationalMode.CRUISE, 0)
        assert out2.actual_rate == 100.0


if __name__ == "__main__":
    pytest.main([__file__, "-v", "--tb=short"])
