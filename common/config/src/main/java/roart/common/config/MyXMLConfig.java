package roart.common.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;

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

import org.springframework.context.annotation.Configuration;

import roart.common.constants.Constants;

@Configuration
public class MyXMLConfig {

    protected static Logger log = LoggerFactory.getLogger(MyXMLConfig.class);

    protected static MyXMLConfig instance = null;

    public static MyXMLConfig instance(ConfigMaps configMaps) {
        if (instance == null) {
            instance = new MyXMLConfig(configMaps);
        }
        return instance;
    }

    protected static MyConfig configInstance = null;

    public static MyConfig getConfigInstance(ConfigMaps configMaps) {
        if (configInstance == null) {
            //configInstance = new MyConfig();
            if (instance == null) { 
                instance(configMaps);
            }
        }
        return configInstance;
    }

    private static org.apache.commons.configuration2.Configuration config = null;
    private static XMLConfiguration configxml = null;

    private ConfigMaps configMaps;

    public MyXMLConfig() {
    }
    
    public MyXMLConfig(ConfigMaps configMaps) {
        this.configMaps = configMaps;
        getConfigInstance(configMaps);
        try {
            String configFile = System.getProperty("config");
            if (configFile == null) {
                configFile = ConfigConstants.CONFIGFILE;
            }
            String f2 = System.getenv("s");
            Parameters params = new Parameters();
            FileBasedConfigurationBuilder<XMLConfiguration> fileBuilder =
                    new FileBasedConfigurationBuilder<>(XMLConfiguration.class)
                    .configure(params.fileBased().setFileName("../conf/" + configFile));
            InputStream stream = new FileInputStream(new File("../conf/" + configFile));         
            configxml = fileBuilder.getConfiguration();
            configxml.read(stream);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            configxml = null;
        }
        Document doc = null;
        configInstance.getConfigData().setConfigTreeMap(new ConfigTreeMap());
        configInstance.getConfigData().setConfigValueMap(new HashMap<String, Object>());
        configInstance.getConfigData().setConfigMaps(configMaps);
        /*
        ConfigConstantMaps.makeDefaultMap();
        ConfigConstantMaps.makeTextMap();
        ConfigConstantMaps.makeRangeMap();
        ConfigConstantMaps.makeTypeMap();
        configInstance.setDeflt(ConfigConstantMaps.deflt);
        configInstance.setType(ConfigConstantMaps.map);
        configInstance.setText(ConfigConstantMaps.text);
        configInstance.setRange(ConfigConstantMaps.range);
        configInstance.getDeflt().putAll(ConfigConstantMaps.deflt2);
        configInstance.getType().putAll(ConfigConstantMaps.map2);
        configInstance.getText().putAll(ConfigConstantMaps.text2);
        configInstance.getRange().putAll(ConfigConstantMaps.range2);
        */
        if (configxml != null) {
            printout();
            doc = configxml.getDocument();
            if (doc != null) {
                handleDoc(doc.getDocumentElement(), configInstance.getConfigData().getConfigTreeMap(), "");
            }
            Iterator<String> iter = configxml.getKeys();
            while(iter.hasNext()) {
                String s = iter.next();
                Object o = null;
                String text = s;
                Class myclass = configMaps.map.get(text);

                if (myclass == null) {
                    log.info("Unknown {}", text);
                    continue;
                }
                String str = configxml.getString(s);
                switch (myclass.getName()) {
                case "java.lang.String":
                    o = configxml.getString(s);
                    break;
                case "java.lang.Integer":
                    o = !str.isEmpty() ? configxml.getInt(s) : null;
                    break;
                case "java.lang.Double":
                    o = !str.isEmpty() ? configxml.getDouble(s) : null;
                    break;
                case "java.lang.Boolean":
                    o = !str.isEmpty() ? configxml.getBoolean(s) : null;
                    break;
                default:
                    log.info("unknown {}", myclass.getName());
                }
                configInstance.getConfigData().getConfigValueMap().put(s, o);
            }
        }
        Set<String> setKeys = configInstance.getConfigData().getConfigValueMap().keySet();
        Set<String> dfltKeys = new HashSet<>(configMaps.deflt.keySet());
        dfltKeys.removeAll(setKeys);
        System.out.println("keys to set " + dfltKeys);
        for (String key : dfltKeys) {
            ConfigTreeMap map = configInstance.getConfigData().getConfigTreeMap();
            ConfigTreeMap.insert(map.getConfigTreeMap(), key, key, "", configMaps.deflt);
            Object object = configMaps.deflt.get(key);
            if (configInstance.getConfigData().getConfigValueMap().get(key) == null) {
                configInstance.getConfigData().getConfigValueMap().put(key, object);
            }
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

    private void print(ConfigTreeMap map2, int indent) {
        String space = "      ";
        log.info("map2 {} {} {}", space.substring(0, indent), map2.getName(), map2.getEnabled());
        Map<String, ConfigTreeMap> map3 = map2.getConfigTreeMap();
        for (Entry<String, ConfigTreeMap> entry : map3.entrySet()) {
            print(entry.getValue(), indent + 1);
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
        configMap.setName(baseString + "." + name);
        configMap.setName(configMap.getName().replaceFirst(".config.", ""));
        configMap.setEnabled(enabled);
        configMap.setConfigTreeMap(new HashMap<String, ConfigTreeMap>());
        for (int i = 0; i < elements.getLength(); i++) {
            Node node = elements.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                ConfigTreeMap newMap = new ConfigTreeMap();
                Element element = (Element) node;
                String newBaseString = baseString + "." + basename;
                newBaseString = newBaseString.replaceFirst(".config.", "");
                handleDoc(element, newMap, newBaseString);
                String text = element.getNodeName();
                configMap.getConfigTreeMap().put(text, newMap);
            }
        }

    }

}
