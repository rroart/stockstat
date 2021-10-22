package roart.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijXMLConfig;
import roart.iclij.config.Market;
import roart.iclij.config.MarketConfig;
import roart.iclij.config.MarketFilter;

public class IclijXMLConfigTest {

@BeforeEach
public void setup() {
    //IclijXMLConfig.instance();
}

@Test
public void test() throws JsonParseException, JsonMappingException, IOException {
    MarketConfig m = new MarketConfig();
    m.setFindtime((short) 5);
    m.setMarket("cb");
    List<MarketConfig> l = new ArrayList<>();
    l.add(m);
    ObjectMapper mapper = new ObjectMapper();
    
    System.out.println(mapper.writeValueAsString(l));
    //IclijConfig conf = IclijXMLConfig.getConfigInstance();
    //List<Market> ret = IclijXMLConfig.getMarkets(conf);
    //List<MarketFilter> ret2 = IclijXMLConfig.getFilterMarkets(conf);
}
}


