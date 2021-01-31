package ar.edu.ubp.das.resources;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.time.Duration;

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

import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory;

import ar.edu.ubp.das.beans.ServiceBean;
import ar.edu.ubp.das.db.Dao;
import ar.edu.ubp.das.db.DaoFactory;
import ar.edu.ubp.das.security.Secured;
import io.jsonwebtoken.io.IOException;

@Path("services")
public class ServicesResource {

	private static final String PROTOCOL_REST = "REST";
	private static final String PROTOCOL_SOAP = "SOAP";
	
	private static final HttpClient MyHttpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(5))
            .build();

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
			System.out.println("PROBANDO ENDPOINT " + endpoint + " (" + protocol + ")");
			if (protocol.equals(PROTOCOL_REST)) {
				HttpRequest request = HttpRequest.newBuilder()
						.GET()
						.uri(URI.create(endpoint))
						.build();
				HttpResponse<String> response = null;
				response = MyHttpClient.send(request, HttpResponse.BodyHandlers.ofString());
				System.out.println("RESPUESTA REST");
				System.out.println(response.body());
				if (response.statusCode() >= 400) {
					throw new Exception();
				}
			} else if (protocol.equals(PROTOCOL_SOAP)) {
				JaxWsDynamicClientFactory jdcf = JaxWsDynamicClientFactory.newInstance();
				org.apache.cxf.endpoint.Client client = jdcf.createClient(endpoint);
				Object obj[] = client.invoke("getList");
				client.close();
				System.out.println("RESPUESTA SOAP:");
				System.out.println(obj[0]);		
			}
		} catch (Exception e) {
			// Genezamos todas las excepciones que puedan saltar en una sola
			throw new Exception("Error al impactar el endpoint de ping del servicio");
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
