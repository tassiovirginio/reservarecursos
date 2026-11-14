# Testes visuais (regressão de interface)

Compara **pixel a pixel** a interface da stack antiga (Wicket 6 / Spring 4 /
Hibernate 4 / Jetty 8 / Java 11, commit `4788022`) com a stack atual
(Wicket 11 / Spring 7 / Hibernate 7 / Jetty 12 / Java 21). Foi criado para
provar que a correção do `Content-Security-Policy` do Wicket 11 devolveu o CSS
sem alterar o visual.

## Rodar

```bash
mise run visual-test
```

Saída: `target/visual-tests/index.html` (galeria *baseline × atual × diff*),
`target/visual-tests/results.json` e `target/visual-tests/shots/*.png`.
`target/` já está no `.gitignore`.

**Exit code**: `0` aprovado, `1` regressão visual (ou asserção de CSS diferente,
HTTP com falha, novos erros de console/página), `2` pré-requisito ausente.
Serve direto como gate de CI.

## Variáveis

| Variável | Padrão | Uso |
|---|---|---|
| `VISUAL_BASE_REF` | `4788022` | commit/git-ref do baseline |
| `VISUAL_CURRENT_REF` | `HEAD` | `HEAD`, outro ref, ou `WORKTREE` para testar alterações não commitadas |
| `VISUAL_CONTROL` | `0` | `1` inclui o controle negativo (build com CSP bloqueante) |
| `VISUAL_CONTROL_REF` | `278e804` | ref do controle negativo |
| `VISUAL_PORT` | `18081` | porta dos servidores temporários (não conflita com o `mise run start` na 8081) |
| `VISUAL_MAX_DIFF_PCT` | `1.0` | limite de diferença de pixels por tela; use `0` para exigir identidade |
| `VISUAL_CHROME` | auto | caminho do Chromium, se a detecção automática falhar |

Exemplos:

```bash
VISUAL_CURRENT_REF=WORKTREE mise run visual-test   # testa o working tree
VISUAL_CONTROL=1 mise run visual-test              # inclui prova da regressão de CSP
VISUAL_MAX_DIFF_PCT=0 mise run visual-test        # exige pixel-idêntico
```

## Pré-requisitos

- `node`/`npm` (o script instala `playwright-core` em `tests/visual/node_modules` se faltar).
- Python 3 com **Pillow** (`python3 -m pip install Pillow`).
- Chromium do Playwright: `npx playwright install chromium` (ou `VISUAL_CHROME=...`).
- `mise`, `git`, e `ss` (iproute2) para checar a porta.
- Nenhum servidor na porta escolhida.

## O que é verificado

1. **12 telas**: home anônima (desktop 1440×900 e mobile 390×844) + 10 páginas
   após login automatizado `admin/admin`.
2. **Asserções de CSS** por página: nenhuma folha com `cssRules` inacessível
   (é isso que detecta CSP bloqueando `<link>`), as 4 folhas base presentes com
   regras (`bootstrap.min.css`, `bootstrap-theme.min.css`,
   `daterangepicker.css`, `BasePage.css`), `body.padding-top`, cor e altura da
   navbar. O **número total** de folhas é informativo, não é gate: em páginas
   com modal o Kendo antigo contribuía um `modal.min.css` extra (5 × 4 folhas)
   sem nenhuma diferença de pixels.
3. **Requisições HTTP ≥ 400**, erros de console e erros de página.
4. **Diff de pixels** com Pillow (limiar 24 por canal), com imagem de diff em vermelho.

Cada stack sobe isolada via `git archive` em `target/visual-tests/`, com banco
SQLite zerado e dado de teste equalizado (`grupo_id`), para que as mesmas telas
renderizem nos dois lados. O repositório de trabalho não é alterado.

## Controle negativo

`VISUAL_CONTROL=1` sobe também o commit `278e804` (antes do fix do CSP) e
captura só a home. Nele as 4 folhas aparecem como `bloqueado`, o
`padding-top` é `0px`, a navbar fica transparente e há 14 erros de CSP —
resultando em ~84% de diferença. Isso garante que a bateria **detecta** a
regressão em vez de aprovar tudo cegamente.

## Histórico

Primeira execução (14/11/2026): 12 telas, **0,000%** de diferença, PNGs com o
mesmo SHA-256 entre baseline e atual. O único erro de JS
(`ReferenceError: require is not defined`, do `npm.js` do Bootstrap) aparece
nas duas stacks e é pré-existente.
