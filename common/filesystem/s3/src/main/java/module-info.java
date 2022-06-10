/**
 * 
 */
/**
 * @author roart
 *
 */
module s3 {
    exports roart.filesystem.s3;

    requires common.config;
    requires common.constants;
    requires common.filesystem;
    requires common.model;
    requires common.inmemory.model;
    requires common.inmemory.factory;
    requires filesystem;
    //requires commons.io;
    requires org.slf4j;
    requires spring.boot.autoconfigure;
    requires spring.cloud.commons;
    requires spring.web;
    requires spring.boot;
    requires org.apache.commons.codec;
    requires common.util;
    requires software.amazon.awssdk.services.s3;
    requires software.amazon.awssdk.auth;
    requires software.amazon.awssdk.regions;
    requires software.amazon.awssdk.core;
}
