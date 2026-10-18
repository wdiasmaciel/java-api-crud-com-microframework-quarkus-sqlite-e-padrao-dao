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


# Arquivo de Configuração de Dependências (pom.xml)

O arquivo `pom.xml` gerencia as dependências do ecossistema `Quarkus` necessárias para expor endpoints `JSON` (`quarkus-rest-jackson`), fornecer o `pool` de conexões `JDBC` corporativo (`quarkus-agroal`) e embarcar o driver nativo do `SQLite` (`quarkus-jdbc-sqlite`).

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://apache.org"
         xmlns:xsi="http://w3.org"
         xsi:schemaLocation="http://apache.org https://apache.org">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.exemplo</groupId>
    <artifactId>quarkus-sqlite-dao</artifactId>
    <version>1.0.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.release>21</maven.compiler.release>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <quarkus.platform.version>3.15.1</quarkus.platform.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>io.quarkus.platform</groupId>
                <artifactId>quarkus-bom</artifactId>
                <version>${quarkus.platform.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <dependencies>
        <!-- Quarkus REST com Jackson para serialização automática de JSON -->
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-rest-jackson</artifactId>
        </dependency>

        <!-- Driver JDBC do SQLite nativo para o ecossistema Quarkus -->
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-jdbc-sqlite</artifactId>
        </dependency>

        <!-- Agroal: Pool de conexões padrão de alto desempenho do Quarkus -->
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-agroal</artifactId>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>io.quarkus.platform</groupId>
                <artifactId>quarkus-maven-plugin</artifactId>
                <version>${quarkus.platform.version}</version>
                <extensions>true</extensions>
                <executions>
                    <execution>
                        <goals>
                            <goal>build</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```