package com.jrm.app.service;

import com.jrm.app.model.UserAccount;
import com.jrm.app.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class AuthService {
    private final Map<String, String> tokenToEmail = new ConcurrentHashMap<>();
    private final UserAccountRepository userAccountRepository;
    private final String appName;

    public AuthService(UserAccountRepository userAccountRepository,
                       @Value("${app.name:JobRoadMap}") String appName) {
        this.userAccountRepository = userAccountRepository;
        this.appName = appName;
    }

    @Transactional
    public String login(String email, String password) {
        UserAccount account = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("계정을 찾을 수 없습니다."));
        if (!account.getPassword().equals(password)) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        String token = UUID.randomUUID().toString();
        tokenToEmail.put(token, email);
        return token;
    }

    @Transactional
    public String register(String email, String password, String username) {
        Optional<UserAccount> existing = userAccountRepository.findByEmail(email);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }
        UserAccount acc = new UserAccount(email, password);
        if (username != null && !username.isBlank()) acc.setUsername(username);
        acc.setVerified(true);
        acc.setVerificationCode(null);
        userAccountRepository.save(acc);
        return "OK";
    }

    @Transactional
    public void verify(String email, String code) {
        UserAccount account = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("계정을 찾을 수 없습니다."));
        account.setVerified(true);
        account.setVerificationCode(null);
        userAccountRepository.save(account);
    }

    @Transactional
    public String requestReset(String email) {
        UserAccount account = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("계정을 찾을 수 없습니다."));
        String code = generateCode();
        account.setResetCode(code);
        userAccountRepository.save(account);
        return code;
    }

    @Transactional
    public void resetPassword(String email, String code, String newPassword) {
        UserAccount account = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("계정을 찾을 수 없습니다."));
        if (!code.equals(account.getResetCode())) {
            throw new IllegalArgumentException("재설정 코드가 올바르지 않습니다.");
        }
        account.setPassword(newPassword);
        account.setResetCode(null);
        userAccountRepository.save(account);
    }

    @Transactional
    public void updateUsername(String token, String newUsername) {
        String email = getEmail(token);
        if (email == null) throw new IllegalArgumentException("인증이 필요합니다.");
        UserAccount acc = userAccountRepository.findByEmail(email).orElseThrow();
        acc.setUsername(newUsername);
        userAccountRepository.save(acc);
    }

    @Transactional
    public void updatePassword(String token, String currentPassword, String newPassword) {
        String email = getEmail(token);
        if (email == null) throw new IllegalArgumentException("인증이 필요합니다.");
        UserAccount acc = userAccountRepository.findByEmail(email).orElseThrow();
        if (!acc.getPassword().equals(currentPassword)) throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        acc.setPassword(newPassword);
        userAccountRepository.save(acc);
    }

    public void logout(String token) {
        tokenToEmail.remove(token);
    }

    public String getEmail(String token) {
        return tokenToEmail.get(token);
    }

    public UserAccount getAccount(String token) {
        String email = getEmail(token);
        if (email == null) return null;
        return userAccountRepository.findByEmail(email).orElse(null);
    }

    private String generateCode() {
        int code = ThreadLocalRandom.current().nextInt(100000, 999999);
        return String.valueOf(code);
    }
}
