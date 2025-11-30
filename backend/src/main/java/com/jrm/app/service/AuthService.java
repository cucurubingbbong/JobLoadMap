package com.jrm.app.service;

import com.jrm.app.model.UserAccount;
import com.jrm.app.repository.UserAccountRepository;
import com.jrm.app.repository.UserSessionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class AuthService {
    private final UserAccountRepository userAccountRepository;
    private final UserSessionRepository userSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final String appName;

    public AuthService(UserAccountRepository userAccountRepository,
                       UserSessionRepository userSessionRepository,
                       PasswordEncoder passwordEncoder,
                       @Value("${app.name:JobRoadMap}") String appName) {
        this.userAccountRepository = userAccountRepository;
        this.userSessionRepository = userSessionRepository;
        this.passwordEncoder = passwordEncoder;
        this.appName = appName;
    }

    @Transactional
    public String login(String email, String password) {
        UserAccount account = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("계정을 찾을 수 없습니다."));
        if (!account.isVerified()) throw new IllegalArgumentException("이메일 인증이 필요합니다.");
        if (!matchesAndUpgradePassword(account, password)) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        // 기존 세션 정리 후 새 토큰 발급
        userSessionRepository.deleteByUser(account);
        var session = userSessionRepository.save(new com.jrm.app.model.UserSession(account));
        return session.getToken();
    }

    @Transactional
    public String register(String email, String password, String username) {
        Optional<UserAccount> existing = userAccountRepository.findByEmail(email);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }
        if (email == null || email.isBlank()) throw new IllegalArgumentException("이메일을 입력하세요.");
        if (password == null || password.isBlank()) throw new IllegalArgumentException("비밀번호를 입력하세요.");
        UserAccount acc = new UserAccount(email, password);
        acc.setPassword(passwordEncoder.encode(password));
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
        account.setPassword(passwordEncoder.encode(newPassword));
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
        if (!passwordEncoder.matches(currentPassword, acc.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }
        acc.setPassword(passwordEncoder.encode(newPassword));
        userAccountRepository.save(acc);
    }

    public void logout(String token) {
        if (token != null) {
            userSessionRepository.deleteByToken(token);
        }
    }

    public String getEmail(String token) {
        return userSessionRepository.findByToken(token)
                .map(session -> session.getUser().getEmail())
                .orElse(null);
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

    private boolean matchesAndUpgradePassword(UserAccount account, String rawPassword) {
        String stored = account.getPassword();
        if (passwordEncoder.matches(rawPassword, stored)) {
            return true;
        }
        // 기존 평문 비밀번호 데이터가 있을 경우 한 번 허용 후 즉시 해시로 교체
        if (!isEncoded(stored) && stored.equals(rawPassword)) {
            account.setPassword(passwordEncoder.encode(rawPassword));
            userAccountRepository.save(account);
            return true;
        }
        return false;
    }

    private boolean isEncoded(String value) {
        return value != null && value.startsWith("$2");
    }
}
