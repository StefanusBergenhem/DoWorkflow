// Package tempcontrol implements a temperature controller with deadband hysteresis.
package tempcontrol

import "math"

// Action represents the controller's output decision.
type Action string

const (
	ActionHeat Action = "HEAT"
	ActionCool Action = "COOL"
	ActionIdle Action = "IDLE"
)

// Config holds the controller configuration with defaults.
type Config struct {
	TargetTemp   float64 // Default 22.0
	Deadband     float64 // Default 2.0, min 0.5, max 10.0
	SensorMin    float64 // Default -50.0
	SensorMax    float64 // Default 150.0
	TargetMin    float64 // Default 0.0
	TargetMax    float64 // Default 100.0
}

// DefaultConfig returns the default configuration.
func DefaultConfig() Config {
	return Config{
		TargetTemp: 22.0,
		Deadband:   2.0,
		SensorMin:  -50.0,
		SensorMax:  150.0,
		TargetMin:  0.0,
		TargetMax:  100.0,
	}
}

// Result is the output of a controller evaluation.
type Result struct {
	Action       Action
	StateChanged bool
}

// Controller manages temperature regulation with deadband hysteresis.
type Controller struct {
	config       Config
	currentState Action
	initialized  bool
}

// NewController creates a controller with the given configuration.
func NewController(config Config) *Controller {
	config = sanitizeConfig(config)
	return &Controller{
		config:       config,
		currentState: ActionIdle,
		initialized:  false,
	}
}

// Evaluate processes a temperature reading and returns the action to take.
func (c *Controller) Evaluate(currentTemp float64) Result {
	cfg := c.config

	// Error: sensor out of range
	if currentTemp < cfg.SensorMin || currentTemp > cfg.SensorMax {
		return Result{Action: ActionIdle, StateChanged: false}
	}

	lowerThreshold := cfg.TargetTemp - cfg.Deadband/2
	upperThreshold := cfg.TargetTemp + cfg.Deadband/2

	var newState Action

	if !c.initialized {
		c.initialized = true
		c.currentState = ActionIdle
	}

	switch c.currentState {
	case ActionIdle:
		if currentTemp < lowerThreshold {
			newState = ActionHeat
		} else if currentTemp > upperThreshold {
			newState = ActionCool
		} else {
			newState = ActionIdle
		}
	case ActionHeat:
		if currentTemp >= upperThreshold {
			newState = ActionIdle
		} else {
			newState = ActionHeat
		}
	case ActionCool:
		if currentTemp <= lowerThreshold {
			newState = ActionIdle
		} else {
			newState = ActionCool
		}
	}

	changed := newState != c.currentState
	c.currentState = newState

	return Result{Action: newState, StateChanged: changed}
}

func sanitizeConfig(cfg Config) Config {
	// Clamp target to valid range
	cfg.TargetTemp = math.Max(cfg.TargetMin, math.Min(cfg.TargetMax, cfg.TargetTemp))

	// Deadband: zero or negative → default; otherwise clamp to [0.5, 10.0]
	if cfg.Deadband <= 0 {
		cfg.Deadband = 2.0
	} else {
		cfg.Deadband = math.Max(0.5, math.Min(10.0, cfg.Deadband))
	}

	return cfg
}
