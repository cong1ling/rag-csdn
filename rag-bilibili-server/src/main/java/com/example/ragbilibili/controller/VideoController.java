package com.example.ragbilibili.controller;

import com.example.ragbilibili.common.Result;
import com.example.ragbilibili.dto.request.ImportVideoRequest;
import com.example.ragbilibili.dto.request.RebuildVideoRequest;
import com.example.ragbilibili.dto.response.VideoResponse;
import com.example.ragbilibili.service.VideoService;
import com.example.ragbilibili.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 视频控制器
 */
@RestController
@RequestMapping("/api/videos")
public class VideoController {
    @Autowired
    private VideoService videoService;

    @PostMapping
    public Result<VideoResponse> importVideo(@Valid @RequestBody ImportVideoRequest request) {
        return Result.success(videoService.importVideo(request, UserContext.get()));
    }

    @PostMapping("/{id}/rebuild")
    public Result<VideoResponse> rebuildVideo(@PathVariable Long id, @Valid @RequestBody RebuildVideoRequest request) {
        return Result.success(videoService.rebuildVideo(id, request, UserContext.get()));
    }

    @GetMapping
    public Result<List<VideoResponse>> listVideos() {
        return Result.success(videoService.listVideos(UserContext.get()));
    }

    @GetMapping("/{id}")
    public Result<VideoResponse> getVideo(@PathVariable Long id) {
        return Result.success(videoService.getVideo(id, UserContext.get()));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteVideo(@PathVariable Long id) {
        videoService.deleteVideo(id, UserContext.get());
        return Result.success();
    }
}
