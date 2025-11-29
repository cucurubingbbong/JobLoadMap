package com.jrm.app.service;

import com.jrm.app.model.JobPost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class CrawlerService {
    private static final Logger log = LoggerFactory.getLogger(CrawlerService.class);

    private final CopyOnWriteArrayList<JobPost> cachedPosts = new CopyOnWriteArrayList<>();
    private final RestTemplate restTemplate = new RestTemplate();
    private final String worknetKey;
    private final String worknetUrl;

    public CrawlerService(
            @Value("${worknet.api.key:}") String worknetKey,
            @Value("${worknet.api.url:https://openapi.work.go.kr/opi/opi/opia/wantedApi.do}") String worknetUrl
    ) {
        this.worknetKey = worknetKey;
        this.worknetUrl = worknetUrl;
    }

    public List<JobPost> getCachedPosts() {
        return Collections.unmodifiableList(cachedPosts);
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void crawlDaily() {
        try {
            cachedPosts.clear();
            cachedPosts.addAll(fetchWorkNet());
        } catch (Exception e) {
            log.warn("크롤링 실패, 이전 캐시 유지", e);
            if (cachedPosts.isEmpty()) {
                cachedPosts.addAll(sample());
            }
        }
    }

    private List<JobPost> fetchWorkNet() {
        if (worknetKey == null || worknetKey.isBlank()) return sample();
        List<JobPost> results = new ArrayList<>();
        try {
            String url = worknetUrl +
                    "?callTp=L&returnType=XML&authKey=" + worknetKey +
                    "&pageNum=1&display=50";
            String xml = restTemplate.getForObject(url, String.class);
            if (xml == null || xml.isBlank()) return sample();
            Document doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
            NodeList list = doc.getElementsByTagName("wanted");
            for (int i = 0; i < list.getLength(); i++) {
                Node n = list.item(i);
                String id = text(n, "wantedAuthNo");
                String title = text(n, "jobCont");
                String company = text(n, "company");
                String region = text(n, "region");
                String link = text(n, "wantedInfoUrl");
                LocalDate deadline = parseDate(text(n, "closeDate"));
                List<String> keywords = new ArrayList<>();
                String jobType = text(n, "jobType");
                if (!jobType.isBlank()) keywords.add(jobType);
                results.add(new JobPost(id, company, title, link, region, deadline, keywords));
            }
            if (results.isEmpty()) return sample();
        } catch (Exception ex) {
            log.warn("워크넷 크롤 실패, 샘플 사용", ex);
            return sample();
        }
        return results;
    }

    private String text(Node node, String tag) {
        try {
            NodeList list = ((org.w3c.dom.Element) node).getElementsByTagName(tag);
            if (list.getLength() > 0 && list.item(0).getTextContent() != null) {
                return list.item(0).getTextContent().trim();
            }
        } catch (Exception ignored) {}
        return "";
    }

    private LocalDate parseDate(String s) {
        try {
            return LocalDate.parse(s);
        } catch (Exception e) {
            return LocalDate.now().plusDays(30);
        }
    }

    private List<JobPost> sample() {
        List<JobPost> results = new ArrayList<>();
        results.add(new JobPost("sample-1", "샘플(워크넷)", "백엔드 개발자", "https://www.work.go.kr", "서울", LocalDate.now().plusDays(20), List.of("Java", "Spring")));
        results.add(new JobPost("sample-2", "샘플(잡코리아)", "프론트엔드", "https://www.jobkorea.co.kr", "판교", LocalDate.now().plusDays(15), List.of("TypeScript", "React")));
        return results;
    }
}
