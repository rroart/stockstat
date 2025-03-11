package roart.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import roart.common.model.MetaItem;
import roart.common.model.MyDataSource;
import roart.common.model.StockItem;
import roart.iclij.config.IclijConfig;

public class TestDataSources extends MyDataSource{

    private List<TestDataSource> testDataSources;
    
    private List<MetaItem> metas = new ArrayList<>();
    
    private Map<String, List<StockItem>> stockMap = new HashMap<>();

    public TestDataSources(List<TestDataSource> testDataSources) {
        super();
        this.testDataSources = testDataSources;
        for (TestDataSource testDataSource : testDataSources) {
            metas.addAll(testDataSource.getMetas());
            stockMap.put(testDataSource.marketName, testDataSource.getAll(testDataSource.marketName, null));
        }
    }

    @Override
    public List<MetaItem> getMetas() {
        return metas;
    }

    @Override
    public List<StockItem> getAll(String market, IclijConfig conf) {
        return stockMap.get(market);
    }

}
