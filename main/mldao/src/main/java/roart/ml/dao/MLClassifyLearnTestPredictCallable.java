package roart.ml.dao;

import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.pipeline.common.aggregate.Aggregator;
import roart.common.ml.NeuralNetConfigs;
import roart.ml.common.MLClassifyModel;
import roart.ml.model.LearnTestClassifyResult;

public class MLClassifyLearnTestPredictCallable implements Callable {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private NeuralNetConfigs nnconfigs;
    
    private Aggregator indicator;
    
    private Map<String, Pair<double[], Double>> map;
    
    private MLClassifyModel model;
    
    private int size;
    
    private String period;
    
    private String mapname;
    
    private int outcomes;
    
    private Map<MLClassifyModel, Long> mapTime;
    
    private Map<String, Pair<double[], Double>> map2;
    
    private  Map<Double, String> shortMap;

    private MLClassifyDao mldao;
    
    public MLClassifyLearnTestPredictCallable(NeuralNetConfigs nnconfigs, MLClassifyDao mldao, Aggregator indicator, Map<String, Pair<double[], Double>> map, MLClassifyModel model,
            int size, String period, String mapname, int outcomes, Map<MLClassifyModel, Long> mapTime,
            Map<String, Pair<double[], Double>> map2, Map<Double, String> shortMap) {
        super();
        this.nnconfigs = nnconfigs;
        this.mldao = mldao;
        this.indicator = indicator;
        this.map = map;
        this.model = model;
        this.size = size;
        this.period = period;
        this.mapname = mapname;
        this.outcomes = outcomes;
        this.mapTime = mapTime;
        this.map2 = map2;
        this.shortMap = shortMap;
    }

    @Override
    public LearnTestClassifyResult call() throws Exception {
        log.info("call1");
        return mldao.learntestclassify(nnconfigs, indicator, map, model, size, period, mapname, 4, mapTime, map2, shortMap);  
    }
    
}