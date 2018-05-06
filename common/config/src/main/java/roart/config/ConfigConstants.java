package roart.config;

public class ConfigConstants {
    public static final String PROPFILE = "stockstat.prop";
    public static final String CONFIGFILE = "stockstat.xml";
    public static final String SPARK = "spark";
    public static final String HIBERNATE = "hibernate";
    public static final String SPARKMASTER = "sparkmaster";
    public static final String[] dbvalues = { HIBERNATE, SPARK };
    public static final String TENSORFLOW = "tensorflow";
    public static final String DATABASEMAXHOLES = "database.maxholes";
    public static final String DATABASESPARK = "database.spark[@enable]";
    public static final String DATABASESPARKSPARKMASTER = "database.spark.sparkmaster";
    public static final String DATABASEHIBERNATE = "database.hibernate[@enable]";
    public static final String MACHINELEARNING = "machinelearning[@enable]";
    public static final String MACHINELEARNINGMP = "machinelearning.mp[@enable]";
    public static final String MACHINELEARNINGMPCPU = "machinelearning.mp.cpu";
    public static final String MACHINELEARNINGSPARKML = "machinelearning.sparkml[@enable]";
    public static final String MACHINELEARNINGSPARKMLSPARKMASTER = "machinelearning.sparkml.sparkmaster";
    public static final String MACHINELEARNINGSPARKMLSPARKNETWORKTIMEOUT = "machinelearning.sparkml.sparknetworktimeout";
    public static final String MACHINELEARNINGSPARKMLMCP = "machinelearning.sparkml.mcp[@enable]";
    public static final String MACHINELEARNINGSPARKMLLR = "machinelearning.sparkml.lr[@enable]";
    public static final String MACHINELEARNINGSPARKMLOVR = "machinelearning.sparkml.ovr[@enable]";
    public static final String MACHINELEARNINGTENSORFLOW = "machinelearning.tensorflow[@enable]";
    public static final String MACHINELEARNINGTENSORFLOWDNN = "machinelearning.tensorflow.dnn[@enable]";
    public static final String MACHINELEARNINGTENSORFLOWDNNL = "machinelearning.tensorflow.dnnl[@enable]";
    public static final String MACHINELEARNINGTENSORFLOWL = "machinelearning.tensorflow.l[@enable]";
    public static final String MACHINELEARNINGTENSORFLOWSERVER = "machinelearning.tensorflow.server";
    public static final String INDICATORS = "indicators[@enable]";
    public static final String INDICATORSMOVE = "indicators.move[@enable]";
    public static final String INDICATORSMACD = "indicators.macd[@enable]";
    public static final String INDICATORSMACDMACDHISTOGRAMDELTA = "indicators.macd.macdhistogramdelta[@enable]";
    public static final String INDICATORSMACDMACHHISTOGRAMDELTADAYS = "indicators.macd.macdhistogramdeltadays";
    public static final String INDICATORSMACDMACDMOMENTUMDELTA = "indicators.macd.macdmomentumdelta[@enable]";
    public static final String INDICATORSMACDACDMOMENTUMDELTADAYS = "indicators.macd.macdmomentumdeltadays";
    public static final String INDICATORSMACDRECOMMEND = "indicators.macd.recommend[@enable]";
    public static final String INDICATORSMACDRECOMMENDBUY = "indicators.macd.recommend.buy";
    public static final String INDICATORSMACDRECOMMENDBUYWEIGHTHISTOGRAM = "indicators.macd.recommend.buy.weighthistogram";
    public static final String INDICATORSMACDRECOMMENDBUYWEIGHTHISTOGRAMDELTA = "indicators.macd.recommend.buy.weighthistogramdelta";
    public static final String INDICATORSMACDRECOMMENDBUYWEIGHTMOMENTUM = "indicators.macd.recommend.buy.weightmomemtum";
    public static final String INDICATORSMACDRECOMMENDBUYWEIGHTMOMENTUMDELTA = "indicators.macd.recommend.buy.weightmomemtumdelta";
    public static final String INDICATORSMACDRECOMMENDSELL = "indicators.macd.recommend.sell";
    public static final String INDICATORSMACDRECOMMENDSELLWEIGHTHISTOGRAM = "indicators.macd.recommend.sell.weighthistogram";
    public static final String INDICATORSMACDRECOMMENDSELLWEIGHTHISTOGRAMDELTA = "indicators.macd.recommend.sell.weighthistogramdelta";
    public static final String INDICATORSMACDRECOMMENDSELLWEIGHTMOMENTUM = "indicators.macd.recommend.sell.weightmomemtum";
    public static final String INDICATORSMACDRECOMMENDSELLWEIGHTMOMENTUMDELTA = "indicators.macd.recommend.sell.weightmomemtumdelta";
    public static final String INDICATORSMACDMACHINELEARNING = "indicators.macd.machinelearning[@enable]";
    public static final String INDICATORSMACDMACHINELEARNINGMOMENTUMML = "indicators.macd.machinelearning.momemtumml[@enable]";
    public static final String INDICATORSMACDMACHINELEARNINGHISTOGRAMML = "indicators.macd.machinelearning.histogramml[@enable]";
    public static final String INDICATORSMACDDAYSBEFOREZERO ="indicators.macd.daysbeforezero";
    public static final String INDICATORSMACDDAYSAFTERZERO = "indicators.macd.daysafterzero";
    public static final String INDICATORSRSI = "indicators.rsi[@enable]";
    public static final String INDICATORSRSIDELTA = "indicators.rsi.rsidelta[@enable]";
    public static final String INDICATORSRSIDELTADAYS = "indicators.rsi.rsideltadays";
    public static final String INDICATORSSTOCHRSI = "indicators.stochrsi[@enable]";
    public static final String INDICATORSSTOCHRSIDELTA = "indicators.stochrsi.stochrsidelta[@enable]";
    public static final String INDICATORSSTOCHRSIDELTADAYS = "indicators.stochrsi.stochrsideltadays";
    public static final String INDICATORSRSIRECOMMEND = "indicators.rsi.recommend[@enable]";
    public static final String INDICATORSRSIRECOMMENDBUY = "indicators.rsi.recommend.buy";
    public static final String INDICATORSRSIRECOMMENDBUYWEIGHT = "indicators.rsi.recommend.buy.weight";
    public static final String INDICATORSRSIRECOMMENDBUYWEIGHTDELTA = "indicators.rsi.recommend.buy.weightdelta";
    public static final String INDICATORSRSIRECOMMENDSELL = "indicators.rsi.recommend.sell";
    public static final String INDICATORSRSIRECOMMENDSELLWEIGHT = "indicators.rsi.recommend.sell.weight";
    public static final String INDICATORSRSIRECOMMENDSELLWEIGHTDELTA = "indicators.rsi.recommend.sell.weightdelta";
    public static final String INDICATORSCCI = "indicators.cci[@enable]";
    public static final String INDICATORSCCIDELTA ="indicators.cci.ccidelta[@enable]";
    public static final String INDICATORSCCIDELTADAYS = "indicators.cci.ccideltadays";
    public static final String INDICATORSATR ="indicators.atr[@enable]";
    public static final String INDICATORSATRDELTA = "indicators.atr.atrdelta[@enable]";
    public static final String INDICATORSATRDELTADAYS = "indicators.atr.atrdeltadays";
    public static final String INDICATORSSTOCH = "indicators.stoch[@enable]";
    public static final String INDICATORSSTOCHSTOCHDELTA ="indicators.stoch.stochdelta[@enable]";
    public static final String INDICATORSSTOCHSTOCHDELTADAYS = "indicators.stoch.stochdeltadays";
    public static final String PREDICTORS = "predictors[@enable]";
    public static final String PREDICTORSLSTM = "predictors.lstm[@enable]";
    public static final String PREDICTORSLSTMWINDOWSIZE = "predictors.lstm.windowsize";
    public static final String PREDICTORSLSTMHORIZON = "predictors.lstm.horizon";
    public static final String PREDICTORSLSTMEPOCHS = "predictors.lstm.epochs";
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
    public static final String EVOLVEINDICATORRECOMMENDEREVOLUTIONCONFIG = "evolve.indicatorrecommender.evolutionconfig";
    public static final String EVOLVEINDICATORRECOMMENDERCOMPLEXFUTUREDAYS = "evolve.indicatorrecommender.complex.futuredays";
    public static final String EVOLVEINDICATORRECOMMENDERCOMPLEXINTERVALDAYS = "evolve.indicatorrecommender.complex.intervaldays";
    public static final String EVOLVEINDICATORRECOMMENDERSIMPLEFUTUREDAYS = "evolve.indicatorrecommender.simple.futuredays";
    public static final String EVOLVEINDICATORRECOMMENDERSIMPLEINTERVALDAYS = "evolve.indicatorrecommender.simple.intervaldays";
    public static final String EVOLVEMLEVOLUTIONCONFIG = "evolve.ml.evolutionconfig";
    public static final String AGGREGATORS = "aggregators[@enable]";
    public static final String AGGREGATORSINDICATORRECOMMENDER = "aggregators.indicatorrecommender[@enable]";
    public static final String AGGREGATORSINDICATORRECOMMENDERSIMPLE = "aggregators.indicatorrecommender.simple[@enable]";
    public static final String AGGREGATORSINDICATORRECOMMENDERSIMPLEMACD = "aggregators.indicatorrecommender.simple.macd[@enable]";
    public static final String AGGREGATORSINDICATORRECOMMENDERSIMPLEMACDBUY = "aggregators.indicatorrecommender.simple.macd.buy";
    public static final String AGGREGATORSINDICATORRECOMMENDERSIMPLEMACDBUYWEIGHTHISTOGRAM = "aggregators.indicatorrecommender.simple.macd.buy.weighthistogram";
    public static final String AGGREGATORSINDICATORRECOMMENDERSIMPLEMACDBUYWEIGHTHISTOGRAMDELTA = "aggregators.indicatorrecommender.simple.macd.buy.weighthistogramdelta";
    public static final String AGGREGATORSINDICATORRECOMMENDERSIMPLEMACDBUYWEIGHTMOMENTUM = "aggregators.indicatorrecommender.simple.macd.buy.weightmomemtum";
    public static final String AGGREGATORSINDICATORRECOMMENDERSIMPLEMACDBUYWEIGHTMOMENTUMDELTA = "aggregators.indicatorrecommender.simple.macd.buy.weightmomemtumdelta";
    public static final String AGGREGATORSINDICATORRECOMMENDERSIMPLEMACDSELL = "aggregators.indicatorrecommender.simple.macd.sell";
    public static final String AGGREGATORSINDICATORRECOMMENDERSIMPLEMACDSELLWEIGHTHISTOGRAM = "aggregators.indicatorrecommender.simple.macd.sell.weighthistogram";
    public static final String AGGREGATORSINDICATORRECOMMENDERSIMPLEMACDSELLWEIGHTHISTOGRAMDELTA = "aggregators.indicatorrecommender.simple.macd.sell.weighthistogramdelta";
    public static final String AGGREGATORSINDICATORRECOMMENDERSIMPLEMACDSELLWEIGHTMOMENTUM = "aggregators.indicatorrecommender.simple.macd.sell.weightmomemtum";
    public static final String AGGREGATORSINDICATORRECOMMENDERSIMPLEMACDSELLWEIGHTMOMENTUMDELTA = "aggregators.indicatorrecommender.simple.macd.sell.weightmomemtumdelta";
    public static final String AGGREGATORSINDICATORRECOMMENDERSIMPLERSI = "aggregators.indicatorrecommender.simple.rsi[@enable]";
    public static final String AGGREGATORSINDICATORRECOMMENDERSIMPLERSIBUY = "aggregators.indicatorrecommender.simple.rsi.buy";
    public static final String AGGREGATORSINDICATORRECOMMENDERSIMPLERSIBUYWEIGHTRSI = "aggregators.indicatorrecommender.simple.rsi.buy.weightrsi";
    public static final String AGGREGATORSINDICATORRECOMMENDERSIMPLERSIBUYWEIGHTRSIDELTA = "aggregators.indicatorrecommender.simple.rsi.buy.weightrsidelta";
    public static final String AGGREGATORSINDICATORRECOMMENDERSIMPLERSISELL = "aggregators.indicatorrecommender.simple.rsi.sell";
    public static final String AGGREGATORSINDICATORRECOMMENDERSIMPLERSISELLWEIGHTRSI = "aggregators.indicatorrecommender.simple.rsi.sell.weightrsi";
    public static final String AGGREGATORSINDICATORRECOMMENDERSIMPLERSISELLWEIGHTRSIDELTA = "aggregators.indicatorrecommender.simple.rsi.sell.weightrsidelta";
    public static final String AGGREGATORSINDICATORRECOMMENDERCOMPLEX = "aggregators.indicatorrecommender.complex[@enable]";
    public static final String AGGREGATORSINDICATORRECOMMENDERCOMPLEXMACD = "aggregators.indicatorrecommender.complex.macd[@enable]";
    public static final String AGGREGATORSINDICATORRECOMMENDERCOMPLEXMACDBUY = "aggregators.indicatorrecommender.complex.macd.buy";
    public static final String AGGREGATORSINDICATORRECOMMENDERCOMPLEXMACDBUYWEIGHTHISTOGRAMNODE = "aggregators.indicatorrecommender.complex.macd.buy.weighthistogramnode";
    public static final String AGGREGATORSINDICATORRECOMMENDERCOMPLEXMACDBUYWEIGHTHISTOGRAMDELTANODE = "aggregators.indicatorrecommender.complex.macd.buy.weighthistogramdeltanode";
    public static final String AGGREGATORSINDICATORRECOMMENDERCOMPLEXMACDBUYWEIGHTMOMENTUMNODE = "aggregators.indicatorrecommender.complex.macd.buy.weightmomemtumnode";
    public static final String AGGREGATORSINDICATORRECOMMENDERCOMPLEXMACDBUYWEIGHTMOMENTUMDELTANODE = "aggregators.indicatorrecommender.complex.macd.buy.weightmomemtumdeltanode";
    public static final String AGGREGATORSINDICATORRECOMMENDERCOMPLEXMACDSELL = "aggregators.indicatorrecommender.complex.macd.sell";
    public static final String AGGREGATORSINDICATORRECOMMENDERCOMPLEXMACDSELLWEIGHTHISTOGRAMNODE = "aggregators.indicatorrecommender.complex.macd.sell.weighthistogramnode";
    public static final String AGGREGATORSINDICATORRECOMMENDERCOMPLEXMACDSELLWEIGHTHISTOGRAMDELTANODE = "aggregators.indicatorrecommender.complex.macd.sell.weighthistogramdeltanode";
    public static final String AGGREGATORSINDICATORRECOMMENDERCOMPLEXMACDSELLWEIGHTMOMENTUMNODE = "aggregators.indicatorrecommender.complex.macd.sell.weightmomemtumnode";
    public static final String AGGREGATORSINDICATORRECOMMENDERCOMPLEXMACDSELLWEIGHTMOMENTUMDELTANODE = "aggregators.indicatorrecommender.complex.macd.sell.weightmomemtumdeltanode";
    public static final String AGGREGATORSINDICATORRECOMMENDERCOMPLEXRSI = "aggregators.indicatorrecommender.complex.rsi[@enable]";
    public static final String AGGREGATORSINDICATORRECOMMENDERCOMPLEXRSIBUY = "aggregators.indicatorrecommender.complex.rsi.buy";
    public static final String AGGREGATORSINDICATORRECOMMENDERCOMPLEXRSIBUYWEIGHTRSINODE = "aggregators.indicatorrecommender.complex.rsi.buy.weightrsinode";
    public static final String AGGREGATORSINDICATORRECOMMENDERCOMPLEXRSIBUYWEIGHTRSIDELTANODE = "aggregators.indicatorrecommender.complex.rsi.buy.weightrsideltanode";
    public static final String AGGREGATORSINDICATORRECOMMENDERCOMPLEXRSISELL = "aggregators.indicatorrecommender.complex.rsi.sell";
    public static final String AGGREGATORSINDICATORRECOMMENDERCOMPLEXRSISELLWEIGHTRSINODE = "aggregators.indicatorrecommender.complex.rsi.sell.weightrsinode";
    public static final String AGGREGATORSINDICATORRECOMMENDERCOMPLEXRSISELLWEIGHTRSIDELTANODE = "aggregators.indicatorrecommender.complex.rsi.sell.weightrsideltanode";
    public static final String AGGREGATORSINDICATOR = "aggregators.indicator[@enable]";
    public static final String AGGREGATORSINDICATORMLCONFIG = "aggregators.indicator.mlconfig";
    public static final String AGGREGATORSINDICATORMACD = "aggregators.indicator.macd[@enable]";
    public static final String AGGREGATORSINDICATORRSI = "aggregators.indicator.rsi[@enable]";
    public static final String AGGREGATORSINDICATOREXTRAS = "aggregators.indicator.extras";
    public static final String AGGREGATORSINDICATOREXTRASDELTAS = "aggregators.indicator.extrasdeltas";
    public static final String AGGREGATORSINDICATOREXTRASMACD = "aggregators.indicator.extrasmacd[@enable]";
    public static final String AGGREGATORSINDICATOREXTRASRSI = "aggregators.indicator.extrasrsi[@enable]";
    public static final String AGGREGATORSINDICATORFUTUREDAYS = "aggregators.indicator.futuredays";
    public static final String AGGREGATORSINDICATORINTERVALDAYS = "aggregators.indicator.intervaldays";
    public static final String AGGREGATORSINDICATORTHRESHOLD = "aggregators.indicator.threshold";
    public static final String AGGREGATORSMLMACD = "aggregators.mlmacd[@enable]";
    public static final String AGGREGATORSMLMACDMLCONFIG = "aggregators.mlmacd.mlconfig";
}
