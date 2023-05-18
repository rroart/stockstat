package roart.controller;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import roart.action.FindProfitAction;
import roart.action.MarketAction;
import roart.common.model.ConfigItem;
import roart.common.model.TimingBLItem;
import roart.db.dao.IclijDbDao;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijXMLConfig;
import roart.common.config.MyConfig;
import roart.common.config.MyMyConfig;
import roart.common.config.MyXMLConfig;

@Import(DbDaoUtil.class)
@SpringBootTest
public class ServiceControllerIT {

    @Autowired
    private IclijDbDao dbDao;

    @Autowired
    private DbDaoUtil dbDaoUtil;
    
    @Autowired
    public IclijConfig iclijConfig;
    
    @Autowired
    MyXMLConfig conf2;
    
    @Test
    public void t() {
        MarketAction c = new FindProfitAction(iclijConfig, dbDao);
        TimingBLItem t = new TimingBLItem();
        List l = c.getActionData().getDbDao().getAllTimingBLItem();
        c.getActionData().getDbDao().save(t);
        List l2 = c.getActionData().getDbDao().getAllTimingBLItem();
        System.out.println("" + l.size() + " " + l2.size());
    }

    @Test
    public void t2() {
        MarketAction c = new FindProfitAction(iclijConfig, dbDao);
        List<TimingBLItem> cs = c.getActionData().getDbDao().getAllTimingBLItem();
    }

    @Test
    public void t4() throws Exception {
        List<ConfigItem> l0 = dbDaoUtil.getAll("ose", 0);
        List<ConfigItem> l1 = dbDaoUtil.getAll("ose", 1);
        List<ConfigItem> l2 = dbDaoUtil.getAll("ose", 2);
                System.out.println("" + l0.size() + " " + l1.size() + " " + l2.size());
        System.out.println("" + l0.get(0).getValue());
        System.out.println("" + l1.get(0).getValue());
        System.out.println("" + l2.get(0).getValue());
    }
}
