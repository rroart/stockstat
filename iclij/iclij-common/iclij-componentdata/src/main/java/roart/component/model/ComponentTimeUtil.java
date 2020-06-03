package roart.component.model;

import roart.iclij.config.Market;
import roart.iclij.model.component.ComponentInput;

public class ComponentTimeUtil {
    public int getFindProfitOffset(Market market, ComponentInput input) {
        Integer offsetMult = input.getLoopoffset();
        if (offsetMult == null) {
            return 0;
        }
        int marketactiondays = market.getConfig().getFindtime();
        return offsetMult * marketactiondays;
    }
}
