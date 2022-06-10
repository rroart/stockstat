package roart.common.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;

import roart.common.util.JsonUtil;

import java.util.Optional;

public class MyMyConfig extends MyConfig {
    protected Logger log = LoggerFactory.getLogger(this.getClass());

    public MyMyConfig() {
    }
    
    public MyMyConfig(MyConfig config) {
        setConfigTreeMap(config.getConfigTreeMap());
        setConfigValueMap(new HashMap<>(config.getConfigValueMap()));
        setDeflt(config.getDeflt());
        setText(config.getText());
        setRange(config.getRange());
        setType(config.getType());
        fixIntegerDouble();
        this.mydate = config.mydate;
        this.mymarket = config.mymarket;
        this.mlmarket = config.mlmarket;
        this.dataset = config.dataset;
    }

    private void fixIntegerDouble() {
        for(Entry<String, Object> entry : getConfigValueMap().entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value == null) {
                //System.out.println("Null val " + key);
                continue;
            }
            Class classType = getType().get(key);
            if (classType == null) {
                System.out.println("Null class " + key);
                continue;
            }
                    
            //System.out.println("k " + key + " " + value + " " +classType);
            if (value.getClass().isAssignableFrom(Integer.class) && classType.isAssignableFrom(Double.class)) {
                getConfigValueMap().put(key, Double.valueOf((Integer)value));
            }
        }
    }

    public MyMyConfig copy() {
        MyMyConfig newConfig = new MyMyConfig(this);
        newConfig.setConfigValueMap(new HashMap<>(getConfigValueMap()));
        return newConfig;
    }

    /*
    	public void setDays(Integer integer) {
    	    mydays = integer;
    	}
     */
    public int getDays() {
        return (Integer) getValueOrDefault(ConfigConstants.MISCMYDAYS);
    }

    /*
    	public void setTopBottom(Integer integer) {
    	    mytopbottom = integer;
    	}
     */
    public int getTopBottom() {
        return (Integer) getValueOrDefault(ConfigConstants.MISCMYTOPBOTTOM);
    }

    /*
    	public void setTableDays(Integer integer) {
    	    mytabledays = integer;
    	}
     */
    public int getTableDays() {
        return (Integer) getValueOrDefault(ConfigConstants.MISCMYTABLEDAYS);
    }

    /*
    	public void setTableIntervalDays(Integer integer) {
    	    mytableintervaldays = integer;
    	}
     */
    public int getTableIntervalDays() {
        return (Integer) getValueOrDefault(ConfigConstants.MISCMYTABLEINTERVALDAYS);
    }

    /*
    	public void setTableMoveIntervalDays(Integer integer) {
    	    mytablemoveintervaldays = integer;
    	}
     */
    public int getTableMoveIntervalDays() {
        return (Integer) getValueOrDefault(ConfigConstants.MISCMYTABLEMOVEINTERVALDAYS);
    }

    /*
    	public void setEqualize(Boolean integer) {
    	    myequalize = integer;
    	}
     */
    public boolean isEqualize() {
        return (Boolean) getValueOrDefault(ConfigConstants.MISCMYEQUALIZE);
    }

    /*
        public int testRecommendFactor() {
            return (int) getValueOrDefault(ConfigConstants.TESTRECOMMENDFACTOR);
        }
     */
    /*
    	public void setMoveEnabled(Boolean bool) {
    	    moveEnabled = bool;
    	}
     */

    public boolean wantIndicators() {
        return (Boolean) getValueOrDefault(ConfigConstants.INDICATORS);	    
    }

    public boolean isMoveEnabled() {
        return (Boolean) getValueOrDefault(ConfigConstants.INDICATORSMOVE)
                && wantIndicators();
    }

    /*
    	public void setMACDEnabled(Boolean bool) {
    	    macdEnabled = bool;
    	}
     */
    public boolean isMACDEnabled() {
        return (Boolean) getValueOrDefault(ConfigConstants.INDICATORSMACD)
                && wantIndicators();
    }

    /*
    	public void setMACDDeltaEnabled(Boolean bool) {
    	    macdDeltaEnabled = bool;
    	}
     */
    /*
    	public void setMACDHistogramDeltaEnabled(Boolean bool) {
    	    macdHistogramDeltaEnabled = bool;
    	}
     */
    public boolean isCCIDeltaEnabled() {
        return (Boolean) getValueOrDefault(ConfigConstants.INDICATORSCCIDELTA);
    }

    /*
    	public void setCCIDeltaEnabled(Boolean bool) {
    	    cciDeltaEnabled = bool;
    	}
     */
    public boolean isATRDeltaEnabled() {
        return (Boolean) getValueOrDefault(ConfigConstants.INDICATORSATRDELTA);
    }

    /*
    	public void setATRDeltaEnabled(Boolean bool) {
    	    atrDeltaEnabled = bool;
    	}
     */
    public boolean isSTOCHDeltaEnabled() {
        return (Boolean) getValueOrDefault(ConfigConstants.INDICATORSSTOCHSTOCHDELTA);
    }

    /*
    	public void setSTOCHDeltaEnabled(Boolean bool) {
    	    stochDeltaEnabled = bool;
    	}
     */
    public boolean isRSIDeltaEnabled() {
        return (Boolean) getValueOrDefault(ConfigConstants.INDICATORSRSIDELTA);
    }

    /*
    	public void setRSIDeltaEnabled(Boolean bool) {
    	    rsiDeltaEnabled = bool;
    	}
     */
    public boolean isSTOCHRSIDeltaEnabled() {
        return (Boolean) getValueOrDefault(ConfigConstants.INDICATORSSTOCHRSI);
    }

    /*
    	public void setSTOCHRSIDeltaEnabled(Boolean bool) {
    	    stochrsiDeltaEnabled = bool;
    	}
     */
    public boolean isMACDDeltaEnabled() {
        return (Boolean) getValueOrDefault(ConfigConstants.INDICATORSMACDMACDMACDDELTA);
    }

    public boolean isMACDHistogramDeltaEnabled() {
        return (Boolean) getValueOrDefault(ConfigConstants.INDICATORSMACDMACDHISTOGRAMDELTA);
    }

    public boolean isMACDSignalDeltaEnabled() {
        return (Boolean) getValueOrDefault(ConfigConstants.INDICATORSMACDMACDSIGNALDELTA);
    }

    /*
    	public void setRSIEnabled(Boolean bool) {
    	    rsiEnabled = bool;
    	}
     */
    public boolean isRSIEnabled() {
        return (Boolean) getValueOrDefault(ConfigConstants.INDICATORSRSI)
                && wantIndicators();
    }

    /*
    	public void setSTOCHRSIEnabled(Boolean bool) {
    	    stochrsiEnabled = bool;
    	}
     */
    public boolean isSTOCHRSIEnabled() {
        return (Boolean) getValueOrDefault(ConfigConstants.INDICATORSSTOCHRSI)
                && wantIndicators();
    }

    /*
    	public void setCCIEnabled(Boolean bool) {
    	    cciEnabled = bool;
    	}
     */
    public boolean isCCIEnabled() {
        return (Boolean) getValueOrDefault(ConfigConstants.INDICATORSCCI)
                && wantIndicators();
    }

    /*
    	public void setATREnabled(Boolean bool) {
    	    atrEnabled = bool;
    	}
     */
    public boolean isATREnabled() {
        return (Boolean) getValueOrDefault(ConfigConstants.INDICATORSATR)
                && wantIndicators();
    }

    /*
    	public void setSTOCHEnabled(Boolean bool) {
    	    stochEnabled = bool;
    	}
     */
    public boolean isSTOCHEnabled() {		
        return (Boolean) getValueOrDefault(ConfigConstants.INDICATORSSTOCH)
                && wantIndicators();
    }

    /*
    	public void setGraphEqualize(Boolean integer) {
    	    mygraphequalize = integer;
    	}
     */
    public boolean isGraphEqualize() {
        return (Boolean) getValueOrDefault(ConfigConstants.MISCMYGRAPHEQUALIZE);
    }

    /*
    	public void setGraphEqUnify(Boolean integer) {
    	    mygraphequalizeunify = integer;
    	}
     */
    public boolean isGraphEqUnify() {
        return (Boolean) getValueOrDefault(ConfigConstants.MISCMYGRAPHEQUALIZEUNIFY);
    }

    public int getMACDDeltaDays() {
        return (Integer) getValueOrDefault(ConfigConstants.INDICATORSMACDMACDMACDDELTADAYS);
    }

    /*
    public void setMACDHistogramDeltaDays(Integer integer) {
    	this.macdHistogramDeltaDays = integer;
    }
     */
    public int getMACDHistogramDeltaDays() {
        return (Integer) getValueOrDefault(ConfigConstants.INDICATORSMACDMACDHISTOGRAMDELTADAYS);
    }

    public int getMACDSignalDeltaDays() {
        return (Integer) getValueOrDefault(ConfigConstants.INDICATORSMACDMACDSIGNALDELTADAYS);
    }

    /*
    public void setMACDDeltaDays(Integer integer) {
    	this.macdDeltaDays = integer;
    }
     */
    public int getRSIDeltaDays() {
        return (Integer) getValueOrDefault(ConfigConstants.INDICATORSRSIDELTADAYS);
    }

    /*
    public void setRSIDeltaDays(Integer integer) {
    	this.rsiDeltaDays = integer;
    }
     */
    public int getSTOCHRSIDeltaDays() {
        return (Integer) getValueOrDefault(ConfigConstants.INDICATORSSTOCHRSIDELTADAYS);
    }

    /*
    public void setSTOCHRSIDeltaDays(Integer integer) {
    	this.stochrsiDeltaDays = integer;
    }
     */
    public int getCCIDeltaDays() {
        return (Integer) getValueOrDefault(ConfigConstants.INDICATORSCCIDELTADAYS);
    }

    /*
    public void setCCIDeltaDays(Integer integer) {
    	this.cciDeltaDays = integer;
    }
     */
    public int getATRDeltaDays() {
        return (Integer) getValueOrDefault(ConfigConstants.INDICATORSATRDELTADAYS);
    }

    /*  
    public void setATRDeltaDays(Integer integer) {
    	this.atrDeltaDays = integer;
    }
     */
    public int getSTOCHDeltaDays() {
        return (Integer) getValueOrDefault(ConfigConstants.INDICATORSSTOCHSTOCHDELTADAYS);
    }

    /*
    	public void setSTOCHDeltaDays(Integer integer) {
    		this.stochDeltaDays = integer;
    	}
     */
    /**
     *  days before positive/negative change
     * @return
     */
    public int getMACDDaysBeforeZero() {
        return (Integer) getValueOrDefault(ConfigConstants.AGGREGATORSMLMACDDAYSBEFOREZERO);
    }

    /**
     *  days after positive/negative change
     * @return
     */
    public int getMACDDaysAfterZero() {
        return (Integer) getValueOrDefault(ConfigConstants.AGGREGATORSMLMACDDAYSAFTERZERO);
    }

    public  boolean wantMACDScore() {
        return (Boolean) getValueOrDefault(ConfigConstants.INDICATORSMACDRECOMMEND);
    }

    public  boolean wantRSIScore() {
        return (Boolean) getValueOrDefault(ConfigConstants.INDICATORSRSIRECOMMEND);
    }

    public void disableML() {
        getConfigValueMap().put(ConfigConstants.MACHINELEARNING, Boolean.FALSE);
    }

    public  boolean wantML() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNING);
    }

    public  boolean wantMLMP() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGMP);
    }

    public double getMLMPCpu() {
        return (Double) getValueOrDefault(ConfigConstants.MACHINELEARNINGMPCPU);
    }

    public  boolean wantMLDynamic() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGMLDYNAMIC)
                && wantML();
    }

    public  boolean wantMLSpark() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGSPARKML)
                && wantML();
    }

    public  boolean wantMLTensorflow() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGTENSORFLOW)
                && wantML();
    }

    public  boolean wantMLPytorch() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGPYTORCH)
                && wantML();
    }

    public  boolean wantMLGem() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGGEM)
                && wantML();
    }

    public  boolean wantSparkMLPC() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGSPARKMLMLPC)
                && wantMLSpark();
    }

    public  boolean wantSparkLOR() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGSPARKMLLOR)
                && wantMLSpark();
    }

    public  boolean wantSparkOVR() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGSPARKMLOVR)
                && wantMLSpark();
    }

    public  boolean wantSparkLSVC() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGSPARKMLLSVC)
                && wantMLSpark();
    }

    public  boolean wantTensorflowDNN() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGTENSORFLOWDNN)
                && wantMLTensorflow();
    }

    public  boolean wantTensorflowLIC() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGTENSORFLOWLIC)
                && wantMLTensorflow();
    }

    public  boolean wantTensorflowMLP() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGTENSORFLOWMLP)
                && wantMLTensorflow();
    }

    public  boolean wantTensorflowCNN() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGTENSORFLOWCNN)
                && wantMLTensorflow();
    }

    public  boolean wantTensorflowCNN2() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGTENSORFLOWCNN2)
                && wantMLTensorflow();
    }

    public  boolean wantTensorflowRNN() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGTENSORFLOWRNN)
                && wantMLTensorflow();
    }

    public  boolean wantTensorflowLSTM() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGTENSORFLOWLSTM)
                && wantMLTensorflow();
    }

    public  boolean wantTensorflowGRU() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGTENSORFLOWGRU)
                && wantMLTensorflow();
    }

    public  boolean wantPytorchMLP() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGPYTORCHMLP)
                && wantMLPytorch();
    }

    public  boolean wantPytorchCNN() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGPYTORCHCNN)
                && wantMLPytorch();
    }

    public  boolean wantPytorchCNN2() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGPYTORCHCNN2)
                && wantMLPytorch();
    }

    public  boolean wantPytorchRNN() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGPYTORCHRNN)
                && wantMLPytorch();
    }

    public  boolean wantPytorchLSTM() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGPYTORCHLSTM)
                && wantMLPytorch();
    }

    public  boolean wantPytorchGRU() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGPYTORCHGRU)
                && wantMLPytorch();
    }

    public  boolean wantGemEWC() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGGEMEWC)
                && wantMLGem();
    }

    public  boolean wantGemGEM() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGGEMGEM)
                && wantMLGem();
    }

    public  boolean wantGemIcarl() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGGEMICARL)
                && wantMLGem();
    }

    public  boolean wantGemIndependent() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGGEMINDEPENDENT)
                && wantMLGem();
    }

    public  boolean wantGemMultiModal() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGGEMMULTIMODAL)
                && wantMLGem();
    }

    public  boolean wantGemSingle() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGGEMSINGLE)
                && wantMLGem();
    }

    public boolean wantMLLearn() {
        Boolean value = (Boolean) getValue(ConfigConstants.MACHINELEARNINGMLLEARN);
        return value != null ? value : false;
    }

    public boolean wantMLClassify() {
        Boolean value =  (Boolean) getValue(ConfigConstants.MACHINELEARNINGMLCLASSIFY);
        return value != null ? value : false;
    }

    public boolean wantMLCross() {
        Boolean value =  (Boolean) getValue(ConfigConstants.MACHINELEARNINGMLCROSS);
        return value != null ? value : false;
    }

    public String getSparkMLPCConfig() {
        return (String) getValueOrDefault(ConfigConstants.MACHINELEARNINGSPARKMLMLPCCONFIG);
    }

    public String getSparkLORConfig() {
        return (String) getValueOrDefault(ConfigConstants.MACHINELEARNINGSPARKMLLORCONFIG);
    }

    public String getSparkOVRConfig() {
        return (String) getValueOrDefault(ConfigConstants.MACHINELEARNINGSPARKMLOVRCONFIG);
    }

    public String getSparkLSVCConfig() {
        return (String) getValueOrDefault(ConfigConstants.MACHINELEARNINGSPARKMLLSVCCONFIG);
    }

    public String getTensorflowDNNConfig() {
        return (String) getValueOrDefault(ConfigConstants.MACHINELEARNINGTENSORFLOWDNNCONFIG);
    }

    public String getTensorflowLICConfig() {
        return (String) getValueOrDefault(ConfigConstants.MACHINELEARNINGTENSORFLOWLICCONFIG);
    }

    public String getTensorflowMLPConfig() {
        return (String) getValueOrDefault(ConfigConstants.MACHINELEARNINGTENSORFLOWMLPCONFIG);
    }

    public String getTensorflowCNNConfig() {
        return (String) getValueOrDefault(ConfigConstants.MACHINELEARNINGTENSORFLOWCNNCONFIG);
    }

    public String getTensorflowCNN2Config() {
        return (String) getValueOrDefault(ConfigConstants.MACHINELEARNINGTENSORFLOWCNN2CONFIG);
    }

    public String getTensorflowRNNConfig() {
        return (String) getValueOrDefault(ConfigConstants.MACHINELEARNINGTENSORFLOWRNNCONFIG);
    }

    public String getTensorflowLSTMConfig() {
        return (String) getValueOrDefault(ConfigConstants.MACHINELEARNINGTENSORFLOWLSTMCONFIG);
    }

    public String getTensorflowGRUConfig() {
        return (String) getValueOrDefault(ConfigConstants.MACHINELEARNINGTENSORFLOWGRUCONFIG);
    }

    public String getPytorchMLPConfig() {
        return (String) getValueOrDefault(ConfigConstants.MACHINELEARNINGPYTORCHMLPCONFIG);
    }

    public String getPytorchCNNConfig() {
        return (String) getValueOrDefault(ConfigConstants.MACHINELEARNINGPYTORCHCNNCONFIG);
    }

    public String getPytorchCNN2Config() {
        return (String) getValueOrDefault(ConfigConstants.MACHINELEARNINGPYTORCHCNN2CONFIG);
    }

    public String getPytorchRNNConfig() {
        return (String) getValueOrDefault(ConfigConstants.MACHINELEARNINGPYTORCHRNNCONFIG);
    }

    public String getPytorchLSTMConfig() {
        return (String) getValueOrDefault(ConfigConstants.MACHINELEARNINGPYTORCHLSTMCONFIG);
    }

    public String getPytorchGRUConfig() {
        return (String) getValueOrDefault(ConfigConstants.MACHINELEARNINGPYTORCHGRUCONFIG);
    }

    public String getGemEWCConfig() {
        return (String) getValueOrDefault(ConfigConstants.MACHINELEARNINGGEMEWCCONFIG);
    }

    public String getGemGEMConfig() {
        return (String) getValueOrDefault(ConfigConstants.MACHINELEARNINGGEMGEMCONFIG);
    }

    public String getGemIcarlConfig() {
        return (String) getValueOrDefault(ConfigConstants.MACHINELEARNINGGEMICARLCONFIG);
    }

    public String getGemIndependentConfig() {
        return (String) getValueOrDefault(ConfigConstants.MACHINELEARNINGGEMINDEPENDENTCONFIG);
    }

    public String getGemMultiModalConfig() {
        return (String) getValueOrDefault(ConfigConstants.MACHINELEARNINGGEMMULTIMODALCONFIG);
    }

    public String getGemSingleConfig() {
        return (String) getValueOrDefault(ConfigConstants.MACHINELEARNINGGEMSINGLECONFIG);
    }

    public boolean wantSparkMLPCPersist() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGSPARKMLMLPCPERSIST);
    }

    public boolean wantSparkLORPersist() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGSPARKMLLORPERSIST);
    }

    public boolean wantSparkOVRPersist() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGSPARKMLOVRPERSIST);
    }

    public boolean wantSparkLSVCPersist() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGSPARKMLLSVCPERSIST);
    }

    public boolean wantTensorflowDNNPersist() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGTENSORFLOWDNNPERSIST);
    }

    public boolean wantTensorflowLICPersist() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGTENSORFLOWLICPERSIST);
    }

    public boolean wantTensorflowMLPPersist() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGTENSORFLOWMLPPERSIST);
    }

    public boolean wantTensorflowCNNPersist() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGTENSORFLOWCNNPERSIST);
    }

    public boolean wantTensorflowCNN2Persist() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGTENSORFLOWCNN2PERSIST);
    }

    public boolean wantTensorflowRNNPersist() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGTENSORFLOWRNNPERSIST);
    }

    public boolean wantTensorflowLSTMPersist() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGTENSORFLOWLSTMPERSIST);
    }

    public boolean wantTensorflowGRUPersist() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGTENSORFLOWGRUPERSIST);
    }

    public boolean wantPytorchMLPPersist() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGPYTORCHMLPPERSIST);
    }

    public boolean wantPytorchCNNPersist() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGPYTORCHCNNPERSIST);
    }

    public boolean wantPytorchCNN2Persist() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGPYTORCHCNN2PERSIST);
    }

    public boolean wantPytorchRNNPersist() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGPYTORCHRNNPERSIST);
    }

    public boolean wantPytorchLSTMPersist() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGPYTORCHLSTMPERSIST);
    }

    public boolean wantPytorchGRUPersist() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGPYTORCHGRUPERSIST);
    }

    public boolean wantGemEWCPersist() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGGEMEWCPERSIST);
    }

    public boolean wantGemGEMPersist() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGGEMGEMPERSIST);
    }

    public boolean wantGemIcarlPersist() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGGEMICARLPERSIST);
    }

    public boolean wantGemIndependentPersist() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGGEMINDEPENDENTPERSIST);
    }

    public boolean wantGemMultiModalPersist() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGGEMMULTIMODALPERSIST);
    }

    public boolean wantGemSinglePersist() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGGEMSINGLEPERSIST);
    }

    public  int weightBuyHist() {
        return (Integer) getValueOrDefault(ConfigConstants.INDICATORSMACDRECOMMENDBUYWEIGHTHISTOGRAM);
    }

    public  int weightBuyHistDelta() {
        return (Integer) getValueOrDefault(ConfigConstants.INDICATORSMACDRECOMMENDBUYWEIGHTHISTOGRAMDELTA);
    }

    public  int weightBuyMacd() {
        return (Integer) getValueOrDefault(ConfigConstants.INDICATORSMACDRECOMMENDBUYWEIGHTMACD);
    }

    public  int weightBuyMacdDelta() {
        return (Integer) getValueOrDefault(ConfigConstants.INDICATORSMACDRECOMMENDBUYWEIGHTMACDDELTA);
    }

    public  int weightSellHist() {
        return (Integer) getValueOrDefault(ConfigConstants.INDICATORSMACDRECOMMENDSELLWEIGHTHISTOGRAM);
    }

    public  int weightSellHistDelta() {
        return (Integer) getValueOrDefault(ConfigConstants.INDICATORSMACDRECOMMENDSELLWEIGHTHISTOGRAMDELTA);
    }

    public  int weightSellMacd() {
        return (Integer) getValueOrDefault(ConfigConstants.INDICATORSMACDRECOMMENDSELLWEIGHTMACD);
    }

    public  int weightSellMacdDelta() {
        return (Integer) getValueOrDefault(ConfigConstants.INDICATORSMACDRECOMMENDSELLWEIGHTMACDDELTA);
    }

    public  boolean wantRecommenderRSI() {
        return (Boolean) getValueOrDefault(ConfigConstants.INDICATORSRSIRECOMMEND);
    }

    public  boolean wantRecommenderMACD() {
        return (Boolean) getValueOrDefault(ConfigConstants.INDICATORSMACDRECOMMEND);
    }

    public  int buyWeightRSI() {
        return (Integer) getValueOrDefault(ConfigConstants.INDICATORSRSIRECOMMENDBUYWEIGHT);
    }

    public  int buyWeightRSIDelta() {
        return (Integer) getValueOrDefault(ConfigConstants.INDICATORSRSIRECOMMENDBUYWEIGHTDELTA);
    }

    public  int sellWeightRSI() {
        return (Integer) getValueOrDefault(ConfigConstants.INDICATORSRSIRECOMMENDSELLWEIGHT);
    }

    public  int sellWeightRSIDelta() {
        return (Integer) getValueOrDefault(ConfigConstants.INDICATORSRSIRECOMMENDSELLWEIGHTDELTA);
    }

    public  boolean wantMLHist() {
        return (Boolean) getValueOrDefault(ConfigConstants.AGGREGATORSMLMACDHISTOGRAMML);
    }

    public  boolean wantMLMacd() {
        return (Boolean) getValueOrDefault(ConfigConstants.AGGREGATORSMLMACDMACDML);
    }

    public  boolean wantMLSignal() {
        return (Boolean) getValueOrDefault(ConfigConstants.AGGREGATORSMLMACDSIGNALML);
    }

    public boolean wantMLTimes() {
        return (Boolean) getConfigValueMap().get(ConfigConstants.MISCMLSTATS)
                && wantML();
    }

    public boolean wantPercentizedPriceIndex() {
        return (Boolean) getConfigValueMap().get(ConfigConstants.MISCPERCENTIZEPRICEINDEX);
    }

    public boolean wantOtherStats() {
        return (Boolean) getConfigValueMap().get(ConfigConstants.MISCOTHERSTATS);

    }

    public boolean wantFilterWeekend() {
        return (Boolean) getValueOrDefault(ConfigConstants.MISCFILTERWEEKEND);
    }

    public double getFilterDate() {
        return (Double) getValueOrDefault(ConfigConstants.MISCFILTERDATE);
    }

    public boolean wantInterpolate() {
        return (Boolean) getValueOrDefault(ConfigConstants.MISCINTERPOLATE);
    }

    public String getInterpolationmethod() {
        return (String) getValueOrDefault(ConfigConstants.MISCINTERPOLATIONMETHOD);
    }

    public boolean getInterpolateLastNull() {
        return (Boolean) getValueOrDefault(ConfigConstants.MISCINTERPOLATIONLASTNULL);
    }

    public String getThreshold() {
        return (String) getValueOrDefault(ConfigConstants.MISCTHRESHOLD);
    }

    public boolean wantMergecy() {
        return (Boolean) getValueOrDefault(ConfigConstants.MISCMERGECY);
    }

    public String getMyservices() {
        return (String) getValueOrDefault(ConfigConstants.MISCMYSERVICES);
    }

    public String getServices() {
        return (String) getValueOrDefault(ConfigConstants.MISCSERVICES);
    }

    public String getCommunications() {
        return (String) getValueOrDefault(ConfigConstants.MISCCOMMUNICATIONS);
    }

    public boolean wantCache() {
        return (Boolean) getValueOrDefault(ConfigConstants.MISCCACHE);
    }

    public int getCacheTTL() {
        return (Integer) getValueOrDefault(ConfigConstants.MISCCACHETTL);
    }

    public String getInmemoryServer() {
        return (String) getNotEmptyValueOrDefault(ConfigConstants.MISCINMEMORYSERVER);
    }

    public String getInmemoryHazelcast() {
        return (String) getNotEmptyValueOrDefault(ConfigConstants.MISCINMEMORYHAZELCAST);
    }

    public String getInmemoryRedis() {
        return (String) getNotEmptyValueOrDefault(ConfigConstants.MISCINMEMORYREDIS);
    }

    public String getZookeeper() {
        return (String) getNotEmptyValueOrDefault(ConfigConstants.MISCZOOKEEPER);
    }

    public Double getAbnormalChange() {
        return (Double) getValueOrDefault(ConfigConstants.MISCABNORMALCHANGE);
    }

    public String getDbSparkMaster() {
        return (String) getConfigValueMap().get(ConfigConstants.DATABASESPARKSPARKMASTER);
    }

    public String getMLSparkMaster() {
        return (String) getConfigValueMap().get(ConfigConstants.MACHINELEARNINGSPARKMLSPARKMASTER);
    }

    public Integer getMLSparkTimeout() {
        return (Integer) getConfigValueMap().get(ConfigConstants.MACHINELEARNINGSPARKMLSPARKNETWORKTIMEOUT);
    }

    public int getMaxHoles() {
        return (Integer) getValueOrDefault(ConfigConstants.DATABASEMAXHOLES);
    }

    public boolean wantDbSpark() {
        System.out.println("sp " + (Boolean) getConfigValueMap().get(ConfigConstants.DATABASESPARK));
        return (Boolean) getValueOrDefault(ConfigConstants.DATABASESPARK);
    }

    public boolean wantDbHibernate() {
        System.out.println("hb " + (Boolean) getConfigValueMap().get(ConfigConstants.DATABASEHIBERNATE));
        return (Boolean) getValueOrDefault(ConfigConstants.DATABASEHIBERNATE);
    }

    public boolean wantPredictors() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGPREDICTORS)
                && wantML();
    }

    public String getPredictorsMLConfig() {
        return (String) getValueOrDefault(ConfigConstants.MACHINELEARNINGPREDICTORSMLCONFIG);
    }

    public int getPredictorsDays() {
        return (Integer) getValueOrDefault(ConfigConstants.MACHINELEARNINGPREDICTORSDAYS);
    }

    public int getPredictorsFuturedays() {
        return (Integer) getValueOrDefault(ConfigConstants.MACHINELEARNINGPREDICTORSFUTUREDAYS);
    }

    public String getPredictorsThreshold() {
        return (String) getValueOrDefault(ConfigConstants.MACHINELEARNINGPREDICTORSTHRESHOLD);
    }

    public boolean wantPredictorsTensorflow() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOW)
                && wantPredictors();
    }

    public boolean wantPredictorsPytorch() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCH)
                && wantPredictors();
    }

    public  boolean wantPredictorTensorflowLIR() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWLIR)
                && wantPredictorsTensorflow();
    }

    public  boolean wantPredictorTensorflowMLP() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWMLP)
                && wantPredictorsTensorflow();
    }

    public  boolean wantPredictorTensorflowRNN() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWRNN)
                && wantPredictorsTensorflow();
    }

    public boolean wantPredictorTensorflowLSTM() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWLSTM) 
                && wantPredictorsTensorflow();
    }

    public  boolean wantPredictorTensorflowGRU() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWGRU)
                && wantPredictorsTensorflow();
    }

    public  boolean wantPredictorPytorchMLP() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHMLP)
                && wantPredictorsPytorch();
    }

    public  boolean wantPredictorPytorchRNN() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHRNN)
                && wantPredictorsPytorch();
    }

    public boolean wantPredictorPytorchLSTM() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHLSTM) 
                && wantPredictorsPytorch();
    }

    public  boolean wantPredictorPytorchGRU() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHGRU)
                && wantPredictorsPytorch();
    }

    public  boolean wantPredictorTensorflowLIRPersist() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWLIRPERSIST)
                && wantPredictorsTensorflow();
    }

    public  boolean wantPredictorTensorflowMLPPersist() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWMLPPERSIST)
                && wantPredictorsTensorflow();
    }

    public  boolean wantPredictorTensorflowRNNPersist() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWRNNPERSIST)
                && wantPredictorsTensorflow();
    }

    public boolean wantPredictorTensorflowLSTMPersist() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWLSTMPERSIST) 
                && wantPredictorsTensorflow();
    }

    public  boolean wantPredictorTensorflowGRUPersist() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWGRUPERSIST)
                && wantPredictorsTensorflow();
    }

    public  boolean wantPredictorPytorchMLPPersist() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHMLPPERSIST)
                && wantPredictorsPytorch();
    }

    public  boolean wantPredictorPytorchRNNPersist() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHRNNPERSIST)
                && wantPredictorsPytorch();
    }

    public boolean wantPredictorPytorchLSTMPersist() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHLSTMPERSIST) 
                && wantPredictorsPytorch();
    }

    public  boolean wantPredictorPytorchGRUPersist() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHGRUPERSIST)
                && wantPredictorsPytorch();
    }

    public  String getPredictorTensorflowLIRConfig() {
        return (String) getValueOrDefault(ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWLIRCONFIG);
    }

    public  String getPredictorTensorflowMLPConfig() {
        return (String) getValueOrDefault(ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWMLPCONFIG);
    }

    public  String getPredictorTensorflowRNNConfig() {
        return (String) getValueOrDefault(ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWRNNCONFIG);
    }

    public String getPredictorTensorflowLSTMConfig() {
        return (String) getValueOrDefault(ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWLSTMCONFIG);
    }

    public  String getPredictorTensorflowGRUConfig() {
        return (String) getValueOrDefault(ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWGRUCONFIG);
    }

    public  String getPredictorPytorchMLPConfig() {
        return (String) getValueOrDefault(ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHMLPCONFIG);
    }

    public  String getPredictorPytorchRNNConfig() {
        return (String) getValueOrDefault(ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHRNNCONFIG);
    }

    public String getPredictorPytorchLSTMConfig() {
        return (String) getValueOrDefault(ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHLSTMCONFIG);
    }

    public  String getPredictorPytorchGRUConfig() {
        return (String) getValueOrDefault(ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHGRUCONFIG);
    }

    /*
    @Deprecated
    public Integer getTestRecommendIntervalTimes() {
        return (Integer) getValueOrDefault(ConfigConstants.TESTRECOMMENDINTERVALTIMES);
    }

    @Deprecated
   public Integer getTestRecommendIterations() {
        return (Integer) getValueOrDefault(ConfigConstants.TESTRECOMMENDITERATIONS);
    }

    @Deprecated
    public String getTestRecommendPeriod() {
        return (String) getValueOrDefault(ConfigConstants.TESTRECOMMENDPERIOD);
    }
     */

    public Integer getTestIndicatorRecommenderComplexFutureDays() {
        return (Integer) getValueOrDefault(ConfigConstants.EVOLVEINDICATORRECOMMENDERCOMPLEXFUTUREDAYS);
    }

    public String getTestIndicatorRecommenderComplexThreshold() {
        return (String) getValueOrDefault(ConfigConstants.EVOLVEINDICATORRECOMMENDERCOMPLEXTHRESHOLD);
    }

    public Integer getTestIndicatorRecommenderComplexIntervalDays() {
        return (Integer) getValueOrDefault(ConfigConstants.EVOLVEINDICATORRECOMMENDERCOMPLEXINTERVALDAYS);
    }

    public Integer getTestIndicatorRecommenderSimpleFutureDays() {
        return (Integer) getValueOrDefault(ConfigConstants.EVOLVEINDICATORRECOMMENDERSIMPLEFUTUREDAYS);
    }

    public Integer getTestIndicatorRecommenderSimpleIntervalDays() {
        return (Integer) getValueOrDefault(ConfigConstants.EVOLVEINDICATORRECOMMENDERSIMPLEINTERVALDAYS);
    }

    /*
    public Integer getEvolutionGenerations() {
        return (Integer) getValueOrDefault(ConfigConstants.EVOLUTIONGENERATIONS);
    }

    public Integer getEvolutionCrossover() {
        return (Integer) getValueOrDefault(ConfigConstants.EVOLUTIONCROSSOVER);
    }

    public Integer getEvolutionGenerationCreate() {
        return (Integer) getValueOrDefault(ConfigConstants.EVOLUTIONGENERATIONCREATE);
    }

    public Integer getEvolutionMutate() {
        return (Integer) getValueOrDefault(ConfigConstants.EVOLUTIONMUTATE);
    }

    public Integer getEvolutionSelect() {
        return (Integer) getValueOrDefault(ConfigConstants.EVOLUTIONSELECT);
    }

    public Integer getEvolutionElite() {
        return (Integer) getValueOrDefault(ConfigConstants.EVOLUTIONELITE);
    }

    public Integer getEvolutionEliteCloneAndMutate() {
        return (Integer) getValueOrDefault(ConfigConstants.EVOLUTIONELITECLONEANDMUTATE);
    }
     */

    public String getTestIndictorrecommenderEvolutionConfig() {
        return (String) getValueOrDefault(ConfigConstants.EVOLVEINDICATORRECOMMENDEREVOLUTIONCONFIG);
    }

    public String getTestMLEvolutionConfig() {
        return (String) getValueOrDefault(ConfigConstants.EVOLVEMLEVOLUTIONCONFIG);
    }

    public String getEvolveSaveLocation() {
        return (String) getNotEmptyValueOrDefault(ConfigConstants.EVOLVESAVELOCATION);
    }

    public String getEvolveSavePath() {
        return (String) getNotEmptyValueOrDefault(ConfigConstants.EVOLVESAVEPATH);
    }

    public boolean wantAggregators() {
        return (Boolean) getValueOrDefault(ConfigConstants.AGGREGATORS); 
    }

    public boolean wantMACDRSIRecommender() {
        return (Boolean) getValueOrDefault(ConfigConstants.AGGREGATORSINDICATORRECOMMENDER) 
                && wantAggregators();
    }

    public boolean wantRecommenderSimpleMACD() {
        return (Boolean) getValueOrDefault(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERSIMPLEMACD) 
                && wantIndicatorRecommenderSimple();
    }

    public boolean wantRecommenderComplexMACD() {
        return (Boolean) getValueOrDefault(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXMACD) 
                && wantIndicatorRecommenderComplex();
    }

    public boolean wantRecommenderSimpleRSI() {
        return (Boolean) getValueOrDefault(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERSIMPLERSI) 
                && wantIndicatorRecommenderSimple();
    }

    public boolean wantRecommenderComplexRSI() {
        return (Boolean) getValueOrDefault(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXRSI) 
                && wantIndicatorRecommenderComplex();
    }

    public boolean wantIndicatorRecommenderSimple() {
        return (Boolean) getValueOrDefault(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERSIMPLE) 
                && wantIndicatorRecommender();
    }

    public boolean wantIndicatorRecommenderComplex() {
        return (Boolean) getValueOrDefault(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEX) 
                && wantIndicatorRecommender();
    }

    public boolean wantIndicatorRecommender() {
        return (Boolean) getValueOrDefault(ConfigConstants.AGGREGATORSINDICATORRECOMMENDER) 
                && wantAggregators();
    }

    public boolean wantRecommenderComplexATR() {
        return (Boolean) getValueOrDefault(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXATR) 
                && wantIndicatorRecommenderComplex();
    }

    public boolean wantRecommenderComplexCCI() {
        return (Boolean) getValueOrDefault(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXCCI) 
                && wantIndicatorRecommenderComplex();
    }

    public boolean wantRecommenderComplexSTOCH() {
        return (Boolean) getValueOrDefault(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXSTOCH) 
                && wantIndicatorRecommenderComplex();
    }

    public boolean wantRecommenderComplexSTOCHRSI() {
        return (Boolean) getValueOrDefault(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXSTOCHRSI) 
                && wantIndicatorRecommenderComplex();
    }

    public boolean wantAggregatorsIndicatorML() {
        return (Boolean) getValueOrDefault(ConfigConstants.AGGREGATORSINDICATOR) &&
                wantAggregators() &&
                wantML ();
    }

    public String getAggregatorsMLIndicatorMLConfig() {
        return (String) getValueOrDefault(ConfigConstants.AGGREGATORSINDICATORMLCONFIG);
    }

    public boolean wantMLMACD() {
        return (Boolean) getValueOrDefault(ConfigConstants.AGGREGATORSMLMACD) &&
                wantAggregators() &&
                isMACDEnabled() &&
                wantML();
    }

    public String getMLMACDMLConfig() {
        return (String) getValueOrDefault(ConfigConstants.AGGREGATORSMLMACDMLCONFIG);
    }

    public String getMLMACDThreshold() {
        return (String) getValueOrDefault(ConfigConstants.AGGREGATORSMLMACDTHRESHOLD);
    }

    public boolean wantMLRSI() {
        return (Boolean) getValueOrDefault(ConfigConstants.AGGREGATORSMLRSI) &&
                wantAggregators() &&
                isRSIEnabled() &&
                isSTOCHRSIEnabled() &&
                wantML();
    }

    public String getMLRSIMLConfig() {
        return (String) getValueOrDefault(ConfigConstants.AGGREGATORSMLRSIMLCONFIG);
    }

    public int getMLRSIDaysBeforeLimit() {
        return (Integer) getValueOrDefault(ConfigConstants.AGGREGATORSMLRSIDAYSBEFORELIMIT);
    }

    public int getMLRSIDaysAfterLimit() {
        return (Integer) getValueOrDefault(ConfigConstants.AGGREGATORSMLRSIDAYSAFTERLIMIT);
    }

    public int getMLRSIBuyRSILimit() {
        return (Integer) getValueOrDefault(ConfigConstants.AGGREGATORSMLRSIBUYRSILIMIT);
    }

    public double getMLRSIBuySRSILimit() {
        return (Double) getValueOrDefault(ConfigConstants.AGGREGATORSMLRSIBUYSRSILIMIT);
    }

    public int getMLRSISellRSILimit() {
        return (Integer) getValueOrDefault(ConfigConstants.AGGREGATORSMLRSISELLRSILIMIT);
    }

    public double getMLRSISellSRSILimit() {
        return (Double) getValueOrDefault(ConfigConstants.AGGREGATORSMLRSISELLSRSILIMIT);
    }

    public String getMLRSIThreshold() {
        return (String) getValueOrDefault(ConfigConstants.AGGREGATORSMLRSITHRESHOLD);
    }

    public boolean wantMLATR() {
        return (Boolean) getValueOrDefault(ConfigConstants.AGGREGATORSMLATR) &&
                wantAggregators() &&
                isATREnabled() &&
                wantML();
    }

    public String getMLATRMLConfig() {
        return (String) getValueOrDefault(ConfigConstants.AGGREGATORSMLATRMLCONFIG);
    }

    public int getMLATRDaysBeforeLimit() {
        return (Integer) getValueOrDefault(ConfigConstants.AGGREGATORSMLATRDAYSBEFORELIMIT);
    }

    public int getMLATRDaysAfterLimit() {
        return (Integer) getValueOrDefault(ConfigConstants.AGGREGATORSMLATRDAYSAFTERLIMIT);
    }

    public int getMLATRBuyLimit() {
        return (Integer) getValueOrDefault(ConfigConstants.AGGREGATORSMLATRBUYLIMIT);
    }

    public int getMLATRSellLimit() {
        return (Integer) getValueOrDefault(ConfigConstants.AGGREGATORSMLATRSELLLIMIT);
    }

    public String getMLATRThreshold() {
        return (String) getValueOrDefault(ConfigConstants.AGGREGATORSMLATRTHRESHOLD);
    }

    public boolean wantMLCCI() {
        return (Boolean) getValueOrDefault(ConfigConstants.AGGREGATORSMLCCI) &&
                wantAggregators() &&
                isCCIEnabled() &&
                wantML();
    }

    public String getMLCCIMLConfig() {
        return (String) getValueOrDefault(ConfigConstants.AGGREGATORSMLCCIMLCONFIG);
    }

    public int getMLCCIDaysBeforeLimit() {
        return (Integer) getValueOrDefault(ConfigConstants.AGGREGATORSMLCCIDAYSBEFORELIMIT);
    }

    public int getMLCCIDaysAfterLimit() {
        return (Integer) getValueOrDefault(ConfigConstants.AGGREGATORSMLCCIDAYSAFTERLIMIT);
    }

    public int getMLCCIBuyLimit() {
        return (Integer) getValueOrDefault(ConfigConstants.AGGREGATORSMLCCIBUYLIMIT);
    }

    public int getMLCCISellLimit() {
        return (Integer) getValueOrDefault(ConfigConstants.AGGREGATORSMLCCISELLLIMIT);
    }

    public String getMLCCIThreshold() {
        return (String) getValueOrDefault(ConfigConstants.AGGREGATORSMLCCITHRESHOLD);
    }

    public boolean wantMLSTOCH() {
        return (Boolean) getValueOrDefault(ConfigConstants.AGGREGATORSMLSTOCH) &&
                wantAggregators() &&
                isSTOCHEnabled() &&
                wantML();
    }

    public String getMLSTOCHMLConfig() {
        return (String) getValueOrDefault(ConfigConstants.AGGREGATORSMLSTOCHMLCONFIG);
    }

    public int getMLSTOCHDaysBeforeLimit() {
        return (Integer) getValueOrDefault(ConfigConstants.AGGREGATORSMLSTOCHDAYSBEFORELIMIT);
    }

    public int getMLSTOCHDaysAfterLimit() {
        return (Integer) getValueOrDefault(ConfigConstants.AGGREGATORSMLSTOCHDAYSAFTERLIMIT);
    }

    public int getMLSTOCHBuyLimit() {
        return (Integer) getValueOrDefault(ConfigConstants.AGGREGATORSMLSTOCHBUYLIMIT);
    }

    public int getMLSTOCHSellLimit() {
        return (Integer) getValueOrDefault(ConfigConstants.AGGREGATORSMLSTOCHSELLLIMIT);
    }

    public String getMLSTOCHThreshold() {
        return (String) getValueOrDefault(ConfigConstants.AGGREGATORSMLSTOCHTHRESHOLD);
    }

    public boolean wantAggregatorsIndicatorMACD() {
        return (Boolean) getValueOrDefault(ConfigConstants.AGGREGATORSINDICATORMACD)
                && wantAggregatorsIndicatorML();
    }

    public boolean wantAggregatorsIndicatorRSI() {
        return (Boolean) getValueOrDefault(ConfigConstants.AGGREGATORSINDICATORRSI)
                && wantAggregatorsIndicatorML();
    }

    public boolean wantAggregatorsIndicatorATR() {
        return (Boolean) getValueOrDefault(ConfigConstants.AGGREGATORSINDICATORATR)
                && wantAggregatorsIndicatorML();
    }

    public boolean wantAggregatorsIndicatorCCI() {
        return (Boolean) getValueOrDefault(ConfigConstants.AGGREGATORSINDICATORCCI)
                && wantAggregatorsIndicatorML();
    }

    public boolean wantAggregatorsIndicatorSTOCH() {
        return (Boolean) getValueOrDefault(ConfigConstants.AGGREGATORSINDICATORSTOCH)
                && wantAggregatorsIndicatorML();
    }

    public boolean wantAggregatorsIndicatorSTOCHRSI() {
        return (Boolean) getValueOrDefault(ConfigConstants.AGGREGATORSINDICATORSTOCHRSI)
                && wantAggregatorsIndicatorML();
    }

    public String getAggregatorsIndicatorExtras() {
        return (String) getValueOrDefault(ConfigConstants.AGGREGATORSINDICATOREXTRAS);
    }

    @JsonIgnore
    public String getAggregatorsIndicatorExtrasList() {
        return (String) getValueOrDefault(ConfigConstants.AGGREGATORSINDICATOREXTRASLIST);
    }

    public String getAggregatorsIndicatorExtrasBits() {
        return (String) getValueOrDefault(ConfigConstants.AGGREGATORSINDICATOREXTRASBITS);
    }

    public Integer getAggregatorsIndicatorExtrasDeltas() {
        return (Integer) getValueOrDefault(ConfigConstants.AGGREGATORSINDICATOREXTRASDELTAS);
    }

    public Boolean wantAggregatorsIndicatorExtrasMACD() {
        return (Boolean) getValueOrDefault(ConfigConstants.AGGREGATORSINDICATOREXTRASMACD);
    }

    public Boolean wantAggregatorsIndicatorExtrasRSI() {
        return (Boolean) getValueOrDefault(ConfigConstants.AGGREGATORSINDICATOREXTRASRSI);
    }

    public Boolean wantAggregatorsIndicatorExtrasATR() {
        return (Boolean) getValueOrDefault(ConfigConstants.AGGREGATORSINDICATOREXTRASATR);
    }

    public Boolean wantAggregatorsIndicatorExtrasCCI() {
        return (Boolean) getValueOrDefault(ConfigConstants.AGGREGATORSINDICATOREXTRASCCI);
    }

    public Boolean wantAggregatorsIndicatorExtrasSTOCH() {
        return (Boolean) getValueOrDefault(ConfigConstants.AGGREGATORSINDICATOREXTRASSTOCH);
    }

    public Boolean wantAggregatorsIndicatorExtrasSTOCHRSI() {
        return (Boolean) getValueOrDefault(ConfigConstants.AGGREGATORSINDICATOREXTRASSTOCHRSI);
    }

    public int getAggregatorsIndicatorDays() {
        return (Integer) getValueOrDefault(ConfigConstants.AGGREGATORSINDICATORDAYS);
    }

    public int getAggregatorsIndicatorFuturedays() {
        return (Integer) getValueOrDefault(ConfigConstants.AGGREGATORSINDICATORFUTUREDAYS);
    }

    public int getAggregatorsIndicatorIntervaldays() {
        return (Integer) getValueOrDefault(ConfigConstants.AGGREGATORSINDICATORINTERVALDAYS);
    }

    public String getAggregatorsIndicatorThreshold() {
        return (String) getValueOrDefault(ConfigConstants.AGGREGATORSINDICATORTHRESHOLD);
    }

    public boolean wantAggregatorsMlmultiML() {
        return (Boolean) getValueOrDefault(ConfigConstants.AGGREGATORSMLMULTI) &&
                wantAggregators() &&
                wantML ();
    }

    public String getAggregatorsMLMlmultiMLConfig() {
        return (String) getValueOrDefault(ConfigConstants.AGGREGATORSMLMULTIMLCONFIG);
    }

    public int getMLMultiDaysBeforeLimit() {
        return (Integer) getValueOrDefault(ConfigConstants.AGGREGATORSMLMULTIDAYSBEFORELIMIT);
    }

    public int getMLMultiDaysAfterLimit() {
        return (Integer) getValueOrDefault(ConfigConstants.AGGREGATORSMLMULTIDAYSAFTERLIMIT);
    }

    public String getMLMULTIThreshold() {
        return (String) getValueOrDefault(ConfigConstants.AGGREGATORSMLMULTITHRESHOLD);
    }

    public boolean wantAggregatorsMlmultiMACD() {
        return (Boolean) getValueOrDefault(ConfigConstants.AGGREGATORSMLMULTIMACD)
                && wantAggregatorsMlmultiML();
    }

    public boolean wantAggregatorsMlmultiRSI() {
        return (Boolean) getValueOrDefault(ConfigConstants.AGGREGATORSMLMULTIRSI)
                && wantAggregatorsMlmultiML();
    }

    public boolean wantAggregatorsMlmultiATR() {
        return (Boolean) getValueOrDefault(ConfigConstants.AGGREGATORSMLMULTIATR)
                && wantAggregatorsMlmultiML();
    }

    public boolean wantAggregatorsMlmultiCCI() {
        return (Boolean) getValueOrDefault(ConfigConstants.AGGREGATORSMLMULTICCI)
                && wantAggregatorsMlmultiML();
    }

    public boolean wantAggregatorsMlmultiSTOCH() {
        return (Boolean) getValueOrDefault(ConfigConstants.AGGREGATORSMLMULTISTOCH)
                && wantAggregatorsMlmultiML();
    }

    public boolean wantAggregatorsMlmultiSTOCHRSI() {
        return (Boolean) getValueOrDefault(ConfigConstants.AGGREGATORSMLMULTISTOCHRSI)
                && wantAggregatorsMlmultiML();
    }

    public String getTensorflowServer() {
        return (String) getValueOrDefault(ConfigConstants.MACHINELEARNINGTENSORFLOWSERVER);              
    }

    public String getPytorchServer() {
        return (String) getValueOrDefault(ConfigConstants.MACHINELEARNINGPYTORCHSERVER);              
    }

    public String getGEMServer() {
        return (String) getValueOrDefault(ConfigConstants.MACHINELEARNINGGEMSERVER);              
    }

    public String getSparkMLPath() {
        return (String) getValueOrDefault(ConfigConstants.MACHINELEARNINGSPARKMLPATH);              
    }

    public String getTensorflowPath() {
        return (String) getValueOrDefault(ConfigConstants.MACHINELEARNINGTENSORFLOWPATH);              
    }

    public String getPytorchPath() {
        return (String) getValueOrDefault(ConfigConstants.MACHINELEARNINGPYTORCHPATH);              
    }

    public String getGEMPath() {
        return (String) getValueOrDefault(ConfigConstants.MACHINELEARNINGGEMPATH);              
    }

    public String getDatasetMLConfig() {
        return (String) getValueOrDefault(ConfigConstants.DATASETMLCONFIG);
    }

    public Boolean wantSwift() {
        return (Boolean) getNotEmptyValueOrDefault(ConfigConstants.FILESYSTEMSWIFT);
    }
    
    public Boolean wantLocal() {
        return (Boolean) getNotEmptyValueOrDefault(ConfigConstants.FILESYSTEMLOCAL);
    }
    
    public Boolean wantHDFS() {
        return (Boolean) getNotEmptyValueOrDefault(ConfigConstants.FILESYSTEMHDFS);
    }
    
    public Boolean wantS3() {
        return (Boolean) getNotEmptyValueOrDefault(ConfigConstants.FILESYSTEMS3);
    }
    
    @JsonIgnore
    public String getHDFSDefaultName() {
        return (String) getValueOrDefault(ConfigConstants.FILESYSTEMHDFSHDFSCONFFS);
    }
    
    @JsonIgnore
    public String getSwiftUser() {
        return (String) getValueOrDefault(ConfigConstants.FILESYSTEMSWIFTSWIFTCONFUSER);
    }
   
    @JsonIgnore
    public String getSwiftKey() {
        return (String) getValueOrDefault(ConfigConstants.FILESYSTEMSWIFTSWIFTCONFKEY);
    }
   
    @JsonIgnore
    public String getSwiftUrl() {
        return (String) getValueOrDefault(ConfigConstants.FILESYSTEMSWIFTSWIFTCONFURL);
    }
   
    @JsonIgnore
    public String getSwiftContainer() {
        return (String) getValueOrDefault(ConfigConstants.FILESYSTEMSWIFTSWIFTCONFCONTAINER);
    }
   
    @JsonIgnore
    public String getS3Host() {
        return (String) getValueOrDefault(ConfigConstants.FILESYSTEMS3HOST);
    }
   
    @JsonIgnore
    public String getS3Port() {
        return (String) getValueOrDefault(ConfigConstants.FILESYSTEMS3PORT);
    }
   
    @JsonIgnore
    public String getS3Region() {
        return (String) getValueOrDefault(ConfigConstants.FILESYSTEMS3REGION);
    }
   
    @JsonIgnore
    public String getS3AccessKey() {
        return (String) getValueOrDefault(ConfigConstants.FILESYSTEMS3ACCESSKEY);
    }
   
    @JsonIgnore
    public String getS3SecretKey() {
        return (String) getValueOrDefault(ConfigConstants.FILESYSTEMS3SECRETKEY);
    }
   
    public Double getThreshold(String key) {
        if (key == null) {
            return 1.0;
        }
        String myThreshold = (String) getConfigValueMap().get(key);
        try {
            double d = Double.valueOf(myThreshold);
            log.error("Using old format {}", myThreshold);
            return d;
        } catch (Exception e) {            
        }
        try {
            Double[] array = JsonUtil.convert(myThreshold, Double[].class);
            if (array.length == 1) {
                return array[0];
            } else {
                log.error("Threshold size not 1 {}", array);
                return 1.0;
            }
        } catch (Exception e) {
            log.error("Could not parse {}", myThreshold);
            return 1.0;
        }
    }

    public Object getValue(String key) {
        return getConfigValueMap().get(key);
    }
    
     public Object getValueOrDefault(String key) {
        Object retVal = getConfigValueMap().get(key);
        if (retVal != null) {
            String cl = retVal.getClass().getName();
            Class classType = getType().get(key);
            if (retVal.getClass().isAssignableFrom(Integer.class) && classType.isAssignableFrom(Double.class)) {
                getConfigValueMap().put(key, Double.valueOf((Integer)retVal));
                retVal = Double.valueOf((Integer)retVal);
            }
        }
        //System.out.println("r " + retVal + " " + deflt.get(key));
        return Optional.ofNullable(retVal).orElse(getDeflt().get(key));
    }
     
     @JsonIgnore
     public Object getNotEmptyValueOrDefault(String key) {
         Object retVal = getConfigValueMap().get(key);
         //System.out.println("r " + retVal + " " + deflt.get(key));
         if (retVal instanceof String) {
             String str = (String) retVal;
             if (str.isEmpty()) {
                 retVal = null;
             }
         }
         return Optional.ofNullable(retVal).orElse(getDeflt().get(key));
     }

}
