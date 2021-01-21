package ar.edu.ubp.das.beans;

import java.security.Principal;

public class UserBean implements Principal {
	private Integer user_id;
	private String role;
	private String firstName;
	private String lastName;
	private String username;
	private String password;
	
	public Integer getUser_id() {
		return user_id;
	}
	
	public String getRole() {
		return role;
	}
	
	public void setUser_id(Integer user_id) {
		this.user_id = user_id;
	}
	
	public void setRole(String role) {
		this.role = role;
	}
	
	public String getUsername() {
		return username;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String getName() {
		return username;
	}
	
	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
	@Override
	public String toString() {
		String output = "name: " + this.firstName;
		output += " username: " + this.username;
		output += " password: " + this.password;
		return output;
	}

}
