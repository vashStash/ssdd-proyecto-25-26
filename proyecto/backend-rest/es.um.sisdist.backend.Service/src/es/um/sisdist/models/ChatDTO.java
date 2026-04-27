package es.um.sisdist.models;

import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.LinkedList;
import java.util.List;

import es.um.sisdist.backend.dao.models.Chat;
import es.um.sisdist.backend.dao.models.utils.ChatStatus;

@XmlRootElement
public class ChatDTO {

    private String id;
    private String name;
    private String next;
    private String end;
    private ChatStatus status;
    private List<ConversationDTO> conversation;
    
    public ChatDTO(String id, String name, String next, ChatStatus status){
        this.id = id;
        this.name = name;
        this.next = next;
        this.status = status;
        this.conversation = new LinkedList<>();
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public String getNext() {
        return next;
    }

    public void setNext(String nextUrl) {
        this.next = nextUrl;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public ChatStatus getStatus() {
        return status;
    }

    public void setStatus(ChatStatus status) {
        this.status = status;
    }

    public void addConversation(ConversationDTO nuevo){
        this.conversation.add(nuevo);
    }

    public List<ConversationDTO> getConversation() {
        return conversation;
    }

    public void setConversation(List<ConversationDTO> conversation) {
        this.conversation = conversation;
    }


    public static ChatDTO toDTO(Chat chat) {
        if (chat == null) {
            return null;
        }
        ChatDTO dto =  new ChatDTO(chat.getId(), chat.getName(), null ,chat.getStatus());
        String urlNext = "/u/" + chat.getUser_id() + "/chats/" + chat.getId();
        if (chat.getStatus() == ChatStatus.READY) {
            dto.setNext(urlNext + "/next/" + chat.getNextToken());
            dto.setEnd(urlNext + "/end");
        } else {
            //Si el estado no es Ready, no se permite el uso de next o end.
            dto.setNext(null);
            dto.setEnd(null);
        }

        return dto;
    }

    @Override
    public String toString() {
        return "ChatDTO [id=" + id + ", name=" + name + ", nextUrl=" + next + ", status=" + status + ", conversation="
                + conversation + "]";
    }

    public ChatDTO(){
        this.conversation = new LinkedList<>();
    }
}
