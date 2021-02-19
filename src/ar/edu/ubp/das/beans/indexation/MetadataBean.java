package ar.edu.ubp.das.beans.indexation;

import java.util.List;

public class MetadataBean {
	private String id;
	private Integer userId;
	private Integer websiteId;
	private String URL;
	private String type;
	private String title;
	private String text;
	private Integer textLength;
	private Boolean approved;
	private Integer visitors;
	private List<String> topWords;
	private String[] tags;
	private String[] filters;
	private String date;
	
	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public Integer getVisitors() {
		return visitors;
	}

	public void setVisitors(Integer visitors) {
		this.visitors = visitors;
	}

	public String[] getFilters() {
		return filters;
	}

	public void setFilters(String[] filters) {
		this.filters = filters;
	}

	public String[] getTags() {
		return tags;
	}

	public void setTags(String[] tags) {
		this.tags = tags;
	}

	public List<String> getTopWords() {
		return topWords;
	}

	public void setTopWords(List<String> topWords) {
		this.topWords = topWords;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Integer getUserId() {
		return userId;
	}

	public String getURL() {
		return URL;
	}

	public String getType() {
		return type;
	}

	public String getTitle() {
		return title;
	}

	public String getText() {
		return text;
	}

	public Integer getTextLength() {
		return textLength;
	}

	public Boolean getApproved() {
		return approved;
	}

	public Integer getWebsiteId() {
		return websiteId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public void setURL(String uRL) {
		URL = uRL;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setText(String text) {
		this.text = text;
	}

	public void setTextLength(Integer textLength) {
		this.textLength = textLength;
	}

	public void setApproved(Boolean approved) {
		this.approved = approved;
	}

	public void setWebsiteId(Integer websiteId) {
		this.websiteId = websiteId;
	}
}
