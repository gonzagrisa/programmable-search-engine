package ar.edu.ubp.das.daos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import ar.edu.ubp.das.beans.WebsiteBean;
import ar.edu.ubp.das.db.Dao;

public class MSWebsitesDao extends Dao<WebsiteBean, WebsiteBean> {
	
	@Override
	public WebsiteBean make(ResultSet result) throws SQLException {
		WebsiteBean web = new WebsiteBean();
		web.setWebsiteId(result.getInt("website_id"));
		web.setUserId(result.getInt("user_id"));
		web.setUrl(result.getString("url"));
		web.setIsActive(result.getBoolean("isActive"));
		web.setReindex(result.getBoolean("reindex"));
		web.setIndexed(result.getBoolean("indexed"));
		return web;
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
	public void update(Integer id) throws SQLException {
		try {
			this.connect();
			this.setProcedure("dbo.reindex(?)");
			this.setParameter(1, id);
			if (this.executeUpdate() == 0) {
				throw new SQLException();
			}
		} finally {
			this.close();
		}
	}
	
	@Override
	public void update(WebsiteBean website) throws SQLException {
		try {
			this.connect();
			this.setProcedure("dbo.update_website(?,?)");
			this.setParameter(1, website.getWebsiteId());
			this.setParameter(2, website.getUrl());
			if (this.executeUpdate() == 0) {
				throw new SQLException("Error al actualizar");
			}
		} finally {
			this.close();
		}
	}
	
	@Override
	public void insert(WebsiteBean web) throws SQLException {
		try {
			this.connect();
			this.setProcedure("dbo.new_website(?,?,?)");
			this.setParameter(1, web.getUserId());
			this.setParameter(2, web.getUrl());
			this.setNull(3, Types.INTEGER); //Usado por el crawler
			this.executeUpdate();
		} finally {
			this.close();
		}
	}

	@Override
	public void delete(Integer websiteId) throws SQLException {
		try {
			this.connect();
			this.setProcedure("dbo.delete_website(?)");
			this.setParameter(1, websiteId);
			if (this.executeUpdate() != 1) {
				throw new SQLException("Error al realizar la operacion");
			}
		} finally {
			this.close();
		}
	}
	
	@Override
	public List<WebsiteBean> select(WebsiteBean website) throws SQLException {
		try {
			this.connect();
			if (website.getWebsiteId() == null) {
				this.setProcedure("dbo.check_domain(?,?)");				
			} else {
				this.setProcedure("dbo.check_domain(?,?,?)");
				this.setParameter(3, website.getWebsiteId());
			}
			this.setParameter(1, website.getUserId());
			this.setParameter(2, website.getUrl());
			return this.executeQuery();
		} finally {
			this.close();
		}
	}
	
	@Override
	public void delete(WebsiteBean website) throws SQLException {
	}
	
	@Override
	public void delete(WebsiteBean website, Integer id) throws SQLException {
		// TODO Auto-generated method stub
	}

	@Override
	public WebsiteBean find(Integer websiteId) throws SQLException {
		try {
			this.connect();
			this.setProcedure("dbo.find_website(?)");
			this.setParameter(1, websiteId);
			List<WebsiteBean> websites = this.executeQuery();
			if (websites.size() == 0) {
				return null;
			} else {
				return websites.get(0);				
			}
		} finally {
			this.close();
		}
	}

	@Override
	public WebsiteBean find(WebsiteBean website) throws SQLException {
		return null;
	}

	@Override
	public List<WebsiteBean> select() throws SQLException {
		// TODO Auto-generated method stub
		return null;
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
