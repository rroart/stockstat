package roart.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.io.FileLocator;
import org.apache.commons.configuration2.io.FileLocator.FileLocatorBuilder;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import roart.util.Constants;

public class IclijXMLConfig {

    protected static Logger log = LoggerFactory.getLogger(IclijConfig.class);
   
    protected static IclijXMLConfig instance = null;
   
    public static String configFile = "iclij.xml";
    
   public static IclijXMLConfig instance() {
        if (instance == null) {
            instance = new IclijXMLConfig();
        }
        return instance;
    }

    protected static IclijConfig configInstance = null;
    
     public static IclijConfig getConfigInstance() {
        if (configInstance == null) {
            configInstance = new IclijConfig();
            if (instance == null) { 
                instance();
            }
        }
        return (IclijConfig) configInstance;
    }
    
    private static Configuration config = null;
    private static XMLConfiguration configxml = null;
    
    public IclijXMLConfig() {
        try {
            //config = new PropertiesConfiguration(ConfigConstants.PROPFILE);
            configxml = new XMLConfiguration();
            Parameters params = new Parameters();
            FileBasedConfigurationBuilder<XMLConfiguration> fileBuilder =
                    new FileBasedConfigurationBuilder<>(XMLConfiguration.class)
                    .configure(params.fileBased().setFileName(configFile));
            InputStream stream = new FileInputStream(new File(configFile));         
            configxml = fileBuilder.getConfiguration();
            configxml.read(stream);
            String root = configxml.getRootElementName();
            Document doc = configxml.getDocument();
            configInstance.configTreeMap = new ConfigTreeMap();
            configInstance.configValueMap = new HashMap<String, Object>();
            IclijConfigConstantMaps.makeDefaultMap();
            IclijConfigConstantMaps.makeTextMap();
            IclijConfigConstantMaps.makeTypeMap();
            configInstance.deflt = IclijConfigConstantMaps.deflt;
            configInstance.type = IclijConfigConstantMaps.map;
            configInstance.text = IclijConfigConstantMaps.text;
            handleDoc(doc.getDocumentElement(), configInstance.configTreeMap, "");
            //print(configTreeMap, 0);
            //System.out.println("root " + root);
            //System.out.println("maps "+ configTreeMap);
            //makeTypeMap();
            //configxml.load(ConfigConstants.CONFIGFILE);
            //configxml.initFileLocator(new FileLocator(new FileLocatorBuilder()));
            //System.out.println("m " + configxml.getProperty("markets"));
            List<HierarchicalConfiguration<ImmutableNode>> fields = configxml.childConfigurationsAt("config");
            for (HierarchicalConfiguration field : fields) {
                //System.out.println("field" + field.toString());
            }
            //configxml.childConfigurationsAt();
            //new XMLConfiguration("_config.xml");
            fields = configxml.childConfigurationsAt("markets");
            for (HierarchicalConfiguration field : fields) {
                //System.out.println("field" + field.toString());
                Iterator<String> iter = field.getKeys();
                while(iter.hasNext()) {
                    String s = iter.next();
                    //System.out.println("s1 " + s);
                }

            }
            fields = (configxml).childConfigurationsAt("/misc");
            for (HierarchicalConfiguration field : fields) {
                //System.out.println("field" + field.toString());
            }
            fields = ( configxml).childConfigurationsAt("misc");
            for (HierarchicalConfiguration field : fields) {
                //System.out.println("field" + field.toString());
                Iterator<String> iter = field.getKeys();
                while(iter.hasNext()) {
                    String s = iter.next();
                    //System.out.println("s2 " + s);
                }
            }
            //System.out.println("root " + configxml.getList("config"));
            //System.out.println("root " + configxml.getList("/config"));
            //System.out.println("root " + configxml.getList("/misc"));
            //System.out.println("root " + configxml.getList("misc"));
            IclijConfigConstantMaps.makeTypeMap();
            Iterator<String> iter = configxml.getKeys();
            //System.out.println("kk " + configxml.getList("markets.market"));
            //System.out.println("keys " + ConfigConstants.map.keySet());
            while(iter.hasNext()) {
                String s = iter.next();
                //System.out.println("s " + s + " " + configxml.getString(s) + " " + configxml.getProperty(s));
                Object o = null;
                String text = s;
                Class myclass = IclijConfigConstantMaps.map.get(text);

                if (myclass == null) {
                    //System.out.println("Unknown " + text);
                    log.info("Unknown " + text);
                    continue;
                }
                switch (myclass.getName()) {
                case "java.lang.String":
                    o = configxml.getString(s);
                    break;
                case "java.lang.Integer":
                    o = configxml.getInt(s);
                    break;
                case "java.lang.Double":
                    o = configxml.getDouble(s);
                    break;
                case "java.lang.Boolean":
                    o = configxml.getBoolean(s);
                    break;
                default:
                    //System.out.println("unknown " + myclass.getName());
                    log.info("unknown " + myclass.getName());
                }
                configInstance.configValueMap.put(s, o);
            }
            //((AbstractConfiguration) config).setDelimiterParsingDisabled(true);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(Constants.EXCEPTION, e); 
        }
    }

    private void print(ConfigTreeMap map2, int indent) {
        String space = "      ";
        //System.out.print(space.substring(0, indent));
        //System.out.println("map2 " + map2.name + " " + map2.enabled);
        Map<String, ConfigTreeMap> map3 = map2.configTreeMap;
        for (String key : map3.keySet()) {
        print(map3.get(key), indent + 1);
            //Object value = map.get(key);
            //System.out.println("k " + key + " " + value + " " + value.getClass().getName());
        }
       
    }

    private void handleDoc(Element documentElement, ConfigTreeMap configMap, String baseString) {
        String name = documentElement.getNodeName();
        String basename = name;
        String attribute = documentElement.getAttribute("enable");
        NodeList elements = documentElement.getChildNodes();
        boolean leafNode = elements.getLength() == 0;
        Boolean enabled = null;
        if (attribute != null) {
            enabled = !attribute.equals("false");
            if (/*leafNode &&*/ !attribute.isEmpty()) {
                name = name + "[@enable]";
            }
        }
        configMap.name = baseString + "." + name;
        configMap.name = configMap.name.replaceFirst(".config.", "");
        //System.out.println("name " + configMap.name);
        if (leafNode) {
            //enabled = null;
        }
        configMap.enabled = enabled;
        configMap.configTreeMap = new HashMap<String, ConfigTreeMap>();
        for (int i = 0; i < elements.getLength(); i++) {
            Node node = elements.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                ConfigTreeMap newMap = new ConfigTreeMap();
                Element element = (Element) node;
                String newBaseString = baseString + "." + basename;
                newBaseString = newBaseString.replaceFirst(".config.", "");
                handleDoc(element, newMap, newBaseString);
                String text = element.getNodeName();
                configMap.configTreeMap.put(text, newMap);
            }
        }
        
    }

    public void config() throws Exception {
        /*
        if (config == null) {
            return;
        }
        Boolean spark = getBoolean(ConfigConstants.SPARK, false, false, false);
        configSpark(spark);
 */
    }

    /*
    public void configSpark(Boolean spark) throws Exception {
        this.useSpark = spark;
        if (spark != null && spark) {
            String master = getString(ConfigConstants.SPARKMASTER);
            if (master != null) {
                //this.sparkMaster = master;
            } else {
                //throw new Exception("No Spark master");
            }
         }
    }
*/
    
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
        } catch (Exception e) {
            if (fatal) {
                //System.out.println("Can not convert config " + key);
                log.error("Can not convert config " + key);
                System.exit(0);
            } else {
                //System.out.println("Can not convert config " + key);
                log.error("Can not convert config " + key);                
            }
        }
        if (value == null && mandatory) {
            //System.out.println("Can not find mandatory config " + key);
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
                //System.out.println("Illegal value " + key);
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
        } catch (Exception e) {
            if (fatal) {
                //System.out.println("Can not convert config " + key);
                log.error("Can not convert config " + key);
                System.exit(0);
            } else {
                //System.out.println("Can not convert config " + key);
                log.error("Can not convert config " + key);                
            }
        }
        if (value == null && mandatory) {
            //System.out.println("Can not find mandatory config " + key);
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
        } catch (Exception e) {
            if (fatal) {
                //System.out.println("Can not convert config " + key);
                log.error("Can not convert config " + key);
                System.exit(0);
            } else {
                //System.out.println("Can not convert config " + key);
                log.error("Can not convert config " + key);                
            }
        }
        if (value == null && mandatory) {
            //System.out.println("Can not find mandatory config " + key);
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
        } catch (Exception e) {
            if (fatal) {
                //System.out.println("Can not convert config " + key);
                log.error("Can not convert config " + key);
                //System.exit(0);
            } else {
                //System.out.println("Can not convert config " + key);
                log.error("Can not convert config " + key);                
            }
        }
        if (value == null && mandatory) {
            //System.out.println("Can not find mandatory config " + key);
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

    public static XMLConfiguration getConfigXML() {
        return configxml;
    }
}
