package ar.edu.ubp.das.elastic;

import java.io.IOException;
import java.sql.SQLException;
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
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.index.search.MultiMatchQuery;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.significant.ParsedSignificantStringTerms;
import org.elasticsearch.search.aggregations.bucket.significant.SignificantTerms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;

import com.google.gson.Gson;

import ar.edu.ubp.das.beans.indexation.MetadataBean;
import ar.edu.ubp.das.beans.search.ResultsBean;
import ar.edu.ubp.das.beans.search.SearchBean;
import ar.edu.ubp.das.db.Dao;
import ar.edu.ubp.das.db.DaoFactory;
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
	public List<MetadataBean> get(Integer id, Boolean indexed) throws ElasticsearchException, Exception {
		/*
		 * Otra forma: MatchQueryBuilder userId = QueryBuilders.matchQuery("userId",
		 * id); MatchQueryBuilder notApproved = QueryBuilders.matchQuery("approved",
		 * false); BoolQueryBuilder query = QueryBuilders.boolQuery();
		 * query.must(userId).must(notApproved);
		 */
		QueryBuilder query = QueryBuilders.boolQuery().must(QueryBuilders.termQuery("userId", id))
				.must(QueryBuilders.termQuery("approved", indexed));
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
				.doc("approved", true, "title", meta.getTitle(), "tags", meta.getTags(), "filters", meta.getFilters())
				.setRefreshPolicy(REFRESH_POLICY);
		UpdateResponse updateResponse = client.update(request, RequestOptions.DEFAULT);
		logger.log(MyLogger.INFO,
				"Metadato " + meta.getId() + " actualizado. Respuesta: " + updateResponse.status().toString());
	}

	@Override
	public void increasePopularity(String id) throws IOException {
		UpdateRequest request = new UpdateRequest(INDEX, id);
		Script inline = new Script("ctx._source.popularity += 1");
		request.script(inline);
		UpdateResponse updateResponse = client.update(request, RequestOptions.DEFAULT);
		logger.log(MyLogger.INFO,
				"Metadato " + id + " actualizado. Respuesta: " + updateResponse.status().toString());
	}

	@Override
	public void updateBatch(List<MetadataBean> metadataList) throws ElasticsearchException, Exception {
		BulkRequest request = new BulkRequest();
		for (MetadataBean metadata : metadataList) {
			request.add(new UpdateRequest(INDEX, metadata.getId()).doc("approved", true, "title", metadata.getTitle(),
					"tags", metadata.getTags(), "filters", metadata.getFilters()));
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
	public void deleteByWebsiteId(Integer id) throws ElasticsearchException, Exception {
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
				logger.log(MyLogger.ERROR,
						"Error al eliminar Metadatos de pagina id: " + id + " . Error: " + e.getMessage());
			}
		};
		client.deleteByQueryAsync(request, RequestOptions.DEFAULT, listener);
	}
	
	@Override
	public void deleteByUserId(Integer id) throws ElasticsearchException, Exception {
		DeleteByQueryRequest request = new DeleteByQueryRequest(INDEX);
		request.setQuery(new TermQueryBuilder("userId", id));
		request.setRefresh(true);
		ActionListener<BulkByScrollResponse> listener = new ActionListener<BulkByScrollResponse>() {
			@Override
			public void onResponse(BulkByScrollResponse bulkResponse) {
				logger.log(MyLogger.INFO, "Metadatos generados por el usuario con id " + id + " eliminados");
			}

			@Override
			public void onFailure(Exception e) {
				logger.log(MyLogger.ERROR,
						"Error al eliminar los metadatos del usuario con id: " + id + " . Error: " + e.getMessage());
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

	@Override
	public ResultsBean search(SearchBean search) throws ElasticsearchException, Exception {
		QueryBuilder query;
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
		if (search.getType() != null && !search.getType().isEmpty()) {
			boolQueryBuilder.must(QueryBuilders.termQuery("type", search.getType()));
		}
		if (search.getDateFrom() != null && !search.getDateFrom().isEmpty()) {
			boolQueryBuilder.must(QueryBuilders.rangeQuery("date").gte(search.getDateFrom()));
		}
		if (search.getDateTo() != null && !search.getDateTo().isEmpty()) {
			boolQueryBuilder.must(QueryBuilders.rangeQuery("date").lte(search.getDateTo()));
		}
		boolQueryBuilder
				.must(QueryBuilders.termQuery("userId", search.getUserId()))
				.must(QueryBuilders.termQuery("approved", true))
				.must(QueryBuilders.multiMatchQuery(search.getQuery(), "tags", "text", "title", "URL").field("tags", 10)
						.field("text", 1).field("title", 2))
				.mustNot(QueryBuilders.matchQuery("filters", search.getQuery()));
		query = boolQueryBuilder;

		HighlightBuilder highlightBuilder = new HighlightBuilder().preTags("<strong>").postTags("</strong>")
				.fragmentSize(200).noMatchSize(200).field("text");
		SearchRequest searchRequest = new SearchRequest(INDEX);
		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		sourceBuilder.query(query);
		sourceBuilder.highlighter(highlightBuilder);
		sourceBuilder.trackTotalHits(true);
		sourceBuilder.from((search.getPage() - 1) * 10).size(10);

		if (search.getSortBy() != null && !search.getSortBy().isEmpty()) {
			sourceBuilder.sort(search.getSortBy(), search.getOrderBy().equals("asc") ? SortOrder.ASC : SortOrder.DESC);
		}

		String[] includeFields = new String[] {};
		String[] excludeFields = new String[] { "text", "topWords", "approved", "extension", "filters", "tags",
				"textLength", "userId", "websiteId" };

		sourceBuilder.fetchSource(includeFields, excludeFields);
		searchRequest.source(sourceBuilder);

		SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
		MetadataBean metadata;
		Gson gson = new Gson();
		System.out.println("Hit count: " + searchResponse.getHits().getTotalHits());
		ResultsBean results = new ResultsBean();
		results.setResultsAmount(searchResponse.getHits().getTotalHits().value);
		results.setTime(searchResponse.getTook().getMillisFrac());
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
			results.addResult(metadata);
		}
		if (search.getPage() == 1)
			writeSignificantWords(sourceBuilder, search);
		System.out.println("DEVOLVIENDO RESULTADOS");
		return results;
	}

	@Deprecated
	@Override
	public void significantWords(SearchBean search) throws ElasticsearchException, Exception {
		QueryBuilder query = buildQuery(search);
		String[] includeFields = new String[] {};
		String[] excludeFields = new String[] { "text", "topWords", "approved", "extension", "filters", "tags" };
		SearchRequest searchRequest = new SearchRequest(INDEX);
		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		AggregationBuilder aggregation = AggregationBuilders.significantText("keywords", "text");
		sourceBuilder.query(query);
		sourceBuilder.fetchSource(includeFields, excludeFields);
		sourceBuilder.aggregation(aggregation);
		sourceBuilder.size(0);
		sourceBuilder.trackTotalHits(true);
		searchRequest.source(sourceBuilder);
		SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
		ParsedSignificantStringTerms terms = searchResponse.getAggregations().get("keywords");
		System.out.println(searchResponse.getHits().getTotalHits().value);
		for (SignificantTerms.Bucket bucket : terms) {
			System.out.println(bucket.getKeyAsString());
		}
	}

	private void writeSignificantWords(SearchSourceBuilder sourceBuilder, SearchBean search) {
		AggregationBuilder aggregation = AggregationBuilders.significantText("keywords", "text");
		SearchRequest searchRequest = new SearchRequest(INDEX);
		sourceBuilder.aggregation(aggregation);
		searchRequest.source(sourceBuilder);
		client.searchAsync(searchRequest, RequestOptions.DEFAULT, new ActionListener<SearchResponse>() {
			@Override
			public void onResponse(SearchResponse response) {
				ParsedSignificantStringTerms terms = response.getAggregations().get("keywords");
				search.setResults(response.getHits().getTotalHits().value);
				List<String> terminos = new ArrayList<String>();
				for (SignificantTerms.Bucket bucket : terms) {
					terminos.add(bucket.getKeyAsString());
				}
				search.setTerminos(String.join(", ", terminos));

				Dao<SearchBean, SearchBean> daoStats;
				try {
					daoStats = DaoFactory.getDao("Query", "ar.edu.ubp.das");
					daoStats.insert(search);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			@Override
			public void onFailure(Exception e) {

			}
		});
	}

	private BoolQueryBuilder buildQuery(SearchBean search) {
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
		if (search.getType() != null) {
			boolQueryBuilder.must(QueryBuilders.termQuery("type", search.getType()));
		}
		if (search.getDateFrom() != null) {
			boolQueryBuilder.must(QueryBuilders.rangeQuery("date").gte(search.getDateFrom()));
		}
		if (search.getDateTo() != null) {
			boolQueryBuilder.must(QueryBuilders.rangeQuery("date").lte(search.getDateTo()));
		}
		boolQueryBuilder.must(QueryBuilders.termQuery("userId", search.getUserId()))
				.must(QueryBuilders.termQuery("approved", true))
				.must(QueryBuilders.multiMatchQuery(search.getQuery(), "tags", "text", "title", "URL").field("tags", 10)
						.field("text", 1).field("title", 2))
				.mustNot(QueryBuilders.matchQuery("filters", search.getQuery()));
		return boolQueryBuilder;
	}

}
