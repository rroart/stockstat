/**
 * 
 */
/**
 * @author roart
 *
 */
module filesystem {
    exports roart.filesystem;

    requires common.config;
    requires common.constants;
    requires common.filesystem;
    requires curator.client;
    requires curator.framework;
    requires org.slf4j;
    requires spring.beans;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.cloud.commons;
    requires spring.context;
    requires spring.web;
    requires zookeeper;
    requires common.model;
    requires common.util;
    //requires common.zookeeper;
}
