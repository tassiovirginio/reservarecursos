Copyright 2015 - Tássio Guerreiro Antunes Virgínio

#SISTEMA PARA RESERVA DE AMBIENTES E RESERVA DE RECURSOS

1 - Funciona nos Sitemas Operacionais
* Windows
* OSX
* Linux

2 - Requisitos
* Java 21 (o `mise.toml` fixa esta versão)
* Maven 3.x (fixado em 3.10.0 no `mise.toml`)
* Git 2.x
* mise (https://mise.jdx.dev) - recomendado; gerencia Java e Maven do projeto
* Banco de Dados (SQLite por padrão; MySQL, Postgres, Derby entre outros)

3 - Stack atual
* **Spring Boot 4.1.2** (Tomcat 11 embarcado, Jakarta Servlet 6.1, JAR executável)
* Wicket 11.0.0 · Spring Framework 7.0.10 · Hibernate 7.4.15.Final
* Logback · dialeto SQLite de `hibernate-community-dialects`
* Histórico: `docs/MIGRACAO.md` (Wicket 6 → 11) e `docs/SPRING-BOOT.md`
  (WAR/Jetty → Spring Boot)

4 - Instalarção/Run
* git clone https://github.com/tassiovirginio/reservarecursos.git
* cd reservarecursos
* mise install   # instala Java 21 e Maven 3.10.0, se necessário
* mise run       # sobe a aplicação (mvn spring-boot:run)

  Outras formas:
  * `mvn spring-boot:run`
  * `mvn -DskipTests package && java -jar target/reservarecursos-1.0-SNAPSHOT.jar`
  * `mise run boot-jar` (empacota e executa o JAR)

5 - Acesso após a instalação:
* <http://127.0.0.1:8081>
* login: admin
* senha: admin

A porta vem de `server.port` em `application.properties` e pode ser sobrescrita
com `--server.port=OUTRA`. O banco `recursos.db` é criado na primeira execução.
Para parar: `Ctrl+C` (não há mais `jetty:stop`).

6 - Testes automatizados
* `mise run test` — 6 testes JUnit de integração (DAO, regras de negócio,
  converters Joda-Time) com SessionFactory SQLite real.
* `mise run visual-test` — 13/13 rotas, 15 capturas e 4 interações comparadas
  com a stack antiga (Wicket 6); falha com exit 1 em regressão. Detalhes em
  `tests/visual/README.md`.
