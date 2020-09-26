package roart.iclij.evolution.fitness.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.action.Action;
import roart.action.MarketAction;
import roart.common.constants.Constants;
import roart.component.Component;
import roart.component.model.ComponentData;
import roart.iclij.config.Market;
import roart.iclij.factory.actioncomponentconfig.ActionComponentConfigFactory;
import roart.iclij.filter.Memories;
import roart.iclij.model.MemoryItem;
import roart.iclij.model.Parameters;
import roart.iclij.model.WebData;
import roart.iclij.model.config.ActionComponentConfig;
import roart.service.model.ProfitData;
import roart.service.model.ProfitInputData;

public class FitnessIclijConfigMapCommon {

    protected Logger log = LoggerFactory.getLogger(this.getClass());

    public Double fitnessCommon(ProfitData profitdata, Map<String, Object> map, MarketAction action, Market market, ComponentData param, String componentName, String subcomponent, Parameters parameters, List<String> titletexts) {
        List<MemoryItem> memoryItems = null;
        WebData myData = new WebData();
        myData.setUpdateMap(new HashMap<>());
        myData.setMemoryItems(new ArrayList<>());
        //myData.profitData = new ProfitData();
        myData.setTimingMap(new HashMap<>());

        profitdata.setBuys(new HashMap<>());
        profitdata.setSells(new HashMap<>());

        Double score = null;
        try {
            Component component =  action.getComponentFactory().factory(componentName);
            ActionComponentConfig config = ActionComponentConfigFactory.factoryfactory(action.getName()).factory(component.getPipeline());
            component.setConfig(config);
            boolean evolve = false; // component.wantEvolve(param.getInput().getConfig());
            myData.setProfitData(profitdata);

            Memories listMap = new Memories(market);

            ProfitInputData inputdata = new ProfitInputData();
            profitdata.setInputdata(inputdata);
            inputdata.setNameMap(new HashMap<>());

            Map<String, Object> override = param.getInput().getValuemap();
            if (override != null) {
                map.putAll(override);
            }
            
            param.getInput().getConfig().getConfigValueMap().putAll(map);
            
            Action parent = action.getParent();
            action.setParent(null);
            ComponentData componentData2 = component.handle(action, market, param, profitdata, listMap, evolve, map, subcomponent, null, parameters);
            action.setParent(parent);
            String titletext = (String) componentData2.getUpdateMap().get("titletext");
            titletexts.add(titletext);
            Object[] result = component.calculateAccuracy(componentData2);
            score = (Double) result[0];
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return score;
    }

}
