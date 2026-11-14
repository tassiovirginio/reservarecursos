#!/usr/bin/env bash
# Bateria de testes visuais: baseline (Wicket 6) x stack atual (Wicket 11).
# Repetível e própria para CI: exit 0 = aprovado, 1 = regressão, 2 = pré-requisito ausente.
set -euo pipefail

ROOT=$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)
HARNESS="$ROOT/tests/visual"
OUT="$ROOT/target/visual-tests"
SHOTS="$OUT/shots"

PORT=${VISUAL_PORT:-18081}
BASE_REF=${VISUAL_BASE_REF:-4788022}
CUR_REF=${VISUAL_CURRENT_REF:-HEAD}
CONTROL=${VISUAL_CONTROL:-0}
CONTROL_REF=${VISUAL_CONTROL_REF:-278e804}
MAX_PCT=${VISUAL_MAX_DIFF_PCT:-1.0}
BASE_URL="http://127.0.0.1:$PORT"
SRV_PID=""

log() { echo "[visual-test] $*"; }

stop_server() {
  local p=""
  p=$(ss -tlnp 2>/dev/null | grep ":$PORT " | grep -oP 'pid=\K[0-9]+' | head -1 || true)
  [ -n "$p" ] && kill "$p" 2>/dev/null || true
  [ -n "$SRV_PID" ] && kill "$SRV_PID" 2>/dev/null || true
  [ -n "$SRV_PID" ] && wait "$SRV_PID" 2>/dev/null || true
  SRV_PID=""
  sleep 2
}
trap 'stop_server' EXIT

# ---------- pré-requisitos ----------
mkdir -p "$SHOTS"
# Evita que um controle negativo de execução anterior seja reaproveitado.
if [ "$CONTROL" != "1" ]; then
  rm -f "$SHOTS"/control__* 2>/dev/null || true
fi
if ss -tlnp 2>/dev/null | grep -q ":$PORT "; then
  log "ERRO: porta $PORT em uso. Pare o servidor ou use VISUAL_PORT=outro."; exit 2
fi

CHROME=${VISUAL_CHROME:-}
if [ -z "$CHROME" ]; then
  CHROME=$(find "$HOME/.cache/ms-playwright" -maxdepth 5 -type f -path '*chrome-linux*/chrome' 2>/dev/null | head -1 || true)
  [ -z "$CHROME" ] && CHROME=$(find "$HOME/.cache/ms-playwright" -maxdepth 5 -type f -name 'chrome-headless-shell' 2>/dev/null | head -1 || true)
fi
if [ ! -x "$CHROME" ]; then
  log "ERRO: Chromium não encontrado. Instale com 'npx playwright install chromium'"
  log "      ou aponte VISUAL_CHROME=/caminho/para/chrome"; exit 2
fi
log "Chromium: $CHROME"

python3 -c 'import PIL' 2>/dev/null || { log "ERRO: Pillow ausente (python3 -m pip install Pillow)"; exit 2; }
if [ ! -d "$HARNESS/node_modules/playwright-core" ]; then
  log "instalando playwright-core..."
  (cd "$HARNESS" && npm install --no-audit --no-fund)
fi

# ---------- checkouts isolados ----------
log "preparando checkouts em $OUT (baseline=$BASE_REF atual=$CUR_REF)"
rm -rf "$OUT/old" "$OUT/new" "$OUT/control"
mkdir -p "$OUT/old" "$OUT/new"
git -C "$ROOT" archive "$BASE_REF" | tar -xf - -C "$OUT/old"
if [ "$CUR_REF" = "WORKTREE" ]; then
  (cd "$ROOT" && tar -cf - --exclude=./.git --exclude=./target --exclude=./recursos.db \
     --exclude='*.log' --exclude=./tests/visual/node_modules .) | tar -xf - -C "$OUT/new"
else
  git -C "$ROOT" archive "$CUR_REF" | tar -xf - -C "$OUT/new"
fi
if [ "$CONTROL" = "1" ]; then
  mkdir -p "$OUT/control"
  git -C "$ROOT" archive "$CONTROL_REF" | tar -xf - -C "$OUT/control"
fi

# Cada cópia usa uma porta própria para não conflitar com o dev server.
for d in old new control; do
  if [ -f "$OUT/$d/pom.xml" ]; then
    sed -i "s|<port>8081</port>|<port>$PORT</port>|g" "$OUT/$d/pom.xml"
  fi
done

# ---------- servidores + captura ----------
start_server() {
  local dir="$1" name="$2" slog="$OUT/server-$name.log"
  log "subindo $name em $BASE_URL"
  (cd "$dir" && rm -f recursos.db && mise exec -- mvn -B jetty:run > "$slog" 2>&1) &
  SRV_PID=$!
  for _ in $(seq 1 120); do
    grep -qE 'Started Jetty Server|Started oejs.Server' "$slog" 2>/dev/null && break
    kill -0 "$SRV_PID" 2>/dev/null || break
    sleep 1
  done
  sleep 4
  if ! grep -qE 'Started Jetty Server|Started oejs.Server' "$slog" 2>/dev/null; then
    log "ERRO: $name não subiu. Últimas linhas:"; tail -20 "$slog"; return 1
  fi
  # Equaliza o dado de teste: a carga antiga não atribuía grupo aos usuários.
  python3 -c "
import sqlite3, os
p = '$dir/recursos.db'
if os.path.exists(p):
    c = sqlite3.connect(p); c.execute('update Usuario set grupo_id = 1'); c.commit(); c.close()
" || true
}

run_stack() {
  local dir="$1" name="$2"; shift 2
  start_server "$dir" "$name"
  VISUAL_CHROME_PATH="$CHROME" node "$HARNESS/capture.mjs" "$name" "$SHOTS" "$BASE_URL" "$@"
  # Captura o estado do banco DEPOIS das interações (cadastro/exclusão).
  python3 "$HARNESS/db_snapshot.py" "$SHOTS/${name}__meta.json" "$dir/recursos.db"
  stop_server
}

run_stack "$OUT/old" old
run_stack "$OUT/new" new
if [ "$CONTROL" = "1" ]; then
  run_stack "$OUT/control" control --home-only
fi

# ---------- comparação + relatório ----------
set +e
python3 "$HARNESS/compare.py" "$OUT" "$MAX_PCT"
RC=$?
set -e
python3 "$HARNESS/report.py" "$OUT"
log "relatório: $OUT/index.html"
log "resultado: $OUT/results.json (exit=$RC)"
exit "$RC"
