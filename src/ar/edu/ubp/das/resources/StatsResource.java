package ar.edu.ubp.das.resources;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import ar.edu.ubp.das.beans.stats.StatsToShowBean;
import ar.edu.ubp.das.db.Dao;
import ar.edu.ubp.das.db.DaoFactory;
import ar.edu.ubp.das.logging.MyLogger;
import ar.edu.ubp.das.security.Roles;
import ar.edu.ubp.das.security.Secured;

@Path("stats")
public class StatsResource {
	
	MyLogger logger;
	
	@Context
	ContainerRequestContext req;
	
	public StatsResource() {
		this.logger = new MyLogger(this.getClass().getSimpleName());
	}
	
	@GET
	@Path("quantities")
	@Secured
	public Response getAll() {
		try {
			Dao<StatsToShowBean, StatsToShowBean> dao = DaoFactory.getDao("Stats", "ar.edu.ubp.das");
			List<StatsToShowBean> stats = new ArrayList<StatsToShowBean>();
			if (req.getProperty("rol").equals(Roles.ADMIN_ROLE)) {
				stats = dao.select();
			} else {
				stats = dao.select((Integer) req.getProperty("id"));
			}
			this.logger.log(MyLogger.INFO, "Petición de estadísticas exitosa");
			return Response.status(Status.OK).entity(stats.get(0)).build();
		} catch (SQLException e) {
			this.logger.log(
				MyLogger.ERROR,
				"Petición de estadísticas con error: " + e.getMessage()
			);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		} catch (Exception e) {
			e.printStackTrace();
			this.logger.log(
				MyLogger.ERROR,
				"Petición de estadísticas con error: " + e.getMessage()
			);
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}
	
//	@GET
//	@Path("queries")
//	public Response getQueries() {
//		
//	}
//	
//	@GET
//	@Path("queries/day")
//	public Response getQueriesByDay() {
//		
//	}
//	
//	@GET
//	@Path("words")
//	public Response getWords() {
//		
//	}
	
}
