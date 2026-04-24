package com.example.ragcsdn.mapper;

import com.example.ragcsdn.entity.VectorMapping;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 向量映射 Mapper 接口
 */
@Mapper
public interface VectorMappingMapper {
    /**
     * 根据文章ID查询向量ID列表
     */
    List<String> selectVectorIdsByArticleId(@Param("articleId") Long articleId);

    /**
     * 批量插入向量映射
     */
    int batchInsert(@Param("mappings") List<VectorMapping> mappings);

    /**
     * 根据文章ID删除映射
     */
    int deleteByArticleId(@Param("articleId") Long articleId);
}

