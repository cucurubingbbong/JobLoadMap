package com.jrm.app.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

@Component
public class JdFetcher {

    public String resolveJdText(String jdText, String jdUrl) {
        if (jdUrl != null && !jdUrl.isBlank()) {
            try {
                return fetchFromUrl(jdUrl);
            } catch (IOException | URISyntaxException e) {
                // fall back to provided text if fetch fails
            }
        }
        return jdText == null || jdText.isBlank() ? "" : jdText;
    }

    private String fetchFromUrl(String jdUrl) throws IOException, URISyntaxException {
        URI uri = new URI(jdUrl);
        Document doc = Jsoup.connect(uri.toString())
                .userAgent("Mozilla/5.0 (compatible; JobRoadMap/1.0)")
                .get();
        String body = doc.body().text();
        return new String(body.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
    }
}
