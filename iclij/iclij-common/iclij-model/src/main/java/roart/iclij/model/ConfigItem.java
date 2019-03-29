package roart.iclij.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import roart.common.util.TimeUtil;
import roart.db.model.Config;

public class ConfigItem {
    private LocalDate record;
    
    private LocalDate date;
    
    private String market;

    private String component;

    private String action;
    
    private String id;
    
    private String value;
    
    private Double score;

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
        this.value = value;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return market + " " + component + " " + action + " " + record + " " + date + " " + id + " " + value + " " + score + "\n"; 
    }
    
    public void save() throws Exception {
        Config config = new Config();
        config.setAction(getAction());
        config.setComponent(getComponent());
        config.setDate(TimeUtil.convertDate(getDate()));
        config.setId(getId());
        config.setMarket(getMarket());
        config.setRecord(TimeUtil.convertDate(getRecord()));
        config.setScore(getScore());
        config.setValue(getValue());
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

    private static ConfigItem getConfigItem(Config config) {
        ConfigItem configItem = new ConfigItem();
        configItem.setAction(config.getAction());
        configItem.setDate(TimeUtil.convertDate(config.getDate()));
        configItem.setId(config.getId());
        configItem.setComponent(config.getComponent());
        configItem.setMarket(config.getMarket());
        configItem.setRecord(TimeUtil.convertDate(config.getRecord()));
        configItem.setScore(config.getScore());
        configItem.setValue(config.getValue());
        return configItem;
    }

}
