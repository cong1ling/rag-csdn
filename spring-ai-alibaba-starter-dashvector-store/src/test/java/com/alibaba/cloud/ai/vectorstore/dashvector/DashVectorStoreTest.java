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
import com.aliyun.dashvector.models.requests.QueryDocRequest;
import com.aliyun.dashvector.models.responses.Response;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.observation.VectorStoreObservationContext;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test class for DashVectorStore
 *
 * @author Your Name
 * @since 2027-03-05
 */
class DashVectorStoreTest {

	@Test
	void testCreateObservationContextBuilder() {
		// Create mock dependencies
		DashVectorCollection mockCollection = mock(DashVectorCollection.class);
		EmbeddingModel mockEmbeddingModel = mock(EmbeddingModel.class);
		when(mockEmbeddingModel.dimensions()).thenReturn(1536);

		// Create DashVectorStore instance
		DashVectorStore vectorStore = DashVectorStore
			.builder(mockCollection, "test_collection", mockEmbeddingModel)
			.build();

		// Test createObservationContextBuilder
		VectorStoreObservationContext.Builder builder = vectorStore
			.createObservationContextBuilder("test_operation");

		// Verify the builder is not null
		assertThat(builder).isNotNull();

		// Verify the builder has the correct properties
		VectorStoreObservationContext context = builder.build();
		assertThat(context.getDatabaseSystem()).isEqualTo("dashvector");
		assertThat(context.getOperationName()).isEqualTo("test_operation");
		assertThat(context.getCollectionName()).isEqualTo("test_collection");
		assertThat(context.getDimensions()).isEqualTo(1536);
	}

	@Test
	void testInitializeSchemaDefaultsFalse() throws Exception {
		DashVectorCollection mockCollection = mock(DashVectorCollection.class);
		EmbeddingModel mockEmbeddingModel = mock(EmbeddingModel.class);

		DashVectorStore vectorStore = DashVectorStore
			.builder(mockCollection, "test_collection", mockEmbeddingModel)
			.build();

		// afterPropertiesSet should be a no-op when initializeSchema is false (default)
		vectorStore.afterPropertiesSet();

		// No exception should be thrown
		assertThat(vectorStore).isNotNull();
	}

	@Test
	void testInitializeSchemaTrue() throws Exception {
		DashVectorCollection mockCollection = mock(DashVectorCollection.class);
		EmbeddingModel mockEmbeddingModel = mock(EmbeddingModel.class);

		DashVectorStore vectorStore = DashVectorStore
			.builder(mockCollection, "test_collection", mockEmbeddingModel)
			.initializeSchema(true)
			.build();

		// afterPropertiesSet should just log a message
		vectorStore.afterPropertiesSet();

		// No exception should be thrown
		assertThat(vectorStore).isNotNull();
	}

	@Test
	void testSimilaritySearchUsesConfiguredDefaults() {
		DashVectorCollection mockCollection = mock(DashVectorCollection.class);
		EmbeddingModel mockEmbeddingModel = mock(EmbeddingModel.class);
		Response<List<Doc>> mockResponse = mock(Response.class);
		Doc mockDoc = mock(Doc.class);

		when(mockEmbeddingModel.embed("Spring AI")).thenReturn(new float[] { 0.1f, 0.2f });
		when(mockDoc.getId()).thenReturn("doc-1");
		when(mockDoc.getFields()).thenReturn(Map.of("content", "Spring AI content"));
		when(mockDoc.getScore()).thenReturn(0.2f);
		when(mockResponse.isSuccess()).thenReturn(true);
		when(mockResponse.getOutput()).thenReturn(List.of(mockDoc));
		when(mockCollection.query(any(QueryDocRequest.class))).thenReturn(mockResponse);

		DashVectorStore vectorStore = DashVectorStore.builder(mockCollection, "test_collection", mockEmbeddingModel)
			.defaultTopK(6)
			.defaultSimilarityThreshold(0.75)
			.build();

		List<org.springframework.ai.document.Document> results = vectorStore.similaritySearch("Spring AI");

		ArgumentCaptor<QueryDocRequest> requestCaptor = ArgumentCaptor.forClass(QueryDocRequest.class);
		verify(mockCollection).query(requestCaptor.capture());
		assertThat(requestCaptor.getValue().getTopk()).isEqualTo(6);
		assertThat(requestCaptor.getValue().getVector()).isNotNull();
		assertThat(results).hasSize(1);
		assertThat(results.get(0).getText()).isEqualTo("Spring AI content");
		assertThat(results.get(0).getScore()).isCloseTo(0.8d, within(1e-6));
		assertThat((Double) results.get(0).getMetadata().get("distance")).isCloseTo(0.2d, within(1e-6));
	}

	@Test
	void testSimilaritySearchFiltersByCosineThreshold() {
		DashVectorCollection mockCollection = mock(DashVectorCollection.class);
		EmbeddingModel mockEmbeddingModel = mock(EmbeddingModel.class);
		Response<List<Doc>> mockResponse = mock(Response.class);
		Doc lowScoreDoc = mock(Doc.class);
		Doc highScoreDoc = mock(Doc.class);

		when(mockEmbeddingModel.embed("Spring AI")).thenReturn(new float[] { 0.1f, 0.2f });
		when(lowScoreDoc.getId()).thenReturn("doc-low");
		when(lowScoreDoc.getFields()).thenReturn(Map.of("content", "Low score content"));
		when(lowScoreDoc.getScore()).thenReturn(0.4f);
		when(highScoreDoc.getId()).thenReturn("doc-high");
		when(highScoreDoc.getFields()).thenReturn(Map.of("content", "High score content"));
		when(highScoreDoc.getScore()).thenReturn(0.1f);
		when(mockResponse.isSuccess()).thenReturn(true);
		when(mockResponse.getOutput()).thenReturn(List.of(lowScoreDoc, highScoreDoc));
		when(mockCollection.query(any(QueryDocRequest.class))).thenReturn(mockResponse);

		DashVectorStore vectorStore = DashVectorStore.builder(mockCollection, "test_collection", mockEmbeddingModel)
			.metric("cosine")
			.build();

		List<org.springframework.ai.document.Document> results = vectorStore.similaritySearch(
			SearchRequest.builder().query("Spring AI").topK(2).similarityThreshold(0.75).build());

		assertThat(results).hasSize(1);
		assertThat(results.get(0).getId()).isEqualTo("doc-high");
		assertThat(results.get(0).getScore()).isCloseTo(0.9d, within(1e-6));
	}

}
