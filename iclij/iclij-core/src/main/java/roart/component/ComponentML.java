package roart.component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import roart.common.config.ConfigConstants;
import roart.common.config.MLConstants;
import roart.common.config.MyMyConfig;
import roart.common.constants.Constants;
import roart.common.ml.NeuralNetConfigs;
import roart.common.ml.TensorflowPredictorLSTMConfig;
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
    protected Map<String, Object> handleEvolve(Market market, String pipeline, boolean evolve, ComponentData param, String subcomponent) {
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
            List<ResultItem> retlist = param.getService().getEvolveML(true, param.getDisableList(), pipeline, param.getService().conf, anUpdateMap);
            mlSaves(mlConfigMap, param, anUpdateMap, subcomponent);
            if (param.getUpdateMap() != null) {
                param.getUpdateMap().putAll(anUpdateMap); 
            }
            return evolveMap;
        }
        return new HashMap<>();
        //Map<String, Object> i = setnns(param.getService().conf, param.getInput().getConfig(), mlConfigMap, false);
    }

    private void mlSaves(Map<String, EvolveMLConfig> mlConfigMap, ComponentData param, Map<String, Object> anUpdateMap, String subcomponent) {
        for (Entry<String, Object> entry : anUpdateMap.entrySet()) {
            String key = entry.getKey();
            String nnconfigString = (String) entry.getValue();
            //Map<String, String> mapToConfig = MLConfigs.getMapToConfig();
            NeuralNetConfigs nnConfigs = null;
            try {
                if (getPipeline().equals(PipelineConstants.PREDICTORSLSTM)) {
                    nnConfigs = new NeuralNetConfigs();
                    ObjectMapper mapper = new ObjectMapper();
                    TensorflowPredictorLSTMConfig nnConfig = mapper.readValue(nnconfigString, TensorflowPredictorLSTMConfig.class);
                    nnConfigs.getTensorflowConfig().setTensorflowPredictorLSTMConfig(nnConfig);                
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
                    TensorflowPredictorLSTMConfig nnConfig = nnConfigs.getTensorflowConfig().getTensorflowPredictorLSTMConfig();
                    value = JsonUtil.convert(nnConfig);
                } else {
                    value = JsonUtil.convert(nnConfigs);
                }
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
            if (value == null) {
                log.error("Config value null");
                continue;
            }
            ConfigItem configItem = new ConfigItem();
            configItem.setAction(param.getAction());
            configItem.setComponent(getPipeline());
            configItem.setDate(param.getBaseDate());
            configItem.setId(key);
            configItem.setMarket(param.getMarket());
            configItem.setRecord(LocalDate.now());
            configItem.setSubcomponent(subcomponent);
            configItem.setValue(value);
            try {
                configItem.save();
            } catch (Exception e) {
                log.info(Constants.EXCEPTION, e);
            }
        }
    }

    @Override
    protected Map<String, Object> mlLoads(ComponentData param, Map<String, Object> anUpdateMap, Market market, Boolean buy, String subcomponent) throws Exception {
        Map<String, EvolveMLConfig> mlConfigMap = getMLConfig(market, param);
        return mlLoads(mlConfigMap, param, anUpdateMap, market, buy, subcomponent);
    }
    
    protected Map<String, Object> mlLoads(Map<String, EvolveMLConfig> mlConfigMap, ComponentData param, Map<String, Object> anUpdateMap, Market market, Boolean buy, String subcomponent) throws Exception {
        Map<String, Object> map = new HashMap<>();
        for (Entry<String, EvolveMLConfig> entry : mlConfigMap.entrySet()) {
            String key = entry.getKey();
            EvolveMLConfig config = entry.getValue();
            if (config.getLoad()) {
                String marketName = market.getConfig().getMarket();
                String component = getPipeline();
                Map<String, Object> configMap  = ServiceUtil.loadConfig(param, market, marketName, param.getAction(), component, false, buy, subcomponent);
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
        String evolveString = config.getEvolveMLEvolutionConfig();
        return JsonUtil.convert(evolveString, EvolutionConfig.class);
    }
    
    @Override
    public boolean wantEvolve(IclijConfig config) {
        return config.wantEvolveML();
    }
    
    @Override
    public boolean wantImproveEvolve() {
        return false;
    }
    
    @Override
    public List<String>[] enableDisable(ComponentData param, List<Integer> positions) {
        if (positions == null || positions.isEmpty()) {
            return new ArrayList[] { new ArrayList<String>(), new ArrayList<String>() };
        }
        List[] list = new ArrayList[2];
        List<String> enable = new ArrayList<>();
        Map<Pair<String, String>, String> map = getMap();
        ComponentMLData paramML = (ComponentMLData) param;
        List<ResultMeta> resultMetas = paramML.getResultMeta();
        int count = 0;
        if (resultMetas != null) {
            for (ResultMeta resultMeta : resultMetas) {
                String mlname = resultMeta.getMlName();
                String name = resultMeta.getModelName();
                Pair<String, String> pair = new ImmutablePair(mlname, name);
                String cnf = map.get(pair);
                if (positions == null || positions.contains(count)) {
                    if (cnf == null) {
                        continue;
                    }
                    if (!enable.contains(cnf)) {
                        enable.add(cnf);
                    }
                    map.remove(name);
                }
                count++;        
            }
        } else {
            int jj = 0;
        }
        list[0] = enable;
        list[1] = new ArrayList<String>(map.values());
        return list;
    }

    @Deprecated
    private Map<String, String> getMapOld() {
        Map<String, String> map = new HashMap<>();
        map.put(MLConstants.MLPC, ConfigConstants.MACHINELEARNINGSPARKMLMLPC);
        map.put(MLConstants.LIR, ConfigConstants.MACHINELEARNINGSPARKMLLOR);
        map.put(MLConstants.OVR, ConfigConstants.MACHINELEARNINGSPARKMLOVR);
        map.put(MLConstants.LSVC, ConfigConstants.MACHINELEARNINGSPARKMLLSVC);
        map.put(MLConstants.DNN, ConfigConstants.MACHINELEARNINGTENSORFLOWDNN);
        map.put(MLConstants.LIC, ConfigConstants.MACHINELEARNINGTENSORFLOWLIC);
        map.put(MLConstants.LSTM, ConfigConstants.MACHINELEARNINGTENSORFLOWPREDICTORLSTM);
        return map;
    }

    private Map<Pair<String, String>, String> getMap() {
        Map<Pair <String, String>, String> map = new HashMap<>();
        map.put(new ImmutablePair(MLConstants.SPARK, MLConstants.MLPC), ConfigConstants.MACHINELEARNINGSPARKMLMLPC);
        map.put(new ImmutablePair(MLConstants.SPARK, MLConstants.LIR), ConfigConstants.MACHINELEARNINGSPARKMLLOR);
        map.put(new ImmutablePair(MLConstants.SPARK, MLConstants.OVR), ConfigConstants.MACHINELEARNINGSPARKMLOVR);
        map.put(new ImmutablePair(MLConstants.SPARK, MLConstants.LSVC), ConfigConstants.MACHINELEARNINGSPARKMLLSVC);
        map.put(new ImmutablePair(MLConstants.TENSORFLOW, MLConstants.DNN), ConfigConstants.MACHINELEARNINGTENSORFLOWDNN);
        map.put(new ImmutablePair(MLConstants.TENSORFLOW, MLConstants.LIC), ConfigConstants.MACHINELEARNINGTENSORFLOWLIC);
        map.put(new ImmutablePair(MLConstants.TENSORFLOW, MLConstants.LSTM), ConfigConstants.MACHINELEARNINGTENSORFLOWPREDICTORLSTM);
        return map;
    }
    
    private Map<String, Pair<String, String>> getMapRev() {
        Map<Pair<String, String>, String> aMap = getMap();
        Map<String, Pair<String, String>> retMap = new HashMap<>();
        for (Entry<Pair<String, String>, String> entry : aMap.entrySet()) {
            retMap.put(entry.getValue(), entry.getKey());
        }
        return retMap;
    }

    private Map<String, String> getMlMap() {
        Map<String, String> map = new HashMap<>();
        map.put(MLConstants.SPARK, ConfigConstants.MACHINELEARNINGSPARKML);
        map.put(MLConstants.TENSORFLOW, ConfigConstants.MACHINELEARNINGTENSORFLOW);
        return map;
    }
    
    @Override
    public List<String> getSubComponents(Market market, ComponentData componentData) {
        List<String> subComponents = new ArrayList<>();
        Map<String, Pair<String, String>> revMap = getMapRev();
        Map<String, EvolveMLConfig> mlConfigs = getMLConfig(market, componentData);
        for (Entry<String, EvolveMLConfig> entry : mlConfigs.entrySet()) {
            EvolveMLConfig mlConfig = entry.getValue();
            if (mlConfig.getEnable()) {
                String key = entry.getKey();
                Pair<String, String> subComponent = revMap.get(key);
                subComponents.add(subComponent.getLeft() + " " + subComponent.getRight());
            }
        }
        return subComponents;
    }

    protected void subenable(Map<String, Object> valueMap, String subcomponent) {
        String[] pairArray = subcomponent.split(" ");
        Pair<String, String> pair = new ImmutablePair(pairArray[0], pairArray[1]);
        Map<Pair<String, String>, String> map = getMap();
        String key = map.get(pair);
        if (!map.containsKey(key)) {
            log.error("Key not found {}", key);
            return;
        }
        valueMap.put(key, Boolean.TRUE);
    }

    protected void subdisable(Map<String, Object> valueMap, String subcomponent) {
        String[] pair = subcomponent.split(" ");
        String ml = pair[0];
        Map<String, String> map = getMlMap();
        if (!map.containsKey(ml)) {
            log.error("Key not found {}", ml);
            return;
        }
        map.remove(ml);
        for (Entry<String, String> entry : map.entrySet()) {
            String disableKey = entry.getValue();
            valueMap.put(disableKey, Boolean.FALSE);
        }
    }

}
