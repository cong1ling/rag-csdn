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

import com.aliyun.dashvector.DashVectorClient;
import com.aliyun.dashvector.DashVectorCollection;
import com.aliyun.dashvector.common.DashVectorException;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.embedding.BatchingStrategy;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.ai.vectorstore.observation.VectorStoreObservationConvention;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.util.Assert;

/**
 * DashVector Vector Store Auto Configuration
 *
 * @author Your Name
 * @since 2027-03-05
 */
@AutoConfiguration
@ConditionalOnClass({ EmbeddingModel.class, DashVectorStore.class, DashVectorClient.class })
@EnableConfigurationProperties({ DashVectorStoreProperties.class })
@ConditionalOnProperty(prefix = DashVectorStoreProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true",
		matchIfMissing = true)
public class DashVectorStoreAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public DashVectorClient dashVectorClient(DashVectorStoreProperties properties) throws DashVectorException {
		Assert.hasText(properties.getApiKey(), "DashVector API key must not be empty");
		Assert.hasText(properties.getEndpoint(), "DashVector endpoint must not be empty");

		return new DashVectorClient(properties.getApiKey(), properties.getEndpoint());
	}

	@Bean
	@ConditionalOnMissingBean
	public DashVectorCollection dashVectorCollection(DashVectorClient client, DashVectorStoreProperties properties)
			throws DashVectorException {
		Assert.hasText(properties.getCollectionName(), "DashVector collection name must not be empty");
		return client.get(properties.getCollectionName());
	}

	@Bean
	@ConditionalOnMissingBean(BatchingStrategy.class)
	public BatchingStrategy dashVectorBatchingStrategy() {
		return new TokenCountBatchingStrategy();
	}

	@Bean
	@ConditionalOnMissingBean
	public DashVectorStore dashVectorStore(DashVectorCollection collection, EmbeddingModel embeddingModel,
			DashVectorStoreProperties properties, ObjectProvider<ObservationRegistry> observationRegistry,
			ObjectProvider<VectorStoreObservationConvention> customObservationConvention,
			BatchingStrategy batchingStrategy) {

		var builder = DashVectorStore.builder(collection, properties.getCollectionName(), embeddingModel)
			.batchingStrategy(batchingStrategy)
			.observationRegistry(observationRegistry.getIfUnique(() -> ObservationRegistry.NOOP))
			.customObservationConvention(customObservationConvention.getIfAvailable());

		if (properties.getDefaultTopK() != null && properties.getDefaultTopK() >= 0) {
			builder.defaultTopK(properties.getDefaultTopK());
		}

		if (properties.getDefaultSimilarityThreshold() != null && properties.getDefaultSimilarityThreshold() >= 0.0) {
			builder.defaultSimilarityThreshold(properties.getDefaultSimilarityThreshold());
		}

		if (properties.getMetric() != null) {
			builder.metric(properties.getMetric());
		}

		return builder.initializeSchema(properties.isInitializeSchema()).build();
	}

}
