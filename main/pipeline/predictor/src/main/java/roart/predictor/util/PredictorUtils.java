package roart.predictor.util;

import java.util.ArrayList;
import java.util.List;

import roart.common.ml.NeuralNetCommand;
import roart.common.pipeline.data.PipelineData;
import roart.iclij.config.IclijConfig;
import roart.pipeline.common.predictor.AbstractPredictor;
import roart.predictor.impl.PredictorPytorchGRU;
import roart.predictor.impl.PredictorPytorchLSTM;
import roart.predictor.impl.PredictorPytorchMLP;
import roart.predictor.impl.PredictorPytorchRNN;
import roart.predictor.impl.PredictorTensorflowGRU;
import roart.predictor.impl.PredictorTensorflowLIR;
import roart.predictor.impl.PredictorTensorflowLSTM;
import roart.predictor.impl.PredictorTensorflowMLP;
import roart.predictor.impl.PredictorTensorflowRNN;

public class PredictorUtils {

    public AbstractPredictor[] getPredictors(IclijConfig conf, PipelineData[] datareaders,
            String catName,
            Integer cat, NeuralNetCommand neuralnetcommand) throws Exception {
        AbstractPredictor[] predictors = new AbstractPredictor[9];
        //predictors[0] = new PredictorLSTM(conf, Constants.INDEX, stocks, marketdatamap, periodDataMap, datareaders, categories);
        //predictors[1] = new PredictorLSTM(conf, Constants.PRICE, stocks, marketdatamap, periodDataMap, datareaders, categories);
        //AbstractPredictor predictor = new PredictorGRU(conf, categories[i].getTitle() + " LSTM", marketdatamap, periodDataMap, categories[i].getTitle(), categories[i].getPeriod(), categories, datareaders);
        List<AbstractPredictor> allpredictors = new ArrayList<>();
        allpredictors.add(new PredictorTensorflowLIR(conf, catName + " LIR", catName, cat, datareaders, neuralnetcommand));
        allpredictors.add(new PredictorTensorflowMLP(conf, catName + " MLP", catName, cat, datareaders, neuralnetcommand));
        allpredictors.add(new PredictorTensorflowRNN(conf, catName + " RNN", catName, cat, datareaders, neuralnetcommand));
        allpredictors.add(new PredictorTensorflowLSTM(conf, catName + " LSTM", catName, cat, datareaders, neuralnetcommand));
        allpredictors.add(new PredictorTensorflowGRU(conf, catName + " GRU", catName, cat, datareaders, neuralnetcommand));
        allpredictors.add(new PredictorPytorchMLP(conf, catName + " MLP", catName, cat, datareaders, neuralnetcommand));
        allpredictors.add(new PredictorPytorchRNN(conf, catName + " RNN", catName, cat, datareaders, neuralnetcommand));
        allpredictors.add(new PredictorPytorchLSTM(conf, catName + " LSTM", catName, cat, datareaders, neuralnetcommand));
        allpredictors.add(new PredictorPytorchGRU(conf, catName + " GRU", catName, cat, datareaders, neuralnetcommand));
        int i = 0;
        for (AbstractPredictor predictor : allpredictors) {
            if (predictor.isEnabled()) {
                if (predictor.hasValue()) {
                    predictors[i++] = predictor;
                }
            }
        }
        return predictors;
    }

    public void calculatePredictors(AbstractPredictor[] predictors) throws Exception {
        for (AbstractPredictor predictor : predictors) {
            if (predictor != null) {
                predictor.calculate();
            }
        }
    }
}
