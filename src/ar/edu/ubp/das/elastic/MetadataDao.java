package ar.edu.ubp.das.elastic;

import java.util.List;

import org.elasticsearch.ElasticsearchException;

import ar.edu.ubp.das.beans.MetadataBean;

public interface MetadataDao {
	public List<MetadataBean> get(Integer id) throws ElasticsearchException, Exception;
	public MetadataBean getId(String id) throws ElasticsearchException, Exception;
	public void deleteWebsiteId(Integer id) throws ElasticsearchException, Exception;
	public void update(MetadataBean meta) throws ElasticsearchException, Exception;
	public void delete(String id) throws ElasticsearchException, Exception;
	public void updateBatch(List<MetadataBean> metadataList) throws ElasticsearchException, Exception;
	public void deleteBatch(List<MetadataBean> metadata) throws ElasticsearchException, Exception;
}
