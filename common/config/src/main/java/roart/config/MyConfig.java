package roart.config;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyConfig {

    protected static Logger log = LoggerFactory.getLogger(MyConfig.class);
    
    protected static MyConfig instance = null;
    
    public static MyConfig instance() {
        return instance;
    }
    
    public MyConfig() {
    }
    
    public boolean useSpark = false;
    
    public String sparkMaster = null;

	private Date mydate = null;

	private String mymarket = "0";

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

	public void setDays(Integer integer) {
	    mydays = integer;
	}

	public int getDays() {
	    return mydays;
	}

	public void setTopBottom(Integer integer) {
	    mytopbottom = integer;
	}

	public int getTopBottom() {
	    return mytopbottom;
	}

	public void setTableDays(Integer integer) {
	    mytabledays = integer;
	}

	public int getTableDays() {
	    return mytabledays;
	}

	public void setTableIntervalDays(Integer integer) {
	    mytableintervaldays = integer;
	}

	public int getTableIntervalDays() {
	    return mytableintervaldays;
	}

	public void setTableMoveIntervalDays(Integer integer) {
	    mytablemoveintervaldays = integer;
	}

	public int getTableMoveIntervalDays() {
	    return mytablemoveintervaldays;
	}

	public void setEqualize(Boolean integer) {
	    myequalize = integer;
	}

	public boolean isEqualize() {
	    return myequalize;
	}

	public void setMoveEnabled(Boolean bool) {
	    moveEnabled = bool;
	}

	public boolean isMoveEnabled() {
	    return moveEnabled;
	}

	public void setMACDEnabled(Boolean bool) {
	    macdEnabled = bool;
	}

	public boolean isMACDEnabled() {
	    return macdEnabled;
	}

	public void setMACDDeltaEnabled(Boolean bool) {
	    macdDeltaEnabled = bool;
	}

	public void setMACDHistogramDeltaEnabled(Boolean bool) {
	    macdHistogramDeltaEnabled = bool;
	}

	public boolean isCCIDeltaEnabled() {
	    return cciDeltaEnabled;
	}

	public void setCCIDeltaEnabled(Boolean bool) {
	    cciDeltaEnabled = bool;
	}

	public boolean isATRDeltaEnabled() {
	    return atrDeltaEnabled;
	}

	public void setATRDeltaEnabled(Boolean bool) {
	    atrDeltaEnabled = bool;
	}

	public boolean isSTOCHDeltaEnabled() {
	    return stochDeltaEnabled;
	}

	public void setSTOCHDeltaEnabled(Boolean bool) {
	    stochDeltaEnabled = bool;
	}

	public boolean isRSIDeltaEnabled() {
	    return rsiDeltaEnabled;
	}

	public void setRSIDeltaEnabled(Boolean bool) {
	    rsiDeltaEnabled = bool;
	}

	public boolean isSTOCHRSIDeltaEnabled() {
	    return stochrsiDeltaEnabled;
	}

	public void setSTOCHRSIDeltaEnabled(Boolean bool) {
	    stochrsiDeltaEnabled = bool;
	}

	public boolean isMACDDeltaEnabled() {
	    return macdDeltaEnabled;
	}

	public boolean isMACDHistogramDeltaEnabled() {
	    return macdHistogramDeltaEnabled;
	}

	public void setRSIEnabled(Boolean bool) {
	    rsiEnabled = bool;
	}

	public boolean isRSIEnabled() {
	    return rsiEnabled;
	}

	public void setSTOCHRSIEnabled(Boolean bool) {
	    stochrsiEnabled = bool;
	}

	public boolean isSTOCHRSIEnabled() {
	    return stochrsiEnabled;
	}

	public void setCCIEnabled(Boolean bool) {
	    cciEnabled = bool;
	}

	public boolean isCCIEnabled() {
	    return cciEnabled;
	}

	public void setATREnabled(Boolean bool) {
	    atrEnabled = bool;
	}

	public boolean isATREnabled() {
	    return atrEnabled;
	}

	public void setSTOCHEnabled(Boolean bool) {
	    stochEnabled = bool;
	}

	public boolean isSTOCHEnabled() {		
	    return stochEnabled;
	}

	public void setGraphEqualize(Boolean integer) {
	    mygraphequalize = integer;
	}

	public boolean isGraphEqualize() {
	    return mygraphequalize;
	}

	public void setGraphEqUnify(Boolean integer) {
	    mygraphequalizeunify = integer;
	}

	public boolean isGraphEqUnify() {
	    return mygraphequalizeunify;
	}

	public int getMACDDeltaDays() {
		return macdDeltaDays;
	}
    
	public void setMACDHistogramDeltaDays(Integer integer) {
		this.macdHistogramDeltaDays = integer;
	}
    
	public int getMACDHistogramDeltaDays() {
		return macdHistogramDeltaDays;
	}
    
	public void setMACDDeltaDays(Integer integer) {
		this.macdDeltaDays = integer;
	}
    
	public int getRSIDeltaDays() {
		return rsiDeltaDays;
	}
    
	public void setRSIDeltaDays(Integer integer) {
		this.rsiDeltaDays = integer;
	}
    
	public int getSTOCHRSIDeltaDays() {
		return stochrsiDeltaDays;
	}
    
	public void setSTOCHRSIDeltaDays(Integer integer) {
		this.stochrsiDeltaDays = integer;
	}
    
	public int getCCIDeltaDays() {
		return cciDeltaDays;
	}
    
	public void setCCIDeltaDays(Integer integer) {
		this.cciDeltaDays = integer;
	}
    
	public int getATRDeltaDays() {
		return atrDeltaDays;
	}
    
	public void setATRDeltaDays(Integer integer) {
		this.atrDeltaDays = integer;
	}
    
	public int getSTOCHDeltaDays() {
		return stochDeltaDays;
	}
    
	public void setSTOCHDeltaDays(Integer integer) {
		this.stochDeltaDays = integer;
	}
    
}
