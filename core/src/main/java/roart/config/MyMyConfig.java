package roart.config;

import org.apache.spark.api.java.Optional;

public class MyMyConfig extends MyPropertyConfig {

    public MyMyConfig(MyConfig config) {
        this.configTreeMap = config.configTreeMap;
        this.configValueMap = config.configValueMap;
        this.deflt = config.deflt;
        this.text = config.text;
        this.type = config.type;
        this.mydate = config.mydate;
        this.mymarket = config.mymarket;
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
    	return (Integer) getValueOrDefault(ConfigConstants.INDICATORSMACDMACHHISTOGRAMDELTADAYS);
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
        return (Boolean) getValueOrDefault(ConfigConstants.INDICATORSMACDRECOMMENDWEIGHTS);
    }

    public  boolean wantRSIScore() {
        return (Boolean) getValueOrDefault(ConfigConstants.INDICATORSRSIRECOMMENDWEIGHTS);
    }

    public void disableML() {
        configValueMap.put(ConfigConstants.MACHINELEARNING, Boolean.FALSE);
    }

    public  boolean wantML() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNING);
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

    public  boolean wantDNN() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGTENSORFLOWDNN)
        && wantMLTensorflow();
    }

    public  boolean wantL() {
        return (Boolean) getValueOrDefault(ConfigConstants.MACHINELEARNINGTENSORFLOWL)
        && wantMLTensorflow();
    }

    public  int weightBuyHist() {
        return (Integer) getValueOrDefault(ConfigConstants.INDICATORSMACDRECOMMENDWEIGHTSBUYHISTOGRAM);
    }

    public  int weightBuyHistDelta() {
        return (Integer) getValueOrDefault(ConfigConstants.INDICATORSMACDRECOMMENDWEIGHTSBUYHISTOGRAMDELTA);
    }

    public  int weightBuyMacd() {
        return (Integer) getValueOrDefault(ConfigConstants.INDICATORSMACDRECOMMENDWEIGHTSBUYMOMENTUM);
    }

    public  int weightBuyMacdDelta() {
        return (Integer) getValueOrDefault(ConfigConstants.INDICATORSMACDRECOMMENDWEIGHTSBUYMOMENTUMDELTA);
    }

    public  int weightSellHist() {
        return (Integer) getValueOrDefault(ConfigConstants.INDICATORSMACDRECOMMENDWEIGHTSSELLHISTOGRAM);
    }

    public  int weightSellHistDelta() {
        return (Integer) getValueOrDefault(ConfigConstants.INDICATORSMACDRECOMMENDWEIGHTSSELLHISTOGRAMDELTA);
    }

    public  int weightSellMacd() {
        return (Integer) getValueOrDefault(ConfigConstants.INDICATORSMACDRECOMMENDWEIGHTSSELLMOMENTUM);
    }

    public  int weightSellMacdDelta() {
        return (Integer) getValueOrDefault(ConfigConstants.INDICATORSMACDRECOMMENDWEIGHTSSELLMOMENTUMDELTA);
    }

    public  boolean wantRecommenderRSI() {
        return (Boolean) getValueOrDefault(ConfigConstants.INDICATORSRSIRECOMMENDWEIGHTS);
    }

    public  boolean wantRecommenderMACD() {
        return (Boolean) getValueOrDefault(ConfigConstants.INDICATORSMACDRECOMMENDWEIGHTS);
    }

    public  int buyWeightRSI() {
        return (Integer) getValueOrDefault(ConfigConstants.INDICATORSRSIRECOMMENDWEIGHTSBUY);
    }

    public  int buyWeightRSIDelta() {
        return (Integer) getValueOrDefault(ConfigConstants.INDICATORSRSIRECOMMENDWEIGHTSBUYDELTA);
    }

    public  int sellWeightRSI() {
        return (Integer) getValueOrDefault(ConfigConstants.INDICATORSRSIRECOMMENDWEIGHTSSELL);
    }

    public  int sellWeightRSIDelta() {
        return (Integer) getValueOrDefault(ConfigConstants.INDICATORSRSIRECOMMENDWEIGHTSSELLDELTA);
    }

    public  boolean wantMLHist() {
        return (Boolean) getValueOrDefault(ConfigConstants.INDICATORSMACDMACHINELEARNINGHISTOGRAMML);
    }

    public  boolean wantMLMacd() {
        return (Boolean) getValueOrDefault(ConfigConstants.INDICATORSMACDMACHINELEARNINGMOMENTUMML);
    }

    public boolean wantMLTimes() {
        return (Boolean) configValueMap.get(ConfigConstants.MISCMLSTATS)
        && wantML();
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
        return (Boolean) getValueOrDefault(ConfigConstants.DATABASESPARK);
    }

    public boolean wantDbHibernate() {
        System.out.println("hb " + (Boolean) configValueMap.get(ConfigConstants.DATABASEHIBERNATE));
        return (Boolean) getValueOrDefault(ConfigConstants.DATABASEHIBERNATE);
    }

    public boolean wantPredictors() {
        return (Boolean) getValueOrDefault(ConfigConstants.PREDICTORS); 
    }

    public boolean wantPredictorLSTM() {
        return (Boolean) getValueOrDefault(ConfigConstants.PREDICTORSLSTM) 
                && wantPredictors() 
                && wantMLTensorflow();
    }

    public Integer getPredictorLSTMHorizon() {
        return (Integer) getValueOrDefault(ConfigConstants.PREDICTORSLSTMHORIZON);
    }

    public Integer getPredictorLSTMEpochs() {
        return (Integer) getValueOrDefault(ConfigConstants.PREDICTORSLSTMEPOCHS);
    }

    public Integer getPredictorLSTMWindowsize() {
        return (Integer) getValueOrDefault(ConfigConstants.PREDICTORSLSTMWINDOWSIZE);
    }

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

    public Integer getTestIndicatorRecommenderComplexFutureDays() {
        return (Integer) getValueOrDefault(ConfigConstants.TESTINDICATORRECOMMENDERCOMPLEXFUTUREDAYS);
    }

    public Integer getTestIndicatorRecommenderComplexIntervalDays() {
        return (Integer) getValueOrDefault(ConfigConstants.TESTINDICATORRECOMMENDERCOMPLEXINTERVALDAYS);
    }

    public Integer getTestIndicatorRecommenderSimpleFutureDays() {
        return (Integer) getValueOrDefault(ConfigConstants.TESTINDICATORRECOMMENDERSIMPLEFUTUREDAYS);
    }

    public Integer getTestIndicatorRecommenderSimpleIntervalDays() {
        return (Integer) getValueOrDefault(ConfigConstants.TESTINDICATORRECOMMENDERSIMPLEINTERVALDAYS);
    }

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
       return (Boolean) getValueOrDefault(ConfigConstants.AGGREGATORSINDICATOR);
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
   
   private Object getValueOrDefault(String key) {
       Object retVal = configValueMap.get(key);
       //System.out.println("r " + retVal + " " + deflt.get(key));
       return Optional.ofNullable(retVal).orElse(deflt.get(key));
   }
}
