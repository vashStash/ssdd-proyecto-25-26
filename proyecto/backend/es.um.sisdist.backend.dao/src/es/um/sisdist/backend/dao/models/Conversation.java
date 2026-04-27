package es.um.sisdist.backend.dao.models;

import java.util.Date;
import java.util.UUID;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;

import es.um.sisdist.backend.dao.models.utils.DateUtils;

public class Conversation {

   
    private String id;
    private String chat_id;
    private String prompt;
    private long timestamp;
    private String answer;
    

    public Conversation() {}

    public Conversation(String id, String chat_id, String prompt, String answer, long timestamp){
        this.id = id;
        this.chat_id = chat_id;
        this.prompt = prompt;
        this.answer = answer;
        this.timestamp = timestamp;
    }

    public Conversation(String id, String chat_id, String prompt, String answer) {
        this(id, chat_id, prompt, answer, System.currentTimeMillis());
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;  
    }

    /**
     * @param id the id to set
     */
    public void setId(final String id) {
        this.id = id;
    }
    /**
     * @return the chat_id
     */
    public String getChat_id() {
        return chat_id;      
    }
    /**
     * @param chat_id the chat_id to set
     */
    public void setChat_id(final String chat_id) {
        this.chat_id = chat_id;
    }

    /**
     * @return the prompt
     */
    public String getPrompt() {
        return prompt;
    }   
    /**
     * @param prompt the prompt to set
     */
    public void setPrompt(final String prompt) {
        this.prompt = prompt;
    }
    /**
     * @return the creationDate
     */
    public long getTimestamp() {
        return timestamp;
    }
    /**
     * @param creationDate the creationDate to set
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * @return the answer
     */
    public String getAnswer() {
        return answer;
    }
    /**
     * @param answer the answer to set
     */
    public void setAnswer(final String answer) {
        this.answer = answer;
    }
    /**
     * @return the answerDate
     */
    
    @Override
    public String toString() {
        return "Conversation{" +
                "id='" + id + '\'' +
                ", chat_id='" + chat_id + '\'' +
                ", prompt='" + prompt + '\'' +
                ", creationDate=" + timestamp +
                ", answer='" + answer + '\'' +
                '}';
    }
}
