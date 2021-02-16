package ar.edu.ubp.das.daos.stats;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import ar.edu.ubp.das.beans.search.SearchBean;
import ar.edu.ubp.das.db.Dao;

public class MSQueryDao extends Dao<SearchBean, SearchBean> {

	@Override
	public SearchBean make(ResultSet result) throws SQLException {
		SearchBean search = new SearchBean();
		search.setQuery(result.getString("query"));
		search.setDate(result.getString("date"));
		search.setResults(result.getLong("results"));
		search.setSortBy(result.getString("sortBy"));
		search.setOrderBy(result.getString("orderBy"));
		search.setType(result.getString("tipoArchivo"));
		search.setTerminos(result.getString("terminos_relevantes"));
		return search;
	}
	
	@Override
	public void insert(SearchBean busqueda) throws SQLException {
		try {
			this.connect();
			this.setProcedure("dbo.insert_busqueda(?,?,?,?,?,?,?)");
			this.setParameter(1, busqueda.getUserId());
			this.setParameter(2, busqueda.getQuery());
			this.setParameter(3, busqueda.getResults());
			this.setParameter(4, busqueda.getSortBy());
			this.setParameter(5, busqueda.getOrderBy());
			this.setParameter(6, busqueda.getType());
			this.setParameter(7, busqueda.getTerminos());
			this.executeUpdate();
		} finally {
			this.close();
		}
	}
	
	@Override
	public List<SearchBean> select(Integer userId) throws SQLException {
		try {
			this.connect();
			this.setProcedure("dbo.get_busquedas(?)");
			if (userId == null) {
				this.setNull(1, java.sql.Types.INTEGER);
			} else {
				this.setParameter(1, userId);				
			}
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
	public void delete(SearchBean arg0) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete(SearchBean arg0, Integer arg1) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public SearchBean find(Integer arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SearchBean find(SearchBean arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void insert(SearchBean arg0, Integer arg1) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<SearchBean> select() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SearchBean> select(SearchBean arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void update(Integer arg0) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(SearchBean arg0) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(SearchBean arg0, Integer arg1) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean valid(SearchBean arg0) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}
	
}
