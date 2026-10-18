# java-api-crud-com-microframework-quarkus-sqlite-e-padrao-dao

# Exemplo Prático: API CRUD com Quarkus, SQLite e Padrão DAO

Objetivo: demonstrar a construção de uma API REST, utilizando o ecossistema **Quarkus**.

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
├── data/
├── SQLiteViewer.png
├── RESTClient.png
├── pom.xml
├── README.md
├── testes.http
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── usuario/
        │           ├── model/
        │           │   └── Usuario.java
        │           ├── dao/
        │           │   └── UsuarioDAO.java
        │           └── controller/
        │               └── UsuarioResourceController.java
        └── resources/
            └── META-INF/
                └── microprofile-config.properties
```

# Variável de Ambiente

O `Quarkus` lê variáveis de ambiente diretamente de um arquivo `.env` localizado na raiz do projeto. 

Crie o arquivo `.env` com o conteúdo abaixo na raiz do projeto:

```properties
DB_PATH=./data/usuarios.db
```

Crie a pasta `data`, para armazenar o arquivo do banco de dados `SQLite`.

# Arquivo de Configuração de Dependências (pom.xml)

O arquivo `pom.xml` gerencia as dependências do ecossistema `Quarkus` necessárias para expor endpoints `JSON` (`quarkus-rest-jackson`), fornecer o `pool` de conexões `JDBC` corporativo (`quarkus-agroal`) e conectar ao `SQLite` pela extensão `quarkus-jdbc-sqlite`, publicada pelo projeto Quarkiverse.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <!-- Identificação do seu projeto com o pacote 'usuario' -->
    <groupId>com.usuario</groupId>
    <artifactId>quarkus-sqlite-dao</artifactId>
    <version>1.0.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.release>21</maven.compiler.release>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <!-- Versão estável do ecossistema Quarkus -->
        <quarkus.platform.version>3.15.1</quarkus.platform.version>
    </properties>

    <!-- Gerenciador de Versões (BOM): Resolve o erro de versão ausente nas dependências abaixo -->
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
        <!-- Quarkus REST com suporte para mapeamento automático de JSON (Jackson) -->
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-rest-jackson</artifactId>
        </dependency>

        <!-- Extensão Quarkiverse para conectar ao SQLite via JDBC -->
        <dependency>
            <groupId>io.quarkiverse.jdbc</groupId>
            <artifactId>quarkus-jdbc-sqlite</artifactId>
            <version>3.0.11</version>
        </dependency>

        <!-- Agroal: Gerenciador e Pool de conexões padrão do Quarkus -->
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-agroal</artifactId>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <!-- Plugin oficial necessário para gerenciar o ciclo de vida do Quarkus e Dev Mode -->
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

O modelo `Usuario.java` representa a entidade de negócio como um Plain Old Java Object (POJO), contendo os campos exigidos para cada usuário.

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
package com.usuario.dao;

import com.usuario.model.Usuario;
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
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
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
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
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

# CDI

O acrônimo `CDI` no contexto de desenvolvimento Java significa `Contexts and Dependency Injection` (Contextos e Injeção de Dependências).

Ele é a especificação oficial do ecossistema `Jakarta EE` (antigo `Java EE`) e `MicroProfile` para gerenciar o ciclo de vida dos componentes e a fiação de dependências na aplicação.

Para quem está acostumado com o framework `Spring Boot`, o `CDI` desempenha exatamente o mesmo papel do `Spring IoC` (`Inversion of Control`). 

Ele resolve dois problemas centrais:

1. Injeção de Dependências (Dependency Injection)

Em vez de você instanciar manualmente as classes usando o operador `new` (o que acopla o código), o framework cria e gerencia os objetos para você.

• No Spring: você usa `@Autowired` ou Injeção via método Construtor.

• No CDI (Quarkus): você usa a anotação `@Inject` (que pertence ao padrão oficial do Java, `jakarta.inject.Inject`).

2. Gerenciamento de Contextos (Contexts / Scopes)

O `CDI` define por quanto tempo um objeto deve "viver" na memória do servidor. 

Os escopos mais comuns que substituem as anotações do Spring são:

• `@ApplicationScoped`: o componente tem uma única instância para toda a aplicação (equivalente ao `@Component` ou `@Service` padrão do Spring, que são o padrão de projeto `GoF` `Singletons`). É o que foi usado no `UsuarioDAO`.

• `@RequestScoped`: uma nova instância do componente é criada a cada requisição HTTP e destruída logo após a resposta ser enviada (equivalente ao `@Scope("request")` do Spring).

• `@SessionScoped`: a instância dura enquanto a sessão HTTP do usuário estiver ativa.

Em resumo: o `CDI` é o motor que permite que anotações como `@Inject` e `@ApplicationScoped` funcionem no `Quarkus` sem que você precise carregar bibliotecas do Spring Boot.

# Camada de Controlador REST (UsuarioResourceController.java)

Expõe os `endpoints` HTTP, utilizando a especificação `JAX-RS` com retornos estruturados via classe `Response`.

```java
package com.usuario.controller;

import com.usuario.dao.UsuarioDAO;
import com.usuario.model.Usuario;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/usuarios") // Rota base da API.
@Produces(MediaType.APPLICATION_JSON) // As respostas do controlador serão estruturadas em JSON.
@Consumes(MediaType.APPLICATION_JSON) // As entradas recebidas devem obrigatoriamente ser JSON.
public class UsuarioResourceController {

    @Inject
    UsuarioDAO usuarioDAO; // Injeta a lógica de persistência de dados (DAO).

    @GET
    public List<Usuario> listar() {
        return usuarioDAO.buscarTodos();
    }

    @GET
    @Path("/{id}")
    public Response buscarPorId(@PathParam("id") Long id) {
        Usuario usuario = usuarioDAO.buscarPorId(id);
        if (usuario == null)
            return Response.status(Response.Status.NOT_FOUND).entity("{\"erro\": \"Usuário não encontrado\"}").build();

        return Response.ok(usuario).build();
    }

    @POST
    public Response criar(Usuario usuario) {
        if (usuario.getNome() == null || usuario.getNascimento() == null) 
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"erro\": \"Campos 'nome' e 'nascimento' são obrigatórios.\"}").build();
        
        Usuario novoUsuario = usuarioDAO.criar(usuario);
        return Response.status(Response.Status.CREATED).entity(novoUsuario).build();
    }

    @PUT
    @Path("/{id}")
    public Response atualizar(@PathParam("id") Long id, Usuario usuario) {
        usuario.setId(id);
        boolean atualizado = usuarioDAO.atualizar(usuario);
        if (!atualizado) 
            return Response.status(Response.Status.NOT_FOUND).entity("{\"erro\": \"Usuário não encontrado para atualização\"}").build();
        
        return Response.ok(usuario).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deletar(@PathParam("id") Long id) {
        boolean deletado = usuarioDAO.deletar(id);
        if (!deletado) 
            return Response.status(Response.Status.NOT_FOUND).entity("{\"erro\": \"Usuário não encontrado para remoção\"}").build();
        
        return Response.noContent().build(); // Retorna 204 No Content (padrão REST para deleção bem-sucedida).
    }
}
```

# Extensão SQLite Viewer do VS Code

Instalar a extensão `SQLite Viewer` do VS Code, para verificar os dados armazenados no arquivo do banco de dados `SQLite`:

![Extensão SQLite Viewer do VS Code](SQLiteViewer.png "Extensão REST Client do VS Code")


# Extensão REST Client do VS Code

Instalar a extensão `REST Client` do VS Code:

![Extensão REST Client do VS Code](RESTClient.png "Extensão REST Client do VS Code")


Criar um arquivo chamado `testes.http` na raiz do projeto.

Colar o código abaixo.

Clicar no link `Send Request` que aparecerá automaticamente em cima de cada método HTTP.

```properties
### Configurações de Variáveis Locais do REST Client
@baseUrl = http://localhost:8082
@contentType = application/json

### 1. Criar Primeiro Usuário (Ana Silva)
# @name criarAna
POST {{baseUrl}}/usuarios
Content-Type: {{contentType}}

{
    "nome": "Ana Silva",
    "nascimento": "2000-05-15"
}

### 2. Criar Segundo Usuário (Carlos Souza)
POST {{baseUrl}}/usuarios
Content-Type: {{contentType}}

{
    "nome": "Carlos Souza",
    "nascimento": "1995-11-23"
}

### 3. Tentar Criar Usuário Inválido (Deve retornar Erro 400)
POST {{baseUrl}}/usuarios
Content-Type: {{contentType}}

{
    "nome": "Usuário Sem Data"
}

### 4. Listar Todos os Usuários
GET {{baseUrl}}/usuarios
Accept: {{contentType}}

### 5. Buscar Usuário por ID Específico
GET {{baseUrl}}/usuarios/1
Accept: {{contentType}}

### 6. Tentar Buscar Usuário Inexistente (Deve retornar Erro 404)
GET {{baseUrl}}/usuarios/999
Accept: {{contentType}}

### 7. Atualizar Dados do Usuário 1
PUT {{baseUrl}}/usuarios/1
Content-Type: {{contentType}}

{
    "nome": "Ana Silva Souza",
    "nascimento": "2000-05-15"
}

### 8. Deletar Usuário 2 (Deve retornar Status 204 No Content)
DELETE {{baseUrl}}/usuarios/2

### 9. Confirmar Deleção Listando Novamente
GET {{baseUrl}}/usuarios
Accept: {{contentType}}
```

# Executar no GitHub Codespaces

O ambiente do `GitHub Codespaces` já oferece suporte nativo ao ecossistema Java (JDK) e ferramentas de build como o Apache Maven. 

Siga o roteiro de comandos abaixo diretamente no terminal integrado:

### Iniciar o servidor em Modo de Desenvolvimento (Dev Mode)

O `Quarkus` se destaca pela funcionalidade de *Live Coding*.

Execute o comando abaixo na raiz do projeto. A propriedade desabilita o build analytics do Quarkus para este comando; ela é uma opção do Maven, não uma configuração de runtime do `microprofile-config.properties`:

```bash
mvn -Dquarkus.analytics.disabled=true quarkus:dev
```
*A infraestrutura do `Quarkus` detectará o arquivo `.env`, mapeará a string de conexão para a propriedade do MicroProfile Config e inicializará o servidor HTTP embutido na porta **8082**.*

O comando pressupõe que a porta `8082` esteja livre. Se outra instância já estiver usando essa porta, encerre-a antes de iniciar o servidor.

---

## Testando a API (CRUD)

Abra uma aba de terminal secundária no `GitHub Codespaces` para realizar chamadas REST de teste via utilitário `curl`:

Os exemplos abaixo usam os IDs `1` e `2` e pressupõem um banco novo. Ao repetir os testes, use os IDs retornados pelas requisições `POST` ou pare o servidor e remova `data/usuarios.db` para recriar o banco. A remoção apaga todos os registros desse banco.

### Cadastrar um Usuário (CREATE)
```bash
curl -X POST http://localhost:8082/usuarios \
  -H "Content-Type: application/json" \
  -d '{"nome": "Ana Silva", "nascimento": "2000-05-15"}'
```

### Listar Todos os Usuários (READ)
```bash
curl -X GET http://localhost:8082/usuarios
```

### Listar Usuário por ID (READ)
```bash
curl -X GET http://localhost:8082/usuarios/1
```

### Modificar Dados do Usuário (UPDATE)
```bash
curl -X PUT http://localhost:8082/usuarios/1 \
  -H "Content-Type: application/json" \
  -d '{"nome": "Ana Silva Souza", "nascimento": "2000-05-15"}'
```

### Remover Registro do Sistema (DELETE)
```bash
curl -X DELETE http://localhost:8082/usuarios/1
```

# Exercícios

Empregando a linguagem de programação Java, o microframework Quarkus, o banco de dados SQLite e Padrão DAO, implemente as seguintes APIs:

### 1) Cliente e Produtos
O sistema deve permitir:
1. O CRUD de clientes e de produtos.
2. Listar todos os produtos adquiridos por um certo cliente, cujo `id` é informado como parâmetro.
3. Listar as categorias de produto adquiridas por mais de `n` clientes, em que `n` é um número informado como parâmetro.

### 2) Agência de Viagens
O sistema deve permitir:
1. O CRUD de clientes e destinos turísticos.
2. Listar todos os destinos turísticos visitados por um certo cliente, cujo `id` é informado como parâmetro.
3. Listar todos os clientes que visitaram um certo destino turístico, cujo `id` é informado como parâmetro.
4) Listar os destinos turísticos agrupados e ordenados por país.
5) Listar todos os países com mais de `n` destinos turísticos, em que `n` é um número informado como parâmetro.