package roart.iclij.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import roart.common.util.JsonUtil;
import roart.common.util.TimeUtil;
import roart.db.model.Config;

public class ConfigItem {
    private LocalDate record;
    
    private LocalDate date;
    
    private String market;

    private String component;

    private String subcomponent;

    private String parameters;
    
    private String action;
    
    private String id;
    
    private String value;
    
    @Deprecated
    private Double score;

    private Boolean buy;
    
    public LocalDate getRecord() {
        return record;
    }

    public void setRecord(LocalDate record) {
        this.record = record;
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

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public String getSubcomponent() {
        return subcomponent;
    }

    public void setSubcomponent(String subcomponent) {
        this.subcomponent = subcomponent;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        //if (value != null && value.length() > 510) {
        //    value = value.substring(0, 510);
        //}
        this.value = value;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public Boolean getBuy() {
        return buy;
    }

    public void setBuy(Boolean buy) {
        this.buy = buy;
    }

    @Override
    public String toString() {
        return market + " " + component + " " + subcomponent + " " + parameters + " " + action + " " + record + " " + date + " " + id + " " + value + " " + score + "\n"; 
    }
    
    public void save() throws Exception {
        Config config = new Config();
        config.setAction(getAction());
        config.setBuy(getBuy());
        config.setComponent(getComponent());
        config.setDate(TimeUtil.convertDate(getDate()));
        config.setId(getId());
        config.setMarket(getMarket());
        config.setParameters(getParameters());
        config.setRecord(TimeUtil.convertDate(getRecord()));
        config.setScore(getScore());
        config.setSubcomponent(getSubcomponent());
        if (getValue() != null) {
        	config.setValue(JsonUtil.strip(getValue()).getBytes());
        }
        config.save();
    }
    
    public static List<ConfigItem> getAll() throws Exception {
        List<Config> configs = Config.getAll();
        List<ConfigItem> configItems = new ArrayList<>();
        for (Config config : configs) {
            ConfigItem memoryItem = getConfigItem(config);
            configItems.add(memoryItem);
        }
        return configItems;
    }

   public static List<ConfigItem> getAll(String market) throws Exception {
        List<Config> configs = Config.getAll(market);
        List<ConfigItem> configItems = new ArrayList<>();
        for (Config config : configs) {
            ConfigItem memoryItem = getConfigItem(config);
            configItems.add(memoryItem);
        }
        return configItems;
    }

   public static List<ConfigItem> getAll(String market, String action, String component, String subcomponent, String parameters, Date startDate, Date endDate) throws Exception {
       List<Config> configs = Config.getAll(market, action, component, subcomponent, parameters, startDate, endDate);
       List<ConfigItem> configItems = new ArrayList<>();
       for (Config config : configs) {
           ConfigItem memoryItem = getConfigItem(config);
           configItems.add(memoryItem);
       }
       return configItems;
   }

    private static ConfigItem getConfigItem(Config config) {
        ConfigItem configItem = new ConfigItem();
        configItem.setAction(config.getAction());
        configItem.setBuy(config.getBuy());
        configItem.setDate(TimeUtil.convertDate(config.getDate()));
        configItem.setId(config.getId());
        configItem.setComponent(config.getComponent());
        configItem.setMarket(config.getMarket());
        configItem.setRecord(TimeUtil.convertDate(config.getRecord()));
        configItem.setParameters(config.getParameters());
        configItem.setScore(config.getScore());
        configItem.setSubcomponent(config.getSubcomponent());
        if (config.getValue() != null) {
        	configItem.setValue(JsonUtil.strip(new String(config.getValue())));
        }
        return configItem;
    }

}
