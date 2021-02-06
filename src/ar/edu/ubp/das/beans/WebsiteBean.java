package ar.edu.ubp.das.beans;

import javax.json.bind.annotation.JsonbTransient;

public class WebsiteBean {
	private Integer websiteId;
	private Integer userId;
	private String url;
	private Boolean isActive;
	private Boolean reindex;
	private Boolean indexed;
	
	public Integer getWebsiteId() {
		return websiteId;
	}

	public void setWebsiteId(Integer websiteId) {
		this.websiteId = websiteId;
	}
	
	public Boolean getIndexed() {
		return indexed;
	}

	public void setIndexed(Boolean indexed) {
		this.indexed = indexed;
	}

	public Integer getUserId() {
		return userId;
	}

	public String getUrl() {
		return url;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public Boolean getReindex() {
		return reindex;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	public void setReindex(Boolean reindex) {
		this.reindex = reindex;
	}

	// Datos obligatorios: userId y url
	@JsonbTransient
	public boolean isValid() {
		return this.getUserId() != null &&
				this.getUrl() != null;
	}
	
}
