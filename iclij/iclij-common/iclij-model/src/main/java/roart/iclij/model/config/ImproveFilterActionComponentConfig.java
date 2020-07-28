package roart.iclij.model.config;

import roart.iclij.config.IclijConfig;

public abstract class ImproveFilterActionComponentConfig extends ActionComponentConfigNoML {

    @Override
    public int getPriority(IclijConfig config) {
        return 0;
    }

}
