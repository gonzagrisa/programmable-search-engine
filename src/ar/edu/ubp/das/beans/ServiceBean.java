package ar.edu.ubp.das.beans;

public class ServiceBean {
	private int userId;
	private int serviceId;
	private String URLResource;
	private String URLPing;
	private boolean reindex;

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}
	
	public int getServiceId() {
		return serviceId;
	}

	public void setServiceId(int serviceId) {
		this.serviceId = userId;
	}

	public String getURLResource() {
		return URLResource;
	}

	public void setURLResource(String uRLresource) {
		URLResource = uRLresource;
	}

	public String getURLPing() {
		return URLPing;
	}

	public void setURLPing(String uRLping) {
		URLPing = uRLping;
	}

	public boolean isReindex() {
		return reindex;
	}

	public void setReindex(boolean reindex) {
		this.reindex = reindex;
	}
}
