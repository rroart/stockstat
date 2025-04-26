package roart.controller;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import roart.action.FindProfitAction;
import roart.action.MarketAction;
import roart.common.model.ConfigDTO;
import roart.common.model.TimingBLDTO;
import roart.db.dao.IclijDbDao;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijXMLConfig;
import roart.iclij.config.bean.ConfigI;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijConfig;
import roart.common.config.MyXMLConfig;

@Import(DbDaoUtil.class)
@SpringBootTest(classes=Config.class)
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
        MarketAction c = new FindProfitAction(iclijConfig);
        TimingBLDTO t = new TimingBLDTO();
        List l = dbDao.getAllTimingBLDTO();
        dbDao.save(t);
        List l2 = dbDao.getAllTimingBLDTO();
        System.out.println("" + l.size() + " " + l2.size());
    }

    @Test
    public void t2() {
        MarketAction c = new FindProfitAction(iclijConfig);
        List<TimingBLDTO> cs = dbDao.getAllTimingBLDTO();
    }

    @Test
    public void t4() throws Exception {
        List<ConfigDTO> l0 = dbDaoUtil.getAll("ose", 0);
        List<ConfigDTO> l1 = dbDaoUtil.getAll("ose", 1);
        List<ConfigDTO> l2 = dbDaoUtil.getAll("ose", 2);
                System.out.println("" + l0.size() + " " + l1.size() + " " + l2.size());
        System.out.println("" + l0.get(0).getValue());
        System.out.println("" + l1.get(0).getValue());
        System.out.println("" + l2.get(0).getValue());
    }
}
