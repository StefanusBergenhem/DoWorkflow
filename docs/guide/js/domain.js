/**
 * Domain Translation Plugin System
 *
 * Single Responsibility: translates generic V-model terms to domain-specific vocabulary.
 * Open/Closed: new domains are added as data (JSON), no code changes needed.
 * Dependency Inversion: accepts a document interface, does not depend on global `document`.
 *
 * Usage (browser):
 *   const dm = createDomainManager(document);
 *   dm.registerDomain({ id: 'do178c', name: 'DO-178C', terms: { ... } });
 *   dm.switchDomain('do178c');
 *   dm.applyToDOM();
 *
 * Usage (Node.js tests):
 *   const dm = createDomainManager(mockDocument);
 */

// Generic V-model terms — the default vocabulary.
// Keys are term IDs (kebab-case), values follow the same shape as domain plugins.
const GENERIC_TERMS = {
  'system-requirement':   { label: 'System Requirement' },
  'sw-requirement':       { label: 'SW Requirement' },
  'sw-architecture':      { label: 'SW Architecture' },
  'detailed-design':      { label: 'Detailed Design' },
  'implementation':       { label: 'Implementation' },
  'unit-test':            { label: 'Unit Test' },
  'integration-test':     { label: 'Integration Test' },
  'sw-verification':      { label: 'SW Verification' },
  'system-test':          { label: 'System Test' },
  'review-record':        { label: 'Review Record' },
  'test-specification':   { label: 'Test Specification' },
  'test-result':          { label: 'Test Result' },
  'coverage-report':      { label: 'Coverage Report' },
  'development-plan':     { label: 'Development Plan' },
  'verification-plan':    { label: 'Verification Plan' },
  'cm-plan':              { label: 'CM Plan' },
  'qa-plan':              { label: 'QA Plan' },
  'source-code':          { label: 'Source Code' },
};

function createDomainManager(doc) {
  const domains = new Map();
  let currentDomainId = 'generic';
  const listeners = [];

  // Register the built-in generic domain
  domains.set('generic', {
    id: 'generic',
    name: 'Generic V-Model',
    terms: GENERIC_TERMS,
  });

  function registerDomain(plugin) {
    if (!plugin.id) {
      throw new Error('Domain plugin must have an id');
    }
    if (domains.has(plugin.id)) {
      throw new Error(`Domain '${plugin.id}' is already registered`);
    }
    domains.set(plugin.id, {
      id: plugin.id,
      name: plugin.name || plugin.id,
      terms: plugin.terms || {},
    });
  }

  function getAvailableDomains() {
    return Array.from(domains.keys());
  }

  function getCurrentDomain() {
    return currentDomainId;
  }

  function switchDomain(domainId) {
    if (!domains.has(domainId)) {
      throw new Error(`Domain '${domainId}' is not registered`);
    }
    if (domainId === currentDomainId) return;

    const from = currentDomainId;
    currentDomainId = domainId;

    for (const listener of listeners) {
      listener({ from, to: domainId });
    }
  }

  function resolveTerm(termId) {
    const domain = domains.get(currentDomainId);

    // Check current domain first
    if (domain && domain.terms[termId]) {
      return { ...domain.terms[termId] };
    }

    // Fall back to generic
    if (currentDomainId !== 'generic') {
      const generic = domains.get('generic');
      if (generic.terms[termId]) {
        return { ...generic.terms[termId] };
      }
    }

    // Unknown term — return the raw ID as label
    return { label: termId };
  }

  function applyToDOM() {
    if (!doc || !doc.querySelectorAll) return;

    const elements = doc.querySelectorAll('[data-term]');
    for (const el of elements) {
      const termId = el.dataset
        ? el.dataset.term
        : el.getAttribute('data-term');

      if (!termId) continue;

      const resolved = resolveTerm(termId);
      el.textContent = resolved.label;

      if (resolved.note) {
        el.setAttribute('title', resolved.note);
      } else {
        el.removeAttribute('title');
      }
    }
  }

  function onDomainChanged(callback) {
    listeners.push(callback);
  }

  return {
    registerDomain,
    getAvailableDomains,
    getCurrentDomain,
    switchDomain,
    resolveTerm,
    applyToDOM,
    onDomainChanged,
  };
}

// Support both browser and Node.js
if (typeof module !== 'undefined' && module.exports) {
  module.exports = { createDomainManager, GENERIC_TERMS };
}
