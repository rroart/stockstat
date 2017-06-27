package roart.config;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyConfig {

    protected static Logger log = LoggerFactory.getLogger(MyConfig.class);
    
    protected static MyConfig instance = null;
    
    public static MyConfig instance() {
        return instance;
    }
    
    public MyConfig() {
    }
    
    public ConfigTreeMap configTreeMap;
    
    public Map<String, Object> configValueMap;
    
    //public boolean useSpark = false;
    
    //public String sparkMaster = null;

	private Date mydate = null;

	private String mymarket = "0";

	/*
	private Integer mydays = 180;

	private Integer mytopbottom = 10;

	private Integer mytabledays = 180;

	private Integer mytablemoveintervaldays = 5;

	private Integer mytableintervaldays = 1;

	private boolean myequalize = false;

	private boolean mygraphequalize = false;

	private boolean mygraphequalizeunify = false;

	private boolean macdEnabled = true;

	private boolean macdDeltaEnabled = true;

	private boolean macdHistogramDeltaEnabled = true;

	private boolean moveEnabled = true;

	private boolean rsiEnabled = true;
    
	private boolean rsiDeltaEnabled = true;
    
	// TODO warning this looks weird, avoid for now?
	private boolean stochrsiEnabled = false;
    
	private boolean stochrsiDeltaEnabled = false;
    
	private boolean cciEnabled = true;
    
	private boolean cciDeltaEnabled = true;
    
	private boolean atrEnabled = true;
    
	private boolean atrDeltaEnabled = true;
    
	private boolean stochEnabled = true;
    
	private boolean stochDeltaEnabled = true;
    
	private int macdDeltaDays = 3;

	private int macdHistogramDeltaDays = 3;

	private int rsiDeltaDays = 3;

	private int stochrsiDeltaDays = 3;

	private int cciDeltaDays = 3;

	private int atrDeltaDays = 3;

	private int stochDeltaDays = 3;
*/
    //public void config() throws Exception;

	/**
	 * Set current date
	 * 
	 * @param date
	 */
	
	public void setdate(Date date) {
	    mydate = date;
	}

	/**
	 * Get current date
	 * 
	 * @return date
	 */
	
	public Date getdate() {
	    return mydate;
	}

	public void setMarket(String value) {
	    mymarket = value;
	}

	public String getMarket() {
	    return mymarket;
	}
/*
	public void setDays(Integer integer) {
	    mydays = integer;
	}
*/
	public int getDays() {
	    return (Integer) configValueMap.get(ConfigConstants.MISCMYDAYS);
	}
/*
	public void setTopBottom(Integer integer) {
	    mytopbottom = integer;
	}
*/
	public int getTopBottom() {
	    return (Integer) configValueMap.get(ConfigConstants.MISCMYTOPBOTTOM);
	}
/*
	public void setTableDays(Integer integer) {
	    mytabledays = integer;
	}
*/
	public int getTableDays() {
	    return (Integer) configValueMap.get(ConfigConstants.MISCMYTBLEDAYS);
	}
/*
	public void setTableIntervalDays(Integer integer) {
	    mytableintervaldays = integer;
	}
*/
	public int getTableIntervalDays() {
	    return (Integer) configValueMap.get(ConfigConstants.MISCMYTABLEINTERVALDAYS);
	}
/*
	public void setTableMoveIntervalDays(Integer integer) {
	    mytablemoveintervaldays = integer;
	}
*/
	public int getTableMoveIntervalDays() {
	    return (Integer) configValueMap.get(ConfigConstants.MISCMYTABLEMOVEINTERVALDAYS);
	}
/*
	public void setEqualize(Boolean integer) {
	    myequalize = integer;
	}
*/
	public boolean isEqualize() {
	    return (Boolean) configValueMap.get(ConfigConstants.MISCMYEQUALIZE);
	}
/*
	public void setMoveEnabled(Boolean bool) {
	    moveEnabled = bool;
	}
*/
	
	public boolean wantIndicators() {
        return (Boolean) configValueMap.get(ConfigConstants.INDICATORS);	    
	}
	
	public boolean isMoveEnabled() {
	    return (Boolean) configValueMap.get(ConfigConstants.INDICATORSMOVE)
	            && wantIndicators();
	}
/*
	public void setMACDEnabled(Boolean bool) {
	    macdEnabled = bool;
	}
*/
	public boolean isMACDEnabled() {
	    return (Boolean) configValueMap.get(ConfigConstants.INDICATORSMACD)
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
	    return (Boolean) configValueMap.get(ConfigConstants.INDICATORSCCIDELTA);
	}
/*
	public void setCCIDeltaEnabled(Boolean bool) {
	    cciDeltaEnabled = bool;
	}
*/
	public boolean isATRDeltaEnabled() {
	    return (Boolean) configValueMap.get(ConfigConstants.INDICATORSATRDELTA);
	}
/*
	public void setATRDeltaEnabled(Boolean bool) {
	    atrDeltaEnabled = bool;
	}
*/
	public boolean isSTOCHDeltaEnabled() {
	    return (Boolean) configValueMap.get(ConfigConstants.INDICATORSSTOCHSTOCHDELTA);
	}
/*
	public void setSTOCHDeltaEnabled(Boolean bool) {
	    stochDeltaEnabled = bool;
	}
*/
	public boolean isRSIDeltaEnabled() {
	    return (Boolean) configValueMap.get(ConfigConstants.INDICATORSRSIDELTA);
	}
/*
	public void setRSIDeltaEnabled(Boolean bool) {
	    rsiDeltaEnabled = bool;
	}
*/
	public boolean isSTOCHRSIDeltaEnabled() {
	    return (Boolean) configValueMap.get(ConfigConstants.INDICATORSSTOCHRSI);
	}
/*
	public void setSTOCHRSIDeltaEnabled(Boolean bool) {
	    stochrsiDeltaEnabled = bool;
	}
*/
	public boolean isMACDDeltaEnabled() {
	    return (Boolean) configValueMap.get(ConfigConstants.INDICATORSMACDMACDMOMENTUMDELTA);
	}

	public boolean isMACDHistogramDeltaEnabled() {
	    return (Boolean) configValueMap.get(ConfigConstants.INDICATORSMACDMACDHISTOGRAMDELTA);
	}
/*
	public void setRSIEnabled(Boolean bool) {
	    rsiEnabled = bool;
	}
*/
	public boolean isRSIEnabled() {
	    return (Boolean) configValueMap.get(ConfigConstants.INDICATORSRSI)
        && wantIndicators();
	}
/*
	public void setSTOCHRSIEnabled(Boolean bool) {
	    stochrsiEnabled = bool;
	}
*/
	public boolean isSTOCHRSIEnabled() {
	    return (Boolean) configValueMap.get(ConfigConstants.INDICATORSSTOCHRSI)
                && wantIndicators();
	}
/*
	public void setCCIEnabled(Boolean bool) {
	    cciEnabled = bool;
	}
*/
	public boolean isCCIEnabled() {
	    return (Boolean) configValueMap.get(ConfigConstants.INDICATORSCCI)
                && wantIndicators();
	}
/*
	public void setATREnabled(Boolean bool) {
	    atrEnabled = bool;
	}
*/
	public boolean isATREnabled() {
	    return (Boolean) configValueMap.get(ConfigConstants.INDICATORSATR)
                && wantIndicators();
	}
/*
	public void setSTOCHEnabled(Boolean bool) {
	    stochEnabled = bool;
	}
*/
	public boolean isSTOCHEnabled() {		
	    return (Boolean) configValueMap.get(ConfigConstants.INDICATORSSTOCH)
                && wantIndicators();
	}
/*
	public void setGraphEqualize(Boolean integer) {
	    mygraphequalize = integer;
	}
*/
	public boolean isGraphEqualize() {
	    return (Boolean) configValueMap.get(ConfigConstants.MISCMYGRAPHEQUALIZE);
	}
/*
	public void setGraphEqUnify(Boolean integer) {
	    mygraphequalizeunify = integer;
	}
*/
	public boolean isGraphEqUnify() {
	    return (Boolean) configValueMap.get(ConfigConstants.MISCMYGRAPHEQUALIZEUNIFY);
	}

	public int getMACDDeltaDays() {
		return (Integer) configValueMap.get(ConfigConstants.INDICATORSMACDACDMOMENTUMDELTADAYS);
	}
    /*
	public void setMACDHistogramDeltaDays(Integer integer) {
		this.macdHistogramDeltaDays = integer;
	}
    */
	public int getMACDHistogramDeltaDays() {
		return (Integer) configValueMap.get(ConfigConstants.INDICATORSMACDMACHHISTOGRAMDELTADAYS);
	}
    /*
	public void setMACDDeltaDays(Integer integer) {
		this.macdDeltaDays = integer;
	}
    */
	public int getRSIDeltaDays() {
		return (Integer) configValueMap.get(ConfigConstants.INDICATORSRSIDELTADAYS);
	}
    /*
	public void setRSIDeltaDays(Integer integer) {
		this.rsiDeltaDays = integer;
	}
    */
	public int getSTOCHRSIDeltaDays() {
		return (Integer) configValueMap.get(ConfigConstants.INDICATORSSTOCHRSIDELTADAYS);
	}
    /*
	public void setSTOCHRSIDeltaDays(Integer integer) {
		this.stochrsiDeltaDays = integer;
	}
    */
	public int getCCIDeltaDays() {
		return (Integer) configValueMap.get(ConfigConstants.INDICATORSCCIDELTADAYS);
	}
    /*
	public void setCCIDeltaDays(Integer integer) {
		this.cciDeltaDays = integer;
	}
    */
	public int getATRDeltaDays() {
		return (Integer) configValueMap.get(ConfigConstants.INDICATORSATRDELTADAYS);
	}
  /*  
	public void setATRDeltaDays(Integer integer) {
		this.atrDeltaDays = integer;
	}
    */
	public int getSTOCHDeltaDays() {
		return (Integer) configValueMap.get(ConfigConstants.INDICATORSSTOCHSTOCHDELTADAYS);
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
        return (Integer) configValueMap.get(ConfigConstants.INDICATORSMACDDAYSBEFOREZERO);
    }

    /**
     *  days after positive/negative change
     * @return
     */
    public int getMACDDaysAfterZero() {
        return (Integer) configValueMap.get(ConfigConstants.INDICATORSMACDDAYSAFTERZERO);
    }

    public  boolean wantScore() {
        return (Boolean) configValueMap.get(ConfigConstants.INDICATORSMACDRECOMMENDWEIGHTS);
    }

    public void disableML() {
        configValueMap.put(ConfigConstants.MACHINELEARNING, Boolean.FALSE);
    }
    
    public  boolean wantML() {
        return (Boolean) configValueMap.get(ConfigConstants.MACHINELEARNING);
    }

    public  boolean wantMLSpark() {
        return (Boolean) configValueMap.get(ConfigConstants.MACHINELEARNINGSPARKML)
        && wantML();
    }

    public  boolean wantMLTensorflow() {
        return (Boolean) configValueMap.get(ConfigConstants.MACHINELEARNINGTENSORFLOW)
        && wantML();
    }

    public  boolean wantMCP() {
        return (Boolean) configValueMap.get(ConfigConstants.MACHINELEARNINGSPARKMLMCP)
                && wantMLSpark();
    }

    public  boolean wantLR() {
        return (Boolean) configValueMap.get(ConfigConstants.MACHINELEARNINGSPARKMLLR)
        && wantMLSpark();
    }

    public  boolean wantDNN() {
        return (Boolean) configValueMap.get(ConfigConstants.MACHINELEARNINGTENSORFLOWDNN)
        && wantMLTensorflow();
    }

    public  boolean wantL() {
        return (Boolean) configValueMap.get(ConfigConstants.MACHINELEARNINGTENSORFLOWL)
        && wantMLTensorflow();
    }

    public  int weightBuyHist() {
        return (Integer) configValueMap.get(ConfigConstants.INDICATORSMACDRECOMMENDWEIGHTSBUYHISTOGRAM);
    }

    public  int weightBuyHistDelta() {
        return (Integer) configValueMap.get(ConfigConstants.INDICATORSMACDRECOMMENDWEIGHTSBUYHISTOGRAMDELTA);
    }

    public  int weightBuyMacd() {
        return (Integer) configValueMap.get(ConfigConstants.INDICATORSMACDRECOMMENDWEIGHTSBUYMOMENTUM);
    }

    public  int weightBuyMacdDelta() {
        return (Integer) configValueMap.get(ConfigConstants.INDICATORSMACDRECOMMENDWEIGHTSBUGMOMENTUMDELTA);
    }

    public  int weightSellHist() {
        return (Integer) configValueMap.get(ConfigConstants.INDICATORSMACDRECOMMENDWEIGHTSSELLHISTOGRAM);
    }

    public  int weightSellHistDelta() {
        return (Integer) configValueMap.get(ConfigConstants.INDICATORSMACDRECOMMENDWEIGHTSSELLHISTOGRAMDELTA);
    }

    public  int weightSellMacd() {
        return (Integer) configValueMap.get(ConfigConstants.INDICATORSMACDRECOMMENDWEIGHTSSELLMOMENTUM);
    }

    public  int weightSellMacdDelta() {
        return (Integer) configValueMap.get(ConfigConstants.INDICATORSMACDRECOMMENDWEIGHTSSELLMOMENTUMDELTA);
    }

    public  boolean wantMLHist() {
        return (Boolean) configValueMap.get(ConfigConstants.INDICATORSMACDMACHINELEARNINGHISTOGRAMML);
    }

    public  boolean wantMLMacd() {
        return (Boolean) configValueMap.get(ConfigConstants.INDICATORSMACDMACHINELEARNINGMOMENTUMML);
    }

    public boolean wantMLTimes() {
        return (Boolean) configValueMap.get(ConfigConstants.MISCMLSTATS);
    }

    public boolean wantPercentizedPriceIndex() {
        return (Boolean) configValueMap.get(ConfigConstants.MISCPERCENTIZEPRICEINDEX);
    }

    public boolean wantOtherStats() {
        return (Boolean) configValueMap.get(ConfigConstants.MISCOTHERSTATS);
        
    }
    
    public String getDbSparkMaster() {
        return (String) configValueMap.get(ConfigConstants.DATABASESPARKSPARKMASTER);
    }
    
    public String getMLSparkMaster() {
        return (String) configValueMap.get(ConfigConstants.MACHINELEARNINGSPARMMLSPARKMASTER);
    }
    
    public boolean wantDbSpark() {
        System.out.println("sp " + (Boolean) configValueMap.get(ConfigConstants.DATABASESPARK));
        return (Boolean) configValueMap.get(ConfigConstants.DATABASESPARK);
    }
        
    
    public boolean wantDbHibernate() {
        System.out.println("hb " + (Boolean) configValueMap.get(ConfigConstants.DATABASEHIBERNATE));
        return (Boolean) configValueMap.get(ConfigConstants.DATABASEHIBERNATE);
    }
    
    public boolean wantPredictors() {
        return (Boolean) configValueMap.get(ConfigConstants.PREDICTORS); 
    }
    
    public boolean wantPredictorLSTM() {
        return (Boolean) configValueMap.get(ConfigConstants.PREDICTORSLSTM) 
                && wantPredictors() 
                && wantMLTensorflow();
    }
    
    public Integer getPredictorLSTMHorizon() {
        return (Integer) configValueMap.get(ConfigConstants.PREDICTORSLSTMHORIZON);
    }

    public Integer getPredictorLSTMEpochs() {
        return (Integer) configValueMap.get(ConfigConstants.PREDICTORSLSTMEPOCHS);
    }

    public Integer getPredictorLSTMWindowsize() {
        return (Integer) configValueMap.get(ConfigConstants.PREDICTORSLSTMWINDOWSIZE);
    }

    @Deprecated
    public Integer getTestRecommendIntervalTimes() {
        return (Integer) configValueMap.get(ConfigConstants.TESTRECOMMENDINTERVALTIMES);
    }

    public Integer getTestRecommendIterations() {
        return (Integer) configValueMap.get(ConfigConstants.TESTRECOMMENDITERATIONS);
    }

    public String getTestRecommendPeriod() {
        return (String) configValueMap.get(ConfigConstants.TESTRECOMMENDPERIOD);
    }

    public Integer getTestRecommendFutureDays() {
        return (Integer) configValueMap.get(ConfigConstants.TESTRECOMMENDFUTUREDAYS);
    }

    public Integer getTestRecommendIntervalDays() {
        return (Integer) configValueMap.get(ConfigConstants.TESTRECOMMENDINTERVALDAYS);
    }
    
    public Integer getTestRecommendGenerations() {
        return (Integer) configValueMap.get(ConfigConstants.TESTRECOMMENDGENERATIONS);
    }
    
    public Integer getTestRecommendChildren() {
        return (Integer) configValueMap.get(ConfigConstants.TESTRECOMMENDCHILDREN);
    }
    
    public boolean wantAggregators() {
        return (Boolean) configValueMap.get(ConfigConstants.AGGREGATORS); 
    }
    
    public boolean wantMACDRSIRecommender() {
        return (Boolean) configValueMap.get(ConfigConstants.AGGREGATORSMACDRSIRECOMMENDER) 
                && wantAggregators();
    }
    

}
