/**
 * Navigation and Section Switching
 *
 * Single Responsibility: manages which content section is visible and which nav link is active.
 * Dependency Inversion: accepts a document interface, does not depend on global `document`.
 */

function createNavigation(doc) {
  let currentSectionId = null;

  function getAllSections() {
    return Array.from(doc.querySelectorAll('.content-section'));
  }

  function getAllNavLinks() {
    return Array.from(doc.querySelectorAll('.nav-link'));
  }

  function getSectionIds() {
    return getAllSections().map(s => s.getAttribute('id'));
  }

  function getCurrentSection() {
    return currentSectionId;
  }

  function showSection(sectionId, subsectionId) {
    const sections = getAllSections();
    const links = getAllNavLinks();

    // Resolve: if unknown or null, default to first section
    const ids = getSectionIds();
    const targetId = (sectionId && ids.includes(sectionId)) ? sectionId : ids[0];

    // Update sections
    for (const section of sections) {
      const id = section.getAttribute('id');
      if (id === targetId) {
        section.classList.add('active');
      } else {
        section.classList.remove('active');
      }
    }

    // Update nav links
    for (const link of links) {
      const linkSection = link.getAttribute('data-section') ||
        (link.getAttribute('href') || '').replace('#', '');
      if (linkSection === targetId) {
        link.classList.add('active');
      } else {
        link.classList.remove('active');
      }
    }

    currentSectionId = targetId;

    // Handle subsection highlighting
    if (subsectionId) {
      const activeSection = sections.find(s => s.getAttribute('id') === targetId);
      if (activeSection) {
        // Clear previous highlights
        const subs = activeSection.querySelectorAll('.subsection');
        for (const sub of subs) {
          sub.classList.remove('highlighted');
        }
        // Highlight target subsection
        const target = activeSection.querySelectorAll('.subsection')
          .find ? null : null; // querySelectorAll returns array-like
        const allSubs = Array.from(activeSection.querySelectorAll('.subsection'));
        for (const sub of allSubs) {
          if (sub.getAttribute('id') === subsectionId) {
            sub.classList.add('highlighted');
            sub.scrollIntoView();
          }
        }
      }
    }
  }

  return {
    showSection,
    getCurrentSection,
    getSectionIds,
  };
}

// Support both browser and Node.js
if (typeof module !== 'undefined' && module.exports) {
  module.exports = { createNavigation };
}
