const { describe, it, beforeEach } = require('node:test');
const assert = require('node:assert/strict');
const { createElement, createMockDocument } = require('./dom-mock');

const { createNavigation } = require('../../docs/guide/js/app.js');

describe('Navigation', () => {

  let doc, root, nav;

  function buildSections() {
    // Sidebar with nav links
    const sidebar = createElement('nav', { id: 'sidebar' });

    const link1 = createElement('a', { 'data-section': 'v-model', href: '#v-model' });
    link1.textContent = 'The V-Model';
    link1.classList.add('nav-link');
    sidebar.appendChild(link1);

    const link2 = createElement('a', { 'data-section': 'artifacts', href: '#artifacts' });
    link2.textContent = 'Artifacts';
    link2.classList.add('nav-link');
    sidebar.appendChild(link2);

    const link3 = createElement('a', { 'data-section': 'traceability', href: '#traceability' });
    link3.textContent = 'Traceability';
    link3.classList.add('nav-link');
    sidebar.appendChild(link3);

    root.appendChild(sidebar);

    // Content sections
    const content = createElement('main', { id: 'content' });

    const sec1 = createElement('section', { id: 'v-model' });
    sec1.classList.add('content-section');
    content.appendChild(sec1);

    const sec2 = createElement('section', { id: 'artifacts' });
    sec2.classList.add('content-section');
    content.appendChild(sec2);

    const sec3 = createElement('section', { id: 'traceability' });
    sec3.classList.add('content-section');
    content.appendChild(sec3);

    root.appendChild(content);
  }

  beforeEach(() => {
    doc = createMockDocument();
    root = doc._root;
    buildSections();
  });

  describe('section switching', () => {
    it('should show the target section and hide others', () => {
      nav = createNavigation(doc);
      nav.showSection('artifacts');

      const sections = root.querySelectorAll('.content-section');
      assert.equal(sections[0].classList.contains('active'), false);
      assert.equal(sections[1].classList.contains('active'), true);
      assert.equal(sections[2].classList.contains('active'), false);
    });

    it('should mark the corresponding nav link as active', () => {
      nav = createNavigation(doc);
      nav.showSection('artifacts');

      const links = root.querySelectorAll('.nav-link');
      assert.equal(links[0].classList.contains('active'), false);
      assert.equal(links[1].classList.contains('active'), true);
      assert.equal(links[2].classList.contains('active'), false);
    });

    it('should default to first section if no section specified', () => {
      nav = createNavigation(doc);
      nav.showSection(null);

      const sections = root.querySelectorAll('.content-section');
      assert.equal(sections[0].classList.contains('active'), true);
    });

    it('should default to first section for unknown section id', () => {
      nav = createNavigation(doc);
      nav.showSection('nonexistent');

      const sections = root.querySelectorAll('.content-section');
      assert.equal(sections[0].classList.contains('active'), true);
    });
  });

  describe('getCurrentSection', () => {
    it('should return current section id', () => {
      nav = createNavigation(doc);
      nav.showSection('traceability');
      assert.equal(nav.getCurrentSection(), 'traceability');
    });
  });

  describe('getSectionIds', () => {
    it('should return all section ids', () => {
      nav = createNavigation(doc);
      assert.deepStrictEqual(nav.getSectionIds(), ['v-model', 'artifacts', 'traceability']);
    });
  });

  describe('subsection navigation', () => {
    it('should expand parent and show subsection', () => {
      // Add a subsection
      const sec1 = root.querySelectorAll('.content-section')[0];
      const sub = createElement('div', { id: 'v-model-shape' });
      sub.classList.add('subsection');
      sec1.appendChild(sub);

      nav = createNavigation(doc);
      nav.showSection('v-model', 'v-model-shape');

      assert.equal(sec1.classList.contains('active'), true);
      assert.equal(sub.classList.contains('highlighted'), true);
    });
  });
});
