package roart.predictor.impl;

import java.util.Map;

import roart.category.AbstractCategory;
import roart.common.config.MLConstants;
import roart.common.config.MyMyConfig;
import roart.common.ml.NeuralNetCommand;
import roart.model.data.MarketData;
import roart.model.data.PeriodData;
import roart.pipeline.Pipeline;

public abstract class TensorflowPredictor extends Predictor {

    public TensorflowPredictor(MyMyConfig conf, String string, int category, NeuralNetCommand neuralnetcommand, Map<String, MarketData> marketdatamap, Map<String, PeriodData> periodDataMap, AbstractCategory[] categories, Pipeline[] datareaders) {
        super(conf, string, category, neuralnetcommand, marketdatamap, periodDataMap, categories, datareaders);
    }
    
    @Override
    public String getType() {
        return MLConstants.TENSORFLOW;
    }
}
