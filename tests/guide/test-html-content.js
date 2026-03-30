const { describe, it, before } = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');

const HTML_PATH = path.join(__dirname, '..', '..', 'docs', 'guide', 'index.html');

let html;

before(() => {
  html = fs.readFileSync(HTML_PATH, 'utf-8');
});

// ---------------------------------------------------------------------------
// Helper: extract a section by its id (everything between <section id="X"> and </section>)
// Handles nested sections by counting depth.
// ---------------------------------------------------------------------------
function extractSection(sectionId) {
  const openTag = `<section id="${sectionId}"`;
  const startIdx = html.indexOf(openTag);
  if (startIdx === -1) return null;

  let depth = 0;
  let i = startIdx;
  while (i < html.length) {
    if (html.startsWith('<section', i)) {
      depth++;
      i += 8;
    } else if (html.startsWith('</section>', i)) {
      depth--;
      if (depth === 0) return html.slice(startIdx, i + '</section>'.length);
      i += 10;
    } else {
      i++;
    }
  }
  return null;
}

// ---------------------------------------------------------------------------
// System Requirement — navigation
// ---------------------------------------------------------------------------
describe('System Requirement nav', () => {
  it('should have a nav link for system-requirement without stub badge', () => {
    const navLinkRe = /data-section="artifact-system-requirement"[^>]*>[\s\S]*?<\/a>/;
    const match = html.match(navLinkRe);
    assert.ok(match, 'nav link for system-requirement should exist');
    assert.ok(!match[0].includes('stub'), 'nav link should not have stub badge');
  });
});

// ---------------------------------------------------------------------------
// System Requirement — full section content
// ---------------------------------------------------------------------------
describe('System Requirement section', () => {
  let section;

  before(() => {
    section = extractSection('artifact-system-requirement');
    assert.ok(section, 'section #artifact-system-requirement must exist');
  });

  it('should not have a stub banner', () => {
    assert.ok(!section.includes('stub-banner'), 'section should not contain stub-banner');
    assert.ok(!section.includes('Coming Soon'), 'section should not say Coming Soon');
  });

  it('should have an introductory explanation (not just a schema reference)', () => {
    // The explanation should answer "what is it" and "why bother" — look for
    // educational content that is longer than a single lead paragraph
    assert.ok(section.includes('class="lead"'), 'should have a lead paragraph');
    // Should have substantive explanatory text — at least a "why it matters" element
    assert.ok(
      section.includes('Why') || section.includes('why'),
      'should explain why system requirements matter'
    );
  });

  it('should have a schema fields table', () => {
    assert.ok(section.includes('field-table'), 'should have a field-table');
  });

  it('should list all required fields in the table', () => {
    const requiredFields = ['title', 'statement', 'requirement_type', 'verification_method'];
    for (const field of requiredFields) {
      assert.ok(section.includes(field), `table should include required field: ${field}`);
    }
  });

  it('should list optional fields in the table', () => {
    const optionalFields = ['rationale', 'acceptance_criteria', 'source', 'attributes', 'notes'];
    for (const field of optionalFields) {
      assert.ok(section.includes(field), `table should include optional field: ${field}`);
    }
  });

  it('should list requirement_type enum values', () => {
    const types = ['functional', 'performance', 'safety', 'interface', 'environmental', 'constraint'];
    for (const t of types) {
      assert.ok(section.includes(t), `should include requirement_type value: ${t}`);
    }
  });

  it('should have a good example with full envelope + body', () => {
    assert.ok(section.includes('SYSREQ-'), 'good example should use SYSREQ- prefix');
    assert.ok(section.includes('artifact_type: "system-requirement"'), 'example should have artifact_type');
    assert.ok(section.includes('requirement_type:'), 'example should include requirement_type');
    assert.ok(section.includes('verification_method:'), 'example should include verification_method');
  });

  it('should have a bad example', () => {
    assert.ok(section.includes('example-block bad'), 'should have a bad example block');
  });

  it('should have trace connections', () => {
    assert.ok(section.includes('trace-flow'), 'should have trace-flow connections');
  });

  it('should list attribute flags including regulatory', () => {
    assert.ok(section.includes('regulatory'), 'should mention the regulatory attribute');
  });
});

// ---------------------------------------------------------------------------
// System Test Case — navigation
// ---------------------------------------------------------------------------
describe('System Test Case nav', () => {
  it('should have a nav link for system-test-case without stub badge', () => {
    const navLinkRe = /data-section="artifact-system-test-case"[^>]*>[\s\S]*?<\/a>/;
    const match = html.match(navLinkRe);
    assert.ok(match, 'nav link for system-test-case should exist');
    assert.ok(!match[0].includes('stub'), 'nav link should not have stub badge');
  });
});

// ---------------------------------------------------------------------------
// System Test Case — full section content
// ---------------------------------------------------------------------------
describe('System Test Case section', () => {
  let section;

  before(() => {
    section = extractSection('artifact-system-test-case');
    assert.ok(section, 'section #artifact-system-test-case must exist');
  });

  it('should not have a stub banner', () => {
    assert.ok(!section.includes('stub-banner'), 'section should not contain stub-banner');
    assert.ok(!section.includes('Coming Soon'), 'section should not say Coming Soon');
  });

  it('should have an introductory explanation', () => {
    assert.ok(section.includes('class="lead"'), 'should have a lead paragraph');
    assert.ok(
      section.includes('What is') || section.includes('what is'),
      'should explain what a system test case is'
    );
  });

  it('should explain why system tests are a YAML artifact (not code)', () => {
    assert.ok(
      section.includes('manual') || section.includes('procedure') || section.includes('demonstration'),
      'should explain that system tests are often not code'
    );
  });

  it('should explain where it sits in the V', () => {
    assert.ok(
      section.includes('Level 0') || section.includes('level 0'),
      'should reference its V-model level'
    );
  });

  it('should have a schema fields table', () => {
    assert.ok(section.includes('field-table'), 'should have a field-table');
  });

  it('should list all required fields in the table', () => {
    const requiredFields = ['title', 'objective', 'verification_method', 'preconditions', 'steps'];
    for (const field of requiredFields) {
      assert.ok(section.includes(field), `table should include required field: ${field}`);
    }
  });

  it('should list optional fields in the table', () => {
    const optionalFields = ['postconditions', 'test_data', 'environment', 'notes'];
    for (const field of optionalFields) {
      assert.ok(section.includes(field), `table should include optional field: ${field}`);
    }
  });

  it('should list step sub-fields', () => {
    const stepFields = ['step_number', 'action', 'expected_result'];
    for (const field of stepFields) {
      assert.ok(section.includes(field), `table should include step field: ${field}`);
    }
  });

  it('should list verification_method enum values', () => {
    const methods = ['test', 'analysis', 'inspection', 'demonstration', 'review'];
    for (const m of methods) {
      assert.ok(section.includes(m), `should include verification_method value: ${m}`);
    }
  });

  it('should have a good example with full envelope + body', () => {
    assert.ok(section.includes('SYSTEST-'), 'good example should use SYSTEST- prefix');
    assert.ok(section.includes('artifact_type: "system-test-case"'), 'example should have artifact_type');
    assert.ok(section.includes('verification_method:'), 'example should include verification_method');
    assert.ok(section.includes('expected_result:'), 'example should include expected_result in steps');
  });

  it('should have a bad example', () => {
    assert.ok(section.includes('example-block bad'), 'should have a bad example block');
  });

  it('should have trace connections', () => {
    assert.ok(section.includes('trace-flow'), 'should have trace-flow connections');
    assert.ok(section.includes('SYSREQ-'), 'trace should reference system requirements');
    assert.ok(section.includes('SYSTEST-'), 'trace should reference system test cases');
  });
});

// ---------------------------------------------------------------------------
// E2E walkthrough — System Test Case in verification section
// ---------------------------------------------------------------------------
describe('E2E walkthrough system test case', () => {
  let section;

  before(() => {
    section = extractSection('e2e-verification');
    assert.ok(section, 'section #e2e-verification must exist');
  });

  it('should include a system test case for the calculator', () => {
    assert.ok(
      section.includes('System Test') || section.includes('system test'),
      'verification section should cover system tests'
    );
  });

  it('should include SYSTEST artifact in verification', () => {
    assert.ok(section.includes('SYSTEST-'), 'should reference a SYSTEST artifact');
  });

  it('should include system test trace links', () => {
    assert.ok(section.includes('SYSREQ-001'), 'should trace back to SYSREQ-001');
  });
});

// ---------------------------------------------------------------------------
// E2E trace matrix — should include System Test column
// ---------------------------------------------------------------------------
describe('E2E trace matrix includes system test', () => {
  let section;

  before(() => {
    section = extractSection('e2e-trace-matrix');
    assert.ok(section, 'section #e2e-trace-matrix must exist');
  });

  it('should have a System Test column in the trace matrix', () => {
    assert.ok(
      section.includes('System Test'),
      'trace matrix header should include System Test'
    );
  });

  it('should reference SYSTEST artifact in trace matrix', () => {
    assert.ok(section.includes('SYSTEST-'), 'trace matrix should include SYSTEST artifact');
  });
});

// ---------------------------------------------------------------------------
// E2E walkthrough — SYSREQ-001 should include new schema fields
// ---------------------------------------------------------------------------
describe('E2E walkthrough SYSREQ-001', () => {
  let section;

  before(() => {
    section = extractSection('e2e-requirements');
    assert.ok(section, 'section #e2e-requirements must exist');
  });

  it('should include requirement_type in SYSREQ-001', () => {
    assert.ok(section.includes('requirement_type:'), 'SYSREQ-001 should have requirement_type');
  });

  it('should include verification_method in SYSREQ-001', () => {
    assert.ok(section.includes('verification_method:'), 'SYSREQ-001 should have verification_method');
  });
});

// ---------------------------------------------------------------------------
// SW Requirement — introductory explanation
// ---------------------------------------------------------------------------
describe('SW Requirement section intro', () => {
  let section;

  before(() => {
    section = extractSection('artifact-sw-requirement');
    assert.ok(section, 'section #artifact-sw-requirement must exist');
  });

  it('should have an introductory explanation card', () => {
    assert.ok(section.includes('class="card"'), 'should have a card with explanation');
  });

  it('should explain what a SW requirement is', () => {
    assert.ok(
      section.includes('What is') || section.includes('what is'),
      'should have a "What is" heading or explanation'
    );
  });

  it('should explain why SW requirements matter', () => {
    assert.ok(
      section.includes('Why') || section.includes('why'),
      'should explain why SW requirements matter'
    );
  });

  it('should explain where it sits in the V', () => {
    assert.ok(
      section.includes('Level 1') || section.includes('derived from system requirements'),
      'should explain its position in the V-model'
    );
  });
});

// ---------------------------------------------------------------------------
// SW Architecture — introductory explanation
// ---------------------------------------------------------------------------
describe('SW Architecture section intro', () => {
  let section;

  before(() => {
    section = extractSection('artifact-sw-architecture');
    assert.ok(section, 'section #artifact-sw-architecture must exist');
  });

  it('should have an introductory explanation card', () => {
    assert.ok(section.includes('class="card"'), 'should have a card with explanation');
  });

  it('should explain what an SW architecture is', () => {
    assert.ok(
      section.includes('What is') || section.includes('what is'),
      'should have a "What is" heading or explanation'
    );
  });

  it('should explain why architecture matters', () => {
    assert.ok(
      section.includes('Why') || section.includes('why'),
      'should explain why architecture matters'
    );
  });

  it('should explain where it sits in the V', () => {
    assert.ok(
      section.includes('Level 2'),
      'should reference its V-model level'
    );
  });
});

// ---------------------------------------------------------------------------
// Detailed Design — introductory explanation
// ---------------------------------------------------------------------------
describe('Detailed Design section intro', () => {
  let section;

  before(() => {
    section = extractSection('artifact-detailed-design');
    assert.ok(section, 'section #artifact-detailed-design must exist');
  });

  it('should have an introductory explanation card', () => {
    assert.ok(section.includes('class="card"'), 'should have a card with explanation');
  });

  it('should explain what a detailed design is', () => {
    assert.ok(
      section.includes('What is') || section.includes('what is'),
      'should have a "What is" heading or explanation'
    );
  });

  it('should explain why detailed design matters', () => {
    assert.ok(
      section.includes('Why') || section.includes('why'),
      'should explain why detailed design matters'
    );
  });

  it('should explain where it sits in the V', () => {
    assert.ok(
      section.includes('Level 3'),
      'should reference its V-model level'
    );
  });
});
