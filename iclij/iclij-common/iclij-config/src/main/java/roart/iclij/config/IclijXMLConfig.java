package roart.iclij.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

//import org.apache.commons.configuration2.Configuration;
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

import org.springframework.context.annotation.Configuration;

import roart.common.config.ConfigConstantMaps;
import roart.common.config.ConfigMaps;
import roart.common.config.ConfigTreeMap;
import roart.common.config.Extra;
import roart.common.config.MarketStockExpression;
import roart.common.constants.Constants;
import roart.common.util.JsonUtil;

public class IclijXMLConfig {

    protected static Logger log = LoggerFactory.getLogger(IclijXMLConfig.class);

    protected static IclijXMLConfig instance = null;

    public static IclijXMLConfig instance(IclijConfig iclijConfig, ConfigMaps configMaps) {
        if (instance == null) {
            instance = new IclijXMLConfig(iclijConfig, configMaps);
        }
        return instance;
    }

    private static org.apache.commons.configuration2.Configuration config = null;
    private static XMLConfiguration configxml = null;

    private ConfigMaps configMaps;

    private IclijConfig configInstance;

    public IclijXMLConfig(IclijConfig configInstance, ConfigMaps configMaps) {
        log.error("confMapps" + configMaps);
        this.configMaps = configMaps;
        this.configInstance = configInstance;
        try {
            String configFile = System.getProperty("config");
            if (configFile == null) {
                return;
            }
            configFile = "../conf/" + configFile;
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
            System.exit(1);
        }
        Document doc = configxml.getDocument();
        configInstance.getConfigData().setConfigTreeMap(new ConfigTreeMap());
        configInstance.getConfigData().setConfigValueMap(new HashMap<String, Object>());
        configInstance.getConfigData().setConfigMaps(configMaps);
        /*
        IclijConfigConstantMaps.makeDefaultMap();
        IclijConfigConstantMaps.makeTextMap();
        IclijConfigConstantMaps.makeTypeMap();
        IclijConfigConstantMaps.makeConvertMap();
        IclijConfigConstantMaps.makeRangeMap();
        ConfigConstantMaps.makeDefaultMap();
        ConfigConstantMaps.makeTextMap();
        ConfigConstantMaps.makeRangeMap();
        ConfigConstantMaps.makeTypeMap();
        ConfigConstantMaps.deflt = IclijConfigConstantMaps.deflt;
        ConfigConstantMaps.map = IclijConfigConstantMaps.map;
        ConfigConstantMaps.text = IclijConfigConstantMaps.text;
        ConfigConstantMaps.range = IclijConfigConstantMaps.range;
        configInstance.setDeflt(IclijConfigConstantMaps.deflt);
        configInstance.setType(IclijConfigConstantMaps.map);
        configInstance.setText(IclijConfigConstantMaps.text);
        configInstance.setConv(IclijConfigConstantMaps.conv);
        configInstance.setRange(IclijConfigConstantMaps.range);
        configInstance.getDeflt().putAll(ConfigConstantMaps.deflt);
        configInstance.getType().putAll(ConfigConstantMaps.map);
        configInstance.getText().putAll(ConfigConstantMaps.text);
        */
        //configInstance.getConv().putAll(ConfigConstantMaps.conv);
        //configInstance.getRange().putAll(ConfigConstantMaps.range);
       if (configxml != null) {
            printout();
            doc = configxml.getDocument();
            if (doc != null) {
                handleDoc(doc.getDocumentElement(), configInstance.getConfigData().getConfigTreeMap(), "");
            }
            setValues(configMaps);
        }
        //((AbstractConfiguration) config).setDelimiterParsingDisabled(true);
    }

    private void setValues(ConfigMaps configMaps) {
        String root = configxml.getRootElementName();
        log.info("Root {}", root);
        List<HierarchicalConfiguration<ImmutableNode>> iter2 = configxml.childConfigurationsAt(".");
        for            (HierarchicalConfiguration<ImmutableNode> i : iter2 ) {
            log.info("Elemwnt {}", i.getRootElementName());
        }
        HierarchicalConfiguration<ImmutableNode> iter = configxml.configurationAt(".");
        log.info("Elem {}", iter.getRootElementName());
        //iter.c
        setValues(iter, "" /*root*/, configMaps);
        //iter.get
        //List<HierarchicalConfiguration<ImmutableNode>> iter3 = configxml.childConfigurationsAt(elem);
        Set<String> setKeys = configInstance.getConfigData().getConfigValueMap().keySet();
        Set<String> dfltKeys = configInstance.getConfigData().getConfigMaps().deflt.keySet();
        dfltKeys.removeAll(setKeys);
        log.info("keys to set {}", dfltKeys);
        for (String key : dfltKeys) {
            ConfigTreeMap map = configInstance.getConfigData().getConfigTreeMap();
            ConfigTreeMap.insert(map.getConfigTreeMap(), key, key, "", configMaps.deflt);
            Object object = configMaps.deflt.get(key);
            if (configInstance.getConfigData().getConfigValueMap().get(key) == null) {
                configInstance.getConfigData().getConfigValueMap().put(key, object);
            }
        }
        int jj = 0;
        try {
            getMarkets(configInstance);
        } catch (IOException e) {
            log.error(Constants.EXCEPTION, e);
            e.printStackTrace();
        }
        // then defalts
    }

    private void setValues(HierarchicalConfiguration<ImmutableNode> elem, String base, ConfigMaps configMaps) {
        log.info("base {}", base);
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
            //String fullnode = node;
            // node is for nodes with eventual children, need id=..., but no enable
            // node0 is for map get put
            log.info("base {}", node);
            String enable = (String) elem2.getProperty("[@enable]");
            if (enable != null) {
                //node = node + "[@enable]";
                //node0 = node0 + "[@enable]";
                //fullnode = node;
                key = "[@enable]";
            }
            log.info("enable {}", enable);
            String id = (String) elem2.getProperty("[@id]");
            if (id != null) {
                node0 = node0 + "[@id=" + id + "]";
                node = node + "[@id=" + id + "]";
                //fullnode = fullnode + "[@id=" + id + "]";
                key = key + "[@id=" + id + "]";
            }
            log.info("id {}", id);
            //Properties i = elem2.getProperties("[]");
            //System.out.println(i);
            //System.out.println("s " + s + " " + configxml.getString(s) + " " + configxml.getProperty(s));
            Object o = null;
            Iterator<String> grr = elem2.getKeys();
            while (grr.hasNext()) {
                String s0 = grr.next();
                log.info("s0 {}", s0);
            }
            String text = node;
            if (text.equals("findprofit.mlindicator.mlconfig")) {
                int jj = 0;
            }
            Class myclass = configMaps.map.get(node0);
            if (myclass == null) {
                myclass = (Class) configMaps.deflt.get(node0);
            }
            if (myclass == null) {
                myclass = configMaps.map.get(node);
            }
            if (myclass == null) {
                String node1 = node0.replaceFirst("\\[@id=[a-z0-9]*\\]", "[@id]");
                myclass = (Class) configMaps.map.get(node1);
            }
            if (myclass == null) {
                String node1 = node.replaceFirst("\\[@id=[a-z0-9]*\\]", "[@id]");
                myclass = (Class) configMaps.map.get(node1);
            }
            String s = "";
            if (myclass == null) {
                //System.out.println("Unknown " + text);
                //Object j = configxml.getList(String.class, s);
                //System.out.println(configxml.getProperties(s));
                //System.out.println(j);
                //o = configxml.getString(s);
                log.info("Unknown {}", text);
                //continue;
                String pri = (String) elem2.getProperty("[@priority]");
                if (pri != null) {
                    Integer priority = Integer.valueOf(pri);
                    configInstance.getConfigData().getConfigValueMap().put(node0 + "[@priority]", priority);
                    log.debug("Pri {} {}", node0, pri);
                }
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
                    log.info("unknown {}", myclass.getName());
                }
                log.info("Node value {} {}", node0, o);
                configInstance.getConfigData().getConfigValueMap().put(node0, o);
                String pri = (String) elem2.getProperty("[@priority]");
                if (pri != null) {
                    Integer priority = Integer.valueOf(pri);
                    configInstance.getConfigData().getConfigValueMap().put(node0 + "[@priority]", priority);
                    log.info("Pri {} {} ", node0, pri);
                }
            }
            setValues(elem2, node, configMaps);
        }
    }

    @Deprecated
    private void setValuesOld(ConfigMaps configMaps) {
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
            Class myclass = configMaps.map.get(text);

            if (myclass == null) {
                //System.out.println("Unknown " + text);
                Object j = configxml.getList(String.class, s);
                log.info("Properties {}", configxml.getProperties(s));
                log.info("List {}", j);
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
            configInstance.getConfigData().getConfigValueMap().put(s, o);
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
            configInstance.getConfigData().getConfigMaps().deflt.put(baseString + "." + name, String.class);
        }
        String attribute = documentElement.getAttribute("enable");
        NodeList elements = documentElement.getChildNodes();
        boolean leafNode = elements.getLength() == 0;
        Boolean enabled = null;
        if (attribute != null) {
            enabled = !attribute.equals("false");
            if (/*leafNode &&*/ !attribute.isEmpty()) {
                //name = name + "[@enable]";
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
                String newBaseString = baseString + "." + name;
                newBaseString = newBaseString.replaceFirst(".config.", "");
                String text = handleDoc(element, newMap, newBaseString);
                configMap.getConfigTreeMap().put(text, newMap);
            }
        }
        log.info("keys {}", configMap.getConfigTreeMap().keySet());
        /*
        Set<String> defKeys = IclijConfigConstantMaps.deflt.keySet();
        System.out.println("defkeys " + defKeys);
        Set<String> myDefKeys = new HashSet<>();
        String newBase = baseString + "." + name;
        newBase = newBase.replaceFirst(".config.", "");
        for (String key : defKeys) {
            if (!baseString.isBlank() && key.startsWith(newBase)) {
                String rest = key.substring(newBase.length());
                if (!rest.isBlank()) {
                    if (rest.startsWith(".")) {
                        rest = rest.substring(1);
                    }
                    int index = rest.indexOf('.');
                    if (index < 0) {
                        rest = rest.replaceFirst("\\[@enable\\]", "");
                        if (!rest.isBlank()) {
                            myDefKeys.add(rest);
                        }
                    }
                }
            }
        }
        System.out.println("keys2 " + myDefKeys);
        myDefKeys.removeAll(configMap.getConfigTreeMap().keySet());
        System.out.println("keys3 " + myDefKeys);
        if (!myDefKeys.isEmpty()) {
            int jj = 0;
        }
        */
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

    @Deprecated
    public String getString(String string) {
        return config.getString(string);
    }

    public String[] getStringArray(String string) {
        return config.getStringArray(string);
    }

    public Boolean getBoolean(String string) {
        return config.getBoolean(string);
    }

    @Deprecated
    public Integer getInteger(String string) {
        return config.getInt(string);
    }

    @Deprecated
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

    @Deprecated
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

    public List<MarketConfig> getMarkets() throws JsonParseException, JsonMappingException, IOException {
        String markets = IclijXMLConfig.getConfigXML().getString("markets.marketlist");
        return JsonUtil.convertnostrip(markets, new TypeReference<List<MarketConfig>>(){});
    }

    public static List<Market> getMarkets(IclijConfig config) throws JsonParseException, JsonMappingException, IOException {
        List<Market> retList = new ArrayList<>();
        ConfigTreeMap map = config.getConfigData().getConfigTreeMap().search("markets.marketlist");
        if (map == null) {
            return retList;
        }
        for (Entry<String, ConfigTreeMap> entry : map.getConfigTreeMap().entrySet()) {
            String text = entry.getValue().getName();
            ConfigTreeMap configMap = entry.getValue();
            Market market = new Market();

            MarketConfig marketConfig = getConfig(configMap, "config", MarketConfig.class, config);
            if (marketConfig == null) {
                int jj = 0;
                log.error("Empty market config");
                continue;
            }
            MarketFilter marketFilter = getConfig(configMap, "filter", MarketFilter.class, config);
            SimulateFilter simulateFilter = getConfig(configMap, "simulatefilter", SimulateFilter.class, config);
            MLConfigs mlConfigs = getConfig(configMap, "mlconfig", MLConfigs.class, config);
            SimulateInvestConfig simulate = getConfig(configMap, "simulate", SimulateInvestConfig.class, config);
            //MLConfigs defaultMlConfigs = getDefaultMlConfigs(config, mapper, text);
            //defaultMlConfigs.merge(mlConfigs);
            market.setConfig(marketConfig);
            market.setFilter(marketFilter);
            //market.setSimulateFilter(simulateFilter);
            market.setMlconfig(mlConfigs);
            market.setSimulate(simulate);
            //String text2 = (String) config.getConfigValueMap().get(text);
            //MarketConfig market = mapper.readValue(text2, new TypeReference<MarketConfig>(){});
            retList.add(market);
        }
        return retList;
    }

    public static List<Extra> getMarketImportants(IclijConfig config) throws JsonParseException, JsonMappingException, IOException {
        List<Extra> retList = new ArrayList<>();
        ConfigTreeMap map = config.getConfigData().getConfigTreeMap().search("markets.importants");
        if (map == null) {
            return retList;
        }
        for (Entry<String, ConfigTreeMap> entry : map.getConfigTreeMap().entrySet()) {
            String text = entry.getValue().getName();
            ConfigTreeMap configMap = entry.getValue();
            Market market = new Market();

            Extra marketConfig = getConfig(configMap, "config", Extra.class, config);
            if (marketConfig == null) {
                int jj = 0;
                log.error("Empty important config");
                continue;
            }
            //String text2 = (String) config.getConfigValueMap().get(text);
            //MarketConfig market = mapper.readValue(text2, new TypeReference<MarketConfig>(){});
            retList.add(marketConfig);
        }
        return retList;
    }

    public static List<SimulateFilter[]> getSimulate(IclijConfig config) throws JsonParseException, JsonMappingException, IOException {
        List<SimulateFilter[]> retList = new ArrayList<>();
        ConfigTreeMap map = config.getConfigData().getConfigTreeMap().search("markets.simulate");
        if (map == null) {
            //return retList;
        }
        String value = (String) config.getValueOrDefault(IclijConfigConstants.MARKETSSIMULATECONFIG);
        if (value != null) {
            SimulateFilter[] marketConfig = null;
            try {
                marketConfig = JsonUtil.convertnostrip(value, SimulateFilter[].class);
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
            retList.add(marketConfig);
        }
        if (true) {
            return retList;
        }
        for (Entry<String, ConfigTreeMap> entry : map.getConfigTreeMap().entrySet()) {
            String text = entry.getValue().getName();
            ConfigTreeMap configMap = entry.getValue();
            Market market = new Market();

            SimulateFilter[] marketConfig = getConfig(configMap, "config", SimulateFilter[].class, config);
            if (marketConfig == null) {
                int jj = 0;
                log.error("Empty important config");
                continue;
            }
            //String text2 = (String) config.getConfigValueMap().get(text);
            //MarketConfig market = mapper.readValue(text2, new TypeReference<MarketConfig>(){});
            retList.add(marketConfig);
        }
        return retList;
    }

    private static MLConfigs getDefaultMlConfigs(IclijConfig config, ObjectMapper mapper, String text) {
        text = text.replaceFirst("\\[@id=[a-z0-9]*\\]", "[@id]");
        String mlConfigsString = (String) config.getConfigData().getConfigMaps().deflt.get(text + ".mlconfig");
        try {
            return mapper.readValue(mlConfigsString, MLConfigs.class);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return null;
    }

    public static <T> T getConfig(ConfigTreeMap configMap, String key, Class<T> myclass, IclijConfig config) {
        ConfigTreeMap value = configMap.getConfigTreeMap().get(key);
        if (value != null) {
            String name = value.getName();
            String atext = (String) config.getConfigData().getConfigValueMap().get(name);
            try {
                return JsonUtil.convertnostrip(atext, myclass);
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
        }
        return null;
    }
    //?
    @Deprecated
    public List<MarketFilter> getFilterMarkets() throws JsonParseException, JsonMappingException, IOException {
        String markets = IclijXMLConfig.getConfigXML().getString("filtermarkets.filtermarket");
        return JsonUtil.convertnostrip(markets, new TypeReference<List<MarketFilter>>(){});
    }    

    public static List<MarketFilter> getFilterMarkets(IclijConfig config) throws JsonParseException, JsonMappingException, IOException {
        List<MarketFilter> retList = new ArrayList<>();
        ConfigTreeMap map = config.getConfigData().getConfigTreeMap().search("markets.filtermarkets");
        log.info("Keyset {}", config.getConfigData().getConfigValueMap().keySet());
        for (Entry<String, ConfigTreeMap> entry : map.getConfigTreeMap().entrySet()) {
            ConfigTreeMap value = entry.getValue();
            String text = entry.getValue().getName();
            String text2 = (String) config.getConfigData().getConfigValueMap().get(text);
            Map<String, ConfigTreeMap> aMap = entry.getValue().getConfigTreeMap();
            MarketFilter market = JsonUtil.convertnostrip(text2, new TypeReference<MarketFilter>(){});
            retList.add(market);
        }
        return retList;
    }    

}
