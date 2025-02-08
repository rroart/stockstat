/**
 * 
 */
/**
 * @author roart
 *
 */
module iclij.common.componentdata {
    exports roart.component.model;
    exports roart.component.util;

    requires common.model;
    requires common.util;
    requires common.config;
    requires iclij.common.config;
    requires iclij.common.constants;
    requires iclij.common.model;
    requires iclij.common.service;
    requires org.apache.commons.lang3;
    requires org.slf4j;
    requires common.constants;
    requires common.inmemory.model;
    requires common.inmemory.factory;
    requires common.webflux;
    requires common.filesystem.client;
}

