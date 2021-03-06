package roart.component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import roart.action.MarketAction;
import roart.common.config.ConfigConstants;
import roart.common.config.MyMyConfig;
import roart.common.constants.Constants;
import roart.common.util.JsonUtil;
import roart.component.model.ComponentData;
import roart.component.model.RecommenderData;
import roart.evolution.config.EvolutionConfig;
import roart.iclij.config.EvolveMLConfig;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijXMLConfig;
import roart.iclij.config.MLConfigs;
import roart.iclij.config.Market;
import roart.iclij.filter.Memories;
import roart.iclij.model.ConfigItem;
import roart.iclij.model.Parameters;
import roart.iclij.util.MiscUtil;
import roart.result.model.ResultItem;
import roart.service.model.ProfitData;
import roart.util.ServiceUtil;

public abstract class ComponentNoML extends Component {

    @Override
    protected Map<String, Object> handleEvolve(Market market, String pipeline, boolean evolve, ComponentData param, String subcomponent, Map<String, Object> scoreMap, String mlmarket, Parameters parameters) {
        if (evolve) {
            String confStr = param.getInput().getConfig().getEvolveIndicatorrecommenderEvolutionConfig();
            if (confStr != null) {
                param.getService().conf.getConfigValueMap().put(ConfigConstants.EVOLVEINDICATORRECOMMENDEREVOLUTIONCONFIG, confStr);
            }
            Map<String, Object> anUpdateMap = new HashMap<>();
            List<ResultItem> retlist = param.getService().getEvolveRecommender(true, param.getDisableList(), anUpdateMap);
            nomlSaves(param, anUpdateMap);
            if (param.getUpdateMap() != null) {
                param.getUpdateMap().putAll(anUpdateMap); 
            }
            return anUpdateMap;
        }
        return new HashMap<>();
    }

    @Override
    protected Map<String, Object> mlLoads(ComponentData param, Map<String, Object> anUpdateMap, Market market, Boolean buy, String subcomponent, String mlmarket, MarketAction action, Parameters parameters) throws Exception {
        Map<String, Object> map = new HashMap<>();
        String marketName = market.getConfig().getMarket();
        String component = getPipeline();
        Map<String, Object> configMap  = new MiscUtil().loadConfig(param.getService(), param.getInput(), market, marketName, param.getAction(), component, false, buy, subcomponent, action.getActionData(), parameters);
        map.putAll(configMap);
        return map;
    }
    
    private void nomlSaves(ComponentData param, Map<String, Object> anUpdateMap) {
        for (Entry<String, Object> entry : anUpdateMap.entrySet()) {
            String key = entry.getKey();
            Object object = entry.getValue();
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
                continue;
            }
            try {
                configItem.save();
            } catch (Exception e) {
                log.info(Constants.EXCEPTION, e);
            }
        }
    }

    @Override
    public EvolutionConfig getEvolutionConfig(ComponentData componentdata) {
        return null;
    }

    @Override
    protected EvolutionConfig getImproveEvolutionConfig(IclijConfig config) {
        /*
        ObjectMapper mapper = new ObjectMapper();
        EvolutionConfig evolutionConfig = null;
        try {
            evolutionConfig = mapper.readValue(conf.getTestMLEvolutionConfig(), EvolutionConfig.class);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        */
        // too heavy?
        // String evolveString = config.getEvolveIndicatorrecommenderEvolutionConfig();
        String evolveString = config.getEvolveMLEvolutionConfig();
        return JsonUtil.convert(evolveString, EvolutionConfig.class);
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
    protected void handleMLMeta(ComponentData param, Map<String, List<Object>> mlMaps) {        
    }

}
