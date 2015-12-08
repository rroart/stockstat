package roart.service;

import roart.model.ResultItem;
import roart.model.Stock;

import javax.servlet.http.*;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.TreeMap;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.io.*;

import roart.util.Constants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ControlService {
    private static Logger log = LoggerFactory.getLogger(ControlService.class);

    public enum Config { REINDEXLIMIT, INDEXLIMIT, FAILEDLIMIT, OTHERTIMEOUT, TIKATIMEOUT, MLTCOUNT, MLTMINTF, MLTMINDF };
    public static Map<Config, Integer> configMap = new HashMap<Config, Integer>();
    public static Map<Config, String> configStrMap = new HashMap<Config, String>();
    
    private static volatile Integer writelock = new Integer(-1);

    private static int dirsizelimit = 100;

    private static volatile int mycounter = 0;
    
    public static int getMyCounter() {
        return mycounter++;
    }
    
    // called from ui
    public void overlapping() {
    }

    @SuppressWarnings("rawtypes")
	public List<List> overlappingDo() {
	List<ResultItem> retList = new ArrayList<ResultItem>();
	ResultItem ri = new ResultItem();
	ri.add("Percent");
	ri.add("Count");
	ri.add("Directory 1");
	ri.add("Directory 2");
	retList.add(ri);

	try {
        List<Stock> stock = Stock.getAll();
    } catch (Exception e) {
        log.error(Constants.EXCEPTION, e);
        }
	
	List<List> retlistlist = new ArrayList<List>();
	retlistlist.add(retList);
	return retlistlist;
    }

    public void dbindex(String md5) throws Exception {
    }

    public void dbsearch(String md5) throws Exception {
    }

    private static Date mydate = null; //new Date();
    
    public void setdate(Date date) {
        this.mydate = date;
    }

    public static List getContent() {
        //mydate.setHours(0);
        //mydate.setMinutes(0);
        //mydate.setSeconds(0);
        List<ResultItem> retList = new ArrayList<ResultItem>();
        ResultItem ri = new ResultItem();
        //ri.add("Id");
	String delta = "Delta";
	delta = "Δ";
        ri.add("Name");
        ri.add("Date");
        ri.add("Period1");
        ri.add(delta + "1");
        ri.add("Period2");
        ri.add(delta + "2");
        ri.add("Period3");
        ri.add(delta + "3");
        ri.add("Period4");
        ri.add(delta + "4");
       ri.add("Period5");
       ri.add(delta + "5");
        ri.add("Price");
        ri.add("Currency");
        retList.add(ri);
        try {
        List<Stock> stocks = Stock.getAll(mymarket);
        HashMap<String, List<Stock>> stockmap = split(stocks);
        for (String key : stockmap.keySet()) {
            List<Stock> stocklist = stockmap.get(key);
            stocklist.sort(StockDateComparator);
        }
        // the main list, based on freshest or specific date.
        List<Stock> datedstocks = new ArrayList<Stock>();
        for (String key : stockmap.keySet()) {
            List<Stock> stocklist = stockmap.get(key);
            int i = getStockDate(stocklist, mydate);
            if (i >= 0) {
            Stock stock = stocklist.get(i);
            datedstocks.add(stock);
            }
        }

	boolean hasPeriod1 = hasStockPeriod1(datedstocks);
	boolean hasPeriod2 = hasStockPeriod2(datedstocks);
	boolean hasPeriod3 = hasStockPeriod3(datedstocks);
	boolean hasPeriod4 = hasStockPeriod4(datedstocks);
	boolean hasPeriod5 = hasStockPeriod5(datedstocks);

        // make sorted period1, sorted current day
        List<Stock> stocklistPeriod1Day0 = new ArrayList<Stock>(datedstocks);
	if (hasPeriod1) {
        stocklistPeriod1Day0.sort(StockPeriod1Comparator);
	}
        List<Stock> stocklistPeriod2Day0 = new ArrayList<Stock>(datedstocks);
	if (hasPeriod2) {
        stocklistPeriod2Day0.sort(StockPeriod2Comparator);
	}
        List<Stock> stocklistPeriod3Day0 = new ArrayList<Stock>(datedstocks);
	if (hasPeriod3) {
        stocklistPeriod3Day0.sort(StockPeriod3Comparator);
	}
        List<Stock> stocklistPeriod4Day0 = new ArrayList<Stock>(datedstocks);
	if (hasPeriod4) {
        stocklistPeriod4Day0.sort(StockPeriod4Comparator);
	}
        //printstock(stocklistPeriod4Day0, 10);
        List<Stock> stocklistPeriod5Day0 = new ArrayList<Stock>(datedstocks);
	if (hasPeriod5) {
        stocklistPeriod5Day0.sort(StockPeriod5Comparator);
	}
        // make sorted period1, sorted day offset
        List<Stock> datedstocksoffset = getOffsetList(stockmap, mydays);
        List<Stock> stocklistPeriod1Day1 = new ArrayList<Stock>(datedstocksoffset);
	if (hasPeriod1) {
        stocklistPeriod1Day1.sort(StockPeriod1Comparator);
	}
        List<Stock> stocklistPeriod2Day1 = new ArrayList<Stock>(datedstocksoffset);
	if (hasPeriod2) {
        stocklistPeriod2Day1.sort(StockPeriod2Comparator);
	}
       List<Stock> stocklistPeriod3Day1 = new ArrayList<Stock>(datedstocksoffset);
	if (hasPeriod3) {
        stocklistPeriod3Day1.sort(StockPeriod3Comparator);
	}
       List<Stock> stocklistPeriod4Day1 = new ArrayList<Stock>(datedstocksoffset);
	if (hasPeriod4) {
        stocklistPeriod4Day1.sort(StockPeriod4Comparator);
	}
        //printstock(stocklistPeriod4Day1, 10);
       List<Stock> stocklistPeriod5Day1 = new ArrayList<Stock>(datedstocksoffset);
	if (hasPeriod5) {
        stocklistPeriod5Day1.sort(StockPeriod5Comparator);
	}
       
        HashMap<String, Integer> period1map = new HashMap<String, Integer>();
	if (hasPeriod1) {
	    period1map = getPeriodmap(stocklistPeriod1Day0, stocklistPeriod1Day1);
	}
HashMap<String, Integer> period2map = new HashMap<String, Integer>();
	if (hasPeriod2) {
	    period2map = getPeriodmap(stocklistPeriod2Day0, stocklistPeriod2Day1);
	}
HashMap<String, Integer> period3map = new HashMap<String, Integer>();
	if (hasPeriod3) {
 period3map = getPeriodmap(stocklistPeriod3Day0, stocklistPeriod3Day1);
	}
 HashMap<String, Integer> period4map = new HashMap<String, Integer>();
	if (hasPeriod4) {
	    period4map = getPeriodmap(stocklistPeriod4Day0, stocklistPeriod4Day1);
	}
HashMap<String, Integer> period5map = new HashMap<String, Integer>();
	if (hasPeriod5) {
	    period5map = getPeriodmap(stocklistPeriod5Day0, stocklistPeriod5Day1);
	}
        log.info("sizes " + stocks.size() + " " + datedstocks.size() + " " + datedstocksoffset.size());
        for (Stock stock : datedstocks) {
            //System.out.println("" + mydate.getTime() + "|" + stock.getDate().getTime());
            if (mymarket == null) {
                continue;
            }
            if (false &&  mydate != null && mydate.getTime() != stock.getDate().getTime()) {
                continue;
            }
            ResultItem r = new ResultItem();
            //r.add(stock.getId());
            r.add(stock.getName());
            SimpleDateFormat dt = new SimpleDateFormat("yyyy.MM.dd");
            r.add(dt.format(stock.getDate()));
            r.add(stock.getPeriod1());
            r.add(period1map.get(stock.getId()));
            r.add(stock.getPeriod2());
           // r.add(null);
            r.add(period2map.get(stock.getId()));
            r.add(stock.getPeriod3());
            //r.add(null);
            r.add(period3map.get(stock.getId()));
            r.add(stock.getPeriod4());
            r.add(period4map.get(stock.getId()));
//            r.add(null);
            r.add(stock.getPeriod5());
            r.add(period5map.get(stock.getId()));
	    //            r.add(null);
            r.add(stock.getPrice());
            r.add(stock.getCurrency());
            //r.add(stock.get());
            retList.add(r);
            
        }
        log.info("retlist " +retList.size());
        } catch (Exception e) {
            log.error("Exception", e);
           e.printStackTrace();
            //log.error(Constants.EXCEPTION, e);
        }
      List<List> retlistlist = new ArrayList<List>();
        retlistlist.add(retList);
        return retlistlist;
    }

    private static void printstock(List<Stock> stocklistPeriod4Day1, int imax) {
        int i = 0;
        for (Stock s : stocklistPeriod4Day1) {
            //log.info(s.getId() + " : " + s.getName() + " : " + s.getPeriod4());
            i++;
            if (i > imax) {
                return;
            }
        }
    }

    private static int getStockDate(List<Stock> stocklist, Date mydate2) {
        if (mydate2 == null) {
            return 0;
        }
        for (int i = 0; i < stocklist.size() ; i++) {
            if (mydate2.equals(stocklist.get(i).getDate())) {
                return i;
            }
        }
        return -1;
    }

    private static HashMap<String, Integer> getPeriodmap(
            List<Stock> stocklistPeriod1Day0, List<Stock> stocklistPeriod1Day1) {
        HashMap<String, Integer> periodmap = new HashMap<String, Integer>();
        for (int i = 0; i < stocklistPeriod1Day1.size(); i++) {
            for (int j = 0; j < stocklistPeriod1Day0.size(); j++) {
                if (stocklistPeriod1Day0.get(i).getId() == null) {
                    log.error("null0 " + stocklistPeriod1Day0.get(i));
                }
                if (stocklistPeriod1Day1.get(i).getId() == null) {
                    log.error("null0 " + stocklistPeriod1Day1.get(i));
                }
                if (stocklistPeriod1Day1.get(i).getId().equals(stocklistPeriod1Day0.get(j).getId())) {
                    periodmap.put(stocklistPeriod1Day1.get(i).getId(), i-j);
                }
            }
        }
        return periodmap;
    }

    private static List<Stock> getOffsetList(
            HashMap<String, List<Stock>> stockmap, int mydays) {
        List<Stock> retstocklist = new ArrayList<Stock>();
        for (String key : stockmap.keySet()) {
            Stock stock = null;
            List<Stock> stocklist = stockmap.get(key);
            if (mydate == null) {
                if (stocklist.size() > mydays) {
                stock = stocklist.get(mydays);
                } else {
                    continue;
                }
            } else {
                int i = getStockDate(stocklist, mydate);
                if (i >= 0) {
                    if (stocklist.size() > (mydays + i)) {
                        stock = stocklist.get(mydays + i);
                    }
                } else {
		    //System.out.println(stocklist.get(0).getName() + " " + stocklist.size() + " " + mydays + " " +i);
		}
            }
            if (stock != null) {
            retstocklist.add(stock);
            }
        }
        return retstocklist;
    }

    private static HashMap<String, List<Stock>> split(List<Stock> stocks) {
        HashMap<String, List<Stock>> mymap = new HashMap<String, List<Stock>>();
        for (Stock stock : stocks) {
            List<Stock> stocklist = mymap.get(stock.getId());
            if (stocklist == null) {
                stocklist = new ArrayList<Stock>();
                mymap.put(stock.getId(), stocklist);
            }
            stocklist.add(stock);
        }
        return mymap;
    }

    static String mymarket = "0";
    
    public void setMarket(String value) {
        mymarket = value;
        }
    
    public static Comparator<Stock> StockDateComparator = new Comparator<Stock>() {

public int compare(Stock stock1, Stock stock2) {

Date comp1 = stock1.getDate();
Date comp2 = stock2.getDate();

//descending order
return comp2.compareTo(comp1);
}

};

public static Comparator<Stock> StockPeriod1Comparator = new Comparator<Stock>() {

public int compare(Stock stock1, Stock stock2) {

Double comp1 = stock1.getPeriod1();
Double comp2 = stock2.getPeriod1();

return compDoubleInner(comp1, comp2);
}

};

public static Comparator<Stock> StockPeriod2Comparator = new Comparator<Stock>() {

public int compare(Stock stock1, Stock stock2) {

Double comp1 = stock1.getPeriod2();
Double comp2 = stock2.getPeriod2();

return compDoubleInner(comp1, comp2);
}

};

public static Comparator<Stock> StockPeriod3Comparator = new Comparator<Stock>() {

public int compare(Stock stock1, Stock stock2) {

Double comp1 = stock1.getPeriod3();
Double comp2 = stock2.getPeriod3();

return compDoubleInner(comp1, comp2);
}

};

public static Comparator<Stock> StockPeriod4Comparator = new Comparator<Stock>() {

public int compare(Stock stock1, Stock stock2) {

Double comp1 = stock1.getPeriod4();
Double comp2 = stock2.getPeriod4();

return compDoubleInner(comp1, comp2);
}

};

public static Comparator<Stock> StockPeriod5Comparator = new Comparator<Stock>() {

public int compare(Stock stock1, Stock stock2) {

Double comp1 = stock1.getPeriod5();
Double comp2 = stock2.getPeriod5();

return compDoubleInner(comp1, comp2);
}

};

private static int compDoubleInner(Double comp1, Double comp2) {
    if (comp1 == null && comp2 == null) {
        return 0;
    }
    
    if (comp1 == null) {
        return 1;
    }
    
    if (comp2 == null) {
        return -1;
    }
    
    //descending order
    return comp2.compareTo(comp1);
}

static Integer mydays = 2;

public void setDays(Integer integer) {
    mydays = integer;
}

public int getDays() {
    return mydays;
}

    public static boolean hasStockPeriod1(List<Stock> stocks) {
	for (Stock s : stocks) {
	    if (s.getPeriod1() != null) {
		return true;
	    }
	}
	return false;
    }

    public static boolean hasStockPeriod2(List<Stock> stocks) {
	for (Stock s : stocks) {
	    if (s.getPeriod2() != null) {
		return true;
	    }
	}
	return false;
    }

    public static boolean hasStockPeriod3(List<Stock> stocks) {
	for (Stock s : stocks) {
	    if (s.getPeriod3() != null) {
		return true;
	    }
	}
	return false;
    }

    public static boolean hasStockPeriod4(List<Stock> stocks) {
	for (Stock s : stocks) {
	    if (s.getPeriod4() != null) {
		return true;
	    }
	}
	return false;
    }

    public static boolean hasStockPeriod5(List<Stock> stocks) {
	for (Stock s : stocks) {
	    if (s.getPeriod5() != null) {
		return true;
	    }
	}
	return false;
    }

}
