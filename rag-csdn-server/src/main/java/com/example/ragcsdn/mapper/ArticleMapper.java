package com.example.ragcsdn.mapper;

import com.example.ragcsdn.entity.Article;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文章 Mapper 接口
 */
@Mapper
public interface ArticleMapper {
    /**
     * 根据ID查询文章
     */
    Article selectById(@Param("id") Long id);

    /**
     * 根据用户ID和来源标识查询记录
     */
    Article selectByUserIdAndSourceId(@Param("userId") Long userId, @Param("sourceId") String sourceId);

    /**
     * 根据用户ID查询文章列表
     */
    List<Article> selectByUserId(@Param("userId") Long userId);

    /**
     * 插入文章
     */
    int insert(Article article);

    /**
     * 更新文章
     */
    int update(Article article);

    /**
     * 删除文章
     */
    int deleteById(@Param("id") Long id);
}

