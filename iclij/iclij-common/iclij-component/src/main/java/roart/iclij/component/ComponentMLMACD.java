package roart.iclij.component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.config.ConfigConstants;
import roart.iclij.config.ConfigUtils;
import roart.iclij.config.IclijConfig;
import roart.common.model.MLMetricsItem;
import roart.common.pipeline.PipelineConstants;
import roart.component.model.ComponentData;
import roart.iclij.evolution.chromosome.impl.ConfigMapChromosome2;
import roart.iclij.evolution.chromosome.impl.MLMACDChromosome;
import roart.gene.impl.ConfigMapGene;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.MLConfigs;
import roart.iclij.config.Market;
import roart.iclij.filter.Memories;
import roart.iclij.model.Parameters;
import roart.iclij.model.action.MarketActionData;
import roart.service.model.ProfitData;

public class ComponentMLMACD extends ComponentMLAggregator {
    private Logger log = LoggerFactory.getLogger(ComponentMLMACD.class);
    @Override
    public void enable(Map<String, Object> valueMap) {
        valueMap.put(ConfigConstants.AGGREGATORS, Boolean.TRUE);        
        valueMap.put(ConfigConstants.AGGREGATORSMLMACD, Boolean.TRUE);        
        valueMap.put(ConfigConstants.MACHINELEARNING, Boolean.TRUE);        
        valueMap.put(ConfigConstants.INDICATORS, Boolean.TRUE);                
        valueMap.put(ConfigConstants.INDICATORSMACD, Boolean.TRUE);                
    }

    @Override
    public void disable(Map<String, Object> valueMap) {
        valueMap.put(ConfigConstants.AGGREGATORSMLMACD, Boolean.FALSE);        
        valueMap.put(ConfigConstants.MACHINELEARNING, Boolean.FALSE);        
        valueMap.put(ConfigConstants.INDICATORS, Boolean.FALSE);                
        valueMap.put(ConfigConstants.INDICATORSMACD, Boolean.FALSE);                
    }

    @Override
    protected int getDaysAfterLimit(ComponentData componentparam) {
        return componentparam.getService().coremlconf.getMACDDaysAfterZero();
    }
    
    @Deprecated
    static void setnns(IclijConfig conf, IclijConfig config, List<String> nns) {
        Map<String, String> map = config.getConfigData().getConfigMaps().conv;
        for (String key : nns) {
            System.out.println(conf.getConfigData().getConfigValueMap().keySet());
            Object o = config.getValueOrDefault(key);
            boolean enable = (boolean) config.getValueOrDefault(key);
            String otherKey = map.get(key);
            conf.getConfigData().getConfigValueMap().put(otherKey, enable);
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
    protected ConfigMapChromosome2 getNewChromosome(MarketActionData action, Market market, ProfitData profitdata,
            Memories positions, Boolean buy, ComponentData param, String subcomponent, Parameters parameters, ConfigMapGene gene, List<MLMetricsItem> mlTests) {
        return new MLMACDChromosome(gene);
    }

    @Override
    protected List<String> getConfList() {
        return new ConfigUtils().getComponentMLMACDConfigList();
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

