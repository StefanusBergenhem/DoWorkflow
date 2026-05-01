# Deployment intent (root scope only)

The root-scope Architecture's Composition section carries one concern no branch does: deployment intent. Architecture Composition is the authoritative spec for deployment; Infrastructure-as-Code (terraform, Kubernetes manifests, compose files) is the *implementation* of that intent. There is no separate prose Detailed Design layer above the IaC.

**Branch scopes inherit deployment intent from root** — do not re-state it. SKILL.md Step 8 is skipped at branch scope.

## Contents

- Environments
- Orchestration target
- Runtime-unit boundaries
- IaC as implementation
- 12-factor stance
- Cost as a constraint
- Architecture-as-code tooling
- Slot-fill check

## Environments

Every environment the system runs in is named (dev, staging, production, production-EU, customer-tenant-X). Differences across environments are enumerated:

- Scale (pod counts, DB instance size, partition counts)
- Data set (synthetic / anonymised / live)
- External integrations (sandbox vs live PSP, etc.)
- Observability stack (which telemetry destination per environment)

Where environment differences matter behaviourally, tie them to specific requirements or ADRs. *Environments that exist but are not enumerated are environments that drift.*

## Orchestration target

The target platform for runtime composition. The choice determines what "wiring" concretely means and bounds the operational primitives the rest of the architecture can reach for.

| Target | What "wiring" concretely means |
|---|---|
| **Kubernetes** | Distribution (EKS / GKE / AKS / on-prem), ingress controller, service mesh choice, namespace strategy |
| **Serverless / FaaS** | Provider, FaaS runtime, managed services (queues, DBs), cold-start budget |
| **Container orch (ECS / Nomad / Swarm)** | Service definitions, task placement, networking mode |
| **VM-based** | systemd units, cloud-init scripts, image-build pipeline |
| **Bare-metal / embedded** | Firmware image, hardware bring-up sequence, boot ROM contract |

Name the target *and* the rationale (or `governing_adrs:` reference). Default to whatever ops already runs is a legitimate rationale; default to "Kubernetes because resume-driven" is not.

## Runtime-unit boundaries

A **runtime unit** is a unit of:

- Deployment (one ship cadence, one rollback target)
- Scaling (one autoscaler, one resource budget)
- Failure (one blast radius — when this unit dies, what fails with it?)

Each runtime unit has its own:

- Lifecycle (start, stop, rolling restart)
- Observability surface (metrics, logs, traces emerge from this unit)
- Resource budget (CPU, memory, concurrency limits)
- Failure story (degradation behaviour when this unit is down)

The Architecture maps which Decomposition components live in which runtime units. **Co-location is a choice** — shared fate is the cost. **Separation is a choice** — network boundary and operational overhead are the cost. State the rationale per unit.

## IaC as implementation

Terraform, Kubernetes manifests, Helm charts, and compose files are declarative: they describe the runtime; the platform applies them. Architecture Composition specifies the *intent*:

- What environments exist
- What the orchestration target is
- Where the unit boundaries fall

IaC artifacts implement that intent concretely. **Do not re-specify what IaC already declares.** Architecture states what IaC cannot capture: rationale, the tying of choices to requirements and ADRs, the unit-boundary logic behind the manifests.

Slot-fill:

```yaml
deployment_intent:
  iac_repo: "<<path or sibling repo reference>>"
  capacity_lives_in: "<<IaC; not in this artifact>>"
  unit_boundary_logic_lives_in: "<<this artifact>>"
```

Future changes to capacity go through IaC. Changes to the unit-boundary structure go through the Architecture.

## 12-factor stance

Heroku's *Twelve-Factor App* (Wiggins, 2011) catalogues operational norms — config in environment, processes disposable, logs as streams, dev/prod parity. Root-scope Architecture states the stance factor by factor where the factor is load-bearing.

Common pattern: list which factors apply unmodified, which are explicitly departed-from with rationale.

```yaml
twelve_factor:
  applied: [config, processes, port_binding, disposability, logs]
  departures:
    - factor: "Backing services as attached resources"
      reason: "<<e.g. on-prem DB cluster cannot be torn down per environment; treated as a fixture>>"
```

## Cost as a constraint

Serverless vs container vs VM; managed vs self-hosted; hot replica vs cold backup; multi-region active-active vs active-passive — all cost decisions as much as architectural ones. Cost sits under Constraints in Requirements; at Architecture it resurfaces as a binding input to orchestration and runtime-unit choices.

Name three things at root scope:

- **Cost envelope** — monthly infra spend ceiling
- **Cost-per-request target** — where this maps to a unit cost
- **Cost-of-a-9** — the marginal cost of the next 9 of availability (where availability is binding)

A well-engineered architecture killed by the bill is a category of failure the Architecture artifact is meant to prevent.

## Architecture-as-code tooling

Mermaid is the framework default — text, version-controllable, AI-readable. Structurizr, PlantUML, D2, draw.io are legitimate alternatives **when the team is already committed to them**. The mandate is that Structure Diagram and sequence diagrams live *in the artifact*, not as images pasted from a whiteboarding tool.

## Slot-fill check

- [ ] Every environment named with differences enumerated
- [ ] Orchestration target chosen and rationale (or `governing_adrs:`) stated
- [ ] Every component in the Decomposition mapped to a runtime unit
- [ ] Every runtime unit has lifecycle / observability / resource-budget / failure-story
- [ ] IaC paths referenced; capacity is not re-specified in this artifact
- [ ] 12-factor applied/departed list stated where load-bearing
- [ ] Cost envelope + per-request target + cost-of-a-9 named (root only)

Cross-link: SKILL.md Step 8; `composition-patterns.md` (wiring concerns).
