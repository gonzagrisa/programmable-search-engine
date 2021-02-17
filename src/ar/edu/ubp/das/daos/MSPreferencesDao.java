package ar.edu.ubp.das.daos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.ws.rs.NotFoundException;

import ar.edu.ubp.das.beans.PreferencesBean;
import ar.edu.ubp.das.db.Dao;

public class MSPreferencesDao extends Dao<PreferencesBean, String>{

	@Override
	public PreferencesBean find(Integer id) throws SQLException {
		try {
			this.connect();
			this.setProcedure("dbo.get_preferences(?)");
			this.setParameter(1, id);
			return this.executeQuery().get(0);
		} finally {
			this.close();
		}
	}

	@Override
	public PreferencesBean make(ResultSet result) throws SQLException {
		PreferencesBean pref = new PreferencesBean();
		pref.setUserId(result.getInt("user_id"));
		pref.setBorderWidth(result.getDouble("border_width"));
		pref.setBorderRadius(result.getInt("border_radius"));
		pref.setIconURL(result.getString("icon_url"));
		pref.setIconSize(result.getInt("icon_size"));
		pref.setPlaceholder(result.getString("placeholder"));
		pref.setColor(result.getString("color"));
		return pref;
	}

	@Override
	public void update(PreferencesBean pref) throws SQLException {
		try {
			this.connect();
			this.setProcedure("dbo.update_preferences(?,?,?,?,?,?,?)");
			this.setParameter(1, pref.getUserId());
			this.setParameter(2, pref.getBorderWidth());
			this.setParameter(3, pref.getBorderRadius());
			this.setParameter(4, pref.getIconURL());
			this.setParameter(5, pref.getIconSize());
			this.setParameter(6, pref.getPlaceholder());
			this.setParameter(7, pref.getColor());
			if (this.executeUpdate() == 0)
				throw new SQLException("La preferencia a actualizar no existe.");
		} finally {
			this.close();
		}
	}

	@Override
	public void delete(Integer arg0) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete(PreferencesBean arg0) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete(PreferencesBean arg0, Integer arg1) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public PreferencesBean find(String token) throws SQLException {
		try {
			this.connect();
			this.setProcedure("dbo.get_preferences_token(?)");
			this.setParameter(1, token);
			List<PreferencesBean> pref = this.executeQuery();
			if (pref.size() == 0) {
				throw new NotFoundException("Token no valido");
			}
			return pref.get(0);
		} finally {
			this.close();
		}
	}

	@Override
	public void insert(PreferencesBean arg0) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void insert(PreferencesBean arg0, Integer arg1) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<PreferencesBean> select() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<PreferencesBean> select(Integer arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<PreferencesBean> select(String arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void update(Integer arg0) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(PreferencesBean arg0, Integer arg1) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean valid(String arg0) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

}