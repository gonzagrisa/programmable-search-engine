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

import ar.edu.ubp.das.beans.indexation.MetadataBean;
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
			List<MetadataBean> metadata = elastic.get((Integer) req.getProperty("id"), false);
			this.logger.log(MyLogger.INFO, "Petición de metadatos exitosa");
			return Response.status(Status.OK).entity(metadata).build();
		} catch (Exception e) {
			this.logger.log(MyLogger.ERROR, "Petición de metadatos con error: " + e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
	}
	
	@GET
	@Path("indexed")
	@Secured
	public Response getMetadataIndexed() {
		try {
			MetadataDao elastic = new MetadataDaoImpl();
			List<MetadataBean> metadata = elastic.get((Integer) req.getProperty("id"), true);
			this.logger.log(MyLogger.INFO, "Petición de metadatos exitosa");
			return Response.status(Status.OK).entity(metadata).build();
		} catch (Exception e) {
			this.logger.log(MyLogger.ERROR, "Petición de metadatos con error: " + e.getMessage());
			if (e.getMessage().equals("Connection refused"))
				return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Error al conectarse a la base de datos").build();
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
			this.logger.log(MyLogger.INFO, "Metadato id: " + id + " eliminado exitosamente");
			return Response.status(Status.NO_CONTENT).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}
	
	@DELETE
	@Secured
	@Path("/selected")
	public Response deleteSelected(List<MetadataBean> metadataList) {
		if (metadataList.size() == 0) {
			return Response.status(Status.BAD_REQUEST).entity("La petici�n no rellena los requisitos").build();
		}
		try {
			MetadataDao elastic = new MetadataDaoImpl();
			elastic.deleteBatch(metadataList);
			return Response.status(Status.NO_CONTENT).build();
		} catch (Exception e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
	}

	@PUT
	@Secured
	public Response updateMetadata(MetadataBean metadata) {
		try {
			MetadataDao elastic = new MetadataDaoImpl();
			elastic.update(metadata);
			this.logger.log(MyLogger.INFO, "Actualización de metadato #" + metadata.getId() + " exitosa");
			return Response.status(Status.NO_CONTENT).build();
		} catch (Exception e) {
			this.logger.log(
				MyLogger.ERROR,
				"Eliminación de metadato #" + metadata.getId() + " con error: " + e.getMessage()
			);
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}
	
	@PUT
	@Secured
	@Path("/selected")
	public Response updateSelected(List<MetadataBean> metadataList) {
		if (metadataList.size() == 0) {
			return Response.status(Status.BAD_REQUEST).entity("La petici�n no rellena los requisitos").build();
		}
		try {
			MetadataDao elastic = new MetadataDaoImpl();
			elastic.updateBatch(metadataList);
			return Response.status(Status.NO_CONTENT).build();
		} catch (Exception e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
	}
}
