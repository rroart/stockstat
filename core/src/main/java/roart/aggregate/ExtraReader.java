package roart.aggregate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.util.Pair;

import com.fasterxml.jackson.databind.ObjectMapper;

import roart.config.MyMyConfig;
import roart.model.StockItem;
import roart.pipeline.Pipeline;
import roart.pipeline.PipelineConstants;
import roart.util.Constants;
import roart.util.MarketData;

public class ExtraReader extends Pipeline {

    Map<Pair, List<StockItem>> retListMap;
    Map<Pair, Map<Date, StockItem>> retMapMap;
    
    public ExtraReader(MyMyConfig conf, int category) throws Exception {
        super(conf, category);
        readData(conf, null, category);        
        
    }
    private void readData(MyMyConfig conf, Map<String, MarketData> marketdatamap, int category) throws Exception {
        retListMap = new HashMap<>();
        retMapMap = new HashMap<>();
        String str0 = conf.getAggregatorsIndicatorExtras();
        //String str = "{ [ 'market' : 'cboevol', 'id' : 'VIX'], [ 'market' : 'tradcomm' , 'id' : 'CL1:COM' ], [ 'market' : 'tradcomm', 'id' : 'XAUUSD:CUR' ] ]   }";
        String str = "[ { \"market\" : \"cboevol\", \"id\" : \"VIX\"}, { \"market\" : \"tradcomm\" , \"id\" : \"CL1:COM\" }, { \"market\" : \"tradcomm\", \"id\" : \"XAUUSD:CUR\" } ]";
        ObjectMapper mapper = new ObjectMapper();
        List<LinkedHashMap> list = (List<LinkedHashMap>) mapper.readValue(str, List.class);
        List<Pair> pairs = new ArrayList<>();
        for (LinkedHashMap mi : list) {
            System.out.println("mi"+mi+" " + mi.toString());
            pairs.add(new Pair(mi.get("market"), mi.get("id")));
            //pairs.add(new Pair(mi.getMarket(), mi.getId()));
        }
        
        SimpleDateFormat dt = new SimpleDateFormat(Constants.MYDATEFORMAT);
        String dateme = dt.format(conf.getdate());
        for (Pair pair : pairs) {
            List<StockItem> stocks = null;
        try {
            stocks = StockItem.getAll((String) pair.getFirst(), conf);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        log.info("stocks " + stocks.size());
        if (stocks == null) {
            continue;
        }
        List<StockItem> mystocks = new ArrayList<>();
        Map<Date, StockItem> mymap = new HashMap<>();
        for (StockItem stock : stocks) {
            String id = (String) pair.getSecond();
            if (stock.getId().equals(id)) {
                mystocks.add(stock);
                mymap.put(stock.getDate(), stock);
            }
        }
        stocks = null;
        retListMap.put(pair, mystocks);
        retMapMap.put(pair, mymap);
        }
    }
    @Override
    public Map<String, Object> getLocalResultMap() {
        Map<String, Object> map = new HashMap<>();
        map.put(PipelineConstants.LIST, retListMap);
        map.put(PipelineConstants.MAP, retMapMap);
        return map;
    }
    @Override
    public Map<Integer, Map<String, Object>> getResultMap() {
        // TODO Auto-generated method stub
        return null;
    }
    
    private class MarketAndId {
        String market;
        String id;
        public String getMarket() {
            return market;
        }
        public void setMarket(String market) {
            this.market = market;
        }
        public String getId() {
            return id;
        }
        public void setId(String id) {
            this.id = id;
        }
        
    }
}
