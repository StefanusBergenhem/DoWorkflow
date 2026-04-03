package fuelratelimiter

import (
	"math"
	"testing"
)

// ============================================================================
// Test helpers
// ============================================================================

const float64Epsilon = 1e-9

func almostEqual(a, b float64) bool {
	return math.Abs(a-b) < float64Epsilon
}

// ============================================================================
// Startup mode tests
// ============================================================================

func TestStartup_RequestWithinBounds(t *testing.T) {
	limiter := NewFuelRateLimiter()
	result := limiter.Limit(30.0, "startup", 100)

	if !almostEqual(result.ActualRate, 30.0) {
		t.Errorf("expected actual_rate 30.0, got %f", result.ActualRate)
	}
	if result.WasClamped {
		t.Error("expected was_clamped false for in-range request")
	}
	if result.ClampingReason != "none" {
		t.Errorf("expected clamping_reason none, got %s", result.ClampingReason)
	}
}

func TestStartup_RequestBelowMinimum(t *testing.T) {
	limiter := NewFuelRateLimiter()
	result := limiter.Limit(5.0, "startup", 100)

	if !almostEqual(result.ActualRate, 10.0) {
		t.Errorf("expected actual_rate clamped to STARTUP_MIN_RATE 10.0, got %f", result.ActualRate)
	}
	if !result.WasClamped {
		t.Error("expected was_clamped true")
	}
	if result.ClampingReason != "mode_min" {
		t.Errorf("expected clamping_reason mode_min, got %s", result.ClampingReason)
	}
}

func TestStartup_RequestAboveMaximum(t *testing.T) {
	limiter := NewFuelRateLimiter()
	result := limiter.Limit(100.0, "startup", 100)

	if !almostEqual(result.ActualRate, 50.0) {
		t.Errorf("expected actual_rate clamped to STARTUP_MAX_RATE 50.0, got %f", result.ActualRate)
	}
	if !result.WasClamped {
		t.Error("expected was_clamped true")
	}
	if result.ClampingReason != "mode_max" {
		t.Errorf("expected clamping_reason mode_max, got %s", result.ClampingReason)
	}
}

func TestStartup_RequestExactlyAtMinBoundary(t *testing.T) {
	limiter := NewFuelRateLimiter()
	result := limiter.Limit(10.0, "startup", 100)

	if !almostEqual(result.ActualRate, 10.0) {
		t.Errorf("expected actual_rate 10.0, got %f", result.ActualRate)
	}
	if result.WasClamped {
		t.Error("expected was_clamped false at exact min boundary")
	}
}

func TestStartup_RequestExactlyAtMaxBoundary(t *testing.T) {
	limiter := NewFuelRateLimiter()
	result := limiter.Limit(50.0, "startup", 100)

	if !almostEqual(result.ActualRate, 50.0) {
		t.Errorf("expected actual_rate 50.0, got %f", result.ActualRate)
	}
	if result.WasClamped {
		t.Error("expected was_clamped false at exact max boundary")
	}
}

// ============================================================================
// Cruise mode tests — basic clamping
// ============================================================================

func TestCruise_RequestWithinBounds(t *testing.T) {
	limiter := NewFuelRateLimiter()
	// First call sets previous_rate, use small value to avoid rate-of-change issue
	limiter.Limit(100.0, "cruise", 10000)
	result := limiter.Limit(100.0, "cruise", 1000)

	if !almostEqual(result.ActualRate, 100.0) {
		t.Errorf("expected actual_rate 100.0, got %f", result.ActualRate)
	}
	if result.WasClamped {
		t.Error("expected was_clamped false")
	}
}

func TestCruise_RequestAboveMaximum(t *testing.T) {
	limiter := NewFuelRateLimiter()
	// Warm up to near max first
	limiter.Limit(200.0, "cruise", 10000)
	result := limiter.Limit(300.0, "cruise", 10000)

	if result.ActualRate > 200.0 {
		t.Errorf("expected actual_rate clamped to CRUISE_MAX_RATE 200.0, got %f", result.ActualRate)
	}
	if !result.WasClamped {
		t.Error("expected was_clamped true")
	}
}

func TestCruise_RequestZero(t *testing.T) {
	limiter := NewFuelRateLimiter()
	result := limiter.Limit(0.0, "cruise", 1000)

	if !almostEqual(result.ActualRate, 0.0) {
		t.Errorf("expected actual_rate 0.0, got %f", result.ActualRate)
	}
	if result.WasClamped {
		t.Error("expected was_clamped false when requesting 0 with previous_rate 0")
	}
}

// ============================================================================
// Cruise mode tests — rate-of-change limiting (core stateful behavior)
// ============================================================================

func TestCruise_RateOfChangeUpwardClamped(t *testing.T) {
	limiter := NewFuelRateLimiter()
	// Start at 0 (initial previous_rate), then request a large jump
	// MAX_RATE_CHANGE = 100 L/h/s, elapsed = 500ms
	// Allowed change = 100 * 500/1000 = 50 L/h
	// From 0, max achievable = 50
	result := limiter.Limit(150.0, "cruise", 500)

	if !almostEqual(result.ActualRate, 50.0) {
		t.Errorf("expected actual_rate 50.0 (rate-of-change limited), got %f", result.ActualRate)
	}
	if !result.WasClamped {
		t.Error("expected was_clamped true")
	}
	if result.ClampingReason != "rate_of_change" {
		t.Errorf("expected clamping_reason rate_of_change, got %s", result.ClampingReason)
	}
}

func TestCruise_RateOfChangeDownwardClamped(t *testing.T) {
	limiter := NewFuelRateLimiter()
	// Establish a high rate first
	limiter.Limit(100.0, "cruise", 10000) // enough time to reach 100
	// Now request a sudden drop to 0 in 500ms
	// Allowed change = 100 * 500/1000 = 50
	// From 100, min achievable = 50
	result := limiter.Limit(0.0, "cruise", 500)

	if !almostEqual(result.ActualRate, 50.0) {
		t.Errorf("expected actual_rate 50.0 (rate-of-change limited downward), got %f", result.ActualRate)
	}
	if !result.WasClamped {
		t.Error("expected was_clamped true")
	}
	if result.ClampingReason != "rate_of_change" {
		t.Errorf("expected clamping_reason rate_of_change, got %s", result.ClampingReason)
	}
}

func TestCruise_RateOfChangeExactlyAtLimit(t *testing.T) {
	limiter := NewFuelRateLimiter()
	// Start at 0, request exactly the allowed change
	// Allowed = 100 * 1000/1000 = 100
	result := limiter.Limit(100.0, "cruise", 1000)

	if !almostEqual(result.ActualRate, 100.0) {
		t.Errorf("expected actual_rate 100.0, got %f", result.ActualRate)
	}
	if result.WasClamped {
		t.Error("expected was_clamped false when change is exactly at limit")
	}
}

func TestCruise_RateOfChangeWithSmallElapsedTime(t *testing.T) {
	limiter := NewFuelRateLimiter()
	// elapsed = 10ms, allowed change = 100 * 10/1000 = 1.0
	result := limiter.Limit(50.0, "cruise", 10)

	if !almostEqual(result.ActualRate, 1.0) {
		t.Errorf("expected actual_rate 1.0 (small time window), got %f", result.ActualRate)
	}
	if !result.WasClamped {
		t.Error("expected was_clamped true")
	}
}

func TestCruise_MultipleCallsGradualRampUp(t *testing.T) {
	limiter := NewFuelRateLimiter()
	// Request 200 repeatedly with 500ms intervals
	// Each call allows +50 L/h increase
	// Call 1: 0 -> 50 (clamped from 200)
	r1 := limiter.Limit(200.0, "cruise", 500)
	if !almostEqual(r1.ActualRate, 50.0) {
		t.Errorf("call 1: expected 50.0, got %f", r1.ActualRate)
	}

	// Call 2: 50 -> 100 (clamped from 200)
	r2 := limiter.Limit(200.0, "cruise", 500)
	if !almostEqual(r2.ActualRate, 100.0) {
		t.Errorf("call 2: expected 100.0, got %f", r2.ActualRate)
	}

	// Call 3: 100 -> 150 (clamped from 200)
	r3 := limiter.Limit(200.0, "cruise", 500)
	if !almostEqual(r3.ActualRate, 150.0) {
		t.Errorf("call 3: expected 150.0, got %f", r3.ActualRate)
	}

	// Call 4: 150 -> 200 (exactly reaches target)
	r4 := limiter.Limit(200.0, "cruise", 500)
	if !almostEqual(r4.ActualRate, 200.0) {
		t.Errorf("call 4: expected 200.0, got %f", r4.ActualRate)
	}
	if r4.WasClamped {
		t.Error("call 4: expected was_clamped false when target is reached")
	}
}

func TestCruise_RampUpThenRampDown(t *testing.T) {
	limiter := NewFuelRateLimiter()
	// Ramp up to 100 in one second
	limiter.Limit(100.0, "cruise", 1000)

	// Now request 0, but can only drop by 50 in 500ms
	r := limiter.Limit(0.0, "cruise", 500)
	if !almostEqual(r.ActualRate, 50.0) {
		t.Errorf("expected 50.0 after ramp-down, got %f", r.ActualRate)
	}

	// Continue dropping
	r2 := limiter.Limit(0.0, "cruise", 500)
	if !almostEqual(r2.ActualRate, 0.0) {
		t.Errorf("expected 0.0 after full ramp-down, got %f", r2.ActualRate)
	}
	if r2.WasClamped {
		t.Error("expected was_clamped false when target 0 is reached")
	}
}

// ============================================================================
// Cruise mode — rate-of-change vs mode-max interaction
// ============================================================================

func TestCruise_ModeMaxTakesPrecedenceOverRateOfChange(t *testing.T) {
	limiter := NewFuelRateLimiter()
	// Establish previous_rate at 190
	limiter.Limit(190.0, "cruise", 10000)
	// Request 500, elapsed 1000ms => rate-of-change allows up to 290
	// But CRUISE_MAX_RATE = 200, so mode_max should win
	result := limiter.Limit(500.0, "cruise", 1000)

	if !almostEqual(result.ActualRate, 200.0) {
		t.Errorf("expected actual_rate clamped to 200.0, got %f", result.ActualRate)
	}
	if !result.WasClamped {
		t.Error("expected was_clamped true")
	}
}

// ============================================================================
// Emergency shutdown tests
// ============================================================================

func TestEmergencyShutdown_AlwaysZero(t *testing.T) {
	limiter := NewFuelRateLimiter()
	result := limiter.Limit(150.0, "emergency_shutdown", 100)

	if !almostEqual(result.ActualRate, 0.0) {
		t.Errorf("expected actual_rate 0.0 in emergency, got %f", result.ActualRate)
	}
	if !result.WasClamped {
		t.Error("expected was_clamped true")
	}
	if result.ClampingReason != "emergency" {
		t.Errorf("expected clamping_reason emergency, got %s", result.ClampingReason)
	}
}

func TestEmergencyShutdown_ZeroRequestStillClamped(t *testing.T) {
	// Even requesting 0 in emergency: the design says was_clamped = true always
	limiter := NewFuelRateLimiter()
	result := limiter.Limit(0.0, "emergency_shutdown", 100)

	if !almostEqual(result.ActualRate, 0.0) {
		t.Errorf("expected actual_rate 0.0, got %f", result.ActualRate)
	}
	if !result.WasClamped {
		t.Error("expected was_clamped true even for zero request in emergency")
	}
	if result.ClampingReason != "emergency" {
		t.Errorf("expected clamping_reason emergency, got %s", result.ClampingReason)
	}
}

func TestEmergencyShutdown_IgnoresRateOfChangeHistory(t *testing.T) {
	limiter := NewFuelRateLimiter()
	// Build up rate in cruise
	limiter.Limit(100.0, "cruise", 10000)
	// Switch to emergency — should go to 0 immediately, no rate-of-change
	result := limiter.Limit(100.0, "emergency_shutdown", 10)

	if !almostEqual(result.ActualRate, 0.0) {
		t.Errorf("expected immediate 0 in emergency regardless of history, got %f", result.ActualRate)
	}
}

// ============================================================================
// Error handling tests
// ============================================================================

func TestErrorHandling_NegativeRequestedRate(t *testing.T) {
	limiter := NewFuelRateLimiter()
	result := limiter.Limit(-10.0, "startup", 100)

	if result.ActualRate < 0 {
		t.Errorf("negative rate should be treated as 0 before clamping, got %f", result.ActualRate)
	}
	if !result.WasClamped {
		t.Error("expected was_clamped true for negative request")
	}
	if result.ClampingReason != "mode_min" {
		t.Errorf("expected clamping_reason mode_min, got %s", result.ClampingReason)
	}
}

func TestErrorHandling_NegativeElapsedTime(t *testing.T) {
	limiter := NewFuelRateLimiter()
	// With negative elapsed_time, rate-of-change limiting should be skipped
	// Request a jump that would normally be rate-limited
	result := limiter.Limit(150.0, "cruise", -100)

	// Should apply mode clamping (150 <= 200, so within cruise max)
	// but skip rate-of-change => actual_rate = 150
	if !almostEqual(result.ActualRate, 150.0) {
		t.Errorf("expected 150.0 (rate-of-change skipped for negative elapsed), got %f", result.ActualRate)
	}
}

func TestErrorHandling_UnrecognizedMode(t *testing.T) {
	limiter := NewFuelRateLimiter()
	result := limiter.Limit(100.0, "invalid_mode", 100)

	// Unrecognized mode should be treated as emergency_shutdown
	if !almostEqual(result.ActualRate, 0.0) {
		t.Errorf("expected 0.0 for unrecognized mode (fail-safe), got %f", result.ActualRate)
	}
	if !result.WasClamped {
		t.Error("expected was_clamped true")
	}
	if result.ClampingReason != "emergency" {
		t.Errorf("expected clamping_reason emergency, got %s", result.ClampingReason)
	}
}

func TestErrorHandling_EmptyMode(t *testing.T) {
	limiter := NewFuelRateLimiter()
	result := limiter.Limit(50.0, "", 100)

	if !almostEqual(result.ActualRate, 0.0) {
		t.Errorf("expected 0.0 for empty mode (fail-safe), got %f", result.ActualRate)
	}
	if result.ClampingReason != "emergency" {
		t.Errorf("expected clamping_reason emergency, got %s", result.ClampingReason)
	}
}

// ============================================================================
// Stateful behavior — previous_rate tracking
// ============================================================================

func TestState_PreviousRateInitiallyZero(t *testing.T) {
	limiter := NewFuelRateLimiter()
	// First call in cruise: previous_rate should be 0
	// Request 50, elapsed 1000ms => allowed change = 100, so 50 is fine
	result := limiter.Limit(50.0, "cruise", 1000)

	if !almostEqual(result.ActualRate, 50.0) {
		t.Errorf("expected 50.0, got %f", result.ActualRate)
	}
	if result.WasClamped {
		t.Error("expected was_clamped false")
	}
}

func TestState_PreviousRateUpdatesAfterClamping(t *testing.T) {
	limiter := NewFuelRateLimiter()
	// Call 1: request 200 in 500ms, from 0 -> clamped to 50
	r1 := limiter.Limit(200.0, "cruise", 500)
	if !almostEqual(r1.ActualRate, 50.0) {
		t.Fatalf("setup failed: expected 50.0, got %f", r1.ActualRate)
	}

	// Call 2: previous_rate should now be 50 (the clamped value, not the requested value)
	// Request 200 in 500ms => allowed change = 50, max = 100
	r2 := limiter.Limit(200.0, "cruise", 500)
	if !almostEqual(r2.ActualRate, 100.0) {
		t.Errorf("expected 100.0 (previous_rate was clamped output 50), got %f", r2.ActualRate)
	}
}

func TestState_PreviousRateUpdatesAcrossModeSwitch(t *testing.T) {
	limiter := NewFuelRateLimiter()
	// Start in startup mode at 30
	limiter.Limit(30.0, "startup", 100)
	// Switch to cruise — previous_rate should be 30 from startup
	// Request 200, elapsed 500ms => allowed change 50, from 30 -> max 80
	result := limiter.Limit(200.0, "cruise", 500)

	if !almostEqual(result.ActualRate, 80.0) {
		t.Errorf("expected 80.0 (previous_rate carried from startup), got %f", result.ActualRate)
	}
}

func TestState_EmergencySetsRateToZero(t *testing.T) {
	limiter := NewFuelRateLimiter()
	// Build up rate in cruise
	limiter.Limit(100.0, "cruise", 10000)
	// Emergency sets to 0
	limiter.Limit(50.0, "emergency_shutdown", 100)
	// Back to cruise — previous_rate should be 0 from emergency
	result := limiter.Limit(200.0, "cruise", 500)

	// From 0, allowed change = 50 in 500ms
	if !almostEqual(result.ActualRate, 50.0) {
		t.Errorf("expected 50.0 (previous_rate was 0 after emergency), got %f", result.ActualRate)
	}
}

// ============================================================================
// Edge cases
// ============================================================================

func TestEdge_ZeroElapsedTimeInCruise(t *testing.T) {
	limiter := NewFuelRateLimiter()
	// elapsed = 0 => allowed change = 0
	// From previous_rate 0, can only stay at 0
	result := limiter.Limit(100.0, "cruise", 0)

	if !almostEqual(result.ActualRate, 0.0) {
		t.Errorf("expected 0.0 with zero elapsed time, got %f", result.ActualRate)
	}
	if !result.WasClamped {
		t.Error("expected was_clamped true")
	}
	if result.ClampingReason != "rate_of_change" {
		t.Errorf("expected clamping_reason rate_of_change, got %s", result.ClampingReason)
	}
}

func TestEdge_VeryLargeElapsedTime(t *testing.T) {
	limiter := NewFuelRateLimiter()
	// Very large elapsed_time effectively removes rate-of-change limit
	result := limiter.Limit(200.0, "cruise", 1000000)

	if !almostEqual(result.ActualRate, 200.0) {
		t.Errorf("expected 200.0 with large elapsed time, got %f", result.ActualRate)
	}
	if result.WasClamped {
		t.Error("expected was_clamped false with large elapsed time")
	}
}

func TestEdge_OutputNeverExceeds500(t *testing.T) {
	// The output constraint says actual_rate <= 500.0
	limiter := NewFuelRateLimiter()
	// Even if somehow all limits are bypassed, output must not exceed 500
	// In normal operation, CRUISE_MAX_RATE=200 prevents this, but test the constraint
	result := limiter.Limit(1000.0, "cruise", 1000000)

	if result.ActualRate > 500.0 {
		t.Errorf("actual_rate %f exceeds absolute maximum 500.0", result.ActualRate)
	}
}

func TestEdge_OutputNeverNegative(t *testing.T) {
	limiter := NewFuelRateLimiter()
	result := limiter.Limit(-500.0, "cruise", 100)

	if result.ActualRate < 0.0 {
		t.Errorf("actual_rate should never be negative, got %f", result.ActualRate)
	}
}

// ============================================================================
// was_clamped correctness (the if-and-only-if rule)
// ============================================================================

func TestWasClamped_TrueOnlyWhenRateDiffers(t *testing.T) {
	limiter := NewFuelRateLimiter()
	// Request exactly what is achievable
	result := limiter.Limit(0.0, "cruise", 100)
	if result.WasClamped {
		t.Error("was_clamped should be false when actual == requested")
	}
}

func TestWasClamped_FalseWhenRateMatches(t *testing.T) {
	limiter := NewFuelRateLimiter()
	// In startup, request within bounds
	result := limiter.Limit(25.0, "startup", 100)
	if result.WasClamped {
		t.Error("was_clamped should be false when actual == requested")
	}
	if result.ClampingReason != "none" {
		t.Errorf("expected clamping_reason none, got %s", result.ClampingReason)
	}
}
