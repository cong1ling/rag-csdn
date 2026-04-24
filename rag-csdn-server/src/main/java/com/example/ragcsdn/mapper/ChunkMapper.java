package com.example.ragcsdn.mapper;

import com.example.ragcsdn.entity.Chunk;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 分片 Mapper 接口
 */
@Mapper
public interface ChunkMapper {
    /**
     * 根据ID查询分片
     */
    Chunk selectById(@Param("id") Long id);

    /**
     * 根据文章ID查询分片列表
     */
    List<Chunk> selectByArticleId(@Param("articleId") Long articleId);

    /**
     * 统计文章分片数量
     */
    int countByArticleId(@Param("articleId") Long articleId);

    /**
     * 基于关键词检索分片
     */
    List<Chunk> searchByKeywords(@Param("userId") Long userId,
                                 @Param("sourceId") String sourceId,
                                 @Param("searchText") String searchText,
                                 @Param("limit") int limit);

    /**
     * 批量插入分片
     */
    int batchInsert(@Param("chunks") List<Chunk> chunks);

    /**
     * 根据文章ID删除分片
     */
    int deleteByArticleId(@Param("articleId") Long articleId);
}

