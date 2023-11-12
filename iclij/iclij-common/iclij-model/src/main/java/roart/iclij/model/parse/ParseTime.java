package roart.iclij.model.parse;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class ParseTime extends ParseObject {
    private String unit;
    
    private Number number;
    
    private String when;
    
    private boolean now;
    
    private boolean plural;
    
    private ChronoUnit timeunit;
    
    private LocalDate date;
    
    private String period;

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Number getNumber() {
        return number;
    }

    public void setNumber(Number number) {
        this.number = number;
    }

    public String getWhen() {
        return when;
    }

    public void setWhen(String when) {
        this.when = when;
    }

    public boolean isNow() {
        return now;
    }

    public void setNow(boolean now) {
        this.now = now;
    }

    public boolean isPlural() {
        return plural;
    }

    public void setPlural(boolean plural) {
        this.plural = plural;
    }

    public ChronoUnit getTimeunit() {
        return timeunit;
    }

    public void setTimeunit(ChronoUnit timeunit) {
        this.timeunit = timeunit;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public void show() {
        log.debug("Time {}", unit);
        log.debug(" number {}", getNumber());
        log.debug(" when {}", when);
        log.debug(" now {}", isNow());
        log.debug(" plural {}", isPlural());
        log.debug(" timeunit {}", getTimeunit());
        log.debug(" date {}", getDate());
        log.debug(" period {}", period);
    }

}


