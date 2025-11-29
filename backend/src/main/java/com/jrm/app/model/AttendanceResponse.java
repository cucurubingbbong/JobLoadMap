package com.jrm.app.model;

import java.time.LocalDate;
import java.util.Set;

public class AttendanceResponse {
    private final Set<LocalDate> checkedDates;
    private final int streak;

    public AttendanceResponse(Set<LocalDate> checkedDates, int streak) {
        this.checkedDates = checkedDates;
        this.streak = streak;
    }

    public Set<LocalDate> getCheckedDates() {
        return checkedDates;
    }

    public int getStreak() {
        return streak;
    }
}
