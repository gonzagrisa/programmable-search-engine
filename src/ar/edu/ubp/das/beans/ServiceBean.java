package ar.edu.ubp.das.beans;

import javax.json.bind.annotation.JsonbTransient;

public class ServiceBean extends StatusBean {
	private Integer serviceId;
	private Integer userId;
	private String 	URLResource;
	private String 	URLPing;
	private String 	protocol;
	
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

	@Override
	public String toString() {
		String output = "userId: " + userId;
		output += "\nserviceId: " + serviceId;
		output += "\nURLResource: " + URLResource;
		output += "\nURLPing: " + URLPing;
		output += "\nprotocol: " + protocol;
		output += super.toString();
		return output;
	}
}
