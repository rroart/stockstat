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
    requires common.controller;
    requires common.eureka;
    requires common.model;
    requires common.service;
    requires common.util;
    requires common.cache;
    requires commons.math3;
    //requires guava;
    requires iclij.common.componentdata;
    requires iclij.common.config;
    requires iclij.common.constants;
    requires iclij.common.filter;
    requires iclij.common.model;
    requires iclij.common.service;
    requires iclij.common.util;
    requires iclij.common.verifyprofit;
    requires iclij.common.simulate;
    requires java.xml;
    //requires javax.servlet.api;
    //requires org.slf4j;
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
    requires iclij.evolutionmarketfilter;
    requires iclij.evolutioniclijconfigmap;
    requires iclij.evolutionchromosome;
    requires db;
    requires common.communication.model;
    requires common.communication.factory;
    //requires com.google.common;
    requires org.slf4j;
    requires spring.core;
    //requires springfox.core;
    //requires springfox.spi;
    //requires springfox.spring.web;
    //requires springfox.swagger2;
    //tweety:
    //requires commons;
    //requires deductive;
    //requires math;
    //?requires pl;
    requires common.inmemory.factory;
    requires common.inmemory.model;
}
