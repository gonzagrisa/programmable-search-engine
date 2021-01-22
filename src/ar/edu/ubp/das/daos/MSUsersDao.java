package ar.edu.ubp.das.daos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import ar.edu.ubp.das.beans.UserBean;
import ar.edu.ubp.das.db.Dao;

public class MSUsersDao extends Dao<UserBean, UserBean> {

	@Override
	public void insert(UserBean user) throws SQLException {
		try {
			this.connect();
			this.setProcedure("dbo.new_user(?,?,?,?)");
			this.setParameter(1, user.getName());
			this.setParameter(2, user.getLastName());
			this.setParameter(3, user.getUsername());
			this.setParameter(4, user.getPassword());
			this.execute();
		} finally {
			this.close();
		}
	}

	@Override
	public UserBean make(ResultSet result) throws SQLException {
		UserBean user = new UserBean();
		user.setUsername(result.getString("username"));
		user.setRole(result.getString("role"));
		user.setUser_id(result.getInt("user_id"));
		return user;
	}

	@Override
	public List<UserBean> select() throws SQLException {
		try {
			this.connect();
			return this.executeQuery();
		} finally {
			this.close();
		}
	}

	@Override
	public void update(UserBean user) throws SQLException {
		try {
			this.connect();
			this.setProcedure("dbo.update_user(?,?,?,?,?)");
			this.setParameter(1, user.getUser_id());
			this.setParameter(2, user.getName());
			this.setParameter(3, user.getLastName());
			this.setParameter(4, user.getUsername());
			this.setParameter(5, user.getPassword());
			this.executeUpdate();
		} finally {
			this.close();
		}
	}

	@Override
	public boolean valid(UserBean user) throws SQLException {
		try {
			this.connect();
			this.setProcedure("dbo.validate_user(?,?)");
			this.setParameter(1, user.getUsername());
			this.setParameter(2, user.getPassword());
			if (this.executeQuery().get(0) == null)
				return false;
			return true;
		} finally {
			this.close();
		}
	}
	
	@Override
	public UserBean find(UserBean user) throws SQLException {
		try {
			this.connect();
			this.setProcedure("dbo.validate_user(?,?)");
			this.setParameter(1, user.getUsername());
			this.setParameter(2, user.getPassword());
			return this.executeQuery().get(0);
		} finally {
			this.close();
		}
	}
	
	@Override
	public UserBean find(Integer id) throws SQLException {
		System.out.println("ID: " + id);
		return null;
	}
	
	@Override
	public void delete(Integer arg0) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<UserBean> select(Integer arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void delete(UserBean arg0) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<UserBean> select(UserBean arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

}
