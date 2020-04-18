package roart.iclij.model.config;




import roart.iclij.config.IclijConfigConstants;
import roart.iclij.config.IclijConfig;

public class ImproveFilterMLIndicatorConfig extends ActionComponentConfigML {
    @Override
    public String getLocalEvolutionConfig(IclijConfig config) {
        return config.getImproveFilterMLIndicatorEvolutionConfig();
    }

    @Override
    public String getLocalMLConfig(IclijConfig config) {
        return config.getImproveFilterMLIndicatorMLConfig();
    }

    @Override
    public int getPriority(IclijConfig srv) {
        return getPriority(srv, IclijConfigConstants.IMPROVEFILTERMLINDICATOR);
    }

}
