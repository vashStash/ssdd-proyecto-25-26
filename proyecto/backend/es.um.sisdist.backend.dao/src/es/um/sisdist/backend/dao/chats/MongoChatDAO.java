package es.um.sisdist.backend.dao.chats;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Updates.set;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import static java.util.Arrays.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;


import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.Conventions;
import org.bson.codecs.pojo.PojoCodecProvider;

import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;

import es.um.sisdist.backend.dao.models.Chat;
import es.um.sisdist.backend.dao.models.utils.ChatStatus;

public class MongoChatDAO implements IChatDAO
{
    private MongoCollection<Chat> collection;
    private Logger logger;

    public MongoChatDAO()
    {
        logger = Logger.getLogger(MongoChatDAO.class.getName());
        CodecProvider pojoCodecProvider = PojoCodecProvider.builder().conventions(asList(Conventions.ANNOTATION_CONVENTION)).automatic(true).build();
        CodecRegistry pojoCodecRegistry = fromRegistries(getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));

        // Replace the uri string with your MongoDB deployment's connection string
        String uri = "mongodb://root:root@"
        		+ Optional.ofNullable(System.getenv("MONGO_SERVER")).orElse("localhost")
                + ":27017/ssdd?authSource=admin";

        // Create a MongoClient with the connection string
       	MongoClient mongoClient = MongoClients.create(uri);
        MongoDatabase database = mongoClient
            .getDatabase(Optional.ofNullable(System.getenv("DB_NAME")).orElse("ssdd"))
        	.withCodecRegistry(pojoCodecRegistry);
        collection = database.getCollection("chats", Chat.class);
    };

    @Override
    public List<Chat> getAllChats() {
        List<Chat> chats = new ArrayList<>();
        try {
            collection.find().into(chats);
            System.out.println("Recuperando todos los chats");
            for (Chat chat : chats) {
                System.out.println("Chat recuperado: " + chat.getName() + ", ID: " + chat.getId() + "userid: " + chat.getUser_id() );
            }

        } catch (MongoException e) {
            logger.log(Level.SEVERE, "Error al recuperar todos los chats", e);
        }

        return chats;
    }

    @Override
    public Optional<Chat> getChatById(String id) {
        Optional<Chat> chat = Optional.ofNullable(collection.find(eq("_id", id)).first());
        return chat;
    }

    @Override
    public List<Chat> getChatsByUserId(String userId) {
        // No se por qué esta función no funciona
        List<Chat> chats = new ArrayList<>();

        try {
            collection.find(eq("user_id", userId)).into(chats);

        } catch (MongoException e) {
            logger.log(Level.SEVERE, "Error retrieving chats", e);
        }
        return chats;
    }

    @Override
    public boolean createChat(Chat chat) {
        try {
            System.out.println("Intentando crear chat " + chat.getName() + " con ID " + chat.getId() + "y userID: " + chat.getUser_id());
            InsertOneResult result = collection.insertOne(chat);
            return result.wasAcknowledged();
        } catch (MongoException e) {
            logger.log(Level.SEVERE, "Error creating chat", e);
            return false;
        }
    }

    @Override
    public boolean updateChat(Chat chat) { // Remplaza el chat existente por una nueva versión
        try {
            UpdateResult result = collection.replaceOne(
                    eq("_id", chat.getId()), chat);
            return result.getModifiedCount() > 0;
        } catch (MongoException e) {
            logger.log(Level.SEVERE, "Error updating chat", e);
            return false;
        }
    }

    @Override
    public boolean deleteChat(String userId, String chatId) {
        try {
            DeleteResult result = collection
                    .deleteOne(and(eq("user_id", userId), eq("_id", chatId)));
            return result.getDeletedCount() > 0;
        } catch (MongoException e) {
            logger.log(Level.SEVERE, "Error deleting chat", e);
            return false;
        }
    }

    @Override
    public boolean deleteAllChatsByUser(String userId) {
        try {
            DeleteResult result = collection.deleteMany(eq("user_id", userId));
            return result.getDeletedCount() > 0;
        } catch (MongoException e) {
            logger.log(Level.SEVERE, "Error deleting all chats by user", e);
            return false;
        }
    }

    @Override
    public int getChatCountByUser(String userId) {
        try {
            return (int) collection.countDocuments(eq("user_id", userId));
        } catch (MongoException e) {
            logger.log(Level.SEVERE, "Error counting chats", e);
            return -1;
        }
    }   
}
