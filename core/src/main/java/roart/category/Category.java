package roart.category;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.config.MyConfig;
import roart.indicator.Indicator;
import roart.model.ResultItemTableRow;
import roart.model.Stock;
import roart.service.ControlService;
import roart.util.MarketData;
import roart.util.PeriodData;

public abstract class Category {

    protected static Logger log = LoggerFactory.getLogger(Category.class);

    protected String title;
    protected MyConfig conf;
    protected List<Stock> stocks;
    protected List<Indicator> indicators = new ArrayList();

    public Category(MyConfig conf, String periodText, List<Stock> stocks) {
        this.conf = conf;
        title = periodText;
        this.stocks = stocks;
    }

    abstract public void addResultItemTitle(ResultItemTableRow r);

    abstract public void addResultItem(ResultItemTableRow r, Stock stock);
}

