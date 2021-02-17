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
			this.setParameter(1, user.getFirstName());
			this.setParameter(2, user.getLastName());
			this.setParameter(3, user.getUsername());
			this.setParameter(4, user.getPassword());
			this.executeUpdate();
		} catch(SQLException e) {
			throw e;
		} finally {
			this.close();
		}	
	}

	@Override
	public UserBean make(ResultSet result) throws SQLException {
		UserBean user = new UserBean();
		user.setUserId(result.getInt("user_id"));
		user.setUsername(result.getString("username"));
		user.setFirstName(result.getString("name"));
		user.setLastName(result.getString("last_name"));
		user.setRole(result.getString("role"));
		user.setPassword(result.getString("password"));
		return user;
	}

	@Override
	public List<UserBean> select() throws SQLException {
		try {
			this.connect();
			this.setProcedure("dbo.get_users");
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
			this.setParameter(1, user.getUserId());
			this.setParameter(2, user.getFirstName());
			this.setParameter(3, user.getLastName());
			this.setParameter(4, user.getUsername());
			this.setParameter(5, user.getPassword());
			if (this.executeUpdate() == 0) {
				throw new SQLException("El usuario a actualizar no existe");
			}
		} finally {
			this.close();
		}
	}

	@Override
	public boolean valid(UserBean user) throws SQLException {
		try {
			this.connect();
			this.setProcedure("dbo.check_password(?,?)");
			this.setParameter(1, user.getUserId());
			this.setParameter(2, user.getPassword());
			if (this.executeQuery().size() == 0)
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
			List<UserBean> result = this.executeQuery();
			if (result.size() == 0) {
				return null;
			} else {
				return result.get(0);
			}
		} finally {
			this.close();
		}
	}

	@Override
	public UserBean find(Integer id) throws SQLException {
		try {
			this.connect();
			this.setProcedure("dbo.get_user_info(?)");
			this.setParameter(1, id);
			return this.executeQuery().get(0);
		} finally {
			this.close();
		}
	}

	@Override
	public void delete(Integer id) throws SQLException {
		try {
			this.connect();
			this.setProcedure("dbo.delete_account(?)");
			this.setParameter(1, id);
			if (this.executeUpdate() == 0)
				throw new SQLException("Error al ejecutar la operación");
			return;
		} finally {
			this.close();
		}

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
	public List<UserBean> select(UserBean user) throws SQLException {
		try {
			this.connect();
			if (user.getUserId() != null) {
				this.setProcedure("dbo.check_username(?,?)");
				this.setParameter(1, user.getUsername());
				this.setParameter(2, user.getUserId());
			} else {
				this.setProcedure("dbo.check_username(?)");
				this.setParameter(1, user.getUsername());
			}
			return this.executeQuery();
		} finally {
			this.close();
		}
	}

	@Override
	public void delete(UserBean arg0, Integer arg1) throws SQLException {
		// TODO Auto-generated method stub

	}

	@Override
	public void insert(UserBean arg0, Integer arg1) throws SQLException {
		System.out.println("INTEGER: " + arg1);

	}

	@Override
	public void update(UserBean arg0, Integer arg1) throws SQLException {
		// TODO Auto-generated method stub

	}

	@Override
	public void update(Integer arg0) throws SQLException {
		// TODO Auto-generated method stub
		
	}

}
