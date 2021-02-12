package ar.edu.ubp.das.security;

import java.io.IOException;
import java.security.Key;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import ar.edu.ubp.das.beans.UserBean;
import ar.edu.ubp.das.logging.MyLogger;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class SecurityFilter implements ContainerRequestFilter {
	private static final String AUTHORIZATION_PREFIX = "Bearer ";
	public static final Key KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
	private MyLogger logger;
	
	public SecurityFilter() {
		this.logger = new MyLogger(this.getClass().getSimpleName());
	}

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		String authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
		if (!isHeaderValid(authHeader)) {
			this.abortWithUnauthorized(requestContext);
		}
		try {
			String token = authHeader.substring(AUTHORIZATION_PREFIX.length()).trim();
			UserBean user = validateToken(token);
            AppSecurityContext secContext = 
            		new AppSecurityContext(validateToken(token), requestContext.getSecurityContext().isSecure());
			requestContext.setSecurityContext(secContext);
			requestContext.setProperty("id", user.getUserId());
		} catch (Exception e) {
			this.abortWithUnauthorized(requestContext);
		}
	}

	private boolean isHeaderValid(String authorizationHeader) {
		return authorizationHeader != null &&
			   authorizationHeader.toLowerCase().startsWith(AUTHORIZATION_PREFIX.toLowerCase());
	}

	private void abortWithUnauthorized(ContainerRequestContext requestContext) {
		this.logger.log(
			MyLogger.ERROR,
			"El usuario no puede acceder al recurso /" + requestContext.getUriInfo().getPath()
			+ " (" + requestContext.getMethod() + ")"
		);
		requestContext.abortWith(
			Response.status(Response.Status.UNAUTHORIZED).entity("El usuario no puede acceder al recurso").build()
		);
	}

	private UserBean validateToken(String token) throws Exception {
		Jws<Claims> jws;
		jws = Jwts.parserBuilder().setSigningKey(KEY).build().parseClaimsJws(token);
		UserBean user = new UserBean();
		user.setUserId((Integer) jws.getBody().get("id"));
		user.setRole(jws.getBody().get("role").toString());
		return user;
	}
}
