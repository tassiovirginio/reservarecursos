# Migração para Spring Boot (integrada)

Integrada ao `master` via branch `migracao/spring-boot`. Substitui o WAR com
`web.xml` + `applicationContext.xml` + Jetty externo por um **JAR executável**
com Tomcat embarcado e configuração Java.

## Por que Boot 4.1.2

| Item | Valor |
|---|---|
| Spring Boot | **4.1.2** (GA) |
| Spring Framework gerenciado | **7.0.10** — exatamente a versão já usada |
| Servlet / servidor | Jakarta Servlet 6.1 / **Tomcat 11.0.15** embarcado |
| Java | baseline 17, projeto usa **21** |

Não foi usado `spring-boot-starter-parent`: importamos apenas o
`spring-boot-dependencies` (BOM) para não alterar o gerenciamento de plugins.
O `wicket-spring-boot-starter` **não** foi usado — o `WicketFilter` é registrado
manualmente, o que dá controle sobre o `filterMappingUrlPattern` e evita
depender da compatibilidade do starter com Wicket 11.

## O que mudou

| Antes (WAR + Jetty) | Depois (Spring Boot) |
|---|---|
| `<packaging>war</packaging>` + `jetty-ee11-maven-plugin` | `<packaging>jar</packaging>` + `spring-boot-maven-plugin:repackage` |
| `src/main/webapp/WEB-INF/web.xml` | `FilterRegistrationBean<WicketFilter>` e `<OpenEntityManagerInViewFilter>` |
| `applicationContext.xml` (XML) | `ReservaRecursosApplication` com `@Bean` (Java config) |
| `ContextLoaderListener` | `@SpringBootApplication` / `SpringApplication.run` |
| `PropertyPlaceholderConfigurer` | `@Value("${...}")` + `application.properties` do Boot |
| assets servidos pelo DefaultServlet do Jetty | `WebMvcConfigurer.addResourceHandlers` apontando para o classpath |
| `mvn jetty:run` / `jetty:stop` | `mvn spring-boot:run` / `java -jar …jar` / `Ctrl+C` |
| `maven-war-plugin`, `c3p0`, `jakarta.servlet-api` explícito | removidos (servlet vem do `tomcat-embed-core`) |

`ReservaRecursosApplication` mantém todos os beans que existiam no XML:
`dataSource` (`DriverManagerDataSource` para SQLite single-writer),
`sessionFactory` (`LocalSessionFactoryBean` + `SpringSessionContext`),
`transactionManager` (`HibernateTransactionManager`), `mailSender`, os quatro
`Boolean` de configuração, `personRepo` e `atualizador`. O `WicketApplication`
continua `@Component`; o `SpringWebApplicationFactory` resolve o
`WebApplication` pelo contexto raiz que o Boot publica no `ServletContext`.

**Preservados** da migração anterior: `getCspSettings().blocking().disabled()`
(sem isso o CSP do Wicket 11 bloqueia o CSS), os `AttributeConverter`s de
Joda-Time, o CSS do `ModalDialog` e toda a suíte de testes.

## Validação

| Verificação | Resultado |
|---|---|
| `mvn -B test-compile` / `mise run compile` | BUILD SUCCESS |
| `mise run test` | 6 testes JUnit, 0 falhas |
| `mvn -DskipTests package` | JAR executável de 71 MB (`JarLauncher` → `ReservaRecursosApplication`) |
| `java -jar … --server.port=18082` | Tomcat em ~3,4 s, home e CSS HTTP 200 |
| `mise run visual-test` | 15 capturas (13/13 rotas), 4 interações, 0,000% de diff, exit 0 |
| `VISUAL_CONTROL=1` | controle negativo (CSP bloqueante) 84,259% |

O `tests/visual/run.sh` **detecta automaticamente** se o checkout é Boot
(presença de `spring-boot-maven-plugin`) e usa `mvn spring-boot:run`
com `--server.port=$PORT`; checkouts WAR antigos (baseline `4788022` e o
controle negativo `278e804`) continuam usando `mvn jetty:run`. Assim a bateria
segue repetível com os dois modos coexistindo.

## Diferenças conhecidas / pendências

- `web.xml` tinha `<error-page>404 → /</error-page>`; não foi reimplementado
  (404 mostra a página padrão do Boot). Se fizer diferença, registrar via
  `WebServerFactoryCustomizer`.
- LDAP e o agendador Quartz continuam desligados/simplificados (herdado da
  migração Wicket 11). `quartz`, `hsqldb`, `mysql-connector-j` e
  `commons-codec` seguem no pom sem uso — podem ser removidos.
- REST (`rest.ligar=false`) e Joda-Time (via converters) seguem como estavam.
- O 404 de `css/dashboard.css` só ocorre no baseline Wicket 6 (iframe do
  `ModalWindow`); está whitelistado em `tests/visual/compare.py`.
- `mvn jetty:run` deixa de existir; `mise run start` agora é Boot.
