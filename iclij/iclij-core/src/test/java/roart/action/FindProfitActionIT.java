package roart.action;

import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import roart.db.dao.IclijDbDao;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijXMLConfig;
import roart.model.io.IO;

@Ignore // TODO
@SpringBootTest
public class FindProfitActionIT {
    
    @Autowired
    private IO io;

    @Autowired
    public IclijConfig iclijConfig;
    
    @Ignore // TODO
 // TODO @Test
    public void test() {
        new FindProfitAction(iclijConfig).goal(null, null, null, iclijConfig, io);
    }
    
}
