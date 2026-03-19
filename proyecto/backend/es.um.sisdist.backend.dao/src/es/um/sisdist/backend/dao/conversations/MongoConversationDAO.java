package es.um.sisdist.backend.dao.conversations;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Logger;

import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.Conventions;
import org.bson.codecs.pojo.PojoCodecProvider;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;

import es.um.sisdist.backend.dao.chats.MongoChatDAO;
import es.um.sisdist.backend.dao.models.Chat;
import es.um.sisdist.backend.dao.models.Conversation;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static com.mongodb.client.model.Filters.eq;
import static java.util.Arrays.asList;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import java.util.ArrayList;
import java.util.logging.Level;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;

public class MongoConversationDAO implements IConversationDAO{

    private MongoCollection<Conversation> collection;
    private final Logger logger;

    public MongoConversationDAO (){
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
        collection = database.getCollection("conversations", Conversation.class);
    }

    @Override
    public Conversation createConversation(Conversation conversation) {
        if (insertConversation(conversation)) {
            return conversation;
        }
        return null;
    }

    @Override
    public Optional<Conversation> getConversationById(String conversationId) {
        try {
            Conversation conversation = collection.find(eq("_id", conversationId)).first();
            return Optional.ofNullable(conversation);
        } catch (MongoException e) {
            logger.log(Level.SEVERE, "Error retrieving conversation by ID", e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<List<Conversation>> getConversationsByChatId(String chatId) {
         try {
            List<Conversation> conversations = new ArrayList<>();
            collection.find(eq("chat_id", chatId)).into(conversations);
            return Optional.of(conversations);
        } catch (MongoException e) {
            logger.log(Level.SEVERE, "Error retrieving conversations by chat ID", e);
            return Optional.empty();
        }
    }

    @Override
    public boolean insertConversation(Conversation conversation) {
        try {
            InsertOneResult result = collection.insertOne(conversation);
            return result.wasAcknowledged();
        } catch (MongoException e) {
            logger.log(Level.SEVERE, "Error inserting conversation", e);
            return false;
        }
    }

    @Override
    public boolean updateConversation(Conversation conversation) {
        try {
            UpdateResult result = collection.replaceOne(
                    eq("_id", conversation.getId()), conversation);
            return result.getModifiedCount() > 0;
        } catch (MongoException e) {
            logger.log(Level.SEVERE, "Error updating chat", e);
            return false;
        }
    }

    @Override
    public boolean deleteConversation(String conversationId) {
        try {
            DeleteResult result = collection.deleteOne(eq("_id", conversationId));
            return result.getDeletedCount() > 0;
        } catch (MongoException e) {
            logger.log(Level.SEVERE, "Error deleting conversation", e);
            return false;
        }
    }

    @Override
    public int getConversationCountByChatId(String chatId) {
        try {
            return (int) collection.countDocuments(eq("chat_id", chatId));
        } catch (MongoException e) {
            logger.log(Level.SEVERE, "Error counting conversations", e);
            return 0;
        }
    }
    
}
