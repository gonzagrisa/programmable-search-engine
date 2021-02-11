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
import ar.edu.ubp.das.logging.MyLogger;
import ar.edu.ubp.das.security.Roles;
import ar.edu.ubp.das.security.Secured;
import ar.edu.ubp.das.security.SecurityFilter;
import io.jsonwebtoken.Jwts;

@Path("users")
public class UsersResource {
	
	private MyLogger logger;

	@Context
	SecurityContext securityContext;

	@Context
	ContainerRequestContext request;
	
	public UsersResource() {
		this.logger = new MyLogger(this.getClass().getSimpleName());
	}

	@GET
	@Secured
	@Path("ping")
	public Response ping() {
		this.logger.log(MyLogger.INFO, "Petición de ping exitosa");
		return Response.status(Status.OK).entity("pong!").build();
	}
	
	@POST
	@Path("login")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response login(UserBean user) {
		try {
			String token = issueToken(authenticate(user));
			this.logger.log(MyLogger.INFO, "Login del usuario #" + user.getUserId() + " exitoso");
			return Response.ok(token).build();
		} catch (Exception e) {
			this.logger.log(
				MyLogger.ERROR,
				"Login del usuario #" + user.getUserId() + " con error: " + e.getMessage()
			);
			return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@POST
	@Path("signup")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response signup(UserBean user) {
		try {
			if (
				user.getUsername() == null ||
				user.getFirstName() == null ||
				user.getLastName() == null ||
				user.getPassword() == null
			) {
				// WARNING porque no es un error de la plataforma, que no pante el cúnico
				this.logger.log(MyLogger.WARNING, "Registro de usuario con datos faltantes.");
				return Response.status(Status.BAD_REQUEST)
					.entity("Datos faltantes para el registro del usuario")
					.build();
			}
			Dao<UserBean, UserBean> dao = this.getDao();
			dao.insert(user);
			this.logger.log(MyLogger.INFO, "Registro de usuario exitoso");
			return Response.status(Status.NO_CONTENT).build();
		} catch (Exception e) {
			this.logger.log(MyLogger.ERROR, "Registro de usuario con error: " + e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}
	
	@GET
	@Secured
	@Path("info")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUserInfo() {
		try {
			Dao<UserBean, UserBean> dao = this.getDao();
			UserBean user = dao.find((Integer) request.getProperty("id"));
			if (user == null) {
				this.logger.log(MyLogger.WARNING, "Petición de información de un usuario inexistente");
				return Response.status(Status.NOT_FOUND).entity("Usuario no encontrado").build();
			}
			this.logger.log(MyLogger.INFO, "Petición de información de un usuario exitosa");
			return Response.status(Status.OK).entity(user).build();
		} catch (SQLException e) {
			this.logger.log(
				MyLogger.ERROR,
				"Petición de información de un usuario con error: " + e.getMessage()
			);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		} catch (Exception e) {
			this.logger.log(
				MyLogger.ERROR,
				"Petición de información de un usuario con error: " + e.getMessage()
			);
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}


	@GET
	@Secured
	@RolesAllowed(Roles.ADMIN_ROLE)
	public Response getUsers() {
		try {
			Dao<UserBean, UserBean> dao = this.getDao();
			this.logger.log(MyLogger.INFO, "Petición de usuarios exitosa");
			return Response.status(Status.OK).entity(dao.select()).build();
		} catch (Exception e) {
			this.logger.log(MyLogger.ERROR, "Petición de usuarios con error: " + e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
	}


	@POST
	@Secured
	@Path("return/{id}")
	public Response returnAccount(@PathParam("id") Integer id) {
		try {
			Dao<UserBean, UserBean> dao = this.getDao();
			UserBean user = dao.find(id);
			if (user == null) {
				this.logger.log(
					MyLogger.WARNING, 
					"Petición de devolución de cuenta de un usuario inexistente"
				);
				throw new Exception("User not Found");
			}
			String token = issueToken(user);
			this.logger.log(
				MyLogger.INFO, 
				"Petición de devolución de cuenta de un usuario exitosa"
			);
			return Response.ok().entity(token).build();
		} catch (Exception e) {
			this.logger.log(
					MyLogger.ERROR, 
					"Petición de devolución de cuenta de un usuario con error: "
					+ e.getMessage()
				);
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}
	
	@POST
	@Secured
	@RolesAllowed(Roles.ADMIN_ROLE)
	@Path("impersonate/{id}")
	public Response changeProfile(@PathParam("id") Integer id) {
		try {
			Dao<UserBean, UserBean> dao = this.getDao();
			UserBean user = dao.find(id);
			if (user == null) {
				this.logger.log(
					MyLogger.WARNING,
					"Petición de suplantación de identidad de un usuario inexistente"
				);
				throw new Exception("User not Found");
			}
			String token = issueToken(user, (Integer) request.getProperty("id"));
			this.logger.log(MyLogger.INFO, "Petición de suplantación de identidad de un usuario exitosa");
			return Response.ok().entity(token).build();
		} catch (Exception e) {
			this.logger.log(
				MyLogger.INFO,
				"Petición de suplantación de identidad de un usuario con error: "
				+ e.getMessage()
			);
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@POST
	@Path("checkUsername")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response checkUsername(UserBean user) {
		try {
			Dao<UserBean, UserBean> dao = this.getDao();
			if (dao.select(user).size() > 0) {
				this.logger.log(MyLogger.WARNING, "Chequeo de nombre de usuario ya registrado");
				return Response.status(Status.CONFLICT).entity("Nombre de usuario ya registrado").build();
			} else {
				this.logger.log(MyLogger.INFO, "Chequeo de nombre de usuario libre");
				return Response.status(Status.OK).build();
			}
		} catch (SQLException e) {
			this.logger.log(MyLogger.ERROR, "Chequeo de nombre de usuario con error: " + e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		} catch (Exception e) {
			this.logger.log(MyLogger.ERROR, "Chequeo de nombre de usuario con error: " + e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@POST
	@Path("checkPassword")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response checkPassword(UserBean user) {
		try {
			Dao<UserBean, UserBean> dao = this.getDao();
			if (!dao.valid(user)) {
				this.logger.log(MyLogger.WARNING, "Chequeo de contraseña errónea");
				return Response.status(Status.BAD_REQUEST).entity("Contraseña erronea").build();
			}
			this.logger.log(MyLogger.INFO, "Chequeo de contraseña correcta");
			return Response.status(Status.OK).build();
		} catch (Exception e) {
			this.logger.log(MyLogger.ERROR, "Chequeo de contraseña con error: " + e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
	}

	@PUT
	@Secured
	@Path("me")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateUser(UserBean user) {
		try {
			user.setUserId((Integer) request.getProperty("id"));
			Dao<UserBean, UserBean> dao = this.getDao();
			dao.update(user);
			this.logger.log(MyLogger.INFO, "Actualización propia de usuario exitosa");
			return Response.status(Status.NO_CONTENT).build();
		} catch (Exception e) {
			this.logger.log(MyLogger.ERROR, "Actualización propia de usuario con error: " + e.getMessage());
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
			Dao<UserBean, UserBean> dao = this.getDao();
			dao.update(user);
			this.logger.log(MyLogger.INFO, "Actualización de usuario por parte del admin exitosa");
			return Response.status(Status.NO_CONTENT).build();
		} catch (Exception e) {
			this.logger.log(
				MyLogger.ERROR,
				"Actualización de usuario por parte del admin con error: "
				+ e.getMessage()
			);
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	// y esto?
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
			Dao<UserBean, UserBean> dao = this.getDao();
			dao.delete(id);
			this.logger.log(MyLogger.INFO, "Eliminación de usuario por parte del admin exitosa");
			return Response.status(Status.NO_CONTENT).build();
		} catch (SQLException e) {
			this.logger.log(
				MyLogger.ERROR,
				"Eliminación de usuario por parte del admin con error: "
				+ e.getMessage()
			);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		} catch (Exception e) {
			this.logger.log(
				MyLogger.ERROR,
				"Eliminación de usuario por parte del admin con error: "
				+ e.getMessage()
			);
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	private UserBean authenticate(UserBean user) throws Exception {
		Dao<UserBean, UserBean> dao = this.getDao();
		UserBean userFound = dao.find(user);
		if (userFound != null)
			return userFound;
		else {
			throw new Exception("Nombre de usuario o contraseña incorrectos");
		}
	}
	
	private String issueToken(UserBean user) {
		return Jwts.builder()
				.setSubject("usr")
				.claim("id", user.getUserId())
				.claim("username", user.getUsername())
				.claim("role", user.getRole())
				.signWith(SecurityFilter.KEY).compact();
	}
	
	private String issueToken(UserBean user, Integer impersonator) {
		return Jwts.builder()
				.setSubject("usr")
				.claim("id", user.getUserId())
				.claim("username", user.getUsername())
				.claim("role", user.getRole())
				.claim("impersonator", impersonator)
				.signWith(SecurityFilter.KEY).compact();
	}
	
	private Dao<UserBean, UserBean> getDao() throws SQLException {
		return DaoFactory.getDao("Users", "ar.edu.ubp.das");
	}
}
