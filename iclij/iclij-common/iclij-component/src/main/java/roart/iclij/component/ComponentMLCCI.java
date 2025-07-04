package roart.iclij.component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.config.ConfigConstants;
import roart.iclij.config.ConfigUtils;
import roart.common.model.MLMetricsDTO;
import roart.common.pipeline.PipelineConstants;
import roart.component.model.ComponentData;
import roart.iclij.evolution.chromosome.impl.ConfigMapChromosome2;
import roart.iclij.evolution.chromosome.impl.MLCCIChromosome;
import roart.gene.impl.ConfigMapGene;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.MLConfigs;
import roart.iclij.config.Market;
import roart.iclij.filter.Memories;
import roart.iclij.model.Parameters;
import roart.iclij.model.action.MarketActionData;
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
        return componentparam.getService().coremlconf.getMLCCIDaysAfterLimit();
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
            Memories positions, Boolean buy, ComponentData param, String subcomponent, Parameters parameters, ConfigMapGene gene, List<MLMetricsDTO> mlTests) {
        return new MLCCIChromosome(gene);
    }

    @Override
    protected List<String> getConfList() {
        return new ConfigUtils().getComponentMLCCIConfigList();
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

