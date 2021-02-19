package ar.edu.ubp.das.beans.indexation;

import javax.json.bind.annotation.JsonbTransient;

public class ServiceBean extends StatusBean {
	private Integer serviceId;
	private Integer userId;
	private String 	Url;
	private String 	protocol;
	
	@JsonbTransient
	public Boolean isValid() {
		return this.Url != null &&
				this.protocol 	!= null;
	}

	public Integer getServiceId() {
		return serviceId;
	}

	public Integer getUserId() {
		return userId;
	}

	public String getUrl() {
		return Url;
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

	public void setUrl(String url) {
		this.Url = url;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	@Override
	public String toString() {
		String output = "userId: " + userId;
		output += "\nserviceId: " + serviceId;
		output += "\nUrl: " + Url;
		output += "\nprotocol: " + protocol;
		output += super.toString();
		return output;
	}
}
