package roart.config;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class IclijConfig {

    public IclijConfig(IclijConfig config) {
        this.configTreeMap = config.configTreeMap;
        this.configValueMap = config.configValueMap;
        this.deflt = config.deflt;
        this.text = config.text;
        this.type = config.type;
    }

    public IclijConfig() {        
    }
    
    public ConfigTreeMap configTreeMap;

    public Map<String, Object> configValueMap;
    public Map<String, String> text = new HashMap();
    public Map<String, Object> deflt = new HashMap();
    public Map<String, Class> type = new HashMap();

    public List<Market> getMarkets() throws JsonParseException, JsonMappingException, IOException {
        //List<Object> list = IclijXMLConfig.getConfigXML().getList("markets.market");
        //return new HashSet<>(list);
        String markets = IclijXMLConfig.getConfigXML().getString("markets.marketlist");
        ObjectMapper mapper = new ObjectMapper();
        List<Market> map = mapper.readValue(markets, new TypeReference<List<Market>>(){});
        return map;
    }
    
    public List<TradeMarket> getTradeMarkets() throws JsonParseException, JsonMappingException, IOException {
        /*
        List<Object> list = IclijXMLConfig.getConfigXML().getList("trademarkets.market");
        return new HashSet<>(list);
         */
        String markets = IclijXMLConfig.getConfigXML().getString("trademarkets");
        ObjectMapper mapper = new ObjectMapper();
        List<TradeMarket> map = mapper.readValue(markets, new TypeReference<List<TradeMarket>>(){});
        return map;

     }
    
     public int getDays() {
        return (Integer) getValueOrDefault(ConfigConstants.MISCMYDAYS);
    }

    private Object getValueOrDefault(String key) {
        Object retVal = configValueMap.get(key);
        return Optional.ofNullable(retVal).orElse(deflt.get(key));
    }
}
