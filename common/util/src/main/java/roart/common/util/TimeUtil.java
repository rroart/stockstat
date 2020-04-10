package roart.common.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
        if (date == null) {
            return null;
        }
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
    
    public static int getIndexEqualBefore(List<String> stockDates, String date) {
        int dateIndex = stockDates.indexOf(date);
        if (dateIndex < 0) {
            dateIndex = stockDates.size() - 1;
            for (int i = 1; i < stockDates.size(); i++) {
                if (date.compareTo(stockDates.get(i)) < 0) {
                    dateIndex = i - 1;
                }
            }
        }
        return dateIndex;
    }

    public List<String> setDates(String date, List<String> stockdates, int offset, int loopoffset, int futuredays) {
        if (date != null) {
            int index = stockdates.indexOf(date);
            if (index < 0) {
                date = null;
            }
        }
        if (date == null) {
            if (stockdates.isEmpty()) {
                int jj = 0;
            }
            date = stockdates.get(stockdates.size() - 1);
        }
        int dateoffset = 0;
        if (date != null) {
            int index = stockdates.indexOf(date);
            if (index >= 0) {
                dateoffset = stockdates.size() - 1 - index;
            }
        }
        String baseDateStr = stockdates.get(stockdates.size() - 1 - futuredays - dateoffset - offset - loopoffset);
        String futureDateStr = stockdates.get(stockdates.size() - 1 - dateoffset - offset - loopoffset);
        List<String> list = new ArrayList<>();
        list.add(futureDateStr);
        list.add(baseDateStr);
        return list;
    }
}
