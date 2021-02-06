package ar.edu.ubp.das.resources;

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
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import ar.edu.ubp.das.beans.WebsiteBean;
import ar.edu.ubp.das.db.Dao;
import ar.edu.ubp.das.db.DaoFactory;
import ar.edu.ubp.das.security.Secured;

@Path("websites")
public class WebsitesResource {

	@Context
	ContainerRequestContext req;

	@GET
	@Path("ping")
	public Response ping() {
		return Response.status(Status.OK).entity("pong!").build();
	}

	@GET
	@Secured
	@Produces(MediaType.APPLICATION_JSON)
	public Response getWebsites() {
		try {
			Dao<WebsiteBean, WebsiteBean> dao = this.getDao();
			List<WebsiteBean> websites = dao.select((Integer) req.getProperty("id"));
			return Response.ok().entity(websites).build();
		} catch (SQLException e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@POST
	@Secured
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addWebsite(WebsiteBean website) {
		try {
			this.checkBody(website);
			this.isDomainValid(website);
			Dao<WebsiteBean, WebsiteBean> dao = this.getDao();
			website.setUserId((Integer) req.getProperty("id"));
			dao.insert(website);
			return Response.status(Status.NO_CONTENT).build();
		} catch (SQLException e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		} catch (Exception e) {
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
			return Response.status(Status.NO_CONTENT).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).build();
		}
	}

	// TODO: AGREGAR LA LOGICA PARA MEILI, HAY QUE BORRAR AHI O HACER OTRA COSA
	@PUT
	@Secured
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateWebsite(WebsiteBean website) {
		try {
			website.setUserId((Integer) req.getProperty("id"));
			this.isDomainValid(website);
			Dao<WebsiteBean, WebsiteBean> dao = this.getDao();
			dao.update(website);
			return Response.status(Status.NO_CONTENT).build();
		} catch (SQLException e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@POST
	@Path("check-domain")
	@Secured
	@Consumes(MediaType.APPLICATION_JSON)
	public Response checkDomain(WebsiteBean website) {
		try {
			website.setUserId((Integer) req.getProperty("id"));
			this.isDomainValid(website);
			return Response.status(Status.OK).build();
		} catch (SQLException e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		} catch (Exception e) {
			return Response.status(Status.CONFLICT).entity("Dominio ya registrado").build();
		}
	}

	// TODO: AGREGAR LA LOGICA PARA MEILI, HAY QUE BORRAR AHI O HACER OTRA COSA
	@DELETE
	@Secured
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deleteWebsite(WebsiteBean website) {
		try {
			Dao<WebsiteBean, WebsiteBean> dao = this.getDao();
			domainExists(website);
			dao.delete(website.getWebsiteId());
			return Response.status(Status.NO_CONTENT).build();
		} catch (SQLException e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	private void isDomainValid(WebsiteBean website) throws Exception, SQLException {
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
				throw new Exception("No se encuentró ninguna página web con ese criterio");
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
