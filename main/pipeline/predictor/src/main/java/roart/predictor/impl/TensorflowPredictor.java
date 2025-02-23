package roart.predictor.impl;

import roart.common.config.MLConstants;
import roart.common.inmemory.model.Inmemory;
import roart.iclij.config.IclijConfig;
import roart.common.ml.NeuralNetCommand;
import roart.common.pipeline.data.PipelineData;
import roart.pipeline.Pipeline;

public abstract class TensorflowPredictor extends Predictor {

    public TensorflowPredictor(IclijConfig conf, String string, String title, int category, NeuralNetCommand neuralnetcommand, PipelineData[] datareaders, Inmemory inmemory) {
        super(conf, string, title, category, neuralnetcommand, datareaders, inmemory);
    }
    
    @Override
    public String getType() {
        return MLConstants.TENSORFLOW;
    }
}
