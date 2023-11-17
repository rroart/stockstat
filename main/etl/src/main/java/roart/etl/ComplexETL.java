package roart.etl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Objects;

import org.apache.commons.lang3.math.NumberUtils;

import roart.common.config.MarketStock;
import roart.common.config.MarketStockExpression;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.data.TwoDimD;
import roart.common.util.MetaUtil;
import roart.common.util.PipelineUtils;
import roart.model.data.StockData;
import roart.pipeline.Pipeline;

public class ComplexETL {

    public void method(MarketStockExpression mse, Set<String> commonDates, Map<String, StockData> stockDataMap,
            Map<String, Pipeline[]> dataReaderMap, Map<String, List<Double>> newMap) {
        List<MarketStock> marketStockList = mse.getItems();
        String expression = mse.getExpression();
        List<String> dates = new ArrayList<>(commonDates);
        Collections.sort(dates);
        List<Double> newValues = new ArrayList<>();
        for (String date : dates) {
            List<Double> values = new ArrayList<>();
            for (MarketStock ms : marketStockList) {
                String market = ms.getMarket();
                String id = ms.getId();
                String catName = ms.getCategory();
                StockData stockData = stockDataMap.get(market);
                if (catName == null) {
                    catName = stockData.catName;
                }
                int cat = stockData.cat;
                Pipeline[] datareaders = dataReaderMap.get(market);
                Map<String, Pipeline> pipelineMap = getPipelineMap(datareaders);
                Pipeline datareader = pipelineMap.get("" + cat); // used id 0-9
                // interpolation does not work yet
                List<String> datelist = (List<String>) datareader.putData().get(PipelineConstants.DATELIST);
                Map<String, Double[][]> listMap = PipelineUtils.convertTwoDimD((Map<String, TwoDimD>) datareader.putData().get(PipelineConstants.LIST));
                Map<String, Double[][]> fillListMap = PipelineUtils.convertTwoDimD((Map<String, TwoDimD>) datareader.putData().get(PipelineConstants.FILLLIST));
                Double[][] fillList = fillListMap.get(id);
                try {
                    int dateIndex = datelist.size() - datelist.indexOf(date);
                    if (fillList == null) {
                        values.add(null);
                        continue;
                    }
                    dateIndex = fillList[0].length - dateIndex;
                    //dateIndex = datelist.indexOf(date);
                    Double value = fillList[0][dateIndex];
                    values.add(value);
                } catch (Exception e) {
                    int jj = 0;
                }
            }
            boolean nonNulls = values.stream().allMatch(Objects::nonNull);
            if (!nonNulls) {
                newValues.add(null);
                continue;
            }
            String[] parts = expression.split(" ");
            for (int i = 0; i < parts.length; i++) {
                if (NumberUtils.isCreatable(parts[i])) {
                    parts[i] = "" + values.get(Integer.parseInt(parts[i]) - 1);
                }
            }
            double newValue = new PostfixCalculator().calculator(parts);
            newValues.add(newValue);
        }
        String newId = "";
        for (MarketStock ms : marketStockList) {
            String market = ms.getMarket();
            String id = ms.getId();
            String cat = ms.getCategory();
            newId = newId + market + "." + id + "." + cat + " ";
        }
        newId = newId + expression;
        newMap.put(newId, newValues);
    }
    
    // dup
    /**
     * Used id 0-9
     * 
     * @param datareaders
     * @return 
     */
    public static Map<String, Pipeline> getPipelineMap(Pipeline[] datareaders) {
        Map<String, Pipeline> pipelineMap = new HashMap<>();
        for (Pipeline datareader : datareaders) {
            pipelineMap.put(datareader.pipelineName(), datareader);
        }
        return pipelineMap;
    }

    class PostfixCalculator {

        public double calculator(String[] strArr) {
            Stack<Double> operands = new Stack<Double>();

            for(String str : strArr) {

                switch (str) {
                    case "+":
                    case "-":
                    case "*":
                    case "/":
                        double right = operands.pop();
                        double left = operands.pop();
                        double value = 0;
                        switch(str) {
                            case "+":
                                value = left + right;
                                break;
                            case "-":
                                value = left - right;
                                break;
                            case "*":
                                value = left * right;
                                break;
                            case "/":
                                value = left / right;
                                break;
                            default:
                                break;
                        }
                        operands.push(value);
                        break;
                    default:
                        operands.push(Double.parseDouble(str));
                        break;  
                }
            }
            return operands.pop();
        }
    }

}
