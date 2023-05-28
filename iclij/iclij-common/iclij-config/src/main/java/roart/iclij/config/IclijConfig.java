package roart.iclij.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;

import roart.common.config.ConfigConstantMaps;
import roart.common.config.ConfigConstants;
import roart.common.config.ConfigData;
import roart.common.config.ConfigMaps;
import roart.common.config.ConfigTreeMap;
import roart.common.config.MyMyConfig;
import roart.config.IclijConfigConstantMaps;

@Configuration
public class IclijConfig extends MyMyConfig {
    protected Logger log = LoggerFactory.getLogger(this.getClass());

    protected static IclijConfig configInstance = null;

    protected static IclijXMLConfig instance = null;

    public IclijConfig(IclijConfig config) {
        super(config);
    }

    public static ConfigMaps instanceC() {
        return ConfigConstantMaps.instance();
    }

    public static ConfigMaps instanceI() {
        return IclijConfigConstantMaps.instance();
    }

    /*
    @Deprecated
    public IclijConfig getConfigInstance() {
        if (configInstance == null) {
            configInstance = new IclijConfig();
            if (instance == null) { 
                instance = IclijXMLConfig.instance(null, configMaps);
            }
        }
        return configInstance;
    }
     */

    /*
    public IclijConfig() {
        log.error("confMapps" + configMaps);
        instance = IclijXMLConfig.instance(this, configMaps);
    }
     */

    @Autowired
    public IclijConfig(ConfigMaps configMaps) {
        ConfigMaps myConfigMaps = instanceC();
        log.error("confMapps" + myConfigMaps);
        myConfigMaps.add(instanceI());
        myConfigMaps.keys(configMaps.keys);
        log.error("confMapps" + myConfigMaps);
        instance = IclijXMLConfig.instance(this, myConfigMaps);
        log.error("confMapps2" + myConfigMaps);
    }

    public IclijConfig(ConfigData data) {
        super(data);
    }

    public IclijConfig copy() {
        return new IclijConfig(getConfigData().copy());
    }

    public Integer serverShutdownHour() {
        return (Integer) getValueOrDefault(IclijConfigConstants.MISCSHUTDOWNHOUR);
    }

    public boolean populate() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.MISCPOPULATE);
    }

    /*
    public String getMyservices() {
        return (String) getValueOrDefault(IclijConfigConstants.MISCMYSERVICES);
    }

    public String getServices() {
        return (String) getValueOrDefault(IclijConfigConstants.MISCSERVICES);
    }

    public String getCommunications() {
        return (String) getValueOrDefault(IclijConfigConstants.MISCCOMMUNICATIONS);
    }

    public boolean wantCache() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.MISCCACHE);
    }

    public int getCacheTTL() {
        return (Integer) getValueOrDefault(IclijConfigConstants.MISCCACHETTL);
    }

    public String getInmemoryServer() {
        return (String) getValueOrDefault(IclijConfigConstants.MISCINMEMORYSERVER);
    }

    public String getInmemoryHazelcast() {
        return (String) getValueOrDefault(IclijConfigConstants.MISCINMEMORYHAZELCAST);
    }

    public String getInmemoryRedis() {
        return (String) getValueOrDefault(IclijConfigConstants.MISCINMEMORYREDIS);
    }

    public Double getAbnormalChange() {
        return (Double) getValueOrDefault(IclijConfigConstants.MISCABNORMALCHANGE);
    }
*/

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

    public boolean getFindProfitMemoryFilter() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.FINDPROFITMEMORYFILTER);
    }

    public boolean wantsFindProfitRerunSave() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.FINDPROFITRERUNSAVE);
    }

    public boolean wantsFindProfitUpdate() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.FINDPROFITUPDATE);
    }

    public boolean wantsImproveProfitAutorun() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.IMPROVEPROFITAUTORUN);
    }

    public boolean wantsImproveProfitUpdate() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.IMPROVEPROFITUPDATE);
    }

    public String getImproveProfitEvolutionConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.IMPROVEPROFITEVOLUTIONCONFIG);
    }

    public String getImproveProfitMLConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.IMPROVEPROFITMLCONFIG);
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

    public boolean wantsImproveFilterAutorun() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.IMPROVEFILTERAUTORUN);
    }

    public int getImproveFilterFitnessMinimum() {
        return (Integer) getValueOrDefault(IclijConfigConstants.IMPROVEFILTERFITNESSMINIMUM);
    }

    public boolean wantsImproveFilterMLMACD() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.IMPROVEFILTERMLMACD);
    }

    public boolean wantsImproveFilterMLRSI() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.IMPROVEFILTERMLRSI);
    }

    public boolean wantsImproveFilterMLATR() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.IMPROVEFILTERMLATR);
    }

    public boolean wantsImproveFilterMLCCI() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.IMPROVEFILTERMLCCI);
    }

    public boolean wantsImproveFilterMLSTOCH() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.IMPROVEFILTERMLSTOCH);
    }

    public boolean wantsImproveFilterMLMulti() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.IMPROVEFILTERMLMULTI);
    }

    public boolean wantsImproveFilterMLIndicator() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.IMPROVEFILTERMLINDICATOR);
    }

    public String getImproveFilterMLMACDMLConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.IMPROVEFILTERMLMACDMLCONFIG);
    }

    public String getImproveFilterMLRSIMLConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.IMPROVEFILTERMLRSIMLCONFIG);
    }

    public String getImproveFilterMLATRMLConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.IMPROVEFILTERMLATRMLCONFIG);
    }

    public String getImproveFilterMLCCIMLConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.IMPROVEFILTERMLCCIMLCONFIG);
    }

    public String getImproveFilterMLSTOCHMLConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.IMPROVEFILTERMLSTOCHMLCONFIG);
    }

    public String getImproveFilterMLMultiMLConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.IMPROVEFILTERMLMULTIMLCONFIG);
    }

    public String getImproveFilterMLIndicatorMLConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.IMPROVEFILTERMLINDICATORMLCONFIG);
    }

    public String getImproveFilterMLMACDEvolutionConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.IMPROVEFILTERMLMACDEVOLUTIONCONFIG);
    }

    public String getImproveFilterMLRSIEvolutionConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.IMPROVEFILTERMLRSIEVOLUTIONCONFIG);
    }

    public String getImproveFilterMLATREvolutionConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.IMPROVEFILTERMLATREVOLUTIONCONFIG);
    }

    public String getImproveFilterMLCCIEvolutionConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.IMPROVEFILTERMLCCIEVOLUTIONCONFIG);
    }

    public String getImproveFilterMLSTOCHEvolutionConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.IMPROVEFILTERMLSTOCHEVOLUTIONCONFIG);
    }

    public String getImproveFilterMLMultiEvolutionConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.IMPROVEFILTERMLMULTIEVOLUTIONCONFIG);
    }

    public String getImproveFilterMLIndicatorEvolutionConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.IMPROVEFILTERMLINDICATOREVOLUTIONCONFIG);
    }

    public boolean wantsImproveFilterRecommender() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.IMPROVEFILTERRECOMMENDER);
    }

    public boolean wantsImproveFilterPredictor() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.IMPROVEFILTERPREDICTOR);
    }

    public String getImproveFilterPredictorMLConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.IMPROVEFILTERPREDICTORMLCONFIG);
    }

    public String getImproveFilterPredictorEvolutionConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.IMPROVEFILTERPREDICTOREVOLUTIONCONFIG);
    }

    public boolean wantsImproveAbovebelowAutorun() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.IMPROVEABOVEBELOWAUTORUN);
    }

    public String getImproveAbovebelowEvolutionConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.IMPROVEABOVEBELOWEVOLUTIONCONFIG);
    }

    public int getImproveAbovebelowFitnessMinimum() {
        return (Integer) getValueOrDefault(IclijConfigConstants.IMPROVEABOVEBELOWFITNESSMINIMUM);
    }

    public boolean wantsMachineLearningAutorun() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.MACHINELEARNINGAUTORUN);
    }

    public String getMachineLearningEvolutionConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.MACHINELEARNINGEVOLUTIONCONFIG);
    }

    public String getMachineLearningMLConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.MACHINELEARNINGMLCONFIG);
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

    public boolean wantsMachineLearningUpdate() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.MACHINELEARNINGUPDATE);
    }

    public boolean wantsSimulateInvestAutorun() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.SIMULATEINVESTAUTORUN);
    }

    public boolean wantsSimulateInvestConfidence() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.SIMULATEINVESTCONFIDENCE);
    }

    public double getSimulateInvestConfidenceValue() {
        return (Double) getValueOrDefault(IclijConfigConstants.SIMULATEINVESTCONFIDENCEVALUE);
    }

    public int getSimulateInvestConfidenceFindtimes() {
        return (Integer) getValueOrDefault(IclijConfigConstants.SIMULATEINVESTCONFIDENCEFINDTIMES);
    }

    public boolean getSimulateInvestAboveBelow() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.SIMULATEINVESTABOVEBELOW);
    }

    public boolean wantsSimulateInvestConfidenceHoldIncrease() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.SIMULATEINVESTCONFIDENCEHOLDINCREASE);
    }

    public boolean wantsSimulateInvestNoConfidenceHoldIncrease() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.SIMULATEINVESTNOCONFIDENCEHOLDINCREASE);
    }

    public boolean wantsSimulateInvestConfidenceTrendIncrease() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.SIMULATEINVESTCONFIDENCETRENDINCREASE);
    }

    public int wantsSimulateInvestConfidenceTrendIncreaseTimes() {
        return (Integer) getValueOrDefault(IclijConfigConstants.SIMULATEINVESTCONFIDENCETRENDINCREASETIMES);
    }

    public boolean wantsSimulateInvestNoConfidenceTrendDecrease() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.SIMULATEINVESTNOCONFIDENCETRENDDECREASE);
    }

    public int wantsSimulateInvestNoConfidenceTrendDecreaseTimes() {
        return (Integer) getValueOrDefault(IclijConfigConstants.SIMULATEINVESTNOCONFIDENCETRENDDECREASETIMES);
    }

    public boolean wantsSimulateInvestStoploss() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.SIMULATEINVESTSTOPLOSS);
    }

    public double getSimulateInvestStoplossValue() {
        return (Double) getValueOrDefault(IclijConfigConstants.SIMULATEINVESTSTOPLOSSVALUE);
    }

    public boolean wantsSimulateInvestIntervalStoploss() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.SIMULATEINVESTINTERVALSTOPLOSS);
    }

    public double getSimulateInvestIntervalStoplossValue() {
        return (Double) getValueOrDefault(IclijConfigConstants.SIMULATEINVESTINTERVALSTOPLOSSVALUE);
    }

    public boolean wantsSimulateInvestIndicatorReverse() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.SIMULATEINVESTINDICATORREVERSE);
    }

    public boolean wantsSimulateInvestIndicatorPure() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.SIMULATEINVESTINDICATORPURE);
    }

    public boolean wantsSimulateInvestIndicatorRebase() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.SIMULATEINVESTINDICATORREBASE);
    }

    public boolean wantsSimulateInvestIndicatorDirection() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.SIMULATEINVESTINDICATORDIRECTION);
    }

    public boolean wantsSimulateInvestIndicatorDirectionUp() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.SIMULATEINVESTINDICATORDIRECTIONUP);
    }

    public boolean wantsSimulateInvestMLDate() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.SIMULATEINVESTMLDATE);
    }

    public int getSimulateInvestStocks() {
        return (Integer) getValueOrDefault(IclijConfigConstants.SIMULATEINVESTSTOCKS);
    }

    public boolean wantsSimulateInvestBuyweight() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.SIMULATEINVESTBUYWEIGHT);
    }

    public boolean wantsSimulateInvestInterpolate() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.SIMULATEINVESTINTERPOLATE);
    }

    public int getSimulateInvestInterval() {
        return (Integer) getValueOrDefault(IclijConfigConstants.SIMULATEINVESTINTERVAL);
    }

    public boolean wantsSimulateInvestIntervalWhole() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.SIMULATEINVESTINTERVALWHOLE);
    }

    public int getSimulateInvestAdviser() {
        return (Integer) getValueOrDefault(IclijConfigConstants.SIMULATEINVESTADVISER);
    }

    @JsonIgnore
    public int getSimulateInvestPeriod() {
        return (Integer) getValueOrDefault(IclijConfigConstants.SIMULATEINVESTPERIOD);
    }

    public int getSimulateInvestDelay() {
        return (Integer) getValueOrDefault(IclijConfigConstants.SIMULATEINVESTDELAY);
    }

    public String getSimulateInvestVolumelimits() {
        return (String) getValueOrDefault(IclijConfigConstants.SIMULATEINVESTVOLUMELIMITS);
    }

    public String getSimulateInvestFilters() {
        return (String) getValueOrDefault(IclijConfigConstants.SIMULATEINVESTFILTERS);
    }

    public boolean getSimulateInvestImproveFilters() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.SIMULATEINVESTIMPROVEFILTERS);
    }

    public String getSimulateInvestStartdate() {
        return (String) getValueOrDefault(IclijConfigConstants.SIMULATEINVESTSTARTDATE);
    }

    public String getSimulateInvestEnddate() {
        return (String) getValueOrDefault(IclijConfigConstants.SIMULATEINVESTENDDATE);
    }

    public int getSimulateInvestDay() {
        return (Integer) getValueOrDefault(IclijConfigConstants.SIMULATEINVESTDAY);
    }

    public int getSimulateInvestFutureCount() {
        return (Integer) getValueOrDefault(IclijConfigConstants.SIMULATEINVESTFUTURECOUNT);
    }

    public int getSimulateInvestFutureTime() {
        return (Integer) getValueOrDefault(IclijConfigConstants.SIMULATEINVESTFUTURETIME);
    }

    public int getAutoSimulateInvestInterval() {
        return (Integer) getValueOrDefault(IclijConfigConstants.AUTOSIMULATEINVESTINTERVAL);
    }

    public boolean getAutoSimulateInvestIntervalwhole() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.AUTOSIMULATEINVESTINTERVALWHOLE);
    }

    public int getAutoSimulateInvestPeriod() {
        return (Integer) getValueOrDefault(IclijConfigConstants.AUTOSIMULATEINVESTPERIOD);
    }

    public int getAutoSimulateInvestLastCount() {
        return (Integer) getValueOrDefault(IclijConfigConstants.AUTOSIMULATEINVESTLASTCOUNT);
    }

    public double getAutoSimulateInvestDelLimit() {
        return (Double) getValueOrDefault(IclijConfigConstants.AUTOSIMULATEINVESTDELLIMIT);
    }

    public double getAutoSimulateInvestScoreLimit() {
        return (Double) getValueOrDefault(IclijConfigConstants.AUTOSIMULATEINVESTSCORELIMIT);
    }

    public double getAutoSimulateInvestAutoScoreLimit() {
        return (Double) getValueOrDefault(IclijConfigConstants.AUTOSIMULATEINVESTAUTOSCORELIMIT);
    }

    public boolean getAutoSimulateInvestKeepAdviser() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.AUTOSIMULATEINVESTKEEPADVISER);
    }

    public double getAutoSimulateInvestKeepAdviserLimit() {
        return (Double) getValueOrDefault(IclijConfigConstants.AUTOSIMULATEINVESTKEEPADVISERLIMIT);
    }

    public String getAutoSimulateInvestStartdate() {
        return (String) getValueOrDefault(IclijConfigConstants.AUTOSIMULATEINVESTSTARTDATE);
    }

    public String getAutoSimulateInvestEnddate() {
        return (String) getValueOrDefault(IclijConfigConstants.AUTOSIMULATEINVESTENDDATE);
    }

    public boolean getAutoSimulateInvestVote() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.AUTOSIMULATEINVESTVOTE);
    }

    public String getAutoSimulateInvestVolumelimits() {
        return (String) getValueOrDefault(IclijConfigConstants.AUTOSIMULATEINVESTVOLUMELIMITS);
    }

    public String getAutoSimulateInvestFilters() {
        return (String) getValueOrDefault(IclijConfigConstants.AUTOSIMULATEINVESTFILTERS);
    }

    public boolean getAutoSimulateInvestImproveFilters() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.AUTOSIMULATEINVESTIMPROVEFILTERS);
    }

    public int getAutoSimulateInvestFutureCount() {
        return (Integer) getValueOrDefault(IclijConfigConstants.AUTOSIMULATEINVESTFUTURECOUNT);
    }

    public int getAutoSimulateInvestFutureTime() {
        return (Integer) getValueOrDefault(IclijConfigConstants.AUTOSIMULATEINVESTFUTURETIME);
    }

    public boolean wantsImproveSimulateInvestAutorun() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.IMPROVESIMULATEINVESTAUTORUN);
    }

    public String getImproveSimulateInvestEvolutionConfig() {
        return (String)getValueOrDefault(IclijConfigConstants.IMPROVESIMULATEINVESTEVOLUTIONCONFIG);
    }

    public String getImproveAutoSimulateInvestEvolutionConfig() {
        return (String)getValueOrDefault(IclijConfigConstants.IMPROVEAUTOSIMULATEINVESTEVOLUTIONCONFIG);
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

    public boolean singlemarketRerun() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.SINGLEMARKETRERUN);
    }

    @Deprecated
    public boolean wantsImproveProfit() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.VERIFICATIONIMPROVEPROFIT);
    }

    @Deprecated
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

    public boolean verificationRerun() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.VERIFICATIONRERUN);
    }

    public int getEvolveGA() {
        return (Integer) getValueOrDefault(IclijConfigConstants.EVOLVEGA);
    }

    public boolean wantEvolveRecommender() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.EVOLVEINDICATORRECOMMENDER);
    }

    public boolean wantEvolveML() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.EVOLVEML);
    }

    /*
    public String getEvolveMLEvolutionConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.EVOLVEMLEVOLUTIONCONFIG);
    }

    public String getEvolveIndicatorrecommenderEvolutionConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.EVOLVEINDICATORRECOMMENDEREVOLUTIONCONFIG);
    }
*/

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

    public String getDatasetEvolutionConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.DATASETEVOLUTIONCONFIG);
    }

    public String getDatasetIndicatorrecommenderEvolutionConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.DATASETINDICATORRECOMMENDEREVOLUTIONCONFIG);
    }

    /*
    public String getDatasetMLConfig() {
        return (String) getValueOrDefault(IclijConfigConstants.DATASETMLCONFIG);
    }
*/
    
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

    public boolean wantsDatasetUpdate() {
        return (Boolean) getValueOrDefault(IclijConfigConstants.DATASETUPDATE);
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

    public boolean wantsIclijSchedule() {
        return (Boolean) getNotEmptyValueOrDefault(IclijConfigConstants.ICLIJSCHEDULE);
    }

    public Object getValueOrDefault(String key) {
        // jackson messes around here...
        if (getConfigData().getConfigValueMap() == null) {
            return null;
        }
        Object retVal = getConfigData().getConfigValueMap().get(key);
        retVal = Optional.ofNullable(retVal).orElse(getConfigData().getConfigMaps().deflt.get(key));
        Class aClass = getConfigData().getConfigMaps().map.get(key);
        if (aClass != null && aClass == Double.class && retVal != null && retVal.getClass() == Integer.class) {
            Integer i = (Integer) retVal;
            retVal = i.doubleValue();
        }
        return retVal;
    }

    @JsonIgnore
    public Object getNotEmptyValueOrDefault(String key) {
        Object retVal = getConfigData().getConfigValueMap().get(key);
        System.out.println("r " + getConfigData().getConfigValueMap().keySet());
        System.out.println("r " + getConfigData().getConfigMaps().deflt.keySet());
        System.out.println("r " + retVal + " " + getConfigData().getConfigMaps().deflt.get(key));
        //System.out.println("r " + retVal + " " + deflt.get(key));
        if (retVal instanceof String) {
            String str = (String) retVal;
            if (str.isEmpty()) {
                retVal = null;
            }
        }
        return Optional.ofNullable(retVal).orElse(getConfigData().getConfigMaps().deflt.get(key));
    }

}
