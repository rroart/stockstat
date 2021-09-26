package roart.component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.action.MarketAction;
import roart.common.config.ConfigConstants;
import roart.common.config.MyMyConfig;
import roart.common.pipeline.PipelineConstants;
import roart.component.model.ComponentData;
import roart.iclij.evolution.chromosome.impl.ConfigMapChromosome2;
import roart.iclij.evolution.chromosome.impl.MLCCIChromosome;
import roart.gene.impl.ConfigMapGene;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.MLConfigs;
import roart.iclij.config.Market;
import roart.iclij.filter.Memories;
import roart.iclij.model.MLMetricsItem;
import roart.iclij.model.Parameters;
import roart.service.model.ProfitData;

public class ComponentMLCCI extends ComponentMLAggregator {
    private Logger log = LoggerFactory.getLogger(ComponentMLCCI.class);
    @Override
    public void enable(Map<String, Object> valueMap) {
        valueMap.put(ConfigConstants.AGGREGATORS, Boolean.TRUE);        
        valueMap.put(ConfigConstants.AGGREGATORSMLCCI, Boolean.TRUE);        
        valueMap.put(ConfigConstants.MACHINELEARNING, Boolean.TRUE);        
        valueMap.put(ConfigConstants.INDICATORS, Boolean.TRUE);                
        valueMap.put(ConfigConstants.INDICATORSCCI, Boolean.TRUE);                
    }

    @Override
    public void disable(Map<String, Object> valueMap) {
        valueMap.put(ConfigConstants.AGGREGATORSMLCCI, Boolean.FALSE);        
        valueMap.put(ConfigConstants.MACHINELEARNING, Boolean.FALSE);        
        valueMap.put(ConfigConstants.INDICATORS, Boolean.FALSE);                
        valueMap.put(ConfigConstants.INDICATORSCCI, Boolean.FALSE);                
    }

    @Override
    protected int getDaysAfterLimit(ComponentData componentparam) {
        return componentparam.getService().conf.getMLCCIDaysAfterLimit();
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

    @Override
    protected ConfigMapChromosome2 getNewChromosome(MarketAction action, Market market, ProfitData profitdata,
            Memories positions, Boolean buy, ComponentData param, String subcomponent, Parameters parameters, ConfigMapGene gene, List<MLMetricsItem> mlTests) {
        return new MLCCIChromosome(gene);
    }

    @Override
    protected List<String> getConfList() {
        List<String> confList = new ArrayList<>();
        confList.add(ConfigConstants.AGGREGATORSMLCCIDAYSAFTERLIMIT);
        confList.add(ConfigConstants.AGGREGATORSMLCCIDAYSBEFORELIMIT);
        confList.add(ConfigConstants.AGGREGATORSMLCCIBUYLIMIT);
        confList.add(ConfigConstants.AGGREGATORSMLCCISELLLIMIT);
        //confList.add(ConfigConstants.AGGREGATORSMLCCITHRESHOLD);
        return confList;
    }

    @Override
    public MLConfigs getOverrideMLConfig(ComponentData componentdata) {
        return ComponentMLIndicator.getDisableLSTM();
    }

    @Override
    public String getPipeline() {
        return PipelineConstants.MLCCI;
    }

    @Override
    public String getThreshold() {
        return ConfigConstants.AGGREGATORSMLCCITHRESHOLD;
    }
    
    @Override
    public String getFuturedays() {
        return ConfigConstants.AGGREGATORSMLCCIDAYSAFTERLIMIT;
    }
    
}

