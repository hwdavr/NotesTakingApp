#!/usr/bin/env python3
import argparse
import json
import math
import re
import xml.etree.ElementTree as ET
from collections import Counter
from pathlib import Path


def strip_ns(tag: str) -> str:
    return tag.split('}', 1)[-1] if '}' in tag else tag


def parse_number(v, default=0.0):
    if v is None:
        return default
    m = re.search(r'-?\d+(?:\.\d+)?', str(v))
    return float(m.group(0)) if m else default


def parse_viewbox(root):
    vb = root.get('viewBox')
    if vb:
        nums = [float(x) for x in re.split(r'[\s,]+', vb.strip()) if x]
        if len(nums) == 4:
            return {'minX': nums[0], 'minY': nums[1], 'width': nums[2], 'height': nums[3]}
    return {
        'minX': 0,
        'minY': 0,
        'width': parse_number(root.get('width'), 0),
        'height': parse_number(root.get('height'), 0),
    }


def classify_element(el):
    tag = strip_ns(el.tag)
    base = {'tag': tag}
    if tag == 'rect':
        x = parse_number(el.get('x'))
        y = parse_number(el.get('y'))
        w = parse_number(el.get('width'))
        h = parse_number(el.get('height'))
        base.update({'x': x, 'y': y, 'width': w, 'height': h, 'cx': x + w / 2, 'cy': y + h / 2})
    elif tag == 'circle':
        cx = parse_number(el.get('cx'))
        cy = parse_number(el.get('cy'))
        r = parse_number(el.get('r'))
        base.update({'x': cx - r, 'y': cy - r, 'width': 2 * r, 'height': 2 * r, 'cx': cx, 'cy': cy})
    elif tag in ('image',):
        x = parse_number(el.get('x'))
        y = parse_number(el.get('y'))
        w = parse_number(el.get('width'))
        h = parse_number(el.get('height'))
        base.update({'x': x, 'y': y, 'width': w, 'height': h, 'cx': x + w / 2, 'cy': y + h / 2})
    elif tag == 'text':
        x = parse_number(el.get('x'))
        y = parse_number(el.get('y'))
        txt = ''.join(el.itertext()).strip()
        fs = parse_number(el.get('font-size'), 0)
        base.update({'x': x, 'y': y, 'width': 0, 'height': 0, 'cx': x, 'cy': y, 'text': txt, 'fontSize': fs})
    else:
        base.update({'x': None, 'y': None, 'width': None, 'height': None, 'cx': None, 'cy': None})
    return base


def guess_bands(elements, artboard_height):
    ys = sorted([e['cy'] for e in elements if e.get('cy') is not None])
    if not ys:
        return []
    threshold = max(24, artboard_height * 0.04)
    bands = []
    current = [ys[0]]
    for y in ys[1:]:
        if abs(y - current[-1]) <= threshold:
            current.append(y)
        else:
            bands.append((min(current), max(current)))
            current = [y]
    bands.append((min(current), max(current)))
    return bands


def main():
    ap = argparse.ArgumentParser(description='Analyze an SVG screen export and emit a quick structural summary.')
    ap.add_argument('svg', help='Path to SVG file')
    ap.add_argument('--json', action='store_true', help='Emit JSON instead of markdown-ish text')
    args = ap.parse_args()

    root = ET.parse(args.svg).getroot()
    artboard = parse_viewbox(root)
    elements = [classify_element(el) for el in root.iter() if strip_ns(el.tag) != 'svg']
    tags = Counter(e['tag'] for e in elements)
    visible = [e for e in elements if e.get('cy') is not None]
    bands = guess_bands(visible, artboard['height'] or 800)
    texts = [e for e in elements if e['tag'] == 'text' and e.get('text')]
    repeated_rect_sizes = Counter(
        (round(e['width'] or 0, 1), round(e['height'] or 0, 1))
        for e in elements if e['tag'] == 'rect' and (e['width'] or 0) > 0 and (e['height'] or 0) > 0
    )
    repeated_rect_sizes = [
        {'size': [w, h], 'count': c}
        for (w, h), c in repeated_rect_sizes.items() if c >= 2
    ]

    report = {
        'artboard': artboard,
        'elementCounts': dict(tags),
        'textSamples': [t['text'] for t in texts[:10]],
        'horizontalBands': [{'startY': a, 'endY': b} for a, b in bands],
        'repeatedRectSizes': repeated_rect_sizes,
        'notes': [
            'Use this as a rough heuristic only.',
            'Bands suggest possible headers, sections, lists, or footers.',
            'Repeated rect sizes often indicate cards, buttons, inputs, or rows.',
        ],
    }

    if args.json:
        print(json.dumps(report, indent=2, ensure_ascii=False))
        return

    print('# SVG screen analysis')
    print()
    print(f"- Artboard: {artboard['width']} x {artboard['height']}")
    print('- Element counts:')
    for k, v in sorted(tags.items()):
        print(f'  - {k}: {v}')
    if texts:
        print('- Text samples:')
        for t in texts[:10]:
            fs = t.get('fontSize')
            suffix = f' (fontSize={fs:g})' if fs else ''
            print(f"  - {t['text']}{suffix}")
    if bands:
        print('- Approximate horizontal bands:')
        for a, b in bands:
            print(f'  - y≈{a:.1f}..{b:.1f}')
    if repeated_rect_sizes:
        print('- Repeated rectangle sizes:')
        for item in repeated_rect_sizes:
            print(f"  - {item['size'][0]} x {item['size'][1]} (count={item['count']})")
    print('- Notes:')
    for note in report['notes']:
        print(f'  - {note}')


if __name__ == '__main__':
    main()
