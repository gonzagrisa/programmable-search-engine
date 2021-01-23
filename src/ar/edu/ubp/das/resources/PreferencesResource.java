package ar.edu.ubp.das.resources;

import java.sql.SQLException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import ar.edu.ubp.das.beans.PreferencesBean;
import ar.edu.ubp.das.db.Dao;
import ar.edu.ubp.das.db.DaoFactory;
import ar.edu.ubp.das.security.Secured;

@Path("settings")
public class PreferencesResource {

	@Context
	ContainerRequestContext req;

	@GET
	@Path("ping")
	public Response ping() {
		return Response.ok().entity("pong").build();
	}

	@GET
	@Secured
	public Response getPreferences() {
		try {
			Dao<PreferencesBean, PreferencesBean> dao = DaoFactory.getDao("Preferences", "ar.edu.ubp.das");
			return Response.ok().entity(dao.find((Integer) req.getProperty("id"))).build();
		} catch (SQLException e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
	}

	@PUT
	@Secured
	@Consumes(MediaType.APPLICATION_JSON)
	public Response update(PreferencesBean preferences) {
		try {
			Dao<PreferencesBean, Integer> dao = DaoFactory.getDao("Preferences", "ar.edu.ubp.das");
			preferences.setUserId((Integer) req.getProperty("id"));
			dao.update(preferences);
			return Response.status(Status.NO_CONTENT).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).build();
		}
	}
}
