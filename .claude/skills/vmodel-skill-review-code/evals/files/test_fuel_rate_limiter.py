"""Tests for FuelRateLimiter — derived from design CD-001."""

import pytest
from fuel_rate_limiter import (
    ClampingReason,
    FuelRateConfig,
    FuelRateLimiter,
    FuelRateResult,
    OperationalMode,
)


@pytest.fixture
def limiter():
    return FuelRateLimiter()


@pytest.fixture
def custom_limiter():
    config = FuelRateConfig(
        startup_min_rate=20.0,
        startup_max_rate=80.0,
        cruise_max_rate=300.0,
        max_rate_change=50.0,
    )
    return FuelRateLimiter(config)


# --- B1: Startup mode, below minimum ---

def test_startup_below_min_clamps_to_minimum(limiter):
    result = limiter.limit(5.0, OperationalMode.STARTUP, 100)
    assert result.actual_rate == 10.0
    assert result.was_clamped is True
    assert result.clamping_reason == ClampingReason.MODE_MIN


# --- B2: Startup mode, above maximum ---

def test_startup_above_max_clamps_to_maximum(limiter):
    result = limiter.limit(100.0, OperationalMode.STARTUP, 100)
    assert result.actual_rate == 50.0
    assert result.was_clamped is True
    assert result.clamping_reason == ClampingReason.MODE_MAX


# --- B3: Startup mode, within bounds ---

def test_startup_within_bounds_passes_through(limiter):
    result = limiter.limit(30.0, OperationalMode.STARTUP, 100)
    assert result.actual_rate == 30.0
    assert result.was_clamped is False
    assert result.clamping_reason == ClampingReason.NONE


# --- B4: Cruise mode, above maximum ---

def test_cruise_above_max_clamps_to_cruise_max(limiter):
    result = limiter.limit(250.0, OperationalMode.CRUISE, 100)
    assert result.actual_rate == 200.0
    assert result.was_clamped is True
    assert result.clamping_reason == ClampingReason.MODE_MAX


# --- B5: Cruise mode, rate-of-change exceeded ---

def test_cruise_rate_of_change_clamps_upward(limiter):
    # First call sets previous_rate to 50
    limiter.limit(50.0, OperationalMode.CRUISE, 100)
    # Second call requests 200, but max change is 100/s * 0.1s = 10
    result = limiter.limit(200.0, OperationalMode.CRUISE, 100)
    assert result.actual_rate == 60.0
    assert result.was_clamped is True
    assert result.clamping_reason == ClampingReason.RATE_OF_CHANGE


def test_cruise_rate_of_change_clamps_downward(limiter):
    limiter.limit(100.0, OperationalMode.CRUISE, 1000)
    # Max change is 100/s * 0.1s = 10, requesting drop from 100 to 0
    result = limiter.limit(0.0, OperationalMode.CRUISE, 100)
    assert result.actual_rate == 90.0
    assert result.was_clamped is True
    assert result.clamping_reason == ClampingReason.RATE_OF_CHANGE


# --- B6: Cruise mode, within bounds and rate-of-change ---

def test_cruise_within_all_limits(limiter):
    limiter.limit(50.0, OperationalMode.CRUISE, 1000)
    result = limiter.limit(55.0, OperationalMode.CRUISE, 1000)
    assert result.actual_rate == 55.0
    assert result.was_clamped is False
    assert result.clamping_reason == ClampingReason.NONE


# --- B7: Emergency shutdown ---

def test_emergency_shutdown_forces_zero(limiter):
    result = limiter.limit(150.0, OperationalMode.EMERGENCY_SHUTDOWN, 100)
    assert result.actual_rate == 0.0
    assert result.was_clamped is True
    assert result.clamping_reason == ClampingReason.EMERGENCY


# --- B8: State update ---

def test_previous_rate_updated_after_each_call(limiter):
    limiter.limit(30.0, OperationalMode.STARTUP, 100)
    # Now in cruise: previous_rate should be 30, request 35 with 1s elapsed
    result = limiter.limit(35.0, OperationalMode.CRUISE, 1000)
    assert result.actual_rate == 35.0
    assert result.was_clamped is False


# --- E1: Negative requested rate ---

def test_negative_rate_treated_as_zero(limiter):
    result = limiter.limit(-10.0, OperationalMode.STARTUP, 100)
    assert result.actual_rate == 10.0
    assert result.was_clamped is True
    assert result.clamping_reason == ClampingReason.MODE_MIN


# --- E2: Negative elapsed time ---

def test_negative_elapsed_time_skips_rate_of_change(limiter):
    limiter.limit(50.0, OperationalMode.CRUISE, 1000)
    # Negative elapsed: rate-of-change limiting skipped, should pass through
    result = limiter.limit(180.0, OperationalMode.CRUISE, -1)
    assert result.actual_rate == 180.0
    assert result.was_clamped is False


# --- Boundary values ---

@pytest.mark.parametrize(
    "rate, expected_rate, expected_clamped",
    [
        (0.0, 10.0, True),      # at minimum boundary
        (10.0, 10.0, False),    # at STARTUP_MIN_RATE
        (50.0, 50.0, False),    # at STARTUP_MAX_RATE
        (50.1, 50.0, True),     # just above max
    ],
)
def test_startup_boundary_values(limiter, rate, expected_rate, expected_clamped):
    result = limiter.limit(rate, OperationalMode.STARTUP, 100)
    assert result.actual_rate == expected_rate
    assert result.was_clamped is expected_clamped


@pytest.mark.parametrize(
    "rate, expected_rate, expected_clamped",
    [
        (200.0, 200.0, False),  # at CRUISE_MAX_RATE
        (200.1, 200.0, True),   # just above max
    ],
)
def test_cruise_max_boundary_values(limiter, rate, expected_rate, expected_clamped):
    result = limiter.limit(rate, OperationalMode.CRUISE, 10000)
    assert result.actual_rate == expected_rate
    assert result.was_clamped is expected_clamped


# --- Custom configuration ---

# --- E3: Unrecognized operational mode ---

def test_unrecognized_mode_treated_as_emergency_shutdown(limiter):
    result = limiter.limit(100.0, "invalid_mode", 100)
    assert result.actual_rate == 0.0
    assert result.was_clamped is True
    assert result.clamping_reason == ClampingReason.EMERGENCY


# --- Custom configuration ---

def test_custom_config_startup_min(custom_limiter):
    result = custom_limiter.limit(15.0, OperationalMode.STARTUP, 100)
    assert result.actual_rate == 20.0
    assert result.was_clamped is True
    assert result.clamping_reason == ClampingReason.MODE_MIN
