package com.jrm.app.service;

import com.jrm.app.model.HiringCategory;
import com.jrm.app.model.JobPost;
import jakarta.annotation.PostConstruct;
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
public class HiringService {
    private static final Logger log = LoggerFactory.getLogger(HiringService.class);

    private final RestTemplate restTemplate = new RestTemplate();
    private final String worknetKey;
    private final String worknetUrl;
    private final CopyOnWriteArrayList<HiringCategory> cache = new CopyOnWriteArrayList<>();

    public HiringService(
            @Value("${worknet.api.key:}") String worknetKey,
            @Value("${worknet.api.url:https://openapi.work.go.kr/opi/opi/opia/wantedApi.do}") String worknetUrl
    ) {
        this.worknetKey = worknetKey;
        this.worknetUrl = worknetUrl;
    }

    @PostConstruct
    public void init() {
        refresh();
    }

    public List<HiringCategory> list() {
        return Collections.unmodifiableList(cache);
    }

    @Scheduled(cron = "0 0 4 * * *")
    public void refresh() {
        try {
            List<HiringCategory> categories = new ArrayList<>();
            categories.add(fetchWorkNet());
            categories.add(sampleJobKorea());
            cache.clear();
            cache.addAll(categories);
            log.info("Hiring categories refreshed: {}", cache.size());
        } catch (Exception ex) {
            log.warn("Hiring refresh failed, using previous cache", ex);
            if (cache.isEmpty()) {
                cache.add(sampleJobKorea());
            }
        }
    }

    private HiringCategory fetchWorkNet() {
        if (worknetKey == null || worknetKey.isBlank()) {
            return sampleWorkNet();
        }
        try {
            // WorkNet API (간략: 최근 공고 50건, XML)
            String url = worknetUrl +
                    "?callTp=L&returnType=XML&authKey=" + worknetKey +
                    "&pageNum=1&display=50";
            String xml = restTemplate.getForObject(url, String.class);
            List<JobPost> posts = parseWorkNetXml(xml);
            if (posts.isEmpty()) return sampleWorkNet();
            return new HiringCategory("워크넷", posts);
        } catch (Exception ex) {
            log.warn("WorkNet fetch failed", ex);
            return sampleWorkNet();
        }
    }

    private List<JobPost> parseWorkNetXml(String xml) {
        List<JobPost> result = new ArrayList<>();
        if (xml == null || xml.isBlank()) return result;
        try {
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
                String url = text(n, "wantedInfoUrl");
                LocalDate deadline = parseDate(text(n, "closeDate"));
                List<String> keywords = new ArrayList<>();
                String jobType = text(n, "jobType");
                if (!jobType.isBlank()) keywords.add(jobType);
                result.add(new JobPost(id, company, title, url, region, deadline, keywords));
            }
        } catch (Exception ex) {
            log.warn("WorkNet XML parse error", ex);
        }
        return result;
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

    private HiringCategory sampleWorkNet() {
        List<JobPost> list = List.of(
                new JobPost("wn-s1", "샘플(워크넷)", "백엔드 개발자", "https://www.work.go.kr", "서울", LocalDate.now().plusDays(20), List.of("Java", "Spring")),
                new JobPost("wn-s2", "샘플(워크넷)", "데이터 엔지니어", "https://www.work.go.kr", "원격", LocalDate.now().plusDays(25), List.of("Python", "ETL"))
        );
        return new HiringCategory("워크넷", list);
    }

    private HiringCategory sampleJobKorea() {
        List<JobPost> list = List.of(
                new JobPost("jk-s1", "샘플(JobKorea)", "프론트엔드", "https://www.jobkorea.co.kr", "판교", LocalDate.now().plusDays(18), List.of("TypeScript", "React")),
                new JobPost("jk-s2", "샘플(JobKorea)", "모바일 iOS", "https://www.jobkorea.co.kr", "서울", LocalDate.now().plusDays(12), List.of("Swift", "UIKit"))
        );
        return new HiringCategory("잡코리아", list);
    }
}
