# Linha de base: sistema atual

> **Nota (14/11/2026):** linha de base **pré-migração**. A arquitetura atual é
> Spring Boot 4.1.2 / JAR / Tomcat 11 / Wicket 11 / Java 21, com suíte de testes
> automatizados. Consulte `docs/MIGRACAO.md` e `docs/SPRING-BOOT.md`; os fatos
> abaixo devem ser confirmados contra o código antes de qualquer alteração.

**Status:** levantamento inicial derivado dos arquivos da branch `master` consultados em 14/11/2026. Não é uma validação em execução. Onde o código não permite concluir o comportamento, o item aparece como ponto a confirmar.

## Propósito observado

O repositório se apresenta como um sistema web para reserva de ambientes e recursos. O código contém entidades e páginas relacionadas a ambientes, recursos, horários, períodos, grupos, usuários, reservas, empréstimos e relatórios.

## Stack e execução

- Projeto Maven com empacotamento WAR.
- Propriedade de compilação Java source/target: 1.7.
- Dependências centrais declaradas: Apache Wicket 6.26.0, Spring 4.2.2, Hibernate 4.3.11 e Jetty 8.1.7.
- O banco configurado por padrão em `src/main/resources/application.properties` é SQLite, usando `recursos.db`; há uma configuração MySQL comentada.
- O comando `mvn jetty:run` consta nos documentos. O plugin Jetty no `pom.xml` define a porta 8081. `README.md`, `INSTALAR.txt` e a configuração do plugin não concordam sobre a porta; a documentação de execução precisa ser reconciliada.
- `EmbeddedJettyServer` usa a porta 8080 como padrão quando iniciado por essa classe; é um caminho de execução distinto do plugin Maven.
- A árvore consultada não contém arquivos de teste, embora o `pom.xml` declare JUnit. O estado de compilação e execução não foi verificado neste levantamento.

## Organização do código

- `br.reservarecursos.entities`: entidades persistidas, incluindo `Ambiente`, `Emprestimo`, `Grupo`, `Horario`, `Periodo`, `Recurso`, `Reserva` e `Usuario`.
- `br.reservarecursos.business` e `business.daos`: regras de negócio e acesso a dados.
- `br.reservarecursos.pages`: páginas Wicket e seus arquivos de marcação.
- `br.reservarecursos.rest`: endpoints REST para período, recurso, reserva e usuário. A configuração `rest.ligar` está como `false` por padrão.
- Também existem pacotes para integração LDAP e importação XML.

## Páginas montadas na aplicação

`WicketApplication` monta rotas para menus, ambientes, recursos, horários, períodos, usuários, reservas de hoje, reservas fixas, reservas da semana, relatórios, gerenciamento e grupos. A presença da rota no código não confirma que o fluxo esteja funcional em produção.

## Configuração e segurança observadas

- `db.teste=true` está no arquivo de configuração e habilita a criação de dados de demonstração durante a inicialização.
- `WicketApplication` cria usuários administrativos se eles não existirem. Os comentários e manuais descrevem credenciais padrão previsíveis; não usar esse padrão em produção.
- As configurações de e-mail e LDAP contêm valores de exemplo e LDAP está desativado por padrão. Confirmar antes de usar em qualquer ambiente real.
- O arquivo `recursos.db` está versionado no repositório. Não presumir que seja descartável nem publicar cópias com dados reais.

## Pontos a confirmar com o responsável

1. Qual versão de Java e qual ambiente são usados para executar o sistema atualmente?
2. Qual dos caminhos de inicialização é o usado: `mvn jetty:run`, WAR em servidor externo ou `EmbeddedJettyServer`?
3. O banco versionado contém apenas dados de demonstração? Qual é o procedimento seguro para desenvolvimento local?
4. Quais perfis de usuário e regras de autorização são esperados hoje?
5. Quais fluxos de reserva, aprovação, empréstimo e relatório são prioritários?
6. Há testes ou documentação mantidos fora deste repositório?

## Limites desta linha de base

Este documento não propõe uma migração de framework, não descreve requisitos futuros e não declara que a aplicação compila ou funciona em um ambiente atual. Novas funcionalidades devem ter specs próprias em `specs/features/`.
