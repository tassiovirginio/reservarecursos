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
- **Carga inicial**: corrigido bug antigo em que `dbTeste()`/`criarAdmin()` não
  atribuíam `grupo` aos usuários (quebrava as telas autenticadas com banco novo).
- `EmbeddedJettyServer` (classe morta, API Jetty 8) removido.

## Validação

`mvn -B test-compile` → BUILD SUCCESS (0 erros).
`mvn -B jetty:run` → Jetty 12.1.15 na porta 8081; login `admin/admin` e
10 páginas HTTP 200, sem erros no log.

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
