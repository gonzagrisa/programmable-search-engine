package ar.edu.ubp.das.beans;

public class WebsiteBean {
	private Integer userId;
	private String url;
	
	public Integer getUserId() {
		return userId;
	}
	
	public String getUrl() {
		return url;
	}
	
	public void setUserId(int userId) {
		this.userId = userId;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	// Datos obligatorios: userId y url
	public boolean isValid() {
		return this.getUserId() != null &&
				this.getUrl() != null;
	}
}
