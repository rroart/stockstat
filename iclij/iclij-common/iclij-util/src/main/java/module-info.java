/**
 * 
 */
/**
 * @author roart
 *
 */
module iclij.common.util {
    exports roart.db;
    exports roart.iclij.util;
    exports roart.iclij.factory.actioncomponentconfig;
    
    requires common.model;
    requires common.util;
    requires iclij.common.config;
    requires iclij.common.model;
    requires iclij.common.service;
    requires db;
    requires common.config;
    requires org.apache.commons.lang3;
    requires slf4j.api;
    requires common.constants;
    requires iclij.common.constants;
}
