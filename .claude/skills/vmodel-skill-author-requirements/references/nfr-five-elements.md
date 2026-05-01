# NFR five-element rule

When drafting an NFR: name system / response / metric+unit / target+stat-level / condition. Emit a finding for any missing slot.

## The five elements

| # | Element | Detail |
|---|---|---|
| 1 | **System or subsystem** | Specific subsystem under measurement |
| 2 | **Response or behaviour** | What is being measured (latency, throughput, availability, error rate) |
| 3 | **Metric with unit** | Milliseconds, requests/second, bytes, nines of availability, percentage |
| 4 | **Target value** | At correct statistical level — percentile (p50/p95/p99) for latency; rate over window for availability; sustained vs peak for throughput |
| 5 | **Condition** | Load, environment, operating mode, measurement point |

## Slot-fill template

```yaml
nfr:
  id: REQ-NNN
  type: quality_attribute
  statement: |
    The <ELEMENT 1: system/subsystem>
    shall <ELEMENT 2: response>
    in <ELEMENT 3: metric+unit>
    at <ELEMENT 4: target value at statistical level>
    under <ELEMENT 5: condition — load + environment + measurement point>.
  rationale: <why this target; what trade-off, derived from what>
  derived_from: [<upstream needs / parent NFRs / inherited constraints>]
```

## Worked example

```
BEFORE — three elements missing:
"Session validation shall be fast."
                ↑ no metric, no target, no condition

AFTER — all five elements present:
REQ-015: "The session-validation endpoint           ← 1. system named
            shall respond                           ← 2. response named
            in ≤ 50 ms                              ← 3. metric + unit
            at p95                                  ← 4. target at percentile
            under 5,000 concurrent sessions in      ← 5. condition
            the production deployment,                  (load + env +
            measured at the API gateway."               measurement point)

  rationale: "Validation runs on every authenticated request. The 50 ms p95
              budget derives from the parent-scope end-to-end commitment of
              400 ms at p95 and the eight downstream operations sharing the
              budget (roughly 45 ms each plus overhead)."
```

## Condition slots

Common conditions to spell out:

- **Load**: concurrent users / requests-per-second / data volume
- **Environment**: staging vs production; deployment region; hardware class
- **Operating mode**: normal / degraded / cold-start / failover
- **Measurement point**: API gateway / application boundary / persistence layer

## Multi-level targets — Planguage

Use Planguage when targets are tiered (scale / meter / fail / goal / stretch / wish).

```yaml
availability_session_validation:
  scale:    "percentage of authenticated-user-facing requests completed within SLO"
  meter:    "computed over 30-day rolling window, excluding planned maintenance"
  fail:     "< 99.5%"      # below this the system is in violation
  goal:     "≥ 99.9%"      # target the team commits to
  stretch:  "≥ 99.95%"     # desirable, not committed
  wish:     "99.99%"       # aspirational; not a commitment
```

`scale` and `meter` are non-negotiable; a target without them is a number, not a measurement.

## Compliance constraints translate into derived NFRs (and other types)

Regulatory regimes (GDPR, HIPAA, PCI-DSS, WCAG, SOC 2) state obligations that must be translated into testable requirements. The translation is a **derivation step**, captured explicitly:

```yaml
inherited_constraints:
  - id: IC-NNN
    source: "GDPR Article 17 (Right to erasure)"
    summary: "Data subjects may request erasure of personal data without undue delay."
    category: regulatory

derived_requirements:
  - id: REQ-NNN
    statement: "When an authenticated data subject submits an erasure request, the
                session service shall destroy all active sessions and remove
                directly-identifying fields from session history records within 7
                calendar days of receiving the request."
    derivation: derived
    derived_from: [IC-NNN]
    rationale: "Article 17(1) requires erasure without undue delay; our
                contractual data-processing commitment is 7 days specifically.
                History records are pseudonymised rather than deleted to preserve
                the audit-log legal-obligation exception (Article 17(3)(b))."
```

Pattern: regulation captured once with its source; each testable behaviour it implies is a derived requirement with `derived_from` linking back. The rationale explains the translation choice (e.g. "7 days" is not in the regulation; it is the contractual commitment).
