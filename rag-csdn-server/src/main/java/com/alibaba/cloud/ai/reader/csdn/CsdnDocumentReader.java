package com.alibaba.cloud.ai.reader.csdn;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CsdnDocumentReader implements DocumentReader {

    private static final Logger log = LoggerFactory.getLogger(CsdnDocumentReader.class);

    private final CsdnResource csdnResource;
    private final List<CsdnResource> csdnResourceList;
    private final HttpClient httpClient;

    public CsdnDocumentReader(CsdnResource csdnResource) {
        this.csdnResource = csdnResource;
        this.csdnResourceList = null;
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build();
    }

    public CsdnDocumentReader(List<CsdnResource> csdnResourceList) {
        this.csdnResource = null;
        this.csdnResourceList = csdnResourceList;
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build();
    }

    @Override
    public List<Document> get() {
        List<CsdnResource> resources = this.csdnResourceList;
        if (resources == null) {
            resources = List.of(this.csdnResource);
        }

        List<Document> documents = new ArrayList<>();
        for (CsdnResource resource : resources) {
            documents.addAll(readResource(resource));
        }
        return documents;
    }

    private List<Document> readResource(CsdnResource resource) {
        try {
            String html = sendGet(resource.getArticleUrl());
            org.jsoup.nodes.Document page = Jsoup.parse(html, resource.getArticleUrl());

            String title = firstNonBlank(
                    attr(page, "meta[property=og:title]", "content"),
                    attr(page, "meta[name=title]", "content"),
                    text(page, "h1.title-article"),
                    text(page, "h1"));

            Element contentElement = findContentElement(page);
            if (contentElement == null) {
                return List.of();
            }

            Element sanitizedContent = contentElement.clone();
            sanitizedContent.select(
                    "script,style,noscript,button,svg,aside,.passport-login-container,.tool-box," +
                            ".recommend-box,.hide-article-box,.blog_extension_box,.article-copyright").remove();

            String content = normalizeWhitespace(sanitizedContent.text());
            if (content.isBlank()) {
                return List.of();
            }

            String description = firstNonBlank(
                    attr(page, "meta[name=description]", "content"),
                    attr(page, "meta[property=og:description]", "content"),
                    summarize(content, 160));
            String author = firstNonBlank(
                    attr(page, "meta[name=author]", "content"),
                    text(page, ".follow-nickName"),
                    text(page, ".article-title-box + .article-info-box a"));
            String canonicalUrl = firstNonBlank(
                    attr(page, "link[rel=canonical]", "href"),
                    resource.getArticleUrl());

            String documentText = String.format(
                    "Article Title: %s%nDescription: %s%nAuthor: %s%nContent:%n%s",
                    title,
                    description,
                    author,
                    content);

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("sourceId", resource.getArticleId());
            metadata.put("sourceUrl", canonicalUrl);
            metadata.put("document_type", "content");
            metadata.put("title", title);
            metadata.put("description", description);
            metadata.put("author", author);
            return List.of(new Document(documentText, metadata));
        } catch (Exception ex) {
            log.error("Failed to read CSDN article: {}", resource.getArticleUrl(), ex);
            throw new RuntimeException("Failed to read CSDN article: " + resource.getArticleUrl(), ex);
        }
    }

    private String sendGet(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .GET()
                .timeout(Duration.ofSeconds(30))
                .header("Accept", "text/html,application/xhtml+xml")
                .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                .header("User-Agent", userAgent())
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("HTTP " + response.statusCode() + " for url: " + url);
        }
        return response.body();
    }

    private Element findContentElement(org.jsoup.nodes.Document page) {
        for (String selector : List.of(
                "main #content_views",
                "article #content_views",
                "#content_views",
                ".blog-content-box",
                ".article_content",
                "article")) {
            Element element = page.selectFirst(selector);
            if (element != null && !normalizeWhitespace(element.text()).isBlank()) {
                return element;
            }
        }
        return null;
    }

    private String text(org.jsoup.nodes.Document page, String selector) {
        Element element = page.selectFirst(selector);
        return element == null ? "" : normalizeWhitespace(element.text());
    }

    private String attr(org.jsoup.nodes.Document page, String selector, String attribute) {
        Element element = page.selectFirst(selector);
        return element == null ? "" : normalizeWhitespace(element.attr(attribute));
    }

    private String summarize(String content, int maxLength) {
        if (content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength).trim() + "...";
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return "";
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
