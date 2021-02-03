package ar.edu.ubp.das.resources;

import java.sql.SQLException;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import ar.edu.ubp.das.beans.UserBean;
import ar.edu.ubp.das.db.Dao;
import ar.edu.ubp.das.db.DaoFactory;
import ar.edu.ubp.das.security.Roles;
import ar.edu.ubp.das.security.Secured;
import ar.edu.ubp.das.security.SecurityFilter;
import io.jsonwebtoken.Jwts;

@Path("users")
public class UsersResource {
	@Context
	SecurityContext securityContext;

	@Context
	ContainerRequestContext request;

	@GET
	@Path("ping")
	public Response ping() {
		return Response.status(Status.OK).entity("pong!").build();
	}
	
	@POST
	@Path("login")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response login(UserBean user) {
		try {
			String token = issueToken(authenticate(user));
			return Response.ok(token).build();
		} catch (Exception e) {
			return Response.status(Response.Status.BAD_REQUEST).entity("Nombre de Usuario o Contrase�a Incorrectos")
					.build();
		}
	}

	@POST
	@Path("signup")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response signup(UserBean user) {
		try {
			if (user.getUsername() == null || user.getFirstName() == null || user.getLastName() == null
					|| user.getPassword() == null) {
				return Response.status(Status.BAD_REQUEST).entity("Datos faltantes para el registro del usuario")
						.build();
			}
			Dao<UserBean, UserBean> dao = DaoFactory.getDao("Users", "ar.edu.ubp.das");
			dao.insert(user);
			return Response.status(Status.NO_CONTENT).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}
	
	@GET
	@Secured
	@Path("info")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUserInfo() {
		try {
			Dao<UserBean, UserBean> dao = DaoFactory.getDao("Users", "ar.edu.ubp.das");
			UserBean user = dao.find((Integer) request.getProperty("id"));
			if (user == null) {
				return Response.status(Status.NOT_FOUND).entity("Usuario no encontrado").build();
			}
			return Response.status(Status.OK).entity(user).build();
		} catch (SQLException e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}


	@GET
	@Secured
	@RolesAllowed(Roles.ADMIN_ROLE)
	public Response getUsers() {
		try {
			Dao<UserBean, UserBean> dao = DaoFactory.getDao("Users", "ar.edu.ubp.das");
			return Response.status(Status.OK).entity(dao.select()).build();
		} catch (Exception e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
	}


	@POST
	@Secured
	@Path("return/{id}")
	public Response returnAccount(@PathParam("id") Integer id) {
		try {
			Dao<UserBean, UserBean> dao = DaoFactory.getDao("Users", "ar.edu.ubp.das");
			UserBean user = dao.find(id);
			if (user == null) {
				throw new Exception("User not Found");
			}
			String token = issueToken(user);
			return Response.ok().entity(token).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}
	
	
	@POST
	@Secured
	@RolesAllowed(Roles.ADMIN_ROLE)
	@Path("impersonate/{id}") // impersonator
	public Response changeProfile(@PathParam("id") Integer id) {
		try {
			Dao<UserBean, UserBean> dao = DaoFactory.getDao("Users", "ar.edu.ubp.das");
			UserBean user = dao.find(id);
			if (user == null) {
				throw new Exception("User not Found");
			}
			String token = issueToken(user, (Integer) request.getProperty("id"));
			return Response.ok().entity(token).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@POST
	@Path("checkUsername")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response checkUsername(UserBean user) {
		try {
			Dao<UserBean, UserBean> dao = DaoFactory.getDao("Users", "ar.edu.ubp.das");
			if (dao.select(user).size() > 0) {
				return Response.status(Status.CONFLICT).entity("Nombre de Usuario ya registrado").build();
			} else {
				return Response.status(Status.OK).build();
			}
		} catch (SQLException e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@POST
	@Path("checkPassword")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response checkPassword(UserBean user) {
		try {
			Dao<UserBean, UserBean> dao = DaoFactory.getDao("Users", "ar.edu.ubp.das");
			if (dao.valid(user)) {
				return Response.status(Status.OK).build();
			}
			return Response.status(Status.BAD_REQUEST).entity("Contrase�a erronea").build();
		} catch (Exception e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
	}

	@PUT
	@Secured
	@Path("me")
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
	
	@PUT
	@Secured
	@Path("{id}")
	@RolesAllowed(Roles.ADMIN_ROLE)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateUserId(@PathParam("id") Integer id, UserBean user) {
		try {
			Dao<UserBean, UserBean> dao = DaoFactory.getDao("Users", "ar.edu.ubp.das");
			dao.update(user);
			return Response.status(Status.NO_CONTENT).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@DELETE
	@Secured
	public Response deleteUser(UserBean user) {
		return Response.ok().build();
	}
	
	@DELETE
	@Secured
	@Path("{id}")
	@RolesAllowed(Roles.ADMIN_ROLE)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deleteUserId(@PathParam("id") Integer id) {
		try {
			Dao<UserBean, UserBean> dao = DaoFactory.getDao("Users", "ar.edu.ubp.das");
			dao.delete(id);
			return Response.status(Status.NO_CONTENT).build();
		} catch (SQLException e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
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
		return Jwts.builder()
				.setSubject("usr")
				.claim("id", user.getUser_id())
				.claim("username", user.getUsername())
				.claim("role", user.getRole())
				.signWith(SecurityFilter.KEY).compact();
	}
	
	private String issueToken(UserBean user, Integer impersonator) {
		return Jwts.builder()
				.setSubject("usr")
				.claim("id", user.getUser_id())
				.claim("username", user.getUsername())
				.claim("role", user.getRole())
				.claim("impersonator", impersonator)
				.signWith(SecurityFilter.KEY).compact();
	}
}
