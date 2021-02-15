package ar.edu.ubp.das.resources;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.elasticsearch.ElasticsearchException;

import ar.edu.ubp.das.beans.MetadataBean;
import ar.edu.ubp.das.beans.SearchBean;
import ar.edu.ubp.das.beans.UserBean;
import ar.edu.ubp.das.db.Dao;
import ar.edu.ubp.das.db.DaoFactory;
import ar.edu.ubp.das.elastic.MetadataDao;
import ar.edu.ubp.das.elastic.MetadataDaoImpl;

@Path("search")
public class SearchResource {
	
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
			System.out.println("/SEARCH");
			Dao<UserBean, String> dao = DaoFactory.getDao("UserToken", "ar.edu.ubp.das");
			search.setUserId(dao.find(token).getUserId());
			System.out.println("GOT USER");
			MetadataDao elastic = new MetadataDaoImpl();
			System.out.println("CREATED DAO");
			List<MetadataBean> metadata = elastic.search(search);
			return Response.status(Status.OK).entity(metadata).build();
		} catch (ElasticsearchException e) {
			e.printStackTrace();
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}  catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}
}
