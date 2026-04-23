package com.example.ragbilibili.service.impl;

import com.alibaba.cloud.ai.reader.bilibili.BilibiliCredentials;
import com.alibaba.cloud.ai.reader.bilibili.BilibiliDocumentReader;
import com.alibaba.cloud.ai.reader.bilibili.BilibiliResource;
import com.alibaba.cloud.ai.vectorstore.dashvector.DashVectorStore;
import com.example.ragbilibili.dto.request.ImportVideoRequest;
import com.example.ragbilibili.dto.request.RebuildVideoRequest;
import com.example.ragbilibili.dto.response.VideoResponse;
import com.example.ragbilibili.entity.Chunk;
import com.example.ragbilibili.entity.VectorMapping;
import com.example.ragbilibili.entity.Video;
import com.example.ragbilibili.enums.VideoStatus;
import com.example.ragbilibili.exception.BusinessException;
import com.example.ragbilibili.exception.ErrorCode;
import com.example.ragbilibili.mapper.*;
import com.example.ragbilibili.service.VideoService;
import com.example.ragbilibili.util.BVIDParser;
import com.example.ragbilibili.util.ChunkDocumentSplitter;
import com.example.ragbilibili.util.VectorIDGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VideoServiceImpl implements VideoService {
    private static final Logger log = LoggerFactory.getLogger(VideoServiceImpl.class);

    @Autowired
    private VideoMapper videoMapper;

    @Autowired
    private ChunkMapper chunkMapper;

    @Autowired
    private VectorMappingMapper vectorMappingMapper;

    @Autowired
    private SessionMapper sessionMapper;

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private ChunkDocumentSplitter chunkDocumentSplitter;

    @Autowired
    private DashVectorStore dashVectorStore;

    @Autowired
    private VideoStatusWriter videoStatusWriter;

    @Autowired
    @Qualifier("taskExecutor")
    private TaskExecutor taskExecutor;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional
    public VideoResponse importVideo(ImportVideoRequest request, Long userId) {
        // 1. 解析 BV 号
        String bvid = BVIDParser.parse(request.getBvidOrUrl());

        // 2. 检查视频是否已存在
        Video existingVideo = videoMapper.selectByUserIdAndBvid(userId, bvid);
        if (existingVideo != null) {
            throw new BusinessException(ErrorCode.VIDEO_ALREADY_EXISTS);
        }

        // 3. 同步创建 IMPORTING 状态的视频记录，立即返回给前端
        Video video = new Video();
        video.setUserId(userId);
        video.setBvid(bvid);
        video.setTitle("导入中...");
        video.setStatus(VideoStatus.IMPORTING.getCode());
        video.setImportTime(LocalDateTime.now());
        videoMapper.insert(video);

        Long videoId = video.getId();

        // 4. 提交异步任务：拉取字幕、切分、向量化
        taskExecutor.execute(() -> executeImport(
                videoId,
                userId,
                bvid,
                request.getSessdata(),
                request.getBiliJct(),
                request.getBuvid3(),
                false));

        return convertToResponse(video);
    }

    @Override
    public VideoResponse rebuildVideo(Long videoId, RebuildVideoRequest request, Long userId) {
        Video video = videoMapper.selectById(videoId);
        if (video == null || !video.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.VIDEO_NOT_FOUND);
        }

        video.setStatus(VideoStatus.IMPORTING.getCode());
        video.setFailReason(null);
        if (video.getTitle() == null || video.getTitle().isBlank()) {
            video.setTitle("重建中...");
        }
        videoMapper.update(video);

        taskExecutor.execute(() -> executeImport(
                videoId,
                userId,
                video.getBvid(),
                request.getSessdata(),
                request.getBiliJct(),
                request.getBuvid3(),
                true));

        return convertToResponse(video);
    }

    /**
     * 异步执行实际的导入流程。
     * 在独立线程中运行，不占用 HTTP 请求线程。
     */
    private void executeImport(Long videoId, Long userId, String bvid,
                               String sessdata, String biliJct, String buvid3,
                               boolean rebuildExistingIndex) {
        try {
            // 1. 使用 BilibiliDocumentReader 读取视频内容
            BilibiliCredentials credentials = buildCredentials(sessdata, biliJct, buvid3);

            BilibiliDocumentReader reader = new BilibiliDocumentReader(new BilibiliResource(bvid, credentials));
            List<Document> documents = reader.get();

            if (documents.isEmpty()) {
                markVideoFailed(videoId, "视频无字幕或字幕为空");
                return;
            }

            Document document = documents.get(0);
            String videoTitle = (String) document.getMetadata().get("title");
            String videoDescription = (String) document.getMetadata().get("description");

            // 2. 文本切分
            List<Document> splitDocuments = chunkDocumentSplitter.split(documents);
            List<Document> indexedDocuments = new ArrayList<>(splitDocuments.size());

            // 3. 生成向量ID并准备数据
            List<Chunk> chunks = new ArrayList<>();
            int totalChunks = splitDocuments.size();

            for (int i = 0; i < splitDocuments.size(); i++) {
                Document doc = splitDocuments.get(i);
                String vectorId = VectorIDGenerator.generate(userId, bvid, i);

                Document indexedDocument = Document.builder()
                        .id(vectorId)
                        .text(doc.getText())
                        .metadata(new HashMap<>(doc.getMetadata()))
                        .metadata("title", videoTitle)
                        .metadata("userId", userId)
                        .metadata("bvid", bvid)
                        .metadata("chunkIndex", i)
                        .metadata("totalChunks", totalChunks)
                        .build();
                indexedDocuments.add(indexedDocument);

                Chunk chunk = new Chunk();
                chunk.setVideoId(videoId);
                chunk.setUserId(userId);
                chunk.setBvid(bvid);
                chunk.setTitle(videoTitle);
                chunk.setChunkIndex(i);
                chunk.setTotalChunks(totalChunks);
                chunk.setChunkText(indexedDocument.getText());
                chunk.setCreateTime(LocalDateTime.now());
                chunks.add(chunk);
            }

            // 4. 重建场景先清理旧索引，再写入新数据
            if (rebuildExistingIndex) {
                cleanupExistingIndex(videoId);
            }

            // 5. 批量插入分片
            if (!chunks.isEmpty()) {
                chunkMapper.batchInsert(chunks);
            }

            // 6. 写入 DashVector
            dashVectorStore.add(indexedDocuments);

            // 7. 创建向量映射
            List<VectorMapping> mappings = new ArrayList<>();
            for (int i = 0; i < chunks.size(); i++) {
                Chunk chunk = chunks.get(i);
                String vectorId = VectorIDGenerator.generate(userId, bvid, i);

                VectorMapping mapping = new VectorMapping();
                mapping.setUserId(userId);
                mapping.setVideoId(videoId);
                mapping.setChunkId(chunk.getId());
                mapping.setVectorId(vectorId);
                mapping.setCreateTime(LocalDateTime.now());
                mappings.add(mapping);
            }

            if (!mappings.isEmpty()) {
                vectorMappingMapper.batchInsert(mappings);
            }

            // 8. 更新视频状态为成功
            Video video = videoMapper.selectById(videoId);
            video.setTitle(videoTitle);
            video.setDescription(videoDescription);
            video.setStatus(VideoStatus.SUCCESS.getCode());
            video.setFailReason(null);
            videoMapper.update(video);

            log.info("视频{}成功: userId={}, bvid={}, chunks={}",
                    rebuildExistingIndex ? "重建" : "导入", userId, bvid, totalChunks);

        } catch (Exception e) {
            log.error("视频{}失败: userId={}, bvid={}",
                    rebuildExistingIndex ? "重建" : "导入", userId, bvid, e);
            markVideoFailed(videoId, e.getMessage());
        }
    }

    private BilibiliCredentials buildCredentials(String sessdata, String biliJct, String buvid3) {
        return BilibiliCredentials.builder()
                .sessdata(sessdata)
                .biliJct(biliJct)
                .buvid3(buvid3)
                .build();
    }

    private void cleanupExistingIndex(Long videoId) {
        List<String> vectorIds = vectorMappingMapper.selectVectorIdsByVideoId(videoId);
        if (!vectorIds.isEmpty()) {
            dashVectorStore.delete(vectorIds);
        }
        vectorMappingMapper.deleteByVideoId(videoId);
        chunkMapper.deleteByVideoId(videoId);
    }

    private void markVideoFailed(Long videoId, String reason) {
        Video video = videoMapper.selectById(videoId);
        if (video != null) {
            videoStatusWriter.markFailed(video, reason);
        }
    }

    @Override
    public List<VideoResponse> listVideos(Long userId) {
        List<Video> videos = videoMapper.selectByUserId(userId);
        return videos.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public VideoResponse getVideo(Long videoId, Long userId) {
        Video video = videoMapper.selectById(videoId);
        if (video == null || !video.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.VIDEO_NOT_FOUND);
        }
        return convertToResponse(video);
    }

    @Override
    @Transactional
    public void deleteVideo(Long videoId, Long userId) {
        // 1. 验证视频是否存在且属于当前用户
        Video video = videoMapper.selectById(videoId);
        if (video == null || !video.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.VIDEO_NOT_FOUND);
        }

        try {
            // 2. 查询向量ID列表
            List<String> vectorIds = vectorMappingMapper.selectVectorIdsByVideoId(videoId);

            // 3. 从 DashVector 删除向量（基于ID删除）
            if (!vectorIds.isEmpty()) {
                dashVectorStore.delete(vectorIds);
            }

            // 4. 查询关联的会话ID列表
            List<Long> sessionIds = sessionMapper.selectIdsByVideoId(videoId);

            // 5. 删除会话关联的消息
            if (!sessionIds.isEmpty()) {
                messageMapper.deleteBySessionIds(sessionIds);
            }

            // 6. 删除会话
            sessionMapper.deleteByVideoId(videoId);

            // 7. 删除向量映射
            vectorMappingMapper.deleteByVideoId(videoId);

            // 8. 删除分片
            chunkMapper.deleteByVideoId(videoId);

            // 9. 删除视频记录
            videoMapper.deleteById(videoId);

            log.info("视频删除成功: userId={}, videoId={}, bvid={}", userId, videoId, video.getBvid());

        } catch (Exception e) {
            log.error("视频删除失败: userId={}, videoId={}", userId, videoId, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
    }

    private VideoResponse convertToResponse(Video video) {
        VideoResponse response = new VideoResponse();
        response.setId(video.getId());
        response.setBvid(video.getBvid());
        response.setTitle(video.getTitle());
        response.setDescription(video.getDescription());
        if (video.getImportTime() != null) {
            response.setImportTime(video.getImportTime().format(FORMATTER));
        }
        response.setStatus(video.getStatus());
        response.setFailReason(video.getFailReason());

        // 查询分片数量
        int chunkCount = chunkMapper.countByVideoId(video.getId());
        response.setChunkCount(chunkCount);

        return response;
    }
}
