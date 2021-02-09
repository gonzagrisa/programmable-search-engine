package ar.edu.ubp.das.daos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import ar.edu.ubp.das.beans.PreferencesBean;
import ar.edu.ubp.das.db.Dao;

public class MSPreferencesDao extends Dao<PreferencesBean, PreferencesBean>{

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
		pref.setColor(result.getString("color"));
		pref.setIconURL(result.getString("icon_url"));
		pref.setBorderRadius(result.getInt("border_radius"));
		pref.setFontSize(result.getInt("font_size"));
		return pref;
	}

	@Override
	public void update(PreferencesBean pref) throws SQLException {
		try {
			this.connect();
			this.setProcedure("dbo.update_preferences(?,?,?,?,?)");
			this.setParameter(1, pref.getUserId());
			this.setParameter(2, pref.getColor());
			this.setParameter(3, pref.getIconURL());
			this.setParameter(4, pref.getBorderRadius());
			this.setParameter(5, pref.getFontSize());
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
	public void insert(PreferencesBean arg0) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<PreferencesBean> select() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<PreferencesBean> select(Integer id) throws SQLException {
		return null;
	}

	@Override
	public List<PreferencesBean> select(PreferencesBean arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void delete(PreferencesBean arg0, Integer arg1) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public PreferencesBean find(PreferencesBean arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void insert(PreferencesBean arg0, Integer arg1) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(PreferencesBean arg0, Integer arg1) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean valid(PreferencesBean arg0) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void update(Integer arg0) throws SQLException {
		// TODO Auto-generated method stub
	}
	
}