#!/usr/bin/env python3
"""Inspects Natural Earth 1:110m shapefile and companion files for Chrona."""
import json, struct
from pathlib import Path

def main():
    base = Path(__file__).resolve().parents[1] / "source" / "original_natural_earth"
    manifest = json.loads((base.parent / "source_manifest.json").read_text(encoding="utf-8"))
    print(json.dumps(manifest, indent=2))

if __name__ == "__main__":
    main()
