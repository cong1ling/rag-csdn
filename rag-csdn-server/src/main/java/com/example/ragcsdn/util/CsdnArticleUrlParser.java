package com.example.ragcsdn.util;

import com.example.ragcsdn.exception.BusinessException;
import com.example.ragcsdn.exception.ErrorCode;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 解析并标准化 CSDN 文章链接。
 */
public final class CsdnArticleUrlParser {

    private static final Pattern ARTICLE_ID_PATTERN = Pattern.compile("/article/details/(\\d+)");

    private CsdnArticleUrlParser() {
    }

    public static String parseId(String input) {
        URI uri = parseUri(input);
        Matcher matcher = ARTICLE_ID_PATTERN.matcher(uri.getPath());
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new BusinessException(ErrorCode.BVID_PARSE_ERROR);
    }

    public static String normalizeUrl(String input) {
        URI uri = parseUri(input);
        return URI.create(uri.getScheme() + "://" + uri.getHost() + uri.getPath()).toString();
    }

    public static boolean isValid(String input) {
        try {
            parseId(input);
            return true;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private static URI parseUri(String input) {
        if (input == null || input.isBlank()) {
            throw new BusinessException(ErrorCode.BVID_PARSE_ERROR);
        }
        try {
            URI uri = new URI(input.trim());
            if (!"https".equalsIgnoreCase(uri.getScheme()) && !"http".equalsIgnoreCase(uri.getScheme())) {
                throw new BusinessException(ErrorCode.BVID_PARSE_ERROR);
            }
            String host = uri.getHost();
            if (host == null || !host.toLowerCase().endsWith("csdn.net")) {
                throw new BusinessException(ErrorCode.BVID_PARSE_ERROR);
            }
            return uri;
        } catch (URISyntaxException ex) {
            throw new BusinessException(ErrorCode.BVID_PARSE_ERROR);
        }
    }
}

