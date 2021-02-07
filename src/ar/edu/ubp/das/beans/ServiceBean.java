package ar.edu.ubp.das.beans;

import javax.json.bind.annotation.JsonbTransient;

public class ServiceBean {
	private Integer serviceId;
	private Integer userId;
	private String 	URLResource;
	private String 	URLPing;
	private String 	protocol;
	private Boolean reindex;
	private Boolean indexed;
	private Boolean isUp;
		
	@JsonbTransient
	public Boolean isValid() {
		return this.URLResource != null &&
				this.URLPing 	!= null &&
				this.protocol 	!= null;
	}

	public Integer getServiceId() {
		return serviceId;
	}

	public Integer getUserId() {
		return userId;
	}

	public String getURLResource() {
		return URLResource;
	}

	public String getURLPing() {
		return URLPing;
	}

	public String getProtocol() {
		return protocol;
	}

	public Boolean getReindex() {
		return reindex;
	}

	public Boolean getIndexed() {
		return indexed;
	}

	public Boolean getIsUp() {
		return isUp;
	}

	public void setServiceId(Integer serviceId) {
		this.serviceId = serviceId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public void setURLResource(String uRLResource) {
		URLResource = uRLResource;
	}

	public void setURLPing(String uRLPing) {
		URLPing = uRLPing;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public void setReindex(Boolean reindex) {
		this.reindex = reindex;
	}

	public void setIndexed(Boolean indexed) {
		this.indexed = indexed;
	}

	public void setIsUp(Boolean isUp) {
		this.isUp = isUp;
	}

	@Override
	public String toString() {
		String output = "\nuserId: " + userId;
		output += "\nserviceId: " + serviceId;
		output += "\nURLResource: " + URLResource;
		output += "\nURLPing: " + URLPing;
		output += "\nprotocol: " + protocol;
		output += "\nreindex: " + reindex;
		output += "\nindexed: " + indexed;
		output += "\nisUp: " + isUp;
		return output;
	}
}
