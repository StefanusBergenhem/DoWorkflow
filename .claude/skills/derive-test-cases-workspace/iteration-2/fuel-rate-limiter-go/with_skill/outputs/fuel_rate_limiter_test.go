package fuel

import (
	"testing"
)

// TestFuelRateLimiter_StartupModeLowerBound verifies that in startup mode,
// a fuel rate below STARTUP_MIN_RATE (10.0 L/h) is clamped to the minimum.
// Requirement-based: behavior rule "Clamp requested_rate to range [STARTUP_MIN_RATE, STARTUP_MAX_RATE]"
// Error handling: "requested_rate is negative"
func TestFuelRateLimiter_StartupModeLowerBound(t *testing.T) {
	// Arrange
	limiter := NewFuelRateLimiter()
	requestedRate := 5.0 // below STARTUP_MIN_RATE (10.0)

	// Act
	result := limiter.Limit(requestedRate, "startup", 100)

	// Assert
	if result.ActualRate != 10.0 {
		t.Errorf("expected actual_rate = 10.0, got %f", result.ActualRate)
	}
	if !result.WasClamped {
		t.Errorf("expected was_clamped = true, got false")
	}
	if result.ClampingReason != "mode_min" {
		t.Errorf("expected clamping_reason = mode_min, got %s", result.ClampingReason)
	}
}

// TestFuelRateLimiter_StartupModeUpperBound verifies that in startup mode,
// a fuel rate above STARTUP_MAX_RATE (50.0 L/h) is clamped to the maximum.
// Requirement-based: behavior rule "Clamp requested_rate to range [STARTUP_MIN_RATE, STARTUP_MAX_RATE]"
func TestFuelRateLimiter_StartupModeUpperBound(t *testing.T) {
	// Arrange
	limiter := NewFuelRateLimiter()
	requestedRate := 75.0 // above STARTUP_MAX_RATE (50.0)

	// Act
	result := limiter.Limit(requestedRate, "startup", 100)

	// Assert
	if result.ActualRate != 50.0 {
		t.Errorf("expected actual_rate = 50.0, got %f", result.ActualRate)
	}
	if !result.WasClamped {
		t.Errorf("expected was_clamped = true, got false")
	}
	if result.ClampingReason != "mode_max" {
		t.Errorf("expected clamping_reason = mode_max, got %s", result.ClampingReason)
	}
}

// TestFuelRateLimiter_StartupModeNominal verifies that in startup mode,
// a fuel rate within [STARTUP_MIN_RATE, STARTUP_MAX_RATE] passes through unclamped.
// Equivalence class: valid startup rate
// Boundary value: nominal value (midpoint of range)
func TestFuelRateLimiter_StartupModeNominal(t *testing.T) {
	// Arrange
	limiter := NewFuelRateLimiter()
	requestedRate := 30.0 // within [10.0, 50.0]

	// Act
	result := limiter.Limit(requestedRate, "startup", 100)

	// Assert
	if result.ActualRate != 30.0 {
		t.Errorf("expected actual_rate = 30.0, got %f", result.ActualRate)
	}
	if result.WasClamped {
		t.Errorf("expected was_clamped = false, got true")
	}
	if result.ClampingReason != "none" {
		t.Errorf("expected clamping_reason = none, got %s", result.ClampingReason)
	}
}

// TestFuelRateLimiter_StartupModeAtMinBoundary verifies that in startup mode,
// a fuel rate exactly at STARTUP_MIN_RATE (10.0) is valid and unclamped.
// Boundary value: at minimum (inclusive)
func TestFuelRateLimiter_StartupModeAtMinBoundary(t *testing.T) {
	// Arrange
	limiter := NewFuelRateLimiter()
	requestedRate := 10.0 // exactly STARTUP_MIN_RATE

	// Act
	result := limiter.Limit(requestedRate, "startup", 100)

	// Assert
	if result.ActualRate != 10.0 {
		t.Errorf("expected actual_rate = 10.0, got %f", result.ActualRate)
	}
	if result.WasClamped {
		t.Errorf("expected was_clamped = false, got true")
	}
	if result.ClampingReason != "none" {
		t.Errorf("expected clamping_reason = none, got %s", result.ClampingReason)
	}
}

// TestFuelRateLimiter_StartupModeAtMaxBoundary verifies that in startup mode,
// a fuel rate exactly at STARTUP_MAX_RATE (50.0) is valid and unclamped.
// Boundary value: at maximum (inclusive)
func TestFuelRateLimiter_StartupModeAtMaxBoundary(t *testing.T) {
	// Arrange
	limiter := NewFuelRateLimiter()
	requestedRate := 50.0 // exactly STARTUP_MAX_RATE

	// Act
	result := limiter.Limit(requestedRate, "startup", 100)

	// Assert
	if result.ActualRate != 50.0 {
		t.Errorf("expected actual_rate = 50.0, got %f", result.ActualRate)
	}
	if result.WasClamped {
		t.Errorf("expected was_clamped = false, got true")
	}
	if result.ClampingReason != "none" {
		t.Errorf("expected clamping_reason = none, got %s", result.ClampingReason)
	}
}

// TestFuelRateLimiter_CruiseModeLowerBound verifies that in cruise mode,
// a fuel rate of zero is valid (lower bound is 0.0).
// Boundary value: at minimum (zero)
func TestFuelRateLimiter_CruiseModeLowerBound(t *testing.T) {
	// Arrange
	limiter := NewFuelRateLimiter()
	requestedRate := 0.0 // at CRUISE minimum

	// Act
	result := limiter.Limit(requestedRate, "cruise", 100)

	// Assert
	if result.ActualRate != 0.0 {
		t.Errorf("expected actual_rate = 0.0, got %f", result.ActualRate)
	}
	if result.WasClamped {
		t.Errorf("expected was_clamped = false, got true")
	}
	if result.ClampingReason != "none" {
		t.Errorf("expected clamping_reason = none, got %s", result.ClampingReason)
	}
}

// TestFuelRateLimiter_CruiseModeUpperBound verifies that in cruise mode,
// a fuel rate above CRUISE_MAX_RATE (200.0) is clamped to the maximum.
// Requirement-based: behavior rule "Clamp requested_rate to range [0.0, CRUISE_MAX_RATE]"
// Boundary value: above maximum
func TestFuelRateLimiter_CruiseModeUpperBound(t *testing.T) {
	// Arrange
	limiter := NewFuelRateLimiter()
	requestedRate := 250.0 // above CRUISE_MAX_RATE (200.0)

	// Act
	result := limiter.Limit(requestedRate, "cruise", 100)

	// Assert
	if result.ActualRate != 200.0 {
		t.Errorf("expected actual_rate = 200.0, got %f", result.ActualRate)
	}
	if !result.WasClamped {
		t.Errorf("expected was_clamped = true, got false")
	}
	if result.ClampingReason != "mode_max" {
		t.Errorf("expected clamping_reason = mode_max, got %s", result.ClampingReason)
	}
}

// TestFuelRateLimiter_CruiseModeNominal verifies that in cruise mode,
// a fuel rate within [0.0, CRUISE_MAX_RATE] with nominal elapsed time
// passes through unclamped (no rate-of-change violation).
// Equivalence class: valid cruise rate with sufficient elapsed time
func TestFuelRateLimiter_CruiseModeNominal(t *testing.T) {
	// Arrange
	limiter := NewFuelRateLimiter()
	requestedRate := 100.0 // within [0.0, 200.0]

	// Act
	result := limiter.Limit(requestedRate, "cruise", 100) // first call, no rate-of-change limit applies

	// Assert
	if result.ActualRate != 100.0 {
		t.Errorf("expected actual_rate = 100.0, got %f", result.ActualRate)
	}
	if result.WasClamped {
		t.Errorf("expected was_clamped = false, got true")
	}
	if result.ClampingReason != "none" {
		t.Errorf("expected clamping_reason = none, got %s", result.ClampingReason)
	}
}

// TestFuelRateLimiter_CruiseModeRateOfChangeViolation verifies that in cruise mode,
// when the rate of change exceeds MAX_RATE_CHANGE (100 L/h/s), it is limited.
// Example: previous_rate = 50, requested = 200, elapsed = 1000ms (1s).
// Allowed change = 100 * 1 = 100 L/h. Clamped to 50 + 100 = 150.
// Requirement-based: behavior rule "Apply rate-of-change limit"
// Boundary value: rate-of-change at limit
func TestFuelRateLimiter_CruiseModeRateOfChangeViolation(t *testing.T) {
	// Arrange
	limiter := NewFuelRateLimiter()
	// First call to establish previous_rate
	limiter.Limit(50.0, "cruise", 100)
	// Second call: attempt to jump from 50 to 200 in 1 second
	requestedRate := 200.0
	elapsedMs := int64(1000) // 1 second

	// Act
	result := limiter.Limit(requestedRate, "cruise", elapsedMs)

	// Assert
	// With MAX_RATE_CHANGE = 100 L/h/s and 1 second elapsed,
	// allowed change is 100 L/h. Previous was 50, so clamped to 50 + 100 = 150.
	if result.ActualRate != 150.0 {
		t.Errorf("expected actual_rate = 150.0 (rate-of-change limited), got %f", result.ActualRate)
	}
	if !result.WasClamped {
		t.Errorf("expected was_clamped = true, got false")
	}
	if result.ClampingReason != "rate_of_change" {
		t.Errorf("expected clamping_reason = rate_of_change, got %s", result.ClampingReason)
	}
}

// TestFuelRateLimiter_CruiseModeRateOfChangeWithinLimit verifies that in cruise mode,
// when the rate of change is within MAX_RATE_CHANGE, no clamping is applied.
// Example: previous_rate = 50, requested = 150, elapsed = 1000ms.
// Allowed change = 100, actual change = 100. Within limit.
// Boundary value: rate-of-change exactly at limit
func TestFuelRateLimiter_CruiseModeRateOfChangeWithinLimit(t *testing.T) {
	// Arrange
	limiter := NewFuelRateLimiter()
	// First call to establish previous_rate
	limiter.Limit(50.0, "cruise", 100)
	// Second call: increase from 50 to 150 in 1 second (change of 100, exactly at limit)
	requestedRate := 150.0
	elapsedMs := int64(1000) // 1 second

	// Act
	result := limiter.Limit(requestedRate, "cruise", elapsedMs)

	// Assert
	if result.ActualRate != 150.0 {
		t.Errorf("expected actual_rate = 150.0, got %f", result.ActualRate)
	}
	if result.WasClamped {
		t.Errorf("expected was_clamped = false, got true")
	}
	if result.ClampingReason != "none" {
		t.Errorf("expected clamping_reason = none, got %s", result.ClampingReason)
	}
}

// TestFuelRateLimiter_CruiseModeRateOfChangeDecreasing verifies that rate-of-change
// limiting applies symmetrically to decreasing rates.
// Example: previous_rate = 200, requested = 0, elapsed = 1000ms (1s).
// Allowed change = 100. Clamped to 200 - 100 = 100.
// Requirement-based: behavior rule "Apply rate-of-change limit" (abs value)
func TestFuelRateLimiter_CruiseModeRateOfChangeDecreasing(t *testing.T) {
	// Arrange
	limiter := NewFuelRateLimiter()
	// First call to establish previous_rate
	limiter.Limit(200.0, "cruise", 100)
	// Second call: attempt to drop from 200 to 0 in 1 second
	requestedRate := 0.0
	elapsedMs := int64(1000) // 1 second

	// Act
	result := limiter.Limit(requestedRate, "cruise", elapsedMs)

	// Assert
	// With MAX_RATE_CHANGE = 100 L/h/s and 1 second elapsed,
	// allowed change is 100. Previous was 200, so clamped to 200 - 100 = 100.
	if result.ActualRate != 100.0 {
		t.Errorf("expected actual_rate = 100.0 (rate-of-change limited), got %f", result.ActualRate)
	}
	if !result.WasClamped {
		t.Errorf("expected was_clamped = true, got false")
	}
	if result.ClampingReason != "rate_of_change" {
		t.Errorf("expected clamping_reason = rate_of_change, got %s", result.ClampingReason)
	}
}

// TestFuelRateLimiter_CruiseModeShortElapsedTime verifies that rate-of-change limiting
// scales with elapsed time. Less elapsed time = smaller allowed change.
// Example: previous_rate = 100, requested = 200, elapsed = 100ms (0.1s).
// Allowed change = 100 * 0.1 = 10. Clamped to 100 + 10 = 110.
// Boundary value: very short elapsed time
func TestFuelRateLimiter_CruiseModeShortElapsedTime(t *testing.T) {
	// Arrange
	limiter := NewFuelRateLimiter()
	// First call to establish previous_rate
	limiter.Limit(100.0, "cruise", 100)
	// Second call: only 100ms elapsed, limited rate change
	requestedRate := 200.0
	elapsedMs := int64(100) // 0.1 seconds

	// Act
	result := limiter.Limit(requestedRate, "cruise", elapsedMs)

	// Assert
	// With MAX_RATE_CHANGE = 100 L/h/s and 0.1 second elapsed,
	// allowed change is 10. Previous was 100, so clamped to 100 + 10 = 110.
	if result.ActualRate != 110.0 {
		t.Errorf("expected actual_rate = 110.0, got %f", result.ActualRate)
	}
	if !result.WasClamped {
		t.Errorf("expected was_clamped = true, got false")
	}
	if result.ClampingReason != "rate_of_change" {
		t.Errorf("expected clamping_reason = rate_of_change, got %s", result.ClampingReason)
	}
}

// TestFuelRateLimiter_CruiseModeZeroElapsedTime verifies that when elapsed_time_ms is zero,
// no rate change is allowed. The actual rate remains at previous_rate.
// Error handling: "elapsed_time_ms is negative" → treat as 0, skip rate-of-change limiting
// Boundary value: zero elapsed time
func TestFuelRateLimiter_CruiseModeZeroElapsedTime(t *testing.T) {
	// Arrange
	limiter := NewFuelRateLimiter()
	// First call to establish previous_rate
	limiter.Limit(100.0, "cruise", 100)
	// Second call: zero elapsed time
	requestedRate := 150.0
	elapsedMs := int64(0)

	// Act
	result := limiter.Limit(requestedRate, "cruise", elapsedMs)

	// Assert
	// With zero elapsed time, allowed change is 0. Rate must remain at 100.
	if result.ActualRate != 100.0 {
		t.Errorf("expected actual_rate = 100.0 (no change allowed), got %f", result.ActualRate)
	}
	if !result.WasClamped {
		t.Errorf("expected was_clamped = true, got false")
	}
	if result.ClampingReason != "rate_of_change" {
		t.Errorf("expected clamping_reason = rate_of_change, got %s", result.ClampingReason)
	}
}

// TestFuelRateLimiter_EmergencyShutdown verifies that in emergency_shutdown mode,
// the actual rate is always set to 0.0 regardless of requested_rate.
// Requirement-based: behavior rule "Set actual_rate to 0.0 regardless of requested_rate"
func TestFuelRateLimiter_EmergencyShutdown(t *testing.T) {
	// Arrange
	limiter := NewFuelRateLimiter()
	requestedRate := 500.0 // arbitrarily high

	// Act
	result := limiter.Limit(requestedRate, "emergency_shutdown", 100)

	// Assert
	if result.ActualRate != 0.0 {
		t.Errorf("expected actual_rate = 0.0, got %f", result.ActualRate)
	}
	if !result.WasClamped {
		t.Errorf("expected was_clamped = true, got false")
	}
	if result.ClampingReason != "emergency" {
		t.Errorf("expected clamping_reason = emergency, got %s", result.ClampingReason)
	}
}

// TestFuelRateLimiter_EmergencyShutdownWithZeroRequest verifies that in emergency_shutdown
// mode, actual rate is 0.0 even if requested_rate is already zero.
// Equivalence class: emergency shutdown mode (always zero)
func TestFuelRateLimiter_EmergencyShutdownWithZeroRequest(t *testing.T) {
	// Arrange
	limiter := NewFuelRateLimiter()
	requestedRate := 0.0

	// Act
	result := limiter.Limit(requestedRate, "emergency_shutdown", 100)

	// Assert
	if result.ActualRate != 0.0 {
		t.Errorf("expected actual_rate = 0.0, got %f", result.ActualRate)
	}
	if result.WasClamped {
		t.Errorf("expected was_clamped = false when requested is already zero, got true")
	}
	if result.ClampingReason != "none" {
		t.Errorf("expected clamping_reason = none, got %s", result.ClampingReason)
	}
}

// TestFuelRateLimiter_NegativeRequestedRate verifies that a negative requested_rate
// is treated as 0.0 and flagged as clamped.
// Error handling: "requested_rate is negative"
// Boundary value: below minimum (negative)
func TestFuelRateLimiter_NegativeRequestedRate(t *testing.T) {
	// Arrange
	limiter := NewFuelRateLimiter()
	requestedRate := -10.0 // negative

	// Act
	result := limiter.Limit(requestedRate, "startup", 100)

	// Assert
	if result.ActualRate != 10.0 {
		t.Errorf("expected actual_rate = 10.0 (startup min, since negative treated as 0), got %f", result.ActualRate)
	}
	if !result.WasClamped {
		t.Errorf("expected was_clamped = true, got false")
	}
	if result.ClampingReason != "mode_min" {
		t.Errorf("expected clamping_reason = mode_min, got %s", result.ClampingReason)
	}
}

// TestFuelRateLimiter_NegativeElapsedTime verifies that a negative elapsed_time_ms
// is treated as 0 and rate-of-change limiting is skipped.
// Error handling: "elapsed_time_ms is negative" → treat as 0, skip rate-of-change limiting
func TestFuelRateLimiter_NegativeElapsedTime(t *testing.T) {
	// Arrange
	limiter := NewFuelRateLimiter()
	// First call to establish previous_rate
	limiter.Limit(100.0, "cruise", 100)
	// Second call: negative elapsed time
	requestedRate := 150.0
	elapsedMs := int64(-500) // negative

	// Act
	result := limiter.Limit(requestedRate, "cruise", elapsedMs)

	// Assert
	// Design says: "Treat as 0, skip rate-of-change limiting for this call"
	// This should mean rate-of-change limit is NOT applied, so the rate changes freely.
	// However, this is ambiguous — does "skip" mean bypass all rate limiting (allowing 150)?
	// Or does it mean treat elapsed as 0 (allowing no change, staying at 100)?
	// Interpreting as: treat elapsed as 0 → zero elapsed = no change allowed.
	if result.ActualRate != 100.0 {
		t.Errorf("expected actual_rate = 100.0 (negative elapsed treated as 0, no change), got %f", result.ActualRate)
	}
	if !result.WasClamped {
		t.Errorf("expected was_clamped = true, got false")
	}
	if result.ClampingReason != "rate_of_change" {
		t.Errorf("expected clamping_reason = rate_of_change, got %s", result.ClampingReason)
	}
}

// TestFuelRateLimiter_UnrecognizedMode verifies that an unrecognized operational mode
// defaults to emergency_shutdown (fail-safe).
// Error handling: "operational_mode is not a recognized value" → treat as emergency_shutdown
func TestFuelRateLimiter_UnrecognizedMode(t *testing.T) {
	// Arrange
	limiter := NewFuelRateLimiter()
	requestedRate := 100.0
	invalidMode := "unknown_mode"

	// Act
	result := limiter.Limit(requestedRate, invalidMode, 100)

	// Assert
	if result.ActualRate != 0.0 {
		t.Errorf("expected actual_rate = 0.0 (fail-safe emergency_shutdown), got %f", result.ActualRate)
	}
	if !result.WasClamped {
		t.Errorf("expected was_clamped = true, got false")
	}
	if result.ClampingReason != "emergency" {
		t.Errorf("expected clamping_reason = emergency, got %s", result.ClampingReason)
	}
}

// TestFuelRateLimiter_WasClampedRule verifies that was_clamped is true if and only if
// actual_rate differs from requested_rate.
// Requirement-based: behavior rule "was_clamped is true if and only if actual_rate != requested_rate"
func TestFuelRateLimiter_WasClampedRule(t *testing.T) {
	// Arrange & Act & Assert: Test unclamped case (no difference)
	limiter1 := NewFuelRateLimiter()
	result1 := limiter1.Limit(30.0, "startup", 100)
	if result1.WasClamped != (result1.ActualRate != 30.0) {
		t.Errorf("was_clamped rule violated: actual=%f requested=30.0 was_clamped=%v",
			result1.ActualRate, result1.WasClamped)
	}

	// Arrange & Act & Assert: Test clamped case (difference exists)
	limiter2 := NewFuelRateLimiter()
	result2 := limiter2.Limit(75.0, "startup", 100)
	if result2.WasClamped != (result2.ActualRate != 75.0) {
		t.Errorf("was_clamped rule violated: actual=%f requested=75.0 was_clamped=%v",
			result2.ActualRate, result2.WasClamped)
	}
}

// TestFuelRateLimiter_StatePreservation verifies that the limiter correctly maintains
// state (previous_rate) across multiple calls for rate-of-change limiting.
// Internal state: previous_rate is updated and used on subsequent calls.
func TestFuelRateLimiter_StatePreservation(t *testing.T) {
	// Arrange
	limiter := NewFuelRateLimiter()
	elapsedMs := int64(1000) // sufficient time for rate changes

	// Act & Assert: Call 1 — establish state
	result1 := limiter.Limit(50.0, "cruise", elapsedMs)
	if result1.ActualRate != 50.0 {
		t.Errorf("call 1: expected 50.0, got %f", result1.ActualRate)
	}

	// Act & Assert: Call 2 — within rate-of-change limit
	result2 := limiter.Limit(100.0, "cruise", elapsedMs)
	if result2.ActualRate != 100.0 {
		t.Errorf("call 2: expected 100.0, got %f", result2.ActualRate)
	}

	// Act & Assert: Call 3 — state reflects call 2's actual_rate (100), not requested
	result3 := limiter.Limit(250.0, "cruise", elapsedMs)
	// From 100, max change in 1s is 100, so clamped to 100 + 100 = 200.
	if result3.ActualRate != 200.0 {
		t.Errorf("call 3: expected 200.0 (limited from 100), got %f", result3.ActualRate)
	}
}

// TestFuelRateLimiter_StatePreservationOnModeSwitch verifies that state is preserved
// even when switching operational modes.
// Internal state: previous_rate persists across mode changes.
func TestFuelRateLimiter_StatePreservationOnModeSwitch(t *testing.T) {
	// Arrange
	limiter := NewFuelRateLimiter()

	// Act & Assert: Call 1 in startup mode
	result1 := limiter.Limit(40.0, "startup", 1000)
	if result1.ActualRate != 40.0 {
		t.Errorf("startup call: expected 40.0, got %f", result1.ActualRate)
	}

	// Act & Assert: Switch to cruise mode. If state is preserved, previous_rate = 40.0.
	// Request 200 in 1 second. Allowed change = 100. Should be clamped to 140.
	result2 := limiter.Limit(200.0, "cruise", 1000)
	if result2.ActualRate != 140.0 {
		t.Errorf("cruise after startup: expected 140.0 (40 + 100 limit), got %f", result2.ActualRate)
	}
	if result2.ClampingReason != "rate_of_change" {
		t.Errorf("cruise after startup: expected rate_of_change reason, got %s", result2.ClampingReason)
	}
}

// TestFuelRateLimiter_FirstCallNoRateOfChangeLimiting verifies that on the first call
// (or when state is uninitialized), rate-of-change limiting does not apply.
// The limiter should accept the requested rate (subject to mode limits).
// Internal state: previous_rate starts at an initial value; rate-of-change only applies on subsequent calls.
func TestFuelRateLimiter_FirstCallNoRateOfChangeLimiting(t *testing.T) {
	// Arrange
	limiter := NewFuelRateLimiter()
	// Requesting a very high rate on the first call
	requestedRate := 500.0

	// Act
	result := limiter.Limit(requestedRate, "cruise", 100)

	// Assert
	// In cruise mode, max is 200, so it should be clamped by mode limit, not rate-of-change.
	if result.ActualRate != 200.0 {
		t.Errorf("first call cruise: expected 200.0 (mode_max), got %f", result.ActualRate)
	}
	if result.ClampingReason != "mode_max" {
		t.Errorf("first call cruise: expected mode_max reason, got %s", result.ClampingReason)
	}
}

// TestFuelRateLimiter_ActualRateAlwaysNonNegative verifies that the output actual_rate
// is always >= 0.0 per the output constraint.
// Output constraint: actual_rate >= 0.0, <= 500.0
func TestFuelRateLimiter_ActualRateAlwaysNonNegative(t *testing.T) {
	// Arrange & Act & Assert: Multiple scenarios
	tests := []struct {
		name     string
		rate     float64
		mode     string
		elapsed  int64
		expected float64
	}{
		{"negative rate in startup", -5.0, "startup", 100, 10.0},
		{"zero rate in cruise", 0.0, "cruise", 100, 0.0},
		{"emergency shutdown", 150.0, "emergency_shutdown", 100, 0.0},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			limiter := NewFuelRateLimiter()
			result := limiter.Limit(tt.rate, tt.mode, tt.elapsed)
			if result.ActualRate < 0.0 {
				t.Errorf("expected actual_rate >= 0.0, got %f", result.ActualRate)
			}
			if result.ActualRate != tt.expected {
				t.Errorf("expected %f, got %f", tt.expected, result.ActualRate)
			}
		})
	}
}

// TestFuelRateLimiter_ActualRateWithinOutputBounds verifies that actual_rate
// never exceeds the output constraint maximum (500.0).
// Output constraint: actual_rate <= 500.0
func TestFuelRateLimiter_ActualRateWithinOutputBounds(t *testing.T) {
	// Arrange
	limiter := NewFuelRateLimiter()
	excessiveRate := 1000.0

	// Act
	result := limiter.Limit(excessiveRate, "cruise", 100)

	// Assert
	if result.ActualRate > 500.0 {
		t.Errorf("expected actual_rate <= 500.0, got %f", result.ActualRate)
	}
	// In cruise mode, max is 200, so this should be clamped to 200.
	if result.ActualRate != 200.0 {
		t.Errorf("expected cruise mode clamp to 200.0, got %f", result.ActualRate)
	}
}
