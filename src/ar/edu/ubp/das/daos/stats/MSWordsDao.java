package ar.edu.ubp.das.daos.stats;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import ar.edu.ubp.das.beans.WordBean;
import ar.edu.ubp.das.db.Dao;

public class MSWordsDao extends Dao<WordBean, WordBean> {

	@Override
	public void delete(Integer arg0) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete(WordBean arg0) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete(WordBean arg0, Integer arg1) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public WordBean find(Integer arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WordBean find(WordBean arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void insert(WordBean word) throws SQLException {
		try {
			this.connect();
			this.setProcedure("dbo.insert_palabra(?,?)");
			this.setParameter(1, word.getUserId());
			this.setParameter(2, word.getWord());
			this.executeUpdate();
		} finally {
			this.close();
		}
		
	}

	@Override
	public void insert(WordBean arg0, Integer arg1) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public WordBean make(ResultSet arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<WordBean> select() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<WordBean> select(Integer arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<WordBean> select(WordBean arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void update(Integer arg0) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(WordBean arg0) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(WordBean arg0, Integer arg1) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean valid(WordBean arg0) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

}
