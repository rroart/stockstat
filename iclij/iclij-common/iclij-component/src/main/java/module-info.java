/**
 * 
 */
/**
 * @author roart
 *
 */
module iclij.common.component {
    exports roart.iclij.component;
    exports roart.iclij.component.factory;
	requires org.apache.commons.lang3;
	requires org.slf4j;
	requires common.cache;
	requires common.model;
	requires common.util;
	requires common.io;
	requires common.constants;
	requires common.filesystem.client;
	requires iclij.common.componentdata;
	requires iclij.common.constants;
	requires iclij.common.model;
	requires iclij.common.config;
	requires iclij.common.util;
    requires iclij.common.verifyprofit;
    requires iclij.common.service;
    requires evolution.model;
    requires evolution.gene;
    requires evolution.algorithm;
    requires evolution.config;
    requires iclij.evolutionmarketfilter;
    requires iclij.evolutioniclijconfigmap;
    requires iclij.evolutionchromosome;
    requires iclij.common.simulate;
    requires iclij.common.filter;
	requires common.config;
	requires guava;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
	requires evolution.fitness;
    requires curator.client;
    requires curator.framework;
    requires spring.beans;
    requires dbdao;
    requires stockstat.shadow.zookeeper;
}
