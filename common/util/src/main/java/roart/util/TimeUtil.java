package roart.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class TimeUtil {
    public static Date convertDate(LocalDate date) {
        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public static LocalDate convertDate(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();        
    }

    public static String convertDate2(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        return date.format(formatter);
    }

    public static long daysSince(LocalDate date) {
        return ChronoUnit.DAYS.between(date, LocalDate.now());
    }
    
    public static Date convertDate(LocalDateTime date) {
        return Date.from(date.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static LocalDateTime convertDate2(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();      
    }

    public static long daysSince(LocalDateTime date) {
        return ChronoUnit.DAYS.between(LocalDate.now(), date);
    }
    
}
