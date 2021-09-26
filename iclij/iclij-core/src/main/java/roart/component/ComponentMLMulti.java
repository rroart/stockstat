package roart.component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.action.MarketAction;
import roart.common.config.ConfigConstants;
import roart.common.config.MyMyConfig;
import roart.common.constants.Constants;
import roart.common.pipeline.PipelineConstants;
import roart.component.model.ComponentData;
import roart.iclij.evolution.chromosome.impl.ConfigMapChromosome2;
import roart.iclij.evolution.chromosome.impl.MLMultiChromosome;
import roart.iclij.evolution.chromosome.winner.ConfigMapChromosomeWinner;
import roart.gene.impl.ConfigMapGene;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.MLConfigs;
import roart.iclij.config.Market;
import roart.iclij.evolution.fitness.impl.FitnessConfigMap;
import roart.iclij.filter.Memories;
import roart.iclij.model.MLMetricsItem;
import roart.iclij.model.Parameters;
import roart.service.model.ProfitData;

public class ComponentMLMulti extends ComponentMLAggregator {
    private Logger log = LoggerFactory.getLogger(ComponentMLMulti.class);
    @Override
    public void enable(Map<String, Object> valueMap) {
        valueMap.put(ConfigConstants.AGGREGATORS, Boolean.TRUE);        
        valueMap.put(ConfigConstants.AGGREGATORSMLMULTI, Boolean.TRUE);        
        valueMap.put(ConfigConstants.MACHINELEARNING, Boolean.TRUE);        
        valueMap.put(ConfigConstants.INDICATORS, Boolean.TRUE);                
        valueMap.put(ConfigConstants.INDICATORSATR, Boolean.TRUE);                
        valueMap.put(ConfigConstants.INDICATORSCCI, Boolean.TRUE);                
        valueMap.put(ConfigConstants.INDICATORSMACD, Boolean.TRUE);                
        valueMap.put(ConfigConstants.INDICATORSRSI, Boolean.TRUE);                
        valueMap.put(ConfigConstants.INDICATORSSTOCH, Boolean.TRUE);                
        valueMap.put(ConfigConstants.INDICATORSSTOCHRSI, Boolean.TRUE);                
    }

    @Override
    public void disable(Map<String, Object> valueMap) {
        valueMap.put(ConfigConstants.AGGREGATORSMLMULTI, Boolean.FALSE);        
        valueMap.put(ConfigConstants.MACHINELEARNING, Boolean.FALSE);        
        valueMap.put(ConfigConstants.INDICATORS, Boolean.FALSE);                
        valueMap.put(ConfigConstants.INDICATORSATR, Boolean.FALSE);                
        valueMap.put(ConfigConstants.INDICATORSCCI, Boolean.FALSE);                
        valueMap.put(ConfigConstants.INDICATORSMACD, Boolean.FALSE);                
        valueMap.put(ConfigConstants.INDICATORSRSI, Boolean.FALSE);                
        valueMap.put(ConfigConstants.INDICATORSSTOCH, Boolean.FALSE);                
        valueMap.put(ConfigConstants.INDICATORSSTOCHRSI, Boolean.FALSE);                
    }

    @Override
    protected int getDaysAfterLimit(ComponentData componentparam) {
        return componentparam.getService().conf.getMLMultiDaysAfterLimit();
    }
    
    @Deprecated
    static void setnns(MyMyConfig conf, IclijConfig config, List<String> nns) {
        Map<String, String> map = config.getConv();
        for (String key : nns) {
            System.out.println(conf.getConfigValueMap().keySet());
            Object o = config.getValueOrDefault(key);
            boolean enable = (boolean) config.getValueOrDefault(key);
            String otherKey = map.get(key);
            conf.getConfigValueMap().put(otherKey, enable);
        }
    }

    @Deprecated
    static List<String> getnns() {
        List<String> nns = new ArrayList<>();
        /*
        nns.add(IclijConfigConstants.EVOLVEMLDNN);
        nns.add(IclijConfigConstants.EVOLVEMLDNNL);
        nns.add(IclijConfigConstants.EVOLVEMLL);
        nns.add(IclijConfigConstants.EVOLVEMLLR);
        nns.add(IclijConfigConstants.EVOLVEMLMLPC);
        nns.add(IclijConfigConstants.EVOLVEMLOVR);
         */
        return nns;
    }

    @Deprecated
    private boolean anythingHere(Map<String, List<List<Double>>> listMap2, int size) {
        for (List<List<Double>> array : listMap2.values()) {
            if (size == Constants.OHLC && size != array.get(0).size()) {
                return false;
            }
            for (int i = 0; i < array.get(0).size(); i++) {
                if (array.get(0).get(i) != null) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public ComponentData improve(MarketAction action, ComponentData componentparam, Market market, ProfitData profitdata, Memories positions, Boolean buy, String subcomponent, Parameters parameters, boolean wantThree, List<MLMetricsItem> mlTests) {
        ComponentData param = new ComponentData(componentparam);
        List<String> confList = getConfList();
        Map<String, List<List<Double>>> listMap = param.getCategoryValueMap();
        if (wantThree) {
            confList.addAll(getThreeConfList());
        }
        ConfigMapGene gene = new ConfigMapGene(confList, param.getService().conf);
        ConfigMapChromosome2 chromosome = new MLMultiChromosome(gene);
        loadme(param, chromosome, market, confList, buy, subcomponent, action, parameters);
        List<String> stockDates = param.getService().getDates(market.getConfig().getMarket());
        FitnessConfigMap fit = new FitnessConfigMap(action, param, profitdata, market, null, getPipeline(), buy, subcomponent, parameters, gene, stockDates);
        return improve(action, param, chromosome, subcomponent, new ConfigMapChromosomeWinner(), buy, fit);
    }

    @Override
    protected ConfigMapChromosome2 getNewChromosome(MarketAction action, Market market, ProfitData profitdata,
            Memories positions, Boolean buy, ComponentData param, String subcomponent, Parameters parameters, ConfigMapGene gene, List<MLMetricsItem> mlTests) {
        return new MLMultiChromosome(gene);
    }

    @Override
    protected List<String> getConfList() {
        List<String> list = new ArrayList<>();
        list.add(ConfigConstants.AGGREGATORSMLMULTIMACD);
        list.add(ConfigConstants.AGGREGATORSMLMULTIRSI);
        list.add(ConfigConstants.AGGREGATORSMLMULTISTOCHRSI);
        //list.add(ConfigConstants.AGGREGATORSMLMACDDAYSBEFOREZERO);
        //list.add(ConfigConstants.AGGREGATORSMLMACDDAYSAFTERZERO);
        //list.add(ConfigConstants.AGGREGATORSMLRSIDAYSBEFORELIMIT);
        //list.add(ConfigConstants.AGGREGATORSMLRSIDAYSAFTERLIMIT);
        list.add(ConfigConstants.AGGREGATORSMLMULTIDAYSBEFORELIMIT);
        list.add(ConfigConstants.AGGREGATORSMLMULTIDAYSAFTERLIMIT);
        //list.add(ConfigConstants.AGGREGATORSMLMULTITHRESHOLD);
        return list;
    }

    protected List<String> getThreeConfList() {
        List<String> list = new ArrayList<>();
        list.add(ConfigConstants.AGGREGATORSMLMULTIATR);
        list.add(ConfigConstants.AGGREGATORSMLMULTICCI);
        list.add(ConfigConstants.AGGREGATORSMLMULTISTOCH);
        //list.add(ConfigConstants.AGGREGATORSMLATRDAYSBEFORELIMIT);
        //list.add(ConfigConstants.AGGREGATORSMLATRDAYSAFTERLIMIT);
        //list.add(ConfigConstants.AGGREGATORSMLCCIDAYSBEFORELIMIT);
        //list.add(ConfigConstants.AGGREGATORSMLCCIDAYSAFTERLIMIT);
        //list.add(ConfigConstants.AGGREGATORSMLSTOCHDAYSBEFORELIMIT);
        //list.add(ConfigConstants.AGGREGATORSMLSTOCHDAYSAFTERLIMIT);
        return list;
    }

    @Override
    public MLConfigs getOverrideMLConfig(ComponentData componentdata) {
        return ComponentMLIndicator.getDisableLSTM();
    }

    @Override
    public String getPipeline() {
        return PipelineConstants.MLMULTI;
    }

    @Override
    public String getThreshold() {
        return ConfigConstants.AGGREGATORSMLMULTITHRESHOLD;
    }
    
    @Override
    public String getFuturedays() {
        return ConfigConstants.AGGREGATORSMLMULTIDAYSAFTERLIMIT;
    }
    
}

