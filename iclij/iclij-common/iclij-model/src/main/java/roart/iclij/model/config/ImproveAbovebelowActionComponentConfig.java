package roart.iclij.model.config;

import java.util.List;
import java.util.Map;

import roart.common.ml.MLMaps;
import roart.iclij.config.EvolveMLConfig;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.Market;

public abstract class ImproveAbovebelowActionComponentConfig extends ActionComponentConfigNoML {

    @Override
    public int getPriority(IclijConfig config) {
        return 0;
    }

}
