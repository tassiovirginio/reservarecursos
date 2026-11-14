#!/usr/bin/env python3
"""Gera o relatório HTML a partir de results.json. Uso: python3 report.py <outDir>"""
import html
import json
import os
import sys

OUT = sys.argv[1] if len(sys.argv) > 1 else 'target/visual-tests'
R = json.load(open(os.path.join(OUT, 'results.json')))
s, rows, ctl, ass = R['summary'], R['rows'], R['control'], R['assertions']
verdict = 'APROVADO' if s['max_pct'] <= 1 else 'REVISAR'
color = '#1a7f37' if verdict == 'APROVADO' else '#b42318'


def img(src, w=430):
    return (f'<a href="{src}" target="_blank"><img src="{src}" '
            f'style="width:{w}px;max-width:100%;border:1px solid #ddd;border-radius:4px"></a>')


def esc(x):
    return html.escape(str(x))


p = []
p.append(f'''<!doctype html><html lang="pt-BR"><head><meta charset="utf-8">
<title>Bateria de testes visuais — Reserva de Recursos</title>
<style>
body {{ font-family: system-ui,-apple-system,Segoe UI,sans-serif; margin:0; background:#f6f8fa; color:#1f2328 }}
header {{ background:#0d1117; color:#fff; padding:28px 36px }}
h1 {{ margin:0 0 6px; font-size:26px }} .sub {{ color:#9da7b3; font-size:14px }}
.badge {{ display:inline-block; background:{color}; color:#fff; padding:5px 14px; border-radius:20px; font-weight:700; margin-top:12px }}
main {{ max-width:1400px; margin:0 auto; padding:28px 24px 60px }}
.cards {{ display:grid; grid-template-columns:repeat(auto-fit,minmax(190px,1fr)); gap:14px; margin-bottom:28px }}
.card {{ background:#fff; border:1px solid #d8dee4; border-radius:8px; padding:16px }}
.card b {{ display:block; font-size:26px; margin-bottom:4px }} .card span {{ color:#57606a; font-size:13px }}
h2 {{ margin-top:36px; border-bottom:1px solid #d8dee4; padding-bottom:8px; font-size:20px }}
table {{ border-collapse:collapse; width:100%; background:#fff; font-size:13px }}
th,td {{ border:1px solid #d8dee4; padding:7px 9px; text-align:left }} th {{ background:#f0f3f6 }}
.ok {{ color:#1a7f37; font-weight:700 }} .bad {{ color:#b42318; font-weight:700 }}
.grid {{ display:grid; grid-template-columns:repeat(auto-fit,minmax(430px,1fr)); gap:18px }}
.tile {{ background:#fff; border:1px solid #d8dee4; border-radius:8px; padding:12px }}
.tile h3 {{ margin:0 0 8px; font-size:15px }} .tile .imgs {{ display:flex; flex-wrap:wrap; gap:6px }}
.tile .imgs a {{ flex:1 1 45% }} .tile .imgs a:last-child {{ flex:1 1 100% }}
code {{ background:#eaeef2; padding:2px 5px; border-radius:4px; font-size:12px }}
.note {{ background:#fff8c5; border:1px solid #d4a72c; border-radius:8px; padding:14px; font-size:14px }}
</style></head><body>
<header><h1>Bateria de testes visuais — Reserva de Recursos</h1>
<div class="sub">Baseline Wicket 6 / Jetty 8 × stack atual Wicket 11 / Jetty 12 · gerado por <code>mise run visual-test</code></div>
<div class="badge">{verdict} — diferença máxima {s['max_pct']:.3f}%</div></header><main>
<div class="cards">
<div class="card"><b>{s['pages']}</b><span>telas comparadas (desktop + mobile)</span></div>
<div class="card"><b>{s['avg_pct']:.3f}%</b><span>diferença média de pixels</span></div>
<div class="card"><b>{s['max_pct']:.3f}%</b><span>diferença máxima</span></div>
<div class="card"><b>{s['control_pct'] if s['control_pct'] is not None else 'n/a'}{'%' if s['control_pct'] is not None else ''}</b><span>controle negativo (CSP)</span></div>
<div class="card"><b>{s['old_failed']}/{s['new_failed']}</b><span>requisições HTTP com falha</span></div>
<div class="card"><b>{s['old_console']}/{s['new_console']}</b><span>erros de console (old/new)</span></div>
</div>''')

if ctl and s.get('prefix_probe'):
    pp = s['prefix_probe']
    p.append(f'''<div class="note"><b>Controle negativo:</b> uma build com CSP bloqueante (commit
<code>278e804</code>) foi capturada para provar que a bateria detecta a regressão:
folhas de estilo <code>bloqueado</code>, <code>padding-top: {esc(pp.get('bodyPaddingTop'))}</code>,
navbar <code>{esc(pp.get('navbarBg'))}</code>, {s['prefix_console_count']} erros de CSP e
<b>{ctl['pct']:.3f}%</b> de diferença. A versão atual passa.</div>
<div class="grid"><div class="tile"><h3>Home — baseline</h3>{img('shots/old__01-home.png', 620)}</div>
<div class="tile"><h3>Home — CSP bloqueante (sem estilo)</h3>{img('shots/control__01-home.png', 620)}</div></div>''')

p.append('<h2>Asserções de CSS (por página)</h2><table><tr><th>Página</th><th>HTTP old/new</th><th>Folhas (total)</th><th>Bloqueadas</th><th>Base (4 esperadas)</th><th>body padding-top</th><th>navbar background</th><th>altura navbar</th><th>Resultado</th></tr>')
for a in ass:
    same = (a['blocked_old'] == 0 and a['blocked_new'] == 0
            and a['core_old'] == 4 and a['core_new'] == 4
            and a['pad_old'] == a['pad_new'] and a['nav_old'] == a['nav_new']
            and a['http_old'] == a['http_new'])
    p.append(f"<tr><td>{esc(a['page'])}</td><td>{esc(a['http_old'])} / {esc(a['http_new'])}</td>"
             f"<td>{esc(a['sheets_old'])} / {esc(a['sheets_new'])}</td>"
             f"<td>{esc(a['blocked_old'])} / {esc(a['blocked_new'])}</td>"
             f"<td>{esc(a['core_old'])} / {esc(a['core_new'])}</td>"
             f"<td>{esc(a['pad_old'])} / {esc(a['pad_new'])}</td>"
             f"<td>{esc(a['nav_old'])} / {esc(a['nav_new'])}</td><td>{esc(a['navH_old'])} / {esc(a['navH_new'])}</td>"
             f"<td class='{'ok' if same else 'bad'}'>{'IGUAL' if same else 'DIFERENTE'}</td></tr>")
p.append('</table>')

p.append('<h2>Galeria: baseline × atual × diff</h2><div class="grid">')
for r in rows:
    p.append(f'''<div class="tile"><h3>{esc(r['name'])} — {r['pct']:.3f}% ({r['changed']}/{r['total']} px)</h3>
<div class="imgs">{img('shots/'+r['old'])}{img('shots/'+r['new'])}{img('shots/diff__'+r['name']+'.png')}</div>
<p style="font-size:12px;color:#57606a;margin:6px 0 0">baseline · atual · diff (vermelho = pixel alterado &gt; 24)</p></div>''')
p.append('</div>')

p.append('''<h2>Metodologia</h2><ul style="font-size:14px;line-height:1.7">
<li>Dois checkouts isolados via <code>git archive</code> em <code>target/visual-tests/</code>: baseline (<code>VISUAL_BASE_REF</code>, padrão <code>4788022</code>) e atual (<code>VISUAL_CURRENT_REF</code>, padrão <code>HEAD</code>; use <code>WORKTREE</code> para testar alterações não commitadas).</li>
<li>Cada um sobe com banco SQLite zerado e dado de teste equalizado (<code>grupo_id</code>), na porta <code>VISUAL_PORT</code> (padrão 18081), para não conflitar com o dev server.</li>
<li>Chromium headless via <code>playwright-core</code>: 1440×900 desktop e 390×844 mobile, <code>networkidle</code>, login automatizado <code>admin/admin</code>, 11 rotas.</li>
<li>Coleta: HTTP ≥ 400, erros de console/página, <code>document.styleSheets</code> (detecta CSP bloqueando CSS) e estilos computados.</li>
<li>Diff de pixels (Pillow, limiar 24 por canal) + <b>exit code não-zero</b> se qualquer tela divergir, alguma asserção de CSS mudar ou surgirem erros novos — pronto para CI.</li>
</ul>
<h2>Reproduzir</h2><pre style="background:#fff;border:1px solid #d8dee4;padding:14px;border-radius:8px;font-size:12px;overflow:auto"><code>
mise run visual-test                         # baseline x HEAD
VISUAL_CURRENT_REF=WORKTREE mise run visual-test   # testa o working tree
VISUAL_CONTROL=1 mise run visual-test        # inclui controle negativo (CSP)
VISUAL_MAX_DIFF_PCT=0 mise run visual-test  # exige pixel-idêntico
</code></pre>
</main></body></html>''')

open(os.path.join(OUT, 'index.html'), 'w').write('\n'.join(p))
print('Relatório:', os.path.join(OUT, 'index.html'))
