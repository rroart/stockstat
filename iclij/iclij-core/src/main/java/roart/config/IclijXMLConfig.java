package roart.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Map.Entry;

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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import roart.common.config.ConfigTreeMap;
import roart.common.constants.Constants;
import roart.iclij.config.IclijConfig;

public class IclijXMLConfig {

    protected static Logger log = LoggerFactory.getLogger(IclijXMLConfig.class);

    protected static IclijXMLConfig instance = null;

    public static String configFile = "../conf/iclij.xml";
    //public static String configFile = "/tmp/iclij.xml";

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
        } catch (Exception e) {
            e.printStackTrace();
            log.error(Constants.EXCEPTION, e); 
            log.error("Config file not found, can not continue");
            System.out.println("Config file not found, can not continue");
            System.exit(1);
        }
        Document doc = configxml.getDocument();
        configInstance.setConfigTreeMap(new ConfigTreeMap());
        configInstance.setConfigValueMap(new HashMap<String, Object>());
        IclijConfigConstantMaps.makeDefaultMap();
        IclijConfigConstantMaps.makeTextMap();
        IclijConfigConstantMaps.makeTypeMap();
        IclijConfigConstantMaps.makeConvertMap();
        configInstance.setDeflt(IclijConfigConstantMaps.deflt);
        configInstance.setType(IclijConfigConstantMaps.map);
        configInstance.setText(IclijConfigConstantMaps.text);
        configInstance.setConv(IclijConfigConstantMaps.conv);
        if (configxml != null) {
            printout();
            doc = configxml.getDocument();
            if (doc != null) {
                handleDoc(doc.getDocumentElement(), configInstance.getConfigTreeMap(), "");
            }
            setValues();
        }
        //((AbstractConfiguration) config).setDelimiterParsingDisabled(true);
    }

    private void setValues() {
        String root = configxml.getRootElementName();
        System.out.println("r " + root);
        List<HierarchicalConfiguration<ImmutableNode>> iter2 = configxml.childConfigurationsAt(".");
        for            (HierarchicalConfiguration<ImmutableNode> i : iter2 ) {
            System.out.println(i.getRootElementName());
        }
        HierarchicalConfiguration<ImmutableNode> iter = configxml.configurationAt(".");
        System.out.println(iter.getRootElementName());
        //iter.c
        setValues(iter, "" /*root*/);
        //iter.get
        //List<HierarchicalConfiguration<ImmutableNode>> iter3 = configxml.childConfigurationsAt(elem);
    }

    private void setValues(HierarchicalConfiguration<ImmutableNode> elem, String base) {
        System.out.println("base " + base);
        List<HierarchicalConfiguration<ImmutableNode>> iter3 = elem.childConfigurationsAt(".");
        for (HierarchicalConfiguration<ImmutableNode> elem2 : iter3) {
            String here = elem2.getRootElementName();
            String key = here;
            String node = null;
            if (base == null || base.isEmpty()) {
                node = here;
            } else {
                node = base + "." + here;
            }
            String node0 = node;
            // node is for nodes with eventual children, need id=..., but no enable
            // node0 is for map get put
            System.out.println("b " + node);
            String enable = (String) elem2.getProperty("[@enable]");
            if (enable != null) {
                //node = node + "[@enable]";
                node0 = node0 + "[@enable]";
                key = "[@enable]";
            }
            System.out.println("en " + enable);
            String id = (String) elem2.getProperty("[@id]");
            if (id != null) {
                node0 = node0 + "[@id=" + id + "]";
                node = node + "[@id]";
                key = key + "[@id=" + id + "]";
            }
            System.out.println("id " + id);
            //Properties i = elem2.getProperties("[]");
            //System.out.println(i);
            //System.out.println("s " + s + " " + configxml.getString(s) + " " + configxml.getProperty(s));
            Object o = null;
            Iterator<String> grr = elem2.getKeys();
            while (grr.hasNext()) {
                String s0 = grr.next();
                System.out.println("s0 " + s0);
            }
            String text = node;
            Class myclass = IclijConfigConstantMaps.map.get(node0);
            if (myclass == null) {
                myclass = IclijConfigConstantMaps.map.get(node);
            }
            String s = "";
            if (myclass == null) {
                //System.out.println("Unknown " + text);
                //Object j = configxml.getList(String.class, s);
                //System.out.println(configxml.getProperties(s));
                //System.out.println(j);
                //o = configxml.getString(s);
                log.info("Unknown " + text);
                //continue;
            } else {
                String s2 = "";
                switch (myclass.getName()) {
                case "java.lang.String":
                    o = elem2.getString("");
                    //o = configxml.getString(s);
                    break;
                case "java.lang.Integer":
                    o = elem2.getInt("");
                    break;
                case "java.lang.Double":
                    o = elem2.getDouble("");
                    break;
                case "java.lang.Boolean":
                    //elem2.get
                    o = Boolean.valueOf(enable);
                    //o = elem2.getBoolean(key);
                    //o = configxml.getBoolean(s);
                    break;
                default:
                    //System.out.println("unknown " + myclass.getName());
                    log.info("unknown " + myclass.getName());
                }
                configInstance.getConfigValueMap().put(node0, o);
            }
            setValues(elem2, node);
        }
    }

    private void setValuesOld() {
        Iterator<String> iter = configxml.getKeys();
        //print(configTreeMap, 0);
        //System.out.println("root " + root);
        //System.out.println("maps "+ configTreeMap);
        //makeTypeMap();
        //configxml.load(ConfigConstants.CONFIGFILE);
        //configxml.initFileLocator(new FileLocator(new FileLocatorBuilder()));
        //System.out.println("m " + configxml.getProperty("markets"));
        printoutnot();
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
                Object j = configxml.getList(String.class, s);
                System.out.println(configxml.getProperties(s));
                System.out.println(j);
                o = configxml.getString(s);
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
            configInstance.getConfigValueMap().put(s, o);
        }
    }

    private void printout() {
        String root = configxml.getRootElementName();
        List<HierarchicalConfiguration<ImmutableNode>> fields = configxml.childConfigurationsAt(root);
        for (HierarchicalConfiguration<ImmutableNode> field : fields) {
            String fieldString = field.toString();
            log.info("field {}", fieldString);
        }
        fields = configxml.childConfigurationsAt("machinelearning");
        for (HierarchicalConfiguration<ImmutableNode> field : fields) {
            String fieldString = field.toString();
            log.info("field {}",  fieldString);
            Iterator<String> iter = field.getKeys();
            while(iter.hasNext()) {
                String s = iter.next();
                log.info("s1 {}", s);
            }

        }
        fields = (configxml).childConfigurationsAt("/misc");
        for (HierarchicalConfiguration<ImmutableNode> field : fields) {
            String fieldString = field.toString();
            log.info("field {}", fieldString);
        }
        fields = ( configxml).childConfigurationsAt("misc");
        for (HierarchicalConfiguration<ImmutableNode> field : fields) {
            String fieldString = field.toString();
            log.info("field {}", fieldString);
            Iterator<String> iter = field.getKeys();
            while(iter.hasNext()) {
                String s = iter.next();
                log.info("s2 {}",  s);
            }
        }
    }

    private void printoutnot() {
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
    }

    private void print(ConfigTreeMap map2, int indent) {
        String space = "      ";
        log.info("map2 {} {} {}", space.substring(0, indent), map2.getName(), map2.getEnabled());
        Map<String, ConfigTreeMap> map3 = map2.getConfigTreeMap();
        for (Entry<String, ConfigTreeMap> entry : map3.entrySet()) {
            print(entry.getValue(), indent + 1);
        }
    }

    private String handleDoc(Element documentElement, ConfigTreeMap configMap, String baseString) {
        String name = documentElement.getNodeName();
        String basename = name;
        String mytext = name;
        String id = documentElement.getAttribute("id");
        if (id != null && !id.isEmpty()) {
            name = name + "[@id=" +id + "]";
            mytext = mytext + "[@id=" +id + "]";
            configInstance.getDeflt().put(baseString + "." + name, String.class);
        }
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
        configMap.setName(baseString + "." + name);
        configMap.setName(configMap.getName().replaceFirst(".config.", ""));
        //System.out.println("name " + configMap.name);
        if (leafNode) {
            //enabled = null;
        }
        configMap.setEnabled(enabled);
        configMap.setConfigTreeMap(new HashMap<String, ConfigTreeMap>());
        for (int i = 0; i < elements.getLength(); i++) {
            Node node = elements.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                ConfigTreeMap newMap = new ConfigTreeMap();
                Element element = (Element) node;
                String newBaseString = baseString + "." + basename;
                newBaseString = newBaseString.replaceFirst(".config.", "");
                String text = handleDoc(element, newMap, newBaseString);
                configMap.getConfigTreeMap().put(text, newMap);
            }
        }
        System.out.println("keys " + configMap.getConfigTreeMap().keySet());
        return mytext;        
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

    public List<Market> getMarkets() throws JsonParseException, JsonMappingException, IOException {
        String markets = IclijXMLConfig.getConfigXML().getString("markets.marketlist");
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(markets, new TypeReference<List<Market>>(){});
    }

    public static List<Market> getMarkets(IclijConfig config) throws JsonParseException, JsonMappingException, IOException {
        List<Market> retList = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        ConfigTreeMap map = config.getConfigTreeMap().search("markets.marketlist");
        for (Entry<String, ConfigTreeMap> entry : map.getConfigTreeMap().entrySet()) {
            String text = entry.getValue().getName();
            String text2 = (String) config.getConfigValueMap().get(text);
            Market market = mapper.readValue(text2, new TypeReference<Market>(){});
            retList.add(market);
        }
        return retList;
    }

    public List<TradeMarket> getTradeMarkets() throws JsonParseException, JsonMappingException, IOException {
        String markets = IclijXMLConfig.getConfigXML().getString("trademarkets.trademarket");
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(markets, new TypeReference<List<TradeMarket>>(){});
    }    

    public static List<TradeMarket> getTradeMarkets(IclijConfig config) throws JsonParseException, JsonMappingException, IOException {
        List<TradeMarket> retList = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        ConfigTreeMap map = config.getConfigTreeMap().search("markets.trademarkets");
        System.out.println(config.getConfigValueMap().keySet());
        for (Entry<String, ConfigTreeMap> entry : map.getConfigTreeMap().entrySet()) {
            ConfigTreeMap value = entry.getValue();
            String text = entry.getValue().getName();
            String text2 = (String) config.getConfigValueMap().get(text);
            Map<String, ConfigTreeMap> aMap = entry.getValue().getConfigTreeMap();
            TradeMarket market = mapper.readValue(text2, new TypeReference<TradeMarket>(){});
            retList.add(market);
        }
        return retList;
    }    

}
