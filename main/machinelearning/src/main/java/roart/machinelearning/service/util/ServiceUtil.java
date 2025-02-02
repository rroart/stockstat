package roart.machinelearning.service.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.constants.Constants;
import roart.common.ml.NeuralNetCommand;
import roart.common.model.StockItem;
import roart.common.pipeline.data.PipelineData;
import roart.iclij.config.IclijConfig;
import roart.pipeline.common.predictor.AbstractPredictor;
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

}
