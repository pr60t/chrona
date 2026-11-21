#!/usr/bin/env python3
"""Projects GeoNames cities5000 records into normalized Natural Earth mapX/mapY coordinates."""
import json, math
from pathlib import Path

R_WGS84 = 6378137.0
A0, A1, A2, A3, A4 = 0.870700, -0.131979, -0.013791, 0.003971, -0.001529
B0, B1, B2, B3, B4 = 1.007226, 0.015085, -0.044475, 0.028874, -0.005916

def project_natearth_normalized(lat_deg: float, lon_deg: float, meta: dict):
    phi = max(-56.0, min(84.0, lat_deg)) * math.pi / 180.0
    lam = max(-180.0, min(180.0, lon_deg)) * math.pi / 180.0
    phi2 = phi * phi
    phi4 = phi2 * phi2
    phi6 = phi4 * phi2
    xm = R_WGS84 * lam * (A0 + A1 * phi2 + A2 * phi4 + phi6 * (A3 + A4 * phi2))
    ym = R_WGS84 * phi * (B0 + B1 * phi2 + phi4 * (B2 + B3 * phi2 + B4 * phi4))
    b = meta["projectedBoundingBoxMeters"]
    c = meta["mapContentBounds"]
    s = meta["horizontalMapping"]["uniformScalePxPerMeter"]
    vb_w, vb_h = meta["svgViewBox"][2], meta["svgViewBox"][3]
    svg_x = c["left"] + (xm - b["minX"]) * s
    svg_y = c["top"] + (b["maxY"] - ym) * s
    return svg_x / vb_w, svg_y / vb_h

if __name__ == "__main__":
    root = Path(__file__).resolve().parents[1]
    meta = json.loads((root / "production" / "world_map_projection.json").read_text(encoding="utf-8"))
    print("Kathmandu normalized:", project_natearth_normalized(27.70169, 85.32060, meta))
