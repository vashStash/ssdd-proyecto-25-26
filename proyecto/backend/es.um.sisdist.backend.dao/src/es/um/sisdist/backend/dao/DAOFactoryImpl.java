/**
 *
 */
package es.um.sisdist.backend.dao;

import es.um.sisdist.backend.dao.chats.IChatDAO;
import es.um.sisdist.backend.dao.chats.MongoChatDAO;
import es.um.sisdist.backend.dao.conversations.IConversationDAO;
import es.um.sisdist.backend.dao.conversations.MongoConversationDAO;
import es.um.sisdist.backend.dao.models.Conversation;
import es.um.sisdist.backend.dao.user.IUserDAO;
import es.um.sisdist.backend.dao.user.MongoUserDAO;
import es.um.sisdist.backend.dao.user.SQLUserDAO;

/**
 * @author dsevilla
 *
 */
public class DAOFactoryImpl implements IDAOFactory
{
    @Override
    public IUserDAO createSQLUserDAO()
    {
        return new SQLUserDAO();
    }

    @Override
    public IUserDAO createMongoUserDAO()
    {
        return new MongoUserDAO();
    }

    @Override
    public IChatDAO createMongoChatDao() {
        return new MongoChatDAO();
    }

    @Override
    public IConversationDAO createMongoConversationDAO() {
        return new MongoConversationDAO();
    }
}
