#!/usr/bin/env python3
"""Builds Chrona production Natural Earth SVG (1200x600) and Android VectorDrawable XML."""
import json
from pathlib import Path

if __name__ == "__main__":
    root = Path(__file__).resolve().parents[1]
    meta = json.loads((root / "production" / "world_map_projection.json").read_text(encoding="utf-8"))
    print("Production projection:", meta["projectionName"], meta["svgViewBox"])
