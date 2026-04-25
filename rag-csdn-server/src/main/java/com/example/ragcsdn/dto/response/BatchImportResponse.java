package com.example.ragcsdn.dto.response;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BatchImportResponse {
    private String mode;
    private String target;
    private Integer discoveredCount;
    private Integer submittedCount;
    private Integer duplicateCount;
    private Integer failedCount;
    private List<BatchImportItemResponse> items = new ArrayList<>();
}
