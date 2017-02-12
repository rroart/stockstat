package roart.category;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.indicator.Indicator;
import roart.model.ResultItem;
import roart.model.Stock;
import roart.service.ControlService;
import roart.util.MarketData;
import roart.util.PeriodData;

public abstract class Category {

    protected static Logger log = LoggerFactory.getLogger(Category.class);

    protected String title;
    protected ControlService controlService;
    protected List<Stock> stocks;
    protected List<Indicator> indicators = new ArrayList();

    public Category(ControlService controlService, String periodText, List<Stock> stocks) {
        this.controlService = controlService;
        title = periodText;
        this.stocks = stocks;
    }

    abstract public void addResultItemTitle(ResultItem ri);

    abstract public void addResultItem(ResultItem ri, Stock stock);
}

