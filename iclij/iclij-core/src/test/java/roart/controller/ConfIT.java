package roart.controller;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;

import tools.jackson.core.exc.StreamReadException;
import tools.jackson.databind.DatabindException;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;

import roart.iclij.config.IclijConfig;
import roart.common.config.MyMyConfig;
import roart.common.config.MyXMLConfig;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijXMLConfig;
import roart.iclij.config.SimulateFilter;

@ComponentScan(basePackages = "roart.controller,roart.db.dao,roart.db.spring,roart.model,roart.common.springdata.repository,roart.iclij.config,roart.common.config")
@SpringBootTest
public class ConfIT {

    @Autowired
    public IclijConfig iclijConfig;
    
    @Test
    public void test() {
        System.out.println("test");
    }
    
    //@Test
    public void t() throws StreamReadException, DatabindException, IOException {
        System.setProperty("config", "stockstat.xml");
        //System.setProperty("config", "iclij.xml");
        extracted();
        }

    //@Test
    public void t2() throws StreamReadException, DatabindException, IOException {
        //System.setProperty("config", "stockstat.xml");
        System.setProperty("config", "iclij.xml");
        extracted();
        }

    private void extracted() {
        //new IclijXMLConfig();
        new MyXMLConfig();
        MyMyConfig instance2 = null; //new MyMyConfig();
        //String node = instance2.getEvolveSaveLocation();
        IclijConfig instance = iclijConfig; //IclijXMLConfig.getConfigInstance();
        //List<SimulateFilter[]> list = IclijXMLConfig.getSimulate(instance);
        System.out.println("" + instance.getConfigData().getConfigValueMap());
        System.out.println("" + instance2.getConfigData().getConfigValueMap());
        System.out.println(instance.getConfigData().getConfigValueMap().equals(instance2.getConfigData().getConfigValueMap()));
        Set<String> keys = new HashSet<>(instance.getConfigData().getConfigValueMap().keySet());
        keys.addAll(instance2.getConfigData().getConfigValueMap().keySet());
        keys.removeAll(instance.getConfigData().getConfigValueMap().keySet());
        System.out.println("dfkeys" + keys);
        MapDifference<String, Object> diff = Maps.difference(instance.getConfigData().getConfigValueMap(), instance2.getConfigData().getConfigValueMap());
        System.out.println("diff" + diff);
    }
    
}
