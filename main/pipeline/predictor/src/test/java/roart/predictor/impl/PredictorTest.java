package roart.predictor.impl;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import roart.common.config.ConfigMaps;
import roart.common.constants.Constants;
import roart.common.ml.NeuralNetCommand;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.data.PipelineData;
import roart.common.util.ArraysUtil;
import roart.iclij.config.IclijConfig;
import roart.ml.model.LearnClassify;
import roart.model.data.StockData;
import roart.pipeline.impl.DataReader;
import roart.testdata.TestConfiguration;
import roart.testdata.TestConstants;
import roart.testdata.TestUtils;
import roart.testdata.TestData;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import static org.junit.jupiter.api.Assertions.*;

@ComponentScan(basePackages = "roart.controller,roart.db.dao,roart.db.spring,roart.model,roart.common.springdata.repository,roart.iclij.config,roart.common.config")
@SpringJUnitConfig
//@TestPropertySource("file:${user.dir}/../../../../config/test/application.properties") 
//@ComponentScan(basePackages = "roart.testdata")
//@SpringBootTest(classes = TestConfiguration.class)
@SpringBootTest(classes = { IclijConfig.class, Config.class } )
public class PredictorTest {

    @Autowired
    IclijConfig conf;

    @Test
    public void test() throws Exception {
        System.setProperty("config", "stockstat.xml");
        System.out.println("conf" + conf);
        Map<String, Double[][]> aListMap = new TestData().getListMap();
        Map<String, double[][]> aTruncListMap = ArraysUtil.getTruncListArr(aListMap);;
        PipelineData[] datareaders = new PipelineData[1];
        NeuralNetCommand neuralnetcommand = new NeuralNetCommand();
        neuralnetcommand.setMldynamic(true);
        neuralnetcommand.setMlclassify(true);
        neuralnetcommand.setMllearn(true);
        //neuralnetcommand.set
        try {
            System.out.println(conf.getConfigData().getConfigValueMap().keySet());
            StockData stockdata = new TestData().getStockdata(conf);
            System.out.println("mark" + conf.getConfigData().getMarket());
            datareaders[0] = new DataReader(conf, stockdata.marketdatamap, Constants.INDEXVALUECOLUMN, TestConstants.MARKET).putData();

            Predictor predictor = new PredictorTensorflowMLP(conf, Constants.INDEX + " MLP", Constants.INDEX, Constants.INDEXVALUECOLUMN, datareaders, neuralnetcommand);
            int days = predictor.getDays(aListMap, aTruncListMap);
            List<LearnClassify> map = predictor.getMap(aListMap, aTruncListMap, days);
            List<LearnClassify> classifylist = predictor.getClassifyList(conf, aListMap, aTruncListMap);
            System.out.println(map.get(0).getArray().getClass().getCanonicalName());
            System.out.println("map" + map.get(0).getClassification() + " " +  Arrays.asList(((Double[])map.get(0).getArray())));
            System.out.println(classifylist.get(0).getArray().getClass().getCanonicalName());
            //System.out.println("map" + map.get(0).getClassification() + " " +  Arrays.asList(((Double[][])map.get(0).getArray())[0]));
            System.out.println("list" + classifylist.get(0).getClassification()+ " " +  Arrays.asList(((Double[])classifylist.get(0).getArray())));
            
            predictor.calculate();
            PipelineData pipelinedata = predictor.putData();
            System.out.println(pipelinedata.keySet());
            for (String key : pipelinedata.keySet()) {
                System.out.println(key + " " + pipelinedata.get(key));
                //System.out.println(resultmap.keySet());
            }
            assertNotNull(pipelinedata);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
