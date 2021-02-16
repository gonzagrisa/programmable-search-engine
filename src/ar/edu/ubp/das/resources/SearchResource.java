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
			Dao<UserBean, String> dao = DaoFactory.getDao("UserToken", "ar.edu.ubp.das");
			Integer userId = dao.find(token).getUserId();
			search.setUserId(userId);
			Dao<WordBean, WordBean> daoWord = DaoFactory.getDao("Words", "ar.edu.ubp.das");
			String[] words = search.getQuery().split(" ");
			WordBean word;
			for (int i = 0; i < words.length; i++) {
				word = new WordBean();
				word.setUserId(userId);
				word.setWord(words[i]);
				daoWord.insert(word);
			}			
			MetadataDao elastic = new MetadataDaoImpl();
			return Response.status(Status.OK).entity(elastic.search(search)).build();
		} catch (ElasticsearchException e) {
			e.printStackTrace();
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}  catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}
	
	@Path("words/{token}")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public Response searchWords(@PathParam("token") String token, SearchBean search) {
		try {
			System.out.println("/SEARCH");
			Dao<UserBean, String> dao = DaoFactory.getDao("UserToken", "ar.edu.ubp.das");
			search.setUserId(dao.find(token).getUserId());
			System.out.println("GOT USER");
			MetadataDao elastic = new MetadataDaoImpl();
			System.out.println("CREATED DAO");
			elastic.significantWords(search);
			return Response.status(Status.OK).entity("").build();
		} catch (ElasticsearchException e) {
			e.printStackTrace();
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}  catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}
}
