/**
 * 
 */
/**
 * @author roart
 *
 */
module swift {
    exports roart.filesystem.swift;

    requires common.config;
    requires common.constants;
    requires common.filesystem;
    requires common.model;
    requires common.inmemory.model;
    requires common.inmemory.factory;
    requires filesystem;
    requires commons.io;
    requires joss;
    requires org.slf4j;
    requires spring.boot.autoconfigure;
    requires spring.cloud.commons;
    requires spring.web;
    requires spring.boot;
    requires org.apache.commons.codec;
}
