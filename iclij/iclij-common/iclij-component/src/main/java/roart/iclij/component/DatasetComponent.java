package roart.iclij.component;

import java.util.List;
import java.util.Map;

import roart.common.config.ConfigConstants;
import roart.common.model.MLMetricsDTO;
import roart.common.model.MemoryDTO;
import roart.common.pipeline.PipelineConstants;
import roart.component.model.ComponentData;
import roart.component.model.DatasetData;
import roart.evolution.config.EvolutionConfig;
import roart.evolution.fitness.Fitness;
import roart.iclij.config.IclijConfigConstants;
import roart.iclij.config.MLConfigs;
import roart.iclij.config.Market;
import roart.iclij.filter.Memories;
import roart.iclij.model.Parameters;
import roart.iclij.model.action.MarketActionData;
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
    public ComponentData handle(MarketActionData action, Market market, ComponentData componentparam, ProfitData profitdata,
            Memories positions, boolean evolve, Map<String, Object> aMap, String subcomponent, String mlmarket, Parameters parameters, boolean hasParent) {
        DatasetData param = new DatasetData(componentparam);

        //int futuredays = (int) param.getService().conf.getAggregatorsIndicatorFuturedays();
        param.setFuturedays(0);

        handle2(action, market, param, profitdata, positions, evolve, aMap, subcomponent, mlmarket, parameters, hasParent);
        //Map resultMaps = param.getResultMap();
        //handleMLMeta(param, resultMaps);
        //Map<String, Object> resultMap = param.getResultMap();
        return param;
    }

    @Override
    public ComponentData improve(MarketActionData action, ComponentData param, Market market, ProfitData profitdata,
                                 Memories positions, Boolean buy, String subcomponent, Parameters parameters, boolean wantThree, List<MLMetricsDTO> mlTests, Fitness fitness, boolean save) {
        return null;
    }

    @Override
    public MLConfigs getOverrideMLConfig(ComponentData componentdata) {
        return null;
    }

    @Override
    public void calculateIncDec(ComponentData param, ProfitData profitdata, Memories positions, Boolean above, List<MLMetricsDTO> mlTests, Parameters parameters) {
    }

    @Override
    public List<MemoryDTO> calculateMemory(MarketActionData actionData, ComponentData param, Parameters parameters) throws Exception {
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
    public String getThreshold() {
        return null;
    }

    @Override
    public String getFuturedays() {
        return null;
    }

}
