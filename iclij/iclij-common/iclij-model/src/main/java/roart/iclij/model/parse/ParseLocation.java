package roart.iclij.model.parse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParseLocation {
    protected static Logger log = LoggerFactory.getLogger(ParseLocation.class);

    private String country;

    public String getCountry() {
        return country;
    }

    public void setCountry(String l) {
        this.country = l;
    }

    public void show() {
        log.debug("Location {}", getCountry());
    }

}


