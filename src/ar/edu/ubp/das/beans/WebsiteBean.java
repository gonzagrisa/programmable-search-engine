package ar.edu.ubp.das.beans;

public class WebsiteBean {
	private int userId;
	private String url;
	private boolean reindex;
	
	public int getUserId() {
		return userId;
	}
	
	public String getUrl() {
		return url;
	}
	
	public boolean isReindex() {
		return reindex;
	}
	
	public void setUserId(int userId) {
		this.userId = userId;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public void setReindex(boolean reindex) {
		this.reindex = reindex;
	}
}
