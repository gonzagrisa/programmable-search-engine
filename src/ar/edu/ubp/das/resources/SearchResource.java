package ar.edu.ubp.das.resources;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.elasticsearch.ElasticsearchException;

import ar.edu.ubp.das.beans.UserBean;
import ar.edu.ubp.das.beans.WordBean;
import ar.edu.ubp.das.beans.search.SearchBean;
import ar.edu.ubp.das.db.Dao;
import ar.edu.ubp.das.db.DaoFactory;
import ar.edu.ubp.das.elastic.MetadataDao;
import ar.edu.ubp.das.elastic.MetadataDaoImpl;
import ar.edu.ubp.das.logging.MyLogger;

@Path("search")
public class SearchResource {
	
	MyLogger logger;
	
	public SearchResource() {
		this.logger = new MyLogger(this.getClass().getSimpleName());
	}

	@GET
	@Path("ping")
	public Response ping() {
		return Response.ok().entity("pong").build();
	}

	@Path("{token}")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public Response search(@PathParam("token") String token, SearchBean search) {
		try {
			Dao<UserBean, String> dao = DaoFactory.getDao("UserToken", "ar.edu.ubp.das");
			Integer userId = dao.find(token).getUserId();
			search.setUserId(userId);
			MetadataDao elastic = new MetadataDaoImpl();
			this.logger.log(MyLogger.INFO, "Inserción de búsqueda del user #" + userId + " exitosa");
			return Response.status(Status.OK).entity(elastic.search(search)).build();
		} catch (ElasticsearchException e) {
			e.printStackTrace();
			this.logger.log(MyLogger.ERROR, "Inserción de búsqueda con error: " + e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		} catch (Exception e) {
			this.logger.log(MyLogger.ERROR, "Inserción de búsqueda con error: " + e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@Path("words/{token}")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public Response searchWords(@PathParam("token") String token, SearchBean search) {
		try {
			Dao<UserBean, String> dao = DaoFactory.getDao("UserToken", "ar.edu.ubp.das");
			int userId = dao.find(token).getUserId();
			search.setUserId(userId);
			MetadataDao elastic = new MetadataDaoImpl();
			elastic.significantWords(search);
			this.logger.log(MyLogger.INFO, "Inserción de palabras del user #" + userId + " exitosa");
			return Response.status(Status.OK).entity("").build();
		} catch (ElasticsearchException e) {
			e.printStackTrace();
			this.logger.log(MyLogger.ERROR, "Inserción de palabras con error: " + e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		} catch (Exception e) {
			this.logger.log(MyLogger.ERROR, "Inserción de palabras con error: " + e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@Path("popular/{id}")
	@POST
	public Response increasePopularity(@PathParam("id") String id) {
		try {
			MetadataDao elastic = new MetadataDaoImpl();
			elastic.increasePopularity(id);
			this.logger.log(MyLogger.INFO, "Se aumentó la popularidad del sitio con id en elastic #" + id);
			return Response.status(Status.OK).build();
		} catch (Exception e) {
			this.logger.log(MyLogger.ERROR, "Aumento de popularidad de un sitio con error: " + e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
	}

}
