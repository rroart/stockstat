package roart.predictor.impl;

import roart.iclij.config.IclijConfig;
import roart.common.inmemory.model.Inmemory;
import roart.common.ml.NeuralNetCommand;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.data.PipelineData;
import roart.pipeline.Pipeline;

public class PredictorTensorflowGRU extends TensorflowPredictor {

    public PredictorTensorflowGRU(IclijConfig conf, String string, String title, int category, PipelineData[] datareaders, NeuralNetCommand neuralnetcommand, Inmemory inmemory) throws Exception {
        super(conf, string, title, category, neuralnetcommand, datareaders, inmemory);
    }

    @Override
    public boolean isEnabled() {
        return conf.wantPredictorTensorflowGRU();
    }

    @Override
    public String predictorName() {
        return PipelineConstants.GRU;
    }

    @Override
    protected String getNeuralNetConfig() {
        return conf.getPredictorTensorflowGRUConfig();
    }
    
}

