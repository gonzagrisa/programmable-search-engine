package ar.edu.ubp.das.beans.search;

public class SearchBean {
	private Integer userId;
	private String query;
	private String sortBy;
	private String orderBy;
	private String type;
	private Integer page;
	private String dateFrom;
	private String dateTo;
	private Long results;
	private String terminos;
	private String date;

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public Long getResults() {
		return results;
	}

	public String getTerminos() {
		return terminos;
	}

	public void setResults(Long results) {
		this.results = results;
	}

	public void setTerminos(String terminos) {
		this.terminos = terminos;
	}

	public String getSortBy() {
		return sortBy;
	}

	public String getOrderBy() {
		return orderBy;
	}

	public void setSortBy(String sortBy) {
		this.sortBy = sortBy;
	}

	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}

	public String getDateFrom() {
		return dateFrom;
	}

	public String getDateTo() {
		return dateTo;
	}

	public void setDateFrom(String dateFrom) {
		this.dateFrom = dateFrom;
	}

	public void setDateTo(String dateTo) {
		this.dateTo = dateTo;
	}

	public Integer getUserId() {
		return userId;
	}

	public String getQuery() {
		return query;
	}

	public String getType() {
		return type;
	}

	public Integer getPage() {
		return page;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setPage(Integer page) {
		this.page = page;
	}

	@Override
	public String toString() {
		String output = "Query: " + query;
		output += "\nUserId: " + userId;
		output += "\nsort: " + sortBy;
		output += "\ntype: " + type;
		output += "\npage: " + page;
		output += "\ndateFrom: " + dateFrom;
		output += "\ndateTo: " + dateTo;
		return output;
	}

}
