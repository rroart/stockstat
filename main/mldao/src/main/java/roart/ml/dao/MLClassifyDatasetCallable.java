package roart.ml.dao;

import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.ml.NeuralNetCommand;
import roart.common.ml.NeuralNetConfigs;
import roart.ml.common.MLClassifyModel;
import roart.ml.common.MLMeta;
import roart.ml.model.LearnTestClassifyResult;
import roart.pipeline.common.aggregate.Aggregator;

public class MLClassifyDatasetCallable implements Callable {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private NeuralNetConfigs nnconfigs;
    
    private Aggregator indicator;
    
    private Map<String, Pair<Object, Double>> learnmap;
    
    private MLClassifyModel model;
    
    private int size;
    
    private Map<MLClassifyModel, Long> mapTime;
    
    private Map<String, Pair<Object, Double>> classifymap;
    
    private  Map<Double, String> shortMap;

    private MLClassifyDao mldao;
    
    private String path;
    
    private String filename;
    
    private NeuralNetCommand neuralnetcommand;
    
    private MLMeta mlmeta;

    private String dataset;
    
    public MLClassifyDatasetCallable(NeuralNetConfigs nnconfigs, MLClassifyDao mldao, MLClassifyModel model,
            Map<MLClassifyModel, Long> mapTime,
            NeuralNetCommand neuralnetcommand, MLMeta mlmeta, String dataset) {
        super();
        this.nnconfigs = nnconfigs;
        this.mldao = mldao;
        this.model = model;
        this.mapTime = mapTime;
        this.neuralnetcommand = neuralnetcommand;
        this.mlmeta = mlmeta;
        this.dataset = dataset;
    }

    @Override
    public LearnTestClassifyResult call() throws Exception {
        log.info("call1");
        return mldao.dataset(nnconfigs, model, mapTime, neuralnetcommand, mlmeta, dataset);  
    }
    

}
