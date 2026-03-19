/**
 *
 */
package es.um.sisdist.models;

import es.um.sisdist.backend.dao.chats.MongoChatDAO;
import es.um.sisdist.backend.dao.models.User;
import es.um.sisdist.backend.dao.models.utils.UserUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bson.codecs.CollectionCodecProvider;

/**
 * @author dsevilla
 *
 */
public class UserDTOUtils
{
    public static User fromDTO(UserDTO udto)
    {

        System.out.println("Convirtiendo userDTO a User");
        System.out.println("contraseña antigua: " + udto.getPassword());
        udto.setPassword(UserUtils.md5pass(udto.getPassword())); // TODO borrar sysouts de debug
        System.out.println("Contraseña que se almacenará: " + udto.getPassword());
        ArrayList<String> chatlist = new ArrayList<>(Arrays.asList(udto.getChatlistIDs().split("?")));
        return new User(udto.getId(), udto.getEmail(), udto.getPassword(), 
            udto.getName(), udto.getToken(), udto.getVisits(), chatlist);
    }

    public static UserDTO toDTO(User u)
    {   
        MongoChatDAO chatDAO = new MongoChatDAO();
        String chatlist = "";
        u.getChatList().stream()
        .map(chatid -> chatDAO.getChatById(chatid))
        .filter(Optional::isPresent)
        .map(chat -> chat.get())
        .forEach(chat -> chatlist.concat(chat.getId() + "-"));

        return new UserDTO(u.getId(), u.getEmail(), "", // Password never is returned back
                u.getName(), u.getToken(), u.getVisits(), chatlist);
    }
}
