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
import org.elasticsearch.ElasticsearchException;

import ar.edu.ubp.das.beans.ServiceBean;
import ar.edu.ubp.das.beans.WebsiteBean;
import ar.edu.ubp.das.db.Dao;
import ar.edu.ubp.das.db.DaoFactory;
import ar.edu.ubp.das.elastic.MetadataDao;
import ar.edu.ubp.das.elastic.MetadataDaoImpl;
import ar.edu.ubp.das.security.Secured;

@Path("services")
public class ServicesResource {

	private static final String PROTOCOL_REST = "REST";
	private static final String PROTOCOL_SOAP = "SOAP";
	private static final String WSDL = "?wsdl";

	private static final HttpClient MyHttpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1)
			.connectTimeout(Duration.ofSeconds(5)).build();

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
			Dao<ServiceBean, ServiceBean> dao = this.getDao();
			List<ServiceBean> services = dao.select((Integer) req.getProperty("id"));
			return Response.status(Status.OK).entity(services).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).build();
		}
	}

	@POST
	@Secured
	@Consumes(MediaType.APPLICATION_JSON)
	public Response insertService(ServiceBean service) {
		service.setUserId((Integer) req.getProperty("id"));
		try {
			this.checkBody(service);
			this.checkResource(service);
			Dao<ServiceBean, ServiceBean> dao = this.getDao();
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
	public Response updateServiceInfo(@PathParam("serviceId") Integer id, ServiceBean service) {
		try {
			this.checkBody(service);
			checkResource(service);
			service.setUserId((Integer) req.getProperty("id"));
			this.deleteServiceWebsites(id);
			Dao<ServiceBean, ServiceBean> dao = this.getDao();
			service.setServiceId(id);
			dao.update(service);
			return Response.status(Status.NO_CONTENT).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@PUT
	@Path("{serviceId}/reindex")
	@Secured
	@Consumes(MediaType.APPLICATION_JSON)
	public Response reindexService(@PathParam("serviceId") Integer id) {
		try {
			this.deleteServiceWebsites(id);
			this.getDao().update(id);;
			return Response.status(Status.NO_CONTENT).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@DELETE
	@Path("{serviceId}")
	@Secured
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deleteService(@PathParam("serviceId") Integer id) {
		try {
			this.deleteServiceWebsites(id);
			this.getDao().delete(id);
			return Response.status(Status.NO_CONTENT).build();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@POST
	@Path("test")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response testPing(ServiceBean service) {
		try {
			checkPingEndpoint(service.getURLPing(), service.getProtocol());
			return Response.status(Status.OK).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	
	private void deleteServiceWebsites(Integer serviceId) throws ElasticsearchException, Exception {
		Dao<WebsiteBean, ServiceBean> dao = DaoFactory.getDao("ServiceWebsites", "ar.edu.ubp.das");
		MetadataDao elastic = new MetadataDaoImpl();
		List<WebsiteBean> websites = dao.select(serviceId);
		for (WebsiteBean website : websites) {
			System.out.println("deleting ID:" + website.getWebsiteId());
			elastic.deleteWebsiteId(website.getWebsiteId());
		}
	}
	
	private void checkResource(ServiceBean service) throws Exception {
		String ping = service.getURLPing().toLowerCase();
		String resource = service.getURLResource().toLowerCase();
		String protocol = service.getProtocol();
		switch (protocol) {
			case PROTOCOL_REST: {
				if (ping.contains(WSDL) || resource.contains(WSDL))
					throw new Exception("El protocolo no coincide con el tipo de recurso");
				break;
			}
			case PROTOCOL_SOAP: {
				if (!ping.contains(WSDL) || !resource.contains(WSDL))
					throw new Exception("El protocolo no coincide con el tipo de recurso");
				break;
			}
		}
	}

	private void checkPingEndpoint(String endpoint, String protocol) throws Exception, BadRequestException {
		try {
			System.out.println("PROBANDO ENDPOINT " + endpoint + " (" + protocol + ")");
			if (protocol.equals(PROTOCOL_REST)) {
				HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(endpoint)).build();
				HttpResponse<String> response = MyHttpClient.send(request, HttpResponse.BodyHandlers.ofString());
				if (response.statusCode() >= 400) {
					throw new Exception();
				}
			} else if (protocol.equals(PROTOCOL_SOAP)) {
				if (!endpoint.toLowerCase().contains("?wsdl")) {
					System.out.println("not a wsdl service");
					throw new BadRequestException("El servicio no es un Servicio Web (SOAP)");
				}
				JaxWsDynamicClientFactory jdcf = JaxWsDynamicClientFactory.newInstance();
				Client client = jdcf.createClient(endpoint);
				Object res[] = client.invoke("ping");
				client.close();
				System.out.println("RESPUESTA SOAP:");
				System.out.println(res[0]);
				System.out.println("Service OK");

			}
		} catch (BadRequestException e) {
			throw e;
		} catch (Exception e) {
			// Genezamos todas las excepciones que puedan saltar en una sola
			throw new Exception("El servicio no responde");
		}
	}

	private Dao<ServiceBean, ServiceBean> getDao() throws SQLException {
		return DaoFactory.getDao("Services", "ar.edu.ubp.das");
	}

	private void checkBody(ServiceBean service) throws Exception {
		if (service == null || !service.isValid()) {
			throw new Exception("Informaci�n requerida faltante");
		}
	}
}
