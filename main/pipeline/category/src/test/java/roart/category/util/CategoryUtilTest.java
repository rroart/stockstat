package roart.category.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import roart.category.AbstractCategory;
import roart.common.config.ConfigConstants;
import roart.common.model.StockItem;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.data.PipelineData;
import roart.common.pipeline.data.SerialMapTA;
import roart.iclij.config.IclijConfig;
import roart.indicator.util.IndicatorUtils;
import roart.model.data.StockData;
import roart.pipeline.Pipeline;
import roart.pipeline.impl.ExtraReader;
import roart.testdata.TestConstants;
import roart.testdata.TestData;

@ComponentScan(basePackages = "roart.controller,roart.db.dao,roart.db.spring,roart.model,roart.common.springdata.repository,roart.iclij.config,roart.common.config")
@SpringJUnitConfig
//@TestPropertySource("file:${user.dir}/../../../../config/test/application.properties") 
//@ComponentScan(basePackages = "roart.testdata")
//@SpringBootTest(classes = TestConfiguration.class)
@SpringBootTest(classes = { IclijConfig.class, Config.class } )
public class CategoryUtilTest {

    @Autowired
    IclijConfig conf;

    @Test
    public void test() throws Exception {
        System.out.println("" + conf.getConfigData().getConfigValueMap().get(ConfigConstants.MISCFILTERDATE));
        System.out.println("" + conf.getConfigData().getConfigMaps().deflt.get(ConfigConstants.MISCFILTERDATE));
        System.out.println("" + conf.getConfigData().getConfigMaps().text.get(ConfigConstants.MISCFILTERDATE));
        System.out.println("" + conf.getConfigData().getConfigMaps().map.get(ConfigConstants.MISCFILTERDATE));
        IndicatorUtils iu = new IndicatorUtils();
        try {  
          List<String> indicators = List.of(PipelineConstants.INDICATORATR, PipelineConstants.INDICATORCCI, PipelineConstants.INDICATORMACD, PipelineConstants.INDICATORRSI, PipelineConstants.INDICATORSTOCH, PipelineConstants.INDICATORSTOCHRSI);
          List<SerialMapTA> objectMapsList = new ArrayList<>();
          List<Map<String, Double[][]>> listList = new ArrayList<>();
          conf.getConfigData().setMarket(TestConstants.MARKET);
          StockData stockData = new TestData().getStockdata(conf);
          
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
          
          int arraySize = IndicatorUtils.getCommonArraySizeAndObjectMap(conf, indicators, objectMapsList, listList, pipelinedata);
          System.out.println("arraysize" + arraySize);
        } catch (Exception e) {
            e.printStackTrace();
        }

        
    }
}
