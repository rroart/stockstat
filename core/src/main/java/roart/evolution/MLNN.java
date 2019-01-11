package roart.evolution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import roart.pipeline.common.aggregate.Aggregator;
import roart.aggregate.AggregatorRecommenderIndicator;
import roart.aggregate.MLIndicator;
import roart.aggregate.MLMACD;
import roart.aggregate.RecommenderRSI;
import roart.common.config.MyMyConfig;
import roart.common.constants.CategoryConstants;
import roart.common.constants.Constants;
import roart.ml.dao.MLClassifyDao;
import roart.ml.common.MLClassifyModel;

public class MLNN {
    private MyMyConfig conf;
    
    public MLNN(MyMyConfig conf) {
        this.conf = conf;
        Object stocks = null;
        Object periodDataMap = null;
        Aggregator[] aggregates = new Aggregator[4];
        //aggregates[2] = new MLMACD(conf, Constants.PRICE, stocks, periodDataMap, CategoryConstants.PRICE, 0, categories);
        //aggregates[3] = new MLIndicator(conf, Constants.PRICE, marketdatamap, periodDataMap, CategoryConstants.PRICE, 0, categories, datareaders);
        if (conf.wantML()) {
            if (conf.wantMLSpark()) {
                mldaos.add(new MLClassifyDao("spark", conf));
            }
            if (conf.wantMLTensorflow()) {
                mldaos.add(new MLClassifyDao("tensorflow", conf));
            }
        }
        for (MLClassifyDao mldao : mldaos) {
            // map from posnegcom to map<id, result>
            Map<String, Map<String, Double[]>> mapResult2 = new HashMap<>();
            for (MLClassifyModel model : mldao.getModels()) {

            }
        }
    }
    
    List<MLClassifyDao> mldaos = new ArrayList<>();

}
