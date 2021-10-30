package roart.iclij.model.action;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import roart.iclij.config.IclijConfig;
import roart.iclij.config.Market;
import roart.iclij.model.config.ActionComponentConfig;

public abstract class MarketActionData {

    public abstract String getName();

    public abstract String getThreshold(IclijConfig conf);
    
    public abstract Short getTime(Market market);
    
    public abstract Boolean[] getBooleans();
    
    public boolean isDataset() {
        return false;
    }
    
    public Map<Boolean, String> getBooleanTexts() {
        Map<Boolean, String> map = new HashMap<>();
        map.put(null, "");
        map.put(false, "down");
        map.put(true, "up");
        return map;
    }

    public abstract List<String> getComponents(IclijConfig config, boolean wantThree);

    public abstract String getFuturedays(IclijConfig conf);
    
    public abstract String getPriority();
    
    public String getParamDateFromConfig(Market market, List<String> stockDates) {
        return null;
    }

    public abstract boolean wantsUpdate(IclijConfig config);
    
	public String getEvolutionConfig(IclijConfig config) {
		return config.getEvolveMLEvolutionConfig();
	}
    
	public String getMLConfig(IclijConfig config) {
		return config.getEvolveMLMLConfig();
	}
}
