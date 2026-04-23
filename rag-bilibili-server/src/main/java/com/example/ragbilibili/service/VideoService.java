package com.example.ragbilibili.service;

import com.example.ragbilibili.dto.request.ImportVideoRequest;
import com.example.ragbilibili.dto.request.RebuildVideoRequest;
import com.example.ragbilibili.dto.response.VideoResponse;
import java.util.List;

/**
 * 视频服务接口
 */
public interface VideoService {
    /**
     * 导入视频
     */
    VideoResponse importVideo(ImportVideoRequest request, Long userId);

    /**
     * 重建已有视频的字幕切分与向量索引
     */
    VideoResponse rebuildVideo(Long videoId, RebuildVideoRequest request, Long userId);

    /**
     * 获取用户视频列表
     */
    List<VideoResponse> listVideos(Long userId);

    /**
     * 获取视频详情
     */
    VideoResponse getVideo(Long videoId, Long userId);

    /**
     * 删除视频（级联删除分片、向量、会话）
     */
    void deleteVideo(Long videoId, Long userId);
}
