package com.example.ragcsdn.util;

/**
 * 向量ID生成工具类
 *
 * 向量ID格式：user_id_source_id_chunk_index
 * 示例：1001_147000001_2
 */
public class VectorIDGenerator {
    /**
     * 生成向量ID
     *
     * @param userId 用户ID
     * @param sourceId 来源标识
     * @param chunkIndex 分片序号
     * @return 向量ID
     */
    public static String generate(Long userId, String sourceId, Integer chunkIndex) {
        return String.format("%d_%s_%d", userId, sourceId, chunkIndex);
    }

    /**
     * 解析向量ID
     *
     * @param vectorId 向量ID
     * @return [user_id, source_id, chunk_index]
     */
    public static String[] parse(String vectorId) {
        return vectorId.split("_", 3);
    }
}

