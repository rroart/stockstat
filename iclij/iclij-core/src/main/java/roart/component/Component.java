package roart.component;

import java.util.List;
import java.util.Map;

import roart.config.ConfigConstants;
import roart.config.IclijConfig;
import roart.config.MyMyConfig;
import roart.model.IncDecItem;
import roart.model.MemoryItem;
import roart.service.ControlService;

public abstract class Component {
    public abstract void enable(MyMyConfig conf);
    public abstract void disable(MyMyConfig conf);
    public static void disabler(MyMyConfig conf) {
        conf.getConfigValueMap().put(ConfigConstants.AGGREGATORSINDICATORRECOMMENDER, Boolean.FALSE);
        conf.getConfigValueMap().put(ConfigConstants.PREDICTORS, Boolean.FALSE);
        conf.getConfigValueMap().put(ConfigConstants.MACHINELEARNING, Boolean.FALSE);        
    }
    public abstract void handle(ControlService srv, MyMyConfig conf, Map<String, Map<String, Object>> maps, List<Integer> positions, Map<String, IncDecItem> buys, Map<String, IncDecItem> sells, Map<Object[], Double> okConfMap, Map<Object[], List<MemoryItem>> okListMap, Map<String, String> nameMap, IclijConfig config, Map<String, Object> updateMap);
    public abstract Map<String, String> improve(MyMyConfig conf, Map<String, Map<String, Object>> maps, List<Integer> positions, Map<String, IncDecItem> buys, Map<String, IncDecItem> sells, Map<Object[], Double> okConfMap, Map<Object[], List<MemoryItem>> okListMap, Map<String, String> nameMap);
}

