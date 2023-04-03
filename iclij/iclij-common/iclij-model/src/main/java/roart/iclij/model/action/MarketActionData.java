package roart.iclij.model.action;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.db.dao.IclijDbDao;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.Market;
import roart.iclij.model.config.ActionComponentConfig;

public abstract class MarketActionData {

    protected Logger log = LoggerFactory.getLogger(this.getClass());
    
    private IclijDbDao dbDao;
    
    public abstract String getName();

    public abstract String getThreshold(IclijConfig conf);
    
    public abstract Short getTime(Market market);
    
    public abstract Boolean[] getBooleans();
    
    public boolean isDataset() {
        return false;
    }
    
    public MarketActionData(IclijDbDao dbDao) {
        super();
        this.dbDao = dbDao;
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
	
	public boolean doHandleMLMeta() {
		return false;
	}
	
	public boolean isEvolving() {
		return false;
	}
	
	public boolean doSaveTiming() {
		return true;
	}
	
    public Object[] getScoreDescription(Object[] accuracy, Map<String, Object> scoreMap) {
        return new Object[] { null, null };
    }

    public IclijDbDao getDbDao() {
        return dbDao;
    }
}
