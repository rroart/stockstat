package roart.iclij.model.config;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.ml.MLMaps;
import roart.iclij.config.EvolveMLConfig;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.Market;

public abstract class ActionComponentConfig {
    
    protected static Logger log = LoggerFactory.getLogger(ActionComponentConfig.class);

    public abstract List<String> getSubComponents(Market market, IclijConfig config, String mlmarket, String actionML);

    public abstract String getLocalEvolutionConfig(IclijConfig config);

    public abstract String getLocalMLConfig(IclijConfig config);

    public abstract int getPriority(IclijConfig config);

    public int getPriority(IclijConfig conf, String key) {
        Integer value = (Integer) conf.getConfigData().getConfigValueMap().get(key + "[@priority]");
        return value != null ? value : 0;
    }

    public abstract MLMaps getMLMaps();

    public abstract Map<String, EvolveMLConfig> getMLConfig(Market market, IclijConfig config, String mlmarket, String actionML);

}
