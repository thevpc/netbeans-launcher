/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.netbeans.launcher.util;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * @author vpc
 */
public class LocalDateTimePeriod {

    private final long years;

    private final long months;

    private final long days;

    private final long hours;

    private final long minutes;

    private final long seconds;

    private final long milliseconds;

    public static LocalDateTimePeriod between(LocalDateTime fromDateTime, LocalDateTime toDateTime) {
        LocalDateTime tempDateTime = LocalDateTime.from(fromDateTime);

        long years = tempDateTime.until(toDateTime, ChronoUnit.YEARS);
        tempDateTime = tempDateTime.plusYears(years);

        long months = tempDateTime.until(toDateTime, ChronoUnit.MONTHS);
        tempDateTime = tempDateTime.plusMonths(months);

        long days = tempDateTime.until(toDateTime, ChronoUnit.DAYS);
        tempDateTime = tempDateTime.plusDays(days);

        long hours = tempDateTime.until(toDateTime, ChronoUnit.HOURS);
        tempDateTime = tempDateTime.plusHours(hours);

        long minutes = tempDateTime.until(toDateTime, ChronoUnit.MINUTES);
        tempDateTime = tempDateTime.plusMinutes(minutes);

        long seconds = tempDateTime.until(toDateTime, ChronoUnit.SECONDS);
        tempDateTime = tempDateTime.plusSeconds(seconds);

        long milliseconds = tempDateTime.until(toDateTime, ChronoUnit.MILLIS);
        return new LocalDateTimePeriod(years, months, days, hours, minutes, seconds, milliseconds);
    }

    public LocalDateTimePeriod(long years, long months, long days, long hours, long minutes, long seconds, long milliseconds) {
        this.years = years;
        this.months = months;
        this.days = days;
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
        this.milliseconds = milliseconds;
    }

    public LocalDateTimePeriod update(long years, long months, long days, long hours, long minutes, long seconds, long milliseconds) {
        return new LocalDateTimePeriod(years < 0 ? this.years : years,
                months < 0 ? this.months : months,
                days < 0 ? this.days : days,
                hours < 0 ? this.hours : hours,
                minutes < 0 ? this.minutes : minutes,
                seconds < 0 ? this.seconds : seconds,
                milliseconds < 0 ? this.milliseconds : milliseconds
        );
    }

    public long getYears() {
        return years;
    }

    public LocalDateTimePeriod setYears(long years) {
        return update(years, months, days, hours, minutes, seconds, milliseconds);
    }

    public long getMonths() {
        return months;
    }

    public LocalDateTimePeriod setMonths(long months) {
        return update(years, months, days, hours, minutes, seconds, milliseconds);
    }

    public long getDays() {
        return days;
    }

    public LocalDateTimePeriod setDays(long days) {
        return update(years, months, days, hours, minutes, seconds, milliseconds);
    }

    public long getHours() {
        return hours;
    }

    public LocalDateTimePeriod setHours(long hours) {
        return update(years, months, days, hours, minutes, seconds, milliseconds);
    }

    public long getMinutes() {
        return minutes;
    }

    public LocalDateTimePeriod setMinutes(long minutes) {
        return update(years, months, days, hours, minutes, seconds, milliseconds);
    }

    public long getSeconds() {
        return seconds;
    }

    public LocalDateTimePeriod setSeconds(long seconds) {
        return update(years, months, days, hours, minutes, seconds, milliseconds);
    }

    public long getMilliseconds() {
        return milliseconds;
    }

    public LocalDateTimePeriod setMilliseconds(long milliseconds) {
        return update(years, months, days, hours, minutes, seconds, milliseconds);
    }

    public String getFullMessage() {
        StringBuilder sb = new StringBuilder();
        if (years > 0) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(years).append(" years");
        }
        if (months > 0) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(months).append(" months");
        }
        if (days > 0) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(days).append(" days");
        }
        if (hours > 0) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(hours).append(" hours");
        }
        if (minutes > 0) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(minutes).append(" minutes");
        }
        if (seconds > 0) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(seconds).append(" seconds");
        }
        if (milliseconds > 0) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(milliseconds).append(" milliseconds");
        }
        if (sb.length() == 0) {
            return "0 seconds";
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (years > 0) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(years).append(" years");
            return sb.toString();
        }
        if (months > 0) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(months).append(" months");
            return sb.toString();
        }
        if (days > 0) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(days).append(" days");
            return sb.toString();
        }
        if (hours > 0) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(hours).append(" hours");
        }
        if (minutes > 0) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(minutes).append(" minutes");
        }
        if(hours>0 || minutes>0){
            return sb.toString();
        }
        if (seconds > 0) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(seconds).append(" seconds");
            return sb.toString();
        }
        if (milliseconds > 0) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(milliseconds).append(" milliseconds");
        }
        if (sb.length() == 0) {
            return "0 seconds";
        }
        return sb.toString();
    }

}
