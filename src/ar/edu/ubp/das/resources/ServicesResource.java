package ar.edu.ubp.das.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("services")
public class ServicesResource {
	
	@GET
	public Response getServices() {
		return Response.ok().build();
	}
}
