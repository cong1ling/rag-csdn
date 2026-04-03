package com.example.ragbilibili.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 导入视频请求
 */
@Data
public class ImportVideoRequest {
    @NotBlank(message = "BV号或URL不能为空")
    private String bvidOrUrl;

    @NotBlank(message = "SESSDATA不能为空")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String sessdata;

    @NotBlank(message = "bili_jct不能为空")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String biliJct;

    @NotBlank(message = "buvid3不能为空")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String buvid3;

    /**
     * 脱敏输出，防止日志意外打印原始 Cookie
     */
    @Override
    public String toString() {
        return "ImportVideoRequest{" +
                "bvidOrUrl='" + bvidOrUrl + '\'' +
                ", sessdata='***'" +
                ", biliJct='***'" +
                ", buvid3='***'" +
                '}';
    }
}
