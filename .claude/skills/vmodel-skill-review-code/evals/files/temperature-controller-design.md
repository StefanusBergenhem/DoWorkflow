# Temperature Controller

Monitors temperature readings from a sensor and decides whether to activate heating, cooling, or neither. Uses a deadband (hysteresis) to prevent rapid toggling between states.

## How it works

The controller receives a current temperature reading and compares it against a target temperature. If the temperature is below the target minus half the deadband, it activates heating. If above the target plus half the deadband, it activates cooling. Otherwise it holds the current state (no change).

The controller tracks its current state (heating, cooling, or idle) and only changes state when the temperature crosses a deadband boundary. This prevents the system from toggling between heating and cooling when the temperature hovers near the target.

## Inputs

- `currentTemp` - float, the current temperature in Celsius. Can be any value from -50.0 to 150.0.
- `targetTemp` - float, the desired temperature in Celsius. Must be between 0.0 and 100.0.
- `deadband` - float, the hysteresis band width in degrees. Must be positive, minimum 0.5, maximum 10.0. Default is 2.0.

## Output

Returns an object with:
- `action` - one of: "HEAT", "COOL", or "IDLE"
- `stateChanged` - boolean, true if the action differs from the previous call's action

## Behavior

- When idle and temperature drops below (target - deadband/2): switch to HEAT
- When idle and temperature rises above (target + deadband/2): switch to COOL
- When heating and temperature reaches (target + deadband/2): switch to IDLE
- When cooling and temperature drops to (target - deadband/2): switch to IDLE
- When heating and temperature is still below target + deadband/2: stay HEAT
- When cooling and temperature is still above target - deadband/2: stay COOL
- First call always starts from IDLE state

## Error cases

- If currentTemp is outside the sensor range (-50 to 150), return IDLE with stateChanged=false and log a warning
- If targetTemp is outside valid range (0 to 100), clamp it to the nearest valid value
- If deadband is below 0.5 or above 10.0, clamp it to the nearest valid value
- If deadband is zero or negative, use the default (2.0)

## Configuration defaults

- Default target: 22.0 (room temperature)
- Default deadband: 2.0
- Sensor range: -50.0 to 150.0
- Valid target range: 0.0 to 100.0
