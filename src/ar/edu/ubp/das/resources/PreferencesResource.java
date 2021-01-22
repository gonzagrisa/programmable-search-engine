package ar.edu.ubp.das.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import ar.edu.ubp.das.beans.PreferencesBean;

@Path("settings")
public class PreferencesResource {
	
	@GET
	@Path("ping")
	public Response ping() {
		return Response.ok().entity("pong").build();
	}
	
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public Response update(PreferencesBean preferences) {
		return Response.status(Status.BAD_REQUEST).build();
	}
}
