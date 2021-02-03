package ar.edu.ubp.das.resources;

import java.sql.SQLException;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
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
			Dao<WebsiteBean, Integer> dao = DaoFactory.getDao("Websites", "ar.edu.ubp.das");
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

			Dao<WebsiteBean, Integer> dao = DaoFactory.getDao("Websites", "ar.edu.ubp.das");
			dao.insert(website);
			return Response.status(Status.NO_CONTENT).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}
	
	@DELETE
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deleteWebsite() {
		return Response.ok().build();
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
