/**
 * 
 */
/**
 * @author roart
 *
 */
module iclij.common.util {
    exports roart.iclij.util;
    exports roart.iclij.factory.actioncomponentconfig;
    
    requires common.util;
    requires common.cache;
    requires iclij.common.config;
    requires iclij.common.model;
    requires common.config;
    requires org.apache.commons.lang3;
    requires org.slf4j;
    requires common.constants;
    requires iclij.common.constants;
    requires spring.beans;
    requires spring.context;
}
