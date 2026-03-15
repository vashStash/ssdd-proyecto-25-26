package es.um.sisdist.backend.dao.conversations;

import java.util.List;
import java.util.Optional;

import es.um.sisdist.backend.dao.models.Conversation;

public class MongoConversationDAO implements IConversationDAO{

    @Override
    public Conversation createConversation(Conversation conversation) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createConversation'");
    }

    @Override
    public Optional<Conversation> getConversationById(String conversationId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getConversationById'");
    }

    @Override
    public Optional<List<Conversation>> getConversationsByChatId(String chatId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getConversationsByChatId'");
    }

    @Override
    public boolean insertConversation(Conversation conversation) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'insertConversation'");
    }

    @Override
    public boolean updateConversation(Conversation conversation) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateConversation'");
    }

    @Override
    public boolean deleteConversation(String conversationId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteConversation'");
    }

    @Override
    public int getConversationCountByChatId(String chatId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getConversationCountByChatId'");
    }
    
}
