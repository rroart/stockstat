package roart.predictor.impl;

import java.util.Map;

import roart.category.AbstractCategory;
import roart.common.config.MyMyConfig;
import roart.common.pipeline.PipelineConstants;
import roart.model.data.MarketData;
import roart.model.data.PeriodData;
import roart.pipeline.Pipeline;

public class PredictorPytorchMLP extends TensorflowPredictor {

    public PredictorPytorchMLP(MyMyConfig conf, String string, Map<String, MarketData> marketdatamap, Map<String, PeriodData> periodDataMap, String title, int category, AbstractCategory[] categories, Pipeline[] datareaders) throws Exception {
        super(conf, string, category);
    }

    @Override
    public boolean isEnabled() {
        return conf.wantPredictorPytorchMLP();
    }

    @Override
    public String predictorName() {
        return PipelineConstants.MLP;
    }

}

