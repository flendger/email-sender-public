package ru.flendger.email.sender.gui.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class SenderSchedule {

    private boolean scheduleStatus;
    private LocalDateTime lastDateTime;
    private LocalDateTime nextDateTime;
    private final boolean[] days;
    private int hours;
    private int minutes;


    public SenderSchedule() {
        days = new boolean[7];
    }

    public void start() {
        scheduleStatus = true;
    }

    public void stop() {
        scheduleStatus = false;
        nextDateTime = null;
    }

    public boolean getStatus() {
        return scheduleStatus;
    }

    public LocalDateTime getLastDateTime() {
        return lastDateTime;
    }

    public void setLastDateTime(LocalDateTime lastDateTime) {
        this.lastDateTime = lastDateTime;
    }

    public LocalDateTime getNextDateTime() {
        return nextDateTime;
    }

    public void setNextDateTime(LocalDateTime nextDateTime) {
        this.nextDateTime = nextDateTime;
    }

    public boolean isDaySelected() {
        for (boolean isDay : days) {
            if (isDay) return true;
        }
        return false;
    }

    public LocalDateTime getScheduleDateTime() {
        if (!isDaySelected()) return null;

        LocalDateTime curDateTime = LocalDateTime.now();
        LocalDate scheduleDate = curDateTime.toLocalDate();

        LocalTime scheduleTime = LocalTime.of(hours, minutes);

        if (curDateTime.toLocalTime().compareTo(scheduleTime) >= 0) {
            scheduleDate = scheduleDate.plusDays(1);
        }

        while (!days[scheduleDate.getDayOfWeek().getValue()-1]) {
            scheduleDate = scheduleDate.plusDays(1);
        }

        return LocalDateTime.of(scheduleDate, scheduleTime);
    }

    public void setSchedule(boolean monday,
                            boolean tuesday,
                            boolean wednesday,
                            boolean thursday,
                            boolean friday,
                            boolean saturday,
                            boolean sunday,
                            int hours,
                            int minutes) {
        days[0] = monday;
        days[1] = tuesday;
        days[2] = wednesday;
        days[3] = thursday;
        days[4] = friday;
        days[5] = saturday;
        days[6] = sunday;
        this.hours = hours;
        this.minutes = minutes;
    }
}
