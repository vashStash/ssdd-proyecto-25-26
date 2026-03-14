package es.um.sisdist.backend.dao.conversations;

import java.util.List;
import java.util.Optional;

import es.um.sisdist.backend.dao.models.Conversation;

public interface IConversationDAO {

    Conversation createConversation(Conversation conversation);

    Optional<Conversation> getConversationById(String conversationId);

    Optional<List<Conversation>> getConversationsByChatId(String chatId);

    boolean insertConversation(Conversation conversation);

    boolean updateConversation(Conversation conversation);

    boolean deleteConversation(String conversationId);

    int getConversationCountByChatId(String chatId);
}
