package client;

import java.util.Map;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/v1/events")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RegisterRestClient // Register this interface as a REST client
public interface NotificationClient {
    @POST
    @Path("/add")
    Response sendNotification(Map<String, Object> payload);
}