package ar.edu.ubp.das.daos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import ar.edu.ubp.das.beans.WebsiteBean;
import ar.edu.ubp.das.db.Dao;

public class MSWebsitesDao extends Dao<WebsiteBean, WebsiteBean> {

	@Override
	public void delete(Integer arg0) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete(WebsiteBean website) throws SQLException {
		try {
			this.connect();
			this.setProcedure("dbo.delete_website(?,?)");
			this.setParameter(1, website.getUserId());
			this.setParameter(2, website.getUrl());
			this.executeUpdate();
		} finally {
			this.close();
		}
		
	}
	
	@Override
	public void delete(WebsiteBean website, Integer id) throws SQLException {
		try {
			this.connect();
			this.setProcedure("dbo.delete_website(?,?)");
			this.setParameter(1, id);
			this.setParameter(2, website.getUrl());
			if (this.executeUpdate() == 0) {
				throw new SQLException();
			}
		} finally {
			this.close();
		}
	}

	@Override
	public WebsiteBean find(Integer arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WebsiteBean find(WebsiteBean arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void insert(WebsiteBean website) throws SQLException {
		try {
			this.connect();
			this.setProcedure("dbo.new_website(?,?)");
			this.setParameter(1, website.getUserId());
			this.setParameter(2, website.getUrl());
			this.executeUpdate();
		} finally {
			this.close();
		}
	}

	@Override
	public WebsiteBean make(ResultSet result) throws SQLException {
		WebsiteBean web = new WebsiteBean();
		web.setUserId(result.getInt("user_id"));
		web.setUrl(result.getString("url"));
		web.setReindex(result.getBoolean("reindex"));
		return web;
	}

	@Override
	public List<WebsiteBean> select() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<WebsiteBean> select(Integer id) throws SQLException {
		try {
			this.connect();
			this.setProcedure("dbo.get_websites(?)");
			this.setParameter(1, id);
			return this.executeQuery();
		} finally {
			this.close();
		}
	}

	@Override
	public List<WebsiteBean> select(WebsiteBean arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void update(WebsiteBean arg0) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean valid(WebsiteBean arg0) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	

	@Override
	public void insert(WebsiteBean arg0, Integer arg1) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(WebsiteBean arg0, Integer arg1) throws SQLException {
		// TODO Auto-generated method stub
		
	}
}
