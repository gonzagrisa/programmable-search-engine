package ar.edu.ubp.das.daos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import ar.edu.ubp.das.beans.UserBean;
import ar.edu.ubp.das.db.Dao;

public class MSUserTokenDao extends Dao<UserBean, String> {
	
	@Override
	public UserBean make(ResultSet result) throws SQLException {
		UserBean user = new UserBean();
		user.setUserId(result.getInt("user_id"));
		return user;
	}
	
	@Override
	public UserBean find(String token) throws SQLException {
		try {
			this.connect();
			this.setProcedure("dbo.find_user_token(?)");
			this.setParameter(1, token);
			List<UserBean> users = this.executeQuery();			
			if (users.size() > 0) {
				return users.get(0);
			} else {
				throw new SQLException("Token no válido");
			}
		} finally {
			this.close();
		}
	}
	
	@Override
	public void delete(Integer arg0) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete(UserBean arg0) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete(UserBean arg0, Integer arg1) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public UserBean find(Integer arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void insert(UserBean arg0) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void insert(UserBean arg0, Integer arg1) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<UserBean> select() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<UserBean> select(Integer arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<UserBean> select(String arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void update(Integer arg0) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(UserBean arg0) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(UserBean arg0, Integer arg1) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean valid(String arg0) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

}
