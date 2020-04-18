package roart.iclij.model.config;



import roart.iclij.config.IclijConfigConstants;
import roart.iclij.config.IclijConfig;

public class CrosstestPredictorConfig extends ActionComponentConfigMLPredictor {
    @Override
    public String getLocalEvolutionConfig(IclijConfig config) {
        return config.getCrosstestPredictorEvolutionConfig();
    }

    @Override
    public String getLocalMLConfig(IclijConfig config) {
        return config.getCrosstestPredictorMLConfig();
    }

    @Override
    public int getPriority(IclijConfig srv) {
        return getPriority(srv, IclijConfigConstants.CROSSTESTPREDICTOR);
    }
}
