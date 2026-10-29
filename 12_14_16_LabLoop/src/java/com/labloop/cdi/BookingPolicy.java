package com.labloop.cdi;

import com.labloop.entity.LabUser;
import com.labloop.entity.UserRole;
import com.labloop.service.LabLoopSettingsBean;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * CDI @ApplicationScoped bean holding the lab's booking rules in ONE place.
 * Injected into BookingService (an EJB) - shows CDI and EJB working together.
 * The limits come from the Singleton settings bean, so an admin can change them at runtime.
 */
@ApplicationScoped
public class BookingPolicy {

    private static final LocalTime LAB_OPENS = LocalTime.of(8, 0);
    private static final LocalTime LAB_CLOSES = LocalTime.of(20, 0);
    private static final int MIN_MINUTES = 30;

    @EJB
    private LabLoopSettingsBean settings;

    /** Returns every rule the requested slot breaks (empty list = allowed). */
    public List<String> check(LabUser user, LocalDateTime start, LocalDateTime end) {
        List<String> problems = new ArrayList<>();
        if (start == null || end == null) {
            problems.add("Start and end time are both required.");
            return problems;
        }
        LocalDateTime now = LocalDateTime.now();
        if (!end.isAfter(start)) {
            problems.add("End time must be after start time.");
            return problems;
        }
        if (start.isBefore(now.minusMinutes(5))) {
            problems.add("Start time is in the past.");
        }
        if (start.isAfter(now.plusDays(settings.getMaxAdvanceDays()))) {
            problems.add("Bookings can be made at most " + settings.getMaxAdvanceDays() + " days in advance.");
        }
        long minutes = Duration.between(start, end).toMinutes();
        if (minutes < MIN_MINUTES) {
            problems.add("Minimum booking is " + MIN_MINUTES + " minutes.");
        }
        int maxHours = maxHoursFor(user.getRole());
        if (minutes > maxHours * 60L) {
            problems.add(user.getRole().getLabel() + "s can book for at most " + maxHours + " hours at a time.");
        }
        if (user.getRole() == UserRole.STUDENT) {
            if (start.getDayOfWeek() == DayOfWeek.SUNDAY || end.getDayOfWeek() == DayOfWeek.SUNDAY) {
                problems.add("Labs are closed for students on Sundays.");
            }
            if (start.toLocalTime().isBefore(LAB_OPENS) || end.toLocalTime().isAfter(LAB_CLOSES)
                    || !start.toLocalDate().equals(end.toLocalDate())) {
                problems.add("Student bookings must be within lab hours (08:00 - 20:00) on a single day.");
            }
        }
        return problems;
    }

    public int maxHoursFor(UserRole role) {
        return role == UserRole.STUDENT ? settings.getStudentMaxHours() : settings.getFacultyMaxHours();
    }

    /** Faculty and staff bookings are trusted; student bookings wait for a lab assistant. */
    public boolean autoApprove(LabUser user) {
        return user.getRole() != UserRole.STUDENT;
    }
}
