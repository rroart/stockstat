package roart.controller;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import roart.common.constants.Constants;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.curator.framework.CuratorFramework;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roart.pipeline.common.aggregate.Aggregator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

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
import roart.common.config.ConfigConstants;
import roart.common.config.ConfigMaps;
import roart.common.config.MLConstants;
import roart.common.ml.NeuralNetCommand;
import roart.common.model.ActionComponentItem;
import roart.common.model.MetaItem;
import roart.common.model.StockItem;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.data.PipelineData;
import roart.common.pipeline.data.SerialListPlain;
import roart.common.pipeline.data.SerialMapTA;
import roart.common.util.JsonUtil;
import roart.common.util.ServiceConnectionUtil;
import roart.common.util.TimeUtil;
import roart.db.common.DbAccess;
import roart.db.dao.IclijDbDao;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijConfigConstants;
import roart.iclij.config.IclijXMLConfig;
import roart.iclij.config.bean.ConfigC;
import roart.iclij.config.bean.ConfigI;
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
import roart.util.ServiceUtil;
import roart.sim.Sim;
import roart.model.io.IO;

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
import roart.iclij.model.component.ComponentInput;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.Collection;
import java.util.Collections;
import roart.filesystem.FileSystemDao;
import roart.iclij.config.SimulateInvestConfig;
import roart.iclij.config.AutoSimulateInvestConfig;
import roart.util.ServiceUtil;
import roart.common.inmemory.factory.InmemoryFactory;

@TestInstance(Lifecycle.PER_CLASS)
@ComponentScan(basePackages = "roart.controller,roart.db.dao,roart.db.spring,roart.model,roart.common.springdata.repository,roart.iclij.config,roart.common.config")
@SpringJUnitConfig
//@TestPropertySource("file:${user.dir}/../../../../config/test/application.properties") 
//@ComponentScan(basePackages = "roart.testdata")
//@SpringBootTest(classes = TestConfiguration.class)
@SpringBootTest(classes = { IclijConfig.class, IclijDbDao.class, ConfigI.class, ConfigDb.class } )
public class AllTest {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    IclijConfig iconf = null;
    
    IclijDbDao iclijDbDao = mock(IclijDbDao.class);
    
    // no autowiring
    IclijConfig conf = null;
   
    TestDataSource dataSource;
    
    private static final ObjectMapper mapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();

    IclijDbDao dbDao = mock(IclijDbDao.class);

    WebFluxUtil webFluxUtil;
    
    FileSystemDao fileSystemDao;
    
    Parameters parameters;
    
    ActionThread ac;
    
    IO io;

    private InmemoryFactory inmemoryFactory = new TestInmemoryFactory();

    private CommunicationFactory communicationFactory = new TestCommunicationFactory();
    
    @Test
    public void test() {
        IclijServiceResult result = new IclijServiceResult();
        //getContentOuterC(result);
        String json = JsonUtil.convert(result);
        IclijServiceResult resultC = JsonUtil.convertnostrip(json, IclijServiceResult.class);
        // TODO getContentM(resultC);
    }
    
    @Deprecated
    public void getContentC(IclijConfig conf, List<String> disableList, IclijServiceResult result) {
        System.out.println("conff" + conf.getConfigData().getConfigMaps().keys.size());
        String str = conf.getAggregatorsIndicatorExtras();
        System.out.println("strstrr " + str );
        System.out.println("strstrr2 " + conf.getAbnormalChange() );
        IndicatorUtils iu = new IndicatorUtils();
        try {
            /*
          List<String> indicators = List.of(PipelineConstants.INDICATORATR, PipelineConstants.INDICATORCCI, PipelineConstants.INDICATORMACD, PipelineConstants.INDICATORRSI, PipelineConstants.INDICATORSTOCH, PipelineConstants.INDICATORSTOCHRSI);
          List<SerialMapTA> objectMapsList = new ArrayList<>();
          List<Map<String, Double[][]>> listList = new ArrayList<>();
          conf.getConfigData().setMarket(TestConstants.MARKET);
          */
          String market = conf.getConfigData().getMarket();
          StockData stockData = new TestData().getStockdata(conf, new TimeUtil().convertDate2("2024.01.01"), new TimeUtil().convertDate2("2025.01.01"), market, 26, false, Constants.INDEXVALUECOLUMN, false);
          
          // TODO check when this is modified
          System.out.println("mark" + market);
          Map<String, StockData> extraStockDataMap = new TestData().getExtraStockdataMap(conf);
          
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
         
          /*
          int arraySize = IndicatorUtils.getCommonArraySizeAndObjectMap(conf, indicators, objectMapsList, listList, pipelinedata);
          System.out.println("arraysize" + arraySize);
          assertEquals(4, arraySize); /// TODO check 4 or 10, alternates
          */
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    
    @Deprecated
    public IclijServiceResult getContentM(List<String> disableList, IclijServiceParam origparam) throws Exception {
        IclijConfig conf = new IclijConfig(origparam.getConfigData());
        NeuralNetCommand neuralnetcommand = origparam.getNeuralnetcommand();
        log.info("mydate {}", conf.getConfigData().getDate());
        log.info("mydate {}", conf.getDays());
        //createOtherTables();
        

        String json = JsonUtil.convert(origparam, mapper);
        IclijServiceParam myparam = JsonUtil.convertnostrip(json, IclijServiceParam.class, mapper);
        IclijServiceResult result = null; // getContentOuterC(origparam);        
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
    
    @BeforeAll
    public void before() throws Exception {
        ConfigMaps configMaps = IclijConfig.instanceC();
        conf = new IclijConfig(configMaps, "config2", null);
        conf.getConfigData().getConfigValueMap().put(ConfigConstants.MACHINELEARNINGRANDOM, Boolean.TRUE);
        String market = TestConstants.MARKET;
        dataSource = new TestDataSource(conf, new TimeUtil().convertDate2("2024.01.01"), new TimeUtil().convertDate2("2025.01.01"), market, 26, false, Constants.INDEXVALUECOLUMN, false);
        webFluxUtil = new TestWebFluxUtil(conf, dataSource);
        parameters = new Parameters();
        parameters.setThreshold(1.0);
        parameters.setFuturedays(10);

        fileSystemDao = mock(FileSystemDao.class);
        doReturn("dummy.txt").when(fileSystemDao).writeFile(any(), any(), any(), any());

        CuratorFramework curatorClient = mock(CuratorFramework.class);
        
        io = new IO(iclijDbDao, null, dataSource, webFluxUtil, fileSystemDao, inmemoryFactory, communicationFactory, curatorClient);
        ((TestWebFluxUtil)webFluxUtil).setIo(io);
        ((TestCommunicationFactory)communicationFactory).setIo(io);
        ((TestCommunicationFactory)communicationFactory).setConfig(iconf);
        
        ac = new ActionThread(iconf, io);
        
        //String content = "";
        //new Sim(iconf, dbDao, fileSystemDao).method((String) content, "sim", true);

    }

    @Test
    public void test2() throws Exception {
        ActionComponentItem aci = new ActionComponentItem(TestConstants.MARKET, IclijConstants.FINDPROFIT, PipelineConstants.MLRSI, MLConstants.TENSORFLOW + " " + MLConstants.GRU, 0, JsonUtil.convert(parameters));
        //aci.setBuy(null);
        //aci.setRecord(LocalDate.now());
        try {
            ac.runAction(iconf, aci, new ArrayList<>());
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
    }

    @Test
    public void testEvolve() throws Exception {
        ActionComponentItem aci = new ActionComponentItem(TestConstants.MARKET, IclijConstants.EVOLVE, PipelineConstants.MLRSI, MLConstants.TENSORFLOW + " " + MLConstants.GRU, 0, JsonUtil.convert(parameters));
        try {
            ac.runAction(iconf, aci, new ArrayList<>());
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
    }

    @Test
    public void testEvolveARI() throws Exception {
        ActionComponentItem aci = new ActionComponentItem(TestConstants.MARKET, IclijConstants.EVOLVE, PipelineConstants.AGGREGATORRECOMMENDERINDICATOR, null, 0, JsonUtil.convert(parameters));
        try {
            ac.runAction(iconf, aci, new ArrayList<>());
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
    }

    @Test
    public void testImproveProfit() throws Exception {
        ActionComponentItem aci = new ActionComponentItem(TestConstants.MARKET, IclijConstants.IMPROVEPROFIT, PipelineConstants.MLRSI, MLConstants.TENSORFLOW + " " + MLConstants.GRU, 0, JsonUtil.convert(parameters));
        try {
            ac.runAction(iconf, aci, new ArrayList<>());
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
    }

    @Test
    public void testFilter() throws Exception {
        ActionComponentItem aci = new ActionComponentItem(TestConstants.MARKET, IclijConstants.IMPROVEFILTER, PipelineConstants.MLRSI, MLConstants.TENSORFLOW + " " + MLConstants.GRU, 0, JsonUtil.convert(parameters));
        try {
            ac.runAction(iconf, aci, new ArrayList<>());
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
    }

    @Test
    public void testAboveBelow() throws Exception {
        ActionComponentItem aci = new ActionComponentItem(TestConstants.MARKET, IclijConstants.IMPROVEABOVEBELOW, PipelineConstants.MLRSI, MLConstants.TENSORFLOW + " " + MLConstants.GRU, 0, JsonUtil.convert(parameters));
        try {
            ac.runAction(iconf, aci, new ArrayList<>());
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
    }

    @Test
    public void testSim() throws Exception {
        SimulateInvestConfig simConfig = getSimConfigDefault();
        String market = TestConstants.MARKET;
        simConfig.setStartdate("2024-11-01");
        simConfig.setEnddate("2024-12-01");
        IclijServiceResult result = null;
        try {
            result = getSimulateInvestMarket(simConfig, market);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        System.out.println("map" + result.getWebdatajson().getUpdateMap());
        System.out.println("queue" + ActionThread.queue.size() + " " + ActionThread.queued.size());
    }

    @Test
    public void testAutoSim() throws Exception {
        AutoSimulateInvestConfig simConfig = getAutoSimConfigDefault();
        String market = TestConstants.MARKET;
        simConfig.setStartdate("2024-11-01");
        simConfig.setEnddate("2024-12-01");
        IclijServiceResult result = null;
        try {
            result = getAutoSimulateInvestMarket(market, simConfig);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        System.out.println("map" + result.getWebdatajson().getUpdateMap());
        System.out.println("queue" + ActionThread.queue.size() + " " + ActionThread.queued.size());
    }

    @Test
    public void testImproveSim() throws Exception {
        SimulateInvestConfig simConfig = getImproveSimConfigDefault();
        String market = TestConstants.MARKET;
        simConfig.setStartdate("2024-11-01");
        simConfig.setEnddate("2024-12-01");
        IclijServiceResult result = null;
        try {
            result = getImproveSimulateInvest(market, simConfig);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        //System.out.println("map" + result.getWebdatajson().getUpdateMap());
        //System.out.println("queue" + ActionThread.queue.size() + " " + ActionThread.queued.size());
    }

    @Test
    public void testImproveAutoSim() throws Exception {
        AutoSimulateInvestConfig simConfig = getImproveAutoSimConfigDefault();
        String market = TestConstants.MARKET;
        simConfig.setStartdate("2024-11-01");
        simConfig.setEnddate("2024-12-01");
        IclijServiceResult result = null;
        try {
            result = getImproveAutoSimulateInvest(market, simConfig);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        //System.out.println("map" + result.getWebdatajson().getUpdateMap());
        //System.out.println("queue" + ActionThread.queue.size() + " " + ActionThread.queued.size());
    }

    private IclijServiceResult getSimulateInvestMarket(SimulateInvestConfig simConfig, String market) {
        Map<String, Object> map = simConfig.asMap();
        IclijConfig myConfig = iconf.copy();
        myConfig.getConfigData().getConfigValueMap().putAll(map);
        return ServiceUtil.getSimulateInvest(new ComponentInput(myConfig.getConfigData(), null, market, null, null, false, false, new ArrayList<>(), new HashMap<>()), myConfig, io);
    }

    public IclijServiceResult getImproveSimulateInvest(String market, SimulateInvestConfig simConfig)
            throws Exception {
        //MainAction.goals.add(new ImproveProfitAction());
        //int result = new ImproveProfitAction().goal(param.getIclijConfig(), );
        if ("null".equals(market) || "None".equals(market)) {
            market = null;
        }
        IclijConfig myConfig = iconf.copy();
        Map<String, Object> map = simConfig.asValuedMap();
        myConfig.getConfigData().getConfigValueMap().put(IclijConfigConstants.SIMULATEINVESTSTARTDATE, simConfig.getStartdate());
        myConfig.getConfigData().getConfigValueMap().put(IclijConfigConstants.SIMULATEINVESTENDDATE, simConfig.getEnddate());
        map.remove(IclijConfigConstants.SIMULATEINVESTSTARTDATE);
        map.remove(IclijConfigConstants.SIMULATEINVESTENDDATE);
        if (simConfig.getGa() != null) {
            int ga = simConfig.getGa();
            simConfig.setGa(null);
            myConfig.getConfigData().getConfigValueMap().put(IclijConfigConstants.EVOLVEGA, ga);
        }
        /*
        config.getConfigValueMap().putAll(map);
        if (simConfig.getAdviser() != null) {
            int adviser = simConfig.getAdviser();
            simConfig.setAdviser(null);
            config.getConfigValueMap().put(IclijConfigConstants.SIMULATEINVESTADVISER, adviser);
        }
        if (simConfig.getIndicatorPure() != null) {
            boolean adviser = simConfig.getIndicatorPure();
            simConfig.setIndicatorPure(null);
            config.getConfigValueMap().put(IclijConfigConstants.SIMULATEINVESTINDICATORPURE, adviser);
        }
         */
        return ServiceUtil.getImproveSimulateInvest(new ComponentInput(myConfig.getConfigData(), null, market, null, null, false, false, new ArrayList<>(), map), myConfig, io);
    }
    
     public IclijServiceResult getAutoSimulateInvestMarket(String market, AutoSimulateInvestConfig simConfig)
            throws Exception {
        //MainAction.goals.add(new ImproveProfitAction());
        //int result = new ImproveProfitAction().goal(param.getIclijConfig(), );
        Map<String, Object> map = simConfig.asMap();
        IclijConfig myConfig = iconf.copy();
        myConfig.getConfigData().getConfigValueMap().putAll(map);
        return ServiceUtil.getAutoSimulateInvest(myConfig, new ComponentInput(myConfig.getConfigData(), null, market, null, null, false, false, new ArrayList<>(), new HashMap<>()), io);
    }

    public IclijServiceResult getImproveAutoSimulateInvest(String market, AutoSimulateInvestConfig simConfig)
            throws Exception {
        //MainAction.goals.add(new ImproveProfitAction());
        //int result = new ImproveProfitAction().goal(param.getIclijConfig(), );
        if ("null".equals(market) || "None".equals(market)) {
            market = null;
        }
        IclijConfig myConfig = iconf.copy();
        Map<String, Object> map = simConfig.asValuedMap();
        //myConfig.getConfigData().unmute();
        myConfig.getConfigData().getConfigValueMap().put(IclijConfigConstants.AUTOSIMULATEINVESTSTARTDATE, simConfig.getStartdate());
        myConfig.getConfigData().getConfigValueMap().put(IclijConfigConstants.AUTOSIMULATEINVESTENDDATE, simConfig.getEnddate());
        map.remove(IclijConfigConstants.AUTOSIMULATEINVESTSTARTDATE);
        map.remove(IclijConfigConstants.AUTOSIMULATEINVESTENDDATE);
        if (simConfig.getGa() != null) {
            int ga = simConfig.getGa();
            simConfig.setGa(null);
            myConfig.getConfigData().getConfigValueMap().put(IclijConfigConstants.EVOLVEGA, ga);
        }
        //myConfig.getConfigData().mute();
        /*
        config.getConfigValueMap().putAll(map);
        if (simConfig.getAdviser() != null) {
            int adviser = simConfig.getAdviser();
            simConfig.setAdviser(null);
            config.getConfigValueMap().put(IclijConfigConstants.SIMULATEINVESTADVISER, adviser);
        }
        if (simConfig.getIndicatorPure() != null) {
            boolean adviser = simConfig.getIndicatorPure();
            simConfig.setIndicatorPure(null);
            config.getConfigValueMap().put(IclijConfigConstants.SIMULATEINVESTINDICATORPURE, adviser);
        }
         */
        return ServiceUtil.getImproveAutoSimulateInvest(new ComponentInput(myConfig.getConfigData(), null, market, null, null, false, false, new ArrayList<>(), map), myConfig, io);
    }

   private SimulateInvestConfig getSimConfigDefault() {
        SimulateInvestConfig simConfig = new SimulateInvestConfig();
        simConfig.setConfidence(false);
        simConfig.setConfidenceValue(0.7);
        simConfig.setConfidenceFindTimes(4);
        simConfig.setStoploss(true);
        simConfig.setStoplossValue(0.9);
        simConfig.setIndicatorPure(false);
        simConfig.setIndicatorRebase(false);
        simConfig.setIndicatorReverse(false);
        simConfig.setMldate(false);
        simConfig.setStocks(5);
        simConfig.setBuyweight(false);
        simConfig.setInterval(7);
        simConfig.setAdviser(0);
        simConfig.setPeriod(0);
        simConfig.setInterpolate(false);
        simConfig.setIntervalStoploss(true);
        simConfig.setIntervalStoplossValue(0.9);
        simConfig.setDay(1);
        simConfig.setDelay(1);
        simConfig.setIntervalwhole(false);
        simConfig.setConfidenceholdincrease(false);
        simConfig.setNoconfidenceholdincrease(true);
        simConfig.setNoconfidencetrenddecrease(false);
        simConfig.setNoconfidencetrenddecreaseTimes(1);
        simConfig.setConfidencetrendincrease(false);
        simConfig.setConfidencetrendincreaseTimes(1);
        simConfig.setIndicatorDirection(false);
        simConfig.setIndicatorDirectionUp(true);
        simConfig.setVolumelimits(null);
        simConfig.setAbovebelow(false);
        return simConfig;
    }

    private SimulateInvestConfig getImproveSimConfigDefault() {
        SimulateInvestConfig simConfig = new SimulateInvestConfig();
        simConfig.setGa(0);
        simConfig.setIndicatorPure(null);
        simConfig.setIndicatorReverse(null);
        simConfig.setStocks(null);
        simConfig.setBuyweight(false);
        simConfig.setInterval(null);
        simConfig.setAdviser(null);
        simConfig.setPeriod(0);
        simConfig.setDelay(1);
        simConfig.setIntervalwhole(true);
        simConfig.setVolumelimits(null);
        simConfig.setFuturecount(0);
        simConfig.setFuturetime(0);
        simConfig.setImproveFilters(false);
        return simConfig;
    }

    private AutoSimulateInvestConfig getAutoSimConfigDefault() {
        AutoSimulateInvestConfig simConfig = new AutoSimulateInvestConfig();
        simConfig.setInterval(1);
        simConfig.setPeriod(0);
        simConfig.setLastcount(5);
        simConfig.setDellimit(0.5);
        simConfig.setScorelimit(1.0);
        simConfig.setAutoscorelimit(0.0);
        simConfig.setIntervalwhole(false);        
        simConfig.setFilters(null);
        simConfig.setVolumelimits(null);
        simConfig.setVote(false);
        simConfig.setKeepAdviser(false);
        simConfig.setKeepAdviserLimit(0.0);
        return simConfig;
    }

    private AutoSimulateInvestConfig getImproveAutoSimConfigDefault() {
        AutoSimulateInvestConfig simConfig = new AutoSimulateInvestConfig();
        // TODO simConfig.setStocks(5);
        simConfig.setIntervalwhole(false);
        
        simConfig.setGa(0);
        simConfig.setFuturecount(0);
        simConfig.setFuturetime(0);
        simConfig.setImproveFilters(false);
        simConfig.setFilters(null);
        simConfig.setVolumelimits(null);
        simConfig.setVote(false);
        return simConfig;
    }

    @Deprecated
    private Object sendMMe(Class<IclijServiceResult> class1, Object any, String getcontent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Deprecated
    /// TODO too big
    public void getDates(IclijConfig conf, IclijServiceResult result) throws Exception {
        PipelineData[] pipelineData = new PipelineData[0];
        Map<String, Object> aMap = new HashMap<>();
        /*
        aMap.put(ConfigConstants.MACHINELEARNING, false);
        aMap.put(ConfigConstants.AGGREGATORS, false);
        aMap.put(ConfigConstants.INDICATORS, false);
        aMap.put(ConfigConstants.MISCTHRESHOLD, null);
        */        
        aMap.put(ConfigConstants.MISCMYTABLEDAYS, 0);
        aMap.put(ConfigConstants.MISCMYDAYS, 0);
        /*
        aMap.put(ConfigConstants.MISCPERCENTIZEPRICEINDEX, true);
        aMap.put(ConfigConstants.MISCINTERPOLATIONMETHOD, market.getConfig().getInterpolate());
        aMap.put(ConfigConstants.MISCINTERPOLATIONLASTNULL, Boolean.TRUE);
        aMap.put(ConfigConstants.MISCMERGECY, false);        
        conf.setConfigValueMap(new HashMap<>(conf.getConfigValueMap()));
        */
        conf.getConfigData().getConfigValueMap().putAll(aMap);
        StockData stockData = new TestData().getStockdata(conf, new TimeUtil().convertDate2("2024.01.01"), new TimeUtil().convertDate2("2025.01.01"), TestConstants.MARKET, 26, false, Constants.INDEXVALUECOLUMN, false);
        //StockData stockData = new Extract(dbDao).getStockData(conf);
        if (stockData != null) {
            PipelineData map = new PipelineData();
            map.setName(PipelineConstants.DATELIST);
            map.put(PipelineConstants.DATELIST, new SerialListPlain(stockData.stockdates));
            pipelineData = ArrayUtils.add(pipelineData, map);
            result.setPipelineData(pipelineData);
            return;
        }
        
        // TODO not used anymore?
        List<String> dates = null;
        try {
            if ("0".equals(conf.getConfigData().getMarket())) {
                int jj = 0;
            }
            // TODO dates = dbDao.getDates(conf.getConfigData().getMarket(), conf);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        if (dates == null) {
            return;
        }
        log.info("stocks {}", dates.size());
        Set<String> markets = new HashSet<>();
        markets.add(conf.getConfigData().getMarket());

        try {
            Collections.sort(dates);
            PipelineData map = new PipelineData();
            map.setName(PipelineConstants.DATELIST);
            map.put(PipelineConstants.DATELIST, new SerialListPlain(dates));
            pipelineData = ArrayUtils.add(pipelineData, map);
            result.setPipelineData(pipelineData);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return;
        }
    }
    
    @Deprecated
    public List<MetaItem> getMetas() {
        try {
            return new TestData().getMetas();
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return new ArrayList<>();
    }

}
