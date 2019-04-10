package roart.component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import roart.common.config.ConfigConstants;
import roart.common.config.MyMyConfig;
import roart.common.constants.Constants;
import roart.common.ml.NeuralNetConfigs;
import roart.common.ml.TensorflowLSTMConfig;
import roart.common.pipeline.PipelineConstants;
import roart.common.util.JsonUtil;
import roart.component.model.ComponentData;
import roart.component.model.ComponentMLData;
import roart.component.model.MLMACDData;
import roart.config.Market;
import roart.evolution.config.EvolutionConfig;
import roart.iclij.config.EvolveMLConfig;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.MLConfigs;
import roart.iclij.model.ConfigItem;
import roart.result.model.ResultItem;
import roart.result.model.ResultMeta;
import roart.service.model.ProfitData;
import roart.util.ServiceUtil;

public abstract class ComponentML extends Component {

    @Override
    protected Map<String, Object> handleEvolve(Market market, String pipeline, boolean evolve, ComponentData param) {
        // special
        //String localMl = param.getInput().getConfig().getFindProfitMLIndicatorMLConfig();
        Map<String, EvolveMLConfig> mlConfigMap = getMLConfig(market, param);
        // part special
        // if (param.getInput().getConfig().wantEvolveML()) {
        if (evolve) {
            EvolutionConfig evolveConfig = getEvolutionConfig(param);
            String newConfStr = JsonUtil.convert(evolveConfig);
            param.getService().conf.getConfigValueMap().put(ConfigConstants.EVOLVEMLEVOLUTIONCONFIG, newConfStr);
             
            Map<String, Object> evolveMap = setnns(param.getService().conf, param.getInput().getConfig(), mlConfigMap, true);
            param.getService().conf.getConfigValueMap().putAll(evolveMap);
            Map<String, Object> anUpdateMap = new HashMap<>();
            List<ResultItem> retlist = param.getService().getEvolveML(true, new ArrayList<>(), pipeline, param.getService().conf, anUpdateMap);
            mlSaves(mlConfigMap, param, anUpdateMap);
            if (param.getUpdateMap() != null) {
                param.getUpdateMap().putAll(anUpdateMap); 
            }
            return evolveMap;
        }
        return new HashMap<>();
        //Map<String, Object> i = setnns(param.getService().conf, param.getInput().getConfig(), mlConfigMap, false);
    }

    private void mlSaves(Map<String, EvolveMLConfig> mlConfigMap, ComponentData param, Map<String, Object> anUpdateMap) {
        for (Entry<String, Object> entry : anUpdateMap.entrySet()) {
            String key = entry.getKey();
            String nnconfigString = (String) entry.getValue();
            //Map<String, String> mapToConfig = MLConfigs.getMapToConfig();
            NeuralNetConfigs nnConfigs = null;
            try {
                if (getPipeline().equals(PipelineConstants.PREDICTORSLSTM)) {
                    nnConfigs = new NeuralNetConfigs();
                    ObjectMapper mapper = new ObjectMapper();
                    TensorflowLSTMConfig nnConfig = mapper.readValue(nnconfigString, TensorflowLSTMConfig.class);
                    nnConfigs.setTensorflowLSTMConfig(nnConfig);                
                } else {
                    ObjectMapper mapper = new ObjectMapper();
                    nnConfigs = mapper.readValue(nnconfigString, NeuralNetConfigs.class);            
                }
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
            for (Entry<String, EvolveMLConfig> entry2 : mlConfigMap.entrySet()) {
                String key2 = entry2.getKey();
                EvolveMLConfig config = entry2.getValue();
                if (!config.getSave()) {
                    if (nnConfigs.get(key2) != null) {
                        log.error("Key {} not null", key2);
                    }
                    nnConfigs.set(key2, null);
                }
            }
            String value = null;
            try {
                if (getPipeline().equals(PipelineConstants.PREDICTORSLSTM)) {
                    TensorflowLSTMConfig nnConfig = nnConfigs.getTensorflowLSTMConfig();
                    value = JsonUtil.convert(nnConfig);
                } else {
                    value = JsonUtil.convert(nnConfigs);
                }
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
            if (value == null) {
                continue;
            }
            ConfigItem configItem = new ConfigItem();
            configItem.setAction(param.getAction());
            configItem.setComponent(getPipeline());
            configItem.setDate(param.getBaseDate());
            configItem.setId(key);
            configItem.setMarket(param.getMarket());
            configItem.setRecord(LocalDate.now());
            configItem.setValue(value);
            try {
                configItem.save();
            } catch (Exception e) {
                log.info(Constants.EXCEPTION, e);
            }
        }
    }

    private void mlSavesNot(Map<String, EvolveMLConfig> mlConfigMap, ComponentData param, Map<String, Object> anUpdateMap) {
        Map<String, String> mapToConfig = MLConfigs.getMapToConfig();
        for (Entry<String, EvolveMLConfig> entry : mlConfigMap.entrySet()) {
            String key = entry.getKey();
            EvolveMLConfig config = entry.getValue();
            if (config.getSave()) {
                ConfigItem configItem = new ConfigItem();
                configItem.setAction(param.getAction());
                configItem.setComponent(getPipeline());
                configItem.setDate(param.getBaseDate());
                configItem.setId(key);
                configItem.setMarket(param.getMarket());
                configItem.setRecord(LocalDate.now());
                String configKey = mapToConfig.get(key);
                String value = JsonUtil.convert(anUpdateMap.get(configKey));
                if (value == null) {
                    continue;
                }
                configItem.setValue(value);
                try {
                    configItem.save();
                } catch (Exception e) {
                    log.info(Constants.EXCEPTION, e);
                }
            }
        }
    }

    @Override
    protected Map<String, Object> mlLoads(ComponentData param, Map<String, Object> anUpdateMap, Market market) throws Exception {
        Map<String, EvolveMLConfig> mlConfigMap = getMLConfig(market, param);
        return mlLoads(mlConfigMap, param, anUpdateMap, market);
    }
    
    protected Map<String, Object> mlLoads(Map<String, EvolveMLConfig> mlConfigMap, ComponentData param, Map<String, Object> anUpdateMap, Market market) throws Exception {
        Map<String, Object> map = new HashMap<>();
        for (Entry<String, EvolveMLConfig> entry : mlConfigMap.entrySet()) {
            String key = entry.getKey();
            EvolveMLConfig config = entry.getValue();
            if (config.getLoad()) {
                String marketName = market.getConfig().getMarket();
                String component = getPipeline();
                Map<String, Object> configMap  = ServiceUtil.loadConfig(param, market, marketName, param.getAction(), component, false);
                map.putAll(configMap);
            }
        }
        return map;
    }

    @Override
    public Map<String, EvolveMLConfig> getMLConfig(Market market, ComponentData param) {
        System.out.println(param.getInput().getConfig().getEvolveMLMLConfig());
        String localMl = getLocalMLConfig(param);
        String ml = param.getInput().getConfig().getEvolveMLMLConfig();
        MLConfigs marketMlConfig = market.getMlconfig();
        MLConfigs mlConfig = JsonUtil.convert(ml, MLConfigs.class);
        MLConfigs localMLConfig = JsonUtil.convert(localMl, MLConfigs.class);
        // special
        MLConfigs overrideLSTM = getOverrideMLConfig(param);
        mlConfig.merge(localMLConfig);
        mlConfig.merge(marketMlConfig);
        mlConfig.merge(overrideLSTM);
        Map<String, EvolveMLConfig> mlConfigMap = mlConfig.getAll();
        return mlConfigMap;
    }

    @Override
    public EvolutionConfig getEvolutionConfig(ComponentData param) {
        String confStr = param.getInput().getConfig().getEvolveMLEvolutionConfig();
        EvolutionConfig evolveConfig = JsonUtil.convert(confStr, EvolutionConfig.class);
        EvolutionConfig localEvolveConfig = getLocalEvolutionConfig(param);
        evolveConfig.merge(localEvolveConfig);
        return evolveConfig;
    }

    protected void handleMLMeta(ComponentMLData param, Map<String, List<Object>> mlMaps) {
        Map<String, List<Double>> probabilityMap = (Map<String, List<Double>>) mlMaps.get(PipelineConstants.PROBABILITY);
        List<List> resultMetaArray = (List) mlMaps.get(PipelineConstants.RESULTMETAARRAY);
        param.setResultMetaArray(resultMetaArray);
        //List<ResultMeta> resultMeta = (List<ResultMeta>) mlMACDMaps.get(PipelineConstants.RESULTMETA);
        List<Object> objectList = (List<Object>) mlMaps.get(PipelineConstants.RESULTMETA);
        List<ResultMeta> resultMeta = new ObjectMapper().convertValue(objectList, new TypeReference<List<ResultMeta>>() { });
        param.setResultMeta(resultMeta);
    }

    public static Map<String, Object> setnns(MyMyConfig conf, IclijConfig config, Map<String, EvolveMLConfig> mlConfigMap, boolean useEvolve) {
        Map<String, Object> returnmap = new HashMap<>();
        Map<String, String> map = config.getConv();
        for (Entry<String, EvolveMLConfig> entry : mlConfigMap.entrySet()) {
            String key = entry.getKey();
            EvolveMLConfig value = entry.getValue();
            boolean myenable = Boolean.TRUE.equals(value.getEnable());
            if (useEvolve) {
                boolean evolve = Boolean.TRUE.equals(value.getEvolve());
                myenable = myenable && evolve;
            }
            //System.out.println(conf.getConfigValueMap().keySet());
            Object o = conf.getValueOrDefault(key);
            boolean enable = (boolean) conf.getValueOrDefault(key);
            String otherKey = map.get(key);
            returnmap.put(key, myenable);
            if (otherKey != null) {
                returnmap.put(otherKey, enable);
            }
        }
        return returnmap;
    }
}
