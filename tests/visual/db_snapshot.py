#!/usr/bin/env python3
"""Anexa a contagem de linhas do SQLite ao meta.json (prova de persistência).

Uso: python3 db_snapshot.py <meta.json> <recursos.db>
"""
import json
import os
import sqlite3
import sys

meta_path, db_path = sys.argv[1], sys.argv[2]
TABELAS = ['Usuario', 'Grupo', 'Periodo', 'Ambiente', 'Horario', 'Recurso', 'Reserva', 'Emprestimo']

counts = {}
if not os.path.exists(db_path):
    counts['_erro'] = 'banco nao encontrado'
else:
    conn = sqlite3.connect(db_path)
    for t in TABELAS:
        try:
            counts[t] = conn.execute(f'select count(*) from "{t}"').fetchone()[0]
        except Exception as e:
            counts[t] = f'erro: {e}'
    conn.close()

meta = json.load(open(meta_path))
meta['db'] = counts
with open(meta_path, 'w') as f:
    json.dump(meta, f, indent=2)
print(f"[{meta['label']}] db: {counts}")
