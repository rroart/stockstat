package roart.component;

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
import roart.config.IclijXMLConfig;
import roart.config.Market;
import roart.evolution.config.EvolutionConfig;
import roart.iclij.config.EvolveMLConfig;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.MLConfigs;
import roart.iclij.model.ConfigItem;
import roart.result.model.ResultItem;
import roart.service.model.ProfitData;
import roart.util.ServiceUtil;

public abstract class ComponentNoML extends Component {

    @Override
    protected Map<String, Object> handleEvolve(Market market, String pipeline, boolean evolve, ComponentData param, String subcomponent, Map<String, Object> scoreMap) {
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
    protected Map<String, Object> mlLoads(ComponentData param, Map<String, Object> anUpdateMap, Market market, Boolean buy, String subcomponent) throws Exception {
        Map<String, Object> map = new HashMap<>();
        String marketName = market.getConfig().getMarket();
        String component = getPipeline();
        Map<String, Object> configMap  = ServiceUtil.loadConfig(param, market, marketName, param.getAction(), component, false, buy, subcomponent);
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
    public EvolutionConfig getLocalEvolutionConfig(ComponentData componentdata) {
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
    public List<String>[] enableDisable(ComponentData param, List<Integer> positions) {
        return new ArrayList[] { new ArrayList<String>(), new ArrayList<String>() };
    }

    @Override
    public List<String> getSubComponents(Market market, ComponentData componentData) {
        List<String> list = new ArrayList<>();
        list.add("");
        return list;
    }

    @Override
    protected void handleMLMeta(ComponentData param, Map<String, List<Object>> mlMaps) {        
    }

}
