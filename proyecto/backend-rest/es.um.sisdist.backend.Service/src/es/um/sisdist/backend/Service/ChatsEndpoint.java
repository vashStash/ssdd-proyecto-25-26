package es.um.sisdist.backend.Service;

import java.net.URI;
import java.util.ArrayList;
import java.util.Optional;

import es.um.sisdist.backend.Service.impl.AppLogicImpl;
import es.um.sisdist.backend.dao.models.Chat;
import es.um.sisdist.backend.dao.models.utils.ChatStatus;
import es.um.sisdist.backend.grpc.PromptResponse;
import es.um.sisdist.backend.grpc.TicketResponse;
import es.um.sisdist.models.ChatDTO;
import es.um.sisdist.models.DialogueDTO;
import es.um.sisdist.models.ResultadoEnvioLlama;
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

@Path("/u/{username}")
public class ChatsEndpoint {
    private AppLogicImpl impl = AppLogicImpl.getInstance();


    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/chats")
    public Response getChatList(@PathParam("username") String username, UserDTO uo)
    {
        return Response.ok().build();
    }

    @POST
    @Path("/u/{userId}/dialogue/{dialogueId}/next")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response enviarPrompt (@PathParam("userId") String userId, @PathParam("dialogueId") String dialogueId, DialogueDTO input, @Context UriInfo uriInfo){

        ResultadoEnvioLlama resLlama = impl.enviarPromptLlama(userId, input.getPrompt());

        if ("ACEPTADO".equals(resLlama.getEstado())) {
            
            URI location = uriInfo.getBaseUriBuilder()
                                .path("u").path(userId)
                                .path("dialogue").path(dialogueId)
                                .queryParam("t", resLlama.getRespuesta())
                                .build();

            return Response.status(Response.Status.ACCEPTED)
                        .location(location)
                        .build();
        }

        if ("BUSY".equals(resLlama.getEstado())) {
           
            return Response.status(Response.Status.NO_CONTENT)
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
    public Response obtenerChat(@PathParam("username") String userId, @PathParam("dialogueId") String dialogueId, @QueryParam("t") String ticket) {

        ResultadoEnvioLlama resIA = impl.consultarRespuestaLlama(userId, ticket);
        ChatDTO dto = new ChatDTO();
        dto.setId(dialogueId);
        String urlConsulta = "/u/" + userId + "/dialogue/" + dialogueId + "?t=" + ticket;
        dto.setNextUrl(urlConsulta);

        if ("READY".equals(resIA.getEstado())) {
            dto.setStatus(ChatStatus.READY);
            DialogueDTO lineaConversacion = new DialogueDTO();
            lineaConversacion.setAnswer(resIA.getRespuesta()); 
            lineaConversacion.setAnswerDate(new java.util.Date());
            dto.addDialogue(lineaConversacion);
        } 
        else if ("BUSY".equals(resIA.getEstado())) {
            dto.setStatus(ChatStatus.BUSY);
        } 
        else {
            
            dto.setStatus(ChatStatus.FINISHED);
        }

        return Response.ok(dto).build();
    }

   

}
