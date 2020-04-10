package roart.iclij.model.config;




import roart.iclij.config.IclijConfigConstants;
import roart.iclij.config.IclijConfig;

public class ImproveFilterMLATRConfig extends ActionComponentConfig {
    @Override
    public String getLocalEvolutionConfig(IclijConfig config) {
        return config.getImproveFilterMLATREvolutionConfig();
    }

    @Override
    public String getLocalMLConfig(IclijConfig config) {
        return config.getImproveFilterMLATRMLConfig();
    }

    @Override
    public int getPriority(IclijConfig srv) {
        return getPriority(srv, IclijConfigConstants.IMPROVEFILTERMLATR);
    }

}
