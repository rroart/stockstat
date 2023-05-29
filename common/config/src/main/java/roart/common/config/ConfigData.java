package roart.common.config;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ConfigData {

    private ConfigMaps configMaps;

    private ConfigTreeMap configTreeMap;
    
    private Map<String, Object> configValueMap;
    
    private LocalDate date;
    
    private String market;
    
    private String mlmarket;

    private boolean dataset = false;

    public ConfigMaps getConfigMaps() {
        return configMaps;
    }

    public void setConfigMaps(ConfigMaps configMaps) {
        this.configMaps = configMaps;
    }

    public ConfigTreeMap getConfigTreeMap() {
        return configTreeMap;
    }

    public void setConfigTreeMap(ConfigTreeMap configTreeMap) {
        this.configTreeMap = configTreeMap;
    }

    public Map<String, Object> getConfigValueMap() {
        return configValueMap;
    }

    public void setConfigValueMap(Map<String, Object> configValueMap) {
        this.configValueMap = configValueMap;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public String getMlmarket() {
        return mlmarket;
    }

    public void setMlmarket(String mlmarket) {
        this.mlmarket = mlmarket;
    }

    public boolean isDataset() {
        return dataset;
    }

    public void setDataset(boolean dataset) {
        this.dataset = dataset;
    }

    public ConfigData copy() {
        ConfigData data = new ConfigData();
        data.setConfigValueMap(new HashMap<>(getConfigValueMap()));
        data.setConfigMaps(configMaps);
        data.setConfigTreeMap(configTreeMap);
        data.setDataset(dataset);
        data.setDate(date);
        data.setMarket(market);
        data.setMlmarket(mlmarket);
        return data;
    }
    
    public void mute() {
        configValueMap = Collections.unmodifiableMap(configValueMap);
    }
    
    public void unmute() {
        configValueMap = new HashMap(configValueMap);
    }
}
