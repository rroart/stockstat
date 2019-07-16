package roart.ml.common;

import roart.common.config.MyMyConfig;
import roart.pipeline.common.aggregate.Aggregator;

public class MLClassifyRandomModel extends MLClassifyModel {
    public MLClassifyRandomModel(MyMyConfig conf) {
        super(conf);
    }

    @Override
    public String getEngineName() {
        return "Random";
    }
    
    @Override
    public int getSizes(Aggregator indicator) { 
        return super.getSizes(indicator);
    }

    @Override
    public int getReturnSize() {
        return 1;
    }

    @Override
    public int getId() {
        return 0;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getKey() {
        return "Random";
    }
    
}
