package ar.edu.ubp.das.resources;

import java.sql.SQLException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
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

	private static final String PROTOCOL_REST = "REST";
	private static final String PROTOCOL_SOAP = "SOAP";

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
	public Response insert(ServiceBean service) {
		service.setUserId((Integer) req.getProperty("id"));
		try {
			this.checkBody(service);
			this.checkPingEndpoint(service.getURLPing(), service.getProtocol());
			
			Dao<ServiceBean, Integer> dao = this.getDao();
			dao.insert(service);
			return Response.status(Status.NO_CONTENT).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@PUT
	@Secured
	@Consumes(MediaType.APPLICATION_JSON)
	public Response update(ServiceBean service) {
		service.setUserId((Integer) req.getProperty("id"));
		try {
			this.checkBody(service);
			this.checkPingEndpoint(service.getURLPing(), service.getProtocol());
			
			Dao<ServiceBean, Integer> dao = this.getDao();
			dao.update(service);
			return Response.status(Status.NO_CONTENT).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	private void checkPingEndpoint(String endpoint, String protocol) throws Exception {
		try {
			if (protocol == PROTOCOL_REST) {
				Client client = ClientBuilder.newClient();
				WebTarget webTarget = client.target(endpoint);
				Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
				Response response = invocationBuilder.get();
				if (response.getStatus() > 400) {
					throw new Exception();
				}
			} else if (protocol == PROTOCOL_SOAP) {
				// TODO: Agregar llamada al endpoint de ping por SOAP				
			}
		} catch (Exception e) {
			// Genezamos todas las excepciones que puedan saltar en una sola
			throw new Exception("Error al probar el endpoint de ping");
		}
	}

	private Dao<ServiceBean, Integer> getDao() throws SQLException {
		return DaoFactory.getDao("Services", "ar.edu.ubp.das");
	}

	private void checkBody(ServiceBean service) throws Exception {
		if (service == null ||!service.isValid()) {
			throw new Exception("Debe enviar la información requerida del servicio");
		}
	}
}
