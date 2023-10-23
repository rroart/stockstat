package roart.predictor.impl;

import roart.iclij.config.IclijConfig;
import roart.common.ml.NeuralNetCommand;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.data.PipelineData;
import roart.pipeline.Pipeline;

public class PredictorPytorchMLP extends PytorchPredictor {

    public PredictorPytorchMLP(IclijConfig conf, String string, String title, int category, PipelineData[] datareaders, NeuralNetCommand neuralnetcommand) throws Exception {
        super(conf, string, category, neuralnetcommand, datareaders);
    }

    @Override
    public boolean isEnabled() {
        return conf.wantPredictorPytorchMLP();
    }

    @Override
    public String predictorName() {
        return PipelineConstants.MLP;
    }

    @Override
    protected String getNeuralNetConfig() {
        return conf.getPredictorPytorchMLPConfig();
    }
    
}

