package com.alibaba.cloud.ai.reader.csdn;

import com.example.ragcsdn.util.CsdnArticleUrlParser;
import com.example.ragcsdn.util.CsdnAuthorUrlParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CsdnDiscoveryReader {

    private static final int DEFAULT_AUTHOR_PAGE_LIMIT = 10;
    private static final int DEFAULT_ARTICLE_LIMIT = 20;

    private final HttpClient httpClient;
    private final String cookieHeader;

    public CsdnDiscoveryReader(String cookieHeader) {
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build();
        this.cookieHeader = cookieHeader;
    }

    public List<CsdnArticleLink> discoverAuthorArticles(String authorUrl, Integer maxArticles, Integer maxPages) throws IOException, InterruptedException {
        String author = CsdnAuthorUrlParser.parseAuthor(authorUrl);
        String normalizedAuthorUrl = CsdnAuthorUrlParser.normalizeAuthorUrl(authorUrl);
        int articleLimit = clamp(maxArticles, 1, 100, DEFAULT_ARTICLE_LIMIT);
        int pageLimit = clamp(maxPages, 1, 20, DEFAULT_AUTHOR_PAGE_LIMIT);

        Map<String, CsdnArticleLink> results = new LinkedHashMap<>();
        collectArticleLinks(normalizedAuthorUrl, author, results, articleLimit);
        for (int page = 1; page <= pageLimit && results.size() < articleLimit; page++) {
            int before = results.size();
            collectArticleLinks(normalizedAuthorUrl + "/article/list/" + page, author, results, articleLimit);
            if (results.size() == before && page > 1) {
                break;
            }
        }
        return List.copyOf(results.values());
    }

    public List<CsdnArticleLink> discoverRecommendedArticles(Integer limit) throws IOException, InterruptedException {
        int articleLimit = clamp(limit, 1, 100, DEFAULT_ARTICLE_LIMIT);
        Map<String, CsdnArticleLink> results = new LinkedHashMap<>();
        collectArticleLinks("https://blog-html.csdn.net/", null, results, articleLimit);
        if (results.size() < articleLimit) {
            collectArticleLinks("https://blog.csdn.net/", null, results, articleLimit);
        }
        return List.copyOf(results.values());
    }

    private void collectArticleLinks(String url, String expectedAuthor, Map<String, CsdnArticleLink> results, int limit) throws IOException, InterruptedException {
        String html = sendGet(url);
        org.jsoup.nodes.Document page = Jsoup.parse(html, url);

        for (Element anchor : page.select("a[href*='/article/details/']")) {
            if (results.size() >= limit) {
                return;
            }
            String href = anchor.attr("abs:href");
            if (href == null || href.isBlank()) {
                href = anchor.attr("href");
            }
            CsdnArticleLink link = toArticleLink(href, anchor, expectedAuthor);
            if (link == null) {
                continue;
            }

            results.merge(link.sourceId(), link, (current, candidate) -> {
                if (isBetterTitle(candidate.title(), current.title())) {
                    return candidate;
                }
                return current;
            });
        }
    }

    private CsdnArticleLink toArticleLink(String href, Element anchor, String expectedAuthor) {
        if (href == null || href.isBlank()) {
            return null;
        }
        try {
            String normalizedUrl = CsdnArticleUrlParser.normalizeUrl(href);
            String sourceId = CsdnArticleUrlParser.parseId(href);
            String author = CsdnAuthorUrlParser.parseAuthor(href);
            if (expectedAuthor != null && !expectedAuthor.equalsIgnoreCase(author)) {
                return null;
            }

            String title = pickTitle(anchor);
            return new CsdnArticleLink(sourceId, normalizedUrl, title, author);
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private String pickTitle(Element anchor) {
        String directText = normalizeWhitespace(anchor.text());
        String titleAttr = normalizeWhitespace(anchor.attr("title"));
        String ariaLabel = normalizeWhitespace(anchor.attr("aria-label"));

        String best = "";
        for (String candidate : List.of(titleAttr, directText, ariaLabel)) {
            if (isBetterTitle(candidate, best)) {
                best = candidate;
            }
        }
        return best;
    }

    private boolean isBetterTitle(String candidate, String current) {
        return scoreTitle(candidate) < scoreTitle(current);
    }

    private int scoreTitle(String text) {
        if (text == null || text.isBlank()) {
            return Integer.MAX_VALUE;
        }
        String normalized = normalizeWhitespace(text);
        int score = Math.abs(normalized.length() - 24);
        String lower = normalized.toLowerCase(Locale.ROOT);
        if (lower.contains("收藏") || lower.contains("阅读") || lower.contains("赞")) {
            score += 1000;
        }
        if (normalized.length() > 120) {
            score += 400;
        }
        if (normalized.length() < 4) {
            score += 200;
        }
        return score;
    }

    private String sendGet(String url) throws IOException, InterruptedException {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(url))
                .GET()
                .timeout(Duration.ofSeconds(30))
                .header("Accept", "text/html,application/xhtml+xml")
                .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                .header("User-Agent", userAgent());
        if (cookieHeader != null && !cookieHeader.isBlank()) {
            request.header("Cookie", cookieHeader);
        }

        HttpResponse<String> response = httpClient.send(request.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("HTTP " + response.statusCode() + " for url: " + url);
        }
        return response.body();
    }

    private int clamp(Integer value, int min, int max, int fallback) {
        if (value == null) {
            return fallback;
        }
        return Math.max(min, Math.min(max, value));
    }

    private String normalizeWhitespace(String value) {
        return value == null ? "" : value.replace('\u00A0', ' ').replaceAll("\\s+", " ").trim();
    }

    private String userAgent() {
        return String.format("SpringAIAlibaba/1.0.0; java/%s; platform/%s; processor/%s",
                System.getProperty("java.version"),
                System.getProperty("os.name"),
                System.getProperty("os.arch"));
    }
}
