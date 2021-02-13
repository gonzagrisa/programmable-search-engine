package ar.edu.ubp.das.resources;

import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import ar.edu.ubp.das.beans.WebsiteBean;
import ar.edu.ubp.das.db.Dao;
import ar.edu.ubp.das.db.DaoFactory;
import ar.edu.ubp.das.elastic.MetadataDao;
import ar.edu.ubp.das.elastic.MetadataDaoImpl;
import ar.edu.ubp.das.logging.MyLogger;
import ar.edu.ubp.das.security.Secured;

@Path("websites")
public class WebsitesResource {
	private MyLogger logger;

	@Context
	ContainerRequestContext req;
	
	public WebsitesResource() {
		this.logger = new MyLogger(this.getClass().getSimpleName());
	}

	@GET
	@Path("ping")
	public Response ping() {
		this.logger.log(MyLogger.INFO, "Petición de ping exitosa");
		return Response.status(Status.OK).entity("pong!").build();
	}

	@GET
	@Secured
	@Produces(MediaType.APPLICATION_JSON)
	public Response getWebsites() {
		try {
			Dao<WebsiteBean, WebsiteBean> dao = this.getDao();
			List<WebsiteBean> websites = dao.select((Integer) req.getProperty("id"));
			this.logger.log(MyLogger.INFO, "Petición de páginas exitosa");
			return Response.ok().entity(websites).build();
		} catch (SQLException e) {
			this.logger.log(MyLogger.ERROR, "Petición de páginas con error: " + e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		} catch (Exception e) {
			this.logger.log(MyLogger.ERROR, "Petición de páginas con error: " + e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@POST
	@Secured
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addWebsite(WebsiteBean website) {
		try {
			this.checkBody(website);
			this.isDomainRegistered(website);
			Dao<WebsiteBean, WebsiteBean> dao = this.getDao();
			website.setUserId((Integer) req.getProperty("id"));
			dao.insert(website);
			this.logger.log(MyLogger.INFO, "Inserción de página exitosa");
			return Response.status(Status.NO_CONTENT).build();
		} catch (SQLException e) {
			this.logger.log(MyLogger.ERROR, "Inserción de página con error: " + e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		} catch (Exception e) {
			this.logger.log(MyLogger.ERROR, "Inserción de página con error: " + e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}
	
	
	@PUT
	@Path("{id}/reindex")
	@Secured
	@Consumes(MediaType.APPLICATION_JSON)
	public Response reindexWebsite(@PathParam("id") Integer websiteId) {
		try {
			Dao<WebsiteBean, WebsiteBean> dao = this.getDao();
			dao.update(websiteId);
			MetadataDao elastic = new MetadataDaoImpl();
			elastic.deleteWebsiteId(websiteId);
			this.logger.log(MyLogger.INFO, "Petición de reindexado de página exitosa");
			return Response.status(Status.NO_CONTENT).build();
		} catch (Exception e) {
			this.logger.log(MyLogger.ERROR, "Petición de reindexado de página con error: " + e.getMessage());
			return Response.status(Status.BAD_REQUEST).build();
		}
	}

	// TODO: AGREGAR LA LOGICA PARA ELASTIC, HAY QUE BORRAR AHI O HACER OTRA COSA
	@PUT
	@Secured
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateWebsite(WebsiteBean website) {
		try {
			System.out.println("UPDATING");
			website.setUserId((Integer) req.getProperty("id"));
			isDomainRegistered(website);
			MetadataDao elastic = new MetadataDaoImpl();
			elastic.deleteWebsiteId(website.getWebsiteId());
			Dao<WebsiteBean, WebsiteBean> dao = this.getDao();
			dao.update(website);
			this.logger.log(MyLogger.INFO, "Actualización de página exitosa");
			return Response.status(Status.NO_CONTENT).build();
		} catch (SQLException e) {
			this.logger.log(MyLogger.ERROR, "Actualización de página con error: " + e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		} catch (Exception e) {
			this.logger.log(MyLogger.ERROR, "Actualización de página con error: " + e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@GET
	@Path("check")
	@Secured
	public Response pingUrl(@QueryParam("url") String url) {
		url = url.replaceFirst("^https", "http"); // Otherwise an exception may be thrown on invalid SSL certificates.
		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setConnectTimeout(5000);
			connection.setReadTimeout(5000);
			connection.setRequestMethod("HEAD");
			int responseCode = connection.getResponseCode();
			if (200 <= responseCode && responseCode <= 399) {
				return Response.status(Status.OK).build();
			} else {
				throw new Exception();
			}
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).build();
		}
	}
	
	@POST
	@Path("check-domain")
	@Secured
	@Consumes(MediaType.APPLICATION_JSON)
	public Response checkDomain(WebsiteBean website) {
		try {
			website.setUserId((Integer) req.getProperty("id"));
			this.isDomainRegistered(website);
			this.logger.log(MyLogger.INFO, "Chequeo de dominio exitoso");
			return Response.status(Status.OK).build();
		} catch (SQLException e) {
			this.logger.log(MyLogger.ERROR, "Chequeo de dominio con error: " + e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		} catch (Exception e) {
			this.logger.log(MyLogger.ERROR, "Chequeo de dominio con error: " + e.getMessage());
			return Response.status(Status.CONFLICT).entity("Dominio ya registrado").build();
		}
	}

	@DELETE
	@Secured
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deleteWebsite(WebsiteBean website) {
		try {
			Dao<WebsiteBean, WebsiteBean> dao = this.getDao();
			domainExists(website);
			MetadataDao elastic = new MetadataDaoImpl();
			elastic.deleteWebsiteId(website.getWebsiteId());
			dao.delete(website.getWebsiteId());
			this.logger.log(MyLogger.INFO, "Eliminacion de pagina exitosa");
			return Response.status(Status.NO_CONTENT).build();
		} catch (SQLException e) {
			this.logger.log(MyLogger.ERROR, "Eliminación de página con error: " + e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		} catch (Exception e) {
			this.logger.log(MyLogger.ERROR, "Eliminación de página con error: " + e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	private void isDomainRegistered(WebsiteBean website) throws Exception, SQLException {
		try {
			Dao<WebsiteBean, WebsiteBean> dao = this.getDao();
			if (dao.select(website).size() > 0) {
				throw new Exception("Dominio ya registrado");
			}
		} catch (SQLException e) {
			throw e;
		}
	}
	
	private void domainExists(WebsiteBean website) throws Exception, SQLException {
		try {
			Dao<WebsiteBean, WebsiteBean> dao = this.getDao();
			if (dao.find(website.getWebsiteId()) == null) {
				throw new Exception("No se encuentr� ninguna p�gina web con ese criterio");
			}
		} catch (SQLException e) {
			throw e;
		}
	}

	private Dao<WebsiteBean, WebsiteBean> getDao() throws SQLException {
		return DaoFactory.getDao("Websites", "ar.edu.ubp.das");
	}

	private void checkBody(WebsiteBean web) throws Exception {
		try {
			if (web != null) {
				web.setUserId((Integer) req.getProperty("id"));
			} else {
				throw new Exception();
			}
			if (!web.isValid()) {
				throw new Exception();
			}
		} catch (Exception e) {
			throw new Exception("Debe enviar la información requerida de la página");
		}
	}
}
