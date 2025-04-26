package roart.controller;

import java.util.List;

import roart.common.model.MetaDTO;
import roart.common.model.MyDataSource;
import roart.common.model.StockDTO;
import roart.iclij.config.IclijConfig;

public class DummyDataSource extends MyDataSource {

    @Override
    public List<MetaDTO> getMetas() {
        return List.of();
    }

    @Override
    public List<StockDTO> getAll(String market, IclijConfig conf, boolean disableCache) {
        return List.of();
    }

}
