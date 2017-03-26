package roart.category;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.config.MyConfig;
import roart.indicator.Indicator;
import roart.model.ResultItemTableRow;
import roart.model.StockItem;

public abstract class Category {

    protected static Logger log = LoggerFactory.getLogger(Category.class);

    protected String title;
    protected MyConfig conf;
    protected List<StockItem> stocks;
    protected List<Indicator> indicators = new ArrayList();

    public Category(MyConfig conf, String periodText, List<StockItem> stocks) {
        this.conf = conf;
        title = periodText;
        this.stocks = stocks;
    }

    abstract public void addResultItemTitle(ResultItemTableRow r);

    abstract public void addResultItem(ResultItemTableRow r, StockItem stock);
}

