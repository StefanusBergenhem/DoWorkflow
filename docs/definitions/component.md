# Component

## Definition

A **component** is an architectural building block — the result of decomposing the system during Software Architectural Design. It groups related functionality behind defined interfaces and represents a subsystem boundary.

## In V-Model Standards

| Standard | Where It Lives | Term Used |
|----------|---------------|-----------|
| ASPICE | SWE.2 (Software Architectural Design) | Software Component |
| DO-178C | Software Architecture (HLR level) | Component / Module |
| ISO 26262 | Part 6, Clause 7 (Software Architectural Design) | Software Component |
| IEC 62304 | 5.3 (Software Architectural Design) | Software Item / Module |

## What It Captures

- Responsibilities and purpose within the system
- Interfaces to other components (provided and required)
- Internal decomposition into units
- Non-functional constraints (timing budgets, resource limits, partitioning)
- Data flow and control flow between components

## Practical Mapping

In a typical codebase, a component maps to a **package**, **module**, or **bounded subsystem**:

```
Component: "fuel-control"          (Java package / Go package / C++ namespace)
  ├── Unit: FuelRateLimiter        (class / struct / module)
  ├── Unit: FuelModeResolver       (class / struct / module)
  └── Unit: FuelValveController    (class / struct / module)
```

A component may contain anywhere from a handful to dozens of units. The component defines *boundaries* between subsystems; the units within it implement the actual behavior and logic.

## Relationship to Other Concepts

- A component is defined in the **SW Architecture** artifact
- A component contains one or more **units**
- Each unit belongs to exactly one component
- Component-level interfaces are verified by **integration tests**
- Unit-level interfaces within a component are verified by **unit tests**
