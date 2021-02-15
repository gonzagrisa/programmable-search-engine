package ar.edu.ubp.das.beans;

public class SearchBean {
	private Integer userId;
	private String query;
	private String sort;
	private String type;
	private Integer page;
	private String dateFrom;
	private String dateTo;
	
	public String getSort() {
		return sort;
	}

	public void setSort(String sort) {
		this.sort = sort;
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
		output += "\nsort: " + sort;
		output += "\ntype: " + type;
		output += "\npage: " + page;
		output += "\ndateFrom: " + dateFrom;
		output += "\ndateTo: " + dateTo;
		return output;
	}
	
}
