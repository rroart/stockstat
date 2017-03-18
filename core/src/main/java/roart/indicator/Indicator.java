package roart.indicator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.config.MyConfig;
import roart.model.Stock;

public abstract class Indicator {

    protected static Logger log = LoggerFactory.getLogger(Indicator.class);

    protected String title;
    protected MyConfig conf;
    
    public Indicator(MyConfig conf, String string) {
        this.title = string;
        this.conf = conf;
    }

    abstract public boolean isEnabled();

    public String getResultItemTitle() {
        return title;
    }

    abstract public Object getResultItem(Stock stock);

}

