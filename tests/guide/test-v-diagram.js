const { describe, it, beforeEach } = require('node:test');
const assert = require('node:assert/strict');

const { createVDiagram, V_LEVELS } = require('../../docs/guide/js/v-diagram.js');

describe('V-Diagram Data Model', () => {

  describe('V_LEVELS definition', () => {
    it('should define all V-model levels', () => {
      const ids = V_LEVELS.map(l => l.id);
      assert.ok(ids.includes('system-requirement'));
      assert.ok(ids.includes('sw-requirement'));
      assert.ok(ids.includes('sw-architecture'));
      assert.ok(ids.includes('detailed-design'));
      assert.ok(ids.includes('implementation'));
      assert.ok(ids.includes('unit-test'));
      assert.ok(ids.includes('integration-test'));
      assert.ok(ids.includes('sw-verification'));
      assert.ok(ids.includes('system-test'));
    });

    it('should have left and right sides plus implementation at the bottom', () => {
      const leftSide = V_LEVELS.filter(l => l.side === 'left');
      const rightSide = V_LEVELS.filter(l => l.side === 'right');
      const bottom = V_LEVELS.filter(l => l.side === 'bottom');

      assert.ok(leftSide.length >= 4, 'Should have at least 4 left-side levels');
      assert.ok(rightSide.length >= 4, 'Should have at least 4 right-side levels');
      assert.equal(bottom.length, 1, 'Should have exactly 1 bottom level');
      assert.equal(bottom[0].id, 'implementation');
    });

    it('should pair left levels with right levels', () => {
      for (const level of V_LEVELS) {
        if (level.side === 'left') {
          assert.ok(level.verifiedBy, `${level.id} should have a verifiedBy counterpart`);
          const counterpart = V_LEVELS.find(l => l.id === level.verifiedBy);
          assert.ok(counterpart, `Counterpart ${level.verifiedBy} should exist`);
          assert.equal(counterpart.side, 'right');
        }
      }
    });

    it('should assign each level a depth (V position)', () => {
      for (const level of V_LEVELS) {
        assert.ok(typeof level.depth === 'number', `${level.id} should have numeric depth`);
      }
    });

    it('should have section links for navigating to documentation', () => {
      for (const level of V_LEVELS) {
        assert.ok(level.section, `${level.id} should have a section reference`);
      }
    });
  });

  describe('trace connections', () => {
    let diagram;

    beforeEach(() => {
      diagram = createVDiagram();
    });

    it('should return trace connections between levels', () => {
      const traces = diagram.getTraces();
      assert.ok(traces.length > 0, 'Should have trace connections');
    });

    it('should have horizontal traces (left-right verification pairs)', () => {
      const traces = diagram.getTraces();
      const horizontal = traces.filter(t => t.type === 'verified-by');
      assert.ok(horizontal.length >= 4, 'Should have verification traces for each pair');
    });

    it('should have vertical traces (derivation down the left side)', () => {
      const traces = diagram.getTraces();
      const vertical = traces.filter(t => t.type === 'derived-from');
      assert.ok(vertical.length >= 3, 'Should have derivation traces down the V');
    });

    it('each trace should reference valid level ids', () => {
      const traces = diagram.getTraces();
      const ids = V_LEVELS.map(l => l.id);
      for (const trace of traces) {
        assert.ok(ids.includes(trace.from), `${trace.from} should be a valid level`);
        assert.ok(ids.includes(trace.to), `${trace.to} should be a valid level`);
      }
    });
  });

  describe('level lookup', () => {
    let diagram;

    beforeEach(() => {
      diagram = createVDiagram();
    });

    it('should find a level by id', () => {
      const level = diagram.getLevel('sw-requirement');
      assert.equal(level.id, 'sw-requirement');
      assert.ok(level.label);
    });

    it('should return null for unknown level', () => {
      const level = diagram.getLevel('nonexistent');
      assert.equal(level, null);
    });

    it('should return traces for a specific level', () => {
      const traces = diagram.getTracesFor('detailed-design');
      assert.ok(traces.length > 0);
      // Should include both incoming and outgoing
      const ids = traces.map(t => t.from === 'detailed-design' ? t.to : t.from);
      assert.ok(ids.length > 0);
    });
  });

  describe('SVG generation', () => {
    let diagram;

    beforeEach(() => {
      diagram = createVDiagram();
    });

    it('should generate SVG string', () => {
      const svg = diagram.toSVG();
      assert.ok(svg.startsWith('<svg'), 'Should start with svg tag');
      assert.ok(svg.includes('</svg>'), 'Should end with closing svg tag');
    });

    it('should include all level nodes', () => {
      const svg = diagram.toSVG();
      for (const level of V_LEVELS) {
        assert.ok(svg.includes(`data-level="${level.id}"`),
          `SVG should include node for ${level.id}`);
      }
    });

    it('should include clickable groups for each node', () => {
      const svg = diagram.toSVG();
      for (const level of V_LEVELS) {
        assert.ok(svg.includes(`data-section="${level.section}"`),
          `SVG should include section link for ${level.id}`);
      }
    });

    it('should include trace lines', () => {
      const svg = diagram.toSVG();
      assert.ok(svg.includes('trace-line'), 'Should have trace line elements');
    });
  });
});
