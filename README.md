Copyright 2015 - Tássio Guerreiro Antunes Virgínio

#SISTEMA PARA RESERVA DE AMBIENTES E RESERVA DE RECURSOS

1 - Funciona nos Sitemas Operacionais
* Windows
* OSX
* Linux

2 - Requisitos
* Java 11 (o `mise.toml` fixa esta versão). O `pom.xml` compila com
  `-source 1.7`, que o JDK 21 rejeita, e o javassist do Hibernate 4 é
  bloqueado no JDK 17+.
* Maven 3.x (fixado em 3.10.0 no `mise.toml`)
* Git 2.x
* mise (https://mise.jdx.dev) - recomendado; gerencia Java e Maven do projeto
* Banco de Dados (SQLite por padrão; MySQL, Postgres, Derby entre outros)

3 - Instalarção/Run
* git clone https://github.com/tassiovirginio/reservarecursos.git
* cd reservarecursos
* mise install   # instala Java 11 e Maven 3.10.0, se necessário
* mise run       # mesma coisa que: mvn jetty:run

  Sem o mise, use um JDK 11 e execute `mvn jetty:run` diretamente.

4 - Acesso após a instalação:
* <http://127.0.0.1:8081>
* login: admin
* senha: admin

O servidor é o Jetty 8 na porta 8081 (configurada no `pom.xml`).
Para parar: Ctrl+C, ou `mvn jetty:stop` em outro terminal.
