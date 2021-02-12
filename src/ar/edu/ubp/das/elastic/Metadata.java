package ar.edu.ubp.das.elastic;

import java.util.List;

public class Metadata {
	private String id;
	private Integer userId;
	private Integer websiteId;
	private String URL;
	private String type;
	private String extension;
	private String title;
	private String text;
	private Integer textLength;
	private Boolean approved;
	private Integer popularity;
	private List<String> topWords;
	private String[] tags;
	
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

	public String getExtension() {
		return extension;
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

	public Integer getPopularity() {
		return popularity;
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

	public void setExtension(String extension) {
		this.extension = extension;
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

	public void setPopularity(Integer popularity) {
		this.popularity = popularity;
	}

	public void setWebsiteId(Integer websiteId) {
		this.websiteId = websiteId;
	}
}
