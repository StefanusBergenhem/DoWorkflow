package fuelcontrol

import (
	"math"
	"testing"
)

// ---------------------------------------------------------------------------
// Types — these mirror the design artifact DD-001 interfaces.
// In real code these would live in the production package; here they define
// the contract the implementation must satisfy.
// ---------------------------------------------------------------------------

// ClampingReason represents why clamping was applied.
type ClampingReason string

const (
	ClampingNone         ClampingReason = "none"
	ClampingModeMax      ClampingReason = "mode_max"
	ClampingModeMin      ClampingReason = "mode_min"
	ClampingRateOfChange ClampingReason = "rate_of_change"
	ClampingEmergency    ClampingReason = "emergency"
)

// OperationalMode represents the engine operational mode.
type OperationalMode string

const (
	ModeStartup           OperationalMode = "startup"
	ModeCruise            OperationalMode = "cruise"
	ModeEmergencyShutdown OperationalMode = "emergency_shutdown"
)

// LimiterResult is the output of a Limit call.
type LimiterResult struct {
	ActualRate     float64
	WasClamped     bool
	ClampingReason ClampingReason
}

// FuelRateLimiterConfig holds configuration parameters.
type FuelRateLimiterConfig struct {
	StartupMinRate float64 // liters per hour — minimum during startup
	StartupMaxRate float64 // liters per hour — maximum during startup
	CruiseMaxRate  float64 // liters per hour — maximum during cruise
	MaxRateChange  float64 // liters per hour per second
}

// DefaultConfig returns the default configuration from the design spec.
func DefaultConfig() FuelRateLimiterConfig {
	return FuelRateLimiterConfig{
		StartupMinRate: 10.0,
		StartupMaxRate: 50.0,
		CruiseMaxRate:  200.0,
		MaxRateChange:  100.0,
	}
}

// FuelRateLimiter is the unit under test interface.
// The implementation must be provided to make these tests pass.
type FuelRateLimiter struct {
	config       FuelRateLimiterConfig
	previousRate float64
}

// NewFuelRateLimiter creates a limiter with the given configuration.
func NewFuelRateLimiter(cfg FuelRateLimiterConfig) *FuelRateLimiter {
	return &FuelRateLimiter{config: cfg}
}

// Limit applies mode-specific and rate-of-change limiting to the requested rate.
// This signature is derived from the design artifact DD-001.
func (f *FuelRateLimiter) Limit(requestedRate float64, mode OperationalMode, elapsedTimeMs int) LimiterResult {
	// Implementation to be provided — tests are written first (TDD red phase).
	panic("not implemented")
}

// ---------------------------------------------------------------------------
// Helper
// ---------------------------------------------------------------------------

const floatTolerance = 1e-9

func assertFloat(t *testing.T, label string, got, want float64) {
	t.Helper()
	if math.Abs(got-want) > floatTolerance {
		t.Errorf("%s: got %f, want %f", label, got, want)
	}
}

func assertBool(t *testing.T, label string, got, want bool) {
	t.Helper()
	if got != want {
		t.Errorf("%s: got %v, want %v", label, got, want)
	}
}

func assertReason(t *testing.T, label string, got, want ClampingReason) {
	t.Helper()
	if got != want {
		t.Errorf("%s: got %q, want %q", label, got, want)
	}
}

// ---------------------------------------------------------------------------
// Strategy 1 — Requirement-based: behavior rules
// ---------------------------------------------------------------------------

// -- Behavior rule 1: startup mode clamping --

func TestStartupMode_RequestBelowMin_ClampsToMin(t *testing.T) {
	// Arrange
	lim := NewFuelRateLimiter(DefaultConfig())
	// 5.0 < STARTUP_MIN_RATE (10.0)

	// Act
	res := lim.Limit(5.0, ModeStartup, 100)

	// Assert — design: clamp to STARTUP_MIN_RATE, reason mode_min
	assertFloat(t, "actual_rate", res.ActualRate, 10.0)
	assertBool(t, "was_clamped", res.WasClamped, true)
	assertReason(t, "clamping_reason", res.ClampingReason, ClampingModeMin)
}

func TestStartupMode_RequestAboveMax_ClampsToMax(t *testing.T) {
	lim := NewFuelRateLimiter(DefaultConfig())
	// 75.0 > STARTUP_MAX_RATE (50.0)

	res := lim.Limit(75.0, ModeStartup, 100)

	assertFloat(t, "actual_rate", res.ActualRate, 50.0)
	assertBool(t, "was_clamped", res.WasClamped, true)
	assertReason(t, "clamping_reason", res.ClampingReason, ClampingModeMax)
}

func TestStartupMode_RequestWithinRange_NoClamping(t *testing.T) {
	lim := NewFuelRateLimiter(DefaultConfig())
	// 30.0 is within [10.0, 50.0]

	res := lim.Limit(30.0, ModeStartup, 100)

	assertFloat(t, "actual_rate", res.ActualRate, 30.0)
	assertBool(t, "was_clamped", res.WasClamped, false)
	assertReason(t, "clamping_reason", res.ClampingReason, ClampingNone)
}

// -- Behavior rule 2: cruise mode clamping + rate-of-change --

func TestCruiseMode_RequestAboveMax_ClampsToMax(t *testing.T) {
	lim := NewFuelRateLimiter(DefaultConfig())
	// 250.0 > CRUISE_MAX_RATE (200.0), with enough elapsed time so ROC is not the limiter

	res := lim.Limit(250.0, ModeCruise, 10000)

	assertFloat(t, "actual_rate", res.ActualRate, 200.0)
	assertBool(t, "was_clamped", res.WasClamped, true)
	assertReason(t, "clamping_reason", res.ClampingReason, ClampingModeMax)
}

func TestCruiseMode_RequestWithinMax_NoClamping(t *testing.T) {
	lim := NewFuelRateLimiter(DefaultConfig())
	// 50.0 within [0.0, 200.0], previous_rate=0 => ROC allows up to 100*10=1000 change in 10s

	res := lim.Limit(50.0, ModeCruise, 10000)

	assertFloat(t, "actual_rate", res.ActualRate, 50.0)
	assertBool(t, "was_clamped", res.WasClamped, false)
	assertReason(t, "clamping_reason", res.ClampingReason, ClampingNone)
}

func TestCruiseMode_RateOfChangeLimitsIncrease(t *testing.T) {
	// Arrange: previous_rate = 100.0, requesting 200.0
	// elapsed = 500ms => max allowed change = 100.0 * 0.5 = 50.0 L/hr
	// So actual_rate should be clamped to 100.0 + 50.0 = 150.0
	lim := NewFuelRateLimiter(DefaultConfig())

	// First call to establish previous_rate = 100.0
	// (request exactly 100 in cruise with long elapsed so no ROC issue)
	lim.Limit(100.0, ModeCruise, 10000)

	// Act: second call with short elapsed time
	res := lim.Limit(200.0, ModeCruise, 500)

	// Assert: rate-of-change limits the increase
	assertFloat(t, "actual_rate", res.ActualRate, 150.0)
	assertBool(t, "was_clamped", res.WasClamped, true)
	assertReason(t, "clamping_reason", res.ClampingReason, ClampingRateOfChange)
}

func TestCruiseMode_RateOfChangeLimitsDecrease(t *testing.T) {
	// Arrange: previous_rate = 150.0, requesting 0.0
	// elapsed = 500ms => max allowed change = 100.0 * 0.5 = 50.0 L/hr
	// So actual_rate should be clamped to 150.0 - 50.0 = 100.0
	lim := NewFuelRateLimiter(DefaultConfig())

	// Establish previous_rate = 150.0
	lim.Limit(150.0, ModeCruise, 10000)

	// Act
	res := lim.Limit(0.0, ModeCruise, 500)

	// Assert: rate-of-change limits the decrease
	assertFloat(t, "actual_rate", res.ActualRate, 100.0)
	assertBool(t, "was_clamped", res.WasClamped, true)
	assertReason(t, "clamping_reason", res.ClampingReason, ClampingRateOfChange)
}

func TestCruiseMode_RateOfChangeExactlyAtLimit_NoClamping(t *testing.T) {
	// Arrange: previous_rate = 100.0, requesting 150.0
	// elapsed = 500ms => max allowed change = 100.0 * 0.5 = 50.0
	// Requested change is exactly 50.0 — should NOT be clamped (<=)
	lim := NewFuelRateLimiter(DefaultConfig())

	lim.Limit(100.0, ModeCruise, 10000)

	res := lim.Limit(150.0, ModeCruise, 500)

	assertFloat(t, "actual_rate", res.ActualRate, 150.0)
	assertBool(t, "was_clamped", res.WasClamped, false)
	assertReason(t, "clamping_reason", res.ClampingReason, ClampingNone)
}

// -- Behavior rule 3: emergency shutdown --

func TestEmergencyShutdown_ForcesZeroRate(t *testing.T) {
	lim := NewFuelRateLimiter(DefaultConfig())

	res := lim.Limit(150.0, ModeEmergencyShutdown, 100)

	assertFloat(t, "actual_rate", res.ActualRate, 0.0)
	assertBool(t, "was_clamped", res.WasClamped, true)
	assertReason(t, "clamping_reason", res.ClampingReason, ClampingEmergency)
}

func TestEmergencyShutdown_AlreadyZero_StillClamped(t *testing.T) {
	// Design says "set was_clamped to true" unconditionally in emergency
	lim := NewFuelRateLimiter(DefaultConfig())

	res := lim.Limit(0.0, ModeEmergencyShutdown, 100)

	assertFloat(t, "actual_rate", res.ActualRate, 0.0)
	assertBool(t, "was_clamped", res.WasClamped, true)
	assertReason(t, "clamping_reason", res.ClampingReason, ClampingEmergency)
}

// -- Behavior rule 4: was_clamped iff actual_rate != requested_rate --
// (Covered implicitly in every test above, but let's add an explicit cross-check)

func TestWasClampedFalse_WhenRateUnchanged(t *testing.T) {
	lim := NewFuelRateLimiter(DefaultConfig())

	res := lim.Limit(30.0, ModeStartup, 100)

	if res.ActualRate == 30.0 && res.WasClamped {
		t.Error("was_clamped should be false when actual_rate equals requested_rate")
	}
}

func TestWasClampedTrue_WhenRateChanged(t *testing.T) {
	lim := NewFuelRateLimiter(DefaultConfig())

	res := lim.Limit(5.0, ModeStartup, 100)

	if res.ActualRate != 5.0 && !res.WasClamped {
		t.Error("was_clamped should be true when actual_rate differs from requested_rate")
	}
}

// ---------------------------------------------------------------------------
// Strategy 2 — Equivalence class partitioning
// ---------------------------------------------------------------------------

// -- requested_rate classes --

func TestRequestedRate_Zero_CruiseMode(t *testing.T) {
	lim := NewFuelRateLimiter(DefaultConfig())

	res := lim.Limit(0.0, ModeCruise, 1000)

	assertFloat(t, "actual_rate", res.ActualRate, 0.0)
	assertBool(t, "was_clamped", res.WasClamped, false)
	assertReason(t, "clamping_reason", res.ClampingReason, ClampingNone)
}

func TestRequestedRate_Zero_StartupMode_ClampsToMin(t *testing.T) {
	lim := NewFuelRateLimiter(DefaultConfig())

	res := lim.Limit(0.0, ModeStartup, 100)

	// 0.0 < STARTUP_MIN_RATE => clamped to 10.0
	assertFloat(t, "actual_rate", res.ActualRate, 10.0)
	assertBool(t, "was_clamped", res.WasClamped, true)
	assertReason(t, "clamping_reason", res.ClampingReason, ClampingModeMin)
}

// -- operational_mode: each enum value is a class --
// (Covered by mode-specific tests above: startup, cruise, emergency_shutdown)

// -- elapsed_time_ms: zero value --

func TestElapsedTimeZero_CruiseMode_NoRateOfChangeApplied(t *testing.T) {
	// elapsed_time_ms = 0 => max allowed change = 100.0 * 0/1000 = 0.0
	// So any change from previous_rate would be clamped.
	// With previous_rate = 0 and requesting 0, no change => no clamping.
	lim := NewFuelRateLimiter(DefaultConfig())

	res := lim.Limit(0.0, ModeCruise, 0)

	assertFloat(t, "actual_rate", res.ActualRate, 0.0)
	assertBool(t, "was_clamped", res.WasClamped, false)
}

func TestElapsedTimeZero_CruiseMode_RateChangeBlockedEntirely(t *testing.T) {
	// previous_rate = 50, requesting 100 with elapsed=0
	// max allowed change = 0 => actual_rate stays at 50
	lim := NewFuelRateLimiter(DefaultConfig())
	lim.Limit(50.0, ModeCruise, 10000) // establish previous_rate = 50

	res := lim.Limit(100.0, ModeCruise, 0)

	assertFloat(t, "actual_rate", res.ActualRate, 50.0)
	assertBool(t, "was_clamped", res.WasClamped, true)
	assertReason(t, "clamping_reason", res.ClampingReason, ClampingRateOfChange)
}

// ---------------------------------------------------------------------------
// Strategy 3 — Boundary value analysis
// ---------------------------------------------------------------------------

// -- requested_rate boundaries --

func TestRequestedRate_AtStartupMinBoundary(t *testing.T) {
	lim := NewFuelRateLimiter(DefaultConfig())

	res := lim.Limit(10.0, ModeStartup, 100)

	assertFloat(t, "actual_rate", res.ActualRate, 10.0)
	assertBool(t, "was_clamped", res.WasClamped, false)
	assertReason(t, "clamping_reason", res.ClampingReason, ClampingNone)
}

func TestRequestedRate_JustBelowStartupMin(t *testing.T) {
	lim := NewFuelRateLimiter(DefaultConfig())

	res := lim.Limit(9.999, ModeStartup, 100)

	assertFloat(t, "actual_rate", res.ActualRate, 10.0)
	assertBool(t, "was_clamped", res.WasClamped, true)
	assertReason(t, "clamping_reason", res.ClampingReason, ClampingModeMin)
}

func TestRequestedRate_AtStartupMaxBoundary(t *testing.T) {
	lim := NewFuelRateLimiter(DefaultConfig())

	res := lim.Limit(50.0, ModeStartup, 100)

	assertFloat(t, "actual_rate", res.ActualRate, 50.0)
	assertBool(t, "was_clamped", res.WasClamped, false)
	assertReason(t, "clamping_reason", res.ClampingReason, ClampingNone)
}

func TestRequestedRate_JustAboveStartupMax(t *testing.T) {
	lim := NewFuelRateLimiter(DefaultConfig())

	res := lim.Limit(50.001, ModeStartup, 100)

	assertFloat(t, "actual_rate", res.ActualRate, 50.0)
	assertBool(t, "was_clamped", res.WasClamped, true)
	assertReason(t, "clamping_reason", res.ClampingReason, ClampingModeMax)
}

func TestRequestedRate_AtCruiseMaxBoundary(t *testing.T) {
	lim := NewFuelRateLimiter(DefaultConfig())

	res := lim.Limit(200.0, ModeCruise, 10000)

	assertFloat(t, "actual_rate", res.ActualRate, 200.0)
	assertBool(t, "was_clamped", res.WasClamped, false)
	assertReason(t, "clamping_reason", res.ClampingReason, ClampingNone)
}

func TestRequestedRate_JustAboveCruiseMax(t *testing.T) {
	lim := NewFuelRateLimiter(DefaultConfig())

	res := lim.Limit(200.001, ModeCruise, 10000)

	assertFloat(t, "actual_rate", res.ActualRate, 200.0)
	assertBool(t, "was_clamped", res.WasClamped, true)
	assertReason(t, "clamping_reason", res.ClampingReason, ClampingModeMax)
}

// -- rate-of-change boundary: change exactly at allowed limit --
// (Covered in TestCruiseMode_RateOfChangeExactlyAtLimit_NoClamping)

// -- rate-of-change boundary: change just above allowed limit --

func TestCruiseMode_RateOfChangeJustAboveLimit(t *testing.T) {
	// previous_rate = 100.0, elapsed = 500ms => max change = 50.0
	// Requesting 150.01 => 0.01 over limit => should clamp to 150.0
	lim := NewFuelRateLimiter(DefaultConfig())
	lim.Limit(100.0, ModeCruise, 10000)

	res := lim.Limit(150.01, ModeCruise, 500)

	assertFloat(t, "actual_rate", res.ActualRate, 150.0)
	assertBool(t, "was_clamped", res.WasClamped, true)
	assertReason(t, "clamping_reason", res.ClampingReason, ClampingRateOfChange)
}

// -- output boundary: actual_rate never exceeds 500.0 --

func TestOutputRate_NeverExceeds500(t *testing.T) {
	// Use custom config with high cruise max to test output constraint
	cfg := FuelRateLimiterConfig{
		StartupMinRate: 10.0,
		StartupMaxRate: 50.0,
		CruiseMaxRate:  600.0, // exceeds output max
		MaxRateChange:  10000.0,
	}
	lim := NewFuelRateLimiter(cfg)

	res := lim.Limit(550.0, ModeCruise, 10000)

	if res.ActualRate > 500.0 {
		t.Errorf("actual_rate %f exceeds output constraint of 500.0", res.ActualRate)
	}
}

// ---------------------------------------------------------------------------
// Strategy 4 — Error handling / fault injection
// ---------------------------------------------------------------------------

func TestErrorHandling_NegativeRequestedRate_TreatedAsZero(t *testing.T) {
	lim := NewFuelRateLimiter(DefaultConfig())

	res := lim.Limit(-10.0, ModeStartup, 100)

	// Design: treat as 0.0, was_clamped=true, reason=mode_min
	// In startup mode, 0.0 < STARTUP_MIN_RATE => clamped to 10.0
	assertFloat(t, "actual_rate", res.ActualRate, 10.0)
	assertBool(t, "was_clamped", res.WasClamped, true)
	assertReason(t, "clamping_reason", res.ClampingReason, ClampingModeMin)
}

func TestErrorHandling_NegativeRequestedRate_CruiseMode(t *testing.T) {
	lim := NewFuelRateLimiter(DefaultConfig())

	res := lim.Limit(-5.0, ModeCruise, 10000)

	// Design: treat as 0.0, was_clamped=true, reason=mode_min
	assertFloat(t, "actual_rate", res.ActualRate, 0.0)
	assertBool(t, "was_clamped", res.WasClamped, true)
	assertReason(t, "clamping_reason", res.ClampingReason, ClampingModeMin)
}

func TestErrorHandling_NegativeElapsedTime_SkipsRateOfChange(t *testing.T) {
	// Design: treat elapsed as 0, skip rate-of-change limiting
	// With previous_rate=100 and requesting 200, normally ROC would limit.
	// With negative elapsed, ROC is skipped so 200 is allowed (within cruise max).
	lim := NewFuelRateLimiter(DefaultConfig())
	lim.Limit(100.0, ModeCruise, 10000) // establish previous_rate = 100

	res := lim.Limit(200.0, ModeCruise, -500)

	assertFloat(t, "actual_rate", res.ActualRate, 200.0)
	assertBool(t, "was_clamped", res.WasClamped, false)
	assertReason(t, "clamping_reason", res.ClampingReason, ClampingNone)
}

func TestErrorHandling_UnrecognizedMode_TreatedAsEmergencyShutdown(t *testing.T) {
	lim := NewFuelRateLimiter(DefaultConfig())

	res := lim.Limit(100.0, OperationalMode("invalid_mode"), 100)

	// Design: treat as emergency_shutdown => actual_rate=0, clamped, reason=emergency
	assertFloat(t, "actual_rate", res.ActualRate, 0.0)
	assertBool(t, "was_clamped", res.WasClamped, true)
	assertReason(t, "clamping_reason", res.ClampingReason, ClampingEmergency)
}

func TestErrorHandling_EmptyStringMode_TreatedAsEmergencyShutdown(t *testing.T) {
	lim := NewFuelRateLimiter(DefaultConfig())

	res := lim.Limit(50.0, OperationalMode(""), 100)

	assertFloat(t, "actual_rate", res.ActualRate, 0.0)
	assertBool(t, "was_clamped", res.WasClamped, true)
	assertReason(t, "clamping_reason", res.ClampingReason, ClampingEmergency)
}

// ---------------------------------------------------------------------------
// Stateful behavior — rate-of-change limiting across multiple calls
// ---------------------------------------------------------------------------

func TestStateful_PreviousRateUpdatedAfterEachCall(t *testing.T) {
	// Call sequence in cruise mode to verify state accumulates correctly.
	// Call 1: request 50, elapsed 10s => allowed, previous_rate becomes 50
	// Call 2: request 120, elapsed 500ms => max change = 50 => actual = 100
	// Call 3: request 120, elapsed 500ms => max change = 50 => actual = 120 (100+50 > 120, so 120)
	lim := NewFuelRateLimiter(DefaultConfig())

	res1 := lim.Limit(50.0, ModeCruise, 10000)
	assertFloat(t, "call1 actual_rate", res1.ActualRate, 50.0)

	res2 := lim.Limit(120.0, ModeCruise, 500)
	// max change = 100 * 0.5 = 50; 50 + 50 = 100
	assertFloat(t, "call2 actual_rate", res2.ActualRate, 100.0)
	assertBool(t, "call2 was_clamped", res2.WasClamped, true)
	assertReason(t, "call2 reason", res2.ClampingReason, ClampingRateOfChange)

	res3 := lim.Limit(120.0, ModeCruise, 500)
	// previous = 100, max change = 50; 100 + 50 = 150 > 120, so 120 is allowed
	assertFloat(t, "call3 actual_rate", res3.ActualRate, 120.0)
	assertBool(t, "call3 was_clamped", res3.WasClamped, false)
	assertReason(t, "call3 reason", res3.ClampingReason, ClampingNone)
}

func TestStateful_ModeTransitionStartupToCruise(t *testing.T) {
	// Start in startup mode, switch to cruise. Previous rate from startup
	// should carry over to cruise rate-of-change calculation.
	lim := NewFuelRateLimiter(DefaultConfig())

	// Startup: request 40 (within [10, 50])
	res1 := lim.Limit(40.0, ModeStartup, 1000)
	assertFloat(t, "startup actual_rate", res1.ActualRate, 40.0)

	// Cruise: request 200, elapsed 500ms => max change = 50
	// previous = 40, so max = 40 + 50 = 90
	res2 := lim.Limit(200.0, ModeCruise, 500)
	assertFloat(t, "cruise actual_rate", res2.ActualRate, 90.0)
	assertBool(t, "cruise was_clamped", res2.WasClamped, true)
	assertReason(t, "cruise reason", res2.ClampingReason, ClampingRateOfChange)
}

func TestStateful_EmergencyResetsToZero(t *testing.T) {
	// After emergency shutdown sets rate to 0, the next cruise call
	// should use previous_rate = 0.
	lim := NewFuelRateLimiter(DefaultConfig())
	lim.Limit(100.0, ModeCruise, 10000) // previous = 100

	lim.Limit(50.0, ModeEmergencyShutdown, 100) // previous becomes 0

	// Now in cruise: previous = 0, request 100, elapsed 500ms => max change = 50
	res := lim.Limit(100.0, ModeCruise, 500)
	assertFloat(t, "actual_rate", res.ActualRate, 50.0)
	assertBool(t, "was_clamped", res.WasClamped, true)
	assertReason(t, "clamping_reason", res.ClampingReason, ClampingRateOfChange)
}

func TestStateful_RapidDecelerationInCruise(t *testing.T) {
	// Verify rate-of-change works symmetrically for decreases
	// previous = 180, request = 0, elapsed = 1000ms => max change = 100
	// actual = 180 - 100 = 80
	lim := NewFuelRateLimiter(DefaultConfig())
	lim.Limit(180.0, ModeCruise, 10000)

	res := lim.Limit(0.0, ModeCruise, 1000)

	assertFloat(t, "actual_rate", res.ActualRate, 80.0)
	assertBool(t, "was_clamped", res.WasClamped, true)
	assertReason(t, "clamping_reason", res.ClampingReason, ClampingRateOfChange)
}

// ---------------------------------------------------------------------------
// Configuration variants
// ---------------------------------------------------------------------------

func TestConfig_CustomStartupRange(t *testing.T) {
	cfg := FuelRateLimiterConfig{
		StartupMinRate: 20.0,
		StartupMaxRate: 80.0,
		CruiseMaxRate:  200.0,
		MaxRateChange:  100.0,
	}
	lim := NewFuelRateLimiter(cfg)

	// 15 < custom min 20 => clamps to 20
	res := lim.Limit(15.0, ModeStartup, 100)
	assertFloat(t, "actual_rate", res.ActualRate, 20.0)
	assertBool(t, "was_clamped", res.WasClamped, true)
	assertReason(t, "clamping_reason", res.ClampingReason, ClampingModeMin)
}

func TestConfig_CustomCruiseMax(t *testing.T) {
	cfg := FuelRateLimiterConfig{
		StartupMinRate: 10.0,
		StartupMaxRate: 50.0,
		CruiseMaxRate:  100.0, // lower than default
		MaxRateChange:  100.0,
	}
	lim := NewFuelRateLimiter(cfg)

	// 150 > custom cruise max 100
	res := lim.Limit(150.0, ModeCruise, 10000)
	assertFloat(t, "actual_rate", res.ActualRate, 100.0)
	assertBool(t, "was_clamped", res.WasClamped, true)
	assertReason(t, "clamping_reason", res.ClampingReason, ClampingModeMax)
}

func TestConfig_CustomMaxRateChange(t *testing.T) {
	cfg := FuelRateLimiterConfig{
		StartupMinRate: 10.0,
		StartupMaxRate: 50.0,
		CruiseMaxRate:  200.0,
		MaxRateChange:  20.0, // much stricter: 20 L/hr/s
	}
	lim := NewFuelRateLimiter(cfg)
	lim.Limit(50.0, ModeCruise, 10000) // establish previous = 50

	// request 100, elapsed 1000ms => max change = 20*1 = 20 => actual = 70
	res := lim.Limit(100.0, ModeCruise, 1000)
	assertFloat(t, "actual_rate", res.ActualRate, 70.0)
	assertBool(t, "was_clamped", res.WasClamped, true)
	assertReason(t, "clamping_reason", res.ClampingReason, ClampingRateOfChange)
}

// ---------------------------------------------------------------------------
// Edge cases — NaN/Inf for float inputs
// ---------------------------------------------------------------------------

func TestRequestedRate_NaN_TreatedAsInvalid(t *testing.T) {
	lim := NewFuelRateLimiter(DefaultConfig())

	res := lim.Limit(math.NaN(), ModeStartup, 100)

	// NaN comparisons are tricky. The implementation should handle this.
	// At minimum: should not panic, and was_clamped should be true since
	// NaN != any valid rate.
	assertBool(t, "was_clamped", res.WasClamped, true)
	if math.IsNaN(res.ActualRate) {
		t.Error("actual_rate should not be NaN — must produce a valid output")
	}
}

func TestRequestedRate_PositiveInf_ClampsToMax(t *testing.T) {
	lim := NewFuelRateLimiter(DefaultConfig())

	res := lim.Limit(math.Inf(1), ModeCruise, 10000)

	// +Inf > CRUISE_MAX_RATE => should clamp
	if res.ActualRate > 500.0 {
		t.Errorf("actual_rate %f exceeds output max 500.0 for +Inf input", res.ActualRate)
	}
	assertBool(t, "was_clamped", res.WasClamped, true)
}
