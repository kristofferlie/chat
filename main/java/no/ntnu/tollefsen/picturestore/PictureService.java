package no.ntnu.tollefsen.picturestore;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import net.coobird.thumbnailator.Thumbnails;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

/**
 * Enables storing and retrieving of images.
 * 
 * @author mikael
 */
@Path("store")
@Produces(MediaType.APPLICATION_JSON)
public class PictureService {
    // Formats the date the way JavaScripts likes it
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    
    /**
     * A Simple REST method to demonstrate some JAX-RS features. Uses both
     * @QueryParam and @PathParam methods of sending parameters.
     * 
     * @GET this method will respond to a HTTP GET request
     * @Path absolute path will be '{dns-name}/pstore/api/store/hello/{gender}
     * @Produces(MediaType.APPLICATION_JSON) output will be JSON
     * 
     * @param name name of user
     * @param gender the gender of the user
     * @return a friendly reply
     */
    @GET
    @Path("hello/{gender}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response helloWorld(@QueryParam("name") String name, 
                               @PathParam("gender") String gender) {
        JsonObject result = Json.createObjectBuilder()
            .add("message", "Hello," + name + "! " + gender)
            .add("sub", Json.createObjectBuilder()
                    .add("subattrib", "sub"))
            .add("time",format.format(new Date()))
            .build();
        return Response.ok(result).build();
    } 
    
    
    
    /**
     * Returns an array of images as JSON objects (meta-data, not pixels)
     * 
     * @return array of image meta-data
     */
    @GET
    @Path("images")
    public Response getImages() {
        JsonArrayBuilder builder = Json.createArrayBuilder();
        
        try {
            Files.list(Paths.get("images"))
                 .filter(Files::isRegularFile)
                 .map(java.nio.file.Path::toFile)
                 .forEach((File f) -> {
                     builder.add(Json.createObjectBuilder()
                        .add("name", f.getName())
                        .add("size", f.length())
                        .add("date", format.format(new Date(f.lastModified())))
                     );
                 });
        } catch(IOException e) {            
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
        
        return Response.ok(builder.build()).build();
    }
    
    
    
    /**
     * Streams an image to the browser(the actual compressed pixels). The image
     * will be scaled to the appropriate with if the with parameter is provided.
     *
     * @param name the filename of the image
     * @param width the required scaled with of the image
     * 
     * @return the image in original format or in jpeg if scaled
     */
    @GET
    @Path("{name}")
    @Produces("image/jpeg")
    public Response getImage(@PathParam("name") String name, 
                             @QueryParam("width") int width) {
        StreamingOutput result = (OutputStream os) -> {
            java.nio.file.Path image = Paths.get("images",name);
            if(width == 0) {
                Files.copy(image, os);
                os.flush();
            } else {
                Thumbnails.of(image.toFile())
                          .size(width, width)
                          .outputFormat("jpeg")
                          .toOutputStream(os);
            }
        };
        
        // Ask the browser to cache the image for 24 hours
        CacheControl cc = new CacheControl();
        cc.setMaxAge(86400);
        cc.setPrivate(true);
        
        return Response.ok(result).cacheControl(cc).build();
    }
    
    
    
    /**
     * Uploads an file (image) to the application server. Accepts a 
     * multipart-post request
     * 
     * @param is The InputStream of the data
     * @param details Metadata
     *
     * @return HTTP ok if all is ok
     */
    @POST
    @Path("upload")
    @Consumes({MediaType.MULTIPART_FORM_DATA})
    public Response upload(
        @FormDataParam("file")InputStream is,
        @FormDataParam("file")FormDataContentDisposition details) {
        
        try {
            Files.copy(is, Paths.get("images",details.getFileName()));
        } catch (IOException ex) {
            Logger.getLogger(PictureService.class.getName()).log(Level.SEVERE, null, ex);
            return Response.serverError().build();
        }
        
        return Response.ok().build();
    }
}




