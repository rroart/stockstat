package roart.predictor.impl;

import java.util.Map;

import roart.category.AbstractCategory;
import roart.iclij.config.IclijConfig;
import roart.common.ml.NeuralNetCommand;
import roart.common.pipeline.PipelineConstants;
import roart.model.data.MarketData;
import roart.pipeline.Pipeline;

public class PredictorPytorchGRU extends PytorchPredictor {

    public PredictorPytorchGRU(IclijConfig conf, String string, Map<String, MarketData> marketdatamap, String title, int category, AbstractCategory[] categories, Pipeline[] datareaders, NeuralNetCommand neuralnetcommand) throws Exception {
        super(conf, string, category, neuralnetcommand, marketdatamap, categories, datareaders);
    }

    @Override
    public boolean isEnabled() {
        return conf.wantPredictorPytorchGRU();
    }

    @Override
    public String predictorName() {
        return PipelineConstants.GRU;
    }

    @Override
    protected String getNeuralNetConfig() {
        return conf.getPredictorPytorchGRUConfig();
    }
    
}

