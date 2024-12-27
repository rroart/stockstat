package roart.iclij.component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import roart.common.constants.Constants;
import roart.common.model.IncDecItem;
import roart.common.model.MLMetricsItem;
import roart.common.model.MemoryItem;
import roart.common.pipeline.PipelineConstants;
import roart.common.util.TimeUtil;
import roart.component.model.ComponentData;
import roart.evolution.fitness.Fitness;
import roart.iclij.config.MLConfigs;
import roart.iclij.config.Market;
import roart.iclij.filter.Memories;
import roart.iclij.model.Parameters;
import roart.iclij.model.action.MarketActionData;
import roart.iclij.service.util.MiscUtil;
import roart.service.model.ProfitData;

public class AboveBelowComponent extends ComponentML {

    private static final int AVERAGE_SIZE = 5;

    @Override
    public void enable(Map<String, Object> valueMap) {
    }

    @Override
    public void disable(Map<String, Object> valueMap) {
    }

    @Override
    public ComponentData handle(MarketActionData action, Market market, ComponentData param, ProfitData profitdata,
            Memories positions, boolean evolve, Map<String, Object> aMap, String subcomponent, String mlmarket,
            Parameters parameters, boolean hasParent) {
        long time0 = System.currentTimeMillis();
        ComponentData componentData = new ComponentData(param);
        
        int verificationdays = param.getConfig().verificationDays();
        List<String> stockDates = param.getService().getDates(market.getConfig().getMarket());
        List<IncDecItem> allIncDecs = null;
        LocalDate date = param.getFutureDate();
        date = TimeUtil.getBackEqualBefore2(date, verificationdays, stockDates);
        LocalDate prevDate = date.minusDays(market.getConfig().getFindtime());
        try {
            allIncDecs = action.getDbDao().getAllIncDecs(market.getConfig().getMarket(), prevDate, date, null);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        List<IncDecItem> incdecs = allIncDecs; // new MiscUtil().getCurrentIncDecs(date, allIncDecs, market, market.getConfig().getFindtime(), false);
        List<String> parametersList = new MiscUtil().getParameters(incdecs);
        if (parametersList.isEmpty()) {
            saveTiming(action, param, true, time0, null, null, subcomponent, null, null, null, action.doSaveTiming());
        }

        return componentData;
    }

    @Override
    public ComponentData improve(MarketActionData action, ComponentData param, Market market, ProfitData profitdata,
            Memories positions, Boolean buy, String subcomponent, Parameters parameters, boolean wantThree,
            List<MLMetricsItem> mlTests, Fitness fitness, boolean save) {
        return null;
    }

    @Override
    public MLConfigs getOverrideMLConfig(ComponentData componentdata) {
        return null;
    }

    @Override
    public void calculateIncDec(ComponentData param, ProfitData profitdata, Memories positions, Boolean above,
            List<MLMetricsItem> mlTests, Parameters parameters) {
    }

    @Override
    public List<MemoryItem> calculateMemory(MarketActionData actionData, ComponentData param, Parameters parameters) throws Exception {
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
