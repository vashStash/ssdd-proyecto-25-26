package es.um.sisdist.backend.Service;

import java.util.Optional;

import javax.print.attribute.standard.Media;

import es.um.sisdist.backend.Service.impl.AppLogicImpl;
import es.um.sisdist.backend.dao.models.User;
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

@Path("/profile")
public class UsersEndpoint
{
    private AppLogicImpl impl = AppLogicImpl.getInstance();

    @GET
    @Path("/{userid}/info")
    @Produces(MediaType.APPLICATION_JSON)
    public UserDTO getUserInfo(@PathParam("userid") String userid)
    {
        System.out.println("userprofile nuevo");
        return UserDTOUtils.toDTO(impl.getUserByEmail(userid).orElse(null));
    }

    @POST
    @Path("/{userid}/cambiardatos")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response cambiarDatos(@PathParam("userid") String userid, UserDTO userDTO){

        System.out.println("se ha recibido " + userDTO.toString());

        int res = impl.updateUser(userid, userDTO);
        //res == 1 -> user not found
        if(res == 1){
            System.out.println("cambiardatos: Usuario no encontrado: " + userid);
            return Response.status(Status.NOT_FOUND).build();
        } else {
            System.out.println("recuperando chats de: " + userid);
        }

        // res == 2 -> error updating user
        if(res == 2){
            return Response.status(Status.NOT_ACCEPTABLE).build();
        }
        else return Response.status(Status.OK).build();
             

    }

    @POST
    @Path("/{userid}/cambiar_password")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    // TODO implementar lógica de contrasenas
    public Response cambiarPassword(@PathParam("userid") String userid, String newpwd){
        Optional<User> user = impl.getUserById(userid);

        if(!user.isPresent()){
            System.out.println("getChatList: Usuario no encontrado: " + userid);
            return Response.status(Status.NOT_FOUND).build();
        } else {
            System.out.println("recuperando chats de: " + user.get().getName());
        }

        if(impl.updatePwd(user.get(), newpwd)){
            return Response.status(Status.NOT_ACCEPTABLE).build();
        }
        else return Response.status(Status.OK).build();
    }

    @POST
    @Path("/{username}/profile/cambiar_nombre")
    @Produces(MediaType.APPLICATION_JSON)
    public Response cambiarNombre(@PathParam("userid") String userid, String newUsername){
        Optional<User> user = impl.getUserById(userid);

        if(!user.isPresent()){
            System.out.println("getChatList: Usuario no encontrado: " + userid);
            return Response.status(Status.NOT_FOUND).build();
        } else {
            System.out.println("recuperando chats de: " + user.get().getName());
        }

        // no se si esto es correcto hacerlo aquí o mejor en el impl
        user.get().setName(newUsername);

        if(impl.updateUser(user.get(), newUsername)){
            return Response.status(Status.NOT_ACCEPTABLE).build();
        }
        else return Response.status(Status.OK).build();
    }

    @POST
    @Path("/{username}/profile/cambiar_password")
    @Produces(MediaType.APPLICATION_JSON)
    public Response cambiarPassword(@PathParam("userid") String userid, String newpwd){
        Optional<User> user = impl.getUserById(userid);

        if(!user.isPresent()){
            System.out.println("getChatList: Usuario no encontrado: " + userid);
            return Response.status(Status.NOT_FOUND).build();
        } else {
            System.out.println("recuperando chats de: " + user.get().getName());
        }

        if(impl.updateUser(user.get(), newpwd)){
            return Response.status(Status.NOT_ACCEPTABLE).build();
        }
        else return Response.status(Status.OK).build();
    }
}
