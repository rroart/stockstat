package roart.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import roart.db.hibernate.DbHibernate;
import roart.db.hibernate.DbHibernateAccess;
import roart.common.model.StockItem;
import roart.common.springdata.model.Stock;
import roart.common.springdata.repository.SpringStockRepository;
import roart.common.springdata.repository.StockRepository;

@Configuration
@ComponentScan
@Component
@Service
public class DbDaoUtil {

    @Autowired
    SpringStockRepository repo;

    @Autowired
    StockRepository repo2;

    public List<String> getMarkets() throws Exception {
        return repo2.getMarkets();
    }
    
    public List<StockItem> getAll(String market, int type) throws Exception {
        List<StockItem> list = new ArrayList<>();
        long time0 = System.currentTimeMillis();
        if (type == 0) {
            return DbHibernateAccess.instance().getStocksByMarket(market);
        }
        if (type == 1) {
            return repo2.getAll(market);
        }
        List<Stock> list2 = repo.findByMarketid(market);
        for (Stock stock : list2) {
            list.add(new StockItem(stock.getDbid(), stock.getMarketid(), stock.getId(), stock.getIsin(), stock.getName(), stock.getDate(), stock.getIndexvalue(), stock.getIndexvaluelow(), stock.getIndexvaluehigh(), stock.getIndexvalueopen(), stock.getPrice(), stock.getPricelow(), stock.getPricehigh(), stock.getPriceopen(), stock.getVolume(), stock.getCurrency(), stock.getPeriod1(), stock.getPeriod2(), stock.getPeriod3(), stock.getPeriod4(), stock.getPeriod5(), stock.getPeriod6(), stock.getPeriod7(), stock.getPeriod8(), stock.getPeriod9()));

        }
        System.out.println("" + (System.currentTimeMillis() - time0) / 1000);
        return list;
    }

    public void bla() throws Exception {
        //System.setProperty("connection.url","jdbc:postgresql://localhost:5432/stockstatdev?user=stockstat&password=password");
        //System.setProperty("
        long time0;
 
        System.out.println("t0");
        time0 = System.currentTimeMillis();
        getAll("ose", 0);
        System.out.println("" + (System.currentTimeMillis() - time0) / 1000);
        
        System.out.println("t1");
        time0 = System.currentTimeMillis();
        DbHibernate.getAll2("ose");
        System.out.println("" + (System.currentTimeMillis() - time0) / 1000);
        
        System.out.println("t2");
        time0 = System.currentTimeMillis();
        getAll("ose", 2);
        System.out.println("" + (System.currentTimeMillis() - time0) / 1000);
        
        System.out.println("t3");
        time0 = System.currentTimeMillis();
        getAll("ose", 1);
        System.out.println("" + (System.currentTimeMillis() - time0) / 1000);
        System.out.println("t4");
    }

    @Value("${spring.datasource.url}")
    String str;
    
    public void bla2() throws Exception {
        //System.setProperty("connection.url","jdbc:postgresql://localhost:5432/stockstatdev?user=stockstat&password=password");
        System.out.println("pr " + System.getProperty("spring.datasource.url"));
        System.out.println("str " + str);
        long time0;
 
        System.out.println("t3");
        time0 = System.currentTimeMillis();
        getAll("ose", 1);
        System.out.println("" + (System.currentTimeMillis() - time0) / 1000);
        
        System.out.println("t4");
    }

}

