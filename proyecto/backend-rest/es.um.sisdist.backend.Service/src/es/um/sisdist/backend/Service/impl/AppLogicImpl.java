package es.um.sisdist.backend.Service.impl;

import java.util.ArrayList;
<<<<<<< HEAD
import java.util.HashMap;
=======
>>>>>>> bcee22c6274b404ae4b7db721c33675dfd558b45
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import es.um.sisdist.backend.grpc.GrpcServiceGrpc;
import es.um.sisdist.backend.grpc.PingRequest;
import es.um.sisdist.backend.grpc.PromptRequest;
import es.um.sisdist.backend.grpc.TicketResponse;
import es.um.sisdist.backend.grpc.TicketRequest;
import es.um.sisdist.backend.grpc.PromptResponse;
<<<<<<< HEAD
import es.um.sisdist.models.ChatDTO;
import es.um.sisdist.models.DialogueDTO;
=======
>>>>>>> bcee22c6274b404ae4b7db721c33675dfd558b45
import es.um.sisdist.models.ResultadoEnvioLlama;
import es.um.sisdist.models.UserDTO;
import es.um.sisdist.models.UserDTOUtils;
import es.um.sisdist.backend.dao.DAOFactoryImpl;
import es.um.sisdist.backend.dao.IDAOFactory;
import es.um.sisdist.backend.dao.models.Chat;
import es.um.sisdist.backend.dao.models.Dialogue;
import es.um.sisdist.backend.dao.models.User;
import es.um.sisdist.backend.dao.models.utils.ChatStatus;
import es.um.sisdist.backend.dao.models.utils.UserUtils;
import es.um.sisdist.backend.dao.user.IUserDAO;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.ServiceUnavailableException;
import jakarta.ws.rs.core.Response;

/**
 * @author dsevilla
 *
 */
public class AppLogicImpl
{
    IDAOFactory daoFactory;
    IUserDAO dao;
    // CHATS SIN BD
    private Map<String, ChatDTO> mapChats = new ConcurrentHashMap<>();

    private static final Logger logger = Logger.getLogger(AppLogicImpl.class.getName());

    private final ManagedChannel channel;
    private final GrpcServiceGrpc.GrpcServiceBlockingStub blockingStub;
    //private final GrpcServiceGrpc.GrpcServiceStub asyncStub;

    static AppLogicImpl instance = new AppLogicImpl();

    private AppLogicImpl()
    {
        daoFactory = new DAOFactoryImpl();
        Optional<String> backend = Optional.ofNullable(System.getenv("DB_BACKEND"));
        
        if (backend.isPresent() && backend.get().equals("mongo"))
            dao = daoFactory.createMongoUserDAO();
        else
            dao = daoFactory.createSQLUserDAO();

        var grpcServerName = Optional.ofNullable(System.getenv("GRPC_SERVER"));
        var grpcServerPort = Optional.ofNullable(System.getenv("GRPC_SERVER_PORT"));

        channel = ManagedChannelBuilder
                .forAddress(grpcServerName.orElse("localhost"), Integer.parseInt(grpcServerPort.orElse("50051")))
                // Channels are secure by default (via SSL/TLS). For the example we disable TLS
                // to avoid needing certificates.
                .usePlaintext().build();
        blockingStub = GrpcServiceGrpc.newBlockingStub(channel);
        //asyncStub = GrpcServiceGrpc.newStub(channel);
        
    }

    public static AppLogicImpl getInstance()
    {
        return instance;
    }

    public Optional<User> getUserByEmail(String userId)
    {
        Optional<User> u = dao.getUserByEmail(userId);
        return u;
    }

    public Optional<User> getUserById(String userId)
    {
        return dao.getUserById(userId);
    }

    public boolean ping(int v)
    {
    	logger.info("Issuing ping, value: " + v);
    	
        // Test de grpc, puede hacerse con la BD
    	var msg = PingRequest.newBuilder().setV(v).build();
        var response = blockingStub.ping(msg);
        
        return response.getV() == v;
    }

    // El frontend, a través del formulario de login,
    // envía el usuario y pass, que se convierte a un DTO. De ahí
    // obtenemos la consulta a la base de datos, que nos retornará,
    // si procede,
    public Optional<User> checkLogin(String email, String pass)
    {
        Optional<User> u = dao.getUserByEmail(email);

        if (u.isPresent())
        {
            System.out.println("applogic: usuario recuperado" + u.get().toString());
            String hashed_pass = UserUtils.md5pass(pass);
            System.out.println("Contraseña recibida (hashed): " + hashed_pass + " \nContraseña almacenada: " + u.get().getPassword_hash());
            if (0 == hashed_pass.compareTo(u.get().getPassword_hash()))
                return u;
        }
        System.out.println("applogic: Nose ha encontrado el user para login");
        return Optional.empty();
    }

    public boolean registerUser(User user){
        return dao.registerUser(user);
    }

    //helper para el register
    public boolean userExists(User u){
        if(!dao.getUserByEmail(u.getEmail()).equals(Optional.empty())){
            System.out.println("applogic: El email ya existe");
            return true;
        }
        if (!dao.getUserById(u.getId()).equals(Optional.empty())){
            System.out.println("applogic: El id ya existe");
            return true;
        }
        System.out.println("applogic: el usuario no existe");
        return false;
        

        // if(!(dao.getUserByEmail(u.getEmail()).equals(Optional.empty())
        //         || dao.getUserById(u.getId()).equals(Optional.empty()))){ 
        //     return true;
        // }
        // return false;

        // return dao.getUserByEmail(u.getEmail()).equals(Optional.empty()) 
        //     ? dao.getUserById(u.getId()).equals(Optional.empty()) 
        //         ? true
        //         : false
        //     : false;
    }

    //////////////////////// CHATS /////////////////////
    public List<Chat> getChatList(){
        return new ArrayList<Chat>();
<<<<<<< HEAD

=======
>>>>>>> bcee22c6274b404ae4b7db721c33675dfd558b45
    }

    //Enviamos la solicitud para recibir un Token:

<<<<<<< HEAD
    public ResultadoEnvioLlama enviarPromptLlama(String userId, String dialogueId, String token, String prompt) {
    
        Chat chat = chatDAO.findById(dialogueId);
        
        if (chat == null) {
            return new ResultadoEnvioLlama("ERROR", "Chat no encontrado");
        }

        if (token != null && !token.equals(chat.getNextToken())) {
            return new ResultadoEnvioLlama("TOKEN_INVALIDO", null);
        }

        if (chat.getStatus() == ChatStatus.BUSY) {
            return new ResultadoEnvioLlama("BUSY", null);
        }

        try {
        
            Dialogue nuevoMensaje = new Dialogue(UUID.randomUUID().toString(), dialogueId, prompt, "");
            chat.addDialogue(nuevoMensaje);
            chat.setStatus(ChatStatus.BUSY);
            chat.setNextToken(null);
            chatDAO.update(chat);

=======
    public ResultadoEnvioLlama enviarPromptLlama(String userId, String prompt) {
    
        try {
        
>>>>>>> bcee22c6274b404ae4b7db721c33675dfd558b45
            PromptRequest request = PromptRequest.newBuilder()
                    .setIdUser(userId)
                    .setPromptRequest(prompt)
                    .build();

            TicketResponse tr = blockingStub.preguntarLlama(request);

            return new ResultadoEnvioLlama(tr.getStatus(), tr.getTicketResponse());

        } catch (Exception e) {
<<<<<<< HEAD
            // Si cae lo mejor es no bloquearlo
            chat.setStatus(ChatStatus.READY);
            chatDAO.update(chat);
=======
            // Si gRPC falla o el servidor Python está caído
>>>>>>> bcee22c6274b404ae4b7db721c33675dfd558b45
            return new ResultadoEnvioLlama("ERROR", e.getMessage());
        }
    }

    // Consulta de token

<<<<<<< HEAD
    public ChatDTO consultarRespuestaLlama(String userId, String dialogueId, String ticket) {
=======
    public ResultadoEnvioLlama consultarRespuestaLlama(String userId, String ticket) {
>>>>>>> bcee22c6274b404ae4b7db721c33675dfd558b45
        
        try {
            
            TicketRequest request = TicketRequest.newBuilder().setTicketRequest(ticket)
                    .build();

            PromptResponse response = blockingStub.consultaTicket(request);

            return new ResultadoEnvioLlama(response.getStatus(), response.getResponse());

        } catch (Exception e) {
            // Si gRPC falla o el servidor Python está caído
            return new ResultadoEnvioLlama("ERROR_CONEXION", e.getMessage());
        }

    }

}
