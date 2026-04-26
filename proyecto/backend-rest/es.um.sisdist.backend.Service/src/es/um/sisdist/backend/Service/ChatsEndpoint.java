package es.um.sisdist.backend.Service;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.print.attribute.standard.Media;

import es.um.sisdist.backend.Service.impl.AppLogicImpl;
import es.um.sisdist.backend.dao.models.Chat;
import es.um.sisdist.backend.dao.models.Conversation;
import es.um.sisdist.backend.dao.models.utils.ChatStatus;
import es.um.sisdist.backend.grpc.PromptResponse;
import es.um.sisdist.backend.grpc.TicketResponse;
import es.um.sisdist.models.ChatDTO;
import es.um.sisdist.models.ConversationDTO;
import es.um.sisdist.models.ResultadoEnvioLlama;
import es.um.sisdist.backend.dao.models.User;
import es.um.sisdist.models.UserDTO;
import es.um.sisdist.models.UserDTOUtils;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.core.Response.Status;


@Path("/u/{userid}")
public class ChatsEndpoint {
    private AppLogicImpl impl = AppLogicImpl.getInstance();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/chats")
    public Response getChatList(@PathParam("userid") String userid)
    {

        Optional<User> u = impl.getUserById(userid);
        
        if(!u.isPresent()){
            System.out.println("getChatList: Usuario no encontrado: " + userid);
            return Response.status(Status.NOT_FOUND).build();
        } else {
            System.out.println("recuperando chats de: " + u.get().getName());
        }

        // recuperamos la lista de ids de los chats y devolvemos una respuesta
        List<ChatDTO> chatlist = impl.getChatList(u.get().getId());
        if (chatlist == null){
            return Response.status(Status.NOT_FOUND).build();
        }

        System.out.println("se devolverá " + chatlist.toString());
        // System.out.println("se devolverá " + chatlist.toString());
        return Response.ok(chatlist, MediaType.APPLICATION_JSON).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/chats")
    public Response newChat(@Context UriInfo uriInfo, @PathParam("userid") String userid, ChatDTO chatname)
    {
        Optional<User> u = impl.getUserById(userid);
         if(!u.isPresent()){
            System.out.println("getChatList: Usuario no encontrado: " + userid);
            return Response.status(Status.NOT_FOUND).build();
         }
        System.out.println("creando nuevo chat para " + u.get().getName());

        String chatNuevo = impl.crearChat(userid, chatname.getName());

        if(chatNuevo == null) { 
            return Response.status(Status.NOT_MODIFIED).build();
        }

        URI location = uriInfo.getAbsolutePathBuilder()
                          .path(chatNuevo)
                          .build();

        return Response.created(location).build();
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/chats/{chatid}")
    public Response getChat(@PathParam("userid") String userid, @PathParam("chatid") String chatid){
      
        Optional<Chat> chat = impl.getChat(userid, chatid);
       
        if (chat.isEmpty()) {
            return Response.status(Status.NOT_FOUND).build();
        } else {
            System.out.println("Recibida petición de chat: " + chat.get());
            System.out.println("devolviendo chat: " + chat.get().toString());
            ChatDTO chatDTO = ChatDTO.toDTO(chat.get());
            for (Conversation conversation : chat.get().getConversation()) {
                chatDTO.addConversation(ConversationDTO.toDTO(conversation));
            }
            return Response.ok(chatDTO, MediaType.APPLICATION_JSON).build();
        }
    }





    @POST
    @Path("/dialogue/{dialogueId}/next/{token : (.*)}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response enviarPrompt (@PathParam("userid") String userId, @PathParam("dialogueId") String dialogueId, @PathParam("token") String token, ConversationDTO input, @Context UriInfo uriInfo){
        System.out.println("Entra para pedir al token");
        String tokenTratado = (token == null || token.isEmpty()) ? null : token;

        ResultadoEnvioLlama resLlama = impl.enviarPromptLlama(userId, dialogueId, tokenTratado, input.getPrompt());
        System.out.println("Vuelve de la llamada a grpc");
        if ("ACEPTADO".equals(resLlama.getEstado())) {
            
            URI location = uriInfo.getBaseUriBuilder()
                .path("u").path(userId)
                .path("dialogue").path(dialogueId)
                .queryParam("t", resLlama.getRespuesta())
                .build();
                System.out.println("Este es el token recibido " + resLlama.getRespuesta());

            return Response.status(Response.Status.ACCEPTED)
                        .location(location)
                        .build();
        }

        if ("TOKEN_INVALIDO".equals(resLlama.getEstado())) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        if ("BUSY".equals(resLlama.getEstado())) {
           
            return Response.status(Response.Status.NO_CONTENT)
                        .build();
        }
        
        if ("ERROR".equals(resLlama.getEstado())) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                   .build();
        }

        if ("FORMATO INVALIDO".equals(resLlama.getEstado())) {

            return Response.status(Response.Status.BAD_REQUEST)
                       .build();
        }

        return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .build();
    }

    @GET
    @Path("/dialogue/{dialogueId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response consultarEstado(@PathParam("userid") String userId, @PathParam("dialogueId") String dialogueId, @QueryParam("t") String ticket) { 
        System.out.println("Lo consulta de verdad el token");
        if (ticket == null || ticket.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\":\"Falta el token de seguimiento\"}")
                        .build();
        }

        ChatDTO resultado = impl.consultarRespuestaLlama(userId, dialogueId, ticket);

        if (resultado == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (resultado.getStatus() == ChatStatus.READY) {
        
            return Response.ok(resultado).build();
            
        } else {
        
            return Response.status(Response.Status.ACCEPTED).build();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/chat/{chatId}/end")
    public Response finalizarChat(@PathParam("userid") String userid, @PathParam("chatId") String chatid){
      
        boolean resultado = impl.finalizarChat(userid, chatid);

        if (resultado) {

            return Response.ok().build();
        } 

        return Response.status(Response.Status.NOT_FOUND).build();
    }
   




}
