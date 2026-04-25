package com.example.ragcsdn.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 保存用户 CSDN 登录态
 */
@Data
public class UpdateCsdnSessionRequest {
    @NotBlank(message = "CSDN Cookie 不能为空")
    private String cookie;
}
