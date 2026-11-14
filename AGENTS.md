# Instruções para agentes de código

> **Estado atual (14/11/2026, pós-migrações)** — A "linha de base" abaixo foi
> escrita **antes** das migrações e está desatualizada quanto à stack. Hoje o
> projeto é **Spring Boot 4.1.2 / JAR executável / Tomcat 11 embarcado**, com
> Wicket 11.0.0, Spring 7.0.10, Hibernate 7.4.15, **Java 21** e Jakarta
> Servlet 6.1.
>
> - Rodar: `mise run start` (`mvn spring-boot:run`), porta 8081.
>   `mvn jetty:run` / `jetty:stop` **não existem mais**.
> - Testes: **existem e funcionam** — `mise run test` (6 testes JUnit) e
>   `mise run visual-test` (13/13 rotas, 4 interações, gate de CI).
> - Documentação vigente: `README.md`, `docs/MIGRACAO.md`,
>   `docs/SPRING-BOOT.md`, `tests/visual/README.md`.
> - `AGENTS.md`, `specs/README.md` e `specs/current-system.md` registram a
>   linha de base histórica: confirme sempre contra o código antes de agir.

## Objetivo
Manter e evoluir o Reserva de Recursos em mudanças pequenas, compreensíveis e verificáveis. Este arquivo orienta o trabalho no repositório; não substitui requisitos aprovados nem validação humana.

## Antes de alterar código
1. Leia `specs/README.md` e `specs/current-system.md`.
2. Inspecione os arquivos e fluxos afetados. A especificação inicial foi derivada do código e pode não refletir todos os comportamentos em produção.
3. Separe fatos observados de hipóteses. Se um requisito estiver ambíguo, registre a dúvida antes de escolher um comportamento.
4. Faça a menor alteração coerente com o pedido. Não modernize a stack nem refatore áreas não relacionadas sem escopo explícito.

## Contexto técnico observado
- Aplicação Java empacotada como WAR, com Maven.
- O `pom.xml` define Java source/target 1.7, Apache Wicket 6.26.0, Spring 4.2.2, Hibernate 4.3.11 e Jetty 8.1.7.
- A aplicação usa páginas Wicket, componentes Spring e entidades JPA/Hibernate.
- O banco padrão está configurado como SQLite (`recursos.db`); há configuração comentada para MySQL.
- `mvn jetty:run` aparece na documentação. O plugin Jetty do `pom.xml` configura a porta 8081, enquanto os documentos antigos citam outras portas. Confirme a configuração antes de alterar instruções de execução.
- Os pacotes principais estão em `src/main/java/br/reservarecursos`: `business`, `entities`, `pages`, `rest`, `ldap` e `xml`.
- Não foram encontrados arquivos de teste no inventário inicial, embora o Maven declare JUnit. Não presuma que a suíte exista ou esteja funcional.

## Segurança e dados
- A inicialização cria contas administrativas com credenciais previsíveis; a configuração também ativa dados de teste. Não considere esses padrões seguros para produção.
- Nunca acrescente credenciais reais, tokens, dados pessoais ou cópias do banco a documentação, prompts, logs ou commits.
- Trate `recursos.db` como dado persistente: não o sobrescreva nem o use em experimentos sem autorização explícita.
- Não altere autenticação, permissões, esquema ou dados existentes sem critérios de aceite claros.

## Implementação e verificação
- Preserve a compatibilidade com a stack atual enquanto o escopo não incluir uma migração.
- Antes de propor uma migração de Java ou dependências, identifique impactos e proponha etapas separadas.
- Ao mudar código, descreva os arquivos afetados e a verificação adequada. Não afirme que compilou, executou ou testou algo sem ter feito isso e observado o resultado.
- Prefira verificações direcionadas. Não crie testes nem execute uma suíte por iniciativa própria quando o pedido não incluir verificação.
- Atualize `specs/current-system.md` somente quando uma mudança aprovada alterar o comportamento ou a arquitetura observável. Crie uma spec separada para cada nova funcionalidade.
