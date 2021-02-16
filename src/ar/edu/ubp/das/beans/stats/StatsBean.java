package ar.edu.ubp.das.beans.stats;

public class StatsBean {
	private Integer userId;
	private String query;
	private Long results;
	private String terminos;

	public Integer getUserId() {
		return userId;
	}

	public String getQuery() {
		return query;
	}

	public Long getResults() {
		return results;
	}

	public String getTerminos() {
		return terminos;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public void setResults(Long results) {
		this.results = results;
	}

	public void setTerminos(String terminos) {
		this.terminos = terminos;
	}
}
