package ar.edu.ubp.das.beans;

public class ServiceBean {
	private int userId;
	private String URLresource;
	private String URLping;
	private boolean reindex;

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getURLresource() {
		return URLresource;
	}

	public void setURLresource(String uRLresource) {
		URLresource = uRLresource;
	}

	public String getURLping() {
		return URLping;
	}

	public void setURLping(String uRLping) {
		URLping = uRLping;
	}

	public boolean isReindex() {
		return reindex;
	}

	public void setReindex(boolean reindex) {
		this.reindex = reindex;
	}
}
