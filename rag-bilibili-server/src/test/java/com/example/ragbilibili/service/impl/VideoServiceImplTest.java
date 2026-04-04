package com.example.ragbilibili.service.impl;

import com.example.ragbilibili.dto.request.ImportVideoRequest;
import com.example.ragbilibili.dto.response.VideoResponse;
import com.example.ragbilibili.entity.Video;
import com.example.ragbilibili.enums.VideoStatus;
import com.example.ragbilibili.exception.BusinessException;
import com.example.ragbilibili.mapper.ChunkMapper;
import com.example.ragbilibili.mapper.MessageMapper;
import com.example.ragbilibili.mapper.SessionMapper;
import com.example.ragbilibili.mapper.VectorMappingMapper;
import com.example.ragbilibili.mapper.VideoMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.TaskExecutor;
import com.alibaba.cloud.ai.vectorstore.dashvector.DashVectorStore;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VideoServiceImplTest {

    @Mock
    private VideoMapper videoMapper;

    @Mock
    private ChunkMapper chunkMapper;

    @Mock
    private VectorMappingMapper vectorMappingMapper;

    @Mock
    private SessionMapper sessionMapper;

    @Mock
    private MessageMapper messageMapper;

    @Mock
    private TokenTextSplitter tokenTextSplitter;

    @Mock
    private DashVectorStore dashVectorStore;

    @Mock
    private VideoStatusWriter videoStatusWriter;

    @Mock
    private TaskExecutor taskExecutor;

    @InjectMocks
    private VideoServiceImpl videoService;

    /**
     * 验证同步阶段：importVideo() 应创建 IMPORTING 状态的视频记录并立即返回，
     * 实际导入工作被提交给 taskExecutor 异步执行。
     */
    @Test
    void importVideoShouldCreateImportingRecordAndReturnImmediately() {
        ImportVideoRequest request = new ImportVideoRequest();
        request.setBvidOrUrl("BV1KMwgeKECx");
        request.setSessdata("sessdata");
        request.setBiliJct("biliJct");
        request.setBuvid3("buvid3");

        when(videoMapper.selectByUserIdAndBvid(1L, "BV1KMwgeKECx")).thenReturn(null);

        doAnswer(invocation -> {
            Video video = invocation.getArgument(0);
            video.setId(100L);
            return 1;
        }).when(videoMapper).insert(any(Video.class));

        when(chunkMapper.countByVideoId(100L)).thenReturn(0);
        doNothing().when(taskExecutor).execute(any(Runnable.class));

        VideoResponse response = videoService.importVideo(request, 1L);

        // 验证：创建了 IMPORTING 状态的视频记录
        ArgumentCaptor<Video> videoCaptor = ArgumentCaptor.forClass(Video.class);
        verify(videoMapper).insert(videoCaptor.capture());
        Video insertedVideo = videoCaptor.getValue();

        assertEquals("BV1KMwgeKECx", insertedVideo.getBvid());
        assertEquals(VideoStatus.IMPORTING.getCode(), insertedVideo.getStatus());
        assertEquals("导入中...", insertedVideo.getTitle());
        assertNotNull(response.getId());

        // 验证：异步任务已提交
        verify(taskExecutor).execute(any(Runnable.class));
    }

    /**
     * 验证同步阶段的去重校验：当视频已存在时，应直接抛出 BusinessException，
     * 不创建新记录也不提交异步任务。
     */
    @Test
    void importVideoShouldRejectDuplicateVideo() {
        ImportVideoRequest request = new ImportVideoRequest();
        request.setBvidOrUrl("BV1KMwgeKECx");
        request.setSessdata("sessdata");
        request.setBiliJct("biliJct");
        request.setBuvid3("buvid3");

        Video existingVideo = new Video();
        existingVideo.setId(100L);
        existingVideo.setBvid("BV1KMwgeKECx");
        when(videoMapper.selectByUserIdAndBvid(1L, "BV1KMwgeKECx")).thenReturn(existingVideo);

        assertThrows(BusinessException.class, () -> videoService.importVideo(request, 1L));
    }
}
