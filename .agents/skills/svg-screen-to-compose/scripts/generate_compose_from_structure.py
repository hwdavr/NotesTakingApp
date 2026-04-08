#!/usr/bin/env python3
import argparse
import json
import re
from pathlib import Path


def pascal(name: str) -> str:
    parts = [p for p in re.split(r'[^a-zA-Z0-9]+', name) if p]
    return ''.join(p[:1].upper() + p[1:] for p in parts) or 'GeneratedScreen'


def kotlin_string(value: str) -> str:
    return value.replace('\\', '\\\\').replace('"', '\\"')


def to_color(value):
    if not value:
        return None
    value = str(value).strip()
    if re.fullmatch(r'#[0-9a-fA-F]{6}', value):
        return f'Color(0xFF{value[1:].upper()})'
    if re.fullmatch(r'#[0-9a-fA-F]{8}', value):
        return f'Color(0x{value[1:].upper()})'
    named = {
        'white': 'Color.White',
        'black': 'Color.Black',
        'red': 'Color.Red',
        'blue': 'Color.Blue',
        'green': 'Color.Green',
    }
    return named.get(value.lower())


def main():
    ap = argparse.ArgumentParser(description='Generate Jetpack Compose starter code from screen-structure JSON.')
    ap.add_argument('json_file', help='Path to screen-structure JSON file')
    ap.add_argument('--name', help='Base screen name; defaults to JSON filename')
    ap.add_argument('--out', help='Output Kotlin file path')
    args = ap.parse_args()

    path = Path(args.json_file)
    data = json.loads(path.read_text(encoding='utf-8'))

    screen_name = pascal(args.name or path.stem.replace('-structure', '').replace('_structure', ''))
    components = data.get('components', [])
    regions = data.get('regions', [])

    background = next((c for c in components if c.get('inferredRole') == 'background'), None)
    title = next((c for c in components if c.get('inferredRole') == 'screen-title'), None)
    hero = next((c for c in components if c.get('inferredRole') == 'hero-card'), None)
    list_items = [c for c in components if c.get('type') == 'list-row']
    grouped = {}
    for item in list_items:
        grouped.setdefault(item.get('repeatGroupId') or item.get('id'), []).append(item)

    lines = []
    lines += [
        '// Generated from screen-structure JSON',
        '// Review semantics, typography, interactions, and asset fallbacks manually.',
        '',
        'import androidx.compose.foundation.layout.Arrangement',
        'import androidx.compose.foundation.layout.Box',
        'import androidx.compose.foundation.layout.Column',
        'import androidx.compose.foundation.layout.Row',
        'import androidx.compose.foundation.layout.Spacer',
        'import androidx.compose.foundation.layout.fillMaxSize',
        'import androidx.compose.foundation.layout.fillMaxWidth',
        'import androidx.compose.foundation.layout.height',
        'import androidx.compose.foundation.layout.padding',
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
        '@Composable',
        f'fun {screen_name}Screen() {{',
    ]

    bg_color = to_color(background.get('style', {}).get('fill')) if background else None
    lines.append('    Surface(')
    lines.append('        modifier = Modifier.fillMaxSize(),')
    lines.append(f'        color = {bg_color or "MaterialTheme.colorScheme.background"}')
    lines.append('    ) {')
    lines.append('        Column(')
    lines.append('            modifier = Modifier')
    lines.append('                .fillMaxSize()')
    lines.append('                .padding(horizontal = 24.dp, vertical = 24.dp),')
    lines.append('            verticalArrangement = Arrangement.spacedBy(16.dp)')
    lines.append('        ) {')

    if title and title.get('text', {}).get('value'):
        text = kotlin_string(title['text']['value'])
        font_size = int(round(title.get('text', {}).get('fontSize') or 24))
        lines.append(f'            Text("{text}", fontSize = {max(font_size, 20)}.sp, fontWeight = FontWeight.Bold)')

    if hero:
        hero_color = to_color(hero.get('style', {}).get('fill')) or 'MaterialTheme.colorScheme.surfaceVariant'
        hero_radius = int(round(hero.get('style', {}).get('cornerRadius') or 16))
        hero_height = int(round(hero.get('bounds', {}).get('height') or 140))
        lines.append('            Card(')
        lines.append(f'                shape = RoundedCornerShape({max(hero_radius, 0)}.dp),')
        lines.append(f'                colors = CardDefaults.cardColors(containerColor = {hero_color}),')
        lines.append('                modifier = Modifier.fillMaxWidth()')
        lines.append('            ) {')
        lines.append(f'                Box(modifier = Modifier.fillMaxWidth().height({max(hero_height, 80)}.dp)) {{')
        lines.append('                    Text(')
        lines.append('                        text = "Hero section",')
        lines.append('                        modifier = Modifier.align(Alignment.Center),')
        lines.append('                        style = MaterialTheme.typography.titleMedium')
        lines.append('                    )')
        lines.append('                }')
        lines.append('            }')

    if grouped:
        first_group = next(iter(grouped.values()))
        lines.append('            val itemsData = List(' + str(len(first_group)) + ') { index -> "Item ${index + 1}" }')
        lines.append('            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {')
        lines.append('                items(itemsData) { label ->')
        prototype = first_group[0]
        row_height = int(round(prototype.get('bounds', {}).get('height') or 72))
        row_radius = int(round(prototype.get('style', {}).get('cornerRadius') or 12))
        row_color = to_color(prototype.get('style', {}).get('fill')) or 'MaterialTheme.colorScheme.surfaceVariant'
        lines.append(f'                    GeneratedListRow(label = label, height = {max(row_height, 56)}, cornerRadius = {max(row_radius, 0)}, containerColor = {row_color})')
        lines.append('                }')
        lines.append('            }')

    # Emit a note block as comments if there are important notes/fallbacks.
    notes = data.get('notes') or []
    fallback_components = [c for c in components if c.get('fallbackType') and c.get('fallbackType') != 'none']
    if not grouped and not hero:
        lines.append('            Text("Generated from structure JSON. Add semantic sections here.")')
    if fallback_components or notes:
        lines.append('')
        lines.append('            // Fallback / analysis notes:')
        for note in notes[:8]:
            lines.append(f'            // - {kotlin_string(str(note))}')
        for comp in fallback_components[:8]:
            lines.append(f'            // - {comp.get("id")}: fallbackType={comp.get("fallbackType")}')

    lines.append('        }')
    lines.append('    }')
    lines.append('}')
    lines.append('')
    lines.append('@Composable')
    lines.append('private fun GeneratedListRow(label: String, height: Int, cornerRadius: Int, containerColor: Color) {')
    lines.append('    Card(')
    lines.append('        modifier = Modifier.fillMaxWidth(),')
    lines.append('        shape = RoundedCornerShape(cornerRadius.dp),')
    lines.append('        colors = CardDefaults.cardColors(containerColor = containerColor)')
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

    output = '\n'.join(lines) + '\n'
    if args.out:
        Path(args.out).write_text(output, encoding='utf-8')
    else:
        print(output, end='')


if __name__ == '__main__':
    main()
