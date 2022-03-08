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
    //requires aws.java.sdk.core;
    //requires aws.java.sdk.s3;
    requires stockstat.shadow.s3;
    requires common.util;
    //requires software.amazon.awssdk.protocols.core;
    //requires software.amazon.awssdk.services.s3;
}
