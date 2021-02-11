package ar.edu.ubp.das.resources;

import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import ar.edu.ubp.das.elastic.Metadata;
import ar.edu.ubp.das.elastic.MetadataDao;
import ar.edu.ubp.das.elastic.MetadataDaoImpl;
import ar.edu.ubp.das.logging.MyLogger;
import ar.edu.ubp.das.security.Secured;

@Path("metadata")
public class MetadataResource {
	
	private MyLogger logger;

	@Context
	ContainerRequestContext req;
	
	public MetadataResource() {
		this.logger = new MyLogger(this.getClass().getSimpleName());
	}
	
	@GET
	@Path("ping")
	public Response ping() {
		this.logger.log(MyLogger.INFO, "Petición de ping exitosa");
		return Response.ok().entity("pong").build();
	}

	@GET
	@Secured
	public Response getMetadata() {
		try {
			MetadataDao elastic = new MetadataDaoImpl();
			List<Metadata> metadata = elastic.get((Integer) req.getProperty("id"));
			this.logger.log(MyLogger.INFO, "Petición de metadatos exitosa");
			return Response.status(Status.OK).entity(metadata).build();
		} catch (Exception e) {
			this.logger.log(MyLogger.ERROR, "Petición de metadatos con error: " + e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
	}
	
	@DELETE
	@Secured
	public Response deleteMetadata(Metadata metadata) {
		try {
			MetadataDao elastic = new MetadataDaoImpl();
			elastic.delete(metadata.getId());
			this.logger.log(MyLogger.INFO, "Eliminación de metadato exitosa");
			return Response.status(Status.NO_CONTENT).build();
		} catch (Exception e) {
			this.logger.log(MyLogger.ERROR, "Eliminación de metadato con error: " + e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

}
