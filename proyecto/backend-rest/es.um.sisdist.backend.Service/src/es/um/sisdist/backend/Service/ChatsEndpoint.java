package es.um.sisdist.backend.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import es.um.sisdist.backend.Service.impl.AppLogicImpl;
import es.um.sisdist.backend.dao.models.Chat;
import es.um.sisdist.backend.dao.models.User;
import es.um.sisdist.models.ChatDTO;
import es.um.sisdist.models.UserDTO;
import es.um.sisdist.models.UserDTOUtils;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
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
        return Response.ok(chatlist, MediaType.APPLICATION_JSON).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/nuevochat")
    public Response newChat(@PathParam("userid") String userid, String chatname)
    {
        Optional<User> u = impl.getUserById(userid);
         if(!u.isPresent()){
            System.out.println("getChatList: Usuario no encontrado: " + userid);
            return Response.status(Status.NOT_FOUND).build();
        } else {
            System.out.println("creando nuevo chat para " + u.get().getName());
        }

        String chatID = impl.crearChat(userid, chatname);

        if(chatID == null) return Response.status(Status.NOT_MODIFIED).build();
        return Response.ok(chatID, MediaType.APPLICATION_JSON).build();
    }
}
