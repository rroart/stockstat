/**
 * 
 */
/**
 * @author roart
 *
 */
module local {
    exports roart.filesystem.local;

    requires common.config;
    requires common.constants;
    requires common.filesystem;
    requires common.model;
    requires commons.io;
    requires common.inmemory.model;
    requires common.inmemory.factory;
    requires filesystem;
    requires org.slf4j;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.cloud.commons;
    requires spring.context;
    requires spring.web;
    requires org.apache.commons.codec;
    requires common.util;
}
