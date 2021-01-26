package ar.edu.ubp.das.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.client.ClientConfig;

import ar.edu.ubp.das.beans.ServiceBean;
import ar.edu.ubp.das.db.Dao;
import ar.edu.ubp.das.db.DaoFactory;
import ar.edu.ubp.das.security.Secured;

@Path("services")
public class ServicesResource {
	
	@Context
	ContainerRequestContext req;
	
	@GET
	@Path("ping")
	public Response ping() {
		return Response.status(Status.OK).entity("pong").build();
	}
	
//	@GET
//	public Response getServices() {
//		return Response.ok().build();
//	}
	
	@POST
	@Secured
	@Consumes(MediaType.APPLICATION_JSON)
	public Response update(ServiceBean service) {
		try {
			this.checkPingEndpoint(service.getURLPing());
			
			Dao<ServiceBean, Integer> dao = DaoFactory.getDao("Services", "ar.edu.ubp.das");
			service.setUserId((Integer) req.getProperty("id"));
			dao.insert(service);
			return Response.status(Status.NO_CONTENT).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}
	
	private void checkPingEndpoint(String endpoint) throws Exception {
		try {
			Client client = ClientBuilder.newClient();
			WebTarget webTarget = client.target(endpoint);
			Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
			Response response = invocationBuilder.get();
			if (response.getStatus() > 400) {
				throw new Exception();
			}
		} catch (Exception e) {
			// Genezamos todas las excepciones que puedan saltar en una sola
			throw new Exception("Error al probar el endpoint de ping");
		}
		
		
	}
}
