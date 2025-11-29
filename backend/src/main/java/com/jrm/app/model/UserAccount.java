package com.jrm.app.model;

import jakarta.persistence.*;

@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class UserAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private int roadmapCredits = 0;

    @Column(nullable = false)
    private int aiCredits = 0;

    @Column(nullable = false)
    private boolean verified = true;

    @Column
    private String verificationCode;

    @Column
    private String resetCode;

    public UserAccount() {
    }

    public UserAccount(String email, String password) {
        this.email = email;
        this.username = deriveUsername(email);
        this.password = password;
        this.roadmapCredits = 0;
        this.aiCredits = 0;
        this.verified = true;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getRoadmapCredits() {
        return roadmapCredits;
    }

    public int getAiCredits() {
        return aiCredits;
    }

    public void addRoadmapCredits(int delta) {
        this.roadmapCredits = Math.max(0, this.roadmapCredits + delta);
    }

    public void addAiCredits(int delta) {
        this.aiCredits = Math.max(0, this.aiCredits + delta);
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }

    public String getResetCode() {
        return resetCode;
    }

    public void setResetCode(String resetCode) {
        this.resetCode = resetCode;
    }

    private String deriveUsername(String email) {
        if (email == null) return "";
        int idx = email.indexOf('@');
        return idx > 0 ? email.substring(0, idx) : email;
    }
}
