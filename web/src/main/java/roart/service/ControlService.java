package roart.service;

import roart.model.ResultItem;
import roart.model.Stock;

import javax.servlet.http.*;

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
    private Logger log = LoggerFactory.getLogger(this.getClass());

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

    private static Date mydate = new Date();
    
    public void setdate(Date date) {
        this.mydate = date;
    }

    public static List getContent() {
        mydate.setHours(0);
        mydate.setMinutes(0);
        mydate.setSeconds(0);
        List<ResultItem> retList = new ArrayList<ResultItem>();
        ResultItem ri = new ResultItem();
        //ri.add("Id");
        ri.add("Name");
        ri.add("Date");
        ri.add("Day");
        ri.add("Week");
        ri.add("Month");
        ri.add("This year");
        ri.add("Price");
        ri.add("Currency");
        retList.add(ri);
        try {
        List<Stock> stocks = Stock.getAll();
        //System.out.println("size " +stocks.size());
        for (Stock stock : stocks) {
            //System.out.println("" + mydate.getTime() + "|" + stock.getDate().getTime());
            if (mydate.getTime() != stock.getDate().getTime()) {
                continue;
            }
            ResultItem r = new ResultItem();
            //r.add(stock.getId());
            r.add(stock.getName());
            r.add(stock.getDate());
            r.add(stock.getDay());
            r.add(stock.getWeek());
            r.add(stock.getMonth());
            r.add(stock.getThisyear());
            r.add(stock.getPrice());
            r.add(stock.getCurrency());
            //r.add(stock.get());
            retList.add(r);
            
        }
        } catch (Exception e) {
            e.printStackTrace();
            //log.error(Constants.EXCEPTION, e);
        }
      List<List> retlistlist = new ArrayList<List>();
        retlistlist.add(retList);
        return retlistlist;
    }
    
}
