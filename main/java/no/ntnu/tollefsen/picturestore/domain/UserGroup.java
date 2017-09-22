package no.ntnu.tollefsen.picturestore.domain;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A row in the table associates a user to a group. A normal system would 
 * probably also have a table of defined groups.
 * 
 * @author mikael
 */
@Entity
@Data @AllArgsConstructor @NoArgsConstructor
public class UserGroup implements Serializable {
    public static final String USER  = "user";
    public static final String ADMIN = "admin";

    @Id String name;
    @Id String userid;
}
