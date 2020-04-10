package roart.iclij.model.config;

import roart.iclij.config.IclijConfig;

public abstract class ActionComponentConfig {
    public abstract String getLocalEvolutionConfig(IclijConfig config);

    public abstract String getLocalMLConfig(IclijConfig config);

    public abstract int getPriority(IclijConfig config);

    public int getPriority(IclijConfig conf, String key) {
        Integer value = (Integer) conf.getConfigValueMap().get(key + "[@priority]");
        return value != null ? value : 0;
    }
    
}
