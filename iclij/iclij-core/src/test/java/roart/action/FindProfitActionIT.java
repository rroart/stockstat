package roart.action;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import roart.db.dao.IclijDbDao;
import roart.iclij.config.IclijXMLConfig;

@SpringBootTest
public class FindProfitActionIT {
    
    @Autowired
    private IclijDbDao dbDao;

    @Test
    public void test() {
        IclijXMLConfig.getConfigInstance();
        new FindProfitAction(dbDao).goal(null, null, null);
    }
    
}
