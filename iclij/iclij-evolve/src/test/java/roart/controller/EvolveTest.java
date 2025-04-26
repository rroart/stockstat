package roart.controller;

import org.junit.jupiter.api.BeforeAll;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import static org.mockito.Mockito.*;

import roart.common.constants.Constants;
import roart.common.pipeline.data.PipelineData;
import roart.common.pipeline.data.SerialScoreChromosome;
import roart.common.util.JsonUtil;
import roart.db.dao.IclijDbDao;
import roart.iclij.config.IclijConfig;
import roart.iclij.evolution.marketfilter.chromosome.impl.MarketFilterChromosome2;
import roart.model.io.IO;
import roart.evolution.marketfilter.genetics.gene.impl.MarketFilterGene;
import roart.evolve.Evolve;
import roart.filesystem.FileSystemDao;
import roart.iclij.config.MarketFilter;
import roart.common.webflux.WebFluxUtil;
import org.apache.curator.framework.CuratorFramework;

@TestInstance(Lifecycle.PER_CLASS)
@ComponentScan(basePackages = "roart.controller,roart.db.dao,roart.db.spring,roart.model,roart.common.springdata.repository,roart.iclij.config,roart.common.config")
@SpringJUnitConfig
@SpringBootTest(classes = { IclijConfig.class, Config.class } )
public class EvolveTest {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    IclijConfig iclijConfig;

    //@Autowired
    //public IclijDbDao dbDao;
    IclijDbDao dbDao = mock(IclijDbDao.class);
    
    FileSystemDao fileSystemDao = null;

    IO io = null;

    public String string(String file) throws IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream(file);
        return new String(is.readAllBytes());        
    }
    
    @BeforeAll
    public void before() throws Exception {
        WebFluxUtil webFluxUtil = mock(WebFluxUtil.class);

        fileSystemDao = mock(FileSystemDao.class);
        doReturn("dummy.txt").when(fileSystemDao).writeFile(any(), any(), any(), any());

        CuratorFramework curatorClient = mock(CuratorFramework.class);
        
        io = new IO(dbDao, null, webFluxUtil, fileSystemDao, null, null, curatorClient);
    }

    @Test
    public void test0() throws IOException {
        MarketFilterGene gene = new MarketFilterGene(new MarketFilter(List.of("Price")), List.of("Price"));
        SerialScoreChromosome pair = new SerialScoreChromosome(3.0, new MarketFilterChromosome2(List.of("cf"), gene));
        String str = JsonUtil.convert(pair);
        System.out.println(str);
        SerialScoreChromosome pair2 = convert(str, SerialScoreChromosome.class);
        String str2 = JsonUtil.convert(pair2);
        System.out.println(str);
        System.out.println(str2);
        System.out.println(pair.getLeft().getClass().getCanonicalName());
        System.out.println(pair2.getLeft().getClass().getCanonicalName());
        System.out.println(pair.getRight().getClass().getCanonicalName());
        System.out.println(pair2.getRight().getClass().getCanonicalName());
    }
    
    @Test
    public void testEvolve() throws IOException {
        String evolve1 = string("evolve1.json");
        PipelineData d1 = JsonUtil.convertnostrip(evolve1, PipelineData.class);
        System.out.println("" + d1);
        Evolve evolve = spy(new Evolve(iclijConfig, io));
        doNothing().when(evolve).print(anyString());
        try {
            evolve.handleEvolve(evolve1);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
    }
    
    @Test
    public void testImproveProfit() throws IOException {
        String evolve1 = string("improveprofit2.json");
        PipelineData d1 = JsonUtil.convertnostrip(evolve1, PipelineData.class);
        System.out.println("" + d1);
        Evolve evolve = spy(new Evolve(iclijConfig, io));
        doNothing().when(evolve).print(anyString());
        try {
            evolve.handleProfit(evolve1);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
    }
    
    @Test
    public void testFilter() throws IOException {
        String s2 = string("filter1.json");
        PipelineData d2 = JsonUtil.convertnostrip(s2, PipelineData.class);
        System.out.println("" + d2);
        Evolve evolve = spy(new Evolve(iclijConfig, io));
        doNothing().when(evolve).print(anyString());
        try {
            evolve.handleFilter(s2);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
    }
    
    @Test
    public void testAboveBelow() throws IOException {
        String s1 = string("abovebelow1.json");
        PipelineData d1 = JsonUtil.convertnostrip(s1, PipelineData.class);
        System.out.println("" + d1);
        Evolve evolve = spy(new Evolve(iclijConfig, io));
        doNothing().when(evolve).print(anyString());
        try {
            evolve.handleAboveBelow(s1);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
    }
    
    public <T> T convert(String text, Class<T> myclass) {
        if (text != null) {
            try {
                String strippedtext = (text);
                return new ObjectMapper().readValue(strippedtext, myclass);
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
        }
        return null;
    }


}
