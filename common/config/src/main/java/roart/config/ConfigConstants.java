package roart.config;

import java.util.HashMap;
import java.util.Map;

public class ConfigConstants {
    public static final String PROPFILE = "stockstat.prop";
    public static final String CONFIGFILE = "stockstat.xml";
   public static final String SPARK = "spark";
    public static final String HIBERNATE = "hibernate";
    public static final String SPARKMASTER = "sparkmaster";
    public static final String[] dbvalues = { HIBERNATE, SPARK };
    public static final String TENSORFLOW = "tensorflow";
    public static final String DATABASESPARK = "database.spark[@enable]";
    public static final String DATABASESPARKSPARKMASTER = "database.spark.sparkmaster";
    public static final String DATABASEHIBERNATE = "database.hibernate[@enable]";
    public static final String MACHINELEARNING = "machinelearning[@enable]";
    public static final String MACHINELEARNINGSPARKML = "machinelearning.sparkml[@enable]";
    public static final String MACHINELEARNINGSPARMMLSPARKMASTER = "machinelearning.sparkml.sparkmaster";
    public static final String MACHINELEARNINGSPARKMLMCP = "machinelearning.sparkml.mcp[@enable]";
    public static final String MACHINELEARNINGSPARKMLLR = "machinelearning.sparkml.lr[@enable]";
    public static final String MACHINELEARNINGTENSORFLOW = "machinelearning.tensorflow[@enable]";
    public static final String MACHINELEARNINGTENSORFLOWDNN = "machinelearning.tensorflow.dnn[@enable]";
    public static final String MACHINELEARNINGTENSORFLOWL = "machinelearning.tensorflow.l[@enable]";
    public static final String INDICATORS = "indicators[@enable]";
    public static final String INDICATORSMOVE = "indicators.move[@enable]";
    public static final String INDICATORSMACD = "indicators.macd[@enable]";
    public static final String INDICATORSMACDMACDHISTOGRAMDELTA = "indicators.macd.macdhistogramdelta[@enable]";
    public static final String INDICATORSMACDMACHHISTOGRAMDELTADAYS = "indicators.macd.macdhistogramdeltadays";
    
    public static final String  INDICATORSMACDMACDMOMENTUMDELTA = "indicators.macd.macdmomentumdelta[@enable]";
    public static final String INDICATORSMACDACDMOMENTUMDELTADAYS = "indicators.macd.macdmomentumdeltadays";
    public static final String  INDICATORSMACDRECOMMENDWEIGHTS = "indicators.macd.recommend[@enable]";
    public static final String  INDICATORSMACDRECOMMENDWEIGHTSBUYHISTOGRAM = "indicators.macd.recommend.weightbuyhistogram";
    public static final String  INDICATORSMACDRECOMMENDWEIGHTSBUYHISTOGRAMDELTA = "indicators.macd.recommend.weightbuyhistogramdelta";
    public static final String  INDICATORSMACDRECOMMENDWEIGHTSBUYMOMENTUM = "indicators.macd.recommend.weightbuymomemtum";
    public static final String  INDICATORSMACDRECOMMENDWEIGHTSBUGMOMENTUMDELTA = "indicators.macd.recommend.weightbuymomemtumdelta";
    public static final String  INDICATORSMACDRECOMMENDWEIGHTSSELLHISTOGRAM = "indicators.macd.recommend.weightsellhistogram";
    public static final String  INDICATORSMACDRECOMMENDWEIGHTSSELLHISTOGRAMDELTA = "indicators.macd.recommend.weightsellhistogramdelta";
    public static final String  INDICATORSMACDRECOMMENDWEIGHTSSELLMOMENTUM = "indicators.macd.recommend.weightsellmomemtum";
    public static final String INDICATORSMACDRECOMMENDWEIGHTSSELLMOMENTUMDELTA = "indicators.macd.recommend.weightsellmomemtumdelta";
    public static final String INDICATORSMACDMACHINELEARNING = "indicators.macd.machinelearning[@enable]";
    public static final String INDICATORSMACDMACHINELEARNINGMOMENTUMML = "indicators.macd.machinelearning.momemtumml[@enable]";
    public static final String INDICATORSMACDMACHINELEARNINGHISTOGRAMML = "indicators.macd.machinelearning.histogramml[@enable]";
    public static final String  INDICATORSMACDDAYSBEFOREZERO ="indicators.macd.daysbeforezero";
    public static final String  INDICATORSMACDDAYSAFTERZERO = "indicators.macd.daysafterzero";
    public static final String  INDICATORSRSI = "indicators.rsi[@enable]";
    public static final String  INDICATORSRSIDELTA = "indicators.rsi.rsidelta[@enable]";
    public static final String  INDICATORSRSIDELTADAYS = "indicators.rsi.rsideltadays";
    public static final String  INDICATORSSTOCHRSI = "indicators.stochrsi[@enable]";
    public static final String  INDICATORSSTOCHRSIDELTA = "indicators.stochrsi.stochrsidelta[@enable]";
    public static final String  INDICATORSSTOCHRSIDELTADAYS = "indicators.stochrsi.stochrsideltadays";
    public static final String  INDICATORSCCI = "indicators.cci[@enable]";
    public static final String  INDICATORSCCIDELTA ="indicators.cci.ccidelta[@enable]";
    public static final String INDICATORSCCIDELTADAYS = "indicators.cci.ccideltadays";
    public static final String INDICATORSATR ="indicators.atr[@enable]";
    public static final String INDICATORSATRDELTA = "indicators.atr.atrdelta[@enable]";
    public static final String INDICATORSATRDELTADAYS = "indicators.atr.atrdeltadays";
    public static final String INDICATORSSTOCH = "indicators.stoch[@enable]";
    public static final String INDICATORSSTOCHSTOCHDELTA ="indicators.stoch.stochdelta[@enable]";
    public static final String INDICATORSSTOCHSTOCHDELTADAYS = "indicators.stoch.stochdeltadays";
    public static final String MISC = "misc";
    public static final String MISCPERCENTIZEPRICEINDEX = "misc.percentizepriceindex[@enable]";
    public static final String MISCMLSTATS = "misc.mlstats[@enable]";
    public static final String MISCOTHERSTATS = "misc.otherstats[@enable]";
    public static final String MISCMYDAYS = "misc.mydays";
    public static final String MISCMYTOPBOTTOM = "misc.mytopbottom";
    public static final String MISCMYTBLEDAYS = "misc.mytabledays";
    public static final String MISCMYTABLEMOVEINTERVALDAYS = "misc.mytablemoveintervaldays";
    public static final String MISCMYTABLEINTERVALDAYS = "misc.mytableintervaldays";
    public static final String MISCMYEQUALIZE = "misc.myequalize[@enable]";
    public static final String MISCMYGRAPHEQUALIZE = "misc.mygraphequalize[@enable]";
    public static final String MISCMYGRAPHEQUALIZEUNIFY = "misc.mygraphequalizeunify[@enable]";

    public static Map<String, Class> map = new HashMap();
    
    public static void makeTypeMap() {
        if (!map.isEmpty()) {
            return;
        }
        map.put(ConfigConstants.DATABASESPARK, Boolean.class);
        map.put(ConfigConstants.DATABASESPARKSPARKMASTER, String.class);
        map.put(ConfigConstants.DATABASEHIBERNATE, Boolean.class);
        map.put(ConfigConstants.MACHINELEARNING, Boolean.class);
        map.put(ConfigConstants.MACHINELEARNINGSPARKML, Boolean.class);
        map.put(ConfigConstants.MACHINELEARNINGSPARMMLSPARKMASTER, String.class);
        map.put(ConfigConstants.MACHINELEARNINGSPARKMLMCP, Boolean.class);
        map.put(ConfigConstants.MACHINELEARNINGSPARKMLLR, Boolean.class);
        map.put(ConfigConstants.MACHINELEARNINGTENSORFLOW, Boolean.class);
        map.put(ConfigConstants.MACHINELEARNINGTENSORFLOWDNN, Boolean.class);
        map.put(ConfigConstants.MACHINELEARNINGTENSORFLOWL, Boolean.class);
        map.put(ConfigConstants.INDICATORS, Boolean.class);
        map.put(ConfigConstants.INDICATORSMOVE, Boolean.class);
        map.put(ConfigConstants.INDICATORSMACD, Boolean.class);
        map.put(ConfigConstants.INDICATORSMACDMACDHISTOGRAMDELTA, Boolean.class);
        map.put(ConfigConstants.INDICATORSMACDMACHHISTOGRAMDELTADAYS, Integer.class);
        
        map.put(ConfigConstants. INDICATORSMACDMACDMOMENTUMDELTA, Boolean.class);
        map.put(ConfigConstants.INDICATORSMACDACDMOMENTUMDELTADAYS, Integer.class);
        map.put(ConfigConstants. INDICATORSMACDRECOMMENDWEIGHTS, Boolean.class);
        map.put(ConfigConstants. INDICATORSMACDRECOMMENDWEIGHTSBUYHISTOGRAM, Integer.class);
        map.put(ConfigConstants. INDICATORSMACDRECOMMENDWEIGHTSBUYHISTOGRAMDELTA, Integer.class);
        map.put(ConfigConstants. INDICATORSMACDRECOMMENDWEIGHTSBUYMOMENTUM, Integer.class);
        map.put(ConfigConstants. INDICATORSMACDRECOMMENDWEIGHTSBUGMOMENTUMDELTA, Integer.class);
        map.put(ConfigConstants. INDICATORSMACDRECOMMENDWEIGHTSSELLHISTOGRAM, Integer.class);
        map.put(ConfigConstants. INDICATORSMACDRECOMMENDWEIGHTSSELLHISTOGRAMDELTA, Integer.class);
        map.put(ConfigConstants. INDICATORSMACDRECOMMENDWEIGHTSSELLMOMENTUM, Integer.class);
        map.put(ConfigConstants.INDICATORSMACDRECOMMENDWEIGHTSSELLMOMENTUMDELTA, Integer.class);
        map.put(ConfigConstants.INDICATORSMACDMACHINELEARNING, Boolean.class);
        map.put(ConfigConstants.INDICATORSMACDMACHINELEARNINGMOMENTUMML, Boolean.class);
        map.put(ConfigConstants.INDICATORSMACDMACHINELEARNINGHISTOGRAMML, Boolean.class);
        map.put(ConfigConstants. INDICATORSMACDDAYSBEFOREZERO , Integer.class);
        map.put(ConfigConstants. INDICATORSMACDDAYSAFTERZERO, Integer.class);
        map.put(ConfigConstants. INDICATORSRSI, Boolean.class);
        map.put(ConfigConstants. INDICATORSRSIDELTA, Boolean.class);
        map.put(ConfigConstants. INDICATORSRSIDELTADAYS, Integer.class);
        map.put(ConfigConstants. INDICATORSSTOCHRSI, Boolean.class);
        map.put(ConfigConstants. INDICATORSSTOCHRSIDELTA, Boolean.class);
        map.put(ConfigConstants. INDICATORSSTOCHRSIDELTADAYS, Integer.class);
        map.put(ConfigConstants. INDICATORSCCI, Boolean.class);
        map.put(ConfigConstants. INDICATORSCCIDELTA , Boolean.class);
        map.put(ConfigConstants.INDICATORSCCIDELTADAYS, Integer.class);
        map.put(ConfigConstants.INDICATORSATR , Boolean.class);
        map.put(ConfigConstants.INDICATORSATRDELTA, Boolean.class);
        map.put(ConfigConstants.INDICATORSATRDELTADAYS, Integer.class);
        map.put(ConfigConstants.INDICATORSSTOCH, Boolean.class);
        map.put(ConfigConstants.INDICATORSSTOCHSTOCHDELTA , Boolean.class);
        map.put(ConfigConstants.INDICATORSSTOCHSTOCHDELTADAYS, Integer.class);
        map.put(ConfigConstants.MISC, Boolean.class);
        map.put(ConfigConstants.MISCPERCENTIZEPRICEINDEX, Boolean.class);
        map.put(ConfigConstants.MISCMLSTATS, Boolean.class);
        map.put(ConfigConstants.MISCOTHERSTATS, Boolean.class);
        map.put(ConfigConstants.MISCMYDAYS, Integer.class);
        map.put(ConfigConstants.MISCMYTOPBOTTOM, Integer.class);
        map.put(ConfigConstants.MISCMYTBLEDAYS, Integer.class);
        map.put(ConfigConstants.MISCMYTABLEMOVEINTERVALDAYS, Integer.class);
        map.put(ConfigConstants.MISCMYTABLEINTERVALDAYS, Integer.class);
        map.put(ConfigConstants.MISCMYEQUALIZE, Boolean.class);
        map.put(ConfigConstants.MISCMYGRAPHEQUALIZE, Boolean.class);
        map.put(ConfigConstants.MISCMYGRAPHEQUALIZEUNIFY, Boolean.class);
       
    }

    public static Map<String, String> text = new HashMap();
    
    public static void makeTextMap() {
        if (!text.isEmpty()) {
            return;
        }
        text.put(ConfigConstants.DATABASESPARK, "Enable Spark Database backend");
        text.put(ConfigConstants.DATABASESPARKSPARKMASTER, "Database Spark Master");
        text.put(ConfigConstants.DATABASEHIBERNATE, "Enable Hibernate Database backend");
        text.put(ConfigConstants.MACHINELEARNING, "Enable machine learning");
        text.put(ConfigConstants.MACHINELEARNINGSPARKML, "Enable Spark ML");
        text.put(ConfigConstants.MACHINELEARNINGSPARMMLSPARKMASTER, "Machine Learning Spark Master");
        text.put(ConfigConstants.MACHINELEARNINGSPARKMLMCP, "Enable Spark ML MCP");
        text.put(ConfigConstants.MACHINELEARNINGSPARKMLLR, "Enable Spark ML LR");
        text.put(ConfigConstants.MACHINELEARNINGTENSORFLOW, "Enable Tensorflow");
        text.put(ConfigConstants.MACHINELEARNINGTENSORFLOWDNN, "Enable Tensorflow DNN");
        text.put(ConfigConstants.MACHINELEARNINGTENSORFLOWL, "Enable Tensorflow L");
        text.put(ConfigConstants.INDICATORS, "Enable indicators");
        text.put(ConfigConstants.INDICATORSMOVE, "Enable move indicator");
        text.put(ConfigConstants.INDICATORSMACD, "Enable MACD indicator");
        text.put(ConfigConstants.INDICATORSMACDMACDHISTOGRAMDELTA, "Enable MACD histogram delta");
        text.put(ConfigConstants.INDICATORSMACDMACHHISTOGRAMDELTADAYS, "MACD histogram delta days");
        
        text.put(ConfigConstants. INDICATORSMACDMACDMOMENTUMDELTA, "Enable MACD momentum delta");
        text.put(ConfigConstants.INDICATORSMACDACDMOMENTUMDELTADAYS, "MACD momentum delta days");
        text.put(ConfigConstants. INDICATORSMACDRECOMMENDWEIGHTS, "Enable MACD buy/sell recommendation");
        text.put(ConfigConstants. INDICATORSMACDRECOMMENDWEIGHTSBUYHISTOGRAM, "Buy weight histogram");
        text.put(ConfigConstants. INDICATORSMACDRECOMMENDWEIGHTSBUYHISTOGRAMDELTA, "Buy weight histogram delta");
        text.put(ConfigConstants. INDICATORSMACDRECOMMENDWEIGHTSBUYMOMENTUM, "Buy weight momentum");
        text.put(ConfigConstants. INDICATORSMACDRECOMMENDWEIGHTSBUGMOMENTUMDELTA, "Buy weight momentum delta");
        text.put(ConfigConstants. INDICATORSMACDRECOMMENDWEIGHTSSELLHISTOGRAM, "Sell weight histogram");
        text.put(ConfigConstants. INDICATORSMACDRECOMMENDWEIGHTSSELLHISTOGRAMDELTA, "Sell weight histogram delta");
        text.put(ConfigConstants. INDICATORSMACDRECOMMENDWEIGHTSSELLMOMENTUM, "Sell weight momentum");
        text.put(ConfigConstants.INDICATORSMACDRECOMMENDWEIGHTSSELLMOMENTUMDELTA, "Sell weight momentum delta");
        text.put(ConfigConstants.INDICATORSMACDMACHINELEARNING, "Enable indicator MACD machine learning");
        text.put(ConfigConstants.INDICATORSMACDMACHINELEARNINGMOMENTUMML, "Enable indicator MACD momentum machine learning");
        text.put(ConfigConstants.INDICATORSMACDMACHINELEARNINGHISTOGRAMML, "Enable indicator MACD histogram machine learning");
        text.put(ConfigConstants. INDICATORSMACDDAYSBEFOREZERO , "Days before zero");
        text.put(ConfigConstants. INDICATORSMACDDAYSAFTERZERO, "Days after zero");
        text.put(ConfigConstants. INDICATORSRSI, "Enable indicator RSI");
        text.put(ConfigConstants. INDICATORSRSIDELTA, "Enable indicator RSI delta");
        text.put(ConfigConstants. INDICATORSRSIDELTADAYS, "RSI delta days");
        text.put(ConfigConstants. INDICATORSSTOCHRSI, "Enable indicator STOCH RSI");
        text.put(ConfigConstants. INDICATORSSTOCHRSIDELTA, "Enable indicator STOCH RSI delta");
        text.put(ConfigConstants. INDICATORSSTOCHRSIDELTADAYS, "STOCH RSI delta days");
        text.put(ConfigConstants. INDICATORSCCI, "Enable indicator CCI");
        text.put(ConfigConstants. INDICATORSCCIDELTA , "Enable indicator CCI delta");
        text.put(ConfigConstants.INDICATORSCCIDELTADAYS, "CCI delta days");
        text.put(ConfigConstants.INDICATORSATR , "Enable indicator ATR");
        text.put(ConfigConstants.INDICATORSATRDELTA, "Enable indicator ATR delta");
        text.put(ConfigConstants.INDICATORSATRDELTADAYS, "ATR delta days");
        text.put(ConfigConstants.INDICATORSSTOCH, "Enable indicator STOCH");
        text.put(ConfigConstants.INDICATORSSTOCHSTOCHDELTA , "Enable indicator STOCH delta");
        text.put(ConfigConstants.INDICATORSSTOCHSTOCHDELTADAYS, "STOCH delta days");
        text.put(ConfigConstants.MISC, "Misc");
        text.put(ConfigConstants.MISCPERCENTIZEPRICEINDEX, "Enable turning price/index into percent based on first date");
        text.put(ConfigConstants.MISCMLSTATS, "Enable ML stats for time usage");
        text.put(ConfigConstants.MISCOTHERSTATS, "Enable other stat pages");
        text.put(ConfigConstants.MISCMYDAYS, "Number of days to display");
        text.put(ConfigConstants.MISCMYTOPBOTTOM, "Number of items to display");
        text.put(ConfigConstants.MISCMYTBLEDAYS, "Table days");
        text.put(ConfigConstants.MISCMYTABLEMOVEINTERVALDAYS, "Interval days for table move");
        text.put(ConfigConstants.MISCMYTABLEINTERVALDAYS, "Table interval days");
        text.put(ConfigConstants.MISCMYEQUALIZE, "Enable equalizing");
        text.put(ConfigConstants.MISCMYGRAPHEQUALIZE, "Enable graph equalizing");
        text.put(ConfigConstants.MISCMYGRAPHEQUALIZEUNIFY, "Enable unified graph equalizing");
       
    }


}
