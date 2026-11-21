#!/usr/bin/env python3
"""Generates Natural Earth, Robinson, and Equirectangular SVG projection variants."""
import json
from pathlib import Path
import pyproj

PROJ_DEFS = {
    "natearth": "+proj=natearth +lon_0=0 +x_0=0 +y_0=0 +datum=WGS84 +units=m +no_defs",
    "robin": "+proj=robin +lon_0=0 +x_0=0 +y_0=0 +datum=WGS84 +units=m +no_defs",
    "eqc": "+proj=eqc +lat_ts=0 +lon_0=0 +x_0=0 +y_0=0 +datum=WGS84 +units=m +no_defs",
}

if __name__ == "__main__":
    for name, pstr in PROJ_DEFS.items():
        t = pyproj.Transformer.from_crs("EPSG:4326", pstr, always_xy=True)
        print(name, "sample (0,0)->", t.transform(0.0, 0.0))
