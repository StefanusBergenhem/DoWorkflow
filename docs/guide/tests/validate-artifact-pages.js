/**
 * Structural validation tests for artifact documentation pages.
 *
 * RED phase: these tests define the contract for source-code.html and unit-test.html.
 * They validate structure, heading hierarchy, required sections, navigation,
 * domain translation attributes, and cross-page link integrity.
 *
 * Run: node docs/guide/tests/validate-artifact-pages.js
 */

const fs = require('fs');
const path = require('path');

// ---------------------------------------------------------------------------
// Minimal HTML parser helpers (no dependencies)
// ---------------------------------------------------------------------------

function readFile(relPath) {
  const fullPath = path.join(__dirname, '..', relPath);
  if (!fs.existsSync(fullPath)) {
    return null;
  }
  return fs.readFileSync(fullPath, 'utf-8');
}

function extractElements(html, tag) {
  const regex = new RegExp(`<${tag}[^>]*>([\\s\\S]*?)</${tag}>`, 'gi');
  const matches = [];
  let m;
  while ((m = regex.exec(html)) !== null) {
    matches.push({ full: m[0], inner: m[1].trim(), outer: m[0] });
  }
  return matches;
}

function extractById(html, id) {
  // Finds a section with the given id attribute
  const regex = new RegExp(`<section[^>]*id=["']${id}["'][^>]*>[\\s\\S]*?</section>`, 'i');
  const m = html.match(regex);
  return m ? m[0] : null;
}

function extractAllIds(html) {
  const regex = /id=["']([^"']+)["']/g;
  const ids = [];
  let m;
  while ((m = regex.exec(html)) !== null) {
    ids.push(m[1]);
  }
  return ids;
}

function extractHeadings(html) {
  const regex = /<(h[1-6])[^>]*>([\s\S]*?)<\/\1>/gi;
  const headings = [];
  let m;
  while ((m = regex.exec(html)) !== null) {
    const level = parseInt(m[1][1]);
    const text = m[2].replace(/<[^>]+>/g, '').trim();
    headings.push({ level, text });
  }
  return headings;
}

function extractDataTerms(html) {
  const regex = /data-term=["']([^"']+)["']/g;
  const terms = [];
  let m;
  while ((m = regex.exec(html)) !== null) {
    terms.push(m[1]);
  }
  return terms;
}

function extractHrefs(html) {
  const regex = /href=["']([^"']+)["']/g;
  const hrefs = [];
  let m;
  while ((m = regex.exec(html)) !== null) {
    hrefs.push(m[1]);
  }
  return hrefs;
}

function hasStylesheetLink(html, href) {
  return html.includes(`href="${href}"`) || html.includes(`href='${href}'`);
}

function hasScriptSrc(html, src) {
  return html.includes(`src="${src}"`) || html.includes(`src='${src}'`);
}

// ---------------------------------------------------------------------------
// Test runner
// ---------------------------------------------------------------------------

let passed = 0;
let failed = 0;
let errors = [];

function assert(condition, message) {
  if (condition) {
    passed++;
  } else {
    failed++;
    errors.push(message);
  }
}

function assertContains(haystack, needle, message) {
  assert(haystack && haystack.includes(needle), message);
}

// ---------------------------------------------------------------------------
// Shared structure contract for all artifact pages
// ---------------------------------------------------------------------------

const REQUIRED_SECTIONS = {
  'source-code': [
    'sc-intro',                // Section 1: What is source code
    'sc-vmodel',               // Section 2: V-model context
    'sc-quality',              // Section 3: Producing quality source code (bulk)
    'sc-coding-standards',     //   3.1 Coding standards
    'sc-clean-code',           //   3.2 Clean code principles
    'sc-architecture',         //   3.3 Architecture at the code level
    'sc-ai-development',       //   3.4 AI-assisted development
    'sc-code-review',          //   3.5 Code review
    'sc-vmodel-considerations',// Section 4: V-model specific considerations
    'sc-framework',            // Section 5: Framework integration
    'sc-skills',               // Section 6: AI skills (stub)
  ],
  'unit-test': [
    'ut-intro',                // Section 1: What is unit testing
    'ut-vmodel',               // Section 2: V-model context
    'ut-quality',              // Section 3: Producing quality unit tests (bulk)
    'ut-derivation-process',   //   3.1 Test derivation process
    'ut-derivation-strategies',//   3.2 Derivation strategies
    'ut-structure-naming',     //   3.3 Test structure and naming
    'ut-anti-patterns',        //   3.4 Anti-patterns
    'ut-coverage',             //   3.5 Coverage and completeness
    'ut-vmodel-considerations',// Section 4: V-model specific considerations
    'ut-framework',            // Section 5: Framework integration
    'ut-skills',               // Section 6: AI skills (stub)
  ],
};

// Each artifact page must link shared CSS and JS
const SHARED_ASSETS = {
  css: ['../css/main.css'],
  js: ['../js/domain.js'],
};

// Each page must use domain translation for at least these terms
const MINIMUM_DOMAIN_TERMS = {
  'source-code': ['detailed-design', 'unit-test', 'source-code'],
  'unit-test': ['detailed-design', 'unit-test', 'source-code'],
};

// ---------------------------------------------------------------------------
// Page-level tests
// ---------------------------------------------------------------------------

function testArtifactPage(pageName, fileName) {
  const html = readFile(`artifacts/${fileName}`);

  // --- File exists ---
  assert(html !== null, `${fileName} must exist in docs/guide/artifacts/`);
  if (!html) return; // Can't test further

  // --- Valid HTML structure ---
  assert(html.includes('<!DOCTYPE html>'), `${fileName}: must have DOCTYPE`);
  assert(html.includes('<html'), `${fileName}: must have <html> tag`);
  assert(html.includes('</html>'), `${fileName}: must close <html> tag`);

  // --- Shared assets ---
  for (const css of SHARED_ASSETS.css) {
    assert(hasStylesheetLink(html, css), `${fileName}: must link ${css}`);
  }
  for (const js of SHARED_ASSETS.js) {
    assert(hasScriptSrc(html, js), `${fileName}: must include ${js}`);
  }

  // --- Required sections exist ---
  const allIds = extractAllIds(html);
  for (const sectionId of REQUIRED_SECTIONS[pageName]) {
    assert(
      allIds.includes(sectionId),
      `${fileName}: must have section with id="${sectionId}"`
    );
  }

  // --- Heading hierarchy ---
  const headings = extractHeadings(html);
  assert(headings.length > 0, `${fileName}: must have headings`);

  // First content heading should be h1 or h2
  if (headings.length > 0) {
    assert(
      headings[0].level <= 2,
      `${fileName}: first heading should be h1 or h2, got h${headings[0].level}`
    );
  }

  // No heading level jumps (h2 -> h4 without h3)
  let prevLevel = 0;
  for (const h of headings) {
    if (prevLevel > 0 && h.level > prevLevel + 1) {
      assert(false, `${fileName}: heading jump from h${prevLevel} to h${h.level} ("${h.text}")`);
    }
    prevLevel = h.level;
  }

  // --- Domain translation ---
  const terms = extractDataTerms(html);
  for (const requiredTerm of MINIMUM_DOMAIN_TERMS[pageName]) {
    assert(
      terms.includes(requiredTerm),
      `${fileName}: must use data-term="${requiredTerm}"`
    );
  }

  // --- Code examples present ---
  const codeBlocks = html.match(/<code[\s>]/g) || [];
  assert(
    codeBlocks.length >= 5,
    `${fileName}: must have at least 5 code examples (found ${codeBlocks.length})`
  );

  // --- No empty sections ---
  for (const sectionId of REQUIRED_SECTIONS[pageName]) {
    const section = extractById(html, sectionId);
    if (section) {
      // Strip tags, check remaining text length
      const textContent = section.replace(/<[^>]+>/g, '').replace(/\s+/g, ' ').trim();
      assert(
        textContent.length > 50,
        `${fileName}: section "${sectionId}" appears to have no meaningful content`
      );
    }
  }

  // --- Domain switcher present ---
  assert(
    html.includes('domain-select'),
    `${fileName}: must include domain switcher`
  );

  // --- Back navigation to index ---
  const hrefs = extractHrefs(html);
  assert(
    hrefs.some(h => h.includes('index.html') || h === '../' || h === '..'),
    `${fileName}: must have a link back to the main guide`
  );
}

// ---------------------------------------------------------------------------
// Index.html sidebar tests
// ---------------------------------------------------------------------------

function testIndexSidebar() {
  const html = readFile('index.html');
  assert(html !== null, 'index.html must exist');
  if (!html) return;

  // Sidebar must link to artifact pages
  const hrefs = extractHrefs(html);
  assert(
    hrefs.some(h => h.includes('artifacts/source-code.html')),
    'index.html sidebar must link to artifacts/source-code.html'
  );
  assert(
    hrefs.some(h => h.includes('artifacts/unit-test.html')),
    'index.html sidebar must link to artifacts/unit-test.html'
  );
}

// ---------------------------------------------------------------------------
// Cross-page link integrity
// ---------------------------------------------------------------------------

function testCrossPageLinks() {
  const pages = ['artifacts/source-code.html', 'artifacts/unit-test.html'];

  for (const page of pages) {
    const html = readFile(page);
    if (!html) continue;

    const hrefs = extractHrefs(html);
    for (const href of hrefs) {
      // Skip external links, anchors-only, and javascript:
      if (href.startsWith('http') || href.startsWith('#') || href.startsWith('javascript')) continue;
      // Skip CSS/JS assets
      if (href.endsWith('.css') || href.endsWith('.js') || href.endsWith('.json')) continue;

      // Resolve relative to page location
      const resolved = path.resolve(path.join(__dirname, '..', 'artifacts'), href);
      assert(
        fs.existsSync(resolved),
        `${page}: broken link "${href}" (resolved to ${resolved})`
      );
    }
  }
}

// ---------------------------------------------------------------------------
// Run
// ---------------------------------------------------------------------------

console.log('=== Artifact Page Structural Validation ===\n');

console.log('--- source-code.html ---');
testArtifactPage('source-code', 'source-code.html');

console.log('--- unit-test.html ---');
testArtifactPage('unit-test', 'unit-test.html');

console.log('--- index.html sidebar ---');
testIndexSidebar();

console.log('--- cross-page links ---');
testCrossPageLinks();

console.log(`\n=== Results: ${passed} passed, ${failed} failed ===\n`);

if (errors.length > 0) {
  console.log('Failures:');
  for (const err of errors) {
    console.log(`  FAIL: ${err}`);
  }
}

process.exit(failed > 0 ? 1 : 0);
