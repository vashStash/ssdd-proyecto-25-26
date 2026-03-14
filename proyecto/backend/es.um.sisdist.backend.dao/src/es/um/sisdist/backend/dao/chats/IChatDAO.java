package es.um.sisdist.backend.dao.chats;

import java.util.List;
import java.util.Optional;

import es.um.sisdist.backend.dao.models.Chat;
import es.um.sisdist.backend.dao.models.utils.ChatStatus;

public interface IChatDAO {

    Optional<Chat> getChatById(String chatId);

    List<Chat> getChatsByUserId(String userId);

    List<Chat> getAllChats();

    boolean createChat(Chat chat);

    boolean updateChat(Chat chat);

    boolean deleteChat(String userId, String chatId);

    boolean deleteAllChatsByUser(String userId);

    int getChatCountByUser(String userId);
}
