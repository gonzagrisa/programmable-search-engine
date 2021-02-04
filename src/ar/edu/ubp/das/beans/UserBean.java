package ar.edu.ubp.das.beans;

import java.security.Principal;

import javax.json.bind.annotation.JsonbTransient;

public class UserBean implements Principal {
	private Integer userId;
	private String role;
	private String firstName;
	private String lastName;
	private String username;
	private String password;
	
	public Integer getUserId() {
		return userId;
	}
	
	public String getRole() {
		return role;
	}
	
	public void setUserId(Integer userId) {
		this.userId = userId;
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

	@JsonbTransient
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
		String output = "user_id: " + this.userId;
		output += "\nusername: " + this.username;
		output += "\npassword: " + this.password;
		output += "\nfirstName: " + this.firstName;
		output += "\nlastName: " + this.lastName;
		output += "\nrole: " + this.role;
		return output;
	}

}
