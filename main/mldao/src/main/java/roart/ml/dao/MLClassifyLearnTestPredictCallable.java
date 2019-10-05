package roart.ml.dao;

import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.pipeline.common.aggregate.Aggregator;
import roart.common.ml.NeuralNetCommand;
import roart.common.ml.NeuralNetConfigs;
import roart.ml.common.MLClassifyModel;
import roart.ml.common.MLMeta;
import roart.ml.model.LearnTestClassifyResult;

public class MLClassifyLearnTestPredictCallable implements Callable {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private NeuralNetConfigs nnconfigs;
    
    private Aggregator indicator;
    
    private Map<String, Pair<Object, Double>> learnmap;
    
    private MLClassifyModel model;
    
    private int size;
    
    private String period;
    
    private String mapname;
    
    private int outcomes;
    
    private Map<MLClassifyModel, Long> mapTime;
    
    private Map<String, Pair<Object, Double>> classifymap;
    
    private  Map<Double, String> shortMap;

    private MLClassifyDao mldao;
    
    private String path;
    
    private String filename;
    
    private NeuralNetCommand neuralnetcommand;
    
    private MLMeta mlmeta;
    
    public MLClassifyLearnTestPredictCallable(NeuralNetConfigs nnconfigs, MLClassifyDao mldao, Aggregator indicator, Map<String, Pair<Object, Double>> learnmap, MLClassifyModel model,
            int size, int outcomes, Map<MLClassifyModel, Long> mapTime, Map<String, Pair<Object, Double>> classifymap, Map<Double, String> shortMap,
            String path, String filename, NeuralNetCommand neuralnetcommand, MLMeta mlmeta) {
        super();
        this.nnconfigs = nnconfigs;
        this.mldao = mldao;
        this.indicator = indicator;
        this.learnmap = learnmap;
        this.model = model;
        this.size = size;
        this.outcomes = outcomes;
        this.mapTime = mapTime;
        this.classifymap = classifymap;
        this.shortMap = shortMap;
        this.path = path;
        this.filename = filename;
        this.neuralnetcommand = neuralnetcommand;
        this.mlmeta = mlmeta;
    }

    @Override
    public LearnTestClassifyResult call() throws Exception {
        log.info("call1");
        return mldao.learntestclassify(nnconfigs, indicator, learnmap, model, size, 4, mapTime, classifymap, shortMap, path, filename, neuralnetcommand, mlmeta);  
    }
    
}