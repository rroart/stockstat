package roart.predictor.impl;

import roart.common.config.MLConstants;
import roart.iclij.config.IclijConfig;
import roart.common.ml.NeuralNetCommand;
import roart.common.pipeline.data.PipelineData;
import roart.pipeline.Pipeline;

public abstract class PytorchPredictor extends Predictor {
    public PytorchPredictor(IclijConfig conf, String string, String title, int category, NeuralNetCommand neuralnetcommand, PipelineData[] datareaders) {
        super(conf, string, title, category, neuralnetcommand, datareaders);
    }
    
    @Override
    public String getType() {
        return MLConstants.PYTORCH;
    }

}
