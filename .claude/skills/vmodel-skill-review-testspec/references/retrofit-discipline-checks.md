# Retrofit discipline checks

When the TestSpec has `recovery_status:` declared, retrofit-mode rules apply. Refusal A (fabricated retrofit intent) is non-negotiable. Mirrors `retrofit-discipline.md` on the author side. Conditional gating: every check here applies only when `recovery_status:` is declared.

## check.retrofit.intent-on-title (HARD ★ refusal A)

**Check that** retrofit case `title` describes the observed scenario, not inferred designer intent.

**Reject when** a retrofit case `title` reads as an intent statement (e.g. *"verifies user can log in"*, *"ensures session expiry is enforced"*) on a case reconstructed from existing test code without human confirmation. Title and notes are human-only fields in retrofit.

**Approve when** retrofit titles describe what the existing test observes (e.g. *"existing test login_test.py::test_password_match — observed"*) OR the human has confirmed the intent statement with a citation.

**Evidence pattern:** quote the case title; note absence of human source citation.

**recommended_action:** *"Replace with an observed-scenario title, OR cite the human source confirming the intent. Refusal A — retrofit `title` is human-only."*

## check.retrofit.intent-on-notes (HARD ★ refusal A)

**Check that** retrofit case `notes` does not carry inferred designer intent.

**Reject when** `notes` reads as a rationale or designer-intent narrative on a retrofit case (e.g. *"this test ensures the password complexity policy is honoured because security requires..."*) without a cited human source.

**Approve when** `notes` is empty, observational (e.g. *"observed in `login_test.py` line 47"*), or cites a human source for the intent claim.

**Evidence pattern:** quote the offending notes; cite refusal A.

**recommended_action:** *"Replace with observational notes, OR cite the human source confirming the intent. The matched author skill's retrofit-discipline reference covers human-only fields."*

## check.retrofit.recovery-status-reconstructed-verifies (HARD ★ refusal A)

**Check that** every `verifies` link reconstructed without human confirmation carries `recovery_status: unknown` per case (or per-link granularity if the schema permits).

**Reject when** a retrofit case has `recovery_status: verified` (or no `recovery_status` at all) on a `verifies` link that was reconstructed from existing test code rather than confirmed by a human.

**Approve when** every reconstructed `verifies` link carries `recovery_status: unknown`, OR carries `recovery_status: verified` with a cited human source.

**Evidence pattern:** name the case id; quote the reconstructed `verifies` line; note absence of human-source citation.

**recommended_action:** *"Mark the `verifies` link `recovery_status: unknown`, or cite the human confirming the link. Refusal A — reconstructed links default to unknown."*

## check.retrofit.recovery-status-missing (soft)

**Check that** every retrofit case carries `recovery_status` per case.

**Reject when** a retrofit-mode TestSpec has cases with no `recovery_status` field at all.

**Approve when** every case carries `recovery_status` with a value from the closed enum.

**recommended_action:** *"Populate `recovery_status` per case. Default for reconstruction is `unknown`."*

## check.retrofit.gap-report-missing (soft)

**Check that** retrofit-mode TestSpecs include a Gap Report enumerating: spec elements with no observed test; observed tests that did not map to any spec element; observed tests under suspicion (flaky, vacuous, tautological).

**Reject when** the artifact is retrofit-mode but no Gap Report section is present.

**Approve when** the Gap Report is present, even if any subsection is empty.

**recommended_action:** *"Add a Gap Report section enumerating the three categories. The matched author skill's retrofit-discipline reference covers Gap-Report shape."*

## Cross-link

`anti-patterns-catalog.md` (fabricated-retrofit-intent 13 ★, test-as-requirement-inversion 3) · `verifies-traceability-checks.md` · `quality-bar-gate.md` (Retrofit card)
