package es.um.sisdist.backend.Service.impl;

import java.lang.StackWalker.Option;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.lang.reflect.Array;
import java.util.LinkedList;
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
import es.um.sisdist.models.ChatDTO;
import es.um.sisdist.models.ConversationDTO;
import es.um.sisdist.models.ResultadoEnvioLlama;
import es.um.sisdist.models.ChatDTO;
import es.um.sisdist.models.UserDTO;
import es.um.sisdist.models.UserDTOUtils;
import es.um.sisdist.backend.dao.DAOFactoryImpl;
import es.um.sisdist.backend.dao.IDAOFactory;
import es.um.sisdist.backend.dao.chats.IChatDAO;
import es.um.sisdist.backend.dao.models.Chat;
import es.um.sisdist.backend.dao.models.Conversation;
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
    IChatDAO chatDao;

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
            
        chatDao = daoFactory.createMongoChatDao();

        chatDao = daoFactory.createMongoChatDao();

        chatDao = daoFactory.createMongoChatDao();

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
            List<Chat> chatUser = chatDao.getChatsByUserId(u.get().getId());
            if (chatUser == null) {
                chatUser = new LinkedList<Chat>();
            }
            u.get().setChatList(chatUser);

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
    
    public List<ChatDTO> getChatList(String userid){
        LinkedList<ChatDTO> chatlist = new LinkedList<ChatDTO>();
        for (Chat chat: chatDao.getChatsByUserId(userid)) {
            chatlist.add(new ChatDTO(chat.getId(), chat.getName(), chat.getNextToken(), chat.getStatus()));
        }
        return chatlist;
    }

    public String crearChat(String userid, String chatName){
        Chat chat = new Chat(userid, chatName, ChatStatus.READY, null);
        User user = dao.getUserById(userid).get();
        chatDao.createChat(chat);        
        user.addChat(chat);
        dao.updateUser(user);
        return chat.getId();
    }

    public Optional<Chat> getChat(String userid, String chatid){
        
        Optional<Chat> hola = chatDao.getChatById(chatid);
        System.out.println("Se llega hasta getChats");
        Optional<User> user = dao.getUserById(userid);
        
        if (!hola.isPresent() || !user.isPresent()) {

            System.out.println("No lo termina de crear");
            return Optional.empty();
        }

        System.out.println("Se muestra info de usuario");
        System.out.println(user.get().toString());

        for (Chat chat : user.get().getChatList()) {
            if (chat.getId().equals(hola.get().getId())) {
                
                System.out.println("devuelve algo");
                return hola;
            }
        }
        return Optional.empty();
    }


    /////////////////////// PROMPTS //////////////////////

    //Enviamos la solicitud para recibir un Token:

    public ResultadoEnvioLlama enviarPromptLlama(String userId, String dialogueId, String token, String prompt) {
        
        Chat chat = chatDao.getChatById(dialogueId).orElse(null);
        
        if (chat == null || !chat.getUser_id().equals(userId)) {

            return new ResultadoEnvioLlama("ERROR", "Chat no encontrado");
        }


        if (token != null && !token.equals(chat.getNextToken())) {
            return new ResultadoEnvioLlama("TOKEN_INVALIDO", null);
        }

        if (chat.getStatus() == ChatStatus.BUSY) {
            return new ResultadoEnvioLlama("BUSY", null);
        }

        try {
        
            Conversation nuevoMensaje = new Conversation(UUID.randomUUID().toString(), dialogueId, prompt, "");
            nuevoMensaje.setCreationDate(new java.util.Date());
            chat.addConversation(nuevoMensaje);
            chat.setStatus(ChatStatus.BUSY);
            chat.setNextToken(null);
            chatDao.updateChat(chat);
            PromptRequest request = PromptRequest.newBuilder()
                    .setIdUser(userId)
                    .setPromptRequest(prompt)
                    .build();

            TicketResponse tr = blockingStub.preguntarLlama(request);

            return new ResultadoEnvioLlama(tr.getStatus(), tr.getTicketResponse());

        } catch (Exception e) {
            // Si cae lo mejor es no bloquearlo
            chat.setStatus(ChatStatus.READY);
            chatDao.updateChat(chat);

            return new ResultadoEnvioLlama("ERROR", e.getMessage());
        }
    }

    public ChatDTO consultarRespuestaLlama(String userId, String dialogueId, String ticket) {
        
        Chat chat = chatDao.getChatById(dialogueId).orElse(null);
        if (chat == null) return null;
        System.out.println("Otra vez toca reinicio?");
        String ticketLimpio = (ticket != null) ? ticket.replace("]", "").replace("[", "").trim() : "";
        try {
            
            TicketRequest request = TicketRequest.newBuilder().setTicketRequest(ticketLimpio)
                    .build();

            PromptResponse response = blockingStub.consultaTicket(request);

            if ("READY".equals(response.getStatus()) && chat.getStatus() == ChatStatus.BUSY) {
                List<Conversation> convs = chat.getConversation();
                if (!convs.isEmpty()) {
                    Conversation ultima = convs.get(convs.size() - 1);
                    ultima.setAnswer(response.getResponse());
                    ultima.setAnswerDate(new java.util.Date());
                    chat.setNextToken(UUID.randomUUID().toString());
                    chat.setStatus(ChatStatus.READY);
                    chatDao.updateChat(chat);
                }
            }
        } catch (Exception e) {
            logger.severe("Error consultando ticket gRPC: " + e.getMessage());
        }
            ChatDTO dto = ChatDTO.toDTO(chat);
            return dto;
    }       

    

}
