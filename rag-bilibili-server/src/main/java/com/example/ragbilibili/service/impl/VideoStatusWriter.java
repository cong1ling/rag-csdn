package com.example.ragbilibili.service.impl;

import com.example.ragbilibili.entity.Video;
import com.example.ragbilibili.enums.VideoStatus;
import com.example.ragbilibili.mapper.VideoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 视频状态写入器
 *
 * 使用独立事务（REQUIRES_NEW）更新视频失败状态，确保即使外层事务回滚，
 * 失败记录也能持久化。这解决了 @Transactional + catch-rethrow 场景下
 * 失败状态被一同回滚的问题。
 */
@Component
public class VideoStatusWriter {

    @Autowired
    private VideoMapper videoMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(Video video, String reason) {
        video.setStatus(VideoStatus.FAILED.getCode());
        video.setFailReason(reason);
        videoMapper.update(video);
    }
}
