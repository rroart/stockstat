package roart.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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

import roart.common.config.ConfigConstants;
import roart.common.config.ConfigTreeMap;
import roart.common.config.MyConfig;
import roart.common.constants.Constants;

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
        return configInstance;
    }

    private static Configuration config = null;
    private static XMLConfiguration configxml = null;

    public MyXMLConfig() {
        try {
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
        configInstance.setConfigTreeMap(new ConfigTreeMap());
        configInstance.setConfigValueMap(new HashMap<String, Object>());
        ConfigConstantMaps.makeDefaultMap();
        ConfigConstantMaps.makeTextMap();
        ConfigConstantMaps.makeTypeMap();
        configInstance.setDeflt(ConfigConstantMaps.deflt);
        configInstance.setType(ConfigConstantMaps.map);
        configInstance.setText(ConfigConstantMaps.text);
        if (configxml != null) {
            printout();
            doc = configxml.getDocument();
            if (doc != null) {
                handleDoc(doc.getDocumentElement(), configInstance.getConfigTreeMap(), "");
            }
            Iterator<String> iter = configxml.getKeys();
            while(iter.hasNext()) {
                String s = iter.next();
                Object o = null;
                String text = s;
                Class myclass = ConfigConstantMaps.map.get(text);

                if (myclass == null) {
                    log.info("Unknown {}", text);
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
                    log.info("unknown {}", myclass.getName());
                }
                configInstance.getConfigValueMap().put(s, o);
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
