/**
 * 
 */
/**
 * @author roart
 *
 */
module iclij.core {
    exports roart.controller;
    exports roart.action;
    
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
    requires iclij.common.component;
    requires iclij.common.config;
    requires iclij.common.constants;
    requires iclij.common.model;
    requires iclij.common.service;
    requires iclij.common.util;
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
    requires org.apache.commons.lang3;
    requires io.jenetics.base;
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
	requires iclij.evolutioniclijconfigmap;
	requires iclij.evolutionfitness;
		requires iclij.common.filter;
	requires iclij.common.verifyprofit;
	requires evolution.model;
	requires evolution.gene;
	requires evolution.config;
	requires iclij.evolutionchromosome;
	requires iclij.evolutionevolve;
    requires spring.data.jdbc;
    requires jakarta.cdi;
    requires jakarta.interceptor;
    requires dbdao;
    requires db;
    requires springdata;
    requires coredb.hibernate;
    requires coredb;
    requires coredb.spring;
}
