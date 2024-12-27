package roart.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import static org.mockito.Mockito.*;

import roart.common.pipeline.data.PipelineData;
import roart.common.pipeline.data.SerialScoreChromosome;
import roart.common.util.JsonUtil;
import roart.db.dao.IclijDbDao;
import roart.iclij.config.IclijConfig;
import roart.iclij.evolution.marketfilter.chromosome.impl.MarketFilterChromosome2;
import roart.evolution.marketfilter.genetics.gene.impl.MarketFilterGene;
import roart.iclij.config.MarketFilter;

@ComponentScan(basePackages = "roart.controller,roart.db.dao,roart.db.spring,roart.model,roart.common.springdata.repository,roart.iclij.config,roart.common.config")
@SpringJUnitConfig
@SpringBootTest(classes = { IclijConfig.class, Config.class } )
public class EvolveTest {
    @Autowired
    IclijConfig iclijConfig;

    //@Autowired
    //public IclijDbDao dbDao;
    IclijDbDao dbDao = mock(IclijDbDao.class);
    
    public String string(String file) throws IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream(file);
        return new String(is.readAllBytes());        
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
    public void test() throws IOException {
        String s1 = string("e1x.json");
        String s2 = string("e2.json");
        PipelineData d1 = convert(s1, PipelineData.class);
        PipelineData d2 = JsonUtil.convert(s2, PipelineData.class);
        System.out.println("" + d1);
        System.out.println("" + d2);
        Evolve evolve = new Evolve(dbDao, iclijConfig);
        try {
        //evolve.method3(s2);
         evolve.method4(s1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static <T> T convert(String text, Class<T> myclass) {
        if (text != null) {
            try {
                String strippedtext = (text);
                return new ObjectMapper().readValue(strippedtext, myclass);
            } catch (Exception e) {
                e.printStackTrace();            }
        }
        return null;
    }


}
