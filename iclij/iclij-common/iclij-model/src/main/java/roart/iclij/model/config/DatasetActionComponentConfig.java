package roart.iclij.model.config;

import roart.iclij.config.IclijConfig;

public abstract class DatasetActionComponentConfig extends ActionComponentConfigML {

    @Override
    public int getPriority(IclijConfig config) {
        return 0;
    }

}
