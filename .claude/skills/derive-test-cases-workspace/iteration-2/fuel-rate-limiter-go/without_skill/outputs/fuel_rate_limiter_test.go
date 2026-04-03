package main

import (
	"testing"
)

// FuelRateLimiter constants (matches design configuration)
const (
	startupMinRate    = 10.0
	startupMaxRate    = 50.0
	cruiseMaxRate     = 200.0
	maxRateChange     = 100.0 // liters_per_hour_per_second
)

// OperationalMode represents the engine operational mode
type OperationalMode string

const (
	Startup           OperationalMode = "startup"
	Cruise            OperationalMode = "cruise"
	EmergencyShutdown OperationalMode = "emergency_shutdown"
)

// ClampingReason indicates why clamping was applied
type ClampingReason string

const (
	NoneReason        ClampingReason = "none"
	ModeMaxReason     ClampingReason = "mode_max"
	ModeMinReason     ClampingReason = "mode_min"
	RateOfChangeReason ClampingReason = "rate_of_change"
	EmergencyReason   ClampingReason = "emergency"
)

// FuelRateLimiterOutput contains the output of a call to the limiter
type FuelRateLimiterOutput struct {
	ActualRate       float64
	WasClamped       bool
	ClampingReason   ClampingReason
}

// FuelRateLimiter is the unit under test
type FuelRateLimiter struct {
	previousRate float64
}

// NewFuelRateLimiter creates a new FuelRateLimiter instance
func NewFuelRateLimiter() *FuelRateLimiter {
	return &FuelRateLimiter{
		previousRate: 0.0,
	}
}

// Call processes the requested fuel rate with the given mode and elapsed time
func (f *FuelRateLimiter) Call(
	requestedRate float64,
	mode OperationalMode,
	elapsedTimeMs int,
) FuelRateLimiterOutput {
	result := FuelRateLimiterOutput{
		ClampingReason: NoneReason,
	}

	// Handle negative requested_rate: treat as 0.0
	if requestedRate < 0.0 {
		requestedRate = 0.0
		result.WasClamped = true
		result.ClampingReason = ModeMinReason
	}

	// Handle negative elapsed_time_ms: treat as 0
	if elapsedTimeMs < 0 {
		elapsedTimeMs = 0
	}

	// Handle unrecognized mode: fail-safe to emergency shutdown
	if mode != Startup && mode != Cruise && mode != EmergencyShutdown {
		mode = EmergencyShutdown
	}

	// Apply mode-specific behavior
	switch mode {
	case Startup:
		result = f.handleStartup(requestedRate)

	case Cruise:
		result = f.handleCruise(requestedRate, elapsedTimeMs)

	case EmergencyShutdown:
		result.ActualRate = 0.0
		result.WasClamped = true
		result.ClampingReason = EmergencyReason
	}

	// Update previous_rate for next call
	f.previousRate = result.ActualRate

	return result
}

// handleStartup applies startup mode clamping
func (f *FuelRateLimiter) handleStartup(requestedRate float64) FuelRateLimiterOutput {
	result := FuelRateLimiterOutput{
		ActualRate:     requestedRate,
		WasClamped:     false,
		ClampingReason: NoneReason,
	}

	if requestedRate < startupMinRate {
		result.ActualRate = startupMinRate
		result.WasClamped = true
		result.ClampingReason = ModeMinReason
	} else if requestedRate > startupMaxRate {
		result.ActualRate = startupMaxRate
		result.WasClamped = true
		result.ClampingReason = ModeMaxReason
	}

	return result
}

// handleCruise applies cruise mode clamping and rate-of-change limits
func (f *FuelRateLimiter) handleCruise(requestedRate float64, elapsedTimeMs int) FuelRateLimiterOutput {
	result := FuelRateLimiterOutput{
		ActualRate:     requestedRate,
		WasClamped:     false,
		ClampingReason: NoneReason,
	}

	// Apply mode max constraint
	if result.ActualRate > cruiseMaxRate {
		result.ActualRate = cruiseMaxRate
		result.WasClamped = true
		result.ClampingReason = ModeMaxReason
	}

	// Apply rate-of-change limiting
	if elapsedTimeMs > 0 {
		maxAllowedChange := maxRateChange * float64(elapsedTimeMs) / 1000.0
		actualChange := result.ActualRate - f.previousRate

		if actualChange > maxAllowedChange {
			result.ActualRate = f.previousRate + maxAllowedChange
			result.WasClamped = true
			result.ClampingReason = RateOfChangeReason
		} else if actualChange < -maxAllowedChange {
			result.ActualRate = f.previousRate - maxAllowedChange
			result.WasClamped = true
			result.ClampingReason = RateOfChangeReason
		}
	}

	return result
}

// Test cases

// TestStartupModeMinimumRate verifies enforcement of minimum startup rate
func TestStartupModeMinimumRate(t *testing.T) {
	limiter := NewFuelRateLimiter()

	tests := []struct {
		name          string
		requestedRate float64
		expectedRate  float64
		expectedClamped bool
		expectedReason ClampingReason
	}{
		{
			name:            "Below minimum, clamp to min",
			requestedRate:   5.0,
			expectedRate:    startupMinRate,
			expectedClamped: true,
			expectedReason:  ModeMinReason,
		},
		{
			name:            "At minimum, no clamping",
			requestedRate:   startupMinRate,
			expectedRate:    startupMinRate,
			expectedClamped: false,
			expectedReason:  NoneReason,
		},
		{
			name:            "Zero rate, clamp to min",
			requestedRate:   0.0,
			expectedRate:    startupMinRate,
			expectedClamped: true,
			expectedReason:  ModeMinReason,
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			result := limiter.Call(tt.requestedRate, Startup, 100)

			if result.ActualRate != tt.expectedRate {
				t.Errorf("ActualRate = %f, want %f", result.ActualRate, tt.expectedRate)
			}
			if result.WasClamped != tt.expectedClamped {
				t.Errorf("WasClamped = %v, want %v", result.WasClamped, tt.expectedClamped)
			}
			if result.ClampingReason != tt.expectedReason {
				t.Errorf("ClampingReason = %v, want %v", result.ClampingReason, tt.expectedReason)
			}
		})
	}
}

// TestStartupModeMaximumRate verifies enforcement of maximum startup rate
func TestStartupModeMaximumRate(t *testing.T) {
	limiter := NewFuelRateLimiter()

	tests := []struct {
		name          string
		requestedRate float64
		expectedRate  float64
		expectedClamped bool
		expectedReason ClampingReason
	}{
		{
			name:            "Above maximum, clamp to max",
			requestedRate:   60.0,
			expectedRate:    startupMaxRate,
			expectedClamped: true,
			expectedReason:  ModeMaxReason,
		},
		{
			name:            "At maximum, no clamping",
			requestedRate:   startupMaxRate,
			expectedRate:    startupMaxRate,
			expectedClamped: false,
			expectedReason:  NoneReason,
		},
		{
			name:            "Far above maximum, clamp to max",
			requestedRate:   150.0,
			expectedRate:    startupMaxRate,
			expectedClamped: true,
			expectedReason:  ModeMaxReason,
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			result := limiter.Call(tt.requestedRate, Startup, 100)

			if result.ActualRate != tt.expectedRate {
				t.Errorf("ActualRate = %f, want %f", result.ActualRate, tt.expectedRate)
			}
			if result.WasClamped != tt.expectedClamped {
				t.Errorf("WasClamped = %v, want %v", result.WasClamped, tt.expectedClamped)
			}
			if result.ClampingReason != tt.expectedReason {
				t.Errorf("ClampingReason = %v, want %v", result.ClampingReason, tt.expectedReason)
			}
		})
	}
}

// TestStartupModeInRange verifies that in-range requests are not clamped
func TestStartupModeInRange(t *testing.T) {
	limiter := NewFuelRateLimiter()

	tests := []struct {
		name          string
		requestedRate float64
	}{
		{
			name:          "At minimum",
			requestedRate: startupMinRate,
		},
		{
			name:          "In middle of range",
			requestedRate: (startupMinRate + startupMaxRate) / 2,
		},
		{
			name:          "At maximum",
			requestedRate: startupMaxRate,
		},
		{
			name:          "Just below maximum",
			requestedRate: startupMaxRate - 0.1,
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			result := limiter.Call(tt.requestedRate, Startup, 100)

			if result.ActualRate != tt.requestedRate {
				t.Errorf("ActualRate = %f, want %f", result.ActualRate, tt.requestedRate)
			}
			if result.WasClamped != false {
				t.Errorf("WasClamped = %v, want false", result.WasClamped)
			}
			if result.ClampingReason != NoneReason {
				t.Errorf("ClampingReason = %v, want NoneReason", result.ClampingReason)
			}
		})
	}
}

// TestEmergencyShutdownMode verifies emergency mode always forces rate to zero
func TestEmergencyShutdownMode(t *testing.T) {
	tests := []struct {
		name          string
		requestedRate float64
	}{
		{
			name:          "Positive rate becomes zero",
			requestedRate: 100.0,
		},
		{
			name:          "Startup rate becomes zero",
			requestedRate: startupMinRate,
		},
		{
			name:          "Maximum rate becomes zero",
			requestedRate: cruiseMaxRate,
		},
		{
			name:          "Already zero stays zero",
			requestedRate: 0.0,
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			limiter := NewFuelRateLimiter()
			result := limiter.Call(tt.requestedRate, EmergencyShutdown, 100)

			if result.ActualRate != 0.0 {
				t.Errorf("ActualRate = %f, want 0.0", result.ActualRate)
			}
			if result.WasClamped != true {
				t.Errorf("WasClamped = %v, want true", result.WasClamped)
			}
			if result.ClampingReason != EmergencyReason {
				t.Errorf("ClampingReason = %v, want EmergencyReason", result.ClampingReason)
			}
		})
	}
}

// TestCruiseModeMaximumRate verifies cruise mode respects maximum rate constraint
func TestCruiseModeMaximumRate(t *testing.T) {
	limiter := NewFuelRateLimiter()

	tests := []struct {
		name          string
		requestedRate float64
		expectedRate  float64
		expectedClamped bool
		expectedReason ClampingReason
	}{
		{
			name:            "Above cruise max, clamp to max",
			requestedRate:   250.0,
			expectedRate:    cruiseMaxRate,
			expectedClamped: true,
			expectedReason:  ModeMaxReason,
		},
		{
			name:            "At cruise max, no clamping",
			requestedRate:   cruiseMaxRate,
			expectedRate:    cruiseMaxRate,
			expectedClamped: false,
			expectedReason:  NoneReason,
		},
		{
			name:            "Below cruise max, no clamping",
			requestedRate:   100.0,
			expectedRate:    100.0,
			expectedClamped: false,
			expectedReason:  NoneReason,
		},
		{
			name:            "Zero in cruise, no clamping",
			requestedRate:   0.0,
			expectedRate:    0.0,
			expectedClamped: false,
			expectedReason:  NoneReason,
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			result := limiter.Call(tt.requestedRate, Cruise, 100)

			if result.ActualRate != tt.expectedRate {
				t.Errorf("ActualRate = %f, want %f", result.ActualRate, tt.expectedRate)
			}
			if result.WasClamped != tt.expectedClamped {
				t.Errorf("WasClamped = %v, want %v", result.WasClamped, tt.expectedClamped)
			}
			if result.ClampingReason != tt.expectedReason {
				t.Errorf("ClampingReason = %v, want %v", result.ClampingReason, tt.expectedReason)
			}
		})
	}
}

// TestCruiseRateOfChangeIncrease verifies rate-of-change limiting for upward transitions
func TestCruiseRateOfChangeIncrease(t *testing.T) {
	tests := []struct {
		name                string
		previousRate        float64
		requestedRate       float64
		elapsedTimeMs       int
		expectedRate        float64
		expectedClamped     bool
		expectedReason      ClampingReason
	}{
		{
			name:            "Slow increase within limit (100ms, 10 lph/s max)",
			previousRate:    50.0,
			requestedRate:   51.0,
			elapsedTimeMs:   100,
			expectedRate:    51.0,
			expectedClamped: false,
			expectedReason:  NoneReason,
		},
		{
			name:            "Fast increase exceeds limit (100ms, 10 lph/s max, tries 60 -> clamp to 60)",
			previousRate:    50.0,
			requestedRate:   60.0,
			elapsedTimeMs:   100,
			expectedRate:    50.0 + (100.0 * 100.0 / 1000.0), // 50 + 10 = 60
			expectedClamped: true,
			expectedReason:  RateOfChangeReason,
		},
		{
			name:            "Very fast increase with longer time window (500ms)",
			previousRate:    50.0,
			requestedRate:   150.0,
			elapsedTimeMs:   500,
			expectedRate:    50.0 + (100.0 * 500.0 / 1000.0), // 50 + 50 = 100
			expectedClamped: true,
			expectedReason:  RateOfChangeReason,
		},
		{
			name:            "Full second duration, full rate change budget (1000ms)",
			previousRate:    50.0,
			requestedRate:   200.0,
			elapsedTimeMs:   1000,
			expectedRate:    50.0 + 100.0, // Full 100 lph/s * 1 second
			expectedClamped: true,
			expectedReason:  RateOfChangeReason,
		},
		{
			name:            "Increase exactly at limit boundary (100ms, rate change = 10)",
			previousRate:    50.0,
			requestedRate:   50.0 + (100.0 * 100.0 / 1000.0), // Exactly 60
			elapsedTimeMs:   100,
			expectedRate:    60.0,
			expectedClamped: false,
			expectedReason:  NoneReason,
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			limiter := NewFuelRateLimiter()
			// First, set up the previous rate state
			limiter.previousRate = tt.previousRate

			result := limiter.Call(tt.requestedRate, Cruise, tt.elapsedTimeMs)

			const tolerance = 0.001
			if diff := result.ActualRate - tt.expectedRate; diff < -tolerance || diff > tolerance {
				t.Errorf("ActualRate = %f, want %f (diff = %f)", result.ActualRate, tt.expectedRate, diff)
			}
			if result.WasClamped != tt.expectedClamped {
				t.Errorf("WasClamped = %v, want %v", result.WasClamped, tt.expectedClamped)
			}
			if result.ClampingReason != tt.expectedReason {
				t.Errorf("ClampingReason = %v, want %v", result.ClampingReason, tt.expectedReason)
			}
		})
	}
}

// TestCruiseRateOfChangeDecrease verifies rate-of-change limiting for downward transitions
func TestCruiseRateOfChangeDecrease(t *testing.T) {
	tests := []struct {
		name            string
		previousRate    float64
		requestedRate   float64
		elapsedTimeMs   int
		expectedRate    float64
		expectedClamped bool
		expectedReason  ClampingReason
	}{
		{
			name:            "Slow decrease within limit (100ms, 10 lph/s max)",
			previousRate:    100.0,
			requestedRate:   99.0,
			elapsedTimeMs:   100,
			expectedRate:    99.0,
			expectedClamped: false,
			expectedReason:  NoneReason,
		},
		{
			name:            "Fast decrease exceeds limit (100ms)",
			previousRate:    100.0,
			requestedRate:   80.0,
			elapsedTimeMs:   100,
			expectedRate:    100.0 - (100.0 * 100.0 / 1000.0), // 100 - 10 = 90
			expectedClamped: true,
			expectedReason:  RateOfChangeReason,
		},
		{
			name:            "Decrease to zero with time limit (100ms)",
			previousRate:    50.0,
			requestedRate:   0.0,
			elapsedTimeMs:   100,
			expectedRate:    50.0 - (100.0 * 100.0 / 1000.0), // 50 - 10 = 40
			expectedClamped: true,
			expectedReason:  RateOfChangeReason,
		},
		{
			name:            "Rapid decrease over 500ms",
			previousRate:    150.0,
			requestedRate:   0.0,
			elapsedTimeMs:   500,
			expectedRate:    150.0 - (100.0 * 500.0 / 1000.0), // 150 - 50 = 100
			expectedClamped: true,
			expectedReason:  RateOfChangeReason,
		},
		{
			name:            "Decrease exactly at limit boundary",
			previousRate:    100.0,
			requestedRate:   100.0 - (100.0 * 100.0 / 1000.0), // Exactly 90
			elapsedTimeMs:   100,
			expectedRate:    90.0,
			expectedClamped: false,
			expectedReason:  NoneReason,
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			limiter := NewFuelRateLimiter()
			limiter.previousRate = tt.previousRate

			result := limiter.Call(tt.requestedRate, Cruise, tt.elapsedTimeMs)

			const tolerance = 0.001
			if diff := result.ActualRate - tt.expectedRate; diff < -tolerance || diff > tolerance {
				t.Errorf("ActualRate = %f, want %f (diff = %f)", result.ActualRate, tt.expectedRate, diff)
			}
			if result.WasClamped != tt.expectedClamped {
				t.Errorf("WasClamped = %v, want %v", result.WasClamped, tt.expectedClamped)
			}
			if result.ClampingReason != tt.expectedReason {
				t.Errorf("ClampingReason = %v, want %v", result.ClampingReason, tt.expectedReason)
			}
		})
	}
}

// TestNegativeRequestedRate verifies error handling for negative requested rate
func TestNegativeRequestedRate(t *testing.T) {
	limiter := NewFuelRateLimiter()

	result := limiter.Call(-5.0, Cruise, 100)

	if result.ActualRate != 0.0 {
		t.Errorf("ActualRate = %f, want 0.0", result.ActualRate)
	}
	if result.WasClamped != true {
		t.Errorf("WasClamped = %v, want true", result.WasClamped)
	}
	if result.ClampingReason != ModeMinReason {
		t.Errorf("ClampingReason = %v, want ModeMinReason", result.ClampingReason)
	}
}

// TestNegativeElapsedTime verifies error handling for negative elapsed time
func TestNegativeElapsedTime(t *testing.T) {
	tests := []struct {
		name          string
		previousRate  float64
		requestedRate float64
		elapsedTimeMs int
		expectedRate  float64
		expectedClamped bool
	}{
		{
			name:            "Negative elapsed time skips RoC limit (fast increase allowed)",
			previousRate:    50.0,
			requestedRate:   150.0,
			elapsedTimeMs:   -100,
			expectedRate:    150.0, // Would be clamped to 60 with normal RoC limit
			expectedClamped: false,
			// Should not clamp unless exceeds mode max, and 150 < cruiseMaxRate is false, so it clamps to cruiseMaxRate
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			limiter := NewFuelRateLimiter()
			limiter.previousRate = tt.previousRate

			result := limiter.Call(tt.requestedRate, Cruise, tt.elapsedTimeMs)

			// With negative elapsed time, RoC limiting is skipped, but mode max still applies
			expectedRate := tt.requestedRate
			if expectedRate > cruiseMaxRate {
				expectedRate = cruiseMaxRate
			}

			if result.ActualRate != expectedRate {
				t.Errorf("ActualRate = %f, want %f", result.ActualRate, expectedRate)
			}
		})
	}
}

// TestUnrecognizedMode verifies fail-safe behavior for unrecognized modes
func TestUnrecognizedMode(t *testing.T) {
	limiter := NewFuelRateLimiter()

	// Pass an invalid mode string
	result := limiter.Call(100.0, OperationalMode("invalid_mode"), 100)

	// Should fail safe to emergency shutdown
	if result.ActualRate != 0.0 {
		t.Errorf("ActualRate = %f, want 0.0", result.ActualRate)
	}
	if result.WasClamped != true {
		t.Errorf("WasClamped = %v, want true", result.WasClamped)
	}
	if result.ClampingReason != EmergencyReason {
		t.Errorf("ClampingReason = %v, want EmergencyReason", result.ClampingReason)
	}
}

// TestStatefulBehaviorSequence verifies previous_rate is correctly maintained across calls
func TestStatefulBehaviorSequence(t *testing.T) {
	limiter := NewFuelRateLimiter()

	tests := []struct {
		name              string
		calls             []struct {
			requestedRate float64
			mode          OperationalMode
			elapsedTimeMs int
		}
		expectedOutputs []FuelRateLimiterOutput
	}{
		{
			name: "Gradual increase in cruise mode",
			calls: []struct {
				requestedRate float64
				mode          OperationalMode
				elapsedTimeMs int
			}{
				{100.0, Cruise, 100},  // Rate goes 0 -> 100, but limited to 10 (0 + 10)
				{150.0, Cruise, 100},  // Rate goes 10 -> 150, but limited to 20 (10 + 10)
				{200.0, Cruise, 100},  // Rate goes 20 -> 200, but limited to 30 (20 + 10)
				{150.0, Cruise, 100},  // Rate goes 30 -> 150, no limit needed
			},
			expectedOutputs: []FuelRateLimiterOutput{
				{ActualRate: 10.0, WasClamped: true, ClampingReason: RateOfChangeReason},
				{ActualRate: 20.0, WasClamped: true, ClampingReason: RateOfChangeReason},
				{ActualRate: 30.0, WasClamped: true, ClampingReason: RateOfChangeReason},
				{ActualRate: 40.0, WasClamped: true, ClampingReason: RateOfChangeReason}, // 30 + 10
			},
		},
		{
			name: "Mode transition preserves state",
			calls: []struct {
				requestedRate float64
				mode          OperationalMode
				elapsedTimeMs int
			}{
				{30.0, Startup, 100},      // In startup range [10, 50], no clamp
				{30.0, Cruise, 100},       // In cruise, no clamp (previous = 30)
				{40.0, Cruise, 100},       // Tries to go from 30 to 40, limited to 40 (30 + 10)
			},
			expectedOutputs: []FuelRateLimiterOutput{
				{ActualRate: 30.0, WasClamped: false, ClampingReason: NoneReason},
				{ActualRate: 30.0, WasClamped: false, ClampingReason: NoneReason},
				{ActualRate: 40.0, WasClamped: false, ClampingReason: NoneReason}, // 30 + 10 = 40
			},
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			limiter := NewFuelRateLimiter()

			for i, call := range tt.calls {
				result := limiter.Call(call.requestedRate, call.mode, call.elapsedTimeMs)
				expected := tt.expectedOutputs[i]

				const tolerance = 0.001
				if diff := result.ActualRate - expected.ActualRate; diff < -tolerance || diff > tolerance {
					t.Errorf("Call %d: ActualRate = %f, want %f", i, result.ActualRate, expected.ActualRate)
				}
				if result.WasClamped != expected.WasClamped {
					t.Errorf("Call %d: WasClamped = %v, want %v", i, result.WasClamped, expected.WasClamped)
				}
				if result.ClampingReason != expected.ClampingReason {
					t.Errorf("Call %d: ClampingReason = %v, want %v", i, result.ClampingReason, expected.ClampingReason)
				}
			}
		})
	}
}

// TestRateOfChangeWithVariableTimeStep verifies RoC calculation with different time intervals
func TestRateOfChangeWithVariableTimeStep(t *testing.T) {
	tests := []struct {
		name            string
		previousRate    float64
		requestedRate   float64
		elapsedTimeMs   int
		expectedRate    float64
		expectedClamped bool
	}{
		{
			name:            "Very short time step (10ms, max change = 1.0 lph)",
			previousRate:    50.0,
			requestedRate:   60.0,
			elapsedTimeMs:   10,
			expectedRate:    50.0 + (100.0 * 10.0 / 1000.0), // 50 + 1.0 = 51.0
			expectedClamped: true,
		},
		{
			name:            "Medium time step (250ms, max change = 25.0 lph)",
			previousRate:    50.0,
			requestedRate:   100.0,
			elapsedTimeMs:   250,
			expectedRate:    50.0 + (100.0 * 250.0 / 1000.0), // 50 + 25.0 = 75.0
			expectedClamped: true,
		},
		{
			name:            "Long time step (2000ms, max change = 200.0 lph)",
			previousRate:    50.0,
			requestedRate:   200.0,
			elapsedTimeMs:   2000,
			expectedRate:    50.0 + 100.0, // Capped at 150, but also mode max is 200, so 150
			expectedClamped: true,
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			limiter := NewFuelRateLimiter()
			limiter.previousRate = tt.previousRate

			result := limiter.Call(tt.requestedRate, Cruise, tt.elapsedTimeMs)

			const tolerance = 0.001
			if diff := result.ActualRate - tt.expectedRate; diff < -tolerance || diff > tolerance {
				t.Errorf("ActualRate = %f, want %f", result.ActualRate, tt.expectedRate)
			}
			if result.WasClamped != tt.expectedClamped {
				t.Errorf("WasClamped = %v, want %v", result.WasClamped, tt.expectedClamped)
			}
		})
	}
}

// TestWasClampedInvariant verifies the invariant: was_clamped iff actual_rate != requested_rate
func TestWasClampedInvariant(t *testing.T) {
	tests := []struct {
		name          string
		requestedRate float64
		mode          OperationalMode
		elapsedTimeMs int
		previousRate  float64
	}{
		{
			name:          "Startup, in range",
			requestedRate: 25.0,
			mode:          Startup,
			elapsedTimeMs: 100,
			previousRate:  0.0,
		},
		{
			name:          "Startup, below min",
			requestedRate: 5.0,
			mode:          Startup,
			elapsedTimeMs: 100,
			previousRate:  0.0,
		},
		{
			name:          "Cruise, no limits triggered",
			requestedRate: 50.0,
			mode:          Cruise,
			elapsedTimeMs: 100,
			previousRate:  40.0,
		},
		{
			name:          "Cruise, RoC limit triggered",
			requestedRate: 200.0,
			mode:          Cruise,
			elapsedTimeMs: 100,
			previousRate:  50.0,
		},
		{
			name:          "Emergency shutdown",
			requestedRate: 100.0,
			mode:          EmergencyShutdown,
			elapsedTimeMs: 100,
			previousRate:  100.0,
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			limiter := NewFuelRateLimiter()
			limiter.previousRate = tt.previousRate

			result := limiter.Call(tt.requestedRate, tt.mode, tt.elapsedTimeMs)

			// Verify the invariant
			actualDiffersFromRequested := result.ActualRate != tt.requestedRate
			if result.WasClamped != actualDiffersFromRequested {
				t.Errorf("Invariant violated: WasClamped=%v but ActualRate(%f) %s RequestedRate(%f)",
					result.WasClamped, result.ActualRate,
					map[bool]string{true: "!=", false: "=="}[actualDiffersFromRequested],
					tt.requestedRate)
			}
		})
	}
}

// TestZeroElapsedTime verifies behavior when elapsed time is exactly zero
func TestZeroElapsedTime(t *testing.T) {
	tests := []struct {
		name            string
		previousRate    float64
		requestedRate   float64
		expectedRate    float64
		expectedClamped bool
	}{
		{
			name:            "Zero elapsed time allows any increase within mode max",
			previousRate:    50.0,
			requestedRate:   150.0,
			expectedRate:    150.0,
			expectedClamped: false,
		},
		{
			name:            "Zero elapsed time, still respects mode max",
			previousRate:    50.0,
			requestedRate:   250.0,
			expectedRate:    cruiseMaxRate,
			expectedClamped: true,
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			limiter := NewFuelRateLimiter()
			limiter.previousRate = tt.previousRate

			result := limiter.Call(tt.requestedRate, Cruise, 0)

			if result.ActualRate != tt.expectedRate {
				t.Errorf("ActualRate = %f, want %f", result.ActualRate, tt.expectedRate)
			}
			if result.WasClamped != tt.expectedClamped {
				t.Errorf("WasClamped = %v, want %v", result.WasClamped, tt.expectedClamped)
			}
		})
	}
}

// TestMultipleClampingConstraints verifies correct reason when multiple constraints could apply
func TestMultipleClampingConstraints(t *testing.T) {
	tests := []struct {
		name            string
		previousRate    float64
		requestedRate   float64
		mode            OperationalMode
		elapsedTimeMs   int
		expectedReason  ClampingReason
	}{
		{
			name:           "In startup, below min takes priority",
			previousRate:   0.0,
			requestedRate:  0.0,
			mode:           Startup,
			elapsedTimeMs:  100,
			expectedReason: ModeMinReason,
		},
		{
			name:           "In startup, above max applies",
			previousRate:   0.0,
			requestedRate:  100.0,
			mode:           Startup,
			elapsedTimeMs:  100,
			expectedReason: ModeMaxReason,
		},
		{
			name:           "In cruise, mode max applies before RoC (both would trigger)",
			previousRate:   0.0,
			requestedRate:  500.0, // Exceeds both cruiseMaxRate and RoC limit
			mode:           Cruise,
			elapsedTimeMs:  100,
			expectedReason: ModeMaxReason, // Mode max is applied first
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			limiter := NewFuelRateLimiter()
			limiter.previousRate = tt.previousRate

			result := limiter.Call(tt.requestedRate, tt.mode, tt.elapsedTimeMs)

			if result.ClampingReason != tt.expectedReason {
				t.Errorf("ClampingReason = %v, want %v", result.ClampingReason, tt.expectedReason)
			}
		})
	}
}
