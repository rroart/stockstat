package roart.common.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.constants.Constants;

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

    public static int getIndexEqualBefore(List<String> stockDates, LocalDate date) {
        return getIndexEqualBefore(stockDates, TimeUtil.convertDate2(date));
    }

    public static int getIndexEqualBefore(List<String> stockDates, String date) {
        int index = Collections.binarySearch(stockDates, date);
        if (index >= 0) {
            return index;
        } else {
            return -index - 2;
        }
        /*
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
        */
    }

    public static LocalDate getEqualBefore(List<String> stockDates, LocalDate date) {
        return getEqualBefore(stockDates, convertDate2(date));
    }
    
    public static LocalDate getEqualBefore(List<String> stockDates, String date) {
        int index = getIndexEqualBefore(stockDates, date);
        try {
            return convertDate(stockDates.get(index));
        } catch (ParseException e) {
            log.error(Constants.EXCEPTION, e);
            return null;
        }
    }
    
    public static int getIndexEqualAfter(List<String> stockDates, String date) {
        int index = Collections.binarySearch(stockDates, date);
        if (index >= 0) {
            return index;
        } else {
            index = -index - 1;
            if (index == stockDates.size()) {
                index--;
            }
            return index;
        }
    }

    public List<String> setDates(String date, List<String> stockdates, int loopoffset, int offset, int futuredays) {
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
        String baseDateStr = stockdates.get(stockdates.size() - 1 - futuredays - dateoffset);
        String futureDateStr = stockdates.get(stockdates.size() - 1 - dateoffset);
        LocalDate aBaseDate = null;
        LocalDate aFutureDate = null;
        try {
            aBaseDate = convertDate(baseDateStr);
            aFutureDate = convertDate(futureDateStr);
        } catch (ParseException e) {
            log.error(Constants.EXCEPTION, e);
        }
        aBaseDate = aBaseDate.plusDays(offset + loopoffset);
        aFutureDate = aFutureDate.plusDays(offset + loopoffset);
        baseDateStr = convertDate2(aBaseDate);
        futureDateStr = convertDate2(aFutureDate);
        baseDateStr = stockdates.get(getIndexEqualAfter(stockdates, baseDateStr));
        futureDateStr = stockdates.get(getIndexEqualAfter(stockdates, futureDateStr));
        List<String> list = new ArrayList<>();
        list.add(baseDateStr);
        list.add(futureDateStr);
        return list;
    }
    
    public static String getBackEqualBefore(LocalDate date, int back, List<String> stockDates) {
        int index = getIndexEqualBefore(stockDates, TimeUtil.convertDate2(date));
        index = index - back;
        return stockDates.get(index);
    }

    public static LocalDate getBackEqualBefore2(LocalDate date, int back, List<String> stockDates) {
        String str = getBackEqualBefore(date, back, stockDates);
        try {
            return convertDate(str);
        } catch (ParseException e) {
            log.error(Constants.EXCEPTION, e);
            return null;
        }
    }

}
