package ar.edu.ubp.das.beans;

public class PreferencesBean {
	private int userId;
	private String color;
	private String iconURL;
	private int borderRadius;
	private int fontSize;
	
	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getColor() {
		return color;
	}
	
	public String getIconURL() {
		return iconURL;
	}
	
	public int getBorderRadius() {
		return borderRadius;
	}
	
	public int getFontSize() {
		return fontSize;
	}
	
	public void setColor(String color) {
		this.color = color;
	}
	
	public void setIconURL(String iconURL) {
		this.iconURL = iconURL;
	}
	
	public void setBorderRadius(int borderRadius) {
		this.borderRadius = borderRadius;
	}
	
	public void setFontSize(int fontSize) {
		this.fontSize = fontSize;
	}	
}
