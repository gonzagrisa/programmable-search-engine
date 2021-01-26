package ar.edu.ubp.das.daos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import ar.edu.ubp.das.beans.ServiceBean;
import ar.edu.ubp.das.db.Dao;

public class MSServicesDao extends Dao<ServiceBean, ServiceBean>{
	
	@Override
	public ServiceBean make(ResultSet result) throws SQLException {
		ServiceBean service = new ServiceBean();
		service.setUserId(result.getInt("user_id"));
		service.setServiceId(result.getInt("service_id"));
		service.setURLResource(result.getString("url_resource"));
		service.setURLPing(result.getString("url_ping"));
		service.setReindex(result.getInt("reindex") == 1 ? true : false);
		return service;
	}
	
	@Override
	public void insert(ServiceBean service) throws SQLException {
		try {
			this.connect();
			this.setProcedure("dbo.insert_service(?,?,?)");
			this.setParameter(1, service.getUserId());
			this.setParameter(2, service.getURLResource());
			this.setParameter(3, service.getURLPing());
			if (this.executeUpdate() == 0)
				throw new SQLException("Hubo un problema al insertar el servicio");
		} catch(SQLException e) {
			if (e.getMessage().contains("duplicate key value")) {
				throw new SQLException("El servicio ya se encuentra registrado");
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
	public void delete(ServiceBean arg0) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete(ServiceBean arg0, Integer arg1) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ServiceBean find(Integer arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServiceBean find(ServiceBean arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void insert(ServiceBean arg0, Integer arg1) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<ServiceBean> select() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ServiceBean> select(Integer arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ServiceBean> select(ServiceBean arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void update(ServiceBean arg0) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(ServiceBean arg0, Integer arg1) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean valid(ServiceBean arg0) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}


}
