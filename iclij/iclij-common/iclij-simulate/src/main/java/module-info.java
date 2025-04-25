/**
 * 
 */
/**
 * @author roart
 *
 */
module iclij.common.simulate {
    exports roart.simulate.util;
    
    requires iclij.common.constants;
    requires iclij.common.util;
    requires common.model;
    requires common.util;
    requires org.apache.commons.lang3;
    requires commons.math3;
    requires iclij.common.config;
    requires iclij.common.service;
    requires com.fasterxml.jackson.annotation;
    requires org.slf4j;
    requires common.cache;
    requires common.config;
    requires common.constants;
    requires iclij.common.model;
    requires iclij.common.componentdata;
    requires common.io;
    requires dbdao;
}
