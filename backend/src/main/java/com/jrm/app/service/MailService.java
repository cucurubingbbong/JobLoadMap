package com.jrm.app.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {
    private static final Logger log = LoggerFactory.getLogger(MailService.class);
    private final JavaMailSender mailSender;
    private final String from;
    private final boolean enabled;

    public MailService(JavaMailSender mailSender,
                       @Value("${spring.mail.username:}") String from) {
        this.mailSender = mailSender;
        this.from = from;
        this.enabled = from != null && !from.isBlank();
    }

    /**
     * 이메일이 설정되지 않은 경우 로그로 대체.
     */
    public void send(String to, String subject, String content) {
        if (!enabled) {
            log.info("[메일 전송 생략] to={}, subject={}, content={}", to, subject, content);
            return;
        }
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(content);
        mailSender.send(msg);
    }
}
