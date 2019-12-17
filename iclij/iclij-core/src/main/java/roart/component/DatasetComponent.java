package roart.component;

import java.util.List;
import java.util.Map;

import roart.action.MarketAction;
import roart.common.config.ConfigConstants;
import roart.common.pipeline.PipelineConstants;
import roart.component.model.ComponentData;
import roart.component.model.ComponentMLData;
import roart.component.model.DatasetData;
import roart.config.Market;
import roart.evolution.config.EvolutionConfig;
import roart.iclij.config.IclijConfigConstants;
import roart.iclij.config.MLConfigs;
import roart.iclij.model.MemoryItem;
import roart.iclij.model.Parameters;
import roart.iclij.config.IclijConfig;
import roart.service.model.ProfitData;

public class DatasetComponent extends ComponentML {

    @Override
    public void enable(Map<String, Object> valueMap) {
        valueMap.put(ConfigConstants.MACHINELEARNING, Boolean.TRUE);        
    }

    @Override
    public void disable(Map<String, Object> valueMap) {
        valueMap.put(ConfigConstants.MACHINELEARNING, Boolean.FALSE);        
    }

    @Override
    public ComponentData handle(MarketAction action, Market market, ComponentData componentparam, ProfitData profitdata,
            List<Integer> positions, boolean evolve, Map<String, Object> aMap, String subcomponent, String mlmarket, Parameters parameters) {
        DatasetData param = new DatasetData(componentparam);

        int futuredays = (int) param.getService().conf.getAggregatorsIndicatorFuturedays();
        param.setFuturedays(futuredays);

        handle2(action, market, param, profitdata, positions, evolve, aMap, subcomponent, mlmarket, parameters);
        //Map resultMaps = param.getResultMap();
        //handleMLMeta(param, resultMaps);
        //Map<String, Object> resultMap = param.getResultMap();
        return param;
    }

    @Override
    public ComponentData improve(MarketAction action, ComponentData param, Market market, ProfitData profitdata,
            List<Integer> positions, Boolean buy, String subcomponent, Parameters parameters) {
        return null;
    }

    @Override
    public EvolutionConfig getLocalEvolutionConfig(ComponentData componentdata) {
        return null;
    }

    @Override
    public String getLocalMLConfig(ComponentData componentdata) {
        return null;
    }

    @Override
    public MLConfigs getOverrideMLConfig(ComponentData componentdata) {
        return null;
    }

    @Override
    public void calculateIncDec(ComponentData param, ProfitData profitdata, List<Integer> positions) {
    }

    @Override
    public List<MemoryItem> calculateMemory(ComponentData param, Parameters parameters) throws Exception {
        return null;
    }

    @Override
    public String getPipeline() {
        return PipelineConstants.DATASET;
    }

    @Override
    protected List<String> getConfList() {
        return null;
    }

    @Override
    public int getPriority(IclijConfig srv) {
        return 0;
    }

    @Override
    public String getThreshold() {
        return null;
    }

    @Override
    public String getFuturedays() {
        return null;
    }

}
