# EARS: Easy Approach to Requirements Syntax

## Research Summary

EARS (Easy Approach to Requirements Syntax) is a structured natural language
methodology for writing requirements, developed by Alistair Mavin and colleagues
at Rolls-Royce plc. First published in 2009 at the IEEE International
Requirements Engineering Conference (RE'09), EARS provides a set of sentence
templates that constrain how requirements are written, eliminating common
ambiguities while keeping requirements readable by all stakeholders.

**Key paper:** Mavin, A., Wilkinson, P., Harwood, A., & Novak, M. (2009). "Easy
Approach to Requirements Syntax (EARS)." *Proceedings of the 17th IEEE
International Requirements Engineering Conference (RE'09)*, pp. 317-322.

**Follow-up paper:** Mavin, A. & Wilkinson, P. (2010). "Big Ears (The Return of
Easy Approach to Requirements Syntax)." *Proceedings of the 18th IEEE
International Requirements Engineering Conference (RE'10)*, pp. 277-282.

---

## 1. EARS Templates and Patterns

EARS defines five core sentence templates and one composite pattern. Each
template addresses a specific type of system behavior. Every requirement follows
the fundamental structure: **the system SHALL do something**, but with varying
prefixes that establish context.

### 1.1 Ubiquitous Requirements

**Template:**
```
The <system name> shall <system response>.
```

**Purpose:** Describes behavior that the system must always exhibit, with no
trigger, precondition, or optional context. These are unconditional, always-on
requirements.

**Characteristics:**
- No keyword prefix (no When, While, Where, or If)
- The behavior is invariant -- it must hold at all times
- Often describes fundamental properties, constraints, or qualities

**Examples:**
```
The flight control system shall provide three independent channels.

The database shall encrypt all data at rest using AES-256.

The braking system shall comply with ISO 26262 ASIL-D.

The system shall maintain an audit log of all user actions.
```

**Common use cases:**
- Architectural constraints
- Compliance requirements
- Non-functional properties (performance floors, security baselines)
- Safety invariants

### 1.2 Event-Driven Requirements

**Template:**
```
When <trigger>, the <system name> shall <system response>.
```

**Purpose:** Describes behavior that occurs in response to a specific event or
trigger. The trigger is a discrete, detectable occurrence.

**Characteristics:**
- Keyword: **When**
- The trigger is a single event (not an ongoing state)
- The response happens once per occurrence of the trigger
- Implies a cause-effect relationship

**Examples:**
```
When the pilot presses the engine start button, the engine control system
shall initiate the start sequence.

When the user submits a login form, the authentication service shall
validate the credentials within 2 seconds.

When a sensor reading exceeds the upper threshold, the monitoring system
shall raise an alarm.

When the vehicle speed drops below 5 km/h, the auto-hold system shall
engage the parking brake.
```

**Common use cases:**
- User interactions (button presses, form submissions)
- Signal detection (threshold crossings, message receipt)
- State transitions triggered by external events
- Timeout expirations

### 1.3 State-Driven Requirements (Unwanted Behavior)

**Template:**
```
While <state>, the <system name> shall <system response>.
```

**Purpose:** Describes behavior that must be maintained continuously as long as
a particular state or condition holds.

**Characteristics:**
- Keyword: **While**
- The state is an ongoing condition, not a momentary event
- The behavior persists for the entire duration of the state
- Implies continuous or periodic action

**Examples:**
```
While the aircraft is in flight, the flight recorder shall record all
sensor data at 1 Hz.

While the system is in maintenance mode, the API gateway shall reject
all external requests with HTTP 503.

While the engine is running, the lubrication system shall maintain oil
pressure above 2.5 bar.

While the battery level is below 20%, the device shall disable
non-essential background processes.
```

**Common use cases:**
- Mode-dependent behavior
- Continuous monitoring requirements
- Resource management during specific states
- Safety behavior during hazardous conditions

### 1.4 Optional Feature Requirements

**Template:**
```
Where <feature>, the <system name> shall <system response>.
```

**Purpose:** Describes behavior that only applies when a particular feature,
configuration, or option is included in the system. Addresses product-line
variability.

**Characteristics:**
- Keyword: **Where**
- The feature clause identifies an optional capability or configuration
- The requirement only applies to system variants that include the feature
- Useful for product lines, optional modules, and configurable deployments

**Examples:**
```
Where the system includes a touchscreen display, the navigation system
shall support pinch-to-zoom gestures.

Where dual-redundancy is configured, the controller shall switch to the
backup channel within 50 ms of primary channel failure.

Where the premium audio package is installed, the infotainment system
shall support Dolby Atmos decoding.

Where LDAP integration is enabled, the authentication service shall
validate credentials against the configured LDAP directory.
```

**Common use cases:**
- Product line variability
- Optional hardware configurations
- Licensed feature tiers
- Deployment-specific behavior (cloud vs. on-premise)

### 1.5 Unwanted Behavior Requirements

**Template:**
```
If <unwanted condition>, then the <system name> shall <system response>.
```

**Purpose:** Describes the system's response to undesirable situations --
failures, errors, out-of-range inputs, or hazardous conditions. This is the
safety and fault-tolerance pattern.

**Characteristics:**
- Keywords: **If ... then**
- The condition describes something that should not happen but might
- The response is typically protective, corrective, or mitigating
- Directly supports safety analysis (FMEA, FTA, HAZOP)
- The "then" keyword distinguishes this from event-driven requirements

**Examples:**
```
If the primary power supply fails, then the uninterruptible power supply
shall provide backup power for a minimum of 30 minutes.

If the communication link is lost for more than 5 seconds, then the
autonomous vehicle shall initiate a safe stop maneuver.

If the input temperature reading is outside the range -40C to +85C, then
the sensor interface shall flag the reading as invalid and use the last
known good value.

If a database transaction deadlock is detected, then the transaction
manager shall roll back the youngest transaction and retry after 100 ms.
```

**Common use cases:**
- Fault tolerance and graceful degradation
- Safety responses to hazardous conditions
- Error handling and recovery
- Input validation and boundary conditions

### 1.6 Complex (Compound) Requirements

**Template:**
```
While <state>, when <trigger>, the <system name> shall <system response>.

Where <feature>, while <state>, when <trigger>, the <system name> shall
<system response>.

Where <feature>, if <unwanted condition>, then the <system name> shall
<system response>.
```

**Purpose:** Combines multiple EARS keywords to express requirements that depend
on intersecting conditions. The "Big Ears" extension (2010 paper) formalized
these combinations.

**Ordering rule:** When combining keywords, the canonical order is:
```
Where -> While -> When -> If/then -> shall
```

**Examples:**
```
While the aircraft is on the ground, when the pilot selects reverse
thrust, the engine control system shall deploy the thrust reversers.

Where the enhanced ground proximity warning system is installed, while
the aircraft is in the approach phase, when the terrain clearance drops
below 500 ft, the EGPWS shall issue an aural warning.

While the reactor is in startup mode, if the neutron flux exceeds the
startup rate limit, then the reactor protection system shall insert the
control rods within 2 seconds.

Where the anti-lock braking option is fitted, while the vehicle is
moving above 10 km/h, when the driver applies the brake pedal, the ABS
controller shall modulate brake pressure to prevent wheel lock.
```

**Combination guidelines:**
- Keep combinations to two or at most three keywords
- If a requirement needs more than three keywords, consider splitting it
- Each keyword adds a testable dimension to the requirement
- The ordering convention improves consistency and parseability

---

## 2. Benefits of EARS Over Natural Language Requirements

### 2.1 Problems with Unconstrained Natural Language

Unconstrained natural language requirements commonly suffer from:

| Problem | Example |
|---------|---------|
| **Ambiguity** | "The system should handle errors appropriately" |
| **Vagueness** | "The system shall be fast" |
| **Incompleteness** | Missing trigger, missing response, missing conditions |
| **Optionality confusion** | Mixing "shall", "should", "may", "will" |
| **Buried rationale** | Mixing requirements with design justification |
| **Untestability** | No clear pass/fail criteria |
| **Noise** | Non-requirements disguised as requirements |

Studies by Mavin et al. found that in industrial requirements sets, roughly 50%
of requirements written in unconstrained natural language contained defects
detectable through syntactic analysis alone.

### 2.2 How EARS Addresses These Problems

**Structured yet accessible.** EARS requirements are constrained natural
language -- they remain readable by all stakeholders (engineers, managers,
customers, regulators) while following strict syntactic rules that prevent common
defects.

**Forces completeness.** Each template requires specific slots to be filled:
- Event-driven: you must name the trigger AND the response
- State-driven: you must name the state AND the ongoing behavior
- Unwanted behavior: you must name the bad condition AND the mitigation

**Eliminates ambiguous requirement types.** By choosing a template, the author
must decide: is this always true (ubiquitous)? Is it triggered by an event? Is
it mode-dependent? Is it a fault response? This classification itself reveals
gaps in understanding.

**Low adoption barrier.** Unlike formal methods (Z, B, VDM), EARS requires no
mathematical training. Engineers can learn the five templates in under an hour
and begin writing improved requirements immediately.

**Measurable improvement.** Rolls-Royce reported significant reduction in
requirements defects after adopting EARS across their engine control programs.
Independent studies at companies including Philips, Bosch, and NASA have
confirmed similar improvements.

**Traceability support.** The keyword-based structure makes it straightforward
to trace requirements to test cases, design elements, and safety analysis
artifacts.

### 2.3 EARS vs. Other Approaches

| Approach | Readability | Precision | Adoption Cost | Tool Support |
|----------|------------|-----------|---------------|-------------|
| Free-form NL | High | Low | None | Minimal |
| EARS | High | Medium-High | Very Low | Growing |
| Boilerplates (Rupp) | Medium | Medium | Low | Moderate |
| SysML/UML | Low (for non-engineers) | High | High | Strong |
| Formal (Z, B) | Very Low | Very High | Very High | Specialized |

---

## 3. EARS and Testability

Each EARS pattern naturally suggests a test structure. This is one of the
methodology's most powerful properties: a well-written EARS requirement is
inherently testable because it specifies the observable conditions and expected
behavior.

### 3.1 Pattern-to-Test Mapping

**Ubiquitous -> Invariant Test**
```
Requirement: The system shall encrypt all data at rest using AES-256.

Test structure:
  GIVEN  any data stored by the system
  THEN   that data is encrypted with AES-256
  (Verify at all times, all states, all configurations)
```

**Event-Driven -> Stimulus-Response Test**
```
Requirement: When the user clicks "Submit", the form handler shall validate
             all required fields.

Test structure:
  GIVEN  the form is displayed with required fields
  WHEN   the user clicks "Submit"
  THEN   all required fields are validated
```

**State-Driven -> Mode/Duration Test**
```
Requirement: While the system is in safe mode, the controller shall disable
             all actuator outputs.

Test structure:
  GIVEN  the system enters safe mode
  WHILE  safe mode is active
  THEN   all actuator outputs are disabled
  AND    they remain disabled until safe mode exits
```

**Optional Feature -> Configuration-Variant Test**
```
Requirement: Where GPS is installed, the navigation system shall display
             the current position.

Test structure:
  GIVEN  a system variant WITH GPS installed
  THEN   current position is displayed
  AND
  GIVEN  a system variant WITHOUT GPS installed
  THEN   no position display requirement applies
```

**Unwanted Behavior -> Fault Injection / Negative Test**
```
Requirement: If the primary sensor fails, then the controller shall switch
             to the backup sensor within 100 ms.

Test structure:
  GIVEN  normal operation with primary sensor active
  WHEN   the primary sensor is made to fail (fault injection)
  THEN   the controller switches to backup sensor
  AND    the switchover completes within 100 ms
```

**Complex -> Multi-Condition Test Matrix**
```
Requirement: While in cruise mode, when the driver presses the brake pedal,
             the cruise control system shall disengage.

Test structure:
  GIVEN  the vehicle is in cruise mode
  WHEN   the driver presses the brake pedal
  THEN   cruise control disengages
  AND
  GIVEN  the vehicle is NOT in cruise mode
  WHEN   the driver presses the brake pedal
  THEN   cruise control behavior is not relevant (boundary test)
```

### 3.2 Test Coverage Analysis

The EARS keyword directly maps to the test dimension:

| EARS Keyword | Test Dimension | Coverage Question |
|-------------|----------------|-------------------|
| (none/ubiquitous) | All states, all modes | Does the property hold universally? |
| When | Event triggers | Are all trigger conditions covered? |
| While | State/mode space | Are all relevant states tested? |
| Where | Configuration variants | Are all product variants covered? |
| If/then | Fault conditions | Are all failure modes injected? |

### 3.3 Automated Test Generation

Because EARS requirements follow predictable patterns, they are amenable to
automated or semi-automated test generation:

1. **Parse** the requirement to extract the EARS pattern and slot values
2. **Generate** test scaffolding based on the pattern
3. **Parameterize** boundary values from any quantitative constraints
4. **Combine** conditions from complex requirements into test matrices

This approach has been explored in academic research linking EARS to
Behavior-Driven Development (BDD) tools like Cucumber, where EARS templates
map naturally to Given/When/Then syntax.

---

## 4. EARS in Safety-Critical Systems

EARS was born in the safety-critical domain (aerospace gas turbine control at
Rolls-Royce) and has been adopted across multiple safety-critical industries.

### 4.1 Aerospace

**Origin domain.** EARS was developed specifically for engine control system
requirements at Rolls-Royce. These systems must comply with DO-178C (software)
and DO-254 (hardware) and ARP 4754A (system development).

**Example requirements:**
```
The Full Authority Digital Engine Control (FADEC) shall limit engine
thrust to the certified maximum for the current flight conditions.

When an engine surge is detected, the FADEC shall reduce fuel flow to
recover stable operation within 500 ms.

While the aircraft is on the ground with weight-on-wheels, the thrust
reverser system shall permit reverse thrust selection.

If dual-channel FADEC failure occurs, then the engine shall revert to
hydromechanical backup control.
```

### 4.2 Automotive

EARS has been adopted by automotive suppliers for ISO 26262 compliance.

**Example requirements:**
```
The electronic stability control system shall monitor individual wheel
speeds at a minimum rate of 100 Hz.

When the yaw rate deviation exceeds the stability threshold, the ESC
shall apply differential braking within 20 ms.

While the vehicle is stationary, if an unintended movement is detected,
then the automatic emergency braking system shall apply maximum braking
force.

Where the adaptive cruise control option is fitted, when the forward
distance to the lead vehicle drops below the safe following distance,
the ACC shall reduce engine torque and apply braking as needed.
```

### 4.3 Medical Devices

EARS supports IEC 62304 (medical device software lifecycle) requirements.

**Example requirements:**
```
The infusion pump controller shall limit the maximum flow rate to the
value configured for the selected drug profile.

When the occlusion pressure exceeds the alarm threshold, the infusion
pump shall stop the infusion and sound an audible alarm within 1 second.

If the battery voltage drops below the critical threshold, then the
infusion pump shall complete the current bolus, stop the infusion, and
activate the low-battery alarm.
```

### 4.4 Railway

EARS supports EN 50128 and EN 50129 compliance for railway signalling.

**Example requirements:**
```
While the track circuit indicates occupied, the interlocking system
shall maintain the protecting signal at danger (red aspect).

When the train detection system loses communication with a track
circuit, the interlocking shall assume the track section is occupied.

If the points fail to reach the commanded position within 8 seconds,
then the interlocking shall lock the points and set the relevant
signals to danger.
```

### 4.5 Why EARS Fits Safety-Critical Domains

- **Regulatory alignment:** Safety standards (DO-178C, ISO 26262, IEC 61508)
  demand unambiguous, testable, traceable requirements. EARS delivers this
  without requiring formal methods expertise.
- **Hazard analysis integration:** The "If/then" pattern maps directly to
  FMEA failure modes and FTA basic events.
- **Certification evidence:** The structured syntax supports automated
  completeness and consistency checking, producing evidence for certification.
- **Review efficiency:** Structured requirements are faster and more reliable
  to review than free-form text.

---

## 5. Machine-Parsing EARS Requirements

### 5.1 Regex Patterns for EARS Classification

The keyword-based structure of EARS makes it amenable to regular expression
parsing. The following patterns classify a requirement into its EARS type:

```python
import re

EARS_PATTERNS = {
    "complex_where_while_when": re.compile(
        r"^Where\s+(?P<feature>.+?),\s+"
        r"while\s+(?P<state>.+?),\s+"
        r"when\s+(?P<trigger>.+?),\s+"
        r"the\s+(?P<system>.+?)\s+shall\s+(?P<response>.+)$",
        re.IGNORECASE | re.DOTALL
    ),
    "complex_while_when": re.compile(
        r"^While\s+(?P<state>.+?),\s+"
        r"when\s+(?P<trigger>.+?),\s+"
        r"the\s+(?P<system>.+?)\s+shall\s+(?P<response>.+)$",
        re.IGNORECASE | re.DOTALL
    ),
    "complex_where_if": re.compile(
        r"^Where\s+(?P<feature>.+?),\s+"
        r"if\s+(?P<condition>.+?),\s+"
        r"then\s+the\s+(?P<system>.+?)\s+shall\s+(?P<response>.+)$",
        re.IGNORECASE | re.DOTALL
    ),
    "complex_while_if": re.compile(
        r"^While\s+(?P<state>.+?),\s+"
        r"if\s+(?P<condition>.+?),\s+"
        r"then\s+the\s+(?P<system>.+?)\s+shall\s+(?P<response>.+)$",
        re.IGNORECASE | re.DOTALL
    ),
    "event_driven": re.compile(
        r"^When\s+(?P<trigger>.+?),\s+"
        r"the\s+(?P<system>.+?)\s+shall\s+(?P<response>.+)$",
        re.IGNORECASE | re.DOTALL
    ),
    "state_driven": re.compile(
        r"^While\s+(?P<state>.+?),\s+"
        r"the\s+(?P<system>.+?)\s+shall\s+(?P<response>.+)$",
        re.IGNORECASE | re.DOTALL
    ),
    "optional_feature": re.compile(
        r"^Where\s+(?P<feature>.+?),\s+"
        r"the\s+(?P<system>.+?)\s+shall\s+(?P<response>.+)$",
        re.IGNORECASE | re.DOTALL
    ),
    "unwanted_behavior": re.compile(
        r"^If\s+(?P<condition>.+?),\s+"
        r"then\s+the\s+(?P<system>.+?)\s+shall\s+(?P<response>.+)$",
        re.IGNORECASE | re.DOTALL
    ),
    "ubiquitous": re.compile(
        r"^The\s+(?P<system>.+?)\s+shall\s+(?P<response>.+)$",
        re.IGNORECASE | re.DOTALL
    ),
}

def classify_requirement(text: str) -> dict:
    """Classify an EARS requirement and extract its components."""
    text = text.strip().rstrip(".")
    for pattern_name, regex in EARS_PATTERNS.items():
        match = regex.match(text)
        if match:
            return {
                "type": pattern_name,
                "components": match.groupdict(),
                "raw": text,
            }
    return {"type": "unclassified", "components": {}, "raw": text}
```

**Important parsing notes:**
- Match complex patterns before simple ones (greedy order matters)
- The "If...then" pattern uses "then" to distinguish from event-driven "When"
- Named groups extract the semantic components for downstream processing
- Case-insensitive matching handles inconsistent capitalization

### 5.2 Structured Data Representation

Once parsed, EARS requirements can be represented in structured formats for
tooling integration:

**YAML representation:**
```yaml
- id: REQ-FADEC-042
  type: event_driven
  trigger: "an engine surge is detected"
  system: "FADEC"
  response: "reduce fuel flow to recover stable operation within 500 ms"
  attributes:
    timing_constraint: "500 ms"
    safety_level: "ASIL-D"
    verification_method: "test"
```

**JSON representation:**
```json
{
  "id": "REQ-FADEC-042",
  "type": "event_driven",
  "trigger": "an engine surge is detected",
  "system": "FADEC",
  "response": "reduce fuel flow to recover stable operation within 500 ms",
  "attributes": {
    "timing_constraint_ms": 500,
    "safety_level": "ASIL-D",
    "verification_method": "test"
  }
}
```

### 5.3 Grammar Definition (EBNF)

For more rigorous parsing, EARS can be expressed as an EBNF grammar:

```ebnf
requirement   = [ where_clause ] [ while_clause ] [ when_clause | if_clause ]
                "the" system "shall" response ;

where_clause  = "Where" feature "," ;
while_clause  = "While" state "," ;
when_clause   = "When" trigger "," ;
if_clause     = "If" condition "," "then" ;

system        = noun_phrase ;
response      = verb_phrase ;
feature       = noun_phrase ;
state         = noun_phrase ;
trigger       = noun_phrase ;
condition     = noun_phrase ;
```

### 5.4 Validation Rules

Machine parsing enables automated quality checks:

1. **Keyword validation:** Each requirement starts with an EARS keyword or "The"
2. **Shall detection:** Every requirement contains exactly one "shall"
3. **Completeness check:** All slots for the detected pattern are filled
4. **Quantification check:** Responses containing measurable claims have units
5. **Ambiguity detection:** Flag words like "appropriate", "sufficient",
   "reasonable", "normal", "fast", "etc."
6. **Passive voice detection:** Flag requirements where the actor is unclear
7. **Negation audit:** Flag requirements that specify what the system shall NOT
   do (these are often better expressed positively or as constraints)

---

## 6. Best Practices for Human-Readable, Machine-Processable EARS Requirements

### 6.1 Writing Guidelines

**One requirement per statement.** Never combine multiple "shall" clauses in a
single requirement. If you find yourself writing "shall X and shall Y", split
into two requirements.

**Use consistent system naming.** Refer to the system (or subsystem) by the
same name throughout. Define the canonical name in a glossary and use it in
every requirement.

**Quantify where possible.** Replace vague terms with measurable values:
- Bad: "The system shall respond quickly"
- Good: "When the user submits a query, the search service shall return results within 200 ms at the 95th percentile"

**Keep clauses short.** Each slot (trigger, state, feature, condition, response)
should be a concise phrase, not a paragraph. If a slot exceeds ~20 words,
consider whether it can be decomposed.

**Use active voice in the response.** The system is the actor; the response
should say what it does, not what happens passively.

**Avoid nested conditions.** If you need "If A and If B then...", restructure:
- Option 1: Create separate requirements for each condition
- Option 2: Define a named state that captures the conjunction, then use a
  "While" clause

**Version the glossary.** Terms used in triggers, states, and features should
be defined in a controlled glossary that is version-controlled alongside the
requirements.

### 6.2 Machine-Processability Guidelines

**Standardize delimiters.** Use a comma after each clause (When X**,** the
system shall Y). This enables reliable regex splitting.

**Capitalize keywords consistently.** Start each EARS keyword with a capital
letter (When, While, Where, If) at the beginning of the sentence. Use lowercase
for keywords embedded in complex requirements after the first clause.

**Use "shall" exclusively for requirements.** Reserve "shall" for mandatory
requirements. Use "will" for statements of fact, "should" for recommendations,
and "may" for permissions. This enables automated filtering of true requirements.

**Tag requirements with metadata.** Use a consistent ID scheme (e.g.,
REQ-<subsystem>-<number>) and store metadata (priority, safety level,
verification method) separately from the requirement text.

**Avoid pronouns in requirement text.** Always use the full system or component
name, never "it" or "the system" when the antecedent is ambiguous.

**Use domain-standard terminology.** When terms have ISO or domain-standard
definitions, use those definitions consistently.

### 6.3 Template for Machine-Processable Requirements Document

```yaml
# requirements.ears.yaml
project: "Example Control System"
version: "2.1"
glossary:
  FADEC: "Full Authority Digital Engine Control"
  surge: "Compressor instability event characterized by..."

requirements:
  - id: REQ-ECS-001
    ears_type: ubiquitous
    text: >
      The FADEC shall limit engine thrust to the certified maximum
      for the current flight conditions.
    parsed:
      system: "FADEC"
      response: >
        limit engine thrust to the certified maximum for the current
        flight conditions
    attributes:
      safety_level: DAL-A
      verification: [analysis, test]
      traces_to: [SYS-REQ-042, HAZ-003]

  - id: REQ-ECS-002
    ears_type: event_driven
    text: >
      When an engine surge is detected, the FADEC shall reduce fuel
      flow to recover stable operation within 500 ms.
    parsed:
      trigger: "an engine surge is detected"
      system: "FADEC"
      response: "reduce fuel flow to recover stable operation within 500 ms"
    attributes:
      timing_constraint_ms: 500
      safety_level: DAL-A
      verification: [test, simulation]
      traces_to: [SYS-REQ-043, HAZ-007]
```

---

## 7. Existing Tools and Frameworks

### 7.1 Commercial Tools with EARS Support

**QVscribe (by QVality)**
- Requirements quality analysis tool
- Provides EARS template guidance and quality scoring
- Integrates with IBM DOORS, Jama Connect, and other RM tools
- Checks for ambiguity, incompleteness, and style violations
- One of the most mature commercial tools with explicit EARS support

**IBM DOORS / DOORS Next Generation**
- Industry-standard requirements management tool
- Can be customized with DXL scripts to enforce EARS syntax
- Supports traceability matrices aligned with EARS patterns
- Widely used in aerospace and defense where EARS originated

**Jama Connect**
- Requirements management platform
- Supports custom requirement types that can encode EARS patterns
- API enables automated EARS validation workflows

**Innoslate**
- Model-based systems engineering tool
- Supports natural language requirements with structured parsing
- Can be configured to enforce EARS templates

### 7.2 Open Source and Academic Tools

**EARS CTRL (Requirements Template Library)**
- Academic prototype from research accompanying the EARS papers
- Demonstrates automated classification and quality checking

**NLP-based parsers**
- Several academic projects have built EARS parsers using spaCy or NLTK
- These extract named entities (system, trigger, state) from EARS text
- Published in RE conference proceedings and REFSQ workshops

**Semios (research prototype)**
- Semantic analysis tool for requirements
- Can parse EARS syntax and perform consistency checking
- Maps requirements to ontological models

### 7.3 Integration with Development Tools

**BDD frameworks (Cucumber, Behave, SpecFlow)**
- EARS requirements map naturally to Given/When/Then scenarios:
  - `While` clause -> `Given` (precondition/state)
  - `When` clause -> `When` (trigger/event)
  - `shall` response -> `Then` (expected outcome)
  - `Where` clause -> Scenario Outline with feature tags
  - `If/then` clause -> `Given` (fault injected) + `Then` (recovery)

**CI/CD integration**
- EARS requirements stored as YAML can be validated in CI pipelines
- Regex-based linters can enforce EARS syntax as a pre-commit check
- Traceability to test results can be automated

**Model-Based Systems Engineering (MBSE)**
- EARS requirements can serve as the natural language complement to SysML
  models
- The structured syntax facilitates bidirectional synchronization between
  text requirements and model elements

### 7.4 Building Your Own EARS Tooling

Given the simplicity of EARS syntax, building project-specific tooling is
feasible:

1. **Linter:** Regex-based validator that checks each requirement against
   EARS patterns and flags non-conforming text (~100 lines of Python)
2. **Parser:** Extract components into structured data for downstream tools
   (see Section 5.1)
3. **Test scaffold generator:** Given parsed EARS data, generate test
   stubs in your framework of choice
4. **Traceability checker:** Cross-reference requirement IDs against test
   IDs to identify untested requirements
5. **Quality dashboard:** Aggregate metrics (% EARS-conformant, % with
   timing constraints, % traced to tests)

---

## 8. Summary and Applicability to VModelWorkflow

EARS provides a lightweight, proven methodology for writing requirements that
are simultaneously human-readable and machine-processable. Key takeaways:

1. **Five patterns cover the full requirement space:** ubiquitous, event-driven,
   state-driven, optional feature, and unwanted behavior, plus their
   combinations.

2. **Each pattern implies a test strategy:** The EARS keyword tells you what
   kind of test to write (invariant, stimulus-response, mode-based,
   configuration-variant, or fault-injection).

3. **Regex-parseable by design:** The keyword-based structure enables automated
   classification, validation, and extraction without NLP complexity.

4. **Proven in safety-critical domains:** Adopted by Rolls-Royce, automotive
   OEMs, medical device manufacturers, and railway signalling companies.

5. **Low adoption cost:** Engineers can learn EARS in under an hour and
   immediately produce better requirements.

For a workflow automation system like VModelWorkflow, EARS could serve as the
requirement syntax for task contracts, ensuring that each task specification is
unambiguous, testable, and machine-parseable. The structured format aligns well
with YAML-based pipeline configuration and automated test generation.

---

## References

1. Mavin, A., Wilkinson, P., Harwood, A., & Novak, M. (2009). "Easy Approach
   to Requirements Syntax (EARS)." *Proc. 17th IEEE International Requirements
   Engineering Conference (RE'09)*, pp. 317-322.

2. Mavin, A. & Wilkinson, P. (2010). "Big Ears (The Return of Easy Approach to
   Requirements Syntax)." *Proc. 18th IEEE International Requirements
   Engineering Conference (RE'10)*, pp. 277-282.

3. Mavin, A., Wilkinson, P., Gregory, S., & Uusitalo, E. (2016). "Listens
   Learned (8 Years of EARS)." *Proc. 24th IEEE International Requirements
   Engineering Conference (RE'16)*, pp. 276-282.

4. ISO/IEC/IEEE 29148:2018 — Systems and software engineering — Life cycle
   processes — Requirements engineering.

5. Femmer, H. et al. (2017). "Rapid quality assurance with Requirements
   Smells." *Journal of Systems and Software*, 123, pp. 190-213.

6. Berry, D., Kamsties, E., & Krieger, M. (2003). "From contract drafting to
   software specification: Linguistic sources of ambiguity." *Technical Report*.
