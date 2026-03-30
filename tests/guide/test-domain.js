const { describe, it, beforeEach } = require('node:test');
const assert = require('node:assert/strict');
const { createElement, createMockDocument, MockEvent } = require('./dom-mock');

// domain.js will export a factory that accepts a document root (dependency injection)
// This follows DIP — the module depends on an abstraction (DOM interface), not the global document
const { createDomainManager } = require('../../docs/guide/js/domain.js');

describe('DomainManager', () => {

  describe('registration', () => {
    it('should register a domain plugin', () => {
      const dm = createDomainManager({});
      dm.registerDomain({
        name: 'DO-178C',
        id: 'do178c',
        terms: {
          'sw-requirement': { label: 'High-Level Requirement', abbrev: 'HLR' }
        }
      });
      assert.deepStrictEqual(dm.getAvailableDomains(), ['generic', 'do178c']);
    });

    it('should have generic domain available by default', () => {
      const dm = createDomainManager({});
      assert.deepStrictEqual(dm.getAvailableDomains(), ['generic']);
    });

    it('should reject domain without id', () => {
      const dm = createDomainManager({});
      assert.throws(() => {
        dm.registerDomain({ name: 'Bad', terms: {} });
      }, /id/);
    });

    it('should reject duplicate domain id', () => {
      const dm = createDomainManager({});
      dm.registerDomain({ id: 'foo', name: 'Foo', terms: {} });
      assert.throws(() => {
        dm.registerDomain({ id: 'foo', name: 'Foo 2', terms: {} });
      }, /already registered/);
    });
  });

  describe('term resolution', () => {
    let dm;

    beforeEach(() => {
      dm = createDomainManager({});
      dm.registerDomain({
        id: 'do178c',
        name: 'DO-178C',
        terms: {
          'sw-requirement': { label: 'High-Level Requirement', abbrev: 'HLR', note: 'DO-178C §6.3.1' },
          'detailed-design': { label: 'Low-Level Requirement', abbrev: 'LLR', note: 'DO-178C §6.3.2' },
        }
      });
    });

    it('should resolve term in generic domain (identity)', () => {
      const result = dm.resolveTerm('sw-requirement');
      assert.equal(result.label, 'SW Requirement');
    });

    it('should resolve term in registered domain', () => {
      dm.switchDomain('do178c');
      const result = dm.resolveTerm('sw-requirement');
      assert.equal(result.label, 'High-Level Requirement');
      assert.equal(result.abbrev, 'HLR');
      assert.equal(result.note, 'DO-178C §6.3.1');
    });

    it('should fall back to generic for unknown terms in a domain', () => {
      dm.switchDomain('do178c');
      const result = dm.resolveTerm('review-record');
      assert.equal(result.label, 'Review Record');
    });

    it('should return raw term-id for completely unknown terms', () => {
      const result = dm.resolveTerm('nonexistent-thing');
      assert.equal(result.label, 'nonexistent-thing');
    });
  });

  describe('domain switching', () => {
    let dm;

    beforeEach(() => {
      dm = createDomainManager({});
      dm.registerDomain({ id: 'do178c', name: 'DO-178C', terms: {} });
    });

    it('should start in generic domain', () => {
      assert.equal(dm.getCurrentDomain(), 'generic');
    });

    it('should switch to a registered domain', () => {
      dm.switchDomain('do178c');
      assert.equal(dm.getCurrentDomain(), 'do178c');
    });

    it('should switch back to generic', () => {
      dm.switchDomain('do178c');
      dm.switchDomain('generic');
      assert.equal(dm.getCurrentDomain(), 'generic');
    });

    it('should throw on unknown domain', () => {
      assert.throws(() => {
        dm.switchDomain('nonexistent');
      }, /not registered/);
    });
  });

  describe('DOM application', () => {
    let dm, doc, root;

    beforeEach(() => {
      doc = createMockDocument();
      root = doc._root;

      // Build a mini DOM with data-term spans
      const span1 = createElement('span', { 'data-term': 'sw-requirement' });
      span1.textContent = 'SW Requirement';
      root.appendChild(span1);

      const span2 = createElement('span', { 'data-term': 'detailed-design' });
      span2.textContent = 'Detailed Design';
      root.appendChild(span2);

      const plain = createElement('p');
      plain.textContent = 'No terms here';
      root.appendChild(plain);

      dm = createDomainManager(doc);
      dm.registerDomain({
        id: 'do178c',
        name: 'DO-178C',
        terms: {
          'sw-requirement': { label: 'High-Level Requirement', abbrev: 'HLR' },
          'detailed-design': { label: 'Low-Level Requirement', abbrev: 'LLR' },
        }
      });
    });

    it('should apply domain translations to data-term elements', () => {
      dm.switchDomain('do178c');
      dm.applyToDOM();

      const spans = root.querySelectorAll('[data-term]');
      assert.equal(spans[0].textContent, 'High-Level Requirement');
      assert.equal(spans[1].textContent, 'Low-Level Requirement');
    });

    it('should restore generic terms when switching back', () => {
      dm.switchDomain('do178c');
      dm.applyToDOM();

      dm.switchDomain('generic');
      dm.applyToDOM();

      const spans = root.querySelectorAll('[data-term]');
      assert.equal(spans[0].textContent, 'SW Requirement');
      assert.equal(spans[1].textContent, 'Detailed Design');
    });

    it('should set tooltip with domain note', () => {
      dm.registerDomain({
        id: 'noted',
        name: 'Noted',
        terms: {
          'sw-requirement': { label: 'Req', note: 'See §5.1' },
        }
      });
      dm.switchDomain('noted');
      dm.applyToDOM();

      const span = root.querySelectorAll('[data-term]')[0];
      assert.equal(span.getAttribute('title'), 'See §5.1');
    });

    it('should not modify elements without data-term', () => {
      dm.switchDomain('do178c');
      dm.applyToDOM();

      const p = root.querySelectorAll('p')[0];
      assert.equal(p.textContent, 'No terms here');
    });
  });

  describe('event notification', () => {
    it('should call listeners on domain change', () => {
      const dm = createDomainManager({});
      dm.registerDomain({ id: 'do178c', name: 'DO-178C', terms: {} });

      const events = [];
      dm.onDomainChanged((e) => events.push(e));

      dm.switchDomain('do178c');

      assert.equal(events.length, 1);
      assert.equal(events[0].from, 'generic');
      assert.equal(events[0].to, 'do178c');
    });

    it('should not fire event when switching to current domain', () => {
      const dm = createDomainManager({});
      const events = [];
      dm.onDomainChanged((e) => events.push(e));

      dm.switchDomain('generic');
      assert.equal(events.length, 0);
    });
  });
});
