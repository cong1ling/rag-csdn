package com.example.ragbilibili.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 重建视频索引请求
 */
@Data
public class RebuildVideoRequest {
    @NotBlank(message = "SESSDATA不能为空")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String sessdata;

    @NotBlank(message = "bili_jct不能为空")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String biliJct;

    @NotBlank(message = "buvid3不能为空")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String buvid3;

    @Override
    public String toString() {
        return "RebuildVideoRequest{" +
                "sessdata='***'" +
                ", biliJct='***'" +
                ", buvid3='***'" +
                '}';
    }
}
