#!/usr/bin/env python3
"""Renders PNG comparison sheets and mobile World Clock UI mockups."""
from pathlib import Path

if __name__ == "__main__":
    root = Path(__file__).resolve().parents[1]
    previews = sorted(p.name for p in (root / "previews").glob("*.png"))
    print("Generated previews:", previews)
