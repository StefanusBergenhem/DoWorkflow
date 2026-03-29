"""Tests for the architecture diagram generator tool."""

import os
import sys
import tempfile
import unittest

import yaml

# Add tools directory to path so we can import the module
sys.path.insert(0, os.path.join(os.path.dirname(__file__), "..", "tools"))

from importlib import import_module

# Import the module (filename has hyphens so we use importlib)
generate_arch_diagram = import_module("generate-arch-diagram")
generate_mermaid = generate_arch_diagram.generate_mermaid
update_diagram_in_file = generate_arch_diagram.update_diagram_in_file
indent_multiline = generate_arch_diagram.indent_multiline


class TestGenerateMermaid(unittest.TestCase):
    """Tests for the generate_mermaid function."""

    def test_single_component_no_interfaces(self):
        components = [{"id": "comp-a", "description": "A", "responsibilities": ["do stuff"]}]
        result = generate_mermaid(components, None)
        self.assertIn("graph LR", result)
        self.assertIn("comp_a[comp-a]", result)

    def test_two_components_one_interface(self):
        components = [
            {"id": "sender", "description": "S", "responsibilities": ["send"]},
            {"id": "receiver", "description": "R", "responsibilities": ["receive"]},
        ]
        interfaces = [
            {"from": "sender", "to": "receiver", "mechanism": "function call", "data": "payload"},
        ]
        result = generate_mermaid(components, interfaces)
        self.assertIn("sender[sender]", result)
        self.assertIn("receiver[receiver]", result)
        self.assertIn('sender -->|"payload"| receiver', result)

    def test_three_components_chain(self):
        components = [
            {"id": "a", "description": "A", "responsibilities": ["a"]},
            {"id": "b", "description": "B", "responsibilities": ["b"]},
            {"id": "c", "description": "C", "responsibilities": ["c"]},
        ]
        interfaces = [
            {"from": "a", "to": "b", "mechanism": "call", "data": "x"},
            {"from": "b", "to": "c", "mechanism": "call", "data": "y"},
        ]
        result = generate_mermaid(components, interfaces)
        self.assertIn('a -->|"x"| b', result)
        self.assertIn('b -->|"y"| c', result)

    def test_hyphenated_ids_sanitized(self):
        components = [
            {"id": "fuel-control", "description": "FC", "responsibilities": ["control"]},
        ]
        result = generate_mermaid(components, None)
        self.assertIn("fuel_control[fuel-control]", result)

    def test_long_data_label_truncated(self):
        components = [
            {"id": "a", "description": "A", "responsibilities": ["a"]},
            {"id": "b", "description": "B", "responsibilities": ["b"]},
        ]
        long_data = "x" * 80
        interfaces = [
            {"from": "a", "to": "b", "mechanism": "call", "data": long_data},
        ]
        result = generate_mermaid(components, interfaces)
        # Should be truncated with "..."
        self.assertIn("...", result)
        # No line should contain the full 80-char string
        for line in result.split("\n"):
            self.assertNotIn(long_data, line)

    def test_interface_without_data(self):
        components = [
            {"id": "a", "description": "A", "responsibilities": ["a"]},
            {"id": "b", "description": "B", "responsibilities": ["b"]},
        ]
        interfaces = [
            {"from": "a", "to": "b", "mechanism": "call"},
        ]
        result = generate_mermaid(components, interfaces)
        self.assertIn("a --> b", result)

    def test_empty_interfaces_list(self):
        components = [
            {"id": "solo", "description": "S", "responsibilities": ["exist"]},
        ]
        result = generate_mermaid(components, [])
        self.assertIn("solo[solo]", result)
        self.assertNotIn("-->", result)


class TestIndentMultiline(unittest.TestCase):
    """Tests for the indent_multiline helper."""

    def test_single_line(self):
        result = indent_multiline("hello", 4)
        self.assertEqual(result, "    hello")

    def test_multi_line(self):
        result = indent_multiline("line1\nline2\nline3", 2)
        self.assertEqual(result, "  line1\n  line2\n  line3")

    def test_zero_indent(self):
        result = indent_multiline("hello", 0)
        self.assertEqual(result, "hello")


class TestUpdateDiagramInFile(unittest.TestCase):
    """Tests for the update_diagram_in_file function (file I/O)."""

    def _write_temp_artifact(self, content):
        fd, path = tempfile.mkstemp(suffix=".yaml")
        os.close(fd)
        with open(path, "w") as f:
            f.write(content)
        self.addCleanup(lambda: os.unlink(path))
        return path

    def test_inserts_diagram_into_artifact_without_one(self):
        content = """\
artifact_id: "ARCH-001"
artifact_type: sw-architecture
version: "1.0.0"
status: draft

body:
  title: "Test Architecture"
  description: "Test"

  components:
    - id: "comp-a"
      description: "Component A"
      responsibilities:
        - "Do A"

    - id: "comp-b"
      description: "Component B"
      responsibilities:
        - "Do B"

  interfaces:
    - from: "comp-a"
      to: "comp-b"
      mechanism: "function call"
      data: "some data"

  design_rationale: "Test rationale"

  notes: "Test notes"
"""
        path = self._write_temp_artifact(content)
        update_diagram_in_file(path, None)

        with open(path) as f:
            result = f.read()

        # Should contain diagram field
        self.assertIn("diagram: |", result)
        self.assertIn("graph LR", result)
        self.assertIn("comp_a[comp-a]", result)
        self.assertIn('comp_a -->|"some data"| comp_b', result)

    def test_replaces_existing_diagram(self):
        content = """\
artifact_id: "ARCH-001"
artifact_type: sw-architecture
version: "1.0.0"
status: draft

body:
  title: "Test Architecture"
  description: "Test"

  components:
    - id: "new-comp"
      description: "New Component"
      responsibilities:
        - "Do new things"

  design_rationale: "Test rationale"

  diagram: |
    graph LR
      old_comp[old-comp]

  notes: "Test notes"
"""
        path = self._write_temp_artifact(content)
        update_diagram_in_file(path, None)

        with open(path) as f:
            result = f.read()

        self.assertIn("new_comp[new-comp]", result)
        self.assertNotIn("old_comp", result)

    def test_rejects_non_architecture_artifact(self):
        content = """\
artifact_id: "DD-001"
artifact_type: detailed-design
version: "1.0.0"
status: draft

body:
  title: "Not an architecture"
"""
        path = self._write_temp_artifact(content)

        with self.assertRaises(SystemExit):
            update_diagram_in_file(path, None)

    def test_rejects_artifact_without_components(self):
        content = """\
artifact_id: "ARCH-001"
artifact_type: sw-architecture
version: "1.0.0"
status: draft

body:
  title: "Empty Architecture"
  description: "No components"
"""
        path = self._write_temp_artifact(content)

        with self.assertRaises(SystemExit):
            update_diagram_in_file(path, None)

    def test_preserves_other_fields(self):
        content = """\
artifact_id: "ARCH-002"
artifact_type: sw-architecture
version: "2.0.0"
status: draft

body:
  title: "Preserved Fields Test"
  description: "Testing field preservation"

  components:
    - id: "only-comp"
      description: "The only component"
      responsibilities:
        - "Everything"

  design_rationale: "Kept intact"

  notes: "This should still be here"
"""
        path = self._write_temp_artifact(content)
        update_diagram_in_file(path, None)

        with open(path) as f:
            result = f.read()

        self.assertIn('artifact_id: "ARCH-002"', result)
        self.assertIn('version: "2.0.0"', result)
        self.assertIn("Preserved Fields Test", result)
        self.assertIn("Kept intact", result)
        self.assertIn("This should still be here", result)
        self.assertIn("diagram: |", result)


if __name__ == "__main__":
    unittest.main()
