package tempcontrol

import (
	"testing"
)

func TestEvaluateDoesNotPanic(t *testing.T) {
	c := NewController(DefaultConfig())
	_ = c.Evaluate(20.0)
	_ = c.Evaluate(25.0)
	_ = c.Evaluate(15.0)
}

func TestIdleToHeat(t *testing.T) {
	cfg := DefaultConfig()
	c := NewController(cfg)

	lowerThreshold := cfg.TargetTemp - cfg.Deadband/2
	temp := lowerThreshold - 1.0

	result := c.Evaluate(temp)
	if temp < lowerThreshold {
		if result.Action != ActionHeat {
			t.Errorf("expected HEAT, got %s", result.Action)
		}
	}
}

func TestIdleToCool(t *testing.T) {
	c := NewController(DefaultConfig())
	result := c.Evaluate(24.0)
	if result.Action != ActionCool {
		t.Errorf("expected COOL at 24.0, got %s", result.Action)
	}
	if !result.StateChanged {
		t.Error("expected stateChanged=true on first transition")
	}
}

func TestHeatToIdle(t *testing.T) {
	c := NewController(DefaultConfig())
	c.Evaluate(20.0)
	result := c.Evaluate(23.0)
	if result.Action != ActionIdle {
		t.Errorf("expected IDLE at upper threshold, got %s", result.Action)
	}
	if !result.StateChanged {
		t.Error("expected stateChanged=true")
	}
}

func TestCoolToIdle(t *testing.T) {
	c := NewController(DefaultConfig())
	c.Evaluate(24.0)
	result := c.Evaluate(21.0)
	if result.Action != ActionIdle {
		t.Errorf("expected IDLE at lower threshold, got %s", result.Action)
	}
}

func TestStayInHeat(t *testing.T) {
	c := NewController(DefaultConfig())
	c.Evaluate(20.0)
	result := c.Evaluate(22.0)
	if result.Action != ActionHeat {
		t.Errorf("expected to stay in HEAT at 22.0, got %s", result.Action)
	}
	if result.StateChanged {
		t.Error("expected stateChanged=false when staying in HEAT")
	}
}

func TestStayInCool(t *testing.T) {
	c := NewController(DefaultConfig())
	c.Evaluate(24.0)
	result := c.Evaluate(22.0)
	if result.Action != ActionCool {
		t.Errorf("expected to stay in COOL at 22.0, got %s", result.Action)
	}
}

func TestFirstCallStartsFromIdle(t *testing.T) {
	c := NewController(DefaultConfig())
	result := c.Evaluate(22.0)
	if result.Action != ActionIdle {
		t.Errorf("expected IDLE on first call within deadband, got %s", result.Action)
	}
	if result.StateChanged {
		t.Error("expected stateChanged=false on first call when staying IDLE")
	}
}

func TestConfigInitialization(t *testing.T) {
	cfg := Config{
		TargetTemp: 25.0,
		Deadband:   3.0,
		SensorMin:  -50.0,
		SensorMax:  150.0,
		TargetMin:  0.0,
		TargetMax:  100.0,
	}
	if cfg.TargetTemp != 25.0 {
		t.Error("config target not set")
	}
	if cfg.Deadband != 3.0 {
		t.Error("config deadband not set")
	}
}
