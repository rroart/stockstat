package roart.category.impl;

import java.util.List;

import roart.category.AbstractCategory;
import roart.iclij.config.IclijConfig;
import roart.common.model.StockItem;
import roart.pipeline.Pipeline;
import roart.result.model.ResultItemTableRow;
import roart.stockutil.StockDao;
import roart.stockutil.StockUtil;

public class Category extends AbstractCategory {

    public Category(IclijConfig conf, String periodText, List<StockItem> stocks, Pipeline[] datareaders) {
        super(conf, periodText, stocks, datareaders);
    }

    @Override
    public boolean hasContent() throws Exception {
        return StockUtil.hasStockValue(stocks, period);
    }
    
    @Override
    public Double[] getData(StockItem stock) throws Exception {
        return StockDao.getValue(stock, period);
    }

    @Override
    public void addResultItemTitle(ResultItemTableRow r) {
    }

    @Override
    public void addResultItem(ResultItemTableRow r, StockItem stock) {
    }

}
