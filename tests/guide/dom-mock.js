/**
 * Minimal DOM mock for testing browser JS modules in Node.js.
 * No npm dependencies — just enough to test our domain/nav/diagram logic.
 */

class MockElement {
  constructor(tag, attrs = {}) {
    this.tagName = tag.toUpperCase();
    this.attributes = new Map(Object.entries(attrs));
    this.children = [];
    this.parentNode = null;
    this.classList = new MockClassList();
    this.style = {};
    this.dataset = {};
    this._textContent = '';
    this._innerHTML = '';
    this._eventListeners = {};

    // Parse data-* attributes into dataset
    for (const [key, value] of this.attributes) {
      if (key.startsWith('data-')) {
        const camelKey = key.slice(5).replace(/-([a-z])/g, (_, c) => c.toUpperCase());
        this.dataset[camelKey] = value;
      }
    }
  }

  get textContent() { return this._textContent; }
  set textContent(val) { this._textContent = val; }

  get innerHTML() { return this._innerHTML; }
  set innerHTML(val) { this._innerHTML = val; }

  getAttribute(name) { return this.attributes.get(name) ?? null; }
  setAttribute(name, value) {
    this.attributes.set(name, value);
    if (name.startsWith('data-')) {
      const camelKey = name.slice(5).replace(/-([a-z])/g, (_, c) => c.toUpperCase());
      this.dataset[camelKey] = value;
    }
  }
  hasAttribute(name) { return this.attributes.has(name); }
  removeAttribute(name) { this.attributes.delete(name); }

  addEventListener(event, handler) {
    if (!this._eventListeners[event]) this._eventListeners[event] = [];
    this._eventListeners[event].push(handler);
  }

  removeEventListener(event, handler) {
    if (!this._eventListeners[event]) return;
    this._eventListeners[event] = this._eventListeners[event].filter(h => h !== handler);
  }

  dispatchEvent(event) {
    const handlers = this._eventListeners[event.type] || [];
    for (const h of handlers) h(event);
  }

  appendChild(child) {
    child.parentNode = this;
    this.children.push(child);
    return child;
  }

  querySelector(selector) {
    return this._queryAll(selector)[0] || null;
  }

  querySelectorAll(selector) {
    return this._queryAll(selector);
  }

  _queryAll(selector) {
    const results = [];
    // Simple selector matching for testing purposes
    for (const child of this._allDescendants()) {
      if (matchesSelector(child, selector)) {
        results.push(child);
      }
    }
    return results;
  }

  _allDescendants() {
    const result = [];
    for (const child of this.children) {
      result.push(child);
      if (child._allDescendants) {
        result.push(...child._allDescendants());
      }
    }
    return result;
  }

  closest(selector) {
    let el = this;
    while (el) {
      if (matchesSelector(el, selector)) return el;
      el = el.parentNode;
    }
    return null;
  }

  scrollIntoView() { /* no-op in tests */ }
}

class MockClassList {
  constructor() { this._classes = new Set(); }
  add(...classes) { classes.forEach(c => this._classes.add(c)); }
  remove(...classes) { classes.forEach(c => this._classes.delete(c)); }
  toggle(cls) {
    if (this._classes.has(cls)) { this._classes.delete(cls); return false; }
    this._classes.add(cls); return true;
  }
  contains(cls) { return this._classes.has(cls); }
  get length() { return this._classes.size; }
  [Symbol.iterator]() { return this._classes[Symbol.iterator](); }
}

class MockEvent {
  constructor(type, options = {}) {
    this.type = type;
    this.detail = options.detail || null;
    this.bubbles = options.bubbles || false;
    this.defaultPrevented = false;
  }
  preventDefault() { this.defaultPrevented = true; }
}

function matchesSelector(el, selector) {
  // Handle compound selectors with commas (not needed yet, keep simple)
  // Handle: tag, .class, #id, [attr], [attr=value], tag.class
  if (selector.startsWith('.')) {
    return el.classList && el.classList.contains(selector.slice(1));
  }
  if (selector.startsWith('#')) {
    return el.getAttribute('id') === selector.slice(1);
  }
  if (selector.startsWith('[')) {
    const match = selector.match(/^\[([^\]=]+)(?:="([^"]*)")?\]$/);
    if (match) {
      const [, attr, val] = match;
      if (val !== undefined) return el.getAttribute(attr) === val;
      return el.hasAttribute(attr);
    }
  }
  // tag name
  if (/^[a-z]+$/i.test(selector)) {
    return el.tagName === selector.toUpperCase();
  }
  return false;
}

function createElement(tag, attrs = {}) {
  return new MockElement(tag, attrs);
}

function createMockDocument() {
  const root = new MockElement('div');
  const doc = {
    _root: root,
    createElement(tag) { return new MockElement(tag); },
    querySelector(sel) { return root.querySelector(sel); },
    querySelectorAll(sel) { return root.querySelectorAll(sel); },
    addEventListener(event, handler) { root.addEventListener(event, handler); },
    dispatchEvent(event) { root.dispatchEvent(event); },
  };
  return doc;
}

module.exports = { MockElement, MockClassList, MockEvent, createElement, createMockDocument, matchesSelector };
