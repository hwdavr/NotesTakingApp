#!/usr/bin/env python3
import argparse
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
            return nums[2], nums[3]
    return parse_number(root.get('width'), 360), parse_number(root.get('height'), 800)


def collect_rects_and_text(root):
    rects, texts = [], []
    for el in root.iter():
        tag = strip_ns(el.tag)
        if tag == 'rect':
            rects.append({
                'x': parse_number(el.get('x')),
                'y': parse_number(el.get('y')),
                'width': parse_number(el.get('width')),
                'height': parse_number(el.get('height')),
                'rx': parse_number(el.get('rx')),
                'fill': el.get('fill'),
            })
        elif tag == 'text':
            texts.append({
                'x': parse_number(el.get('x')),
                'y': parse_number(el.get('y')),
                'text': ''.join(el.itertext()).strip() or 'Text',
                'fontSize': parse_number(el.get('font-size'), 16),
                'fill': el.get('fill'),
            })
    return rects, texts


def color_to_compose(fill):
    if not fill:
        return None
    fill = fill.strip()
    if re.fullmatch(r'#[0-9a-fA-F]{6}', fill):
        return f'Color(0xFF{fill[1:].upper()})'
    if re.fullmatch(r'#[0-9a-fA-F]{8}', fill):
        return f'Color(0x{fill[1:].upper()})'
    named = {
        'white': 'Color.White',
        'black': 'Color.Black',
        'red': 'Color.Red',
        'blue': 'Color.Blue',
        'green': 'Color.Green',
    }
    return named.get(fill.lower())


def pascal(name):
    parts = [p for p in re.split(r'[^a-zA-Z0-9]+', name) if p]
    return ''.join(p[:1].upper() + p[1:] for p in parts) or 'GeneratedScreen'


def infer_title(texts):
    if not texts:
        return None
    return sorted(texts, key=lambda t: (-t['fontSize'], t['y']))[0]


def repeated_rect_pattern(rects):
    c = Counter((round(r['width'], 1), round(r['height'], 1)) for r in rects if r['width'] > 0 and r['height'] > 0)
    matches = [(size, count) for size, count in c.items() if count >= 3]
    if not matches:
        return None
    matches.sort(key=lambda x: (-x[1], -x[0][0] * x[0][1]))
    return matches[0][0]


def generate(screen_name, width, height, rects, texts):
    title = infer_title(texts)
    rep = repeated_rect_pattern(rects)
    bg_rect = None
    for r in rects:
        if r['x'] == 0 and r['y'] == 0 and abs(r['width'] - width) < 1 and abs(r['height'] - height) < 1:
            bg_rect = r
            break

    lines = []
    lines += [
        'import androidx.compose.foundation.background',
        'import androidx.compose.foundation.layout.Arrangement',
        'import androidx.compose.foundation.layout.Box',
        'import androidx.compose.foundation.layout.Column',
        'import androidx.compose.foundation.layout.Row',
        'import androidx.compose.foundation.layout.Spacer',
        'import androidx.compose.foundation.layout.fillMaxSize',
        'import androidx.compose.foundation.layout.fillMaxWidth',
        'import androidx.compose.foundation.layout.height',
        'import androidx.compose.foundation.layout.padding',
        'import androidx.compose.foundation.layout.size',
        'import androidx.compose.foundation.lazy.LazyColumn',
        'import androidx.compose.foundation.lazy.items',
        'import androidx.compose.foundation.shape.RoundedCornerShape',
        'import androidx.compose.material3.Card',
        'import androidx.compose.material3.CardDefaults',
        'import androidx.compose.material3.MaterialTheme',
        'import androidx.compose.material3.Surface',
        'import androidx.compose.material3.Text',
        'import androidx.compose.runtime.Composable',
        'import androidx.compose.ui.Alignment',
        'import androidx.compose.ui.Modifier',
        'import androidx.compose.ui.graphics.Color',
        'import androidx.compose.ui.text.font.FontWeight',
        'import androidx.compose.ui.unit.dp',
        'import androidx.compose.ui.unit.sp',
        '',
    ]
    lines.append('@Composable')
    lines.append(f'fun {screen_name}Screen() {{')
    bg = color_to_compose(bg_rect["fill"]) if bg_rect else 'MaterialTheme.colorScheme.background'
    lines.append('    Surface(')
    lines.append('        modifier = Modifier.fillMaxSize(),')
    lines.append(f'        color = {bg or "MaterialTheme.colorScheme.background"}')
    lines.append('    ) {')
    lines.append('        Column(')
    lines.append('            modifier = Modifier')
    lines.append('                .fillMaxSize()')
    lines.append('                .padding(horizontal = 24.dp, vertical = 24.dp),')
    lines.append('            verticalArrangement = Arrangement.spacedBy(16.dp)')
    lines.append('        ) {')

    if title:
        lines.append(f'            Text("{title["text"].replace(chr(34), r"\"")}", fontSize = {max(int(title["fontSize"]), 20)}.sp, fontWeight = FontWeight.Bold)')

    hero = [r for r in rects if r['width'] > width * 0.6 and 80 <= r['height'] <= 220 and r is not bg_rect]
    if hero:
        r = sorted(hero, key=lambda x: (x['y'], -x['width']))[0]
        shape = max(int(r['rx']), 0)
        color = color_to_compose(r['fill']) or 'MaterialTheme.colorScheme.surfaceVariant'
        lines.append('            Card(')
        lines.append(f'                shape = RoundedCornerShape({shape if shape else 16}.dp),')
        lines.append(f'                colors = CardDefaults.cardColors(containerColor = {color}),')
        lines.append('                modifier = Modifier.fillMaxWidth()')
        lines.append('            ) {')
        lines.append(f'                Box(modifier = Modifier.fillMaxWidth().height({max(int(r["height"]), 120)}.dp)) {{')
        lines.append('                    Text(')
        lines.append('                        text = "Hero section",')
        lines.append('                        modifier = Modifier.align(Alignment.Center),')
        lines.append('                        style = MaterialTheme.typography.titleMedium')
        lines.append('                    )')
        lines.append('                }')
        lines.append('            }')

    if rep:
        rw, rh = rep
        lines.append('            val itemsData = listOf("Item 1", "Item 2", "Item 3")')
        lines.append('            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {')
        lines.append('                items(itemsData) { label ->')
        lines.append(f'                    GeneratedListRow(label = label, height = {max(int(rh), 56)})')
        lines.append('                }')
        lines.append('            }')
    else:
        secondary_texts = [t for t in texts if not title or t is not title]
        for t in secondary_texts[:3]:
            lines.append(f'            Text("{t["text"].replace(chr(34), r"\"")}", fontSize = {max(int(t["fontSize"]), 14)}.sp)')

    lines.append('        }')
    lines.append('    }')
    lines.append('}')
    lines.append('')
    lines.append('@Composable')
    lines.append('private fun GeneratedListRow(label: String, height: Int) {')
    lines.append('    Card(')
    lines.append('        modifier = Modifier.fillMaxWidth(),')
    lines.append('        shape = RoundedCornerShape(12.dp),')
    lines.append('        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)')
    lines.append('    ) {')
    lines.append('        Row(')
    lines.append('            modifier = Modifier')
    lines.append('                .fillMaxWidth()')
    lines.append('                .height(height.dp)')
    lines.append('                .padding(horizontal = 16.dp),')
    lines.append('            verticalAlignment = Alignment.CenterVertically')
    lines.append('        ) {')
    lines.append('            Text(text = label, style = MaterialTheme.typography.bodyLarge)')
    lines.append('            Spacer(modifier = Modifier.weight(1f))')
    lines.append('            Text(text = ">", style = MaterialTheme.typography.bodyLarge)')
    lines.append('        }')
    lines.append('    }')
    lines.append('}')
    return '\n'.join(lines) + '\n'


def main():
    ap = argparse.ArgumentParser(description='Generate starter Jetpack Compose screen code from a simple full-screen SVG export.')
    ap.add_argument('svg', help='Path to SVG file')
    ap.add_argument('--name', help='Base screen name, default derived from filename')
    ap.add_argument('--out', help='Output .kt path')
    args = ap.parse_args()

    svg_path = Path(args.svg)
    root = ET.parse(svg_path).getroot()
    width, height = parse_viewbox(root)
    rects, texts = collect_rects_and_text(root)
    screen_name = pascal(args.name or svg_path.stem)
    code = generate(screen_name, width, height, rects, texts)

    header = (
        '// Generated starter code from svg-screen-to-compose\n'
        '// Best for simple exported screen SVGs. Review spacing, semantics, and assets manually.\n\n'
    )
    code = header + code
    if args.out:
        Path(args.out).write_text(code, encoding='utf-8')
    else:
        print(code, end='')


if __name__ == '__main__':
    main()
