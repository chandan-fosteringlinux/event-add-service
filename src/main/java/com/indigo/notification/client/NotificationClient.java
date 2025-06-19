package com.indigo.notification.client;

import java.util.Map;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * REST client interface used to send notification data to an external service.
 * Uses MicroProfile REST client annotations.
 */
@Path("/v1/events")
@RegisterRestClient
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface NotificationClient {
    @POST
    @Path("/add")
    Response sendNotification(Map<String, Object> payload);
}