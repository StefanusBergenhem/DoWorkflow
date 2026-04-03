# Unit

## Definition

A **unit** is the smallest independently testable piece of software within a component. It is the artifact that gets a detailed design and the thing you write unit tests against.

## In V-Model Standards

| Standard | Where It Lives | Term Used |
|----------|---------------|-----------|
| ASPICE | SWE.3 (Software Detailed Design and Unit Construction) | Software Unit |
| DO-178C | Low-Level Requirements (LLR) | Software Module / Unit |
| ISO 26262 | Part 6, Clause 8 (Software Unit Design) | Software Unit |
| IEC 62304 | 5.4 (Software Detailed Design) | Software Unit |

## What It Captures

A unit encapsulates a single, well-defined piece of functionality:

- Interfaces (inputs and outputs with types, units, constraints)
- Behavior (how it transforms inputs to outputs)
- Error handling (what happens when things go wrong)
- Internal state (if stateful)
- Constraints (thread-safety, timing, resource limits)

## Practical Mapping

In a typical codebase, a unit maps to a **class**, **function**, **struct**, or **source module** — the level at which you write unit tests:

```
Component: "fuel-control"
  ├── Unit: FuelRateLimiter        ← complex algorithm, safety-critical
  ├── Unit: FuelModeResolver       ← decision logic, moderate complexity
  └── Unit: FuelModeConfig         ← data holder, trivial
```

Not all units are equal. A safety-critical algorithm and a simple data transfer object are both "units" but demand very different levels of design documentation.

## Relationship to Other Concepts

- A unit belongs to exactly one **component**
- A unit's design is captured in the **detailed design** artifact
- A unit is verified by **unit tests** derived from its detailed design
- The level of design detail required depends on the unit's **criticality** and **complexity**
