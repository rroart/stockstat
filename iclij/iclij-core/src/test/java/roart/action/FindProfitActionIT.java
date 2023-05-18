package roart.action;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import roart.db.dao.IclijDbDao;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijXMLConfig;

@SpringBootTest
public class FindProfitActionIT {
    
    @Autowired
    private IclijDbDao dbDao;

    @Autowired
    public IclijConfig iclijConfig;
    
    @Test
    public void test() {
        new FindProfitAction(iclijConfig, dbDao).goal(null, null, null, iclijConfig);
    }
    
}
