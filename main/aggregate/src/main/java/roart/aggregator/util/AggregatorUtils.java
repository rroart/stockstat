package roart.aggregator.util;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.aggregator.impl.MACDBase;
import roart.aggregator.impl.MLATR;
import roart.aggregator.impl.MLCCI;
import roart.aggregator.impl.MLIndicator;
import roart.aggregator.impl.MLMACD;
import roart.aggregator.impl.MLMulti;
import roart.aggregator.impl.MLRSI;
import roart.aggregator.impl.MLSTOCH;
import roart.common.config.ConfigConstants;
import roart.common.ml.NeuralNetCommand;
import roart.common.pipeline.data.PipelineData;
import roart.iclij.config.IclijConfig;
import roart.pipeline.common.aggregate.Aggregator;

public class AggregatorUtils {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    public Aggregator[] getAggregates(IclijConfig conf, PipelineData[] pipelineData,
            List<String> disableList,
            Map<String, String> idNameMap,
            String catName, Integer cat, NeuralNetCommand neuralnetcommand, List<String> stockDates) throws Exception {
        Aggregator[] aggregates = new Aggregator[10];
        aggregates[0] = new MACDBase(conf, catName, catName, cat, pipelineData, stockDates);
        //aggregates[1] = new AggregatorRecommenderIndicator(conf, catName, marketdatamap, categories, pipelineData, disableList);
        //aggregates[2] = new RecommenderRSI(conf, catName, marketdatamap, categories);
        aggregates[3] = new MLMACD(conf, catName, catName, cat, idNameMap, pipelineData, neuralnetcommand, stockDates);
        aggregates[4] = new MLRSI(conf, catName, catName, cat, idNameMap, pipelineData, neuralnetcommand, stockDates);
        aggregates[5] = new MLATR(conf, catName, catName, cat, idNameMap, pipelineData, neuralnetcommand, stockDates);
        aggregates[6] = new MLCCI(conf, catName, catName, cat, idNameMap, pipelineData, neuralnetcommand, stockDates);
        aggregates[7] = new MLSTOCH(conf, catName, catName, cat, idNameMap, pipelineData, neuralnetcommand, stockDates);
        aggregates[8] = new MLMulti(conf, catName, catName, cat, idNameMap, pipelineData, neuralnetcommand, stockDates);
        aggregates[9] = new MLIndicator(conf, catName, catName, cat, pipelineData, neuralnetcommand, stockDates);
        log.info("Aggregate {}", conf.getConfigData().getConfigValueMap().get(ConfigConstants.MACHINELEARNING));
        log.info("Aggregate {}", conf.getConfigData().getConfigValueMap().get(ConfigConstants.AGGREGATORSMLMACD));
        log.info("Aggregate {}", conf.getConfigData().getConfigValueMap().get(ConfigConstants.INDICATORSMACD));
        log.info("Aggregate {}", conf.getConfigData().getConfigValueMap().get(ConfigConstants.INDICATORSRSI));
        log.info("Aggregate {}", conf.getConfigData().getConfigValueMap().get(ConfigConstants.INDICATORS));
        return aggregates;
    }

}
