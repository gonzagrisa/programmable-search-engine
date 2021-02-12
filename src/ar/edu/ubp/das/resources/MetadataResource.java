package ar.edu.ubp.das.resources;

import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import ar.edu.ubp.das.elastic.Metadata;
import ar.edu.ubp.das.elastic.MetadataDao;
import ar.edu.ubp.das.elastic.MetadataDaoImpl;
import ar.edu.ubp.das.security.Secured;

@Path("metadata")
public class MetadataResource {

	@Context
	ContainerRequestContext req;

	@GET
	@Secured
	public Response getMetadata() {
		try {
			MetadataDao elastic = new MetadataDaoImpl();
			List<Metadata> metadata = elastic.get((Integer) req.getProperty("id"));
			return Response.status(Status.OK).entity(metadata).build();
		} catch (Exception e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
	}

	@DELETE
	@Secured
	@Path("{id}")
	public Response deleteMetadata(@PathParam("id") String id) {
		try {
			MetadataDao elastic = new MetadataDaoImpl();
			elastic.delete(id);
			return Response.status(Status.NO_CONTENT).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@PUT
	@Secured
	public Response updateMetadata(Metadata metadata) {
		try {
			MetadataDao elastic = new MetadataDaoImpl();
			elastic.update(metadata);
			return Response.status(Status.NO_CONTENT).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

}
