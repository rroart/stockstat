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

public class MyXMLConfig {

    protected static Logger log = LoggerFactory.getLogger(MyConfig.class);
   
    protected static MyXMLConfig instance = null;
    
   public static MyXMLConfig instance() {
        if (instance == null) {
            instance = new MyXMLConfig();
        }
        return instance;
    }

    protected static MyConfig configInstance = null;
    
     public static MyConfig getConfigInstance() {
        if (configInstance == null) {
            configInstance = new MyConfig();
            if (instance == null) { 
                instance();
            }
        }
        return (MyConfig) configInstance;
    }
    
    private static Configuration config = null;
    private static XMLConfiguration configxml = null;
    
    public MyXMLConfig() {
        try {
            //config = new PropertiesConfiguration(ConfigConstants.PROPFILE);
            configxml = new XMLConfiguration();
            Parameters params = new Parameters();
            FileBasedConfigurationBuilder<XMLConfiguration> fileBuilder =
                    new FileBasedConfigurationBuilder<>(XMLConfiguration.class)
                    .configure(params.fileBased().setFileName("../conf/" + ConfigConstants.CONFIGFILE));
            InputStream stream = new FileInputStream(new File("../conf/" + ConfigConstants.CONFIGFILE));         
            configxml = fileBuilder.getConfiguration();
            configxml.read(stream);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            configxml = null;
        }
        Document doc = null;
        configInstance.configTreeMap = new ConfigTreeMap();
        configInstance.configValueMap = new HashMap<String, Object>();
        ConfigConstantMaps.makeDefaultMap();
        ConfigConstantMaps.makeTextMap();
        ConfigConstantMaps.makeTypeMap();
        configInstance.deflt = ConfigConstantMaps.deflt;
        configInstance.type = ConfigConstantMaps.map;
        configInstance.text = ConfigConstantMaps.text;
        if (configxml != null) {
            printout();
            doc = configxml.getDocument();
            if (doc != null) {
                handleDoc(doc.getDocumentElement(), configInstance.configTreeMap, "");
            }
            Iterator<String> iter = configxml.getKeys();
            //System.out.println("keys " + ConfigConstants.map.keySet());
            while(iter.hasNext()) {
                String s = iter.next();
                //System.out.println("s " + s + " " + configxml.getString(s) + " " + configxml.getProperty(s));
                Object o = null;
                String text = s;
                Class myclass = ConfigConstantMaps.map.get(text);

                if (myclass == null) {
                    System.out.println("Unknown " + text);
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
                    System.out.println("unknown " + myclass.getName());
                    log.info("unknown " + myclass.getName());
                }
                configInstance.configValueMap.put(s, o);
            }
        }
        //print(configTreeMap, 0);
        //System.out.println("root " + root);
        //System.out.println("maps "+ configTreeMap);
        //makeTypeMap();
        //configxml.load(ConfigConstants.CONFIGFILE);
        //configxml.initFileLocator(new FileLocator(new FileLocatorBuilder()));
        //configxml.childConfigurationsAt();
        //new XMLConfiguration("_config.xml");
        //System.out.println("root " + configxml.getList("config"));
        //System.out.println("root " + configxml.getList("/config"));
        //System.out.println("root " + configxml.getList("/misc"));
        //System.out.println("root " + configxml.getList("misc"));
        //((AbstractConfiguration) config).setDelimiterParsingDisabled(true);
    }

    private void printout() {
        String root = configxml.getRootElementName();
        List<HierarchicalConfiguration<ImmutableNode>> fields = configxml.childConfigurationsAt(root);
        for (HierarchicalConfiguration field : fields) {
            System.out.println("field" + field.toString());
        }
        fields = configxml.childConfigurationsAt("machinelearning");
        for (HierarchicalConfiguration field : fields) {
            System.out.println("field" + field.toString());
            Iterator<String> iter = field.getKeys();
            while(iter.hasNext()) {
                String s = iter.next();
                System.out.println("s1 " + s);
            }

        }
        fields = (configxml).childConfigurationsAt("/misc");
        for (HierarchicalConfiguration field : fields) {
            System.out.println("field" + field.toString());
        }
        fields = ( configxml).childConfigurationsAt("misc");
        for (HierarchicalConfiguration field : fields) {
            System.out.println("field" + field.toString());
            Iterator<String> iter = field.getKeys();
            while(iter.hasNext()) {
                String s = iter.next();
                System.out.println("s2 " + s);
            }
        }
    }

    private void print(ConfigTreeMap map2, int indent) {
        String space = "      ";
        System.out.print(space.substring(0, indent));
        System.out.println("map2 " + map2.name + " " + map2.enabled);
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
        } catch (Exception e) {
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
        } catch (Exception e) {
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
        } catch (Exception e) {
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
