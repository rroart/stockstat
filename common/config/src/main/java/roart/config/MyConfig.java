package roart.config;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyConfig {

    public MyConfig() {
    }
    
    public ConfigTreeMap configTreeMap;
    
    public Map<String, Object> configValueMap;
    public Map<String, String> text = new HashMap();
    public Map<String, Object> deflt = new HashMap();
    public Map<String, Class> type = new HashMap();
    
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
