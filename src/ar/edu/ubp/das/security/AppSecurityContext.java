package ar.edu.ubp.das.security;

import java.security.Principal;

import javax.ws.rs.core.SecurityContext;

import ar.edu.ubp.das.beans.UserBean;

public class AppSecurityContext implements SecurityContext{
	
	private UserBean user;
	private boolean secure;
	
	public AppSecurityContext(UserBean user, boolean secure) {
		this.user = user;
		this.secure = secure;
	}
	
	@Override
	public Principal getUserPrincipal() {
		return user;
	}

	@Override
	public boolean isUserInRole(String role) {
		return this.user.getRole().equals(role);
	}

	@Override
	public boolean isSecure() {
		return this.secure;
	}

	@Override
	public String getAuthenticationScheme() {
		return SecurityContext.FORM_AUTH;
	}
	
	public int getUserId() {
		return this.user.getUserId();
	}
}
