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

import org.springframework.ai.vectorstore.properties.CommonVectorStoreProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the DashVector Vector Store.
 *
 * @author Your Name
 * @since 2027-03-05
 */
@ConfigurationProperties(DashVectorStoreProperties.CONFIG_PREFIX)
public class DashVectorStoreProperties extends CommonVectorStoreProperties {

	public static final String CONFIG_PREFIX = "spring.ai.vectorstore.dashvector";

	/**
	 * DashVector API-KEY (required)
	 */
	private String apiKey;

	/**
	 * DashVector Cluster Endpoint (required)
	 */
	private String endpoint;

	/**
	 * Collection name (required)
	 */
	private String collectionName;

	/**
	 * Vector dimension (optional, inferred from EmbeddingModel if not set)
	 */
	private Integer dimension;

	/**
	 * Distance metric: cosine, euclidean, dotproduct
	 */
	private String metric = "cosine";

	/**
	 * Vector data type: FLOAT, INT
	 */
	private String dataType = "FLOAT";

	/**
	 * Default top-k for similarity search
	 */
	private Integer defaultTopK = 10;

	/**
	 * Default similarity threshold
	 */
	private Double defaultSimilarityThreshold = 0.0;

	/**
	 * Request timeout in seconds
	 */
	private Float timeout = 30f;

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public String getCollectionName() {
		return collectionName;
	}

	public void setCollectionName(String collectionName) {
		this.collectionName = collectionName;
	}

	public Integer getDimension() {
		return dimension;
	}

	public void setDimension(Integer dimension) {
		this.dimension = dimension;
	}

	public String getMetric() {
		return metric;
	}

	public void setMetric(String metric) {
		this.metric = metric;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public Integer getDefaultTopK() {
		return defaultTopK;
	}

	public void setDefaultTopK(Integer defaultTopK) {
		this.defaultTopK = defaultTopK;
	}

	public Double getDefaultSimilarityThreshold() {
		return defaultSimilarityThreshold;
	}

	public void setDefaultSimilarityThreshold(Double defaultSimilarityThreshold) {
		this.defaultSimilarityThreshold = defaultSimilarityThreshold;
	}

	public Float getTimeout() {
		return timeout;
	}

	public void setTimeout(Float timeout) {
		this.timeout = timeout;
	}

}
