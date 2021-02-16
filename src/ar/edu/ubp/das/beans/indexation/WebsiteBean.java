package ar.edu.ubp.das.beans.indexation;

import javax.json.bind.annotation.JsonbTransient;

public class WebsiteBean extends StatusBean {
	private Integer websiteId;
	private Integer userId;
	private String url;
	private Integer serviceId;
	
	public Integer getServiceId() {
		return serviceId;
	}

	public void setServiceId(Integer serviceId) {
		this.serviceId = serviceId;
	}

	public Integer getWebsiteId() {
		return websiteId;
	}

	public void setWebsiteId(Integer websiteId) {
		this.websiteId = websiteId;
	}
	
	public Integer getUserId() {
		return userId;
	}

	public String getUrl() {
		return url;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}

	// Datos obligatorios: userId y url
	@JsonbTransient
	public boolean isValid() {
		return this.getUserId() != null &&
				this.getUrl() != null;
	}
	
	@Override
	public String toString() {
		String output = "Website Id: " + websiteId;
		output += "User Id: " + userId;
		output += "Url: " + url;
		output += "Service Id: " + serviceId;
		output += super.toString();
		return output;
	}
}
