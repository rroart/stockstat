package roart.common.config;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class MyConfig {

    private ConfigData configData = new ConfigData();
    
    public MyConfig() {
        super();
    }

    public MyConfig(MyConfig config) {
        this.configData = config.getConfigData();
    }

    public MyConfig(ConfigData data) {
        this.configData = data;
    }

    //public boolean useSpark = false;

    //public String sparkMaster = null;

    /*
	private Integer mydays = 180;

	private Integer mytopbottom = 10;

	private Integer mytabledays = 180;

	private Integer mytablemoveintervaldays = 5;

	private Integer mytableintervaldays = 1;

	private boolean myequalize = false;

	private boolean mygraphequalize = false;

	private boolean mygraphequalizeunify = false;

	private boolean macdEnabled = true;

	private boolean macdDeltaEnabled = true;

	private boolean macdHistogramDeltaEnabled = true;

	private boolean moveEnabled = true;

	private boolean rsiEnabled = true;

	private boolean rsiDeltaEnabled = true;

	private boolean stochrsiEnabled = false;

	private boolean stochrsiDeltaEnabled = false;

	private boolean cciEnabled = true;

	private boolean cciDeltaEnabled = true;

	private boolean atrEnabled = true;

	private boolean atrDeltaEnabled = true;

	private boolean stochEnabled = true;

	private boolean stochDeltaEnabled = true;

	private int macdDeltaDays = 3;

	private int macdHistogramDeltaDays = 3;

	private int rsiDeltaDays = 3;

	private int stochrsiDeltaDays = 3;

	private int cciDeltaDays = 3;

	private int atrDeltaDays = 3;

	private int stochDeltaDays = 3;
     */
    //public void config() throws Exception;

    public ConfigData getConfigData() {
        return configData;
    }

    public void setConfigData(ConfigData configData) {
        this.configData = configData;
    }

}
