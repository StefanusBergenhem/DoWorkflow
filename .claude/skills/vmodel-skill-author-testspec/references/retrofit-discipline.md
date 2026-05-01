# Retrofit discipline

Retrofit means: existing test code already exists, the spec does not. The retrofit job is to author the TestSpec from the *spec* — not from the tests — and then map existing tests to it. This is the only honest order; reversed, the spec laundered out of test code is shape, not derivation.

## The hard rule (refusal A)

Refuse to:
1. Populate `title:` or `notes:` from inferred intent on a retrofit case (`title: "verifies user can log in"` derived from a method named `testUserLogin`).
2. Set `recovery_status: verified` on a `verifies` link reconstructed without human confirmation. Default `unknown`.
3. Ship a retrofit TestSpec that claims plausible-sounding stated intent on every existing test.

`title:` and `notes:` are **human-only** fields in retrofit. The `# HUMAN-ONLY` marker on `templates/retrofit-stub.yaml.tmpl` is structural — leave the field empty (or with the marker comment) until a human supplies the intent.

Tells: every retrofit case has a confident `title:`; `recovery_status` absent or `verified` on every reconstructed link; gap report missing or empty despite retrofit context; `notes:` reading like a unit-test comment ("checks the happy path").

## The order

The order of operations is **not** negotiable on retrofit:

1. **Read the spec FIRST.** Walk the parent DD / Architecture / Requirements + PB; list the elements that demand cases, exactly as in greenfield (see the per-layer seam files).
2. **Derive cases from the spec.** Apply derivation strategies. Author the TestSpec as if no test code existed.
3. **THEN map existing tests.** For each existing test, find the case in the derived TestSpec it corresponds to. Multiple existing tests may map to one case; one existing test may not map at all.
4. **Produce the gap report.** Three buckets:
   - **Spec elements without tests** — derived cases the existing test suite does not cover. Recommended action: author the missing test code.
   - **Tests without spec elements** — existing tests with no derived case to map to. They are either dead / vestigial, or they verify something the spec did not capture. Recommended action: human review per test.
   - **Observed-but-suspect cases** — existing tests that may map but the assertion is too weak / too narrow / wrong-layered for the derived case. Recommended action: rewrite the test code from the case.

Reversing this order — letting test code dictate cases — produces the *test-as-requirement inversion* anti-pattern (`anti-pattern.test-as-requirement-inversion`). The spec encodes intent; the tests are evidence about what was implemented; intent is not recoverable from evidence alone.

## `recovery_status` discipline

When a `verifies` link is reconstructed without a human confirming the link → `recovery_status: unknown`. When a human confirms — usually by reading the test code, the spec, and saying "yes, this test was indeed written to verify this spec element" — the status can be `verified`.

```yaml
- id: TC-<scope>-N
  title: ""                     # HUMAN-ONLY — leave empty until human supplies intent
  type: <strategy>
  verifies:
    - "DD-<scope>.<field>"
  recovery_status: unknown      # default until human-confirmed
  inputs:
    <observed from existing test>
  expected:
    <observed from existing test assertion>
  notes: ""                     # HUMAN-ONLY
```

The `inputs:` and `expected:` fields can be populated from the existing test code (those are observable facts, not intent). Title and notes carry intent and are off-limits.

## Gap report — required output

A retrofit TestSpec ships with a Retrofit Gap Report section. Skeleton:

```markdown
## Retrofit Gap Report

| Bucket | Items |
|---|---|
| Spec elements without tests | <list, citing upstream IDs> |
| Tests without spec elements | <list, citing existing test file:line> |
| Observed-but-suspect cases | <list of case IDs with reason> |
| Cases with `recovery_status: unknown` | <list of case IDs awaiting human confirmation> |
```

Empty buckets are filled with `(none observed)` — the report is always populated. An absent gap report on a retrofit run is a hard tell of `anti-pattern.fabricated-retrofit-intent`.

## Cross-link

`templates/retrofit-stub.yaml.tmpl` (the case scaffold with `# HUMAN-ONLY` markers) · `verifies-traceability.md` (the resolution rule for reconstructed links) · `anti-patterns.md` (`anti-pattern.fabricated-retrofit-intent`, `anti-pattern.test-as-requirement-inversion`)
