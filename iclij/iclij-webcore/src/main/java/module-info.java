/**
 * 
 */
/**
 * @author roart
 *
 */
open module iclij.webcore {
    exports roart.webcore.util;
    
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
    requires common.io;
    requires commons.configuration2;
    requires commons.math3;
    requires iclij.common.componentdata;
    requires iclij.common.config;
    requires iclij.common.constants;
    requires iclij.common.filter;
    requires iclij.common.model;
    requires iclij.common.service;
    requires iclij.common.util;
    requires iclij.common.verifyprofit;
    requires iclij.evolutionmarketfilter;
    requires java.xml;
    requires spring.beans;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.cloud.commons;
    requires spring.context;
    requires spring.plugin.core;
    requires spring.web;
    requires myexecutor;
    requires evolution.model;
    requires evolution.fitness;
    requires evolution.gene;
    requires evolution.algorithm;
    requires evolution.config;
    requires org.apache.commons.lang3;
    requires io.jenetics.base;
    requires org.slf4j;
    requires dbdao;
    requires spring.data.jdbc;
    requires common.communication.factory;
    requires common.inmemory.factory;
    requires common.webflux;
    requires common.filesystem.client;
    requires curator.client;
    requires curator.framework;
    //requires springfox.core;
    //requires springfox.spi;
    //requires springfox.spring.web;
    //requires springfox.swagger2;
}
