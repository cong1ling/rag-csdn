/*
 * Copyright 2024-2027 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.cloud.ai.vectorstore.dashvector;

import com.aliyun.dashvector.DashVectorCollection;
import com.aliyun.dashvector.models.Doc;
import com.aliyun.dashvector.models.Vector;
import com.aliyun.dashvector.models.requests.DeleteDocRequest;
import com.aliyun.dashvector.models.requests.QueryDocRequest;
import com.aliyun.dashvector.models.requests.UpsertDocRequest;
import com.aliyun.dashvector.models.responses.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingOptions;
import org.springframework.ai.vectorstore.AbstractVectorStoreBuilder;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.observation.AbstractObservationVectorStore;
import org.springframework.ai.vectorstore.observation.VectorStoreObservationContext;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * DashVector Vector Store implementation.
 * Supports vector similarity search with metadata filtering.
 *
 * @author Your Name
 * @since 2027-03-05
 */
public class DashVectorStore extends AbstractObservationVectorStore implements InitializingBean {

	private static final Logger logger = LoggerFactory.getLogger(DashVectorStore.class);

	private static final String DATA_BASE_SYSTEM = "dashvector";

	private static final int DEFAULT_DIMENSION = 1536;

	private final DashVectorCollection collection;

	private final String collectionName;

	private final Integer defaultTopK;

	private final Double defaultSimilarityThreshold;

	private final boolean initializeSchema;

	private final String metric;

	private final DashVectorFilterExpressionConverter filterExpressionConverter;

	protected DashVectorStore(Builder builder) {
		super(builder);
		this.collection = builder.collection;
		this.collectionName = builder.collectionName;
		this.defaultTopK = builder.defaultTopK;
		this.defaultSimilarityThreshold = builder.defaultSimilarityThreshold;
		this.initializeSchema = builder.initializeSchema;
		this.metric = builder.metric;
		this.filterExpressionConverter = new DashVectorFilterExpressionConverter();
	}

	public static Builder builder(DashVectorCollection collection, String collectionName, EmbeddingModel embeddingModel) {
		return new Builder(collection, collectionName, embeddingModel);
	}

	@Override
	public void afterPropertiesSet() {
		if (!this.initializeSchema) {
			return;
		}
		logger.info("DashVector collection '{}' is ready (initializeSchema is enabled, but collection management should be done externally)",
			collectionName);
	}

	@Override
	public void doAdd(List<Document> documents) {
		Assert.notNull(documents, "The document list should not be null.");
		if (CollectionUtils.isEmpty(documents)) {
			return;
		}

		List<float[]> embeddings = this.embeddingModel.embed(documents, EmbeddingOptions.builder().build(),
				this.batchingStrategy);

		List<Doc> docs = new ArrayList<>();
		for (int i = 0; i < documents.size(); i++) {
			Document doc = documents.get(i);
			float[] embedding = embeddings.get(i);

			// Convert float[] to List<Float>
			List<Float> vectorList = new ArrayList<>(embedding.length);
			for (float v : embedding) {
				vectorList.add(v);
			}

			// Build DashVector Doc using builder pattern
			var docBuilder = Doc.builder()
				.vector(Vector.builder().value(vectorList).build());

			// Set ID if present
			if (doc.getId() != null && !doc.getId().isEmpty()) {
				docBuilder.id(doc.getId());
			}

			// Set metadata as fields
			if (doc.getMetadata() != null && !doc.getMetadata().isEmpty()) {
				for (Map.Entry<String, Object> entry : doc.getMetadata().entrySet()) {
					docBuilder.field(entry.getKey(), entry.getValue());
				}
			}

			// Add document text as a field
			docBuilder.field("content", doc.getText());

			docs.add(docBuilder.build());
		}

		// Use upsert to avoid duplicate ID issues
		UpsertDocRequest request = UpsertDocRequest.builder().docs(docs).build();
		Response<?> response = collection.upsert(request);

		if (!response.isSuccess()) {
			throw new RuntimeException("Failed to add documents to DashVector: " + response.getMessage());
		}

		logger.debug("Successfully added {} documents to DashVector collection '{}'", documents.size(), collectionName);
	}

	@Override
	public void doDelete(List<String> ids) {
		if (CollectionUtils.isEmpty(ids)) {
			return;
		}

		DeleteDocRequest request = DeleteDocRequest.builder().ids(ids).build();
		Response<?> response = collection.delete(request);

		if (!response.isSuccess()) {
			throw new RuntimeException("Failed to delete documents from DashVector: " + response.getMessage());
		}

		logger.debug("Successfully deleted {} documents from DashVector collection '{}'", ids.size(), collectionName);
	}

	@Override
	public void doDelete(Filter.Expression filterExpression) {
		throw new UnsupportedOperationException(
			"DashVector does not support filter-based deletion. " +
			"Use doDelete(List<String> ids) instead."
		);
	}

	@Override
	public List<Document> similaritySearch(String query) {
		return this.similaritySearch(SearchRequest.builder()
			.query(query)
			.topK(this.defaultTopK)
			.similarityThreshold(this.defaultSimilarityThreshold)
			.build());
	}

	@Override
	public List<Document> doSimilaritySearch(SearchRequest searchRequest) {
		Assert.notNull(searchRequest, "SearchRequest must not be null");

		// Generate query embedding
		float[] queryEmbedding = this.embeddingModel.embed(searchRequest.getQuery());

		// Convert to List<Float>
		List<Float> queryVector = new ArrayList<>(queryEmbedding.length);
		for (float v : queryEmbedding) {
			queryVector.add(v);
		}

		// Build query request using builder pattern
		var requestBuilder = QueryDocRequest.builder()
			.vector(Vector.builder().value(queryVector).build())
			.topk(searchRequest.getTopK())
			.includeVector(false);

		// Add filter if present
		if (searchRequest.getFilterExpression() != null) {
			String filterExpression = filterExpressionConverter.convertExpression(searchRequest.getFilterExpression());
			requestBuilder.filter(filterExpression);
		}

		// Execute query
		Response<List<Doc>> response = collection.query(requestBuilder.build());

		if (!response.isSuccess()) {
			throw new RuntimeException("Failed to query DashVector: " + response.getMessage());
		}

		List<Doc> results = response.getOutput();
		if (results == null || results.isEmpty()) {
			return new ArrayList<>();
		}

		// Get similarity threshold
		double similarityThreshold = searchRequest.getSimilarityThreshold();

		return results.stream()
			.filter(doc -> matchesSimilarityThreshold(doc.getScore(), similarityThreshold))
			.map(this::convertToDocument)
			.collect(Collectors.toList());
	}

	private boolean matchesSimilarityThreshold(Float rawScore, double similarityThreshold) {
		if (rawScore == null) {
			return false;
		}

		if (isEuclideanMetric()) {
			return rawScore <= similarityThreshold;
		}

		return convertScoreToSimilarity(rawScore) >= similarityThreshold;
	}

	private double convertScoreToSimilarity(float rawScore) {
		if (isCosineMetric()) {
			return 1.0d - rawScore;
		}

		return rawScore;
	}

	private boolean isCosineMetric() {
		return "cosine".equalsIgnoreCase(this.metric);
	}

	private boolean isEuclideanMetric() {
		return "euclidean".equalsIgnoreCase(this.metric);
	}

	private Document convertToDocument(Doc doc) {
		Map<String, Object> metadata = new HashMap<>();
		if (doc.getFields() != null) {
			metadata.putAll(doc.getFields());
		}

		// Extract content from fields
		String content = "";
		if (metadata.containsKey("content")) {
			Object contentObj = metadata.get("content");
			if (contentObj != null) {
				content = contentObj.toString();
			}
			// Remove content from metadata to avoid duplication
			metadata.remove("content");
		}

		// Add score to metadata
		Float rawScore = doc.getScore();
		Double score = null;
		if (rawScore != null) {
			if (isCosineMetric() || isEuclideanMetric()) {
				metadata.put("distance", rawScore.doubleValue());
			}
			score = convertScoreToSimilarity(rawScore);
			metadata.put("score", score);
		}

		return Document.builder()
			.id(doc.getId())
			.text(content)
			.metadata(metadata)
			.score(score != null ? score.doubleValue() : null)
			.build();
	}

	@Override
	public VectorStoreObservationContext.Builder createObservationContextBuilder(String operationName) {
		return VectorStoreObservationContext.builder(DATA_BASE_SYSTEM, operationName)
			.collectionName(this.collectionName)
			.dimensions(this.embeddingModel.dimensions());
	}

	public static class Builder extends AbstractVectorStoreBuilder<Builder> {

		private final DashVectorCollection collection;

		private final String collectionName;

		private int defaultTopK = 10;

		private Double defaultSimilarityThreshold = 0.0;

		private boolean initializeSchema = false;

		private String metric = "cosine";

		private Builder(DashVectorCollection collection, String collectionName, EmbeddingModel embeddingModel) {
			super(embeddingModel);
			Assert.notNull(collection, "DashVectorCollection must not be null");
			Assert.hasText(collectionName, "Collection name must not be empty");
			this.collection = collection;
			this.collectionName = collectionName;
		}

		public Builder defaultTopK(int defaultTopK) {
			Assert.isTrue(defaultTopK >= 0, "The topK should be positive value.");
			this.defaultTopK = defaultTopK;
			return this;
		}

		public Builder defaultSimilarityThreshold(Double defaultSimilarityThreshold) {
			Assert.isTrue(defaultSimilarityThreshold >= 0.0 && defaultSimilarityThreshold <= 1.0,
					"The similarity threshold must be in range [0.0:1.0].");
			this.defaultSimilarityThreshold = defaultSimilarityThreshold;
			return this;
		}

		public Builder initializeSchema(boolean initializeSchema) {
			this.initializeSchema = initializeSchema;
			return this;
		}

		public Builder metric(String metric) {
			this.metric = metric;
			return this;
		}

		@Override
		public DashVectorStore build() {
			return new DashVectorStore(this);
		}

	}

}
