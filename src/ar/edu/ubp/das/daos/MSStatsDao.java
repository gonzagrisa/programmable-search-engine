package ar.edu.ubp.das.daos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import ar.edu.ubp.das.beans.stats.StatsToShowBean;
import ar.edu.ubp.das.db.Dao;

public class MSStatsDao extends Dao<StatsToShowBean, StatsToShowBean>{

	@Override
	public StatsToShowBean make(ResultSet result) throws SQLException {
		StatsToShowBean stats = new StatsToShowBean();
		stats.setPopularidad(result.getInt("popularidad"));
		stats.setFecha(result.getInt("fecha"));
		stats.setAscendente(result.getInt("ascendente"));
		stats.setDescendente(result.getInt("descendente"));
		stats.setSinResultados(result.getInt("sin_resultados"));
		stats.setConResultados(result.getInt("con_resultados"));
		stats.setRealizadasHoy(result.getInt("realizadas_hoy"));
		stats.setTotales(result.getInt("totales"));
		return stats;
	}
	
	@Override
	public List<StatsToShowBean> select(Integer userId) throws SQLException {
		try {
			this.connect();
			this.setProcedure("dbo.get_cantidades(?)");
			this.setParameter(1, userId);
			return this.executeQuery();
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			this.close();
		}
		return null;
	}
	
	@Override
	public List<StatsToShowBean> select() throws SQLException {
		try {
			this.connect();
			this.setProcedure("dbo.get_cantidades(?)");
			this.setNull(1, java.sql.Types.INTEGER);
			return this.executeQuery();
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			this.close();
		}
		return null;
	}

	@Override
	public void delete(Integer arg0) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete(StatsToShowBean arg0) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete(StatsToShowBean arg0, Integer arg1) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public StatsToShowBean find(Integer arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StatsToShowBean find(StatsToShowBean arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void insert(StatsToShowBean arg0) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void insert(StatsToShowBean arg0, Integer arg1) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<StatsToShowBean> select(StatsToShowBean arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void update(Integer arg0) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(StatsToShowBean arg0) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(StatsToShowBean arg0, Integer arg1) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean valid(StatsToShowBean arg0) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

}
