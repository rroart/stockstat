package roart.indicator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.model.Stock;
import roart.service.ControlService;

public abstract class Indicator {

    protected static Logger log = LoggerFactory.getLogger(Indicator.class);

    protected String title;
    protected ControlService controlService;

    public Indicator(ControlService controlService, String string) {
        this.title = string;
        this.controlService = controlService;
    }

    abstract public boolean isEnabled();

    public String getResultItemTitle() {
        return title;
    }

    abstract public Object getResultItem(Stock stock);

}

