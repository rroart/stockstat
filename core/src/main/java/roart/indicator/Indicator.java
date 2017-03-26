package roart.indicator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.config.MyConfig;
import roart.model.StockItem;

public abstract class Indicator {

    protected static Logger log = LoggerFactory.getLogger(Indicator.class);

    protected String title;
    protected MyConfig conf;
    protected int category;
    
    public Indicator(MyConfig conf, String string, int category) {
        this.title = string;
        this.conf = conf;
        this.category = category;
    }

    abstract public boolean isEnabled();

    public String getResultItemTitle() {
        return title;
    }

    abstract public Object getResultItem(StockItem stock);

}

