package com.jrm.app.service;

import com.jrm.app.model.AttendanceResponse;
import com.jrm.app.model.AttendanceEntry;
import com.jrm.app.model.UserAccount;
import com.jrm.app.repository.AttendanceEntryRepository;
import com.jrm.app.repository.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
public class AttendanceService {
    private final AttendanceEntryRepository attendanceRepo;
    private final UserAccountRepository userRepo;

    public AttendanceService(AttendanceEntryRepository attendanceRepo, UserAccountRepository userRepo) {
        this.attendanceRepo = attendanceRepo;
        this.userRepo = userRepo;
    }

    @Transactional
    public AttendanceResponse checkIn(String email) {
        if (!userRepo.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("계정을 찾을 수 없습니다.");
        }
        LocalDate today = LocalDate.now();
        if (!attendanceRepo.existsByEmailAndAttendDate(email, today)) {
            attendanceRepo.save(new AttendanceEntry(email, today));
            allocateReward(email);
        }
        return build(email);
    }

    public AttendanceResponse build(String email) {
        Set<LocalDate> dates = new HashSet<>();
        attendanceRepo.findByEmail(email).forEach(e -> dates.add(e.getAttendDate()));
        int streak = 0;
        LocalDate cursor = LocalDate.now();
        while (dates.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return new AttendanceResponse(dates, streak);
    }

    private void allocateReward(String email) {
        // 적립 로직 제거 (현재는 출석만 기록)
    }
}
