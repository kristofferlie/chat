package no.ntnu.tollefsen.picturestore.domain;

import java.io.Serializable;
import java.sql.Timestamp;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Version;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A Message is associated with a user and a conversation. Used to represent
 * a message from a user.
 * 
 * @author mikael
 */
@Data @NoArgsConstructor
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Entity
public class Message implements Serializable {
    /**
     * @Id specifies the id field as the primary key
     * @GeneratedValue instructs the database that we excpect it to generate
     * a unique value.
     */
    @Id @GeneratedValue
    Long id;
        
    /**
     * @Column overrides the default column name to userid
     * @ManyToOne(optional = false) specifies that user is a required field
     */
    @JoinColumn(name = "userid")
    @ManyToOne(optional = false)
    SecureUser user;
    
    String text;

    @XmlElement(nillable=true)
    @Version
    Timestamp version;
    
    @XmlTransient
    @ManyToOne(optional = false,cascade = CascadeType.PERSIST,fetch = FetchType.LAZY)
    Conversation conversation;

    public Message(SecureUser user, String text) {
        this.user = user;
        this.text = text;
    }
}
