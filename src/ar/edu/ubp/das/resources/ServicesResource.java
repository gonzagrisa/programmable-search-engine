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
import javax.ws.rs.QueryParam;
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
import ar.edu.ubp.das.logging.MyLogger;
import ar.edu.ubp.das.security.Secured;

@Path("services")
public class ServicesResource {

	private static final String PROTOCOL_REST = "REST";
	private static final String PROTOCOL_SOAP = "SOAP";
	private static final String WSDL = "?wsdl";
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
			Dao<ServiceBean, ServiceBean> dao = this.getDao();
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
	public Response insertService(ServiceBean service) {
		service.setUserId((Integer) req.getProperty("id"));
		try {
			this.checkBody(service);
			this.checkResource(service);
			Dao<ServiceBean, ServiceBean> dao = this.getDao();
			dao.insert(service);
			this.logger.log(MyLogger.INFO, "Inserción de servicio exitosa");
			return Response.status(Status.NO_CONTENT).build();
		} catch (Exception e) {
			this.logger.log(
				MyLogger.ERROR,
				"Inserción de servicio con error: " + e.getMessage()
			);
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
			this.deleteServiceWebsites(service);
			Dao<ServiceBean, ServiceBean> dao = this.getDao();
			System.out.println("ACTUALIZANDO SERVICIO");
			service.setServiceId(id);
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
	@Path("reindex")
	@Secured
	@Consumes(MediaType.APPLICATION_JSON)
	public Response reindexService(ServiceBean service) {
		try {
			this.deleteServiceWebsites(service);
			this.getDao().update(service.getServiceId());
			this.logger.log(MyLogger.INFO, "Petición de reindexado para el servicio #" + service.getServiceId() + " exitosa");
			return Response.status(Status.NO_CONTENT).build();
		} catch (Exception e) {
			this.logger.log(
				MyLogger.ERROR,
				"Petición de reindexado para el servicio #" + service.getServiceId() + " con error: " + e.getMessage()
			);
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@DELETE
	@Path("{serviceId}")
	@Secured
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deleteService(ServiceBean service, @PathParam("serviceId") Integer id, @QueryParam("keepWebsites") Boolean keepWebsites) {
		try {
			if (keepWebsites) {
				this.unlinkWebsites(service);
			} else {
				this.deleteServiceWebsites(service);
			}
			this.getDao().delete(id);
			this.logger.log(MyLogger.INFO, "Eliminacion de servicio #" + id + " exitosa");
			return Response.status(Status.NO_CONTENT).build();
		} catch (Exception e) {
			this.logger.log(
				MyLogger.ERROR,
				"Eliminacion del servicio #" + id + " con error: " + e.getMessage()
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

	
	private void unlinkWebsites(ServiceBean service) throws SQLException {
		Dao<WebsiteBean, ServiceBean> serviceWebsiteDao = DaoFactory.getDao("ServiceWebsites", "ar.edu.ubp.das");
		Dao<WebsiteBean, WebsiteBean> websiteDao = DaoFactory.getDao("Websites", "ar.edu.ubp.das");
		List<WebsiteBean> websites = serviceWebsiteDao.select(service.getServiceId());
		for (WebsiteBean website : websites) {
			websiteDao.update(website, null);
		}
	}
	
	private void deleteServiceWebsites(ServiceBean service) throws ElasticsearchException, Exception {
		Dao<WebsiteBean, ServiceBean> serviceWebsiteDao = DaoFactory.getDao("ServiceWebsites", "ar.edu.ubp.das");
		Dao<WebsiteBean, WebsiteBean> websiteDao = DaoFactory.getDao("Websites", "ar.edu.ubp.das");
		MetadataDao elastic = new MetadataDaoImpl();
		List<WebsiteBean> websites = serviceWebsiteDao.select(service);
		for (WebsiteBean website : websites) {
			websiteDao.delete(website.getWebsiteId());
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
			this.logger.log(MyLogger.INFO, "Probando endpoint " + endpoint + " (" + protocol + ")");
			if (protocol.equals(PROTOCOL_REST)) {
				HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(endpoint)).build();
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

	private Dao<ServiceBean, ServiceBean> getDao() throws SQLException {
		return DaoFactory.getDao("Services", "ar.edu.ubp.das");
	}

	private void checkBody(ServiceBean service) throws Exception {
		if (service == null || !service.isValid()) {
			throw new Exception("Información requerida faltante");
		}
	}
}
