package com.alibaba.cloud.ai.vectorstore.dashvector;

import com.aliyun.dashvector.DashVectorCollection;
import com.aliyun.dashvector.models.DocOpResult;
import com.aliyun.dashvector.models.requests.UpsertDocRequest;
import com.aliyun.dashvector.models.responses.Response;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DashVectorStoreTest {

    @Test
    void addShouldSplitDocumentsIntoSmallBatches() {
        DashVectorCollection collection = mock(DashVectorCollection.class);
        EmbeddingModel embeddingModel = mock(EmbeddingModel.class);
        DashVectorStore store = new DashVectorStore(collection, "test", embeddingModel, 10, 0.0, "cosine", 2, 0);

        when(embeddingModel.embed(any(Document.class))).thenReturn(new float[]{0.1f, 0.2f});
        when(collection.upsert(any(UpsertDocRequest.class))).thenReturn(successResponse());

        store.add(List.of(
                doc("1"), doc("2"), doc("3"), doc("4"), doc("5")
        ));

        ArgumentCaptor<UpsertDocRequest> captor = ArgumentCaptor.forClass(UpsertDocRequest.class);
        verify(collection, times(3)).upsert(captor.capture());
        assertEquals(List.of(2, 2, 1), captor.getAllValues().stream().map(r -> r.getDocs().size()).toList());
    }

    @Test
    void addShouldRetryTransientDeadlineExceededFailures() {
        DashVectorCollection collection = mock(DashVectorCollection.class);
        EmbeddingModel embeddingModel = mock(EmbeddingModel.class);
        DashVectorStore store = new DashVectorStore(collection, "test", embeddingModel, 10, 0.0, "cosine", 2, 1);

        when(embeddingModel.embed(any(Document.class))).thenReturn(new float[]{0.1f, 0.2f});
        when(collection.upsert(any(UpsertDocRequest.class))).thenReturn(failedResponse("deadline exceeded after 9.9s"), successResponse());

        store.add(List.of(doc("1"), doc("2")));

        verify(collection, times(2)).upsert(any(UpsertDocRequest.class));
    }

    private Document doc(String id) {
        return Document.builder().id(id).text("text-" + id).build();
    }

    private Response<List<DocOpResult>> successResponse() {
        return Response.create(0, "ok", "req-ok", List.of());
    }

    private Response<List<DocOpResult>> failedResponse(String message) {
        return Response.create(1, message, "req-fail", List.of());
    }
}
