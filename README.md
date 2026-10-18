# java-api-crud-com-microframework-quarkus-sqlite-e-padrao-dao

Criar uma `API REST` com suporte a operações CRUD (CREATE, READ, UPDATE, DELETE), utilizando:

1. O microframework `Quarkus`, 

2. O banco de dados `SQLite`, 

3. Variáveis de ambiente lidas a partir de um arquivo `.env`, 

4. O padrão estrutural DAO (Data Access Object), 

5. A extensão para teste de APIs `REST Client`, 

6. O comando (Command Line Interface, CLI) para teste de APIs `curl` e

7. A extensão para visualização de dados no banco de dados `SQLite Viewer`.

# Arquitetura do projeto:

```text
├── .env
├── pom.xml
└── src
    └── main
        ├── java
        │   └── com
        │       └── exemplo
        │           ├── model
        │           │   └── Usuario.java
        │           ├── dao
        │           │   └── UsuarioDAO.java
        │           └── resource
        │               └── UsuarioResource.java
        └── resources
            └── META-INF
                └── microprofile-config.properties
```