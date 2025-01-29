package roart.controller;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import roart.common.constants.Constants;
import roart.common.constants.EurekaConstants;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roart.pipeline.common.aggregate.Aggregator;

import com.fasterxml.jackson.databind.ObjectMapper;

import roart.common.util.TimeUtil;
import roart.common.webflux.WebFluxUtil;
import roart.constants.IclijConstants;
import roart.common.util.ArraysUtil;
import roart.action.ActionThread;
import roart.action.FindProfitAction;
import roart.category.AbstractCategory;
import roart.category.util.CategoryUtil;
import roart.common.communication.factory.CommunicationFactory;
import roart.common.communication.model.Communication;
import roart.common.config.MLConstants;
import roart.common.ml.NeuralNetCommand;
import roart.common.model.ActionComponentItem;
import roart.common.model.StockItem;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.data.PipelineData;
import roart.common.pipeline.data.SerialMapTA;
import roart.common.util.JsonUtil;
import roart.common.util.MemUtil;
import roart.common.util.ServiceConnectionUtil;
import roart.common.util.TimeUtil;
import roart.db.dao.IclijDbDao;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijConfigConstants;
import roart.iclij.config.IclijXMLConfig;
import roart.iclij.config.bean.ConfigC;
import roart.iclij.service.IclijServiceParam;
import roart.iclij.service.IclijServiceResult;
import roart.indicator.util.IndicatorUtils;
import roart.model.data.StockData;
import roart.pipeline.Pipeline;
import roart.pipeline.impl.ExtraReader;
import roart.result.model.ResultItem;
import roart.stockutil.StockUtil;
import roart.testdata.TestConstants;
import roart.testdata.TestData;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import roart.pipeline.common.predictor.AbstractPredictor;
import roart.predictor.util.PredictorUtils;
import roart.aggregator.util.AggregatorUtils;
import static org.mockito.Mockito.*;
import roart.iclij.model.Parameters;

@ComponentScan(basePackages = "roart.controller,roart.db.dao,roart.db.spring,roart.model,roart.common.springdata.repository,roart.iclij.config,roart.common.config")
@SpringJUnitConfig
//@TestPropertySource("file:${user.dir}/../../../../config/test/application.properties") 
//@ComponentScan(basePackages = "roart.testdata")
//@SpringBootTest(classes = TestConfiguration.class)
@SpringBootTest(classes = { IclijConfig.class, ConfigC.class } )
public class AllTest {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    IclijConfig conf;

    @Test
    public void test() {
        IclijServiceResult result = new IclijServiceResult();
        //getContentOuterC(result);
        String json = JsonUtil.convert(result);
        IclijServiceResult resultC = JsonUtil.convertnostrip(json, IclijServiceResult.class);
        // TODO getContentM(resultC);
    }
    
    public void getContentC(IclijConfig conf, List<String> disableList, IclijServiceResult result) {
        IndicatorUtils iu = new IndicatorUtils();
        try {  
          List<String> indicators = List.of(PipelineConstants.INDICATORATR, PipelineConstants.INDICATORCCI, PipelineConstants.INDICATORMACD, PipelineConstants.INDICATORRSI, PipelineConstants.INDICATORSTOCH, PipelineConstants.INDICATORSTOCHRSI);
          List<SerialMapTA> objectMapsList = new ArrayList<>();
          List<Map<String, Double[][]>> listList = new ArrayList<>();
          conf.getConfigData().setMarket(TestConstants.MARKET);
          StockData stockData = new TestData().getStockdata(conf, new TimeUtil().convertDate2("2024.01.01"), new TimeUtil().convertDate2("2025.01.01"), TestConstants.MARKET, 26, false, Constants.INDEXVALUECOLUMN, false);
          
          // TODO check when this is modified
          Map<String, StockData> extraStockDataMap = new TestData().getExtraStockdataMap(conf);
          System.out.println("mark" + conf.getConfigData().getMarket());
          
          PipelineData[] pipelinedata = new PipelineData[0];

          ExtraReader extraReader = new ExtraReader(conf, stockData.marketdatamap, 0, stockData);
          Pipeline[] datareaders = iu.getDataReaders(conf, stockData.periodText,
                  stockData.marketdatamap, stockData, extraStockDataMap, extraReader);

          pipelinedata = iu.createDatareaderPipelineData(conf, pipelinedata, stockData, datareaders);

          // for categories and adding to pipelinedata

          List<StockItem> dayStocks = iu.getDayStocks(conf, stockData);
          
          List<AbstractCategory> categories = Arrays.asList(new CategoryUtil().getCategories(conf, dayStocks,
                  stockData.periodText, pipelinedata));
          
          // add all indicators for the category

          pipelinedata = iu.createPipelineDataCategories(pipelinedata, categories, stockData);
          result.setPipelineData(pipelinedata);
         
          int arraySize = IndicatorUtils.getCommonArraySizeAndObjectMap(conf, indicators, objectMapsList, listList, pipelinedata);
          System.out.println("arraysize" + arraySize);
          assertEquals(10, arraySize);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    
    public IclijServiceResult getContentM(List<String> disableList, IclijServiceParam origparam) throws Exception {
        IclijConfig conf = new IclijConfig(origparam.getConfigData());
        NeuralNetCommand neuralnetcommand = origparam.getNeuralnetcommand();
        log.info("mydate {}", conf.getConfigData().getDate());
        log.info("mydate {}", conf.getDays());
        //createOtherTables();
        

        String json = JsonUtil.convert(origparam);
        IclijServiceParam myparam = JsonUtil.convertnostrip(json, IclijServiceParam.class);
        IclijServiceResult result = getContentOuterC(origparam);        
        json = JsonUtil.convert(result);
        result = JsonUtil.convertnostrip(json, IclijServiceResult.class);
        //IclijServiceParam origparam = (IclijServiceParam) myparam;
        //IclijServiceResult result = getContentM(new ArrayList<>(), origparam );
        
        List<ResultItem> retlist = result.getList();
        PipelineData[] pipelineData = result.getPipelineData();
        
        StockData stockData = new StockUtil().getStockData(conf, pipelineData);

        try {
            String mydate = TimeUtil.format(conf.getConfigData().getDate());
            int dateIndex = TimeUtil.getIndexEqualBefore(stockData.stockdates, mydate);
            if (dateIndex >= 0) {
                mydate = stockData.stockdates.get(dateIndex);
            }
            
            // TODO split
            
            AbstractPredictor[] predictors = new PredictorUtils().getPredictors(conf, pipelineData,
                    stockData.catName, stockData.cat, neuralnetcommand);
            //new ServiceUtil().createPredictors(categories);
            new PredictorUtils().calculatePredictors(predictors);
            
            Aggregator[] aggregates = new AggregatorUtils().getAggregates(conf, pipelineData,
                    disableList, stockData.idNameMap, stockData.catName, stockData.cat, neuralnetcommand, stockData.stockdates);

            /*
            for (AbstractCategory category : categories) {
                List<AbstractPredictor> predictors = category.getPredictors();
                addOtherTables(predictors);
            }
            */
            
            // TODO rows
            
            /*
                for (int i = 0; i < Constants.ALLPERIODS; i++) {
                    Map map = categories[i].getIndicatorLocalResultMap();
                    maps.put(categories[i].getTitle(), map);
                    log.debug("ca {}", categories[i].getTitle());
                }
             */
            for (int i = 0; i < predictors.length; i++) {
                if (predictors[i] == null) {
                    continue;
                }
                Map map = predictors[i].putData().getMap();
                log.debug("ca {}", predictors[i].getName());
                PipelineData singlePipelinedata = predictors[i].putData();
                pipelineData = ArrayUtils.add(pipelineData, singlePipelinedata);
            }
            for (int i = 0; i < aggregates.length; i++) {
                if (aggregates[i] == null) {
                    continue;
                }
                if (!aggregates[i].isEnabled()) {
                    continue;
                }
                log.debug("ag {}", aggregates[i].getName());
                Map map = aggregates[i].putData().getMap();
                PipelineData singlePipelinedata = aggregates[i].putData();
                pipelineData = ArrayUtils.add(pipelineData, singlePipelinedata);
            }
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        //new CleanETL().fixmap((Map) maps);
        //printmap(maps, 0);
        result.setList(retlist);
        result.setPipelineData(pipelineData);
        result.setConfigData(conf.getConfigData());
        
        return result;
    }

    @Test
    public void test2() {
        WebFluxUtil webFluxUtil = spy(new WebFluxUtil());
        //do(sendMMe(IclijServiceResult.class, any(), EurekaConstants.GETCONTENT)).when(webFluxUtil).sendMMe(IclijServiceResult.class, any(), EurekaConstants.GETCONTENT);
        WebFluxUtil webFluxUtil2 = new MyWebFluxUtil();

        Parameters parameters = new Parameters();
        parameters.setThreshold(1.0);
        parameters.setFuturedays(10);
        ActionThread ac = new ActionThread(conf, null);
        ActionComponentItem aci = new ActionComponentItem();
        aci.setMarket(TestConstants.MARKET);
        aci.setAction(IclijConstants.FINDPROFIT);
        aci.setComponent(PipelineConstants.MLRSI);
        aci.setSubcomponent(MLConstants.TENSORFLOW + " " + MLConstants.GRU);
        aci.setParameters(JsonUtil.convert(parameters));
        aci.setBuy(null);
        aci.setPriority(40);
        aci.setRecord(LocalDate.now());
        try {
        ac.runAction(conf, aci, new ArrayList<>(), webFluxUtil2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Object sendMMe(Class<IclijServiceResult> class1, Object any, String getcontent) {
        // TODO Auto-generated method stub
        return null;
    }

    public class MyWebFluxUtil extends WebFluxUtil {
        public <T> T sendMMe(Class<T> clazz, Object param, String path) {
            log.info("Calling {}", path);
            if (EurekaConstants.GETCONTENT.equals(path)) {
                String json = JsonUtil.convert(param);
                T myparam = JsonUtil.convertnostrip(json, clazz);
                IclijServiceParam origparam = (IclijServiceParam) myparam;
                IclijServiceResult result = getContentOuterM(origparam );
                json = JsonUtil.convert(result);
                myparam = JsonUtil.convertnostrip(json, clazz);
                //json = JsonUtil.convert(param);
                //T myparam = JsonUtil.convertnostrip(json, clazz);
                return myparam;
            }
            return null;
        }
        
        public <T> T sendCMe(Class<T> clazz, Object param, String path) {
            log.info("Calling {}", path);
            return null;
        }
        
        public <T> T sendMeInner(Class<T> myclass, Object param, String url, ObjectMapper objectMapper) {
            log.info("Calling {}", url);
            IclijServiceResult result = new IclijServiceResult();
            result.setConfigData(conf.getConfigData());
            return (T) result;           
        }

    }
    public IclijServiceResult getContentOuterC(IclijServiceParam param)
            throws Exception {
        IclijServiceResult result = new IclijServiceResult();
        Map<String, Map<String, Object>> maps = null;
        if (param.isWantMaps()) {
            maps = new HashMap<>();
        }
        try {
            long[] mem0 = MemUtil.mem();
            log.info("MEM {}", MemUtil.print(mem0));
            List<String> disableList = param.getConfList();
            if (disableList == null) {
                disableList = new ArrayList<>();
            }
            getContentC( new IclijConfig(param.getConfigData()), disableList, result);
            long[] mem1 = MemUtil.mem();
            long[] memdiff = MemUtil.diff(mem1, mem0);
            log.info("MEM {} Δ {}", MemUtil.print(mem1), MemUtil.print(memdiff));
            if (maps != null) {
                //log.info("Length {}", JsonUtil.convert(maps).length());
            }
            //System.out.println(VM.current().details());
            //System.out.println(GraphLayout.parseInstance(maps).toFootprint());
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            result.setError(e.getMessage());
        }
        return result;
    }
    
    public IclijServiceResult getContentOuterM(IclijServiceParam param) {
        IclijServiceResult result = new IclijServiceResult();
        Map<String, Map<String, Object>> maps = null;
        if (param.isWantMaps()) {
            maps = new HashMap<>();
        }
        try {
            long[] mem0 = MemUtil.mem();
            log.info("MEM {}", MemUtil.print(mem0));
            List<String> disableList = param.getConfList();
            if (disableList == null) {
                disableList = new ArrayList<>();
            }
            result = getContentM( disableList, param);
            if (!param.isWantMaps()) {
                result.setMaps(null);
            }
            long[] mem1 = MemUtil.mem();
            long[] memdiff = MemUtil.diff(mem1, mem0);
            log.info("MEM {} Δ {}", MemUtil.print(mem1), MemUtil.print(memdiff));
            if (maps != null) {
                //log.info("Length {}", JsonUtil.convert(maps).length());
            }
            //System.out.println(VM.current().details());
            //System.out.println(GraphLayout.parseInstance(maps).toFootprint());
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            result.setError(e.getMessage());
        }
        return result;
    }

}
