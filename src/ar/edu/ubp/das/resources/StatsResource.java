package ar.edu.ubp.das.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@Path("stats")
public class StatsResource {
	
	@GET
	@Path("quantity")
	public Response getQuantityStats() {
		try {
			
			return Response.status(Status.OK).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).build();
		}
	}
	
	@GET
	@Path("queries")
	public Response getQueries() {
		
	}
	
	@GET
	@Path("queries/day")
	public Response getQueriesByDay() {
		
	}
	
	@GET
	@Path("words")
	public Response getWords() {
		
	}
	
}
