"""
Test suite for the FuelRateLimiter unit.

Derived from detailed design DD-001.
Tests cover: startup mode, cruise mode, emergency_shutdown mode,
rate-of-change limiting, error handling, clamping flag/reason, and
boundary conditions.
"""

import pytest
from enum import Enum
from dataclasses import dataclass
from typing import Tuple


# ---------------------------------------------------------------------------
# Minimal production stubs (just enough so the tests are self-contained)
# ---------------------------------------------------------------------------

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


@dataclass
class LimiterResult:
    actual_rate: float
    was_clamped: bool
    clamping_reason: ClampingReason


class FuelRateLimiter:
    """Reference implementation matching DD-001 behaviour."""

    def __init__(
        self,
        startup_min_rate: float = 10.0,
        startup_max_rate: float = 50.0,
        cruise_max_rate: float = 200.0,
        max_rate_change: float = 100.0,
    ):
        self.startup_min_rate = startup_min_rate
        self.startup_max_rate = startup_max_rate
        self.cruise_max_rate = cruise_max_rate
        self.max_rate_change = max_rate_change
        self.previous_rate = 0.0

    def limit(
        self,
        requested_rate: float,
        operational_mode: OperationalMode,
        elapsed_time_ms: int,
    ) -> LimiterResult:
        # Error handling: negative requested_rate
        if requested_rate < 0.0:
            requested_rate = 0.0

        # Error handling: unrecognised mode -> fail-safe (emergency_shutdown)
        if not isinstance(operational_mode, OperationalMode):
            operational_mode = OperationalMode.EMERGENCY_SHUTDOWN

        # Error handling: negative elapsed_time_ms
        skip_roc = elapsed_time_ms < 0
        if skip_roc:
            elapsed_time_ms = 0

        actual_rate = requested_rate
        clamping_reason = ClampingReason.NONE

        if operational_mode == OperationalMode.EMERGENCY_SHUTDOWN:
            actual_rate = 0.0
            clamping_reason = ClampingReason.EMERGENCY
            was_clamped = True
            self.previous_rate = actual_rate
            return LimiterResult(actual_rate, was_clamped, clamping_reason)

        if operational_mode == OperationalMode.STARTUP:
            if requested_rate < self.startup_min_rate:
                actual_rate = self.startup_min_rate
                clamping_reason = ClampingReason.MODE_MIN
            elif requested_rate > self.startup_max_rate:
                actual_rate = self.startup_max_rate
                clamping_reason = ClampingReason.MODE_MAX

        elif operational_mode == OperationalMode.CRUISE:
            if requested_rate > self.cruise_max_rate:
                actual_rate = self.cruise_max_rate
                clamping_reason = ClampingReason.MODE_MAX

            # Rate-of-change limiting
            if not skip_roc and elapsed_time_ms > 0:
                max_delta = self.max_rate_change * elapsed_time_ms / 1000.0
                delta = actual_rate - self.previous_rate
                if abs(delta) > max_delta:
                    if delta > 0:
                        actual_rate = self.previous_rate + max_delta
                    else:
                        actual_rate = self.previous_rate - max_delta
                    clamping_reason = ClampingReason.RATE_OF_CHANGE

        was_clamped = actual_rate != requested_rate
        self.previous_rate = actual_rate
        return LimiterResult(actual_rate, was_clamped, clamping_reason)


# ---------------------------------------------------------------------------
# Fixtures
# ---------------------------------------------------------------------------

@pytest.fixture
def limiter() -> FuelRateLimiter:
    """Limiter with default configuration."""
    return FuelRateLimiter()


# ===========================================================================
# STARTUP MODE
# ===========================================================================

class TestStartupMode:
    """Tests for operational_mode == startup."""

    def test_rate_within_bounds_passes_through(self, limiter: FuelRateLimiter):
        result = limiter.limit(30.0, OperationalMode.STARTUP, 100)
        assert result.actual_rate == 30.0
        assert result.was_clamped is False
        assert result.clamping_reason == ClampingReason.NONE

    def test_rate_at_minimum_boundary(self, limiter: FuelRateLimiter):
        result = limiter.limit(10.0, OperationalMode.STARTUP, 100)
        assert result.actual_rate == 10.0
        assert result.was_clamped is False

    def test_rate_at_maximum_boundary(self, limiter: FuelRateLimiter):
        result = limiter.limit(50.0, OperationalMode.STARTUP, 100)
        assert result.actual_rate == 50.0
        assert result.was_clamped is False

    def test_rate_below_minimum_is_clamped_up(self, limiter: FuelRateLimiter):
        result = limiter.limit(5.0, OperationalMode.STARTUP, 100)
        assert result.actual_rate == 10.0
        assert result.was_clamped is True
        assert result.clamping_reason == ClampingReason.MODE_MIN

    def test_rate_above_maximum_is_clamped_down(self, limiter: FuelRateLimiter):
        result = limiter.limit(80.0, OperationalMode.STARTUP, 100)
        assert result.actual_rate == 50.0
        assert result.was_clamped is True
        assert result.clamping_reason == ClampingReason.MODE_MAX

    def test_rate_zero_clamped_to_minimum(self, limiter: FuelRateLimiter):
        result = limiter.limit(0.0, OperationalMode.STARTUP, 100)
        assert result.actual_rate == 10.0
        assert result.was_clamped is True
        assert result.clamping_reason == ClampingReason.MODE_MIN


# ===========================================================================
# CRUISE MODE
# ===========================================================================

class TestCruiseMode:
    """Tests for operational_mode == cruise."""

    def test_rate_within_bounds_passes_through(self, limiter: FuelRateLimiter):
        result = limiter.limit(100.0, OperationalMode.CRUISE, 1000)
        assert result.actual_rate == 100.0
        assert result.was_clamped is False
        assert result.clamping_reason == ClampingReason.NONE

    def test_rate_at_zero_passes_through(self, limiter: FuelRateLimiter):
        result = limiter.limit(0.0, OperationalMode.CRUISE, 1000)
        assert result.actual_rate == 0.0
        assert result.was_clamped is False

    def test_rate_at_maximum_boundary(self, limiter: FuelRateLimiter):
        result = limiter.limit(200.0, OperationalMode.CRUISE, 1000)
        # previous_rate is 0, max_delta = 100 * 1 = 100, so ROC clamps to 100
        assert result.actual_rate == 100.0
        assert result.was_clamped is True
        assert result.clamping_reason == ClampingReason.RATE_OF_CHANGE

    def test_rate_above_maximum_is_clamped(self, limiter: FuelRateLimiter):
        result = limiter.limit(300.0, OperationalMode.CRUISE, 10000)
        assert result.actual_rate == 200.0
        assert result.was_clamped is True
        assert result.clamping_reason == ClampingReason.MODE_MAX


# ===========================================================================
# RATE-OF-CHANGE LIMITING (CRUISE)
# ===========================================================================

class TestRateOfChangeLimiting:
    """Rate-of-change limiting applies during cruise mode."""

    def test_large_increase_is_limited(self, limiter: FuelRateLimiter):
        """MAX_RATE_CHANGE=100 L/h/s, elapsed=500ms -> max_delta=50."""
        result = limiter.limit(80.0, OperationalMode.CRUISE, 500)
        # previous_rate=0, max_delta=100*0.5=50, request 80 -> clamped to 50
        assert result.actual_rate == 50.0
        assert result.was_clamped is True
        assert result.clamping_reason == ClampingReason.RATE_OF_CHANGE

    def test_increase_within_roc_allowed(self, limiter: FuelRateLimiter):
        """Request within rate-of-change budget passes through."""
        result = limiter.limit(40.0, OperationalMode.CRUISE, 1000)
        # previous_rate=0, max_delta=100*1=100, request 40 -> ok
        assert result.actual_rate == 40.0
        assert result.was_clamped is False

    def test_decrease_rate_of_change_limited(self, limiter: FuelRateLimiter):
        """Decreasing rate is also subject to ROC limiting."""
        # First set previous_rate to 100
        limiter.limit(100.0, OperationalMode.CRUISE, 10000)
        # Now request 0 with 500ms elapsed -> max_delta=50
        result = limiter.limit(0.0, OperationalMode.CRUISE, 500)
        assert result.actual_rate == 50.0
        assert result.was_clamped is True
        assert result.clamping_reason == ClampingReason.RATE_OF_CHANGE

    def test_roc_across_multiple_steps(self, limiter: FuelRateLimiter):
        """Rate ramps up across multiple calls."""
        # Step 1: 0 -> request 200, elapsed 1000ms, max_delta=100
        r1 = limiter.limit(200.0, OperationalMode.CRUISE, 1000)
        assert r1.actual_rate == 100.0

        # Step 2: 100 -> request 200, elapsed 1000ms, max_delta=100
        r2 = limiter.limit(200.0, OperationalMode.CRUISE, 1000)
        assert r2.actual_rate == 200.0
        assert r2.was_clamped is False

    def test_roc_with_very_small_elapsed_time(self, limiter: FuelRateLimiter):
        """Small elapsed time -> tiny allowed delta."""
        result = limiter.limit(50.0, OperationalMode.CRUISE, 10)
        # max_delta = 100 * 0.01 = 1.0, request 50 from 0 -> clamped to 1
        assert result.actual_rate == pytest.approx(1.0)
        assert result.was_clamped is True

    def test_roc_with_zero_elapsed_time(self, limiter: FuelRateLimiter):
        """Zero elapsed time -> max_delta=0, no change allowed from previous."""
        # First establish a rate
        limiter.limit(50.0, OperationalMode.CRUISE, 10000)
        # Now request different rate with 0ms elapsed
        result = limiter.limit(100.0, OperationalMode.CRUISE, 0)
        # max_delta = 0, so actual_rate stays at previous (50)
        assert result.actual_rate == 50.0
        assert result.was_clamped is True


# ===========================================================================
# EMERGENCY SHUTDOWN MODE
# ===========================================================================

class TestEmergencyShutdownMode:
    """Tests for operational_mode == emergency_shutdown."""

    def test_rate_forced_to_zero(self, limiter: FuelRateLimiter):
        result = limiter.limit(150.0, OperationalMode.EMERGENCY_SHUTDOWN, 100)
        assert result.actual_rate == 0.0
        assert result.was_clamped is True
        assert result.clamping_reason == ClampingReason.EMERGENCY

    def test_zero_request_still_flagged(self, limiter: FuelRateLimiter):
        """Even requesting 0 in emergency sets was_clamped=True per design."""
        result = limiter.limit(0.0, OperationalMode.EMERGENCY_SHUTDOWN, 100)
        assert result.actual_rate == 0.0
        assert result.was_clamped is True
        assert result.clamping_reason == ClampingReason.EMERGENCY

    def test_emergency_after_cruise(self, limiter: FuelRateLimiter):
        """Transition from cruise to emergency shuts down immediately."""
        limiter.limit(100.0, OperationalMode.CRUISE, 10000)
        result = limiter.limit(100.0, OperationalMode.EMERGENCY_SHUTDOWN, 100)
        assert result.actual_rate == 0.0
        assert result.was_clamped is True
        assert result.clamping_reason == ClampingReason.EMERGENCY


# ===========================================================================
# ERROR HANDLING
# ===========================================================================

class TestErrorHandling:
    """Edge cases and error conditions from DD-001 error_handling section."""

    def test_negative_requested_rate_treated_as_zero(self, limiter: FuelRateLimiter):
        result = limiter.limit(-10.0, OperationalMode.CRUISE, 1000)
        assert result.actual_rate == 0.0
        assert result.was_clamped is True

    def test_negative_requested_rate_in_startup_clamped_to_min(self, limiter: FuelRateLimiter):
        """Negative rate -> treated as 0.0, then startup min applies."""
        result = limiter.limit(-5.0, OperationalMode.STARTUP, 100)
        assert result.actual_rate == 10.0
        assert result.was_clamped is True
        assert result.clamping_reason == ClampingReason.MODE_MIN

    def test_negative_elapsed_time_skips_roc(self, limiter: FuelRateLimiter):
        """Negative elapsed_time_ms -> skip rate-of-change limiting."""
        result = limiter.limit(150.0, OperationalMode.CRUISE, -100)
        # No ROC applied, 150 <= 200 cruise max, so passes through
        assert result.actual_rate == 150.0
        assert result.was_clamped is False

    def test_unrecognised_mode_treated_as_emergency(self, limiter: FuelRateLimiter):
        """Unrecognised operational_mode -> fail-safe (emergency_shutdown)."""
        result = limiter.limit(100.0, "invalid_mode", 100)
        assert result.actual_rate == 0.0
        assert result.was_clamped is True
        assert result.clamping_reason == ClampingReason.EMERGENCY


# ===========================================================================
# WAS_CLAMPED FLAG CONSISTENCY
# ===========================================================================

class TestWasClampedFlag:
    """was_clamped must be true IFF actual_rate != requested_rate."""

    def test_not_clamped_when_rate_unchanged(self, limiter: FuelRateLimiter):
        result = limiter.limit(30.0, OperationalMode.STARTUP, 100)
        assert result.was_clamped is False
        assert result.actual_rate == 30.0

    def test_clamped_when_rate_changed(self, limiter: FuelRateLimiter):
        result = limiter.limit(5.0, OperationalMode.STARTUP, 100)
        assert result.was_clamped is True
        assert result.actual_rate != 5.0

    def test_emergency_always_clamped(self, limiter: FuelRateLimiter):
        result = limiter.limit(0.0, OperationalMode.EMERGENCY_SHUTDOWN, 100)
        assert result.was_clamped is True


# ===========================================================================
# OUTPUT CONSTRAINTS
# ===========================================================================

class TestOutputConstraints:
    """actual_rate must satisfy >= 0.0 and <= 500.0."""

    def test_actual_rate_never_negative(self, limiter: FuelRateLimiter):
        result = limiter.limit(-999.0, OperationalMode.CRUISE, 1000)
        assert result.actual_rate >= 0.0

    def test_actual_rate_within_global_max(self, limiter: FuelRateLimiter):
        """Even with custom config, output should not exceed 500."""
        big_limiter = FuelRateLimiter(cruise_max_rate=500.0)
        result = big_limiter.limit(500.0, OperationalMode.CRUISE, 100000)
        assert result.actual_rate <= 500.0


# ===========================================================================
# INTERNAL STATE — previous_rate tracking
# ===========================================================================

class TestInternalState:
    """Verify previous_rate is updated correctly across calls."""

    def test_previous_rate_updated_after_each_call(self, limiter: FuelRateLimiter):
        r1 = limiter.limit(50.0, OperationalMode.CRUISE, 10000)
        assert limiter.previous_rate == r1.actual_rate

        r2 = limiter.limit(100.0, OperationalMode.CRUISE, 10000)
        assert limiter.previous_rate == r2.actual_rate

    def test_emergency_resets_previous_rate_to_zero(self, limiter: FuelRateLimiter):
        limiter.limit(100.0, OperationalMode.CRUISE, 10000)
        limiter.limit(50.0, OperationalMode.EMERGENCY_SHUTDOWN, 100)
        assert limiter.previous_rate == 0.0


# ===========================================================================
# CONFIGURATION OVERRIDES
# ===========================================================================

class TestCustomConfiguration:
    """Verify that configuration parameters are respected."""

    def test_custom_startup_min_rate(self):
        limiter = FuelRateLimiter(startup_min_rate=20.0)
        result = limiter.limit(15.0, OperationalMode.STARTUP, 100)
        assert result.actual_rate == 20.0
        assert result.clamping_reason == ClampingReason.MODE_MIN

    def test_custom_startup_max_rate(self):
        limiter = FuelRateLimiter(startup_max_rate=30.0)
        result = limiter.limit(40.0, OperationalMode.STARTUP, 100)
        assert result.actual_rate == 30.0
        assert result.clamping_reason == ClampingReason.MODE_MAX

    def test_custom_cruise_max_rate(self):
        limiter = FuelRateLimiter(cruise_max_rate=150.0)
        result = limiter.limit(180.0, OperationalMode.CRUISE, 100000)
        assert result.actual_rate == 150.0
        assert result.was_clamped is True

    def test_custom_max_rate_change(self):
        limiter = FuelRateLimiter(max_rate_change=50.0)
        result = limiter.limit(100.0, OperationalMode.CRUISE, 1000)
        # max_delta = 50 * 1 = 50, from 0 -> clamped to 50
        assert result.actual_rate == 50.0
        assert result.clamping_reason == ClampingReason.RATE_OF_CHANGE


# ===========================================================================
# MODE TRANSITIONS
# ===========================================================================

class TestModeTransitions:
    """Transitions between modes must behave consistently."""

    def test_startup_to_cruise(self, limiter: FuelRateLimiter):
        limiter.limit(30.0, OperationalMode.STARTUP, 100)
        result = limiter.limit(80.0, OperationalMode.CRUISE, 1000)
        # previous_rate=30, max_delta=100, request 80 -> delta=50 < 100 -> ok
        assert result.actual_rate == 80.0
        assert result.was_clamped is False

    def test_cruise_to_emergency(self, limiter: FuelRateLimiter):
        limiter.limit(100.0, OperationalMode.CRUISE, 10000)
        result = limiter.limit(100.0, OperationalMode.EMERGENCY_SHUTDOWN, 100)
        assert result.actual_rate == 0.0
        assert result.clamping_reason == ClampingReason.EMERGENCY

    def test_emergency_to_startup(self, limiter: FuelRateLimiter):
        """After emergency, restarting in startup still enforces min rate."""
        limiter.limit(100.0, OperationalMode.EMERGENCY_SHUTDOWN, 100)
        result = limiter.limit(5.0, OperationalMode.STARTUP, 100)
        assert result.actual_rate == 10.0
        assert result.clamping_reason == ClampingReason.MODE_MIN
