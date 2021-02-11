package ar.edu.ubp.das.elastic;

import java.util.List;

import org.elasticsearch.ElasticsearchException;

public interface MetadataDao {
	public List<Metadata> get(Integer id) throws ElasticsearchException, Exception;
	public Metadata getId(String id) throws ElasticsearchException, Exception;
	public void update(String id) throws ElasticsearchException, Exception;
	public void delete(String id) throws ElasticsearchException, Exception;
}
