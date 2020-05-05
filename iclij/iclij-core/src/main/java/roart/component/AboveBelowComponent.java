package roart.component;

import java.util.List;
import java.util.Map;

import roart.action.MarketAction;
import roart.common.pipeline.PipelineConstants;
import roart.component.model.ComponentData;
import roart.iclij.config.MLConfigs;
import roart.iclij.config.Market;
import roart.iclij.model.MLMetricsItem;
import roart.iclij.model.MemoryItem;
import roart.iclij.model.Parameters;
import roart.service.model.ProfitData;

public class AboveBelowComponent extends ComponentML {

    @Override
    public void enable(Map<String, Object> valueMap) {
    }

    @Override
    public void disable(Map<String, Object> valueMap) {
    }

    @Override
    public ComponentData handle(MarketAction action, Market market, ComponentData param, ProfitData profitdata,
            List<Integer> positions, boolean evolve, Map<String, Object> aMap, String subcomponent, String mlmarket,
            Parameters parameters) {
        return null;
    }

    @Override
    public ComponentData improve(MarketAction action, ComponentData param, Market market, ProfitData profitdata,
            List<Integer> positions, Boolean buy, String subcomponent, Parameters parameters, boolean wantThree,
            List<MLMetricsItem> mlTests) {
        return null;
    }

    @Override
    public MLConfigs getOverrideMLConfig(ComponentData componentdata) {
        return null;
    }

    @Override
    public void calculateIncDec(ComponentData param, ProfitData profitdata, List<Integer> positions, Boolean above,
            List<MLMetricsItem> mlTests, Parameters parameters) {
    }

    @Override
    public List<MemoryItem> calculateMemory(ComponentData param, Parameters parameters) throws Exception {
        return null;
    }

    @Override
    public String getPipeline() {
        return PipelineConstants.ABOVEBELOW;
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
