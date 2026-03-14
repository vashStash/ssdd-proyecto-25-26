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
    @Path("/u/{userId}/dialogue/{dialogueId}/next/{token : (.*)}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response enviarPrompt (@PathParam("userId") String userId, @PathParam("dialogueId") String dialogueId, @PathParam("token") String token, DialogueDTO input, @Context UriInfo uriInfo){

        String tokenTratado = (token == null || token.isEmpty()) ? null : token;

        ResultadoEnvioLlama resLlama = impl.enviarPromptLlama(userId, dialogueId, tokenTratado, input.getPrompt());
        
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
    @Path("/u/{userId}/dialogue/{dialogueId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response consultarEstado(@PathParam("userId") String userId, @PathParam("dialogueId") String dialogueId, @QueryParam("t") String ticket) { 

        if (ticket == null || ticket.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\":\"Falta el token de seguimiento\"}")
                        .build();
        }

        // Llamamos a la lógica (Impl) para ver si gRPC tiene ya la respuesta
        // Le pasamos todo lo necesario para que, si está listo, guarde en BD
        ChatResponseDTO resultado = impl.obtenerResultadoLlama(userId, dialogueId, ticket);

        if (resultado.isReady()) {
            // 200 OK: Devolvemos el JSON tal cual la imagen
            return Response.ok(resultado.getJsonData()).build();
        } else {
            // 202 ACCEPTED: Le decimos a Python "sigue preguntando"
            return Response.status(Response.Status.ACCEPTED).build();
        }
    }
   

}
