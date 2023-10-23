package roart.predictor.impl;

import roart.iclij.config.IclijConfig;
import roart.common.ml.NeuralNetCommand;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.data.PipelineData;
import roart.pipeline.Pipeline;

public class PredictorTensorflowLIR extends TensorflowPredictor {

    public PredictorTensorflowLIR(IclijConfig conf, String string, String title, int category, PipelineData[] datareaders, NeuralNetCommand neuralnetcommand) throws Exception {
        super(conf, string, category, neuralnetcommand, datareaders);
    }

    @Override
    public boolean isEnabled() {
        return conf.wantPredictorTensorflowLIR();
    }

    @Override
    public String predictorName() {
        return PipelineConstants.LIR;
    }

    @Override
    protected String getNeuralNetConfig() {
        return conf.getPredictorTensorflowLIRConfig();
    }
    
}

