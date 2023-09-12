package roart.service.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.ImmutablePair;

import roart.category.AbstractCategory;
import roart.category.impl.CategoryIndex;
import roart.category.impl.CategoryPeriod;
import roart.category.impl.CategoryPrice;
import roart.iclij.config.IclijConfig;
import roart.common.constants.Constants;
import roart.common.ml.NeuralNetCommand;
import roart.common.model.MetaItem;
import roart.common.model.StockItem;
import roart.db.dao.DbDao;
import roart.db.dao.util.DbDaoUtil;
import roart.iclij.config.IclijConfig;
import roart.model.data.MarketData;
import roart.model.data.StockData;
import roart.pipeline.Pipeline;
import roart.pipeline.common.predictor.AbstractPredictor;
import roart.pipeline.impl.DataReader;
import roart.pipeline.impl.ExtraReader;
import roart.predictor.impl.PredictorPytorchGRU;
import roart.predictor.impl.PredictorPytorchLSTM;
import roart.predictor.impl.PredictorPytorchMLP;
import roart.predictor.impl.PredictorPytorchRNN;
import roart.predictor.impl.PredictorTensorflowGRU;
import roart.predictor.impl.PredictorTensorflowLIR;
import roart.predictor.impl.PredictorTensorflowLSTM;
import roart.predictor.impl.PredictorTensorflowMLP;
import roart.predictor.impl.PredictorTensorflowRNN;
import roart.result.model.ResultItemTable;
import roart.result.model.ResultItemTableRow;
import roart.stockutil.MetaUtil;
import roart.stockutil.StockUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceUtil {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    public Set<String> getMarkets(Set<Pair<String, String>> ids) {
        Set<String> markets = new HashSet<>();
        for (Pair<String, String> idpair : ids) {
            markets.add((String) idpair.getLeft());
        }
        return markets;
    }

    //protected static int[] otherTableNames = { Constants.EVENT, Constants.MLTIMES }; 
    
    public static ResultItemTable createMLTimesTable(Map<Integer, ResultItemTable> tableMap) {
        ResultItemTable mlTimesTable = new ResultItemTable();
        ResultItemTableRow headrow = new ResultItemTableRow();
        headrow.add("Period");
        headrow.add("Engine");
        headrow.add("Model name");
        headrow.add("Millis");
        mlTimesTable.add(headrow);
        tableMap.put(Constants.MLTIMES, mlTimesTable);
        return mlTimesTable;
    }

    public static ResultItemTable createEventTable(Map<Integer, ResultItemTable> tableMap) {
        ResultItemTable eventTable = new ResultItemTable();
        ResultItemTableRow headrow = new ResultItemTableRow();
        headrow.add("Period");
        headrow.add("Event");
        headrow.add("Name");
        headrow.add("Id");
        eventTable.add(headrow);
        tableMap.put(Constants.EVENT, eventTable);
        return eventTable;
    }

    public Pipeline[] getDataReaders(IclijConfig conf, String[] periodText,
            Map<String, MarketData> marketdatamap, StockData stockData, DbDao dbDao) throws Exception {
        Pipeline[] datareaders = new Pipeline[Constants.PERIODS + 3];
        datareaders[0] = new DataReader(conf, marketdatamap, Constants.INDEXVALUECOLUMN, conf.getConfigData().getMarket());
        datareaders[1] = new DataReader(conf, marketdatamap, Constants.PRICECOLUMN, conf.getConfigData().getMarket());
        datareaders[2] = new ExtraReader(conf, marketdatamap, 0, stockData, dbDao);
        for (int i = 0; i < Constants.PERIODS; i++) {
            datareaders[i + 3] = new DataReader(conf, marketdatamap, i, conf.getConfigData().getMarket());
        }
        return datareaders;
    }

    public AbstractCategory[] getCategories(IclijConfig conf, List<StockItem> stocks,
            String[] periodText,
            Pipeline[] datareaders) throws Exception {
        AbstractCategory[] categories = new AbstractCategory[Constants.PERIODS + 2];
        categories[0] = new CategoryIndex(conf, Constants.INDEX, stocks, datareaders);
        categories[1] = new CategoryPrice(conf, Constants.PRICE, stocks, datareaders);
        for (int i = 0; i < Constants.PERIODS; i++) {
            categories[i + 2] = new CategoryPeriod(conf, i, periodText[i], stocks, datareaders);
        }
        return categories;
    }

    public AbstractPredictor[] getPredictors(IclijConfig conf, String[] periodText,
            Map<String, MarketData> marketdatamap,
            List<StockItem>[] datedstocklists,
            Pipeline[] datareaders, AbstractCategory[] categories, NeuralNetCommand neuralnetcommand) throws Exception {
        AbstractPredictor[] predictors = new AbstractPredictor[Constants.ALLPERIODS];
        //predictors[0] = new PredictorLSTM(conf, Constants.INDEX, stocks, marketdatamap, periodDataMap, datareaders, categories);
        //predictors[1] = new PredictorLSTM(conf, Constants.PRICE, stocks, marketdatamap, periodDataMap, datareaders, categories);
        MarketData marketdata = marketdatamap.get(conf.getConfigData().getMarket());
        for (int i = 0; i < Constants.ALLPERIODS; i++) {
            //AbstractPredictor predictor = new PredictorGRU(conf, categories[i].getTitle() + " LSTM", marketdatamap, periodDataMap, categories[i].getTitle(), categories[i].getPeriod(), categories, datareaders);
            List<AbstractPredictor> allpredictors = new ArrayList<>();
            allpredictors.add(new PredictorTensorflowLIR(conf, categories[i].getTitle() + " LIR", marketdatamap, categories[i].getTitle(), categories[i].getPeriod(), categories, datareaders, neuralnetcommand));
            allpredictors.add(new PredictorTensorflowMLP(conf, categories[i].getTitle() + " MLP", marketdatamap, categories[i].getTitle(), categories[i].getPeriod(), categories, datareaders, neuralnetcommand));
            allpredictors.add(new PredictorTensorflowRNN(conf, categories[i].getTitle() + " RNN", marketdatamap, categories[i].getTitle(), categories[i].getPeriod(), categories, datareaders, neuralnetcommand));
            allpredictors.add(new PredictorTensorflowLSTM(conf, categories[i].getTitle() + " LSTM", marketdatamap, categories[i].getTitle(), categories[i].getPeriod(), categories, datareaders, neuralnetcommand));
            allpredictors.add(new PredictorTensorflowGRU(conf, categories[i].getTitle() + " GRU", marketdatamap, categories[i].getTitle(), categories[i].getPeriod(), categories, datareaders, neuralnetcommand));
            allpredictors.add(new PredictorPytorchMLP(conf, categories[i].getTitle() + " MLP", marketdatamap, categories[i].getTitle(), categories[i].getPeriod(), categories, datareaders, neuralnetcommand));
            allpredictors.add(new PredictorPytorchRNN(conf, categories[i].getTitle() + " RNN", marketdatamap, categories[i].getTitle(), categories[i].getPeriod(), categories, datareaders, neuralnetcommand));
            allpredictors.add(new PredictorPytorchLSTM(conf, categories[i].getTitle() + " LSTM", marketdatamap, categories[i].getTitle(), categories[i].getPeriod(), categories, datareaders, neuralnetcommand));
            allpredictors.add(new PredictorPytorchGRU(conf, categories[i].getTitle() + " GRU", marketdatamap, categories[i].getTitle(), categories[i].getPeriod(), categories, datareaders, neuralnetcommand));
            for (AbstractPredictor predictor : allpredictors) {
                if (predictor.isEnabled()) {
                    if (MetaUtil.normalPeriod(marketdata, categories[i].getPeriod(), categories[i].getTitle())) {
                        if (predictor.hasValue()) {
                            predictors[i] = predictor;
                        }
                    }
                }
            }
        }
        return predictors;
    }

    public void calculatePredictors(AbstractPredictor[] predictors) throws Exception {
        for (AbstractPredictor predictor : predictors) {
            if (predictor != null) {
                predictor.calculate();
            }
        }
    }
}
