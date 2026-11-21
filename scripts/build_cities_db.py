#!/usr/bin/env python3
"""
Chrona Cities Database Generator
Reproducibly compiles all geographic datasets into the optimized SQLite cities.db asset.
Sources:
 - GeoNames cities500.txt (237,320 cities, CC BY 4.0)
 - GeoNames countryInfo.txt (252 countries, CC BY 4.0)
 - GeoNames admin1CodesASCII.txt (3,894 subdivisions, CC BY 4.0)
 - Natural Earth Projection 1:110m (CC0 Public Domain)
 - Universal Time Coordinated (UTC geonameId=1)
"""

import zipfile
import sqlite3
import os
import sys
import unicodedata
import math
from pathlib import Path

def normalize_text(text: str) -> str:
    nfkd = unicodedata.normalize('NFKD', text)
    return ''.join([c for c in nfkd if not unicodedata.combining(c)]).lower()

# Natural Earth Projection Math (Savric et al. 2011)
R_WGS84 = 6378137.0
MIN_X = -17446561.5882
MIN_Y = -6247632.5527
MAX_Y = 8816775.4882
CONTENT_LEFT = 24.0
CONTENT_TOP = 51.3235
UNIFORM_SCALE = 3.301510140485457e-05
VIEWPORT_WIDTH = 1200.0
VIEWPORT_HEIGHT = 600.0
A0, A1, A2, A3, A4 = 0.870700, -0.131979, -0.013791, 0.003971, -0.001529
B0, B1, B2, B3, B4 = 1.007226, 0.015085, -0.044475, 0.028874, -0.005916

def lat_lon_to_normalized(lat: float, lon: float):
    phi = max(-56.0, min(84.0, lat)) * math.pi / 180.0
    lam = max(-179.999, min(179.999, lon)) * math.pi / 180.0
    phi2 = phi * phi
    phi4 = phi2 * phi2
    phi6 = phi4 * phi2
    xm = R_WGS84 * lam * (A0 + A1 * phi2 + A2 * phi4 + phi6 * (A3 + A4 * phi2))
    ym = R_WGS84 * phi * (B0 + B1 * phi2 + phi4 * (B2 + B3 * phi2 + B4 * phi4))
    svg_x = CONTENT_LEFT + (xm - MIN_X) * UNIFORM_SCALE
    svg_y = CONTENT_TOP + (MAX_Y - ym) * UNIFORM_SCALE
    return svg_x / VIEWPORT_WIDTH, svg_y / VIEWPORT_HEIGHT

PALETTE = [
    '#B8C7F2', '#A7E8D8', '#F4C2A1', '#D4B8F0', '#F2DF9B',
    '#E7A19A', '#EAB994', '#8FD8B9', '#C3B1E1', '#F7D070'
]

def build_database(materials_dir: Path, output_db: Path):
    country_info_path = materials_dir / 'countryInfo.txt'
    admin1_path = materials_dir / 'admin1CodesASCII.txt'
    cities_zip_path = materials_dir / 'cities500.zip'

    if not cities_zip_path.exists():
        print(f"Error: {cities_zip_path} not found!")
        sys.exit(1)

    country_map = {}
    if country_info_path.exists():
        with open(country_info_path, encoding='utf-8') as f:
            for line in f:
                if line.startswith('#'): continue
                parts = line.strip().split('\t')
                if len(parts) >= 5:
                    country_map[parts[0]] = parts[4]

    admin1_map = {}
    if admin1_path.exists():
        with open(admin1_path, encoding='utf-8') as f:
            for line in f:
                parts = line.strip().split('\t')
                if len(parts) >= 2:
                    admin1_map[parts[0]] = parts[1]

    if output_db.exists():
        output_db.unlink()
    output_db.parent.mkdir(parents=True, exist_ok=True)

    conn = sqlite3.connect(output_db)
    cur = conn.cursor()
    cur.execute('PRAGMA page_size = 4096')
    cur.execute('PRAGMA journal_mode = OFF')
    cur.execute('PRAGMA synchronous = OFF')

    cur.execute('''
    CREATE TABLE cities (
        geoname_id INTEGER PRIMARY KEY,
        city_name TEXT NOT NULL,
        ascii_name TEXT NOT NULL,
        country_code TEXT NOT NULL,
        country_name TEXT NOT NULL,
        admin1_code TEXT,
        admin1_name TEXT,
        latitude REAL NOT NULL,
        longitude REAL NOT NULL,
        zone_id TEXT NOT NULL,
        population INTEGER NOT NULL,
        search_aliases TEXT,
        map_x REAL NOT NULL,
        map_y REAL NOT NULL,
        identity_color_hex TEXT NOT NULL
    )
    ''')

    rows = []
    # 1. UTC Special Baseline (Zone: Etc/UTC, No physical coordinates)
    rows.append((
        1, 'UTC', 'utc', 'UN', 'Universal Time Coordinated',
        '', 'Global', 0.0, 0.0, 'Etc/UTC', 0,
        'gmt,zulu,universal time', 0.0, 0.0, '#B8C7F2'
    ))

    # 2. Parse GeoNames cities500
    with zipfile.ZipFile(cities_zip_path) as z:
        with z.open('cities500.txt') as f:
            for line in f:
                parts = line.decode('utf-8', errors='replace').split('\t')
                gid = int(parts[0])
                name = parts[1]
                ascii_name = parts[2].lower()
                raw_alt = parts[3]
                if raw_alt:
                    alt_list = [normalize_text(a.strip()) for a in raw_alt.split(',') if a.strip()]
                    seen = set([ascii_name])
                    dedup = []
                    for a in alt_list:
                        if a not in seen and len(a) <= 60:
                            seen.add(a)
                            dedup.append(a)
                    search_aliases = ','.join(dedup[:50])
                else:
                    search_aliases = ''

                lat = float(parts[4])
                lon = float(parts[5])
                cc = parts[8]
                admin1 = parts[10]
                pop = int(parts[14]) if parts[14] else 0
                tz = parts[17]
                if not tz: continue

                c_name = country_map.get(cc, cc)
                a_key = f'{cc}.{admin1}'
                a_name = admin1_map.get(a_key, '')
                mx, my = lat_lon_to_normalized(lat, lon)
                color = PALETTE[gid % len(PALETTE)]
                rows.append((
                    gid, name, ascii_name, cc, c_name,
                    admin1, a_name, lat, lon, tz, pop,
                    search_aliases, mx, my, color
                ))

    cur.executemany('''
    INSERT INTO cities VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    ''', rows)
    conn.commit()

    cur.execute('CREATE INDEX idx_cities_ascii ON cities(ascii_name)')
    cur.execute('CREATE INDEX idx_cities_cc ON cities(country_code)')
    cur.execute('CREATE INDEX idx_cities_pop ON cities(population DESC)')
    cur.execute('CREATE INDEX idx_cities_country ON cities(country_name)')
    cur.execute('CREATE INDEX idx_cities_admin1 ON cities(admin1_name)')
    cur.execute('PRAGMA optimize')
    conn.commit()
    cur.execute('VACUUM')
    conn.close()

    print(f"Successfully generated {output_db} with {len(rows)} records ({output_db.stat().st_size / (1024*1024):.2f} MB)")

if __name__ == '__main__':
    import argparse
    project_root = Path(__file__).resolve().parent.parent
    default_mat_dir = Path.home() / 'chrona-materials' / 'map'
    default_out_file = project_root / 'app' / 'src' / 'main' / 'assets' / 'databases' / 'cities.db'

    parser = argparse.ArgumentParser(description="Build cities SQLite database for Chrona")
    parser.add_argument("--materials", type=Path, default=default_mat_dir, help="Directory containing cities15000.txt and map materials")
    parser.add_argument("--output", type=Path, default=default_out_file, help="Output SQLite database path")
    args = parser.parse_args()

    build_database(args.materials, args.output)
