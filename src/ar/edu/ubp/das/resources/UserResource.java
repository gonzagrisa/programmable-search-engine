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
import javax.ws.rs.core.SecurityContext;

import ar.edu.ubp.das.beans.UserBean;
import ar.edu.ubp.das.daos.UsersDao;
import ar.edu.ubp.das.security.Secured;
import ar.edu.ubp.das.security.SecurityFilter;

import javax.ws.rs.core.Response.Status;

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
	@RolesAllowed("admin")
	public Response ping() {
		System.out.println("USER ID:" + request.getProperty("id"));
		System.out.println(securityContext.getUserPrincipal().getName());
		return Response.status(Status.OK).entity("pong").build();
	}

	@POST
	@Path("login")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response login(UserBean user) {
		try {
			System.out.println(user.getUsername());
			UserBean userFound = authenticate(user);
			String token = issueToken(userFound);
			return Response.ok(token).build();
		} catch (Exception e) {
			return Response.status(Response.Status.FORBIDDEN).build();
		}
	}
	
	private UserBean authenticate(UserBean user) throws Exception {
		UsersDao dao = new UsersDao();
		UserBean userFound = dao.findUser(user);
		if (userFound != null)
			return userFound;
		else {
			throw new Exception();			
		}
	}
	
	private String issueToken(UserBean user) {
		return Jwts.builder()
				   .setSubject("usr")
				   .claim("role", user.getRole())
				   .claim("id", 1)
				   .signWith(SecurityFilter.KEY)
				   .compact();
	}
	
	@POST
	@Path("signup")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response signup(UserBean user) {
		return Response.ok().build();
	}
	
	@PUT
	@Secured
	@Path("update")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateUser(UserBean user) {
		return Response.ok().entity("TOKEN VALID").build();
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
}
