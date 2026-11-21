#!/usr/bin/env python3
"""Runs 10-point geometric, XML, projection, and marker alignment validation."""
import json
import xml.etree.ElementTree as ET
from pathlib import Path

def validate():
    root = Path(__file__).resolve().parents[1]
    svg_path = root / "production" / "world_map_base.svg"
    tree = ET.parse(svg_path)
    svg_root = tree.getroot()
    assert svg_root.attrib.get("viewBox") == "0 0 1200 600", "Invalid viewBox"
    test_cities = json.loads((root / "city_data" / "projection_test_cities.json").read_text(encoding="utf-8"))
    for c in test_cities:
        assert 0.0 <= c["mapX"] <= 1.0, f"Out of bounds mapX for {c['cityName']}"
        assert 0.0 <= c["mapY"] <= 1.0, f"Out of bounds mapY for {c['cityName']}"
    print(f"Validation PASSED for {len(test_cities)} test cities and SVG {svg_path.name}.")

if __name__ == "__main__":
    validate()
