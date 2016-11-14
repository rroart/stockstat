package roart.util;

import java.util.List;
import java.util.Map;

import roart.database.Stock;

public class MarketData {
    public List<Stock> stocks;
    //public Map<String, List<Stock>> stockidmap;
    //public Map<String, List<Stock>> stockdatemap;
    public String[] periodtext;
    //public List<Stock>[][] stocklistperiod;
    public List<Stock>[] datedstocklists;
}
