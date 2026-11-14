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
* Wicket 11.0.0 · Spring Framework 7.0.10 · Hibernate 7.4.15.Final
* Jetty 12.1.15 (`jetty-ee11-maven-plugin`) · Jakarta Servlet 6.1
* Logback · dialeto SQLite de `hibernate-community-dialects`
* Histórico da migração a partir do Wicket 6 / Spring 4 / Hibernate 4 / Jetty 8:
  `docs/MIGRACAO.md`

4 - Instalarção/Run
* git clone https://github.com/tassiovirginio/reservarecursos.git
* cd reservarecursos
* mise install   # instala Java 21 e Maven 3.10.0, se necessário
* mise run       # mesma coisa que: mvn jetty:run

  Sem o mise, use um JDK 21 e execute `mvn jetty:run` diretamente.

5 - Acesso após a instalação:
* <http://127.0.0.1:8081>
* login: admin
* senha: admin

O servidor é o Jetty 12 na porta 8081 (configurada no `pom.xml`).
Para parar: Ctrl+C, ou `mvn jetty:stop` em outro terminal.

6 - Testes automatizados
* `mise run test` — 6 testes JUnit de integração (DAO, regras de negócio,
  converters Joda-Time) com SessionFactory SQLite real.
* `mise run visual-test` — 13/13 rotas, 15 capturas e 4 interações comparadas
  com a stack antiga (Wicket 6); falha com exit 1 em regressão. Detalhes em
  `tests/visual/README.md`.
