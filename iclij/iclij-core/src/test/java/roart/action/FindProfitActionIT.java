package roart.action;

import org.junit.jupiter.api.Test;

import roart.iclij.config.IclijXMLConfig;

public class FindProfitActionIT {
    
    @Test
    public void test() {
        IclijXMLConfig.getConfigInstance();
        new FindProfitAction().goal(null, null, null);
    }
    
}
