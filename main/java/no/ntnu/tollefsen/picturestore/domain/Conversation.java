package no.ntnu.tollefsen.picturestore.domain;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Version;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JPA Entity used to group a set of Message objects together. .
 * 
 * The class is annotated with the JAXB @XmlRootElement to enable serialization 
 * to XML or JSON. @XMLAccessorType(XmlAccessType.FIELD) instructs JAXB to use 
 * the name of the fields as basis of mapping to XML or JSON.
 * 
 * @Data and @NoArgsConstructor from project-lombok will instruct the compiler
 * to add getters and setters in addition to hashCode and equals
 * 
 * @author mikael
 */
@Data @NoArgsConstructor
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Entity
@NamedQueries({
    @NamedQuery(name = Conversation.QUERY_FINDALL, query = "SELECT c FROM Conversation c")
})
public class Conversation implements Serializable {
    public static final String QUERY_FINDALL = "findAll";
    
    /** @Id instructs JPA that id is the primary key */
    @Id
    String id;
    
    /** 
     * JAXB: @XmlTransient instructs JAXB to ignore this field 
     * JPA: @OneToMany ONE conversation may have MANY associated Message objects
     *      mappedBy refers to the Message.conversation field. 
     *      cascade = CascadeType.ALL makes all CRUD opperation on Conversation 
     *      also apply to messages.
     *      fetch = FetchType.LAZY instructs JPA to delay the fetching of the
     *      aggregated Message objects until the List is used.
     */
    @XmlTransient
    @OneToMany(mappedBy = "conversation",cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    List<Message> messages;

    /**
     * @Version allows the JPA engine to use optimistic locking in the database.
     * JPA will update the timestamp on instert and update requests
     */
    @Version
    Timestamp version;

    public Conversation(String id) {
        this.id = id;
    }
    
    public List<Message> getMessages() {
        if(messages == null) {
            messages = new ArrayList<>();
        }
        
        return messages;
    }
}
