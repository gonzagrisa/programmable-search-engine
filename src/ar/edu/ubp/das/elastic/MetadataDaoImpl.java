package ar.edu.ubp.das.elastic;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpHost;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;

import com.google.gson.Gson;

public class MetadataDaoImpl implements MetadataDao {
	RestHighLevelClient client;
	static final String INDEX = "metadata";

	public MetadataDaoImpl() {
		this.client = new RestHighLevelClient(
				RestClient.builder(new HttpHost("localhost", 9200, "http"), new HttpHost("localhost", 9201, "http")));
	}

	@Override
	public List<Metadata> get(Integer id) throws ElasticsearchException, Exception {
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
		sourceBuilder.size(1000);
		sourceBuilder.query(query);
		sourceBuilder.highlighter(highlightBuilder);
		sourceBuilder.fetchSource(includeFields, excludeFields);
		searchRequest.source(sourceBuilder);

		SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
		List<Metadata> metadataList = new ArrayList<Metadata>();
		Metadata metadata;
		Gson gson = new Gson();
		System.out.println("Hit count: " + searchResponse.getHits().getTotalHits());
		for (SearchHit hit : searchResponse.getHits().getHits()) {
			metadata = new Metadata();
			metadata = gson.fromJson(hit.getSourceAsString(), Metadata.class);
			metadata.setId(hit.getId());
			Text[] fragments = hit.getHighlightFields().get("text").fragments();
			metadata.setText(fragments[0].string());
			metadataList.add(metadata);
		}
		return metadataList;
	}

	@Override
	public Metadata getId(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void update(String id) {
		// TODO Auto-generated method stub

	}

	@Override
	public void delete(String id) throws ElasticsearchException, Exception {
		DeleteRequest request = new DeleteRequest(INDEX, id);
		this.client.delete(request, RequestOptions.DEFAULT);
	}

}
