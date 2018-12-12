package roart.config;

import java.time.LocalDate;

public class VerifyConfig {

    public VerifyConfig() {
        // empty due to JSON
    }

    private LocalDate date;

    private String market;

    //private Integer days = 10;

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setMarket(String value) {
        market = value;
    }

    public String getMarket() {
        return market;
    }

    /*
    public Integer getDays() {
        return days;
    }

    public void setDays(Integer mydays) {
        this.days = mydays;
    }
*/
}
