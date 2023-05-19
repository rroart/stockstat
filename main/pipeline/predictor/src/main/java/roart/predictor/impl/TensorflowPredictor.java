package roart.predictor.impl;

import java.util.Map;

import roart.category.AbstractCategory;
import roart.common.config.MLConstants;
import roart.iclij.config.IclijConfig;
import roart.common.ml.NeuralNetCommand;
import roart.model.data.MarketData;
import roart.pipeline.Pipeline;

public abstract class TensorflowPredictor extends Predictor {

    public TensorflowPredictor(IclijConfig conf, String string, int category, NeuralNetCommand neuralnetcommand, Map<String, MarketData> marketdatamap, AbstractCategory[] categories, Pipeline[] datareaders) {
        super(conf, string, category, neuralnetcommand, marketdatamap, categories, datareaders);
    }
    
    @Override
    public String getType() {
        return MLConstants.TENSORFLOW;
    }
}
