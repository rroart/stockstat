package roart.controller;

import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import static org.mockito.Mockito.*;

import roart.common.pipeline.data.PipelineData;
import roart.common.util.JsonUtil;
import roart.db.dao.IclijDbDao;
import roart.iclij.config.IclijConfig;

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
    public void test() throws IOException {
        String s1 = string("e1.json");
        String s2 = string("e2.json");
        PipelineData d1 = JsonUtil.convert(s1, PipelineData.class);
        PipelineData d2 = JsonUtil.convert(s2, PipelineData.class);
        System.out.println("" + d1);
        Evolve evolve = new Evolve(dbDao, iclijConfig);
        try {
        evolve.method4(s1);
        evolve.method3(s2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
