#!/usr/bin/env python3
"""Compara screenshots baseline (old) x atual (new) e falha em regressão.

Uso: python3 compare.py <outDir> [maxDiffPct]
Saída: <outDir>/results.json, <outDir>/shots/diff__*.png
Exit: 0 = aprovado, 1 = regressão, 2 = erro de execução.
"""
import json
import os
import sys
from PIL import Image, ImageChops

OUT = sys.argv[1] if len(sys.argv) > 1 else 'target/visual-tests'
D = os.path.join(OUT, 'shots')
THRESH = 24
MAX_PCT = float(sys.argv[2]) if len(sys.argv) > 2 else 1.0


def maxdiff(a, b):
    r, g, bl = ImageChops.difference(a, b).split()
    return ImageChops.lighter(ImageChops.lighter(r, g), bl)


def compare(name, fa, fb):
    a = Image.open(os.path.join(D, fa)).convert('RGB')
    b = Image.open(os.path.join(D, fb)).convert('RGB')
    if a.size != b.size:
        b = b.resize(a.size)
    md = maxdiff(a, b)
    hist = md.histogram()
    total = a.size[0] * a.size[1]
    changed = sum(hist[THRESH + 1:])
    pct = 100.0 * changed / total
    mean = sum(i * c for i, c in enumerate(hist)) / total
    base = Image.blend(a, Image.new('RGB', a.size, (255, 255, 255)), 0.72)
    mask = md.point(lambda v: 255 if v > THRESH else 0)
    Image.composite(Image.new('RGB', a.size, (230, 30, 30)), base, mask).save(
        os.path.join(D, f'diff__{name}.png'))
    return dict(name=name, old=fa, new=fb, size=a.size, changed=changed,
                total=total, pct=round(pct, 3), mean=round(mean, 3))


def load(label):
    p = os.path.join(D, f'{label}__meta.json')
    if not os.path.exists(p):
        sys.exit(f'ERRO: metadados não encontrados: {p}')
    return json.load(open(p))


old, new = load('old'), load('new')
control_meta = load('control') if os.path.exists(os.path.join(D, 'control__meta.json')) else None

keys = [k for k in old['pages'] if k in new['pages']
        and old['pages'][k].get('file') and new['pages'][k].get('file')]
if len(keys) < 12:
    sys.exit(f'ERRO: esperadas 12 telas, encontradas {len(keys)}')

rows = [compare(k, f'old__{k}.png', f'new__{k}.png') for k in keys]
control = None
if control_meta:
    control = compare('CONTROLE-home', 'old__01-home.png', 'control__01-home.png')

CORE = ('bootstrap.min.css', 'bootstrap-theme.min.css', 'daterangepicker.css', 'BasePage.css')


def sheet_stats(probe):
    sheets = probe.get('sheets') or []
    blocked = sum(1 for s in sheets if s.get('rules') == 'bloqueado')
    core = sum(1 for s in sheets
               if any((s.get('href') or '').endswith(c) for c in CORE)
               and isinstance(s.get('rules'), int) and s['rules'] > 0)
    return blocked, core


assertions = []
for k in keys:
    po = old['pages'][k].get('probe') or {}
    pn = new['pages'][k].get('probe') or {}
    bo, co = sheet_stats(po)
    bn, cn = sheet_stats(pn)
    assertions.append(dict(
        page=k,
        http_old=old['pages'][k].get('http'), http_new=new['pages'][k].get('http'),
        sheets_old=po.get('styleSheets'), sheets_new=pn.get('styleSheets'),
        blocked_old=bo, blocked_new=bn, core_old=co, core_new=cn,
        pad_old=po.get('bodyPaddingTop'), pad_new=pn.get('bodyPaddingTop'),
        nav_old=po.get('navbarBg'), nav_new=pn.get('navbarBg'),
        navH_old=po.get('navbarHeight'), navH_new=pn.get('navbarHeight'),
    ))

summary = dict(
    pages=len(rows),
    avg_pct=round(sum(r['pct'] for r in rows) / len(rows), 3),
    max_pct=max(r['pct'] for r in rows),
    control_pct=control['pct'] if control else None,
    old_failed=len(old['failed']), new_failed=len(new['failed']),
    old_console=len(old['consoleErrors']), new_console=len(new['consoleErrors']),
    old_pageerrors=old['pageErrors'], new_pageerrors=new['pageErrors'],
    prefix_console_count=len(control_meta['consoleErrors']) if control_meta else 0,
    prefix_probe=(control_meta['pages']['01-home']['probe'] if control_meta else None),
)
json.dump(dict(rows=rows, control=control, assertions=assertions, summary=summary),
          open(os.path.join(OUT, 'results.json'), 'w'), indent=2)

# ---- gates ----
failures = []
for r in rows:
    if r['pct'] > MAX_PCT:
        failures.append(f"{r['name']}: {r['pct']:.3f}% de diferença (limite {MAX_PCT}%)")
for a in assertions:
    if not a['page'].endswith('-mobile'):
        # O que importa é: nenhuma folha bloqueada (CSP), as 4 folhas base
        # carregadas com regras e os estilos computados iguais. O número total
        # de folhas pode variar (o Kendo antigo contribuía um CSS extra em
        # páginas com modal) sem nenhuma diferença visual.
        if (a['blocked_old'] or a['blocked_new'] or a['core_old'] != 4 or a['core_new'] != 4
                or a['pad_old'] != a['pad_new'] or a['nav_old'] != a['nav_new']
                or a['http_old'] != a['http_new']):
            failures.append(f"{a['page']}: asserção de CSS/HTTP divergente "
                            f"(bloqueadas {a['blocked_old']}/{a['blocked_new']}, "
                            f"base {a['core_old']}/{a['core_new']}, folhas {a['sheets_old']}/{a['sheets_new']}, "
                            f"pad {a['pad_old']}/{a['pad_new']}, nav {a['nav_old']}/{a['nav_new']}, "
                            f"http {a['http_old']}/{a['http_new']})")
if old['failed'] or new['failed']:
    failures.append(f"requisições com falha: old={old['failed']} new={new['failed']}")
if len(new['consoleErrors']) > len(old['consoleErrors']):
    failures.append(f"novos erros de console: {new['consoleErrors']}")
if set(new['pageErrors']) - set(old['pageErrors']):
    failures.append(f"novos erros de página: {set(new['pageErrors']) - set(old['pageErrors'])}")

print('=== DIFF baseline x atual ===')
for r in rows:
    print(f"  {r['name']:24} {r['pct']:7.3f}%  mean={r['mean']:6.2f}")
print(f"  MÉDIA {summary['avg_pct']:.3f}%  MÁX {summary['max_pct']:.3f}%  LIMITE {MAX_PCT}%")
if control:
    print(f"=== CONTROLE NEGATIVO === {control['pct']:.3f}% (esperado: alto)")
print(f"  HTTP falhos old/new: {summary['old_failed']}/{summary['new_failed']} | "
      f"console old/new: {summary['old_console']}/{summary['new_console']}")

if failures:
    print('\n❌ REPROVADO:')
    for f in failures:
        print('   -', f)
    sys.exit(1)
print('\n✅ APROVADO — nenhuma regressão visual detectada.')
sys.exit(0)
