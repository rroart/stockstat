package roart.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
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
import roart.iclij.config.SimulateFilter;
import roart.simulate.model.SimulateStock;
import roart.iclij.config.IclijConfig;

@ComponentScan(basePackages = "roart.controller,roart.db.dao,roart.db.spring,roart.model,roart.common.springdata.repository,roart.iclij.config,roart.common.config")
@SpringJUnitConfig
@SpringBootTest(classes = { IclijConfig.class, Config.class } )
public class SimTest {
    @Autowired
    IclijConfig iclijConfig;

    //@Autowired
    //public IclijDbDao dbDao;
    IclijDbDao dbDao = mock(IclijDbDao.class);

    public SimTest() {
    }

    @Test
    public void t() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectMapper mapper2 = new ObjectMapper();
        mapper2.registerModule(new JavaTimeModule());
        SimulateStock s = new SimulateStock();
        s.setBuydate(LocalDate.now());
        String str = null;

        try {
            str = mapper.writeValueAsString(s);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("str " + str);
        String str2 = null;

        try {
            str2 = mapper2.writeValueAsString(s);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("str2 " + str2);
        Object res = null;

        try {
            res = mapper.readValue(str, SimulateStock.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Object res2 = null;

        try {
            res2 = mapper2.readValue(str2, SimulateStock.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("res " + String.valueOf(res));
        System.out.println("res2 " + String.valueOf(res2));
        res = null;
        res2 = null;

        try {
            res = mapper.readValue(str2, SimulateStock.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            res2 = mapper2.readValue(str, SimulateStock.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("res " + String.valueOf(res));
        System.out.println("res2 " + String.valueOf(res2));
        String f = "[{\"lucky\":10}]";
        String f2 = "[{'lucky':10}]";
        SimulateFilter[] listoverrides2 = (SimulateFilter[])JsonUtil.convert(f, SimulateFilter[].class);
        System.out.println(listoverrides2);
        SimulateFilter[] listoverrides3 = (SimulateFilter[])JsonUtil.convert(f2, SimulateFilter[].class);
        System.out.println(listoverrides3);
        int jj = 0;
    }
    public String string(String file) throws IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream(file);
        return new String(is.readAllBytes());        
    }
    @Test
    public void test() throws IOException {
        String s1 = string("s1.json");
        String s2 = string("s2.json");
        PipelineData d1 = JsonUtil.convert(s1, PipelineData.class);
        PipelineData d2 = JsonUtil.convert(s2, PipelineData.class);
        System.out.println("" + d1);
        Sim sim = new Sim(iclijConfig, dbDao);
        try {
        sim.method(s1, "sim", true);
        sim.method(s2, "simauto", true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
