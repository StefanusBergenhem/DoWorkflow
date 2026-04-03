package fuel

import (
	"testing"
)

// TestStartupModeMinClamping tests that requested_rate below STARTUP_MIN_RATE is clamped up
func TestStartupModeMinClamping(t *testing.T) {
	limiter := NewFuelRateLimiter()
	limiter.SetMode(ModeStartup)

	// Request 5.0 L/h, below STARTUP_MIN_RATE (10.0)
	actualRate, wasClamped, reason := limiter.Apply(5.0, ModeStartup, 100)

	if actualRate != 10.0 {
		t.Errorf("expected actual_rate 10.0, got %f", actualRate)
	}
	if !wasClamped {
		t.Errorf("expected was_clamped true, got false")
	}
	if reason != ReasonModeMin {
		t.Errorf("expected clamping_reason mode_min, got %v", reason)
	}
}

// TestStartupModeMaxClamping tests that requested_rate above STARTUP_MAX_RATE is clamped down
func TestStartupModeMaxClamping(t *testing.T) {
	limiter := NewFuelRateLimiter()
	limiter.SetMode(ModeStartup)

	// Request 75.0 L/h, above STARTUP_MAX_RATE (50.0)
	actualRate, wasClamped, reason := limiter.Apply(75.0, ModeStartup, 100)

	if actualRate != 50.0 {
		t.Errorf("expected actual_rate 50.0, got %f", actualRate)
	}
	if !wasClamped {
		t.Errorf("expected was_clamped true, got false")
	}
	if reason != ReasonModeMax {
		t.Errorf("expected clamping_reason mode_max, got %v", reason)
	}
}

// TestStartupModeNoClamping tests that requested_rate within bounds is not clamped
func TestStartupModeNoClamping(t *testing.T) {
	limiter := NewFuelRateLimiter()
	limiter.SetMode(ModeStartup)

	// Request 30.0 L/h, within [10.0, 50.0]
	actualRate, wasClamped, reason := limiter.Apply(30.0, ModeStartup, 100)

	if actualRate != 30.0 {
		t.Errorf("expected actual_rate 30.0, got %f", actualRate)
	}
	if wasClamped {
		t.Errorf("expected was_clamped false, got true")
	}
	if reason != ReasonNone {
		t.Errorf("expected clamping_reason none, got %v", reason)
	}
}

// TestCruiseModeMaxClamping tests that requested_rate above CRUISE_MAX_RATE is clamped down
func TestCruiseModeMaxClamping(t *testing.T) {
	limiter := NewFuelRateLimiter()
	limiter.SetMode(ModeCruise)

	// Request 250.0 L/h, above CRUISE_MAX_RATE (200.0)
	actualRate, wasClamped, reason := limiter.Apply(250.0, ModeCruise, 100)

	if actualRate != 200.0 {
		t.Errorf("expected actual_rate 200.0, got %f", actualRate)
	}
	if !wasClamped {
		t.Errorf("expected was_clamped true, got false")
	}
	if reason != ReasonModeMax {
		t.Errorf("expected clamping_reason mode_max, got %v", reason)
	}
}

// TestCruiseModeNegativeAllowed tests that 0.0 is allowed in cruise mode
func TestCruiseModeNegativeAllowed(t *testing.T) {
	limiter := NewFuelRateLimiter()
	limiter.SetMode(ModeCruise)

	// Request 0.0 L/h, should be allowed
	actualRate, wasClamped, reason := limiter.Apply(0.0, ModeCruise, 100)

	if actualRate != 0.0 {
		t.Errorf("expected actual_rate 0.0, got %f", actualRate)
	}
	if wasClamped {
		t.Errorf("expected was_clamped false, got true")
	}
	if reason != ReasonNone {
		t.Errorf("expected clamping_reason none, got %v", reason)
	}
}

// TestCruiseModeRateOfChangeLimit tests that rate-of-change is limited in cruise mode
// MAX_RATE_CHANGE = 100.0 L/h/s, so max delta in 100ms = 10.0 L/h
func TestCruiseModeRateOfChangeLimit(t *testing.T) {
	limiter := NewFuelRateLimiter()

	// First call: establish previous_rate at 100.0 L/h
	actualRate1, _, _ := limiter.Apply(100.0, ModeCruise, 100)
	if actualRate1 != 100.0 {
		t.Fatalf("expected first call to return 100.0, got %f", actualRate1)
	}

	// Second call: request 150.0 L/h (delta = 50.0), but elapsed_time_ms = 100
	// Max allowed delta = 100.0 L/h/s * 0.1s = 10.0 L/h
	// So actual should be clamped to 100.0 + 10.0 = 110.0
	actualRate2, wasClamped, reason := limiter.Apply(150.0, ModeCruise, 100)

	if actualRate2 != 110.0 {
		t.Errorf("expected actual_rate 110.0, got %f", actualRate2)
	}
	if !wasClamped {
		t.Errorf("expected was_clamped true, got false")
	}
	if reason != ReasonRateOfChange {
		t.Errorf("expected clamping_reason rate_of_change, got %v", reason)
	}
}

// TestCruiseModeRateOfChangeDecreasing tests that rate-of-change limit applies when decreasing
func TestCruiseModeRateOfChangeDecreasing(t *testing.T) {
	limiter := NewFuelRateLimiter()

	// First call: establish previous_rate at 150.0 L/h
	actualRate1, _, _ := limiter.Apply(150.0, ModeCruise, 100)
	if actualRate1 != 150.0 {
		t.Fatalf("expected first call to return 150.0, got %f", actualRate1)
	}

	// Second call: request 50.0 L/h (delta = -100.0), but elapsed_time_ms = 100
	// Max allowed delta = 100.0 L/h/s * 0.1s = 10.0 L/h
	// So actual should be clamped to 150.0 - 10.0 = 140.0
	actualRate2, wasClamped, reason := limiter.Apply(50.0, ModeCruise, 100)

	if actualRate2 != 140.0 {
		t.Errorf("expected actual_rate 140.0, got %f", actualRate2)
	}
	if !wasClamped {
		t.Errorf("expected was_clamped true, got false")
	}
	if reason != ReasonRateOfChange {
		t.Errorf("expected clamping_reason rate_of_change, got %v", reason)
	}
}

// TestCruiseModeRateOfChangeWithinLimit tests that no clamping occurs when rate-of-change is within limit
func TestCruiseModeRateOfChangeWithinLimit(t *testing.T) {
	limiter := NewFuelRateLimiter()

	// First call: establish previous_rate at 100.0 L/h
	actualRate1, _, _ := limiter.Apply(100.0, ModeCruise, 100)
	if actualRate1 != 100.0 {
		t.Fatalf("expected first call to return 100.0, got %f", actualRate1)
	}

	// Second call: request 105.0 L/h (delta = 5.0), elapsed_time_ms = 100
	// Max allowed delta = 100.0 L/h/s * 0.1s = 10.0 L/h
	// Since 5.0 <= 10.0, should not clamp
	actualRate2, wasClamped, reason := limiter.Apply(105.0, ModeCruise, 100)

	if actualRate2 != 105.0 {
		t.Errorf("expected actual_rate 105.0, got %f", actualRate2)
	}
	if wasClamped {
		t.Errorf("expected was_clamped false, got true")
	}
	if reason != ReasonNone {
		t.Errorf("expected clamping_reason none, got %v", reason)
	}
}

// TestCruiseModeRateOfChangeWithLongerElapsedTime tests rate-of-change with larger time delta
func TestCruiseModeRateOfChangeWithLongerElapsedTime(t *testing.T) {
	limiter := NewFuelRateLimiter()

	// First call: establish previous_rate at 100.0 L/h
	actualRate1, _, _ := limiter.Apply(100.0, ModeCruise, 100)
	if actualRate1 != 100.0 {
		t.Fatalf("expected first call to return 100.0, got %f", actualRate1)
	}

	// Second call: request 250.0 L/h (delta = 150.0), elapsed_time_ms = 1000 (1 second)
	// Max allowed delta = 100.0 L/h/s * 1.0s = 100.0 L/h
	// So actual should be clamped to 100.0 + 100.0 = 200.0
	actualRate2, wasClamped, reason := limiter.Apply(250.0, ModeCruise, 1000)

	if actualRate2 != 200.0 {
		t.Errorf("expected actual_rate 200.0, got %f", actualRate2)
	}
	if !wasClamped {
		t.Errorf("expected was_clamped true, got false")
	}
	if reason != ReasonRateOfChange {
		t.Errorf("expected clamping_reason rate_of_change, got %v", reason)
	}
}

// TestCruiseModeRateOfChangeZeroTime tests rate-of-change with zero elapsed time (skip limiting)
func TestCruiseModeRateOfChangeZeroTime(t *testing.T) {
	limiter := NewFuelRateLimiter()

	// First call: establish previous_rate at 100.0 L/h
	actualRate1, _, _ := limiter.Apply(100.0, ModeCruise, 100)
	if actualRate1 != 100.0 {
		t.Fatalf("expected first call to return 100.0, got %f", actualRate1)
	}

	// Second call: request 150.0 L/h (delta = 50.0), elapsed_time_ms = 0
	// When elapsed_time_ms = 0, rate-of-change limiting should be skipped
	// But the request is still within mode bounds, so should not clamp
	actualRate2, wasClamped, reason := limiter.Apply(150.0, ModeCruise, 0)

	if actualRate2 != 150.0 {
		t.Errorf("expected actual_rate 150.0, got %f", actualRate2)
	}
	if wasClamped {
		t.Errorf("expected was_clamped false, got true")
	}
	if reason != ReasonNone {
		t.Errorf("expected clamping_reason none, got %v", reason)
	}
}

// TestEmergencyShutdownMode tests that emergency_shutdown always returns 0.0
func TestEmergencyShutdownMode(t *testing.T) {
	limiter := NewFuelRateLimiter()

	// Request any rate in emergency shutdown mode
	actualRate, wasClamped, reason := limiter.Apply(100.0, ModeEmergencyShutdown, 100)

	if actualRate != 0.0 {
		t.Errorf("expected actual_rate 0.0, got %f", actualRate)
	}
	if !wasClamped {
		t.Errorf("expected was_clamped true, got false")
	}
	if reason != ReasonEmergency {
		t.Errorf("expected clamping_reason emergency, got %v", reason)
	}
}

// TestEmergencyShutdownZeroRequest tests emergency shutdown with zero request
func TestEmergencyShutdownZeroRequest(t *testing.T) {
	limiter := NewFuelRateLimiter()

	actualRate, wasClamped, reason := limiter.Apply(0.0, ModeEmergencyShutdown, 100)

	if actualRate != 0.0 {
		t.Errorf("expected actual_rate 0.0, got %f", actualRate)
	}
	// Edge case: if requested was already 0.0, should we still set was_clamped?
	// Per design: "was_clamped is true if and only if actual_rate != requested_rate"
	// So if both are 0.0, was_clamped should be false. But design says emergency always clamps.
	// Interpreting design as: emergency mode *forces* the rate to 0.0, so was_clamped is true.
	if !wasClamped {
		t.Errorf("expected was_clamped true (emergency forces 0.0), got false")
	}
	if reason != ReasonEmergency {
		t.Errorf("expected clamping_reason emergency, got %v", reason)
	}
}

// TestNegativeRequestedRate tests that negative requested_rate is treated as 0.0
func TestNegativeRequestedRate(t *testing.T) {
	limiter := NewFuelRateLimiter()

	actualRate, wasClamped, reason := limiter.Apply(-5.0, ModeCruise, 100)

	if actualRate != 0.0 {
		t.Errorf("expected actual_rate 0.0, got %f", actualRate)
	}
	if !wasClamped {
		t.Errorf("expected was_clamped true, got false")
	}
	if reason != ReasonModeMin {
		t.Errorf("expected clamping_reason mode_min, got %v", reason)
	}
}

// TestNegativeElapsedTime tests that negative elapsed_time_ms is treated as 0
func TestNegativeElapsedTime(t *testing.T) {
	limiter := NewFuelRateLimiter()

	// First call: establish previous_rate at 100.0 L/h
	actualRate1, _, _ := limiter.Apply(100.0, ModeCruise, 100)
	if actualRate1 != 100.0 {
		t.Fatalf("expected first call to return 100.0, got %f", actualRate1)
	}

	// Second call: request 150.0 L/h, but elapsed_time_ms = -50
	// Negative time should be treated as 0, so rate-of-change limiting is skipped
	// Request should pass through as-is (within mode bounds)
	actualRate2, wasClamped, reason := limiter.Apply(150.0, ModeCruise, -50)

	if actualRate2 != 150.0 {
		t.Errorf("expected actual_rate 150.0, got %f", actualRate2)
	}
	if wasClamped {
		t.Errorf("expected was_clamped false, got true")
	}
	if reason != ReasonNone {
		t.Errorf("expected clamping_reason none, got %v", reason)
	}
}

// TestUnrecognizedMode tests that unrecognized operational_mode is treated as emergency_shutdown
func TestUnrecognizedMode(t *testing.T) {
	limiter := NewFuelRateLimiter()

	// Use an invalid mode value (e.g., 999)
	actualRate, wasClamped, reason := limiter.Apply(100.0, OperationalMode(999), 100)

	if actualRate != 0.0 {
		t.Errorf("expected actual_rate 0.0 (fail-safe), got %f", actualRate)
	}
	if !wasClamped {
		t.Errorf("expected was_clamped true, got false")
	}
	if reason != ReasonEmergency {
		t.Errorf("expected clamping_reason emergency, got %v", reason)
	}
}

// TestWasClampedInvariant tests that was_clamped == (actual_rate != requested_rate)
func TestWasClampedInvariant(t *testing.T) {
	tests := []struct {
		name           string
		mode           OperationalMode
		requested      float64
		elapsedMs      int
		expectedClamped bool
	}{
		{"startup no clamp", ModeStartup, 30.0, 100, false},
		{"startup min clamp", ModeStartup, 5.0, 100, true},
		{"startup max clamp", ModeStartup, 75.0, 100, true},
		{"cruise no clamp", ModeCruise, 50.0, 100, false},
		{"cruise max clamp", ModeCruise, 250.0, 100, true},
		{"emergency", ModeEmergencyShutdown, 100.0, 100, true},
	}

	for _, tc := range tests {
		t.Run(tc.name, func(t *testing.T) {
			limiter := NewFuelRateLimiter()
			actualRate, wasClamped, _ := limiter.Apply(tc.requested, tc.mode, tc.elapsedMs)

			shouldBeClamped := (actualRate != tc.requested)
			if wasClamped != shouldBeClamped {
				t.Errorf("was_clamped=%v but actual(%f) != requested(%f) is %v",
					wasClamped, actualRate, tc.requested, shouldBeClamped)
			}
			if wasClamped != tc.expectedClamped {
				t.Errorf("expected was_clamped %v, got %v", tc.expectedClamped, wasClamped)
			}
		})
	}
}

// TestMultipleCallsPreservePreviousRate tests that state is maintained across multiple calls
func TestMultipleCallsPreservePreviousRate(t *testing.T) {
	limiter := NewFuelRateLimiter()

	// Call 1: 100.0 L/h in cruise
	rate1, _, _ := limiter.Apply(100.0, ModeCruise, 100)
	if rate1 != 100.0 {
		t.Fatalf("call 1: expected 100.0, got %f", rate1)
	}

	// Call 2: request 105.0 L/h (delta=5.0, within limit of 10.0)
	rate2, clamped2, _ := limiter.Apply(105.0, ModeCruise, 100)
	if rate2 != 105.0 || clamped2 {
		t.Fatalf("call 2: expected 105.0 unclamped, got %f clamped=%v", rate2, clamped2)
	}

	// Call 3: request 150.0 L/h (delta=45.0, exceeds limit of 10.0)
	rate3, clamped3, reason3 := limiter.Apply(150.0, ModeCruise, 100)
	if rate3 != 115.0 || !clamped3 || reason3 != ReasonRateOfChange {
		t.Errorf("call 3: expected 115.0 clamped with rate_of_change, got %f clamped=%v reason=%v",
			rate3, clamped3, reason3)
	}

	// Call 4: request 140.0 L/h (delta=25.0 from 115.0, exceeds limit of 10.0)
	rate4, clamped4, reason4 := limiter.Apply(140.0, ModeCruise, 100)
	if rate4 != 125.0 || !clamped4 || reason4 != ReasonRateOfChange {
		t.Errorf("call 4: expected 125.0 clamped with rate_of_change, got %f clamped=%v reason=%v",
			rate4, clamped4, reason4)
	}
}

// TestModeSwitch tests behavior when switching from one mode to another
func TestModeSwitch(t *testing.T) {
	limiter := NewFuelRateLimiter()

	// Call 1: startup mode, 30.0 L/h
	rate1, _, _ := limiter.Apply(30.0, ModeStartup, 100)
	if rate1 != 30.0 {
		t.Fatalf("startup call: expected 30.0, got %f", rate1)
	}

	// Call 2: switch to cruise, request 150.0 L/h
	// This should be within cruise mode max (200.0) and rate-of-change should check against previous (30.0)
	// Max delta in 100ms = 10.0, so should clamp to 40.0
	rate2, clamped2, reason2 := limiter.Apply(150.0, ModeCruise, 100)
	if rate2 != 40.0 || !clamped2 || reason2 != ReasonRateOfChange {
		t.Errorf("cruise call after startup: expected 40.0 clamped with rate_of_change, got %f clamped=%v reason=%v",
			rate2, clamped2, reason2)
	}
}

// TestStartupMinRateZeroRequest tests startup mode with zero request (below minimum)
func TestStartupMinRateZeroRequest(t *testing.T) {
	limiter := NewFuelRateLimiter()

	actualRate, wasClamped, reason := limiter.Apply(0.0, ModeStartup, 100)

	if actualRate != 10.0 {
		t.Errorf("expected actual_rate 10.0 (startup min), got %f", actualRate)
	}
	if !wasClamped {
		t.Errorf("expected was_clamped true, got false")
	}
	if reason != ReasonModeMin {
		t.Errorf("expected clamping_reason mode_min, got %v", reason)
	}
}

// TestCruiseZeroToMax tests full range in cruise mode without rate-of-change
func TestCruiseZeroToMax(t *testing.T) {
	limiter := NewFuelRateLimiter()

	// First request: 0.0 (no previous state)
	rate1, clamped1, _ := limiter.Apply(0.0, ModeCruise, 100)
	if rate1 != 0.0 || clamped1 {
		t.Fatalf("call 1: expected 0.0 unclamped, got %f clamped=%v", rate1, clamped1)
	}

	// Second request: 200.0 (at max, delta=200 but within rate-of-change = 10.0 in 100ms)
	// Should clamp to 0.0 + 10.0 = 10.0
	rate2, clamped2, reason2 := limiter.Apply(200.0, ModeCruise, 100)
	if rate2 != 10.0 || !clamped2 || reason2 != ReasonRateOfChange {
		t.Errorf("call 2: expected 10.0 clamped with rate_of_change, got %f clamped=%v reason=%v",
			rate2, clamped2, reason2)
	}
}

// TestActualRateWithinOutputConstraints tests that actual_rate never exceeds 500.0 L/h (per spec)
func TestActualRateWithinOutputConstraints(t *testing.T) {
	limiter := NewFuelRateLimiter()

	tests := []struct {
		name      string
		mode      OperationalMode
		requested float64
	}{
		{"startup max is < 500", ModeStartup, 500.0},
		{"cruise max is < 500", ModeCruise, 500.0},
		{"emergency is 0", ModeEmergencyShutdown, 500.0},
	}

	for _, tc := range tests {
		t.Run(tc.name, func(t *testing.T) {
			limiter := NewFuelRateLimiter()
			actualRate, _, _ := limiter.Apply(tc.requested, tc.mode, 100)

			if actualRate < 0.0 || actualRate > 500.0 {
				t.Errorf("actual_rate %f is outside [0.0, 500.0]", actualRate)
			}
		})
	}
}

// TestRatOfChangeCalcSmallValues tests rate-of-change with very small delta and time
func TestRateOfChangeCalcSmallValues(t *testing.T) {
	limiter := NewFuelRateLimiter()

	// First call: 100.0 L/h
	rate1, _, _ := limiter.Apply(100.0, ModeCruise, 100)
	if rate1 != 100.0 {
		t.Fatalf("call 1: expected 100.0, got %f", rate1)
	}

	// Second call: request 100.1 L/h (delta=0.1), elapsed=1ms
	// Max allowed delta = 100.0 * 0.001 = 0.1 L/h
	// So 0.1 <= 0.1, should not clamp
	rate2, clamped2, reason2 := limiter.Apply(100.1, ModeCruise, 1)
	if rate2 != 100.1 || clamped2 {
		t.Errorf("call 2: expected 100.1 unclamped, got %f clamped=%v reason=%v",
			rate2, clamped2, reason2)
	}

	// Third call: request 100.2 L/h (delta=0.1 from 100.1), elapsed=1ms
	// Max allowed delta = 100.0 * 0.001 = 0.1 L/h
	// So 0.1 <= 0.1, should not clamp
	rate3, clamped3, _ := limiter.Apply(100.2, ModeCruise, 1)
	if rate3 != 100.2 || clamped3 {
		t.Errorf("call 3: expected 100.2 unclamped, got %f clamped=%v", rate3, clamped3)
	}

	// Fourth call: request 100.4 L/h (delta=0.2 from 100.2), elapsed=1ms
	// Max allowed delta = 100.0 * 0.001 = 0.1 L/h
	// So 0.2 > 0.1, should clamp to 100.3
	rate4, clamped4, reason4 := limiter.Apply(100.4, ModeCruise, 1)
	expectedRate := 100.2 + 0.1
	if rate4 != expectedRate || !clamped4 || reason4 != ReasonRateOfChange {
		t.Errorf("call 4: expected %f clamped with rate_of_change, got %f clamped=%v reason=%v",
			expectedRate, rate4, clamped4, reason4)
	}
}

// TestCruiseModeMaxAndRateOfChangeBoth tests when both max and rate-of-change would clamp
func TestCruiseModeMaxAndRateOfChangeBoth(t *testing.T) {
	limiter := NewFuelRateLimiter()

	// First call: 100.0 L/h
	rate1, _, _ := limiter.Apply(100.0, ModeCruise, 100)
	if rate1 != 100.0 {
		t.Fatalf("call 1: expected 100.0, got %f", rate1)
	}

	// Second call: request 300.0 L/h (exceeds both cruise max 200.0 AND rate-of-change 110.0)
	// If rate-of-change is checked after mode max, it should be clamped to 200.0 (mode_max)
	// If mode max is checked first, result would be 200.0, then rate-of-change would limit to 110.0
	// This tests implementation order: typically constraints are applied in order
	rate2, clamped2, reason2 := limiter.Apply(300.0, ModeCruise, 100)

	// Both constraints would apply, but reason should indicate which was applied
	// Design doesn't specify order; typical flow: mode constraint, then rate constraint
	// If mode is applied first: rate2=200, reason=mode_max (rate-of-change wouldn't apply)
	// If rate is applied first: rate2=110, reason=rate_of_change
	// Assuming implementation checks mode bounds first, then rate:
	if rate2 > 110.0 {
		t.Errorf("expected rate2 <= 110.0 (both constraints), got %f", rate2)
	}
	if !clamped2 {
		t.Errorf("expected was_clamped true, got false")
	}
	// We don't assert a specific reason here since design is ambiguous about constraint order
	_ = reason2
}

// BenchmarkApply benchmarks the Apply method to ensure constant-time execution
func BenchmarkApply(b *testing.B) {
	limiter := NewFuelRateLimiter()

	b.ResetTimer()
	for i := 0; i < b.N; i++ {
		limiter.Apply(100.0, ModeCruise, 100)
	}
}

// BenchmarkApplyVariousRequests benchmarks Apply with varying requests
func BenchmarkApplyVariousRequests(b *testing.B) {
	limiter := NewFuelRateLimiter()
	requests := []float64{0.0, 50.0, 100.0, 150.0, 200.0, 250.0}

	b.ResetTimer()
	for i := 0; i < b.N; i++ {
		req := requests[i%len(requests)]
		limiter.Apply(req, ModeCruise, 100)
	}
}
