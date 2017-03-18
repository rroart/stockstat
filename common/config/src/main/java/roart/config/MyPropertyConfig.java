package roart.config;

import java.util.NoSuchElementException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConversionException;
import org.apache.commons.configuration.PropertiesConfiguration;

import roart.util.Constants;

public class MyPropertyConfig extends MyConfig {

     public static MyConfig instance() {
        if (instance == null) {
            instance = new MyPropertyConfig();
        }
        return instance;
    }
    
    private static Configuration config = null;
    
    public MyPropertyConfig() {
        super();
	try {
         config = new PropertiesConfiguration(ConfigConstants.PROPFILE);
         //((AbstractConfiguration) config).setDelimiterParsingDisabled(true);
    } catch (ConfigurationException e) {
        log.error(Constants.EXCEPTION, e); 
    }
    }

    public void config() throws Exception {
        if (config == null) {
            return;
        }
        configSpark();
 
    }

    private void configSpark() throws Exception {
        Boolean spark = getBoolean(ConfigConstants.SPARK, false, false, false);
        this.useSpark = spark;
        if (spark != null && spark) {
            String master = getString(ConfigConstants.SPARKMASTER);
            if (master != null) {
                this.sparkMaster = master;
            } else {
                throw new Exception("No Spark master");
            }
        }
    }

    public String getString(String string) {
        return config.getString(string);
    }
    
    public String[] getStringArray(String string) {
        return config.getStringArray(string);
    }
    
    public Boolean getBoolean(String string) {
        return config.getBoolean(string);
    }
    
    public Integer getInteger(String string) {
        return config.getInt(string);
    }
    
    public String getString(String key, String defaultvalue, boolean mandatory, boolean fatal, String [] legalvalues) {
        String value = null;
        try {
            value = getString(key);
        } catch (NoSuchElementException e) {
        } catch (ConversionException e) {
            if (fatal) {
                System.out.println("Can not convert config " + key);
                log.error("Can not convert config " + key);
                System.exit(0);
            } else {
                System.out.println("Can not convert config " + key);
                log.error("Can not convert config " + key);                
            }
        }
        if (value == null && mandatory) {
            System.out.println("Can not find mandatory config " + key);
            log.error("Can not find mandatory config " + key);
            System.exit(0);            
        }
        if (value == null) {
            value = defaultvalue;
        }
        boolean foundvalue = false;
        if (legalvalues != null) {
            for (String legalvalue : legalvalues) {
                if (legalvalue.equals(value)) {
                    foundvalue = true;
                    break;
                }
            }
        } else {
            foundvalue = true;
        }
        if (!foundvalue) {
            if (fatal || mandatory) {
                System.out.println("Illegal value " + key);
                log.error("Illegal value " + key);
                System.exit(0);                
            }
            if (defaultvalue != null) {
                log.error("Illegal value " + key + " = " + value + " setting to default " + defaultvalue);
                value = defaultvalue;
            } else {
                log.error("Ignoring illegal value " + key + " = " + value);
            }
        }
        return value;
    }
    
    public String[] getStringArray(String key, String[] defaultvalue, boolean mandatory, boolean fatal) {
        String value[] = null;
        try {
            value = getStringArray(key);
        } catch (NoSuchElementException e) {
        } catch (ConversionException e) {
            if (fatal) {
                System.out.println("Can not convert config " + key);
                log.error("Can not convert config " + key);
                System.exit(0);
            } else {
                System.out.println("Can not convert config " + key);
                log.error("Can not convert config " + key);                
            }
        }
        if (value == null && mandatory) {
            System.out.println("Can not find mandatory config " + key);
            log.error("Can not find mandatory config " + key);
            System.exit(0);            
        }
        if (value == null) {
            value = defaultvalue;
        }
        return value;
    }
    
    public Boolean getBoolean(String key, Boolean defaultvalue, boolean mandatory, boolean fatal) {
        Boolean value = null;
        try {
            value = getBoolean(key);
        } catch (NoSuchElementException e) {
        } catch (ConversionException e) {
            if (fatal) {
                System.out.println("Can not convert config " + key);
                log.error("Can not convert config " + key);
                System.exit(0);
            } else {
                System.out.println("Can not convert config " + key);
                log.error("Can not convert config " + key);                
            }
        }
        if (value == null && mandatory) {
            System.out.println("Can not find mandatory config " + key);
            log.error("Can not find mandatory config " + key);
            System.exit(0);            
        }
        if (value == null) {
            value = defaultvalue;
        }
        return value;
    }
    
    public Integer getInteger(String key, Integer defaultvalue, boolean mandatory, boolean fatal) {
        Integer value = null;
        try {
            value = getInteger(key);
        } catch (NoSuchElementException e) {
        } catch (ConversionException e) {
            if (fatal) {
                System.out.println("Can not convert config " + key);
                log.error("Can not convert config " + key);
                System.exit(0);
            } else {
                System.out.println("Can not convert config " + key);
                log.error("Can not convert config " + key);                
            }
        }
        if (value == null && mandatory) {
            System.out.println("Can not find mandatory config " + key);
            log.error("Can not find mandatory config " + key);
            System.exit(0);            
        }
        if (value == null) {
            value = defaultvalue;
        }           
        if (value < 0) {
            log.error("Illegal value " + key + " " + value + " setting to default " + defaultvalue);
            value = defaultvalue;
        }
        
        return value;
    }
    
}
