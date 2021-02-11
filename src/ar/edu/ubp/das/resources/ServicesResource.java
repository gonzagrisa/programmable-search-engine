package ar.edu.ubp.das.resources;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.time.Duration;
import java.util.List;

import javax.ws.rs.BadRequestException;
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
import ar.edu.ubp.das.logging.MyLogger;
import ar.edu.ubp.das.security.Secured;

@Path("services")
public class ServicesResource {

	private static final String PROTOCOL_REST = "REST";
	private static final String PROTOCOL_SOAP = "SOAP";
	private  HttpClient MyHttpClient;
	private MyLogger logger;

	@Context
	ContainerRequestContext req;
	
	public ServicesResource() {
		this.MyHttpClient = HttpClient.newBuilder()
	            .version(HttpClient.Version.HTTP_1_1)
	            .connectTimeout(Duration.ofSeconds(5))
	            .build();
		this.logger = new MyLogger(this.getClass().getSimpleName());
	}

	@GET
	@Path("ping")
	public Response ping() {
		this.logger.log(MyLogger.INFO, "Petición de ping exitosa");
		return Response.status(Status.OK).entity("pong").build();
	}

	@GET
	@Secured
	public Response getServices() {
		try {
			Dao<ServiceBean, Integer> dao = this.getDao();
			List<ServiceBean> services = dao.select((Integer) req.getProperty("id"));
			this.logger.log(MyLogger.INFO, "Petición de servicios exitosa");
			return Response.status(Status.OK).entity(services).build();
		} catch (Exception e) {
			this.logger.log(MyLogger.ERROR, "Petición de servicios con error: " + e.getMessage());
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
			this.logger.log(MyLogger.INFO, "Inserción de servicio #" + service.getServiceId() + " exitosa");
			return Response.status(Status.NO_CONTENT).build();
		} catch (Exception e) {
			this.logger.log(
				MyLogger.ERROR,
				"Inserción de servicio #" + service.getServiceId() + " con error: " + e.getMessage()
			);
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
			this.logger.log(MyLogger.INFO, "Actualización de servicio #" + service.getServiceId() + " exitosa");
			return Response.status(Status.NO_CONTENT).build();
		} catch (Exception e) {
			this.logger.log(
				MyLogger.ERROR,
				"Actualización de servicio #" + service.getServiceId() + " con error: " + e.getMessage()
			);
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}
	
	@PUT
	@Path("{serviceId}/reindex")
	@Secured
	@Consumes(MediaType.APPLICATION_JSON)
	public Response reindexService(@PathParam("serviceId") Integer id) {
		try {
			Dao<ServiceBean, Integer> dao = this.getDao();
			dao.update(id);
			this.logger.log(MyLogger.INFO, "Petición de reindexado para el servicio #" + id + " exitosa");
			return Response.status(Status.NO_CONTENT).build();
		} catch (Exception e) {
			this.logger.log(
				MyLogger.ERROR,
				"Petición de reindexado para el servicio #" + id + " con error: " + e.getMessage()
			);
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
			this.logger.log(MyLogger.INFO, "Eliminación de servicio #" + id + " exitosa");
			return Response.status(Status.NO_CONTENT).build();
		} catch (Exception e) {
			this.logger.log(
				MyLogger.ERROR,
				"Eliminación del servicio #" + id + " con error: " + e.getMessage()
			);
			return Response.status(Status.BAD_REQUEST).build();
		}
	}
	
	@POST
	@Path("test")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response testPing(ServiceBean service) {
		try {
			this.logger.log(MyLogger.INFO, "Petición manual de chequeo de ping");
			checkPingEndpoint(service.getURLPing(), service.getProtocol());
			return Response.status(Status.OK).build();
		} catch (Exception e) {
			this.logger.log(MyLogger.ERROR, "Petición manual de chequeo de ping con error: " + e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	private void checkPingEndpoint(String endpoint, String protocol) throws Exception, BadRequestException {
		try {
			this.logger.log(MyLogger.INFO, "Probando endpoint " + endpoint + " (" + protocol + ")");
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
				if (!endpoint.toLowerCase().contains("?wsdl")){
					throw new BadRequestException("El servicio no es un Servicio Web (SOAP)");
				}
				JaxWsDynamicClientFactory jdcf = JaxWsDynamicClientFactory.newInstance();
				Client client = jdcf.createClient(endpoint);
				Object res[] = client.invoke("ping");
				client.close();
				System.out.println(res[0]);
				System.out.println("Service OK");
			}
		} catch (BadRequestException e) {
			throw e;
		} catch (Exception e) {
			// Generalizamos todas las excepciones que puedan saltar en una sola
			throw new Exception("El servicio no responde");
		}
	}

	private Dao<ServiceBean, Integer> getDao() throws SQLException {
		return DaoFactory.getDao("Services", "ar.edu.ubp.das");
	}

	private void checkBody(ServiceBean service) throws Exception {
		if (service == null || !service.isValid()) {
			throw new Exception("Información requerida faltante");
		}
	}
}
