package ar.edu.ubp.das.daos;

import ar.edu.ubp.das.beans.UserBean;

public class UsersDao {
	public UserBean findUser(UserBean user) {
		if (user.getUsername().equals("admin") && user.getPassword().equals("admin")) {
			user.setRole("admin");
			user.setUser_id(1);
			return user;
		}
		else
			return null;
	}
}
