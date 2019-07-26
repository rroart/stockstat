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
import roart.component.model.MLRSIData;
import roart.common.pipeline.PipelineConstants;
import roart.config.IclijXMLConfig;
import roart.config.Market;
import roart.evolution.algorithm.impl.OrdinaryEvolution;
import roart.evolution.chromosome.impl.ConfigMapChromosome;
import roart.evolution.chromosome.impl.MLCCIChromosome;
import roart.evolution.chromosome.impl.MLMultiChromosome;
import roart.evolution.chromosome.impl.MLRSIChromosome;
import roart.evolution.config.EvolutionConfig;
import roart.evolution.species.Individual;
import roart.gene.AbstractGene;
import roart.iclij.model.IncDecItem;
import roart.iclij.model.MemoryItem;
import roart.result.model.ResultMeta;
import roart.service.ControlService;
import roart.service.MLService;
import roart.service.model.ProfitData;
import roart.util.ServiceUtil;
import roart.util.ServiceUtilConstants;

public class ComponentMLRSI extends ComponentMLAggregator {
    private Logger log = LoggerFactory.getLogger(ComponentMLRSI.class);
    @Override
    public void enable(Map<String, Object> valueMap) {
        valueMap.put(ConfigConstants.AGGREGATORS, Boolean.TRUE);        
        valueMap.put(ConfigConstants.AGGREGATORSMLRSI, Boolean.TRUE);        
        valueMap.put(ConfigConstants.MACHINELEARNING, Boolean.TRUE);        
        valueMap.put(ConfigConstants.INDICATORSRSI, Boolean.TRUE);                
        valueMap.put(ConfigConstants.INDICATORSSTOCHRSI, Boolean.TRUE);                
    }

    @Override
    public void disable(Map<String, Object> valueMap) {
        valueMap.put(ConfigConstants.AGGREGATORSMLRSI, Boolean.FALSE);        
        valueMap.put(ConfigConstants.MACHINELEARNING, Boolean.FALSE);        
        valueMap.put(ConfigConstants.INDICATORSRSI, Boolean.FALSE);                
        valueMap.put(ConfigConstants.INDICATORSSTOCHRSI, Boolean.FALSE);                
    }

    @Override
    protected int getDaysAfterLimit(ComponentData componentparam) {
        return componentparam.getService().conf.getMLRSIDaysAfterLimit();
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
        nns.add(IclijConfigConstants.EVOLVEMLMCP);
        nns.add(IclijConfigConstants.EVOLVEMLOVR);
         */
        return nns;
    }

    @Override
    protected ConfigMapChromosome getNewChromosome(Market market, ProfitData profitdata, List<Integer> positions,
            Boolean buy, ComponentData param, List<String> confList) {
        return new MLMultiChromosome(param, profitdata, confList, market, positions, getPipeline(), buy);
    }

    @Override
    protected List<String> getConfList() {
        List<String> confList = new ArrayList<>();
        confList.add(ConfigConstants.AGGREGATORSMLRSIDAYSAFTERLIMIT);
        confList.add(ConfigConstants.AGGREGATORSMLRSIDAYSBEFORELIMIT);
        confList.add(ConfigConstants.AGGREGATORSMLRSIBUYRSILIMIT);
        confList.add(ConfigConstants.AGGREGATORSMLRSIBUYSRSILIMIT);
        confList.add(ConfigConstants.AGGREGATORSMLRSISELLRSILIMIT);
        confList.add(ConfigConstants.AGGREGATORSMLRSISELLSRSILIMIT);
        return confList;
    }

    public EvolutionConfig getLocalEvolutionConfig(ComponentData componentdata) {
        String localEvolve = componentdata.getInput().getConfig().getFindProfitMLRSIEvolutionConfig();
        return JsonUtil.convert(localEvolve, EvolutionConfig.class);
    }

    @Override
    public String getLocalMLConfig(ComponentData componentdata) {
        return componentdata.getInput().getConfig().getFindProfitMLRSIMLConfig();
    }

    @Override
    public MLConfigs getOverrideMLConfig(ComponentData componentdata) {
        return ComponentMLIndicator.getDisableLSTM();
    }

    @Override
    public String getPipeline() {
        return PipelineConstants.MLRSI;
    }

}

