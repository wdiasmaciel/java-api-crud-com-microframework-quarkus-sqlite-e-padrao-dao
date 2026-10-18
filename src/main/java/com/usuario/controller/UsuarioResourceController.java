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
