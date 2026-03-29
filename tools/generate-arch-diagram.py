#!/usr/bin/env python3
"""
Generate Mermaid diagram from SW Architecture artifact.

Reads components and interfaces from an sw-architecture YAML file,
generates a Mermaid diagram, and writes it into the diagram field.

Usage:
    python generate-arch-diagram.py <artifact-file>
    python generate-arch-diagram.py docs/artifacts/sw-architecture/ARCH-001.yaml

The file is updated in-place. The diagram field is inserted or replaced.
"""

import sys
import re
from pathlib import Path

try:
    import yaml
except ImportError:
    print("Error: PyYAML is required. Install with: pip install pyyaml")
    sys.exit(1)


def generate_mermaid(components, interfaces):
    """Generate Mermaid diagram string from components and interfaces."""
    lines = ["graph LR"]

    # Add nodes for each component
    for comp in components:
        comp_id = comp["id"]
        # Mermaid node: use sanitized id and display name
        node_id = comp_id.replace("-", "_")
        lines.append(f"  {node_id}[{comp_id}]")

    # Add edges for each interface
    if interfaces:
        lines.append("")
        for iface in interfaces:
            from_id = iface["from"].replace("-", "_")
            to_id = iface["to"].replace("-", "_")
            data = iface.get("data", "")
            if data:
                # Truncate long labels for readability
                if len(data) > 60:
                    data = data[:57] + "..."
                lines.append(f'  {from_id} -->|"{data}"| {to_id}')
            else:
                lines.append(f"  {from_id} --> {to_id}")

    return "\n".join(lines)


def update_diagram_in_file(file_path, mermaid_text):
    """Read YAML file, update diagram field, write back."""
    path = Path(file_path)
    content = path.read_text()

    # Parse to validate and extract data
    doc = yaml.safe_load(content)

    if doc.get("artifact_type") != "sw-architecture":
        print(f"Error: {file_path} is not an sw-architecture artifact")
        sys.exit(1)

    body = doc.get("body", {})
    components = body.get("components", [])
    interfaces = body.get("interfaces", [])

    if not components:
        print(f"Error: no components found in {file_path}")
        sys.exit(1)

    # Generate the diagram
    diagram = generate_mermaid(components, interfaces)

    # Update the file content using text manipulation to preserve
    # formatting, comments, and field order (not re-serializing YAML)
    diagram_yaml = indent_multiline(diagram, 4)

    if "  diagram:" in content:
        # Replace existing diagram field
        content = re.sub(
            r"(  diagram:) \|.*?(?=\n  [a-z]|\nassurance_level:|\ncontent_hash:|\Z)",
            f"\\1 |\n{diagram_yaml}\n",
            content,
            flags=re.DOTALL,
        )
    else:
        # Insert diagram field before constraints, notes, or end of body
        # Find a good insertion point
        for marker in ["  constraints:", "  notes:"]:
            if marker in content:
                content = content.replace(
                    marker,
                    f"  diagram: |\n{diagram_yaml}\n\n{marker}",
                )
                break
        else:
            # Append to end of file
            content = content.rstrip() + f"\n\n  diagram: |\n{diagram_yaml}\n"

    path.write_text(content)
    print(f"Updated diagram in {file_path}")
    print(f"\nGenerated Mermaid:")
    print(diagram)


def indent_multiline(text, spaces):
    """Indent each line of text by given number of spaces."""
    prefix = " " * spaces
    return "\n".join(prefix + line for line in text.split("\n"))


def main():
    if len(sys.argv) != 2:
        print(__doc__.strip())
        sys.exit(1)

    file_path = sys.argv[1]

    if not Path(file_path).exists():
        print(f"Error: file not found: {file_path}")
        sys.exit(1)

    update_diagram_in_file(file_path, None)


if __name__ == "__main__":
    main()
