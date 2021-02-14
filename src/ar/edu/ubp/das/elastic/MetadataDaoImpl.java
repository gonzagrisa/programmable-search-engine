package ar.edu.ubp.das.elastic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpHost;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;

import com.google.gson.Gson;

import ar.edu.ubp.das.beans.MetadataBean;
import ar.edu.ubp.das.logging.MyLogger;

public class MetadataDaoImpl implements MetadataDao {
	RestHighLevelClient client;
	static final String INDEX = "metadata";
	static final String REFRESH_POLICY = "wait_for";
	private MyLogger logger;

	public MetadataDaoImpl() {
		this.logger = new MyLogger(this.getClass().getSimpleName());
		this.client = new RestHighLevelClient(
				RestClient.builder(new HttpHost("localhost", 9200, "http"), new HttpHost("localhost", 9201, "http")));
	}

	@Override
	public List<MetadataBean> get(Integer id) throws ElasticsearchException, Exception {
		/*
		 * Otra forma: MatchQueryBuilder userId = QueryBuilders.matchQuery("userId",
		 * id); MatchQueryBuilder notApproved = QueryBuilders.matchQuery("approved",
		 * false); BoolQueryBuilder query = QueryBuilders.boolQuery();
		 * query.must(userId).must(notApproved);
		 */
		QueryBuilder query = QueryBuilders.boolQuery().must(QueryBuilders.termQuery("userId", id))
				.must(QueryBuilders.termQuery("approved", false));
		HighlightBuilder highlightBuilder = new HighlightBuilder().postTags("").preTags("").fragmentSize(50)
				.noMatchSize(50).field("text");
		String[] includeFields = new String[] {};
		String[] excludeFields = new String[] { "text" };
		SearchRequest searchRequest = new SearchRequest(INDEX);
		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		sourceBuilder.size(10000);
		sourceBuilder.query(query);
		sourceBuilder.highlighter(highlightBuilder);
		sourceBuilder.fetchSource(includeFields, excludeFields);
		searchRequest.source(sourceBuilder);

		SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
		List<MetadataBean> metadataList = new ArrayList<MetadataBean>();
		MetadataBean metadata;
		Gson gson = new Gson();
		System.out.println("Hit count: " + searchResponse.getHits().getTotalHits());
		for (SearchHit hit : searchResponse.getHits().getHits()) {
			metadata = new MetadataBean();
			metadata = gson.fromJson(hit.getSourceAsString(), MetadataBean.class);
			metadata.setId(hit.getId());
			try {
				Text[] fragments = hit.getHighlightFields().get("text").fragments();
				metadata.setText(fragments[0].string());
			} catch (Exception e) {
				System.out.println("No fragment text");
			}
			metadataList.add(metadata);
		}
		return metadataList;
	}

	@Override
	public MetadataBean getId(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void update(MetadataBean meta) throws IOException {
		UpdateRequest request = new UpdateRequest(INDEX, meta.getId())
				.doc("approved", true,
					 "title", meta.getTitle(),
					 "tags", meta.getTags())
				.setRefreshPolicy(REFRESH_POLICY);
		UpdateResponse updateResponse = client.update(request, RequestOptions.DEFAULT);
		logger.log(MyLogger.INFO, "Metadato " + meta.getId()  +" actualizado. Respuesta: " + updateResponse.status().toString());
	}
	
	@Override
	public void updateBatch(List<MetadataBean> metadataList) throws ElasticsearchException, Exception {
		BulkRequest request = new BulkRequest();
		for (MetadataBean metadata : metadataList) {
			request.add(new UpdateRequest(INDEX, metadata.getId()).doc(
						"approved", true,
						"title", metadata.getTitle(),
						"tags", metadata.getTags(),
						"filters", metadata.getFilters()
					));
		}
		request.setRefreshPolicy(REFRESH_POLICY);
		BulkResponse bulkResponse = client.bulk(request, RequestOptions.DEFAULT);
		logger.log(MyLogger.INFO, "Metadatos actualizados. Respuesta: " + bulkResponse.status().toString());
	}

	@Override
	public void delete(String id) throws ElasticsearchException, Exception {
		DeleteRequest request = new DeleteRequest(INDEX, id);
		request.setRefreshPolicy(REFRESH_POLICY);
		DeleteResponse deleteResponse = client.delete(request, RequestOptions.DEFAULT);
		logger.log(MyLogger.INFO, "Metadatos eliminados. Respuesta: " + deleteResponse.status().toString());
	}

	@Override
	public void deleteWebsiteId(Integer id) throws ElasticsearchException, Exception {
		DeleteByQueryRequest request = new DeleteByQueryRequest(INDEX);
		request.setQuery(new TermQueryBuilder("websiteId", id));
		request.setRefresh(true);
		ActionListener<BulkByScrollResponse> listener = new ActionListener<BulkByScrollResponse>() {
		    @Override
		    public void onResponse(BulkByScrollResponse bulkResponse) {
		    	logger.log(MyLogger.INFO, "Metadatos generados a partir de pagina con id " + id + " eliminados");
		    }
		    @Override
		    public void onFailure(Exception e) {
		    	logger.log(MyLogger.ERROR, "Error al eliminar Metadatos de pagina id: " + id + " . Error: " + e.getMessage());
		    }
		};
		client.deleteByQueryAsync(request, RequestOptions.DEFAULT, listener);
	}

	@Override
	public void deleteBatch(List<MetadataBean> metadataList) throws ElasticsearchException, Exception {
		BulkRequest request = new BulkRequest();
		for (MetadataBean metadata : metadataList) {
			request.add(new DeleteRequest(INDEX, metadata.getId()));
		}
		client.bulkAsync(request, RequestOptions.DEFAULT, new ActionListener<BulkResponse>() {
		    @Override
		    public void onResponse(BulkResponse bulkResponse) {
		    	logger.log(MyLogger.INFO, "Metadatos eliminados");
		    }
		    @Override
		    public void onFailure(Exception e) {
		    	logger.log(MyLogger.ERROR, "Error al eliminar los Metadatos. Error: " + e.getMessage());
		    }
		});
	}
}
