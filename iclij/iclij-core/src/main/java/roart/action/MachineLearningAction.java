package roart.action;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.component.Component;
import roart.component.model.ComponentData;
import roart.config.Market;
import roart.constants.IclijConstants;
import roart.iclij.config.IclijConfig;
import roart.iclij.model.IncDecItem;
import roart.iclij.model.MemoryItem;
import roart.service.model.ProfitData;
import roart.service.model.ProfitInputData;

public class MachineLearningAction extends MarketAction {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public String getName() {
        return IclijConstants.MACHINELEARNING;
    }

    @Override
    protected ProfitInputData filterMemoryListMapsWithConfidence(Market market,
            Map<Object[], List<MemoryItem>> listMap) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected void handleComponent(Market market, ProfitData profitdata, ComponentData param,
            Map<String, List<Integer>> listComponent, Map<String, Component> componentMap,
            Map<String, ComponentData> dataMap, Boolean buy, String subcomponent, WebData myData) {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected List<IncDecItem> getIncDecItems() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected List getAnArray() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected Boolean getBool() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected List<String> getProfitComponents(IclijConfig config, String marketName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected Short getTime(Market market) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected Boolean[] getBooleans() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected boolean getEvolve(Component component, ComponentData param) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    protected List<MemoryItem> getMemItems(MarketComponentTime marketTime, WebData myData, ComponentData param,
            IclijConfig config, Boolean evolve, Map<String, ComponentData> dataMap) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected LocalDate getPrevDate(ComponentData param, Market market) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected void setValMap(ComponentData param) {
        // TODO Auto-generated method stub
        
    }
}
