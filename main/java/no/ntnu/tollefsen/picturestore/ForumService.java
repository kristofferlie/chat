package no.ntnu.tollefsen.picturestore;

import java.util.Collections;
import java.util.List;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import no.ntnu.tollefsen.picturestore.domain.Conversation;
import no.ntnu.tollefsen.picturestore.domain.Message;
import no.ntnu.tollefsen.picturestore.domain.SecureUser;

/**
 * A JAX-RS REST service that is also a stateless EJB object. Creates and lists
 * messages.
 * 
 * @author mikael
 */
@Stateless
@Path("messages")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ForumService {   
    @PersistenceContext
    EntityManager em;
    
    /**
     * Returns a list of messages associated with the named Conversation
     * 
     * @param name name of Conversation
     * @return list of Message objects
     */
    @GET
    public List<Message> getMessages(@QueryParam("name") String name) {
        List<Message> result = null;
        if(name != null) {
            result = em.createQuery("SELECT m FROM Message m WHERE m.conversation.id = :id", 
                    Message.class)
                .setParameter("id", name)
                .getResultList();
        }
        
        return result != null ? result : Collections.EMPTY_LIST;
    }

    
    
    /**
     * Returns a list of Conversation objects
     * 
     * @return all converations 
     */
    @GET
    @Path("conversations")
    public List<Conversation> getConversations() {
        return em.createNamedQuery(Conversation.QUERY_FINDALL,Conversation.class)
                 .getResultList();
    }
    
    

    /**
     * Inserts a new Message into the database.
     * 
     * @param sc provides some security information 
     * @param name id of Conversation.
     * @param message a new Message to be inserted into the database
     * 
     * @return returns the message with a generated id if the named Conversation
     *         exists.
     */
    @POST
    @Path("add")
    @RolesAllowed({"user"})
    public Response addMessage(@Context SecurityContext sc,
                               @QueryParam("name")String name, Message message) {
        // Returns no-content if name of Conversation not provided
        if(name != null) {
            SecureUser user = em.find(SecureUser.class, sc.getUserPrincipal().getName());
            if(user == null) {
                throw new IllegalArgumentException("Unknown user " + sc.getUserPrincipal());
            }

            // Set user on message
            message.setUser(user);
            
            // Finds Conversation in database. Creates a new one if none is found.
            Conversation c = em.find(Conversation.class, name);
            if(c == null) {
                c = new Conversation(name);
                // Inserts a new Conversaiont into the database
                em.persist(c);
            }
            
            // Associates a Conversation object with the message
            message.setConversation(c);
            
            // Inserts the Message object into the database
            em.persist(message);

            return Response.ok(message).build();
        } else {
            return Response.noContent().build();
        }
    }
}
