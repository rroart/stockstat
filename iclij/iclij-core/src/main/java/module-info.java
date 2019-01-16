/**
 * 
 */
/**
 * @author roart
 *
 */
module iclij.core {
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires common.config;
    requires common.constants;
    requires common.eureka;
    requires common.model;
    requires common.service;
    requires common.util;
    requires commons;
    requires commons.configuration2;
    requires commons.math3;
    requires deductive;
    requires eureka.client;
    requires guava;
    requires iclij.common.config;
    requires iclij.common.constants;
    requires iclij.common.model;
    requires iclij.common.service;
    requires java.xml;
    requires javax.servlet.api;
    requires math;
    requires pl;
    requires slf4j.api;
    requires spring.beans;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.cloud.commons;
    requires spring.context;
    requires spring.plugin.core;
    requires spring.web;
    requires myexecutor;
    requires evolution.model;
    //requires springfox.core;
    //requires springfox.spi;
    //requires springfox.spring.web;
    //requires springfox.swagger2;
}