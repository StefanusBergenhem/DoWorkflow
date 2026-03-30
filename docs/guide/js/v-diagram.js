/**
 * Interactive V-Model Diagram
 *
 * Single Responsibility: defines the V-model structure and generates SVG representation.
 * Open/Closed: V_LEVELS data is extensible; SVG generation adapts to whatever levels exist.
 *
 * The diagram is a data-driven SVG. Each node is clickable and navigates to the
 * corresponding documentation section. Trace arrows show relationships.
 */

// V-model levels — the canonical definition of the V shape.
// Each level has: id, label (generic term), side (left/right/bottom),
// depth (0=top, increases downward), verifiedBy (right-side counterpart),
// and section (documentation section to navigate to).
const V_LEVELS = [
  // Left side (specification/design) — top to bottom
  { id: 'system-requirement',  label: 'System Requirement',  side: 'left',   depth: 0, verifiedBy: 'system-test',      section: 'artifact-system-requirement' },
  { id: 'sw-requirement',      label: 'SW Requirement',      side: 'left',   depth: 1, verifiedBy: 'sw-verification',   section: 'artifact-sw-requirement' },
  { id: 'sw-architecture',     label: 'SW Architecture',     side: 'left',   depth: 2, verifiedBy: 'integration-test',  section: 'artifact-sw-architecture' },
  { id: 'detailed-design',     label: 'Detailed Design',     side: 'left',   depth: 3, verifiedBy: 'unit-test',         section: 'artifact-detailed-design' },

  // Bottom (implementation)
  { id: 'implementation',      label: 'Implementation',      side: 'bottom', depth: 4, section: 'concept-implementation' },

  // Right side (verification) — bottom to top
  { id: 'unit-test',           label: 'Unit Test',           side: 'right',  depth: 3, verifies: 'detailed-design',     section: 'concept-unit-test' },
  { id: 'integration-test',    label: 'Integration Test',    side: 'right',  depth: 2, verifies: 'sw-architecture',     section: 'concept-integration-test' },
  { id: 'sw-verification',     label: 'SW Verification',     side: 'right',  depth: 1, verifies: 'sw-requirement',      section: 'concept-sw-verification' },
  { id: 'system-test',         label: 'System Test',         side: 'right',  depth: 0, verifies: 'system-requirement',  section: 'concept-system-test' },
];

function createVDiagram() {

  function getLevel(id) {
    return V_LEVELS.find(l => l.id === id) || null;
  }

  function getTraces() {
    const traces = [];

    // Horizontal traces: left level verified-by right level
    for (const level of V_LEVELS) {
      if (level.side === 'left' && level.verifiedBy) {
        traces.push({
          from: level.id,
          to: level.verifiedBy,
          type: 'verified-by',
          label: 'verified by',
        });
      }
    }

    // Vertical traces: derivation down the left side
    const leftLevels = V_LEVELS.filter(l => l.side === 'left').sort((a, b) => a.depth - b.depth);
    for (let i = 0; i < leftLevels.length - 1; i++) {
      traces.push({
        from: leftLevels[i].id,
        to: leftLevels[i + 1].id,
        type: 'derived-from',
        label: 'derives',
      });
    }

    // Left bottom to implementation
    if (leftLevels.length > 0) {
      traces.push({
        from: leftLevels[leftLevels.length - 1].id,
        to: 'implementation',
        type: 'derived-from',
        label: 'implements',
      });
    }

    // Implementation to first right side
    const rightLevels = V_LEVELS.filter(l => l.side === 'right').sort((a, b) => b.depth - a.depth);
    if (rightLevels.length > 0) {
      traces.push({
        from: 'implementation',
        to: rightLevels[0].id,
        type: 'verified-by',
        label: 'tested by',
      });
    }

    return traces;
  }

  function getTracesFor(levelId) {
    return getTraces().filter(t => t.from === levelId || t.to === levelId);
  }

  function toSVG(options = {}) {
    const width = options.width || 900;
    const height = options.height || 520;
    const nodeWidth = 160;
    const nodeHeight = 44;
    const padX = 40;
    const padY = 30;

    // Calculate positions
    const maxDepth = Math.max(...V_LEVELS.map(l => l.depth));
    const usableHeight = height - padY * 2 - nodeHeight;
    const stepY = usableHeight / maxDepth;

    function getNodePos(level) {
      const y = padY + level.depth * stepY;
      if (level.side === 'left') {
        // Left side slopes inward as depth increases
        const x = padX + level.depth * ((width / 2 - padX - nodeWidth / 2) / maxDepth);
        return { x, y };
      } else if (level.side === 'right') {
        // Right side mirrors left
        const x = width - padX - nodeWidth - level.depth * ((width / 2 - padX - nodeWidth / 2) / maxDepth);
        return { x, y };
      } else {
        // Bottom center
        const x = (width - nodeWidth) / 2;
        return { x, y };
      }
    }

    // Build SVG string
    const parts = [];
    parts.push(`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 ${width} ${height}" class="v-diagram">`);

    // Defs for arrow markers
    parts.push(`<defs>`);
    parts.push(`<marker id="arrow" viewBox="0 0 10 10" refX="10" refY="5" markerWidth="6" markerHeight="6" orient="auto-start-reverse"><path d="M 0 0 L 10 5 L 0 10 z" fill="var(--trace-color, #888)"/></marker>`);
    parts.push(`</defs>`);

    // Draw trace lines first (behind nodes)
    const traces = getTraces();
    for (const trace of traces) {
      const fromLevel = getLevel(trace.from);
      const toLevel = getLevel(trace.to);
      if (!fromLevel || !toLevel) continue;

      const fromPos = getNodePos(fromLevel);
      const toPos = getNodePos(toLevel);

      // Connect from center-right of source to center-left of target (horizontal)
      // or center-bottom to center-top (vertical)
      let x1, y1, x2, y2;
      if (trace.type === 'verified-by' && fromLevel.side === 'left') {
        // Horizontal dashed line across the V
        x1 = fromPos.x + nodeWidth;
        y1 = fromPos.y + nodeHeight / 2;
        x2 = toPos.x;
        y2 = toPos.y + nodeHeight / 2;
      } else {
        // Vertical solid line down the V
        x1 = fromPos.x + nodeWidth / 2;
        y1 = fromPos.y + nodeHeight;
        x2 = toPos.x + nodeWidth / 2;
        y2 = toPos.y;
      }

      const dashStyle = trace.type === 'verified-by' ? 'stroke-dasharray="6 4"' : '';
      const traceClass = `trace-line trace-${trace.type}`;
      parts.push(`<line x1="${x1}" y1="${y1}" x2="${x2}" y2="${y2}" class="${traceClass}" ${dashStyle} data-from="${trace.from}" data-to="${trace.to}" data-trace-type="${trace.type}" marker-end="url(#arrow)"/>`);
    }

    // Draw nodes
    for (const level of V_LEVELS) {
      const pos = getNodePos(level);
      const colorClass = `v-level-${level.side}`;

      parts.push(`<g class="v-node ${colorClass}" data-level="${level.id}" data-section="${level.section}" style="cursor:pointer" role="button" tabindex="0">`);
      parts.push(`<rect x="${pos.x}" y="${pos.y}" width="${nodeWidth}" height="${nodeHeight}" rx="8" class="v-node-bg"/>`);
      parts.push(`<text x="${pos.x + nodeWidth / 2}" y="${pos.y + nodeHeight / 2 + 5}" text-anchor="middle" class="v-node-label" data-term="${level.id}">${level.label}</text>`);
      parts.push(`</g>`);
    }

    parts.push(`</svg>`);
    return parts.join('\n');
  }

  return {
    getLevel,
    getTraces,
    getTracesFor,
    toSVG,
  };
}

// Support both browser and Node.js
if (typeof module !== 'undefined' && module.exports) {
  module.exports = { createVDiagram, V_LEVELS };
}
