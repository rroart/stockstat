package roart.category.util;

import java.util.List;

import roart.category.AbstractCategory;
import roart.category.impl.CategoryIndex;
import roart.category.impl.CategoryPeriod;
import roart.category.impl.CategoryPrice;
import roart.common.constants.Constants;
import roart.common.inmemory.model.Inmemory;
import roart.common.model.StockItem;
import roart.common.pipeline.data.PipelineData;
import roart.iclij.config.IclijConfig;

public class CategoryUtil {

    public AbstractCategory[] getCategories(IclijConfig conf, List<StockItem> stocks,
            String[] periodText,
            PipelineData[] datareaders, Inmemory inmemory) throws Exception {
        AbstractCategory[] categories = new AbstractCategory[Constants.PERIODS + 2];
        categories[0] = new CategoryIndex(conf, Constants.INDEX, stocks, datareaders, inmemory);
        categories[1] = new CategoryPrice(conf, Constants.PRICE, stocks, datareaders, inmemory);
        for (int i = 0; i < Constants.PERIODS; i++) {
            categories[i + 2] = new CategoryPeriod(conf, i, periodText[i], stocks, datareaders, inmemory);
        }
        return categories;
    }

}
