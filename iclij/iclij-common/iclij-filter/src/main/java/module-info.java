/**
 * 
 */
/**
 * @author roart
 *
 */
module iclij.common.filter {
    exports roart.iclij.filter;
    
    requires common.model;
    requires common.util;
    requires iclij.common.config;
    requires iclij.common.model;
    requires iclij.common.service;
    requires iclij.common.util;
    requires db;
    requires common.config;
    requires org.apache.commons.lang3;
    requires org.slf4j;
    requires common.constants;
    requires iclij.common.constants;
}
