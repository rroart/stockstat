package roart.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeUtil {
	
    private static Logger log = LoggerFactory.getLogger(TimeUtil.class);
    
    public static final String MYDATEFORMAT = "yyyy.MM.dd";

    public static Date convertDate(LocalDate date) {
        if (date == null) {
            log.error("Date null (break point)");
            return null;
        }
        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public static LocalDate convertDate(String aDate) throws ParseException {
        SimpleDateFormat dt = new SimpleDateFormat(MYDATEFORMAT);
        return convertDate(dt.parse(aDate));
    }
    
    public static LocalDate convertDate(Date date) {
        if (date == null) {
            log.error("Date null (break point)");
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
