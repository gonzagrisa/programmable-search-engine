package ar.edu.ubp.das.resources;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import ar.edu.ubp.das.beans.UserBean;
import ar.edu.ubp.das.db.Dao;
import ar.edu.ubp.das.db.DaoFactory;
import ar.edu.ubp.das.security.Secured;
import ar.edu.ubp.das.security.SecurityFilter;
import io.jsonwebtoken.Jwts;

@Path("/")
public class UserResource {
	@Context
	SecurityContext securityContext;

	@Context
	ContainerRequestContext request;

	@GET
	@Secured
	@Path("ping")
	@RolesAllowed("ADMIN")
	public Response ping() {
		return Response.status(Status.OK).entity("pong").build();
	}

	@POST
	@Path("login")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response login(UserBean user) {
		try {
			System.out.println(user);
			String token = issueToken(authenticate(user));
			return Response.ok(token).build();
		} catch (Exception e) {
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
	}

	@POST
	@Path("signup")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response signup(UserBean user) {
		try {
			Dao<UserBean, UserBean> dao = DaoFactory.getDao("Users", "ar.edu.ubp.das");
			dao.insert(user);
			return Response.status(Status.NO_CONTENT).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@PUT
	@Secured
	@Path("update")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateUser(UserBean user) {
		try {
			user.setUser_id((Integer) request.getProperty("id"));
			Dao<UserBean, UserBean> dao = DaoFactory.getDao("Users", "ar.edu.ubp.das");
			dao.update(user);
			return Response.status(Status.NO_CONTENT).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@DELETE
	@Secured
	@Path("delete")
	public Response deleteUser(UserBean user) {
		return Response.ok().build();
	}

	@DELETE
	@RolesAllowed("admin")
	public Response deleteUserById(UserBean user) {
		return Response.ok().build();
	}
	
	private UserBean authenticate(UserBean user) throws Exception {
		Dao<UserBean, UserBean> dao = DaoFactory.getDao("Users", "ar.edu.ubp.das");
		UserBean userFound = dao.find(user);
		if (userFound != null)
			return userFound;
		else {
			throw new Exception();
		}
	}

	private String issueToken(UserBean user) {
		return Jwts.builder().setSubject("usr").claim("role", user.getRole()).claim("id", 1)
				.signWith(SecurityFilter.KEY).compact();
	}
}
