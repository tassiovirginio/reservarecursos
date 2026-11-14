# Specs do Reserva de Recursos

Esta pasta registra o comportamento conhecido do sistema e os requisitos de mudanças futuras. Os documentos descrevem o código observado; não são garantia de que a aplicação foi executada ou de que todo comportamento em produção foi confirmado.

## Documentos

- [`current-system.md`](current-system.md): linha de base do sistema existente, com fatos observados e pontos ainda a confirmar.
- `features/`: specs de funcionalidades novas ou alterações de comportamento, quando forem propostas.

## Fluxo de trabalho com agentes

Para uma mudança nova:

1. Descreva o problema e quem é afetado.
2. Escreva a spec focando no comportamento esperado, sem escolher prematuramente a solução técnica.
3. Registre critérios de aceite observáveis, limites e perguntas em aberto.
4. Peça ao agente para analisar o código e propor um plano sem editar arquivos.
5. Revise o plano e as tarefas; depois implemente em passos pequenos.
6. Verifique os critérios de aceite e registre exatamente o que foi ou não executado.

## Modelo sugerido para uma feature

Crie `specs/features/<id>-<nome>.md` com estas seções:

```markdown
# <Nome da funcionalidade>

## Problema e objetivo
## Usuários e cenário
## Comportamento esperado
## Critérios de aceite
- [ ] ...
## Fora de escopo
## Restrições e compatibilidade
## Perguntas em aberto
```

Não preencha lacunas com suposições silenciosas. Marque o que precisa de decisão do responsável pelo produto.
