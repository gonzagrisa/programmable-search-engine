package ar.edu.ubp.das.beans;

public class PreferencesBean {
	private int userId;
	private Double borderWidth;
	private Integer borderRadius;
	private String iconURL;
	private Integer iconSize;
	private String placeholder;
	private String color;
	
	public Double getBorderWidth() {
		return borderWidth;
	}

	public Integer getIconSize() {
		return iconSize;
	}

	public String getPlaceholder() {
		return placeholder;
	}

	public void setBorderWidth(Double borderWidth) {
		this.borderWidth = borderWidth;
	}

	public void setBorderRadius(Integer borderRadius) {
		this.borderRadius = borderRadius;
	}

	public void setIconSize(Integer iconSize) {
		this.iconSize = iconSize;
	}

	public void setPlaceholder(String placeholder) {
		this.placeholder = placeholder;
	}

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
	
	public void setColor(String color) {
		this.color = color;
	}
	
	public void setIconURL(String iconURL) {
		this.iconURL = iconURL;
	}
	
	public void setBorderRadius(int borderRadius) {
		this.borderRadius = borderRadius;
	}
}
