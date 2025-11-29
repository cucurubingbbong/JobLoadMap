package com.jrm.app.repository;

import com.jrm.app.model.AttendanceEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceEntryRepository extends JpaRepository<AttendanceEntry, Long> {
    boolean existsByEmailAndAttendDate(String email, LocalDate date);
    List<AttendanceEntry> findByEmail(String email);
    long countByEmail(String email);
}
