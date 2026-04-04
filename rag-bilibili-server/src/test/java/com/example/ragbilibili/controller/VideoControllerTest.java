package com.example.ragbilibili.controller;

import com.example.ragbilibili.dto.request.ImportVideoRequest;
import com.example.ragbilibili.dto.response.VideoResponse;
import com.example.ragbilibili.interceptor.LoginInterceptor;
import com.example.ragbilibili.service.VideoService;
import com.example.ragbilibili.util.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VideoController.class)
class VideoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VideoService videoService;

    @MockBean
    private LoginInterceptor loginInterceptor;

    @BeforeEach
    void mockAuth() throws Exception {
        UserContext.set(1L);
        when(loginInterceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

    @AfterEach
    void clearAuth() {
        UserContext.remove();
    }

    /**
     * 手动构建 JSON，因为 ImportVideoRequest 的 cookie 字段标注了
     * {@code @JsonProperty(access = WRITE_ONLY)}，objectMapper.writeValueAsString()
     * 会跳过这些字段导致请求体缺少必填项。
     */
    private String buildImportJson(String bvidOrUrl, String sessdata, String biliJct, String buvid3) {
        return String.format(
                "{\"bvidOrUrl\":\"%s\",\"sessdata\":\"%s\",\"biliJct\":\"%s\",\"buvid3\":\"%s\"}",
                bvidOrUrl, sessdata, biliJct, buvid3);
    }

    @Test
    void testImportVideo() throws Exception {
        VideoResponse response = new VideoResponse();
        response.setId(1L);
        response.setBvid("BV1xx411c7mD");
        response.setTitle("导入中...");
        response.setStatus("IMPORTING");

        when(videoService.importVideo(any(ImportVideoRequest.class), eq(1L))).thenReturn(response);

        mockMvc.perform(post("/api/videos")
                        .header("Authorization", "Bearer mocked.jwt.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildImportJson("BV1xx411c7mD", "test_sessdata", "test_bili_jct", "test_buvid3")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.bvid").value("BV1xx411c7mD"))
                .andExpect(jsonPath("$.data.status").value("IMPORTING"));
    }

    @Test
    void testListVideos() throws Exception {
        VideoResponse video1 = new VideoResponse();
        video1.setId(1L);
        video1.setBvid("BV1xx411c7mD");
        video1.setTitle("视频1");
        video1.setStatus("SUCCESS");

        VideoResponse video2 = new VideoResponse();
        video2.setId(2L);
        video2.setBvid("BV1yy411c7mE");
        video2.setTitle("视频2");
        video2.setStatus("SUCCESS");

        List<VideoResponse> videos = Arrays.asList(video1, video2);
        when(videoService.listVideos(1L)).thenReturn(videos);

        mockMvc.perform(get("/api/videos")
                        .header("Authorization", "Bearer mocked.jwt.token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].bvid").value("BV1xx411c7mD"))
                .andExpect(jsonPath("$.data[1].bvid").value("BV1yy411c7mE"));
    }

    @Test
    void testGetVideo() throws Exception {
        VideoResponse response = new VideoResponse();
        response.setId(1L);
        response.setBvid("BV1xx411c7mD");
        response.setTitle("测试视频");
        response.setStatus("SUCCESS");

        when(videoService.getVideo(1L, 1L)).thenReturn(response);

        mockMvc.perform(get("/api/videos/1")
                        .header("Authorization", "Bearer mocked.jwt.token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.bvid").value("BV1xx411c7mD"));
    }

    @Test
    void testDeleteVideo() throws Exception {
        doNothing().when(videoService).deleteVideo(1L, 1L);

        mockMvc.perform(delete("/api/videos/1")
                        .header("Authorization", "Bearer mocked.jwt.token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
