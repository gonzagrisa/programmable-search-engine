package ar.edu.ubp.das.daos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import ar.edu.ubp.das.beans.ServiceBean;
import ar.edu.ubp.das.beans.WebsiteBean;
import ar.edu.ubp.das.db.Dao;

public class MSServiceWebsitesDao extends Dao<WebsiteBean, ServiceBean> {

	@Override
	public WebsiteBean make(ResultSet result) throws SQLException {
		WebsiteBean website = new WebsiteBean();
		website.setWebsiteId(result.getInt("website_id"));
		website.setUserId(result.getInt("user_id"));
		website.setUrl(result.getString("url"));
		website.setServiceId(result.getInt("service_id"));
		return website;
	}

	@Override
	public List<WebsiteBean> select(Integer serviceId) throws SQLException {
		try {
			this.connect();
			this.setProcedure("dbo.get_service_websites(?,?)");
			this.setParameter(1, serviceId);
			this.setParameter(2, true);
			return this.executeQuery();
		} finally {
			this.close();
		}
	}

	@Override
	public List<WebsiteBean> select(ServiceBean service) throws SQLException {
		try {
			this.connect();
			this.setProcedure("dbo.get_service_websites(?,?)");
			this.setParameter(1, service.getServiceId());
			this.setParameter(2, false);
			return this.executeQuery();
		} finally {
			this.close();
		}
	}

	@Override
	public void delete(Integer arg0) throws SQLException {
		// TODO Auto-generated method stub

	}

	@Override
	public void delete(WebsiteBean arg0) throws SQLException {
		// TODO Auto-generated method stub

	}

	@Override
	public void delete(WebsiteBean arg0, Integer arg1) throws SQLException {
		// TODO Auto-generated method stub

	}

	@Override
	public WebsiteBean find(Integer arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WebsiteBean find(ServiceBean arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void insert(WebsiteBean arg0) throws SQLException {
		// TODO Auto-generated method stub

	}

	@Override
	public void insert(WebsiteBean arg0, Integer arg1) throws SQLException {
		// TODO Auto-generated method stub

	}

	@Override
	public List<WebsiteBean> select() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void update(Integer arg0) throws SQLException {
		// TODO Auto-generated method stub

	}

	@Override
	public void update(WebsiteBean arg0) throws SQLException {
		// TODO Auto-generated method stub

	}

	@Override
	public void update(WebsiteBean arg0, Integer arg1) throws SQLException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean valid(ServiceBean arg0) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}
}
