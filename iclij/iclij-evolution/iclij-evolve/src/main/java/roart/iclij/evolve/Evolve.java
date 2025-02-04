package roart.iclij.evolve;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.model.MLMetricsItem;
import roart.component.model.ComponentData;
import roart.evolution.config.EvolutionConfig;
import roart.filesystem.FileSystemDao;
import roart.iclij.component.Component;
import roart.iclij.config.Market;
import roart.iclij.model.Parameters;
import roart.iclij.model.action.MarketActionData;
import roart.service.model.ProfitData;

public abstract class Evolve {

    protected Logger log = LoggerFactory.getLogger(this.getClass());

    public abstract ComponentData evolve(MarketActionData action, ComponentData param, Market market, ProfitData profitdata, Boolean buy,
            String subcomponent, Parameters parameters, List<MLMetricsItem> mlTests, Map<String, Object> confMap,
            EvolutionConfig evolutionConfig, String pipeline, Component component, List<String> confList, FileSystemDao fileSystemDao);

}
