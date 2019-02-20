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
    
    private Map<String, String> text = new HashMap<>();
    
    private Map<String, Object> deflt = new HashMap<>();
    
    private Map<String, Double[]> range = new HashMap<>();
    
    private Map<String, Class> type = new HashMap<>();

    //public boolean useSpark = false;

    //public String sparkMaster = null;

    protected Date mydate = null;

    protected String mymarket = "0";

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

	// TODO warning this looks weird, avoid for now?
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

    public Map<String, String> getText() {
        return text;
    }

    public void setText(Map<String, String> text) {
        this.text = text;
    }

    public Map<String, Object> getDeflt() {
        return deflt;
    }

    public void setDeflt(Map<String, Object> deflt) {
        this.deflt = deflt;
    }

    public Map<String, Class> getType() {
        return type;
    }

    public void setType(Map<String, Class> type) {
        this.type = type;
    }

    public Map<String, Double[]> getRange() {
        return range;
    }

    public void setRange(Map<String, Double[]> range) {
        this.range = range;
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


}
