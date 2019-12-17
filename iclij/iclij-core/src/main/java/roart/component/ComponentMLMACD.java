package roart.component;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import roart.action.FindProfitAction;
import roart.action.MarketAction;
import roart.common.config.ConfigConstants;
import roart.iclij.config.EvolveMLConfig;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijConfigConstants;
import roart.iclij.config.MLConfig;
import roart.iclij.config.MLConfigs;
import roart.common.config.MyMyConfig;
import roart.common.constants.Constants;
import roart.common.util.JsonUtil;
import roart.common.util.TimeUtil;
import roart.component.model.ComponentData;
import roart.component.model.ComponentInput;
import roart.component.model.MLIndicatorData;
import roart.component.model.MLMACDData;
import roart.common.pipeline.PipelineConstants;
import roart.config.IclijXMLConfig;
import roart.config.Market;
import roart.evolution.algorithm.impl.OrdinaryEvolution;
import roart.evolution.chromosome.impl.ConfigMapChromosome;
import roart.evolution.chromosome.impl.MLCCIChromosome;
import roart.evolution.chromosome.impl.MLMACDChromosome;
import roart.evolution.config.EvolutionConfig;
import roart.evolution.species.Individual;
import roart.gene.AbstractGene;
import roart.iclij.model.IncDecItem;
import roart.iclij.model.MemoryItem;
import roart.iclij.model.Parameters;
import roart.result.model.ResultMeta;
import roart.service.ControlService;
import roart.service.MLService;
import roart.service.model.ProfitData;
import roart.util.ServiceUtil;
import roart.util.ServiceUtilConstants;

public abstract class ComponentMLMACD extends ComponentMLAggregator {
    private Logger log = LoggerFactory.getLogger(ComponentMLMACD.class);
    @Override
    public void enable(Map<String, Object> valueMap) {
        valueMap.put(ConfigConstants.AGGREGATORS, Boolean.TRUE);        
        valueMap.put(ConfigConstants.AGGREGATORSMLMACD, Boolean.TRUE);        
        valueMap.put(ConfigConstants.MACHINELEARNING, Boolean.TRUE);        
        valueMap.put(ConfigConstants.INDICATORSMACD, Boolean.TRUE);                
    }

    @Override
    public void disable(Map<String, Object> valueMap) {
        valueMap.put(ConfigConstants.AGGREGATORSMLMACD, Boolean.FALSE);        
        valueMap.put(ConfigConstants.MACHINELEARNING, Boolean.FALSE);        
        valueMap.put(ConfigConstants.INDICATORSMACD, Boolean.FALSE);                
    }

    @Override
    protected int getDaysAfterLimit(ComponentData componentparam) {
        return componentparam.getService().conf.getMACDDaysAfterZero();
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
    protected ConfigMapChromosome getNewChromosome(MarketAction action, Market market, ProfitData profitdata,
            List<Integer> positions, Boolean buy, ComponentData param, List<String> confList, String subcomponent, Parameters parameters) {
        return new MLMACDChromosome(action, param, profitdata, confList, market, positions, getPipeline(), buy, subcomponent, parameters);
    }

    @Override
    protected List<String> getConfList() {
        List<String> confList = new ArrayList<>();
        confList.add(ConfigConstants.AGGREGATORSMLMACDDAYSAFTERZERO);
        confList.add(ConfigConstants.AGGREGATORSMLMACDDAYSBEFOREZERO);
        confList.add(ConfigConstants.AGGREGATORSMLMACDHISTOGRAMML);
        confList.add(ConfigConstants.AGGREGATORSMLMACDMACDML);
        confList.add(ConfigConstants.AGGREGATORSMLMACDSIGNALML);
        confList.add(ConfigConstants.AGGREGATORSMLMACDTHRESHOLD);
        return confList;
    }

    @Override
    public MLConfigs getOverrideMLConfig(ComponentData componentdata) {
        return ComponentMLIndicator.getDisableLSTM();
    }

    @Override
    public String getPipeline() {
        return PipelineConstants.MLMACD;
    }

    @Override
    public String getThreshold() {
        return ConfigConstants.AGGREGATORSMLMACDTHRESHOLD;
    }
    
    @Override
    public String getFuturedays() {
        return ConfigConstants.AGGREGATORSMLMACDDAYSAFTERZERO;
    }
    
}

