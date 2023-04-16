package roart.controller;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import roart.action.FindProfitAction;
import roart.action.MarketAction;
import roart.common.model.TimingBLItem;
import roart.db.dao.IclijDbDao;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijXMLConfig;
import roart.common.config.MyConfig;
import roart.common.config.MyMyConfig;
import roart.common.config.MyXMLConfig;

@SpringBootTest
public class ServiceControllerIT {

    @Autowired
    private IclijDbDao dbDao;

    @Autowired
    IclijXMLConfig conf;
    
    @Autowired
    MyXMLConfig conf2;
    
    @Test
    public void t() {
        MarketAction c = new FindProfitAction(dbDao);
        TimingBLItem t = new TimingBLItem();
        List l = c.getActionData().getDbDao().getAllTimingBLItem();
        c.getActionData().getDbDao().save(t);
        List l2 = c.getActionData().getDbDao().getAllTimingBLItem();
        System.out.println("" + l.size() + " " + l2.size());
    }

    @Test
    public void t2() {
        MarketAction c = new FindProfitAction(dbDao);
        List<TimingBLItem> cs = c.getActionData().getDbDao().getAllTimingBLItem();
    }

}
