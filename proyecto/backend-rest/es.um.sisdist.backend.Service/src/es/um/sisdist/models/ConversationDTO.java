package es.um.sisdist.models;

import java.util.Date;

import es.um.sisdist.backend.dao.models.Conversation;

public class ConversationDTO {
    private String conversationId;
    private String prompt;
    private String answer;
    private Date creationDate;
    private Date answerDate;

    
    public ConversationDTO(String conversationId, String prompt, String answer, Date creationDate, Date answerDate) {
        this.conversationId = conversationId;
        this.prompt = prompt;
        this.answer = answer;
        this.creationDate = creationDate;
        this.answerDate = answerDate;
    }
    
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
    
    public Date getcreationDate() {
        return creationDate;
    }
    
    public void setcreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getAnswerDate() {
        return answerDate;
    }
    
    public void setAnswerDate(Date answerDate) {
        this.answerDate = answerDate;
    }
    
    public static ConversationDTO toDTO(Conversation conver){
        if (conver == null) {
            return null;
        }
        return new ConversationDTO(conver.getId(), conver.getPrompt(), conver.getAnswer(), conver.getCreationDate(), conver.getAnswerDate());
    }

    
    
    @Override
    public String toString() {
        return "ConversationDTO [ConversationId=" + conversationId + ", prompt=" + prompt + ", answer=" + answer + ", creationDate="
                + creationDate + ", answerDate=" + answerDate + "]";
    }

    public ConversationDTO(){};
}
