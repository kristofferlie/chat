package no.ntnu.tollefsen.picturestore;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.security.DeclareRoles;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import no.ntnu.tollefsen.picturestore.domain.SecureUser;
import no.ntnu.tollefsen.picturestore.domain.UserGroup;

/**
 * Authentication REST service used for login, logout and to register new users
 *
 * @Path("auth) makes this class into a JAX-RS REST service. "auth" specifies 
 * that the URL of this service would begin with "domainname/pstore/api/auth"
 * depending on the domain, context path of project and the JAX-RS base configuration
 * @Produces(MediaType.APPLICATION_JSON) instructs JAX-RS that the default result 
 * of a method is to be mashalled as JSON
 * 
 * @Stateless makes this class into a transactional stateless EJB, which is a 
 * requirement of using the JPA EntityManager to communicate with the database.
 * 
 * @DeclareRoles({UserGroup.ADMIN,UserGroup.USER}) specifies the roles used in
 * this EJB.
 * 
 * @author mikael
 */
@Path("auth")
@Produces(MediaType.APPLICATION_JSON)
@Stateless
@DeclareRoles({UserGroup.ADMIN,UserGroup.USER})
public class AuthService {
    /** 
     * The application server will inject a EntityManager as a way to communicate 
     * with the database.
     */
    @PersistenceContext
    EntityManager em;

    
    /**
     *  This method is marked in web.xml as having a security constraint, 
     *  which makes the jdbc authentication module intercept the call and do
     *  a BASIC authentication. This method will not be called if the user is
     *  not authenticated.
     * 
     * @param sc - provides access to security related information
     * @param request - represents the HTTP request
     * @return A JSON objects containing the userid and authorization information. 
     *         This might leak more internal information then you want. Just for
     *         demonstration purposes.
     */
    @GET
    @Path("login")
    public Response login(@Context SecurityContext sc,
                          @Context HttpServletRequest request) {
        /* Creates a JSESSIONID cookie. This enables us to call other methods
           without resending the users credentials */
        request.getSession(true); 
        
        return Response.ok(getSecureRole(sc, request)).build();
    }

    
    /**
     * Logs out the user from the Java EE authroization system. Manually removes
     * the JSESSIONID cookie.
     * 
     * @param request the HTTP request
     * @return an empty response
     */
    @GET
    @Path("logout")
    public Response logout(@Context HttpServletRequest request) {
        try {
            request.logout();
        } catch (ServletException ex) {
            Logger.getLogger(AuthService.class.getName()).log(Level.SEVERE, null, ex);
        }

        return Response.ok().cookie((NewCookie[])null).build();
    }

    
    /**
     * Does an insert into the SECUREUSER and USERGROUP tables. It creates
     * a SHA-256 hash of the password and Base64 encodes it before the user is
     * created in the database. The authentication system will read the 
     * SECUREUSER table when doing an authentication.
     * 
     * @param uid
     * @param pwd
     * @return 
     */
    @GET @Path("create")
    //@RolesAllowed(UserGroup.ADMIN)
    public SecureUser createUser(@QueryParam("uid") String uid, @QueryParam("pwd") String pwd) {
        SecureUser result = null;
        try {
            // Gets the UTF-8 byte array and create a SHA-256 hash
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(pwd.getBytes("UTF-8"));
            
            // Creates a new SecureUser object with a Base64 encoded version of the hashed password
            result = new SecureUser(uid, Base64.getEncoder().encodeToString(hash));
            
            // Inserts SecureUser into the database
            em.persist(result);
            
            // Inserts UserGroup into the database.
            em.persist(new UserGroup(UserGroup.USER,uid));
        } catch(UnsupportedEncodingException | NoSuchAlgorithmException e) {
            Logger.getLogger(AuthService.class.getName()).log(Level.WARNING, "message",e);
        }

        return result;
    }


    /**
     * Extract some security information about the user and encode it in a 
     * JsonObject to be returned to the user.
     * 
     * @param sc provides access to security related information
     * @param request the HTTP request
     * @return Security related information about the user
     */
    @GET @Path("status")
    public JsonObject getSecureRole(@Context SecurityContext sc,
                                    @Context HttpServletRequest request) {
        JsonArrayBuilder cookies = Json.createArrayBuilder();
        int length = request.getCookies() != null ? request.getCookies().length : 0;
        for(int i = 0; i < length; i++) {
            Cookie c = request.getCookies()[i];
            cookies.add(Json.createObjectBuilder()
               .add("name", c.getName())
               .add("value", c.getValue())
               .add("maxAge", c.getMaxAge())
               .add("secure", c.getSecure())
               .add("httpOnly", c.isHttpOnly())
               .add("version", c.getVersion())
            );
        }

        Principal user = request.getUserPrincipal();
        String authScheme = sc.getAuthenticationScheme() != null ? sc.getAuthenticationScheme() : "null";
        return Json.createObjectBuilder()
                .add("userid", user != null ? user.getName() : "not logged in")
                .add("authScheme", authScheme)
                .add("admin",  Boolean.toString(sc.isUserInRole("admin")))
                .add("user",  Boolean.toString(sc.isUserInRole("user")))
                .add("cookies",cookies)
                .build();
    }
}
