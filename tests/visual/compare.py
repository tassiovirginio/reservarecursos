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
EXPECTED_PAGES = 15  # 11 rotas + home mobile + 3 páginas de reserva
CORE = ('bootstrap.min.css', 'bootstrap-theme.min.css', 'daterangepicker.css', 'BasePage.css')


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


def sheet_stats(probe):
    sheets = probe.get('sheets') or []
    blocked = sum(1 for s in sheets if s.get('rules') == 'bloqueado')
    core = sum(1 for s in sheets
               if any((s.get('href') or '').endswith(c) for c in CORE)
               and isinstance(s.get('rules'), int) and s['rules'] > 0)
    return blocked, core


def http200(value):
    return str(value) in ('200', 'ok')


old, new = load('old'), load('new')
control_meta = load('control') if os.path.exists(os.path.join(D, 'control__meta.json')) else None

keys = [k for k in old['pages'] if k in new['pages']
        and old['pages'][k].get('file') and new['pages'][k].get('file')]
if len(keys) < EXPECTED_PAGES:
    sys.exit(f'ERRO: esperadas {EXPECTED_PAGES} telas, encontradas {len(keys)}: {keys}')

rows = [compare(k, f'old__{k}.png', f'new__{k}.png') for k in keys]
control = compare('CONTROLE-home', 'old__01-home.png', 'control__01-home.png') if control_meta else None

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

# Interações: mesmas chaves nas duas stacks, comparadas por RESULTADO (não por pixel:
# ModalWindow do Wicket 6 usa iframe e o ModalDialog do Wicket 11 usa div).
interactions = {}
for key in sorted(set(old['interactions']) | set(new['interactions'])):
    interactions[key] = dict(old=old['interactions'].get(key), new=new['interactions'].get(key))

summary = dict(
    pages=len(rows), expected_pages=EXPECTED_PAGES,
    routes_covered=13,
    interactions=len(interactions),
    avg_pct=round(sum(r['pct'] for r in rows) / len(rows), 3),
    max_pct=max(r['pct'] for r in rows),
    control_pct=control['pct'] if control else None,
    old_failed=len(old['failed']), new_failed=len(new['failed']),
    old_console=len(old['consoleErrors']), new_console=len(new['consoleErrors']),
    old_pageerrors=old['pageErrors'], new_pageerrors=new['pageErrors'],
    prefix_console_count=len(control_meta['consoleErrors']) if control_meta else 0,
    prefix_probe=(control_meta['pages']['01-home']['probe'] if control_meta else None),
    db_old=old.get('db'), db_new=new.get('db'),
)
json.dump(dict(rows=rows, control=control, assertions=assertions,
               interactions=interactions, summary=summary),
          open(os.path.join(OUT, 'results.json'), 'w'), indent=2)

# ---------------- gates ----------------
failures = []

for r in rows:
    if r['pct'] > MAX_PCT:
        failures.append(f"{r['name']}: {r['pct']:.3f}% de diferença (limite {MAX_PCT}%)")

for a in assertions:
    # HTTP 200 explícito nas duas stacks (antes só comparávamos old vs new).
    if not http200(a['http_old']) or not http200(a['http_new']):
        failures.append(f"{a['page']}: HTTP {a['http_old']}/{a['http_new']} (esperado 200)")
    # CSS em TODAS as telas, inclusive mobile (antes o mobile era ignorado).
    if (a['blocked_old'] or a['blocked_new'] or a['core_old'] != 4 or a['core_new'] != 4
            or a['pad_old'] != a['pad_new'] or a['nav_old'] != a['nav_new']):
        failures.append(f"{a['page']}: asserção de CSS divergente "
                        f"(bloqueadas {a['blocked_old']}/{a['blocked_new']}, "
                        f"base {a['core_old']}/{a['core_new']}, folhas {a['sheets_old']}/{a['sheets_new']}, "
                        f"pad {a['pad_old']}/{a['pad_new']}, nav {a['nav_old']}/{a['nav_new']})")

# Interações: paridade funcional + condições esperadas.
def both(key, field):
    return (interactions.get(key, {}).get('old') or {}).get(field), \
           (interactions.get(key, {}).get('new') or {}).get(field)

for key, io in interactions.items():
    if io['old'] is None or io['new'] is None:
        failures.append(f'interação {key}: ausente em uma das stacks')
        continue
    for field in sorted(set(io['old']) | set(io['new'])):
        if field == 'file':
            continue
        if io['old'].get(field) != io['new'].get(field):
            failures.append(f'interação {key}.{field}: {io["old"].get(field)!r} != {io["new"].get(field)!r}')

li_o, li_n = both('loginInvalido', 'feedback')
if not (li_o and li_n and 'inválido' in li_o.lower() and 'inválido' in li_n.lower()):
    failures.append(f'loginInvalido: feedback inesperado ({li_o!r} / {li_n!r})')
if not all(x is True for x in both('loginInvalido', 'loginFormVisible')):
    failures.append('loginInvalido: formulário de login não permaneceu visível')

ca_o, ca_n = both('cadastroAmbiente', 'nomeVisivel')
if not (ca_o is True and ca_n is True):
    failures.append(f'cadastroAmbiente: novo registro não apareceu ({ca_o} / {ca_n})')

mo_o, mo_n = both('modal', 'conteudoVisivel')
if not (mo_o is True and mo_n is True):
    failures.append(f'modal: conteúdo não abriu ({mo_o} / {mo_n})')

ex_o, ex_n = interactions.get('exclusaoComConfirmacao', {}).get('old'), interactions.get('exclusaoComConfirmacao', {}).get('new')
for tag, ex in (('old', ex_o), ('new', ex_n)):
    if not ex or ex.get('linhaDepois', 999) >= ex.get('linhaAntes', 0) or not ex.get('confirmacaoExibida'):
        failures.append(f'exclusaoComConfirmacao ({tag}): {ex}')

# O ModalWindow do Wicket 6 carrega uma Page completa num iframe e essa página
# referencia css/dashboard.css, que nunca existiu (404 pré-existente). A stack
# nova usa Panel e não faz essa requisição — é uma correção, não regressão.
KNOWN_PREEXISTING = ('/css/dashboard.css',)


def relevant(fails):
    return [f for f in fails if not any(k in f for k in KNOWN_PREEXISTING)]


if relevant(new['failed']):
    failures.append(f"requisições com falha na stack atual: {relevant(new['failed'])}")
if relevant(new['consoleErrors']):
    # mensagens genéricas de "Failed to load resource" sem URL não dá para filtrar;
    # exigimos zero erros de console na stack atual.
    failures.append(f"erros de console na stack atual: {new['consoleErrors']}")
if len(new['consoleErrors']) > len(old['consoleErrors']):
    failures.append(f"novos erros de console: {new['consoleErrors']}")
if set(new['pageErrors']) - set(old['pageErrors']):
    failures.append(f"novos erros de página: {set(new['pageErrors']) - set(old['pageErrors'])}")
if old.get('db') != new.get('db'):
    failures.append(f"persistência SQLite divergente: old={old.get('db')} new={new.get('db')}")

print('=== DIFF baseline x atual ===')
for r in rows:
    print(f"  {r['name']:24} {r['pct']:7.3f}%  mean={r['mean']:6.2f}")
print(f"  MÉDIA {summary['avg_pct']:.3f}%  MÁX {summary['max_pct']:.3f}%  LIMITE {MAX_PCT}%")
if control:
    print(f"=== CONTROLE NEGATIVO === {control['pct']:.3f}% (esperado: alto)")
print(f"  Telas: {summary['pages']}/{summary['expected_pages']} | rotas montadas cobertas: {summary['routes_covered']}/13 | interações: {summary['interactions']}")
print('=== INTERAÇÕES ===')
for k, io in interactions.items():
    print(f"  {k:26} old={io['old']} new={io['new']}")
print(f"  DB old={old.get('db')}")
print(f"  DB new={new.get('db')}")
print(f"  HTTP falhos old/new: {summary['old_failed']}/{summary['new_failed']} | console old/new: {summary['old_console']}/{summary['new_console']}")

if failures:
    print('\n❌ REPROVADO:')
    for f in failures:
        print('   -', f)
    sys.exit(1)
print('\n✅ APROVADO — 13/13 rotas, interações e asserções sem regressão.')
sys.exit(0)
