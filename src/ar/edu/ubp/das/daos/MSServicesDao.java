package ar.edu.ubp.das.daos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import ar.edu.ubp.das.beans.indexation.ServiceBean;
import ar.edu.ubp.das.db.Dao;

public class MSServicesDao extends Dao<ServiceBean, ServiceBean>{

	@Override
	public ServiceBean make(ResultSet result) throws SQLException {
		ServiceBean service = new ServiceBean();
		service.setServiceId(result.getInt("service_id"));
		service.setUserId(result.getInt("user_id"));
		service.setUrl(result.getString("url"));
		service.setProtocol(result.getString("protocol"));
		service.setReindex(result.getBoolean("reindex"));
		service.setIndexed(result.getBoolean("indexed"));
		service.setIndexDate(result.getString("index_date"));
		service.setIsUp(result.getBoolean("isUp"));
		return service;
	}

	@Override
	public List<ServiceBean> select(Integer userId) throws SQLException {
		try {
			this.connect();
			this.setProcedure("dbo.get_services_user(?)");
			this.setParameter(1, userId);
			return this.executeQuery();
		} finally {
			this.close();
		}
	}

	@Override
	public List<ServiceBean> select(ServiceBean arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void update(Integer serviceId) throws SQLException {
		try {
			this.connect();
			this.setProcedure("dbo.update_reindex_status(?,?)");
			this.setParameter(1, serviceId);
			this.setParameter(2, true);
			if (this.executeUpdate() == 0) {
				throw new SQLException();
			}
		} finally {
			this.close();
		}
	}

	@Override
	public void insert(ServiceBean service) throws SQLException {
		try {
			this.connect();
			this.setProcedure("dbo.insert_service(?,?,?)");
			this.setParameter(1, service.getUserId());
			this.setParameter(2, service.getUrl());
			this.setParameter(3, service.getProtocol());
			if (this.executeUpdate() == 0)
				throw new SQLException("Hubo un problema al insertar el servicio");
		} catch(SQLException e) {
			if (e.getMessage().contains("duplicate key value")) {
				throw new SQLException("El servicio ya se encuentra registrado");
			}
			throw e;
		} finally {
			this.close();
		}
	}

	@Override
	public void update(ServiceBean service) throws SQLException {
		try {
			this.connect();
			this.setProcedure("dbo.update_service(?,?,?)");
			this.setParameter(1, service.getServiceId());
			this.setParameter(2, service.getUrl());
			this.setParameter(3, service.getProtocol());
			if (this.executeUpdate() == 0) {
				throw new SQLException("El servicio a actualizar no existe");
			}
		} catch (SQLException e) {
			throw e;
		} finally {
			this.close();
		}

	}

	@Override
	public void delete(Integer id) throws SQLException {
		try {
			this.connect();
			this.setProcedure("dbo.delete_service(?)");
			this.setParameter(1, id);
			if (this.executeUpdate() == 0) {
				throw new SQLException();
			}
		} finally {
			this.close();
		}
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
	public void update(ServiceBean arg0, Integer arg1) throws SQLException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean valid(ServiceBean arg0) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

}
