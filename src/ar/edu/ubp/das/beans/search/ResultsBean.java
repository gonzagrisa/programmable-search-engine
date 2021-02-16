package ar.edu.ubp.das.beans.search;

import java.util.ArrayList;
import java.util.List;

import ar.edu.ubp.das.beans.indexation.MetadataBean;

public class ResultsBean {
	private Long resultsAmount;
	private Double time;
	private List<MetadataBean> results;
	
	public ResultsBean() {
		this.results = new ArrayList<MetadataBean>();
	}

	public Long getResultsAmount() {
		return resultsAmount;
	}

	public Double getTime() {
		return time;
	}

	public List<MetadataBean> getResults() {
		return results;
	}

	public void setResultsAmount(Long resultsAmount) {
		this.resultsAmount = resultsAmount;
	}

	public void setTime(Double time) {
		this.time = time;
	}

	public void setResults(List<MetadataBean> results) {
		this.results = results;
	}
	
	public void addResult(MetadataBean result) {
		this.results.add(result);
	}

}
