package roart.iclij.component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import roart.common.config.ConfigConstants;
import roart.common.config.MyMyConfig;
import roart.common.constants.Constants;
import roart.common.util.JsonUtil;
import roart.component.model.ComponentData;
import roart.component.model.RecommenderData;
import roart.constants.IclijConstants;
import roart.evolution.config.EvolutionConfig;
import roart.iclij.config.EvolveMLConfig;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijXMLConfig;
import roart.iclij.config.MLConfigs;
import roart.iclij.config.Market;
import roart.iclij.filter.Memories;
import roart.iclij.model.ConfigItem;
import roart.iclij.model.Parameters;
import roart.iclij.model.action.MarketActionData;
import roart.iclij.util.MiscUtil;
import roart.result.model.ResultItem;
import roart.service.model.ProfitData;

public abstract class ComponentNoML extends Component {

    @Override
    protected Map<String, Object> handleEvolve(Market market, String pipeline, boolean evolve, ComponentData param, String subcomponent, Map<String, Object> scoreMap, String mlmarket, Parameters parameters, EvolutionConfig actionEvolveConfig, String actionML) {
        if (evolve) {
            String confStr = param.getInput().getConfig().getEvolveIndicatorrecommenderEvolutionConfig();
            if (confStr != null) {
                param.getService().conf.getConfigValueMap().put(ConfigConstants.EVOLVEINDICATORRECOMMENDEREVOLUTIONCONFIG, confStr);
            }
            param.getService().conf.getConfigValueMap().put(ConfigConstants.MISCMYTABLEDAYS, 100);
            param.getService().conf.getConfigValueMap().put(ConfigConstants.MISCMYDAYS, 100);

            Map<String, Object> anUpdateMap = new HashMap<>();
            Map<String, Object> aScoreMap = new HashMap<>();
            Map<String, Object> resultMap = new HashMap<>();
            List<ResultItem> retlist = param.getService().getEvolveRecommender(true, param.getDisableList(), anUpdateMap, scoreMap, resultMap);
            nomlSaves(param, anUpdateMap);
            if (param.getUpdateMap() != null) {
                param.getUpdateMap().putAll(anUpdateMap); 
            }
            if (scoreMap != null) {
                scoreMap.putAll(aScoreMap);
            }
            param.setResultMap(resultMap);
            return anUpdateMap;
        }
        return new HashMap<>();
    }

    @Override
    protected Map<String, Object> mlLoads(ComponentData param, Map<String, Object> anUpdateMap, Market market, String action, Boolean buy, String subcomponent, String mlmarket, MarketActionData actionData, Parameters parameters) throws Exception {
        Map<String, Object> map = new HashMap<>();
        String marketName = market.getConfig().getMarket();
        String component = getPipeline();
        Map<String, Object> configMap  = new MiscUtil().loadConfig(param.getService(), param.getInput(), market, marketName, param.getAction(), component, false, buy, subcomponent, actionData, parameters);
        map.putAll(configMap);
        return map;
    }
    
    private void nomlSaves(ComponentData param, Map<String, Object> anUpdateMap) {
        //for (Entry<String, Object> entry : anUpdateMap.entrySet()) {
            String key = IclijConstants.ALL;
            Object object = JsonUtil.convert(anUpdateMap);
            ConfigItem configItem = new ConfigItem();
            configItem.setAction(param.getAction());
            configItem.setComponent(getPipeline());
            configItem.setDate(param.getBaseDate());
            configItem.setId(key);
            configItem.setMarket(param.getMarket());
            configItem.setRecord(LocalDate.now());
            String value = JsonUtil.convert(object);
            configItem.setValue(value);
            if (value == null) {
                log.error("Config value null");
                return;
            }
            try {
                configItem.save();
            } catch (Exception e) {
                log.info(Constants.EXCEPTION, e);
            }
        //}
    }

    @Override
    public EvolutionConfig getEvolutionConfig(ComponentData componentdata, EvolutionConfig actionEvolutionConfig) {
        return null;
    }

    @Override
    public boolean wantEvolve(IclijConfig config) {
        return config.wantEvolveRecommender();
    }

    @Override
    public boolean wantImproveEvolve() {
        return true;
    }
    
    @Override
    public List<String>[] enableDisable(ComponentData param, Memories positions, Boolean above) {
        return new ArrayList[] { new ArrayList<String>(), new ArrayList<String>() };
    }

    @Override
    public void handleMLMeta(ComponentData param, Map<String, List<Object>> mlMaps) {        
    }

}
