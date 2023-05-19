package roart.evolution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import roart.iclij.config.IclijConfig;
import roart.ml.common.MLClassifyModel;
import roart.ml.dao.MLClassifyDao;
import roart.pipeline.common.aggregate.Aggregator;

public class MLNN {
    private IclijConfig conf;
    
    public MLNN(IclijConfig conf) {
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
