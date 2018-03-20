package roart.ml;

import java.util.Map;
import java.util.concurrent.Callable;

import org.jfree.util.Log;

import roart.aggregate.Aggregator;
import roart.model.LearnTestClassifyResult;

public class MLClassifyLearnTestPredictCallable implements Callable {
    private Aggregator indicator;
    
    private Map<double[], Double> map;
    
    private MLClassifyModel model;
    
    private int size;
    
    private String period;
    
    private String mapname;
    
    private int outcomes;
    
    private Map<MLClassifyModel, Long> mapTime;
    
    private Map<String, double[]> map2;
    
    private  Map<Double, String> shortMap;

    private MLClassifyDao mldao;
    
    public MLClassifyLearnTestPredictCallable(MLClassifyDao mldao, Aggregator indicator, Map<double[], Double> map, MLClassifyModel model,
            int size, String period, String mapname, int outcomes, Map<MLClassifyModel, Long> mapTime,
            Map<String, double[]> map2, Map<Double, String> shortMap) {
        super();
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
        Log.info("call1");
        return mldao.learntestclassify(indicator, map, model, size, period, mapname, 4, mapTime, map2, shortMap);  
    }
    
}