# Testes automatizados — suíte visual + JUnit

Duas camadas complementares:

| Comando | O que faz | Exit |
|---|---|---|
| `mise run test` | 6 testes JUnit de integração (DAO, regras de negócio, converters) | 0/1 |
| `mise run visual-test` | 15 capturas (13/13 rotas) + 4 interações, comparadas com o baseline Wicket 6 | 0/1/2 |

Ambas são próprias para CI. Saídas ficam em `target/` (já ignorado).

## 1. Cobertura de telas: 13/13

O Wicket monta 13 rotas (`mountPage`). A suíte captura **15 imagens**:

- 11 rotas por GET direto (incluindo a home);
- home também em **390×844** (mobile), agora com asserções de CSS;
- as 3 páginas de reserva (`/reservashoje/`, `/reservasfixas/`,
  `/reservassemana/`) **por navegação real**: o Playwright clica em
  `linkHoje`/`linkVer`/`linkSemana` no `MenuPage`, porque essas páginas exigem
  um `Ambiente` no construtor e não abrem por URL direta.

Cada captura registra: HTTP status, `document.styleSheets` (incluindo
`cssRules` — detecta CSP bloqueando `<link>`), `body.padding-top`, cor/altura da
navbar, requisições ≥ 400 e erros de console/página.

## 2. Testes de interação (4)

Executados nas duas stacks; os **resultados são comparados por valor**, não por
pixel (o `ModalWindow` do Wicket 6 usa iframe e o `ModalDialog` do Wicket 11 usa
`div`, então o visual difere por design):

| Interação | O que valida |
|---|---|
| `loginInvalido` | feedback "Login Inválido" e permanência no formulário |
| `cadastroAmbiente` | submissão do formulário, novo registro visível na lista |
| `exclusaoComConfirmacao` | diálogo `confirm()` aceito e linha removida |
| `modal` | clique AJAX abre o conteúdo do modal (DOM ou iframe) |

`db_snapshot.py` anexa a contagem de linhas do SQLite ao `meta.json` **depois**
das interações; o gate exige bancos idênticos nas duas stacks.

## 3. Gates endurecidos

- **HTTP 200 explícito** em toda tela (antes só se comparava old vs new).
- Asserções de CSS em **todas** as telas, **inclusive mobile** (antes ignorado).
- Nenhuma folha bloqueada por CSP + as 4 folhas base presentes com regras.
- O número **total** de folhas é informativo: o Kendo antigo contribuía um
  `modal.min.css` extra em páginas com modal (5 × 4), sem diferença de pixels.
- Diff de pixels (Pillow, limiar 24 por canal) com `VISUAL_MAX_DIFF_PCT`.
- Paridade das interações e da persistência; zero erros de console na stack atual.

**Exceção conhecida**: o `ModalWindow` do baseline carrega uma `Page` num iframe
que referencia `css/dashboard.css`, arquivo que nunca existiu (404
pré-existente). A stack atual usa `Panel` e não faz essa requisição — é uma
correção, não regressão, e está whitelistada em `compare.py`.

## 4. Bugs reais encontrados por esta suíte

1. `Filtros.eq("id", null)` gerava `id <> NULL` (nunca casa), o que **anulava a
   regra de interseção de períodos**. Corrigido para `cb.isNull`.
2. Os 3 HTMLs de modal ainda eram páginas completas (`<html><head><body>`),
   mas as classes viraram `Panel` no Wicket 11 → `MarkupNotFoundException` ao
   abrir. Convertidos para `<wicket:panel>`.
3. O `ModalDialog` do Wicket 11 não traz CSS próprio (o estilo vinha do Kendo);
   o conteúdo aparecia inline. Adicionado CSS em `css/BasePage.css` e removido
   o `navbar-fixed-top` de dentro dos modais.

## 5. Rodar / variáveis

```bash
mise run visual-test                                  # baseline x HEAD
VISUAL_CURRENT_REF=WORKTREE mise run visual-test     # testa alterações não commitadas
VISUAL_CONTROL=1 mise run visual-test                # inclui controle negativo (CSP)
VISUAL_MAX_DIFF_PCT=0 mise run visual-test           # exige pixel-idêntico
mise run test                                        # suíte JUnit
```

| Variável | Padrão | Uso |
|---|---|---|
| `VISUAL_BASE_REF` | `4788022` | ref do baseline |
| `VISUAL_CURRENT_REF` | `HEAD` | `HEAD`, outro ref ou `WORKTREE` |
| `VISUAL_CONTROL` / `VISUAL_CONTROL_REF` | `0` / `278e804` | controle negativo com CSP bloqueante |
| `VISUAL_PORT` | `18081` | porta dos servidores temporários |
| `VISUAL_MAX_DIFF_PCT` | `1.0` | limite de diferença por tela |
| `VISUAL_CHROME` | auto | caminho do Chromium |

O `run.sh` detecta automaticamente se o checkout é **Spring Boot**
(`spring-boot-maven-plugin`) e usa `mvn spring-boot:run --server.port=…`;
checkouts WAR antigos continuam usando `mvn jetty:run`.

**Pré-requisitos**: node/npm (instala `playwright-core` em `tests/visual/node_modules`),
Python 3 + Pillow, Chromium do Playwright (`npx playwright install chromium`),
`mise`, `git`, `ss`. Nenhum servidor na porta escolhida.

## 6. Histórico

Primeira execução: 12 telas, 0,000%. Após a expansão: **15 capturas, 13/13
rotas, 4 interações, 0,000% de diferença, exit 0**; controle negativo 84,259%.
O único erro de JS (`ReferenceError: require is not defined`, do `npm.js` do
Bootstrap) aparece nas duas stacks e é pré-existente.
