package com.example.ragcsdn.util;

import com.example.ragcsdn.exception.BusinessException;
import com.example.ragcsdn.exception.ErrorCode;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

/**
 * 解析并标准化 CSDN 作者主页链接。
 */
public final class CsdnAuthorUrlParser {

    private static final Set<String> RESERVED_SEGMENTS = Set.of(
            "article", "topics", "nav", "phoenix", "p", "category", "write", "ranking", "blog-html"
    );

    private CsdnAuthorUrlParser() {
    }

    public static String parseAuthor(String input) {
        URI uri = parseUri(input);
        String path = uri.getPath();
        if (path == null || path.isBlank() || "/".equals(path)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "无法从输入中解析作者主页，请提供具体作者 URL");
        }

        String[] segments = path.split("/");
        for (String segment : segments) {
            if (segment == null || segment.isBlank()) {
                continue;
            }
            String normalized = segment.trim();
            if (RESERVED_SEGMENTS.contains(normalized.toLowerCase())) {
                throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "请提供具体作者主页 URL，而不是 CSDN 首页或分类页");
            }
            return normalized;
        }
        throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "无法从输入中解析作者主页，请检查 URL");
    }

    public static String normalizeAuthorUrl(String input) {
        return "https://blog.csdn.net/" + parseAuthor(input);
    }

    private static URI parseUri(String input) {
        if (input == null || input.isBlank()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "作者主页 URL 不能为空");
        }
        try {
            URI uri = new URI(input.trim());
            if (!"https".equalsIgnoreCase(uri.getScheme()) && !"http".equalsIgnoreCase(uri.getScheme())) {
                throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "作者主页 URL 仅支持 http/https");
            }
            String host = uri.getHost();
            if (host == null || !host.toLowerCase().endsWith("csdn.net")) {
                throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "仅支持 CSDN 作者主页 URL");
            }
            return uri;
        } catch (URISyntaxException ex) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "作者主页 URL 格式不正确");
        }
    }
}
