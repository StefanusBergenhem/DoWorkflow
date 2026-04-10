---
artifact_id: CD-001
artifact_type: detailed-design
version: 1.0.0
status: approved
component: fuel-control
layer: 2
trace_from: [ARCH-004, ARCH-005]
---

# Fuel Control

## Purpose

Controls fuel delivery to the engine across all operational modes. Ensures fuel rate stays within safe bounds during mode transitions (startup, cruise, shutdown) and prevents hardware damage from sudden rate changes.

Without this component, the engine controller would send raw fuel rate requests directly to the fuel valve — risking ignition failure during startup (rate too low), engine damage during cruise (rate too high), or unsafe fuel residue during shutdown (non-zero rate).

## External Interfaces

| Direction | Partner Component | Mechanism | Data | Description |
|-----------|-------------------|-----------|------|-------------|
| Receives | engine-control | function call | FuelRateRequest | Requested rate + current operational mode |
| Provides | engine-control | return value | FuelRateResult | Clamped rate + clamping metadata |
| Receives | system-clock | function call | ElapsedTime | Time since last invocation for rate-of-change calc |

## Unit Inventory

| Unit ID | Layer | Brief Description |
|---------|-------|-------------------|
| FuelRateLimiter | 2 | Clamps fuel rate to mode-specific bounds and enforces rate-of-change limits |

## Shared Patterns

### Error Strategy

All units in this component use a **fail-safe** approach: unrecognized or invalid inputs are treated as the safest possible value (emergency shutdown mode, zero rate). No exceptions are thrown — errors are communicated through return values (clamping_reason field).

### Threading Model

Units may be called from multiple control loops concurrently. All units must be thread-safe. No shared mutable state between units — each unit manages its own internal state independently.

## Constraints

- All computations must complete in constant time — no allocations, no unbounded loops
- Deterministic: same inputs and state must always produce same outputs
- Designed for real-time control loop execution at up to 1kHz

---

## Fuel Rate Limiter (FuelRateLimiter)

### Purpose

Clamps the requested fuel rate to mode-specific bounds and enforces rate-of-change limits to prevent sudden fuel delivery changes that could damage the engine or cause unstable combustion.

### Interfaces

#### Inputs

| ID | Name | Type | Unit | Constraints | Description |
|----|------|------|------|-------------|-------------|
| I1 | requested_rate | float | liters/hour | >= 0.0 | Desired fuel rate from the controller |
| I2 | operational_mode | enum | — | startup, cruise, emergency_shutdown | Current engine operational mode |
| I3 | elapsed_time_ms | integer | milliseconds | >= 0 | Time since last call, for rate-of-change calculation |

#### Outputs

| ID | Name | Type | Unit | Constraints | Description |
|----|------|------|------|-------------|-------------|
| O1 | actual_rate | float | liters/hour | >= 0.0, <= 500.0 | The rate after applying all limits |
| O2 | was_clamped | boolean | — | — | True if actual_rate differs from requested_rate |
| O3 | clamping_reason | enum | — | none, mode_max, mode_min, rate_of_change, emergency | Why clamping was applied |

### Behavior

| ID | Condition | Result |
|----|-----------|--------|
| B1 | operational_mode is startup AND requested_rate < STARTUP_MIN_RATE | Set actual_rate to STARTUP_MIN_RATE, was_clamped to true, clamping_reason to mode_min |
| B2 | operational_mode is startup AND requested_rate > STARTUP_MAX_RATE | Set actual_rate to STARTUP_MAX_RATE, was_clamped to true, clamping_reason to mode_max |
| B3 | operational_mode is startup AND STARTUP_MIN_RATE <= requested_rate <= STARTUP_MAX_RATE | Set actual_rate to requested_rate, was_clamped to false, clamping_reason to none |
| B4 | operational_mode is cruise AND requested_rate > CRUISE_MAX_RATE | Set actual_rate to CRUISE_MAX_RATE, was_clamped to true, clamping_reason to mode_max |
| B5 | operational_mode is cruise AND rate-of-change exceeds limit: abs(requested_rate - previous_rate) > MAX_RATE_CHANGE * elapsed_time_ms / 1000 | Clamp actual_rate to previous_rate ± (MAX_RATE_CHANGE * elapsed_time_ms / 1000), was_clamped to true, clamping_reason to rate_of_change |
| B6 | operational_mode is cruise AND within bounds and rate-of-change limit | Set actual_rate to requested_rate, was_clamped to false, clamping_reason to none |
| B7 | operational_mode is emergency_shutdown | Set actual_rate to 0.0, was_clamped to true, clamping_reason to emergency (regardless of requested_rate) |
| B8 | Always (after computing actual_rate) | Update internal state: previous_rate = actual_rate |

### Error Handling

| ID | Condition | Response |
|----|-----------|----------|
| E1 | requested_rate is negative | Treat as 0.0, set was_clamped to true, clamping_reason to mode_min |
| E2 | elapsed_time_ms is negative | Treat as 0, skip rate-of-change limiting for this call |
| E3 | operational_mode is not a recognized value | Treat as emergency_shutdown (fail-safe) |

### Configuration

| Name | Type | Default | Description |
|------|------|---------|-------------|
| STARTUP_MIN_RATE | float (liters/hour) | 10.0 | Minimum fuel rate during startup for stable ignition |
| STARTUP_MAX_RATE | float (liters/hour) | 50.0 | Maximum fuel rate during startup |
| CRUISE_MAX_RATE | float (liters/hour) | 200.0 | Maximum fuel rate during cruise |
| MAX_RATE_CHANGE | float (liters/hour/second) | 100.0 | Maximum allowed rate change per second |

### Internal State

| Name | Type | Description |
|------|------|-------------|
| previous_rate | float | The actual_rate from the previous call, used for rate-of-change limiting |

### Constraints

- Must be thread-safe — may be called from multiple control loops
- Must execute in constant time (no allocations, no loops)
- Must be deterministic — same inputs and state produce same outputs
