package no.ntnu.tollefsen.picturestore;

import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

/**
 * JAX-RS configuration class. Registers the multipart post handler and
 * registers the three JAX-RS REST interfaces.
 * 
 * @author mikael
 */
@ApplicationPath("api")
public class RestConfig extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new HashSet<>();
        resources.add(MultiPartFeature.class);
        addRestResourceClasses(resources);
        return resources;
    }
    
    private void addRestResourceClasses(Set<Class<?>> resources) {
        resources.add(no.ntnu.tollefsen.picturestore.AuthService.class);
        resources.add(no.ntnu.tollefsen.picturestore.ForumService.class);
        resources.add(no.ntnu.tollefsen.picturestore.PictureService.class);
    }
}
