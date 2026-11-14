# Migração para Wicket 11 / Spring 7 / Hibernate 7 / Jetty 12

Branch `migracao/wicket11`. Substitui a stack de 2015 (Wicket 6.26, Spring 4.2,
Hibernate 4.3, Jetty 8, Java 7/11) por versões GA de 2026.

## Versões

| Componente | Antes | Depois |
|---|---|---|
| Java | 7 (`-source 1.7`), JDK 11 para executar | **21** (`maven.compiler.release=21`) |
| Wicket | 6.26.0 | **11.0.0** (Jakarta Servlet 6.1) |
| Spring Framework | 4.2.2 | **7.0.10** |
| Spring LDAP / Data | 2.1 / 1.13 | 4.1.2 / 4.1.2 |
| Hibernate | 4.3.11 | **7.4.15.Final** (`org.hibernate.orm`) |
| Dialeto SQLite | `hibernate4-sqlite-dialect` 0.1.2 | `org.hibernate.community.dialect.SQLiteDialect` |
| Jetty | 8.1.7 (`org.mortbay.jetty`) | **12.1.15** (`jetty-ee11-maven-plugin`) |
| Servlet / persistência | `javax.*` | `jakarta.*` |
| Logging | Log4j 1.2 | Logback |
| Pool | c3p0 0.9.1.2 | `DriverManagerDataSource` (SQLite é single-writer) |

## Principais mudanças de código

- **`DAOGeneric`**: `org.hibernate.criterion` foi removido no Hibernate 6. Reescrito
  com JPA Criteria. Novos tipos `Filtro`, `Filtros`, `Ordem` substituem
  `Criterion`, `Restrictions`, `Order`, `Projections`; os 8 `Business` foram
  adaptados. `save()` usa `persist()` para entidades novas (atribui o id ao próprio
  objeto, como o antigo `saveOrUpdate`) e `merge()` para desanexadas.
- **Modais**: `ModalWindow` foi removido no Wicket 9. `ModalBase` adapta a API
  antiga sobre `ModalDialog`; os 3 modais e suas páginas agora são `Panel` com
  `ModalDialog.CONTENT_ID`. `AjaxSubmitLink.onSubmit` perdeu o parâmetro `Form`.
- **Componentes**: Kendo/jQuery-UI → componentes nativos do Wicket;
  `TimeField` (yui.calendar) removido; `DropDownChoice.onSelectionChanged` →
  `AjaxFormComponentUpdatingBehavior`; APIs extintas de `WicketApplication.init()`.
- **REST**: `HttpMethod` mudou para `org.wicketstuff.restutils.http.HttpMethod`.
- **Entidades**: `@GeneratedValue(strategy = IDENTITY)` e `AttributeConverter`s
  para os tipos Joda-Time (`LocalDate`, `DateTime`, `LocalTime`).
- **Config**: `applicationContext.xml` usa `orm.jpa.hibernate.*`; `web.xml` virou
  Jakarta Servlet 6.1 com `OpenEntityManagerInViewFilter`; `logback.xml` substitui
  `log4j.properties`.
- **CSP**: o Wicket 11 envia por padrão um `Content-Security-Policy` bloqueante
  (`default-src 'none'; style-src 'nonce-…'; script-src 'strict-dynamic' 'nonce-…'`).
  Como os `<link>`/`<script>` do `BasePage.html` não têm nonce, o navegador bloqueava
  Bootstrap, `BasePage.css` e o jQuery do CDN — a página ficava sem estilo.
  `WicketApplication.init()` chama `getCspSettings().blocking().disabled()` para
  preservar o comportamento do Wicket 6. Para religar o CSP, configurar as
  diretivas `STYLE_SRC`/`SCRIPT_SRC` com `'self'` e as origens externas.
- **Carga inicial**: corrigido bug antigo em que `dbTeste()`/`criarAdmin()` não
  atribuíam `grupo` aos usuários (quebrava as telas autenticadas com banco novo).
- `EmbeddedJettyServer` (classe morta, API Jetty 8) removido.

## Validação

`mvn -B test-compile` → BUILD SUCCESS (0 erros).
`mvn -B jetty:run` → Jetty 12.1.15 na porta 8081; login `admin/admin` e
10 páginas HTTP 200 com banco novo, sem erros no log.

**Testes automatizados (versionados):**
- `mise run test` — 6 testes JUnit de integração: `DAOGeneric` (CRUD, filtros,
  ordem, paths aninhados, converters Joda), `PeriodoBusiness` (regra de
  interseção) e `UsuarioBusiness` (login por MD5), com SessionFactory SQLite real.
- `mise run visual-test` — **13/13 rotas montadas**, 15 capturas (desktop +
  mobile), 4 interações (login inválido, cadastro, exclusão com confirmação,
  abertura de modal) e verificação de persistência no SQLite. Compara com a
  stack antiga via Chromium headless e falha com exit 1 em qualquer divergência.
  Documentação completa em `tests/visual/README.md`.

Resultado: 0,000% de diferença em todas as telas; controle negativo com CSP
bloqueante (`VISUAL_CONTROL=1`) acusa 84,259%.

**Bugs reais que os novos testes encontraram e corrigiram:**
1. `Filtros.eq("id", null)` gerava `id <> NULL`, anulando a validação de
   interseção de períodos → agora usa `cb.isNull`.
2. Os 3 HTMLs de modal ainda eram páginas completas embora as classes tenham
   virado `Panel` → `MarkupNotFoundException` ao abrir; convertidos para
   `<wicket:panel>`.
3. O `ModalDialog` do Wicket 11 não traz CSS (antes vinha do Kendo) → conteúdo
   inline; adicionado CSS em `css/BasePage.css`.

Com o `recursos.db` já versionado, `/ambientes/` e `/horarios/` retornam 500
porque o usuário `admin` desse banco está com `grupo_id = NULL` — comportamento
**idêntico na stack antiga** (verificado). As demais 8 páginas respondem 200.

## Pendências conhecidas

1. Joda-Time mantido via `AttributeConverter`; ideal migrar para `java.time`.
2. REST desligado por padrão (`rest.ligar=false`) e não revalidado — o
   restannotations 11 usa Jackson em vez de Gson/Joda.
3. LDAP e agendamento Quartz simplificados (Spring 7 removeu o suporte a Quartz).
   Para reativar: bean `LdapTemplate` (spring-ldap 4) e `@Scheduled`.
4. `ModalBase` é um adaptador: os setters de layout são no-ops e o conteúdo é
   `Panel` (não mais `Page` em iframe). Revisar CSS dos modais.
5. Acesso direto a `/reservashoje/`, `/reservasfixas/`, `/reservassemana/` falha
   (páginas montadas exigem construtor com `Ambiente`); navegação interna funciona.
6. Ainda não há testes automatizados — a validação foi smoke HTTP manual.
7. Para produção, trocar `DriverManagerDataSource` por HikariCP com
   `maximumPoolSize=1` em SQLite (ou pool normal em PostgreSQL/MySQL).
