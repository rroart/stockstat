package roart.component;

import java.util.List;
import java.util.Map;

import roart.action.MarketAction;
import roart.common.pipeline.PipelineConstants;
import roart.component.model.ComponentData;
import roart.component.model.ComponentMLData;
import roart.config.Market;
import roart.evolution.config.EvolutionConfig;
import roart.iclij.config.MLConfigs;
import roart.iclij.model.MemoryItem;
import roart.service.model.ProfitData;

public class DatasetComponent extends ComponentML {

    @Override
    public void enable(Map<String, Object> valueMap) {
    }

    @Override
    public void disable(Map<String, Object> valueMap) {
    }

    @Override
    public ComponentData handle(MarketAction action, Market market, ComponentData param, ProfitData profitdata,
            List<Integer> positions, boolean evolve, Map<String, Object> aMap, String subcomponent, String mlmarket) {
        int futuredays = (int) param.getService().conf.getAggregatorsIndicatorFuturedays();
        param.setFuturedays(futuredays);

        handle2(action, market, param, profitdata, positions, evolve, aMap, subcomponent, mlmarket);
        //Map resultMaps = param.getResultMap();
        //handleMLMeta(param, resultMaps);
        //Map<String, Object> resultMap = param.getResultMap();
        return param;
    }

    @Override
    public ComponentData improve(MarketAction action, ComponentData param, Market market, ProfitData profitdata,
            List<Integer> positions, Boolean buy, String subcomponent) {
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
    public List<MemoryItem> calculateMemory(ComponentData param) throws Exception {
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

}
