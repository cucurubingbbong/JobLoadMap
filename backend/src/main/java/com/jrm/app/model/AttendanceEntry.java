package com.jrm.app.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "attendance", uniqueConstraints = @UniqueConstraint(columnNames = {"email", "attendDate"}))
public class AttendanceEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private LocalDate attendDate;

    public AttendanceEntry() {
    }

    public AttendanceEntry(String email, LocalDate attendDate) {
        this.email = email;
        this.attendDate = attendDate;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public LocalDate getAttendDate() {
        return attendDate;
    }
}
