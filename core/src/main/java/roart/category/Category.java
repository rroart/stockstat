package roart.category;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.config.MyConfig;
import roart.indicator.Indicator;
import roart.model.ResultItem;
import roart.model.ResultItemTable;
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

    public static void mapAdder(Map<Integer, List<ResultItemTableRow>> map, Integer key, List<ResultItemTableRow> add) {
        List<ResultItemTableRow> val = map.get(key);
        if (val == null) {
            val = new ArrayList<>();
            map.put(key, val);
        }
        val.addAll(add);
    }

    public Map<Integer, List<ResultItemTableRow>> otherTables() {
        Map<Integer, List<ResultItemTableRow>> allTablesMap = new HashMap<>();
        for (Indicator indicator : indicators) {
            Map<Integer, List<ResultItemTableRow>> tables = indicator.otherTables();
            if (tables == null) {
                continue;
            }
            for (Integer key : tables.keySet()) {
                mapAdder(allTablesMap, key, tables.get(key));
            }
        }
        return allTablesMap;
    }
}

