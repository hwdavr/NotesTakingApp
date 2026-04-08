#!/usr/bin/env python3
import argparse
import json
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
            return {'x': nums[0], 'y': nums[1], 'width': nums[2], 'height': nums[3]}
    return {'x': 0, 'y': 0, 'width': parse_number(root.get('width'), 360), 'height': parse_number(root.get('height'), 800)}


def collect(root):
    rects, texts, others = [], [], []
    for el in root.iter():
        tag = strip_ns(el.tag)
        if tag == 'svg':
            continue
        if tag == 'rect':
            rects.append({
                'tag': tag,
                'x': parse_number(el.get('x')),
                'y': parse_number(el.get('y')),
                'width': parse_number(el.get('width')),
                'height': parse_number(el.get('height')),
                'rx': parse_number(el.get('rx')),
                'fill': el.get('fill'),
            })
        elif tag == 'text':
            texts.append({
                'tag': tag,
                'x': parse_number(el.get('x')),
                'y': parse_number(el.get('y')),
                'width': 0,
                'height': 0,
                'text': ''.join(el.itertext()).strip(),
                'fontSize': parse_number(el.get('font-size'), 16),
                'fill': el.get('fill'),
            })
        else:
            others.append({'tag': tag})
    return rects, texts, others


def pick_title(texts):
    if not texts:
        return None
    return sorted(texts, key=lambda t: (-t['fontSize'], t['y']))[0]


def repeated_rect_groups(rects):
    counts = Counter((round(r['width'], 1), round(r['height'], 1), round(r['rx'], 1), r.get('fill')) for r in rects if r['width'] > 0 and r['height'] > 0)
    repeated = {k for k, v in counts.items() if v >= 3}
    groups = {}
    gid = 1
    for r in rects:
        key = (round(r['width'], 1), round(r['height'], 1), round(r['rx'], 1), r.get('fill'))
        if key in repeated:
            if key not in groups:
                groups[key] = f'repeat-group-{gid}'
                gid += 1
    return groups


def quantized_spacing(values):
    vals = sorted({int(round(v)) for v in values if v and v > 0})
    return vals[:12]


def main():
    ap = argparse.ArgumentParser(description='Export normalized screen-structure JSON from a simple SVG screen.')
    ap.add_argument('svg', help='Input SVG path')
    ap.add_argument('--out', help='Output JSON file; defaults to stdout')
    args = ap.parse_args()

    src = Path(args.svg)
    root = ET.parse(src).getroot()
    artboard = parse_viewbox(root)
    rects, texts, others = collect(root)
    title = pick_title(texts)
    repeat_groups = repeated_rect_groups(rects)

    colors = []
    for r in rects:
        if r.get('fill'):
            colors.append(r['fill'])
    for t in texts:
        if t.get('fill'):
            colors.append(t['fill'])
    colors = sorted(dict.fromkeys(colors))

    spacing = []
    rects_sorted = sorted(rects, key=lambda r: (r['y'], r['x']))
    for a, b in zip(rects_sorted, rects_sorted[1:]):
        gap = b['y'] - (a['y'] + a['height'])
        if gap > 0:
            spacing.append(gap)
    corner_radii = sorted({int(round(r['rx'])) for r in rects if r.get('rx')})

    regions = []
    components = []
    notes = []

    bg = None
    for r in rects:
        if r['x'] == 0 and r['y'] == 0 and abs(r['width'] - artboard['width']) < 1 and abs(r['height'] - artboard['height']) < 1:
            bg = r
            break
    if bg:
        components.append({
            'id': 'component-background',
            'type': 'container',
            'inferredRole': 'background',
            'bounds': {'x': bg['x'], 'y': bg['y'], 'width': bg['width'], 'height': bg['height']},
            'text': None,
            'style': {'fill': bg.get('fill'), 'cornerRadius': bg.get('rx') or None},
            'children': [],
            'repeatGroupId': None,
            'fallbackType': 'none',
            'confidence': 0.99,
        })

    if title:
        components.append({
            'id': 'component-title',
            'type': 'text',
            'inferredRole': 'screen-title',
            'bounds': {'x': title['x'], 'y': title['y'], 'width': 0, 'height': 0},
            'text': {'value': title['text'], 'fontSize': title['fontSize'], 'fontWeight': None},
            'style': {'fill': title.get('fill'), 'cornerRadius': None},
            'children': [],
            'repeatGroupId': None,
            'fallbackType': 'none',
            'confidence': 0.92,
        })
        regions.append({
            'id': 'region-header',
            'role': 'header',
            'bounds': {'x': 0, 'y': 0, 'width': artboard['width'], 'height': max(80, title['y'] + 24)},
            'children': ['component-title'],
            'fallbackType': None,
        })

    non_bg_rects = [r for r in rects if r is not bg]
    large_cards = [r for r in non_bg_rects if r['width'] >= artboard['width'] * 0.6 and 80 <= r['height'] <= 220]
    if large_cards:
        hero = sorted(large_cards, key=lambda r: (r['y'], -r['width']))[0]
        components.append({
            'id': 'component-hero-card',
            'type': 'card',
            'inferredRole': 'hero-card',
            'bounds': {'x': hero['x'], 'y': hero['y'], 'width': hero['width'], 'height': hero['height']},
            'text': None,
            'style': {'fill': hero.get('fill'), 'cornerRadius': hero.get('rx') or None},
            'children': [],
            'repeatGroupId': None,
            'fallbackType': 'none',
            'confidence': 0.78,
        })
        regions.append({
            'id': 'region-hero',
            'role': 'hero',
            'bounds': {'x': hero['x'], 'y': hero['y'], 'width': hero['width'], 'height': hero['height']},
            'children': ['component-hero-card'],
            'fallbackType': None,
        })

    repeat_index = 1
    for r in non_bg_rects:
        key = (round(r['width'], 1), round(r['height'], 1), round(r['rx'], 1), r.get('fill'))
        repeat_group_id = repeat_groups.get(key)
        if repeat_group_id:
            cid = f'component-list-row-{repeat_index}'
            repeat_index += 1
            components.append({
                'id': cid,
                'type': 'list-row',
                'inferredRole': 'list-item',
                'bounds': {'x': r['x'], 'y': r['y'], 'width': r['width'], 'height': r['height']},
                'text': None,
                'style': {'fill': r.get('fill'), 'cornerRadius': r.get('rx') or None},
                'children': [],
                'repeatGroupId': repeat_group_id,
                'fallbackType': 'none',
                'confidence': 0.74,
            })

    if any(c.get('repeatGroupId') for c in components):
        list_children = [c['id'] for c in components if c.get('repeatGroupId')]
        ys = [c['bounds']['y'] for c in components if c.get('repeatGroupId')]
        hs = [c['bounds']['height'] for c in components if c.get('repeatGroupId')]
        if ys and hs:
            regions.append({
                'id': 'region-list',
                'role': 'list',
                'bounds': {
                    'x': min(c['bounds']['x'] for c in components if c.get('repeatGroupId')),
                    'y': min(ys),
                    'width': max(c['bounds']['width'] for c in components if c.get('repeatGroupId')),
                    'height': (max(ys) + hs[ys.index(max(ys))]) - min(ys),
                },
                'children': list_children,
                'fallbackType': None,
            })

    if others:
        tags = sorted({o['tag'] for o in others})
        notes.append(f'Unhandled SVG element types observed: {", ".join(tags)}')
        if any(t in tags for t in ['path', 'image', 'mask', 'filter', 'clipPath']):
            notes.append('Some regions may require vector/image fallback or manual reconstruction.')

    structure = {
        'version': 1,
        'source': {'type': 'svg', 'path': str(src)},
        'artboard': artboard,
        'tokens': {
            'colors': colors,
            'spacing': quantized_spacing(spacing),
            'cornerRadii': corner_radii,
        },
        'regions': regions,
        'components': components,
        'notes': notes,
    }

    text = json.dumps(structure, indent=2, ensure_ascii=False)
    if args.out:
        Path(args.out).write_text(text + '\n', encoding='utf-8')
    else:
        print(text)


if __name__ == '__main__':
    main()
