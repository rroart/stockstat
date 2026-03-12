package roart.predictor.impl;

import roart.iclij.config.IclijConfig;
import roart.common.inmemory.model.Inmemory;
import roart.common.ml.NeuralNetCommand;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.data.PipelineData;
import roart.common.pipeline.data.SerialPipeline;
import roart.pipeline.Pipeline;

public class PredictorPytorchLSTM extends PytorchPredictor {

    public PredictorPytorchLSTM(IclijConfig conf, String string, String title, int category, SerialPipeline datareaders, NeuralNetCommand neuralnetcommand, Inmemory inmemory) throws Exception {
        super(conf, string, title, category, neuralnetcommand, datareaders, inmemory);
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

