package es.um.sisdist.models;

import java.util.Date;
import es.um.sisdist.backend.dao.models.Conversation;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ConversationDTO {
    private String conversationId;
    private String prompt;
    private String answer;
    private long timestamp;

    public ConversationDTO() {
    }
    
    public ConversationDTO(String conversationId, String prompt, String answer, long timestamp) {
        this.conversationId = conversationId;
        this.prompt = prompt;
        this.answer = answer;
        this.timestamp = timestamp;
    }

    // Getters y Setters
    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setCreationDate(long timestamp) {
        this.timestamp = timestamp;
    }


    public static ConversationDTO toDTO(Conversation conver) {
        if (conver == null) {
            return null;
        }
        return new ConversationDTO(
            conver.getId(), 
            conver.getPrompt(), 
            conver.getAnswer(), 
            conver.getTimestamp()
        );
    }

    @Override
    public String toString() {
        return "ConversationDTO [conversationId=" + conversationId + ", prompt=" + prompt + ", answer=" + answer
                + ", creationDate=" + timestamp + ", answerDate=" + "]";
    }
}
