package roart.predictor.impl;

import java.util.Map;

import roart.category.AbstractCategory;
import roart.common.config.MyMyConfig;
import roart.common.ml.NeuralNetCommand;
import roart.common.pipeline.PipelineConstants;
import roart.model.data.MarketData;
import roart.pipeline.Pipeline;

public class PredictorPytorchLSTM extends PytorchPredictor {

    public PredictorPytorchLSTM(MyMyConfig conf, String string, Map<String, MarketData> marketdatamap, String title, int category, AbstractCategory[] categories, Pipeline[] datareaders, NeuralNetCommand neuralnetcommand) throws Exception {
        super(conf, string, category, neuralnetcommand, marketdatamap, categories, datareaders);
    }

    @Override
    public boolean isEnabled() {
        return conf.wantPredictorPytorchLSTM();
    }

    @Override
    public String predictorName() {
        return PipelineConstants.LSTM;
    }

    @Override
    protected String getNeuralNetConfig() {
        return conf.getPredictorPytorchLSTMConfig();
    }
    
}

