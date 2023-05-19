/**
 * 
 */
/**
 * @author roart
 *
 */
module common.filesystem.client {
    exports roart.filesystem;

    requires iclij.common.config;
    requires common.config;
    requires common.constants;
    requires common.filesystem;
    requires common.model;
    requires common.util;
    requires common.webflux;
    requires common.inmemory.factory;
    requires common.inmemory.model;
    requires com.fasterxml.jackson.annotation;
    requires zookeeper;
    requires curator.client;
    requires curator.framework;
    requires curator.recipes;
    requires org.slf4j;
}
