package roart.iclij.config;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;

import roart.common.config.ConfigTreeMap;

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
    private Map<String, String> conv = new HashMap<>();

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

    public Map<String, String> getConv() {
        return conv;
    }

    public void setConv(Map<String, String> conv) {
        this.conv = conv;
    }

    public Integer serverShutdownHour() {
        return (Integer) getValueOrDefault(IclijConfigConstants.MISCSHUTDOWNHOUR);
    }

    public double mpServerCpu() {
        return (Double) getValueOrDefault(IclijConfigConstants.MPSERVERCPU);
    }

    public double mpClientCpu() {
        return (Double) getValueOrDefault(IclijConfigConstants.MPCLIENTCPU);
    }

    public boolean wantsFindProfitAutorun() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.FINDPROFITAUTORUN);
    }

    public boolean wantsFindProfitMLMACD() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.FINDPROFITMLMACD);
    }

    public boolean wantsFindProfitMLIndicator() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.FINDPROFITMLINDICATOR);
    }

    public String getFindProfitMLMACDMLConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.FINDPROFITMLMACDMLCONFIG);
    }

    public String getFindProfitMLIndicatorMLConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.FINDPROFITMLINDICATORMLCONFIG);
    }

    public String getFindProfitMLMACDEvolutionConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.FINDPROFITMLMACDEVOLUTIONCONFIG);
    }

    public String getFindProfitMLIndicatorEvolutionConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.FINDPROFITMLINDICATOREVOLUTIONCONFIG);
    }

    public boolean wantsFindProfitRecommender() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.FINDPROFITRECOMMENDER);
    }

    public boolean wantsFindProfitPredictor() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.FINDPROFITPREDICTOR);
    }

    public String getFindProfitPredictorMLConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.FINDPROFITPREDICTORMLCONFIG);
    }

    public String getFindProfitPredictorEvolutionConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.FINDPROFITPREDICTOREVOLUTIONCONFIG);
    }

    public boolean wantsImproveProfitAutorun() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.IMPROVEPROFITAUTORUN);
    }

    public int getImproveProfitFitnessMinimum() {
        return (Integer) getValueOrDefault(IclijConfigConstants.IMPROVEPROFITFITNESSMINIMUM);
    }

    public boolean wantsImproveProfitMLMACD() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.IMPROVEPROFITMLMACD);
    }

    public boolean wantsImproveProfitMLIndicator() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.IMPROVEPROFITMLINDICATOR);
    }

    public String getImproveProfitMLMACDMLConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.IMPROVEPROFITMLMACDMLCONFIG);
    }

    public String getImproveProfitMLIndicatorMLConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.IMPROVEPROFITMLINDICATORMLCONFIG);
    }

    public String getImproveProfitMLMACDEvolutionConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.IMPROVEPROFITMLMACDEVOLUTIONCONFIG);
    }

    public String getImproveProfitMLIndicatorEvolutionConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.IMPROVEPROFITMLINDICATOREVOLUTIONCONFIG);
    }

   public boolean wantsImproveProfitRecommender() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.IMPROVEPROFITRECOMMENDER);
    }

    public boolean wantsImproveProfitPredictor() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.IMPROVEPROFITPREDICTOR);
    }

    public String getImproveProfitPredictorMLConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.IMPROVEPROFITPREDICTORMLCONFIG);
    }

    public String getImproveProfitPredictorEvolutionConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.IMPROVEPROFITPREDICTOREVOLUTIONCONFIG);
    }

    public boolean singlemarketEvolveFirstOnly() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.SINGLEMARKETEVOLVEFIRSTONLY);
    }

    public int singlemarketLoops() {
        return (Integer) getValueOrDefault(IclijConfigConstants.SINGLEMARKETLOOPS);
    }

    public int singlemarketLoopInterval() {
        return (Integer) getValueOrDefault(IclijConfigConstants.SINGLEMARKETLOOPINTERVAL);
    }

    @Deprecated
    public boolean wantsImproveProfit() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.VERIFICATIONIMPROVEPROFIT);
    }

    public boolean wantVerificationSave() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.VERIFICATIONSAVE);
    }

    public int verificationDays() {
        return (Integer) getValueOrDefault(IclijConfigConstants.VERIFICATIONDAYS);
    }

    public boolean verificationEvolveFirstOnly() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.VERIFICATIONEVOLVEFIRSTONLY);
    }

    public int verificationLoops() {
        return (Integer) getValueOrDefault(IclijConfigConstants.VERIFICATIONLOOPS);
    }

    public int verificationLoopInterval() {
        return (Integer) getValueOrDefault(IclijConfigConstants.VERIFICATIONLOOPINTERVAL);
    }

    public int recommendTopBottom() {
        return (Integer) getValueOrDefault(IclijConfigConstants.VERIFICATIONRECOMMENDERTOPBOTTOM);
    }

    public boolean wantEvolveRecommender() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.EVOLVEINDICATORRECOMMENDER);
    }

    public boolean wantEvolveML() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.EVOLVEML);
    }

    public String getEvolveMLEvolutionConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.EVOLVEMLEVOLUTIONCONFIG);
    }

    public String getEvolveIndicatorrecommenderEvolutionConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.EVOLVEINDICATORRECOMMENDEREVOLUTIONCONFIG);
    }

    public String getEvolveMLMLConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.EVOLVEMLMLCONFIG);
    }

    public Object getValueOrDefault(String key) {
        // jackson messes around here...
        if (configValueMap == null) {
            return null;
        }
        Object retVal = configValueMap.get(key);
        return Optional.ofNullable(retVal).orElse(deflt.get(key));
    }
}
