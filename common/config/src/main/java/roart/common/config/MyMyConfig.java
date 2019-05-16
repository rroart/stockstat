package roart.common.config;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Optional;

public class MyMyConfig extends MyConfig {

    public MyMyConfig(MyConfig config) {
        setConfigTreeMap(config.getConfigTreeMap());
        setConfigValueMap(config.getConfigValueMap());
        setDeflt(config.getDeflt());
        setText(config.getText());
        setRange(config.getRange());
        setType(config.getType());
        fixIntegerDouble();
        this.mydate = config.mydate;
        this.mymarket = config.mymarket;
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
                    
            //System.out.println("k " + key + " " + value + " " +classType);
            if (value.getClass().isAssignableFrom(Integer.class) && classType.isAssignableFrom(Double.class)) {
                getConfigValueMap().put(key, Double.valueOf(((Integer)value).intValue()));
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
        return (Integer) getValueOrDefault(ConfigConstants.MISCMYTBLEDAYS);
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
        return (Boolean) getValueOrDefault(ConfigConstants.INDICATORSMACDMACDMOMENTUMDELTA);
    }

    public boolean isMACDHistogramDeltaEnabled() {
        return (Boolean) getValueOrDefault(ConfigConstants.INDICATORSMACDMACDHISTOGRAMDELTA);
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
        return (Integer) getValueOrDefault(ConfigConstants.INDICATORSMACDACDMOMENTUMDELTADAYS);
    }

    /*
    public void setMACDHistogramDeltaDays(Integer integer) {
    	this.macdHistogramDeltaDays = integer;
    }
     */
    public int getMACDHistogramDeltaDays() {
        return (Integer) getValueOrDefault(ConfigConstants.INDICATORSMACDMACDHISTOGRAMDELTADAYS);
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
        return (Integer) getValueOrDefault(ConfigConstants.INDICATORSMACDDAYSBEFOREZERO);
    }

    /**
     *  days after positive/negative change
     * @return
     */
    public int getMACDDaysAfterZero() {
        return (Integer) getValueOrDefault(ConfigConstants.INDICATORSMACDDAYSAFTERZERO);
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

    public  boolean wantMLSpark() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGSPARKML)
                && wantML();
    }

    public  boolean wantMLTensorflow() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGTENSORFLOW)
                && wantML();
    }

    public  boolean wantMCP() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGSPARKMLMCP)
                && wantMLSpark();
    }

    public  boolean wantLR() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGSPARKMLLR)
                && wantMLSpark();
    }

    public  boolean wantOVR() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGSPARKMLOVR)
                && wantMLSpark();
    }

    public  boolean wantDNN() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGTENSORFLOWDNN)
                && wantMLTensorflow();
    }

    public  boolean wantDNNL() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGTENSORFLOWDNNL)
                && wantMLTensorflow();
    }

    public  boolean wantL() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGTENSORFLOWL)
                && wantMLTensorflow();
    }

    public  boolean wantLSTM() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGTENSORFLOWLSTM)
                && wantMLTensorflow();
    }

    public String getMCPConfig() {
        return (String) getValueOrDefault(ConfigConstants.MACHINELEARNINGSPARKMLMCPCONFIG);
    }

    public String getLRConfig() {
        return (String) getValueOrDefault(ConfigConstants.MACHINELEARNINGSPARKMLLRCONFIG);
    }

    public String getOVRConfig() {
        return (String) getValueOrDefault(ConfigConstants.MACHINELEARNINGSPARKMLOVRCONFIG);
    }

    public String getDNNConfig() {
        return (String) getValueOrDefault(ConfigConstants.MACHINELEARNINGTENSORFLOWDNNCONFIG);
    }

    public String getDNNLConfig() {
        return (String) getValueOrDefault(ConfigConstants.MACHINELEARNINGTENSORFLOWDNNLCONFIG);
    }

    public String getLConfig() {
        return (String) getValueOrDefault(ConfigConstants.MACHINELEARNINGTENSORFLOWLCONFIG);
    }

    public String getLSTMConfig() {
        return (String) getValueOrDefault(ConfigConstants.MACHINELEARNINGTENSORFLOWLSTMCONFIG);
    }

    public  int weightBuyHist() {
        return (Integer) getValueOrDefault(ConfigConstants.INDICATORSMACDRECOMMENDBUYWEIGHTHISTOGRAM);
    }

    public  int weightBuyHistDelta() {
        return (Integer) getValueOrDefault(ConfigConstants.INDICATORSMACDRECOMMENDBUYWEIGHTHISTOGRAMDELTA);
    }

    public  int weightBuyMacd() {
        return (Integer) getValueOrDefault(ConfigConstants.INDICATORSMACDRECOMMENDBUYWEIGHTMOMENTUM);
    }

    public  int weightBuyMacdDelta() {
        return (Integer) getValueOrDefault(ConfigConstants.INDICATORSMACDRECOMMENDBUYWEIGHTMOMENTUMDELTA);
    }

    public  int weightSellHist() {
        return (Integer) getValueOrDefault(ConfigConstants.INDICATORSMACDRECOMMENDSELLWEIGHTHISTOGRAM);
    }

    public  int weightSellHistDelta() {
        return (Integer) getValueOrDefault(ConfigConstants.INDICATORSMACDRECOMMENDSELLWEIGHTHISTOGRAMDELTA);
    }

    public  int weightSellMacd() {
        return (Integer) getValueOrDefault(ConfigConstants.INDICATORSMACDRECOMMENDSELLWEIGHTMOMENTUM);
    }

    public  int weightSellMacdDelta() {
        return (Integer) getValueOrDefault(ConfigConstants.INDICATORSMACDRECOMMENDSELLWEIGHTMOMENTUMDELTA);
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
        return (Boolean) getValueOrDefault(ConfigConstants.INDICATORSMACDMACHINELEARNINGHISTOGRAMML);
    }

    public  boolean wantMLMacd() {
        return (Boolean) getValueOrDefault(ConfigConstants.INDICATORSMACDMACHINELEARNINGMOMENTUMML);
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
        return (Boolean) getValueOrDefault(ConfigConstants.PREDICTORS); 
    }

    public boolean wantPredictorLSTM() {
        return (Boolean) getValueOrDefault(ConfigConstants.PREDICTORSLSTM) 
                && wantLSTM()
                && wantPredictors() 
                && wantMLTensorflow();
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

    public boolean wantAggregatorsIndicatorMACD() {
        return (Boolean) getValueOrDefault(ConfigConstants.AGGREGATORSINDICATORMACD)
                && wantAggregatorsIndicatorML();
    }

    public boolean wantAggregatorsIndicatorRSI() {
        return (Boolean) getValueOrDefault(ConfigConstants.AGGREGATORSINDICATORRSI)
                && wantAggregatorsIndicatorML();
    }

    public String getAggregatorsIndicatorExtras() {
        return (String) getValueOrDefault(ConfigConstants.AGGREGATORSINDICATOREXTRAS);
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

    public int getAggregatorsIndicatorFuturedays() {
        return (Integer) getValueOrDefault(ConfigConstants.AGGREGATORSINDICATORFUTUREDAYS);
    }

    public int getAggregatorsIndicatorIntervaldays() {
        return (Integer) getValueOrDefault(ConfigConstants.AGGREGATORSINDICATORINTERVALDAYS);
    }

    public double getAggregatorsIndicatorThreshold() {
        return (Double) getValueOrDefault(ConfigConstants.AGGREGATORSINDICATORTHRESHOLD);
    }

    public String getTensorflowServer() {
        return (String) getValueOrDefault(ConfigConstants.MACHINELEARNINGTENSORFLOWSERVER);              
    }

    public Object getValueOrDefault(String key) {
        Object retVal = getConfigValueMap().get(key);
        if (retVal != null) {
            Class classType = getType().get(key);
            if (retVal.getClass().isAssignableFrom(Integer.class) && classType.isAssignableFrom(Double.class)) {
                getConfigValueMap().put(key, Double.valueOf(((Integer)retVal).intValue()));
                retVal = Double.valueOf(((Integer)retVal).intValue());
            }
        }
        //System.out.println("r " + retVal + " " + deflt.get(key));
        return Optional.ofNullable(retVal).orElse(getDeflt().get(key));
    }

}
