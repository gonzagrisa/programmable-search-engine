package ar.edu.ubp.das.resources;

import java.io.File;
import java.sql.SQLException;
import java.util.Date;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.media.multipart.ContentDisposition;

import ar.edu.ubp.das.beans.PreferencesBean;
import ar.edu.ubp.das.db.Dao;
import ar.edu.ubp.das.db.DaoFactory;
import ar.edu.ubp.das.logging.MyLogger;
import ar.edu.ubp.das.security.Secured;

@Path("preferences")
public class PreferencesResource {
	
	private MyLogger logger;

	@Context
	ContainerRequestContext req;
	
	public PreferencesResource() {
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
	public Response getPreferences() {
		try {
			Dao<PreferencesBean, PreferencesBean> dao = DaoFactory.getDao("Preferences", "ar.edu.ubp.das");
			this.logger.log(MyLogger.INFO, "Petición de preferencias exitosa");
			return Response.ok().entity(dao.find((Integer) req.getProperty("id"))).build();
		} catch (SQLException e) {
			this.logger.log(MyLogger.ERROR, "Petición de preferencias con error: " + e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
	}
	
	@GET
	@Path("{token}")
	public Response getPreferencesToken(@PathParam("token") String token) {
		try {
			Dao<PreferencesBean, String> dao = DaoFactory.getDao("Preferences", "ar.edu.ubp.das");
			this.logger.log(MyLogger.INFO, "Petición de token de preferencias exitosa: " + token);
			return Response.status(Status.OK).entity(dao.find(token)).build();
		} catch (NotFoundException e) {
			this.logger.log(MyLogger.ERROR, "Petición de token de preferencias con error: " + e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
		catch (Exception e) {
			this.logger.log(MyLogger.ERROR, "Petición de token de preferencias con error: " + e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}
	
	@GET
	@Path("file")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response getFile() {	
		File f = new File("search-box.js");
		
		System.out.println(f.getAbsolutePath());
	    if (!f.exists()) {
	    	this.logger.log(
	    		MyLogger.ERROR,
	    		"Descarga de componente de búsqueda con error: No se pudo crear el archivo"
	    	);
	        return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Hubo un problema interno al atender la solicitud").build();
	    }
	    ContentDisposition contentDisposition = ContentDisposition.type("attachment")
	    	    .fileName("search-box.js").creationDate(new Date()).build();
	    this.logger.log(MyLogger.INFO, "Descarga de componente de búsqueda exitosa.");
	    return Response.status(Status.OK).entity(f)
	    		.header("Content-Disposition", contentDisposition).build();
	}

	@PUT
	@Secured
	@Consumes(MediaType.APPLICATION_JSON)
	public Response update(PreferencesBean preferences) {
		try {
			Dao<PreferencesBean, Integer> dao = DaoFactory.getDao("Preferences", "ar.edu.ubp.das");
			preferences.setUserId((Integer) req.getProperty("id"));
			dao.update(preferences);
			this.logger.log(MyLogger.INFO, "Actualización de preferencias exitosa");
			return Response.status(Status.NO_CONTENT).build();
		} catch (Exception e) {
			this.logger.log(MyLogger.ERROR, "Actualización de preferencias con error: " + e.getMessage());
			return Response.status(Status.BAD_REQUEST).build();
		}
	}
}
