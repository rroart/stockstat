/**
 * 
 */
/**
 * @author roart
 *
 */
module machinelearningservice {
    exports roart.machinelearning.service;
    exports roart.machinelearning.service.evolution;

    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires common.config;
    requires common.controller;
    //requires common.constants;
    requires common.eureka;
    requires common.model;
    requires common.service;
    requires common.cache;
    requires common.filesystem.client;
    requires common.io;
    //requires common.util;
    //requires commons.lang;
    requires commons.math3;
    requires db;
    //requires guava;
    requires java.desktop;
    requires jcommon;
    requires jfreechart;
    requires org.apache.commons.lang3;
    requires scala.library;
    requires ta.lib;
    requires model;
    requires common.constants;
    requires ml.common;
    //requires ml.spark;
    requires ml.tensorflow;
    requires pipeline.model;
    requires pipeline.common;
    requires common.util;
    requires spring.boot;
    requires coredb;
    //requires coredb.spark;
    requires coredb.hibernate;
    requires evolution.model;
    requires myexecutor;
    requires evolution.config;
    requires evolution.algorithm;
    requires evolution.gene;
    requires stockutil;
    requires talib;
    requires aggregate;
    requires dbdao;
    requires evolution.fitness;
    requires mldao;
    requires evolution.chromosome;
    requires pipeline.category;
    requires pipeline.indicator;
    requires pipeline.predictor;
    requires stocketl;
    requires common.communication.model;
    requires common.communication.factory;
    requires org.slf4j;
    requires jol.core;
    requires curator.client;
    requires curator.framework;
    requires redis.clients.jedis;
    requires spring.jdbc;
    requires java.sql;
    requires jakarta.cdi;
    requires jakarta.interceptor;
    requires spring.data.jdbc;
    requires springdata;
    requires iclij.common.config;
    requires iclij.common.service;
    requires ml.spark;
    requires common.webflux;
    requires common.inmemory.factory;
    requires common.inmemory.model;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires common.queue;
    requires pipeline.util;
}
