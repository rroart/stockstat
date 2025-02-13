package roart.controller;

import java.util.List;

import roart.common.model.MetaItem;
import roart.common.model.MyDataSource;
import roart.common.model.StockItem;
import roart.iclij.config.IclijConfig;

public class DummyDataSource extends MyDataSource {

    @Override
    public List<MetaItem> getMetas() {
        return List.of();
    }

    @Override
    public List<StockItem> getAll(String market, IclijConfig conf) {
        return List.of();
    }

}
