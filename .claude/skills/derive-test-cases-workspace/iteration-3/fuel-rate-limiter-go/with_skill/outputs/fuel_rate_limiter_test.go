package fuelcontrol

import (
	"testing"
)

// TestStartupModeClampingBelowMinimum verifies that requested rates below STARTUP_MIN_RATE are clamped
// Derivation: Requirement-based (behavior rule: clamp to [STARTUP_MIN_RATE, STARTUP_MAX_RATE])
func TestStartupModeClampingBelowMinimum(t *testing.T) {
	limiter := NewFuelRateLimiter()
	actualRate, wasClamped, reason := limiter.LimitFuelRate(5.0, "startup", 10)

	if actualRate != 10.0 { // STARTUP_MIN_RATE default
		t.Errorf("expected actual_rate=10.0, got %f", actualRate)
	}
	if !wasClamped {
		t.Errorf("expected was_clamped=true, got false")
	}
	if reason != "mode_min" {
		t.Errorf("expected clamping_reason=mode_min, got %s", reason)
	}
}

// TestStartupModeClampingAboveMaximum verifies that requested rates above STARTUP_MAX_RATE are clamped
// Derivation: Requirement-based (behavior rule: clamp to [STARTUP_MIN_RATE, STARTUP_MAX_RATE])
func TestStartupModeClampingAboveMaximum(t *testing.T) {
	limiter := NewFuelRateLimiter()
	actualRate, wasClamped, reason := limiter.LimitFuelRate(60.0, "startup", 10)

	if actualRate != 50.0 { // STARTUP_MAX_RATE default
		t.Errorf("expected actual_rate=50.0, got %f", actualRate)
	}
	if !wasClamped {
		t.Errorf("expected was_clamped=true, got false")
	}
	if reason != "mode_max" {
		t.Errorf("expected clamping_reason=mode_max, got %s", reason)
	}
}

// TestStartupModeValidRateNoClamp verifies that valid startup rates are passed through without clamping
// Derivation: Requirement-based + Equivalence class (valid range within startup bounds)
func TestStartupModeValidRateNoClamp(t *testing.T) {
	limiter := NewFuelRateLimiter()
	actualRate, wasClamped, reason := limiter.LimitFuelRate(25.0, "startup", 10)

	if actualRate != 25.0 {
		t.Errorf("expected actual_rate=25.0, got %f", actualRate)
	}
	if wasClamped {
		t.Errorf("expected was_clamped=false, got true")
	}
	if reason != "none" {
		t.Errorf("expected clamping_reason=none, got %s", reason)
	}
}

// TestStartupModeAtMinimumBoundary verifies exact behavior at the minimum boundary
// Derivation: Boundary value analysis (at minimum)
func TestStartupModeAtMinimumBoundary(t *testing.T) {
	limiter := NewFuelRateLimiter()
	actualRate, wasClamped, reason := limiter.LimitFuelRate(10.0, "startup", 10)

	if actualRate != 10.0 {
		t.Errorf("expected actual_rate=10.0, got %f", actualRate)
	}
	if wasClamped {
		t.Errorf("expected was_clamped=false at boundary, got true")
	}
	if reason != "none" {
		t.Errorf("expected clamping_reason=none at boundary, got %s", reason)
	}
}

// TestStartupModeAtMaximumBoundary verifies exact behavior at the maximum boundary
// Derivation: Boundary value analysis (at maximum)
func TestStartupModeAtMaximumBoundary(t *testing.T) {
	limiter := NewFuelRateLimiter()
	actualRate, wasClamped, reason := limiter.LimitFuelRate(50.0, "startup", 10)

	if actualRate != 50.0 {
		t.Errorf("expected actual_rate=50.0, got %f", actualRate)
	}
	if wasClamped {
		t.Errorf("expected was_clamped=false at boundary, got true")
	}
	if reason != "none" {
		t.Errorf("expected clamping_reason=none at boundary, got %s", reason)
	}
}

// TestCruiseModeValidRateNoClamp verifies that valid cruise rates are passed through without clamping
// Derivation: Requirement-based + Equivalence class (valid range [0, CRUISE_MAX_RATE])
func TestCruiseModeValidRateNoClamp(t *testing.T) {
	limiter := NewFuelRateLimiter()
	actualRate, wasClamped, reason := limiter.LimitFuelRate(100.0, "cruise", 10)

	if actualRate != 100.0 {
		t.Errorf("expected actual_rate=100.0, got %f", actualRate)
	}
	if wasClamped {
		t.Errorf("expected was_clamped=false, got true")
	}
	if reason != "none" {
		t.Errorf("expected clamping_reason=none, got %s", reason)
	}
}

// TestCruiseModeAboveMaximum verifies that requested rates above CRUISE_MAX_RATE are clamped
// Derivation: Requirement-based (behavior: clamp to [0, CRUISE_MAX_RATE])
func TestCruiseModeAboveMaximum(t *testing.T) {
	limiter := NewFuelRateLimiter()
	actualRate, wasClamped, reason := limiter.LimitFuelRate(250.0, "cruise", 10)

	if actualRate != 200.0 { // CRUISE_MAX_RATE default
		t.Errorf("expected actual_rate=200.0, got %f", actualRate)
	}
	if !wasClamped {
		t.Errorf("expected was_clamped=true, got false")
	}
	if reason != "mode_max" {
		t.Errorf("expected clamping_reason=mode_max, got %s", reason)
	}
}

// TestCruiseModeAtMaximumBoundary verifies exact behavior at cruise maximum boundary
// Derivation: Boundary value analysis (at maximum)
func TestCruiseModeAtMaximumBoundary(t *testing.T) {
	limiter := NewFuelRateLimiter()
	actualRate, wasClamped, reason := limiter.LimitFuelRate(200.0, "cruise", 10)

	if actualRate != 200.0 {
		t.Errorf("expected actual_rate=200.0, got %f", actualRate)
	}
	if wasClamped {
		t.Errorf("expected was_clamped=false at boundary, got true")
	}
	if reason != "none" {
		t.Errorf("expected clamping_reason=none at boundary, got %s", reason)
	}
}

// TestCruiseModeAtMinimumBoundary verifies exact behavior at zero rate (minimum for cruise)
// Derivation: Boundary value analysis (at minimum)
func TestCruiseModeAtMinimumBoundary(t *testing.T) {
	limiter := NewFuelRateLimiter()
	actualRate, wasClamped, reason := limiter.LimitFuelRate(0.0, "cruise", 10)

	if actualRate != 0.0 {
		t.Errorf("expected actual_rate=0.0, got %f", actualRate)
	}
	if wasClamped {
		t.Errorf("expected was_clamped=false at boundary, got true")
	}
	if reason != "none" {
		t.Errorf("expected clamping_reason=none at boundary, got %s", reason)
	}
}

// TestCruiseModeRateOfChangeExceeded verifies that rate-of-change limits are enforced
// Derivation: Requirement-based (behavior: apply rate-of-change limit)
// Scenario: previous_rate=50, requested_rate=200 (delta=150), elapsed=100ms
// MAX_RATE_CHANGE=100 L/h/s; allowed_change = 100 * 0.1s = 10 L/h
// Expected: actual_rate = 50 + 10 = 60
func TestCruiseModeRateOfChangeExceeded(t *testing.T) {
	limiter := NewFuelRateLimiter()

	// First call: establish previous_rate=50
	limiter.LimitFuelRate(50.0, "cruise", 10)

	// Second call: request 200 (delta 150), but ROC limit allows only 10 in 100ms
	actualRate, wasClamped, reason := limiter.LimitFuelRate(200.0, "cruise", 100)

	expectedRate := 60.0 // 50 + (100 * 0.1)
	if actualRate != expectedRate {
		t.Errorf("expected actual_rate=%f, got %f", expectedRate, actualRate)
	}
	if !wasClamped {
		t.Errorf("expected was_clamped=true, got false")
	}
	if reason != "rate_of_change" {
		t.Errorf("expected clamping_reason=rate_of_change, got %s", reason)
	}
}

// TestCruiseModeRateOfChangeWithinLimit verifies that rate changes within ROC limit are allowed
// Derivation: Requirement-based (behavior: apply rate-of-change limit)
// Scenario: previous_rate=50, requested_rate=60, elapsed=100ms
// Allowed change = 100 * 0.1 = 10; actual delta = 10
// Expected: actual_rate = 60, no clamping
func TestCruiseModeRateOfChangeWithinLimit(t *testing.T) {
	limiter := NewFuelRateLimiter()

	// First call: establish previous_rate=50
	limiter.LimitFuelRate(50.0, "cruise", 10)

	// Second call: request 60 (delta 10), which matches ROC limit exactly
	actualRate, wasClamped, reason := limiter.LimitFuelRate(60.0, "cruise", 100)

	if actualRate != 60.0 {
		t.Errorf("expected actual_rate=60.0, got %f", actualRate)
	}
	if wasClamped {
		t.Errorf("expected was_clamped=false, got true")
	}
	if reason != "none" {
		t.Errorf("expected clamping_reason=none, got %s", reason)
	}
}

// TestCruiseModeRateOfChangeDecreasing verifies that decreasing rates are also subject to ROC limit
// Derivation: Requirement-based (rate-of-change uses absolute value)
// Scenario: previous_rate=100, requested_rate=50, elapsed=1000ms
// Allowed change = 100 * 1.0 = 100; actual delta = 50 (within limit)
// Expected: actual_rate = 50, no clamping
func TestCruiseModeRateOfChangeDecreasing(t *testing.T) {
	limiter := NewFuelRateLimiter()

	// First call: establish previous_rate=100
	limiter.LimitFuelRate(100.0, "cruise", 10)

	// Second call: request 50 (delta -50), well within ROC limit
	actualRate, wasClamped, reason := limiter.LimitFuelRate(50.0, "cruise", 1000)

	if actualRate != 50.0 {
		t.Errorf("expected actual_rate=50.0, got %f", actualRate)
	}
	if wasClamped {
		t.Errorf("expected was_clamped=false, got true")
	}
	if reason != "none" {
		t.Errorf("expected clamping_reason=none, got %s", reason)
	}
}

// TestCruiseModeRateOfChangeDecreasingExceeded verifies decreasing rates exceeding ROC limit are clamped
// Derivation: Requirement-based (rate-of-change uses absolute value)
// Scenario: previous_rate=100, requested_rate=10, elapsed=100ms
// Allowed change = 100 * 0.1 = 10; actual delta = 90 (exceeds limit)
// Expected: actual_rate = 90, clamping applied
func TestCruiseModeRateOfChangeDecreasingExceeded(t *testing.T) {
	limiter := NewFuelRateLimiter()

	// First call: establish previous_rate=100
	limiter.LimitFuelRate(100.0, "cruise", 10)

	// Second call: request 10 (delta -90), exceeds ROC limit of 10
	actualRate, wasClamped, reason := limiter.LimitFuelRate(10.0, "cruise", 100)

	expectedRate := 90.0 // 100 - 10
	if actualRate != expectedRate {
		t.Errorf("expected actual_rate=%f, got %f", expectedRate, actualRate)
	}
	if !wasClamped {
		t.Errorf("expected was_clamped=true, got false")
	}
	if reason != "rate_of_change" {
		t.Errorf("expected clamping_reason=rate_of_change, got %s", reason)
	}
}

// TestCruiseModeRateOfChangeWithZeroElapsedTime verifies that zero elapsed time skips ROC limiting
// Derivation: Error handling (elapsed_time_ms < 0 treated as 0)
// This extends to zero: no time elapsed, no rate-of-change constraint
func TestCruiseModeRateOfChangeWithZeroElapsedTime(t *testing.T) {
	limiter := NewFuelRateLimiter()

	// First call: establish previous_rate=50
	limiter.LimitFuelRate(50.0, "cruise", 10)

	// Second call: request 200 with 0ms elapsed (no ROC constraint applied)
	actualRate, wasClamped, reason := limiter.LimitFuelRate(200.0, "cruise", 0)

	if actualRate != 200.0 {
		t.Errorf("expected actual_rate=200.0 (clamped by cruise max only), got %f", actualRate)
	}
	if !wasClamped {
		t.Errorf("expected was_clamped=true (by CRUISE_MAX_RATE), got false")
	}
	if reason != "mode_max" {
		t.Errorf("expected clamping_reason=mode_max, got %s", reason)
	}
}

// TestEmergencyShutdownMode verifies that emergency mode always returns 0
// Derivation: Requirement-based (behavior: set actual_rate to 0.0 regardless of requested_rate)
func TestEmergencyShutdownMode(t *testing.T) {
	limiter := NewFuelRateLimiter()
	actualRate, wasClamped, reason := limiter.LimitFuelRate(100.0, "emergency_shutdown", 10)

	if actualRate != 0.0 {
		t.Errorf("expected actual_rate=0.0, got %f", actualRate)
	}
	if !wasClamped {
		t.Errorf("expected was_clamped=true, got false")
	}
	if reason != "emergency" {
		t.Errorf("expected clamping_reason=emergency, got %s", reason)
	}
}

// TestEmergencyShutdownModeWithZeroRequest verifies emergency mode returns 0 even when 0 is requested
// Derivation: Equivalence class (any value, including 0)
func TestEmergencyShutdownModeWithZeroRequest(t *testing.T) {
	limiter := NewFuelRateLimiter()
	actualRate, wasClamped, reason := limiter.LimitFuelRate(0.0, "emergency_shutdown", 10)

	if actualRate != 0.0 {
		t.Errorf("expected actual_rate=0.0, got %f", actualRate)
	}
	// Design specifies was_clamped=true unconditionally in emergency
	if !wasClamped {
		t.Errorf("expected was_clamped=true in emergency, got false")
	}
	if reason != "emergency" {
		t.Errorf("expected clamping_reason=emergency, got %s", reason)
	}
}

// TestNegativeRequestedRateErrorHandling verifies negative rates are treated as 0
// Derivation: Error handling (negative requested_rate treated as 0, set was_clamped=true)
func TestNegativeRequestedRateErrorHandling(t *testing.T) {
	limiter := NewFuelRateLimiter()
	actualRate, wasClamped, reason := limiter.LimitFuelRate(-5.0, "cruise", 10)

	if actualRate != 0.0 {
		t.Errorf("expected actual_rate=0.0, got %f", actualRate)
	}
	if !wasClamped {
		t.Errorf("expected was_clamped=true, got false")
	}
	if reason != "mode_min" {
		t.Errorf("expected clamping_reason=mode_min, got %s", reason)
	}
}

// TestNegativeRequestedRateInStartup verifies negative rates in startup mode
// Derivation: Error handling + Mode interaction
func TestNegativeRequestedRateInStartup(t *testing.T) {
	limiter := NewFuelRateLimiter()
	actualRate, wasClamped, reason := limiter.LimitFuelRate(-10.0, "startup", 10)

	// Treated as 0, which is below STARTUP_MIN_RATE (10.0)
	if actualRate != 10.0 {
		t.Errorf("expected actual_rate=10.0 (STARTUP_MIN_RATE), got %f", actualRate)
	}
	if !wasClamped {
		t.Errorf("expected was_clamped=true, got false")
	}
	if reason != "mode_min" {
		t.Errorf("expected clamping_reason=mode_min, got %s", reason)
	}
}

// TestNegativeElapsedTimeErrorHandling verifies negative elapsed time is treated as 0
// Derivation: Error handling (negative elapsed_time_ms treated as 0)
// Negative elapsed time means no ROC constraint is applied
func TestNegativeElapsedTimeErrorHandling(t *testing.T) {
	limiter := NewFuelRateLimiter()

	// First call: establish previous_rate=50
	limiter.LimitFuelRate(50.0, "cruise", 10)

	// Second call: request 200 with negative elapsed time
	actualRate, wasClamped, reason := limiter.LimitFuelRate(200.0, "cruise", -100)

	if actualRate != 200.0 {
		t.Errorf("expected actual_rate=200.0 (clamped by cruise max only), got %f", actualRate)
	}
	if !wasClamped {
		t.Errorf("expected was_clamped=true (by CRUISE_MAX_RATE), got false")
	}
	if reason != "mode_max" {
		t.Errorf("expected clamping_reason=mode_max, got %s", reason)
	}
}

// TestUnrecognizedOperationalModeErrorHandling verifies unrecognized modes default to emergency_shutdown
// Derivation: Error handling (unrecognized mode treated as emergency_shutdown)
func TestUnrecognizedOperationalModeErrorHandling(t *testing.T) {
	limiter := NewFuelRateLimiter()
	actualRate, wasClamped, reason := limiter.LimitFuelRate(100.0, "invalid_mode", 10)

	if actualRate != 0.0 {
		t.Errorf("expected actual_rate=0.0 (emergency behavior), got %f", actualRate)
	}
	if !wasClamped {
		t.Errorf("expected was_clamped=true, got false")
	}
	if reason != "emergency" {
		t.Errorf("expected clamping_reason=emergency, got %s", reason)
	}
}

// TestWasClampedConsistencyWithActualRate verifies the invariant: was_clamped iff actual_rate != requested_rate
// Derivation: Requirement-based (rule: was_clamped is true iff actual_rate != requested_rate)
// Test cases: (1) clamped and actual != requested, (2) not clamped and actual == requested
func TestWasClampedConsistencyNoClamp(t *testing.T) {
	limiter := NewFuelRateLimiter()
	requested := 25.0
	actualRate, wasClamped, _ := limiter.LimitFuelRate(requested, "startup", 10)

	// Should not be clamped and should match requested
	if actualRate != requested {
		t.Errorf("actual_rate (%f) should equal requested (%f) when not clamped", actualRate, requested)
	}
	if wasClamped {
		t.Errorf("was_clamped should be false when actual_rate == requested_rate")
	}
}

// TestWasClampedConsistencyWithClamp verifies was_clamped invariant when clamping occurs
// Derivation: Requirement-based (rule: was_clamped is true iff actual_rate != requested_rate)
func TestWasClampedConsistencyWithClamp(t *testing.T) {
	limiter := NewFuelRateLimiter()
	requested := 60.0
	actualRate, wasClamped, _ := limiter.LimitFuelRate(requested, "startup", 10)

	// Clamped by STARTUP_MAX_RATE (50.0)
	if actualRate == requested {
		t.Errorf("actual_rate should differ from requested when clamped")
	}
	if !wasClamped {
		t.Errorf("was_clamped should be true when actual_rate != requested_rate")
	}
}

// TestMultipleCallsWithStateTracking verifies that previous_rate state is correctly maintained
// Derivation: Internal state + Requirement-based (previous_rate used for ROC limiting)
// Scenario: sequence of calls maintaining correct state transitions
func TestMultipleCallsWithStateTracking(t *testing.T) {
	limiter := NewFuelRateLimiter()

	// Call 1: Set previous_rate = 50
	r1, _, _ := limiter.LimitFuelRate(50.0, "cruise", 10)
	if r1 != 50.0 {
		t.Errorf("call 1: expected 50.0, got %f", r1)
	}

	// Call 2: Request 100 with 100ms elapsed
	// ROC limit: 100 * 0.1 = 10, so actual = 50 + 10 = 60
	r2, _, _ := limiter.LimitFuelRate(100.0, "cruise", 100)
	if r2 != 60.0 {
		t.Errorf("call 2: expected 60.0, got %f", r2)
	}

	// Call 3: Request 80 with 200ms elapsed
	// ROC limit: 100 * 0.2 = 20, so actual = 60 + 20 = 80
	r3, _, _ := limiter.LimitFuelRate(80.0, "cruise", 200)
	if r3 != 80.0 {
		t.Errorf("call 3: expected 80.0, got %f", r3)
	}
}

// TestModeTransitionFromStartupToCruise verifies state is correctly maintained across mode changes
// Derivation: Internal state + Requirement-based (previous_rate persists across mode changes)
func TestModeTransitionFromStartupToCruise(t *testing.T) {
	limiter := NewFuelRateLimiter()

	// Startup mode: request 30
	r1, _, _ := limiter.LimitFuelRate(30.0, "startup", 10)
	if r1 != 30.0 {
		t.Errorf("startup: expected 30.0, got %f", r1)
	}

	// Transition to cruise: request 100 with 100ms elapsed
	// Previous_rate = 30, ROC limit: 100 * 0.1 = 10, so actual = 30 + 10 = 40
	r2, wasClamped, reason := limiter.LimitFuelRate(100.0, "cruise", 100)
	if r2 != 40.0 {
		t.Errorf("cruise after startup: expected 40.0, got %f", r2)
	}
	if !wasClamped || reason != "rate_of_change" {
		t.Errorf("cruise after startup: expected rate_of_change clamping, got %s", reason)
	}
}

// TestModeTransitionThroughEmergency verifies that emergency mode resets state appropriately
// Derivation: Internal state + Mode transition
func TestModeTransitionThroughEmergency(t *testing.T) {
	limiter := NewFuelRateLimiter()

	// Cruise mode: request 100
	limiter.LimitFuelRate(100.0, "cruise", 10)

	// Emergency shutdown
	r2, _, _ := limiter.LimitFuelRate(150.0, "emergency_shutdown", 10)
	if r2 != 0.0 {
		t.Errorf("emergency: expected 0.0, got %f", r2)
	}

	// Back to cruise: request 50 with 100ms elapsed
	// Previous_rate = 0.0 (from emergency), ROC limit: 100 * 0.1 = 10, so actual = 0 + 10 = 10
	r3, _, reason := limiter.LimitFuelRate(50.0, "cruise", 100)
	if r3 != 10.0 {
		t.Errorf("cruise after emergency: expected 10.0, got %f", r3)
	}
	if reason != "rate_of_change" {
		t.Errorf("cruise after emergency: expected rate_of_change clamping, got %s", reason)
	}
}

// TestRateOfChangeWithVeryLargeElapsedTime verifies ROC limiting with large time intervals
// Derivation: Boundary value analysis (large elapsed_time_ms values)
// Scenario: previous_rate=0, requested=200, elapsed=10000ms
// ROC limit: 100 * 10 = 1000, so actual = min(200, 0 + 1000) = 200
func TestRateOfChangeWithVeryLargeElapsedTime(t *testing.T) {
	limiter := NewFuelRateLimiter()

	// First call: establish previous_rate=0
	limiter.LimitFuelRate(0.0, "cruise", 10)

	// Second call: request 200 with 10s elapsed (very large time)
	actualRate, wasClamped, reason := limiter.LimitFuelRate(200.0, "cruise", 10000)

	if actualRate != 200.0 {
		t.Errorf("expected actual_rate=200.0, got %f", actualRate)
	}
	if wasClamped {
		t.Errorf("expected was_clamped=false, got true")
	}
	if reason != "none" {
		t.Errorf("expected clamping_reason=none, got %s", reason)
	}
}

// TestRateOfChangeWithFractionalElapsedTime verifies ROC calculation with fractional milliseconds
// Derivation: Boundary value analysis (very small elapsed times)
// Scenario: previous_rate=100, requested=101, elapsed=1ms
// ROC limit: 100 * 0.001 = 0.1, so actual = 100 + 0.1 = 100.1
func TestRateOfChangeWithSmallElapsedTime(t *testing.T) {
	limiter := NewFuelRateLimiter()

	// First call: establish previous_rate=100
	limiter.LimitFuelRate(100.0, "cruise", 10)

	// Second call: request 101 with 1ms elapsed
	actualRate, wasClamped, reason := limiter.LimitFuelRate(101.0, "cruise", 1)

	expectedRate := 100.1 // 100 + (100 * 0.001)
	if actualRate != expectedRate {
		t.Errorf("expected actual_rate=%f, got %f", expectedRate, actualRate)
	}
	if !wasClamped {
		t.Errorf("expected was_clamped=true, got false")
	}
	if reason != "rate_of_change" {
		t.Errorf("expected clamping_reason=rate_of_change, got %s", reason)
	}
}

// TestOutputBoundaryCheck verifies that actual_rate never exceeds output maximum of 500.0
// Derivation: Boundary value analysis (output constraint: <= 500.0)
// This tests the hard ceiling on actual_rate output
func TestOutputBoundaryCheck(t *testing.T) {
	limiter := NewFuelRateLimiter()
	// Request rate at configured max (200 for cruise)
	actualRate, _, _ := limiter.LimitFuelRate(200.0, "cruise", 10)

	if actualRate > 500.0 {
		t.Errorf("actual_rate should never exceed 500.0, got %f", actualRate)
	}
	if actualRate < 0.0 {
		t.Errorf("actual_rate should never be negative, got %f", actualRate)
	}
}

// TestRateOfChangeMultipleConstraints verifies behavior when both ROC limit and mode max apply
// Derivation: Requirement-based (compound behavior when multiple constraints interact)
// Scenario: cruise mode with ROC limit AND mode max both limiting
// previous_rate=150, requested=300, elapsed=100ms
// ROC limit: 150 + (100 * 0.1) = 160
// Mode max: 200
// Expected: 160 (ROC is more restrictive), reason=rate_of_change
func TestRateOfChangeMultipleConstraints(t *testing.T) {
	limiter := NewFuelRateLimiter()

	// First call: establish previous_rate=150
	limiter.LimitFuelRate(150.0, "cruise", 10)

	// Second call: request 300 with 100ms elapsed
	actualRate, wasClamped, reason := limiter.LimitFuelRate(300.0, "cruise", 100)

	expectedRate := 160.0 // ROC: 150 + 10
	if actualRate != expectedRate {
		t.Errorf("expected actual_rate=%f, got %f", expectedRate, actualRate)
	}
	if !wasClamped {
		t.Errorf("expected was_clamped=true, got false")
	}
	if reason != "rate_of_change" {
		t.Errorf("expected clamping_reason=rate_of_change (most specific), got %s", reason)
	}
}
