# NFR five-element rule — checks

**Contents**
- [The five elements](#the-five-elements)
- [Element-presence checks](#element-presence-checks) — system, response, metric, target, condition
- [Multi-level targets — Planguage checks](#multi-level-targets--planguage-checks)
- [Form-mixing check](#form-mixing-check)

---

Five-element rule: every NFR contains system, response, metric+unit, target+stat-level, condition. Emit a finding for any missing slot.

## The five elements

| # | Element | What it names |
|---|---|---|
| 1 | **System or subsystem** | Specific subsystem under measurement |
| 2 | **Response or behaviour** | What is being measured (latency, throughput, availability, error rate) |
| 3 | **Metric with unit** | Milliseconds, requests/second, bytes, nines of availability, percentage |
| 4 | **Target value** | At correct statistical level (percentile for latency, rate over window for availability) |
| 5 | **Condition** | Load, environment, operating mode, measurement point |

## Element-presence checks

For every requirement filed under the Quality Attributes (NFR) section, walk these five checks. Each missing element is its own finding. (Multiple findings on one NFR is normal — they do not aggregate; each one is surfaced.)

### Check 1 — System or subsystem named

Is the subsystem the requirement applies to specifically named? "The system" generically (in a multi-component scope) is too coarse.

- **check_failed**: `check.nfr.missing-system`
- **severity**: `soft_reject`
- **evidence shape**: quote the statement; note that no specific subsystem is named
- **recommended_action**: *"Name the specific subsystem. See `nfr-five-elements-checks.md` element 1."*

### Check 2 — Response or behaviour named

Is the response or behaviour being measured specified? Statements like "shall be performant" or "shall meet expectations" name no concrete behaviour.

- **check_failed**: `check.nfr.missing-response`
- **severity**: `soft_reject`
- **evidence shape**: quote the statement; note that no measurable response is specified
- **recommended_action**: *"Name the response or behaviour being measured (e.g., 'shall respond', 'shall complete', 'shall sustain'). See `nfr-five-elements-checks.md` element 2."*

### Check 3 — Metric with unit

Is a metric with a unit present? Numbers without units (e.g., "shall respond in 50") and units without numbers (e.g., "in milliseconds") are both incomplete.

- **check_failed**: `check.nfr.missing-metric-unit`
- **severity**: `soft_reject`
- **evidence shape**: quote the statement; note the missing piece
- **recommended_action**: *"Add a metric with unit (ms, requests/second, percentage of availability). See `nfr-five-elements-checks.md` element 3."*

### Check 4 — Target value at correct statistical level

For latency targets, prefer percentile (p50/p95/p99). Flag bare mean. For availability: rate over window. For throughput: sustained vs peak.

- **check_failed**: `check.nfr.missing-target` (no number) or `check.nfr.missing-statistical-level` (number present, level missing or wrong)
- **severity**: `soft_reject`
- **evidence shape**: quote the statement; note the missing or incorrect statistical level
- **recommended_action**: *"Specify the statistical level (e.g., 'at p95', not just '≤ 50 ms'). See `nfr-five-elements-checks.md` element 4."*

### Check 5 — Condition

Is the condition under which the measurement is valid stated? Load, environment, operating mode, measurement point. **This is the most-omitted element**.

```
Without condition:
"The system shall respond in ≤ 50 ms at p95"
                                              ↑ no load, no env, no measurement point

With condition:
"The session-validation endpoint shall respond in ≤ 50 ms at p95
 under 5,000 concurrent sessions in the production deployment,
 measured at the API gateway."
```

Without a condition, the metric is not reproducible: trivially satisfied under 1% load and trivially failed under 100% load.

- **check_failed**: `check.nfr.missing-condition`
- **severity**: `soft_reject`
- **evidence shape**: quote the statement; note that load / environment / measurement point are absent
- **recommended_action**: *"Add the measurement condition: load (concurrent users / RPS), environment (staging vs production), operating mode (normal / degraded / cold-start), and measurement point. See `nfr-five-elements-checks.md` element 5."*

## Multi-level targets — Planguage checks

When the NFR uses multi-level Planguage form (instead of a flat statement), check the canonical fields:

```yaml
planguage:
  scale:    "<what is being measured>"
  meter:    "<how, over what window; exclusions>"
  fail:     "<below which the system is in violation>"
  goal:     "<committed target>"
  stretch:  "<desirable, not committed>"
  wish:     "<aspirational>"
```

Two fields are non-negotiable: `scale` and `meter`. A target without a scale (what is being measured) and a meter (how, over what window) is a number, not a measurement.

- **check_failed**: `check.nfr.planguage-missing-scale` or `check.nfr.planguage-missing-meter`
- **severity**: `soft_reject`
- **evidence shape**: quote the planguage block; name the missing field
- **recommended_action**: *"Add the missing `scale` (what is being measured) and/or `meter` (how, over what window) field. See `nfr-five-elements-checks.md` Planguage section."*

The `fail`, `goal`, `stretch`, `wish` fields are recommended but not all required. At minimum `fail` and `goal` should be present (the violation threshold and the commitment).

- **check_failed**: `check.nfr.planguage-missing-goal-or-fail`
- **severity**: `info`
- **recommended_action**: *"Add `goal` (committed target) and/or `fail` (violation threshold) for completeness. `stretch` and `wish` are optional."*

## Form-mixing check

The NFR should use either flat-statement form OR Planguage form, not both. Mixing two forms in one requirement is confusing.

- **check_failed**: `check.nfr.mixed-forms`
- **severity**: `soft_reject`
- **recommended_action**: *"Pick one form per NFR: flat five-element statement OR Planguage tiered block. Do not combine."*
