package roart.common.config;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MyConfig {

    public MyConfig() {
        super();
    }

    private ConfigTreeMap configTreeMap;

    private Map<String, Object> configValueMap;
    
    private ConfigMaps configMaps;
    
    //public boolean useSpark = false;

    //public String sparkMaster = null;

    protected Date mydate = null;

    protected String mymarket = "0";

    protected String mlmarket = null;

    protected boolean dataset = false;
    
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

    public ConfigTreeMap getConfigTreeMap() {
        return configTreeMap;
    }

    public void setConfigTreeMap(ConfigTreeMap configTreeMap) {
        this.configTreeMap = configTreeMap;
    }

    public Map<String, Object> getConfigValueMap() {
        return configValueMap;
    }

    public void setConfigValueMap(Map<String, Object> configValueMap) {
        this.configValueMap = configValueMap;
    }

    public ConfigMaps getConfigMaps() {
        return configMaps;
    }

    public void setConfigMaps(ConfigMaps configMaps) {
        this.configMaps = configMaps;
    }

    /**
     * Set current date
     * 
     * @param date
     */

    public void setdate(Date date) {
        mydate = date;
    }

    /**
     * Get current date
     * 
     * @return date
     */

    public Date getdate() {
        return mydate;
    }

    public void setMarket(String value) {
        mymarket = value;
    }

    public String getMarket() {
        return mymarket;
    }

    public void setMLmarket(String value) {
        mlmarket = value;
    }

    public String getMLmarket() {
        return mlmarket;
    }

    public boolean isDataset() {
        return dataset;
    }

    public void setDataset(boolean dataset) {
        this.dataset = dataset;
    }


}
