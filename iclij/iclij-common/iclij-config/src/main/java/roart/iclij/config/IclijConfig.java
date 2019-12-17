package roart.iclij.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;

import roart.common.config.ConfigTreeMap;

public class IclijConfig {
    protected Logger log = LoggerFactory.getLogger(this.getClass());

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

    private String mlmarket;

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

    public boolean wantsFindProfitMLDynamic() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.FINDPROFITMLDYNAMIC);
    }

    public boolean wantsFindProfitMLMACD() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.FINDPROFITMLMACD);
    }

    public boolean wantsFindProfitMLRSI() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.FINDPROFITMLRSI);
    }

    public boolean wantsFindProfitMLATR() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.FINDPROFITMLATR);
    }

    public boolean wantsFindProfitMLCCI() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.FINDPROFITMLCCI);
    }

    public boolean wantsFindProfitMLSTOCH() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.FINDPROFITMLSTOCH);
    }

    public boolean wantsFindProfitMLMulti() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.FINDPROFITMLMULTI);
    }

    public boolean wantsFindProfitMLIndicator() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.FINDPROFITMLINDICATOR);
    }

    public String getFindProfitMLMACDMLConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.FINDPROFITMLMACDMLCONFIG);
    }

    public String getFindProfitMLRSIMLConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.FINDPROFITMLRSIMLCONFIG);
    }

    public String getFindProfitMLATRMLConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.FINDPROFITMLATRMLCONFIG);
    }

    public String getFindProfitMLCCIMLConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.FINDPROFITMLCCIMLCONFIG);
    }

    public String getFindProfitMLSTOCHMLConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.FINDPROFITMLSTOCHMLCONFIG);
    }

    public String getFindProfitMLMultiMLConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.FINDPROFITMLMULTIMLCONFIG);
    }

    public String getFindProfitMLIndicatorMLConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.FINDPROFITMLINDICATORMLCONFIG);
    }

    public String getFindProfitMLMACDEvolutionConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.FINDPROFITMLMACDEVOLUTIONCONFIG);
    }

    public String getFindProfitMLRSIEvolutionConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.FINDPROFITMLRSIEVOLUTIONCONFIG);
    }

    public String getFindProfitMLATREvolutionConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.FINDPROFITMLATREVOLUTIONCONFIG);
    }

    public String getFindProfitMLCCIEvolutionConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.FINDPROFITMLCCIEVOLUTIONCONFIG);
    }

    public String getFindProfitMLSTOCHEvolutionConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.FINDPROFITMLSTOCHEVOLUTIONCONFIG);
    }

    public String getFindProfitMLMultiEvolutionConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.FINDPROFITMLMULTIEVOLUTIONCONFIG);
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

    public String getFindProfitFuturedays() {
        return (String) getValueOrDefault(IclijConfigConstants.FINDPROFITFUTUREDAYS);
    }

    public String getFindProfitThreshold() {
        return (String) getValueOrDefault(IclijConfigConstants.FINDPROFITTHRESHOLD);
    }

    public double getFindProfitManualThreshold() {
        return (Double) getValueOrDefault(IclijConfigConstants.FINDPROFITMANUALTHRESHOLD);
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

    public boolean wantsImproveProfitMLRSI() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.IMPROVEPROFITMLRSI);
    }

    public boolean wantsImproveProfitMLATR() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.IMPROVEPROFITMLATR);
    }

    public boolean wantsImproveProfitMLCCI() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.IMPROVEPROFITMLCCI);
    }

    public boolean wantsImproveProfitMLSTOCH() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.IMPROVEPROFITMLSTOCH);
    }

    public boolean wantsImproveProfitMLMulti() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.IMPROVEPROFITMLMULTI);
    }

    public boolean wantsImproveProfitMLIndicator() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.IMPROVEPROFITMLINDICATOR);
    }

    public String getImproveProfitMLMACDMLConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.IMPROVEPROFITMLMACDMLCONFIG);
    }

    public String getImproveProfitMLRSIMLConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.IMPROVEPROFITMLRSIMLCONFIG);
    }

    public String getImproveProfitMLATRMLConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.IMPROVEPROFITMLATRMLCONFIG);
    }

    public String getImproveProfitMLCCIMLConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.IMPROVEPROFITMLCCIMLCONFIG);
    }

    public String getImproveProfitMLSTOCHMLConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.IMPROVEPROFITMLSTOCHMLCONFIG);
    }

    public String getImproveProfitMLMultiMLConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.IMPROVEPROFITMLMULTIMLCONFIG);
    }

    public String getImproveProfitMLIndicatorMLConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.IMPROVEPROFITMLINDICATORMLCONFIG);
    }

    public String getImproveProfitMLMACDEvolutionConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.IMPROVEPROFITMLMACDEVOLUTIONCONFIG);
    }

    public String getImproveProfitMLRSIEvolutionConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.IMPROVEPROFITMLRSIEVOLUTIONCONFIG);
    }

    public String getImproveProfitMLATREvolutionConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.IMPROVEPROFITMLATREVOLUTIONCONFIG);
    }

    public String getImproveProfitMLCCIEvolutionConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.IMPROVEPROFITMLCCIEVOLUTIONCONFIG);
    }

    public String getImproveProfitMLSTOCHEvolutionConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.IMPROVEPROFITMLSTOCHEVOLUTIONCONFIG);
    }

    public String getImproveProfitMLMultiEvolutionConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.IMPROVEPROFITMLMULTIEVOLUTIONCONFIG);
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

    public boolean wantsMachineLearningAutorun() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.MACHINELEARNINGAUTORUN);
    }

    public boolean wantsMachineLearningMLMACD() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.MACHINELEARNINGMLMACD);
    }

    public boolean wantsMachineLearningMLRSI() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.MACHINELEARNINGMLRSI);
    }

    public boolean wantsMachineLearningMLATR() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.MACHINELEARNINGMLATR);
    }

    public boolean wantsMachineLearningMLCCI() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.MACHINELEARNINGMLCCI);
    }

    public boolean wantsMachineLearningMLSTOCH() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.MACHINELEARNINGMLSTOCH);
    }

    public boolean wantsMachineLearningMLMulti() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.MACHINELEARNINGMLMULTI);
    }

    public boolean wantsMachineLearningMLIndicator() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.MACHINELEARNINGMLINDICATOR);
    }

    public String getMachineLearningMLMACDMLConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.MACHINELEARNINGMLMACDMLCONFIG);
    }

    public String getMachineLearningMLRSIMLConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.MACHINELEARNINGMLRSIMLCONFIG);
    }

    public String getMachineLearningMLATRMLConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.MACHINELEARNINGMLATRMLCONFIG);
    }

    public String getMachineLearningMLCCIMLConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.MACHINELEARNINGMLCCIMLCONFIG);
    }

    public String getMachineLearningMLSTOCHMLConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.MACHINELEARNINGMLSTOCHMLCONFIG);
    }

    public String getMachineLearningMLMultiMLConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.MACHINELEARNINGMLMULTIMLCONFIG);
    }

    public String getMachineLearningMLIndicatorMLConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.MACHINELEARNINGMLINDICATORMLCONFIG);
    }

    public String getMachineLearningMLMACDEvolutionConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.MACHINELEARNINGMLMACDEVOLUTIONCONFIG);
    }

    public String getMachineLearningMLRSIEvolutionConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.MACHINELEARNINGMLRSIEVOLUTIONCONFIG);
    }

    public String getMachineLearningMLATREvolutionConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.MACHINELEARNINGMLATREVOLUTIONCONFIG);
    }

    public String getMachineLearningMLCCIEvolutionConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.MACHINELEARNINGMLCCIEVOLUTIONCONFIG);
    }

    public String getMachineLearningMLSTOCHEvolutionConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.MACHINELEARNINGMLSTOCHEVOLUTIONCONFIG);
    }

    public String getMachineLearningMLMultiEvolutionConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.MACHINELEARNINGMLMULTIEVOLUTIONCONFIG);
    }

    public String getMachineLearningMLIndicatorEvolutionConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.MACHINELEARNINGMLINDICATOREVOLUTIONCONFIG);
    }

    public String getMachineLearningFuturedays() {
        return (String) getValueOrDefault(IclijConfigConstants.MACHINELEARNINGFUTUREDAYS);
    }

    public String getMachineLearningThreshold() {
        return (String) getValueOrDefault(IclijConfigConstants.MACHINELEARNINGTHRESHOLD);
    }

    public boolean wantsMachineLearningPredictor() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.MACHINELEARNINGPREDICTOR);
    }

    public String getMachineLearningPredictorMLConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.MACHINELEARNINGPREDICTORMLCONFIG);
    }

    public String getMachineLearningPredictorEvolutionConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.MACHINELEARNINGPREDICTOREVOLUTIONCONFIG);
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

    public boolean wantsEvolveAutorun() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.EVOLVEAUTORUN);
    }

    public int getEvolveFitnessMinimum() {
        return (Integer) getValueOrDefault(IclijConfigConstants.EVOLVEFITNESSMINIMUM);
    }

    public boolean wantsEvolveMLMACD() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.EVOLVEMLMACD);
    }

    public boolean wantsEvolveMLRSI() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.EVOLVEMLRSI);
    }

    public boolean wantsEvolveMLATR() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.EVOLVEMLATR);
    }

    public boolean wantsEvolveMLCCI() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.EVOLVEMLCCI);
    }

    public boolean wantsEvolveMLSTOCH() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.EVOLVEMLSTOCH);
    }

    public boolean wantsEvolveMLMulti() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.EVOLVEMLMULTI);
    }

    public boolean wantsEvolveMLIndicator() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.EVOLVEMLINDICATOR);
    }

    public String getEvolveMLMACDMLConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.EVOLVEMLMACDMLCONFIG);
    }

    public String getEvolveMLRSIMLConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.EVOLVEMLRSIMLCONFIG);
    }

    public String getEvolveMLATRMLConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.EVOLVEMLATRMLCONFIG);
    }

    public String getEvolveMLCCIMLConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.EVOLVEMLCCIMLCONFIG);
    }

    public String getEvolveMLSTOCHMLConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.EVOLVEMLSTOCHMLCONFIG);
    }

    public String getEvolveMLMultiMLConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.EVOLVEMLMULTIMLCONFIG);
    }

    public String getEvolveMLIndicatorMLConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.EVOLVEMLINDICATORMLCONFIG);
    }

    public String getEvolveMLMACDEvolutionConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.EVOLVEMLMACDEVOLUTIONCONFIG);
    }

    public String getEvolveMLRSIEvolutionConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.EVOLVEMLRSIEVOLUTIONCONFIG);
    }

    public String getEvolveMLATREvolutionConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.EVOLVEMLATREVOLUTIONCONFIG);
    }

    public String getEvolveMLCCIEvolutionConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.EVOLVEMLCCIEVOLUTIONCONFIG);
    }

    public String getEvolveMLSTOCHEvolutionConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.EVOLVEMLSTOCHEVOLUTIONCONFIG);
    }

    public String getEvolveMLMultiEvolutionConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.EVOLVEMLMULTIEVOLUTIONCONFIG);
    }

    public String getEvolveMLIndicatorEvolutionConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.EVOLVEMLINDICATOREVOLUTIONCONFIG);
    }

   public boolean wantsEvolveRecommender() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.EVOLVERECOMMENDER);
    }

    public boolean wantsEvolvePredictor() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.EVOLVEPREDICTOR);
    }

    public String getEvolvePredictorMLConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.EVOLVEPREDICTORMLCONFIG);
    }

    public String getEvolvePredictorEvolutionConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.EVOLVEPREDICTOREVOLUTIONCONFIG);
    }

    public String getEvolveFuturedays() {
        return (String) getValueOrDefault(IclijConfigConstants.EVOLVEFUTUREDAYS);
    }

    public String getEvolveThreshold() {
        return (String) getValueOrDefault(IclijConfigConstants.EVOLVETHRESHOLD);
    }

    public boolean wantDatasetML() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.DATASETML);
    }

    public String getDatasetMLEvolutionConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.DATASETMLEVOLUTIONCONFIG);
    }

    public String getDatasetIndicatorrecommenderEvolutionConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.DATASETINDICATORRECOMMENDEREVOLUTIONCONFIG);
    }

    public String getDatasetMLMLConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.DATASETMLMLCONFIG);
    }

    public boolean wantsDatasetAutorun() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.DATASETAUTORUN);
    }

    public int getDatasetFitnessMinimum() {
        return (Integer) getValueOrDefault(IclijConfigConstants.DATASETFITNESSMINIMUM);
    }

    public boolean wantsDatasetMLMACD() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.DATASETMLMACD);
    }

    public boolean wantsDatasetMLRSI() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.DATASETMLRSI);
    }

    public boolean wantsDatasetMLATR() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.DATASETMLATR);
    }

    public boolean wantsDatasetMLCCI() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.DATASETMLCCI);
    }

    public boolean wantsDatasetMLSTOCH() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.DATASETMLSTOCH);
    }

    public boolean wantsDatasetMLMulti() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.DATASETMLMULTI);
    }

    public boolean wantsDatasetMLIndicator() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.DATASETMLINDICATOR);
    }

    public String getDatasetMLMACDMLConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.DATASETMLMACDMLCONFIG);
    }

    public String getDatasetMLRSIMLConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.DATASETMLRSIMLCONFIG);
    }

    public String getDatasetMLATRMLConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.DATASETMLATRMLCONFIG);
    }

    public String getDatasetMLCCIMLConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.DATASETMLCCIMLCONFIG);
    }

    public String getDatasetMLSTOCHMLConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.DATASETMLSTOCHMLCONFIG);
    }

    public String getDatasetMLMultiMLConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.DATASETMLMULTIMLCONFIG);
    }

    public String getDatasetMLIndicatorMLConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.DATASETMLINDICATORMLCONFIG);
    }

    public String getDatasetMLMACDEvolutionConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.DATASETMLMACDEVOLUTIONCONFIG);
    }

    public String getDatasetMLRSIEvolutionConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.DATASETMLRSIEVOLUTIONCONFIG);
    }

    public String getDatasetMLATREvolutionConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.DATASETMLATREVOLUTIONCONFIG);
    }

    public String getDatasetMLCCIEvolutionConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.DATASETMLCCIEVOLUTIONCONFIG);
    }

    public String getDatasetMLSTOCHEvolutionConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.DATASETMLSTOCHEVOLUTIONCONFIG);
    }

    public String getDatasetMLMultiEvolutionConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.DATASETMLMULTIEVOLUTIONCONFIG);
    }

    public String getDatasetMLIndicatorEvolutionConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.DATASETMLINDICATOREVOLUTIONCONFIG);
    }

   public boolean wantsDatasetRecommender() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.DATASETRECOMMENDER);
    }

    public boolean wantsDatasetPredictor() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.DATASETPREDICTOR);
    }

    public String getDatasetPredictorMLConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.DATASETPREDICTORMLCONFIG);
    }

    public String getDatasetPredictorEvolutionConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.DATASETPREDICTOREVOLUTIONCONFIG);
    }

    public boolean wantCrosstestML() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.CROSSTESTML);
    }

    public String getCrosstestMLEvolutionConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.CROSSTESTMLEVOLUTIONCONFIG);
    }

    public String getCrosstestIndicatorrecommenderEvolutionConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.CROSSTESTINDICATORRECOMMENDEREVOLUTIONCONFIG);
    }

    public String getCrosstestMLMLConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.CROSSTESTMLMLCONFIG);
    }

    public boolean wantsCrosstestAutorun() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.CROSSTESTAUTORUN);
    }

    public int getCrosstestFitnessMinimum() {
        return (Integer) getValueOrDefault(IclijConfigConstants.CROSSTESTFITNESSMINIMUM);
    }

    public boolean wantsCrosstestMLMACD() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.CROSSTESTMLMACD);
    }

    public boolean wantsCrosstestMLRSI() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.CROSSTESTMLRSI);
    }

    public boolean wantsCrosstestMLATR() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.CROSSTESTMLATR);
    }

    public boolean wantsCrosstestMLCCI() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.CROSSTESTMLCCI);
    }

    public boolean wantsCrosstestMLSTOCH() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.CROSSTESTMLSTOCH);
    }

    public boolean wantsCrosstestMLMulti() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.CROSSTESTMLMULTI);
    }

    public boolean wantsCrosstestMLIndicator() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.CROSSTESTMLINDICATOR);
    }

    public String getCrosstestMLMACDMLConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.CROSSTESTMLMACDMLCONFIG);
    }

    public String getCrosstestMLRSIMLConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.CROSSTESTMLRSIMLCONFIG);
    }

    public String getCrosstestMLATRMLConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.CROSSTESTMLATRMLCONFIG);
    }

    public String getCrosstestMLCCIMLConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.CROSSTESTMLCCIMLCONFIG);
    }

    public String getCrosstestMLSTOCHMLConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.CROSSTESTMLSTOCHMLCONFIG);
    }

    public String getCrosstestMLMultiMLConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.CROSSTESTMLMULTIMLCONFIG);
    }

    public String getCrosstestMLIndicatorMLConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.CROSSTESTMLINDICATORMLCONFIG);
    }

    public String getCrosstestMLMACDEvolutionConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.CROSSTESTMLMACDEVOLUTIONCONFIG);
    }

    public String getCrosstestMLRSIEvolutionConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.CROSSTESTMLRSIEVOLUTIONCONFIG);
    }

    public String getCrosstestMLATREvolutionConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.CROSSTESTMLATREVOLUTIONCONFIG);
    }

    public String getCrosstestMLCCIEvolutionConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.CROSSTESTMLCCIEVOLUTIONCONFIG);
    }

    public String getCrosstestMLSTOCHEvolutionConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.CROSSTESTMLSTOCHEVOLUTIONCONFIG);
    }

    public String getCrosstestMLMultiEvolutionConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.CROSSTESTMLMULTIEVOLUTIONCONFIG);
    }

    public String getCrosstestMLIndicatorEvolutionConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.CROSSTESTMLINDICATOREVOLUTIONCONFIG);
    }

   public boolean wantsCrosstestRecommender() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.CROSSTESTRECOMMENDER);
    }

    public boolean wantsCrosstestPredictor() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.CROSSTESTPREDICTOR);
    }

    public String getCrosstestPredictorMLConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.CROSSTESTPREDICTORMLCONFIG);
    }

    public String getCrosstestPredictorEvolutionConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.CROSSTESTPREDICTOREVOLUTIONCONFIG);
    }

    public String getCrosstestFuturedays() {
        return (String) getValueOrDefault(IclijConfigConstants.CROSSTESTFUTUREDAYS);
    }

    public String getCrosstestThreshold() {
        return (String) getValueOrDefault(IclijConfigConstants.CROSSTESTTHRESHOLD);
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
