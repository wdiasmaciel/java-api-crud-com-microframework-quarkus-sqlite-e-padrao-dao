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
        │       └── usuario
        │           ├── model
        │           │   └── Usuario.java
        │           ├── dao
        │           │   └── UsuarioDAO.java
        │           └── controller
        │               └── UsuarioResourceController.java
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

# Configurações Globais (arquivo microprofile-config.properties)

Localizado em `src/main/resources/META-INF/microprofile-config.properties`, este arquivo utiliza a especificação `MicroProfile Config` para gerenciar a aplicação. 

Ele altera a porta padrão e injeta dinamicamente a variável `${DB_PATH}` declarada no arquivo de ambiente `.env`.

```properties
# Altera a porta de execução padrão de 8080 para 8082:
quarkus.http.port=8082

# Configuração do Data Source padrão via Agroal e Driver SQLite:
quarkus.datasource.db-kind=sqlite

# Injeta de forma nativa a variável de ambiente DB_PATH obtida do arquivo .env:
quarkus.datasource.jdbc.url=jdbc:sqlite:${DB_PATH}

# Configurações adicionais para otimização de pool de conexões:
quarkus.datasource.jdbc.max-size=5
quarkus.datasource.jdbc.min-size=1
```

# Modelo de Domínio (Usuario.java)

O modelo Usario.java representa a entidade de negócio de forma pura (Plain Old Java Object, POJO), contendo os campos exigidos para cada usuário.

```java
package com.usuario.model;

import java.time.LocalDate;

public class Usuario {
    private Long id;
    private String nome;
    private LocalDate nascimento; 

    // Construtor padrão (obrigatório para serialização/deserialização do Jackson JSON)
    public Usuario() {
    }

    // Construtor com todos os argumentos:
    public Usuario(Long id, String nome, LocalDate nascimento) {
        this.id = id;
        this.nome = nome;
        this.nascimento = nascimento;
    }

    // Getters e Setters:
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public LocalDate getNascimento() {
        return nascimento;
    }

    public void setNascimento(LocalDate nascimento) {
        this.nascimento = nascimento;
    }
}
```


# Camada de Persistência (UsuarioDAO.java)

Gerencia o ciclo de vida dos dados injetando a classe nativa do `pool` do `Agroal` (`AgroalDataSource`). 

Além das operações CRUD tradicionais, foi incluído um iniciador dinâmico (`createTable()`) com a anotação `@Startup` para criar a tabela de banco automaticamente caso ela não exista no banco de dados `SQLite` mapeado no arquivo de ambiente `.env`.

```java
package com.exemplo.dao;

import com.exemplo.model.Usuario;
import io.agroal.api.AgroalDataSource;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped // Define que esta classe é um componente gerenciado de escopo único (Padrão Gof Singleton), Contexts and Dependency Injection (Contextos e Injeção de Dependências, CDI).
public class UsuarioDAO {

    @Inject
    AgroalDataSource dataSource; // Injeta o pool de conexões configurado no microprofile-config.properties.

    @Startup // Executa este método automaticamente no momento em que a aplicação inicia.
    public void createTable() {
        String sql = """
                  CREATE TABLE IF NOT EXISTS usuarios (
                    id INTEGER PRIMARY KEY AUTOINCREMENT
                    nome TEXT NOT NULL, 
                    nascimento TEXT NOT NULL);
                """; // SQLite armazena datas de forma otimizada como TEXT ISO-8601.
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inicializar a tabela SQLite", e);
        }
    }

    public List<Usuario> buscarTodos() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT id, nome, nascimento FROM usuarios";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Usuario(
                        rs.getLong("id"),
                        rs.getString("nome"),
                        LocalDate.parse(rs.getString("nascimento"))));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar usuários", e);
        }
        return lista;
    }

    public Usuario buscarPorId(Long id) {
        String sql = "SELECT id, nome, nascimento FROM usuarios WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.getConnection().prepareStatement(sql)) {
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Usuario(
                            rs.getLong("id"),
                            rs.getString("nome"),
                            LocalDate.parse(rs.getString("nascimento")));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar usuário por ID", e);
        }
        return null;
    }

    public Usuario criar(Usuario usuario) {
        String sql = "INSERT INTO usuarios (nome, nascimento) VALUES (?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, usuario.getNome());
            pstmt.setString(2, usuario.getNascimento().toString()); // Converte LocalDate para String ISO (YYYY-MM-DD).
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    usuario.setId(generatedKeys.getLong(1)); // Define o ID autogerado pelo SQLite no objeto de retorno.
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir usuário", e);
        }
        return usuario;
    }

    public boolean atualizar(Usuario usuario) {
        String sql = "UPDATE usuarios SET nome = ?, nascimento = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, usuario.getNome());
            pstmt.setString(2, usuario.getNascimento().toString());
            pstmt.setLong(3, usuario.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar usuário", e);
        }
    }

    public boolean deletar(Long id) {
        String sql = "DELETE FROM usuarios WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao deletar usuário", e);
        }
    }
}
```