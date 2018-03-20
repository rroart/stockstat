package roart.config;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class IclijConfig {

    public IclijConfig(IclijConfig config) {
        this.configTreeMap = config.configTreeMap;
        this.configValueMap = config.configValueMap;
        this.deflt = config.deflt;
        this.text = config.text;
        this.type = config.type;
    }

    public IclijConfig() {        
        // empty due to JSON
    }

    private ConfigTreeMap configTreeMap;

    private Map<String, Object> configValueMap;
    private Map<String, String> text = new HashMap<>();
    private Map<String, Object> deflt = new HashMap<>();
    private Map<String, Class> type = new HashMap<>();

    private LocalDate date;

    private String market;

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

    public Map<String, String> getText() {
        return text;
    }

    public void setText(Map<String, String> text) {
        this.text = text;
    }

    public Map<String, Object> getDeflt() {
        return deflt;
    }

    public void setDeflt(Map<String, Object> deflt) {
        this.deflt = deflt;
    }

    public Map<String, Class> getType() {
        return type;
    }

    public void setType(Map<String, Class> type) {
        this.type = type;
    }

    public boolean wantsAutorun() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.AUTORUN);
    }

    public boolean wantsMLMACD() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.RECOMMENDERMLMACD);
    }

    public boolean wantsMLIndicator() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.RECOMMENDERMLINDICATOR);
    }

    public boolean wantsRecommender() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.RECOMMENDERRECOMMENDER);
    }

     public boolean wantsPredictor() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.RECOMMENDERPREDICTOR);
    }

    public boolean wantsImproveProfit() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.VERIFICATIONIMPROVEPROFIT);
    }

    public int verificationDays() {
        return (Integer) getValueOrDefault(IclijConfigConstants.VERIFICATIONDAYS);
    }

    public int recommendTopBottom() {
        return (Integer) getValueOrDefault(IclijConfigConstants.VERIFICATIONRECOMMENDERTOPBOTTOM);
    }

    private Object getValueOrDefault(String key) {
        // jackson messes around here...
        if (configValueMap == null) {
            return null;
        }
        Object retVal = configValueMap.get(key);
        return Optional.ofNullable(retVal).orElse(deflt.get(key));
    }
}
