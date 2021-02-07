package ar.edu.ubp.das.resources;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.time.Duration;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory;

import ar.edu.ubp.das.beans.ServiceBean;
import ar.edu.ubp.das.db.Dao;
import ar.edu.ubp.das.db.DaoFactory;
import ar.edu.ubp.das.security.Secured;

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

	@GET
	@Secured
	public Response getServices() {
		try {
			Dao<ServiceBean, Integer> dao = this.getDao();
			List<ServiceBean> services = dao.select((Integer) req.getProperty("id"));
			return Response.status(Status.OK).entity(services).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).build();
		}
	}

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
	@Path("{serviceId}")
	@Secured
	@Consumes(MediaType.APPLICATION_JSON)
	public Response update(@PathParam("serviceId") Integer id, ServiceBean service) {
		// TODO: ver que hacer con las paginas que salieron del servicio
		try {
			this.checkBody(service);
			service.setUserId((Integer) req.getProperty("id"));
			this.checkPingEndpoint(service.getURLPing(), service.getProtocol());
			Dao<ServiceBean, Integer> dao = this.getDao();
			service.setServiceId(id);
			System.out.println(service);
			dao.update(service);
			return Response.status(Status.NO_CONTENT).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@DELETE
	@Path("{serviceId}")
	@Secured
	@Consumes(MediaType.APPLICATION_JSON)
	public Response delete(@PathParam("serviceId") Integer id) {
		// TODO: ver que hacer con las paginas que salieron del servicio
		try {
			Dao<ServiceBean, Integer> dao = this.getDao();
			dao.delete(id);
			return Response.status(Status.NO_CONTENT).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).build();
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
				HttpResponse<String> response = MyHttpClient.send(request, HttpResponse.BodyHandlers.ofString());
				if (response.statusCode() >= 400) {
					throw new Exception();
				}
			} else if (protocol.equals(PROTOCOL_SOAP)) {
				JaxWsDynamicClientFactory jdcf = JaxWsDynamicClientFactory.newInstance();
				Client client = jdcf.createClient(endpoint);
				Object res[] = client.invoke("ping");
				System.out.println("RESPUESTA SOAP:");
				System.out.println(res[0]);
				System.out.println("Service OK");
				client.close();
			}
		} catch (Exception e) {
			// Genezamos todas las excepciones que puedan saltar en una sola
			throw new Exception("El servicio no responde.");
		}
	}

	private Dao<ServiceBean, Integer> getDao() throws SQLException {
		return DaoFactory.getDao("Services", "ar.edu.ubp.das");
	}

	private void checkBody(ServiceBean service) throws Exception {
		if (service == null || !service.isValid()) {
			throw new Exception("Informaci�n requerida faltante");
		}
	}
}
