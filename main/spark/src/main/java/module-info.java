/**
 * 
 */
/**
 * @author roart
 *
 */
open module core {
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
    //requires common.util;
    requires commons.lang;
    requires commons.math3;
    requires db;
    //requires guava;
    requires java.desktop;
    requires jcommon;
    requires jfreechart;
    //requires mockito.all;
    requires org.apache.commons.lang3;
    requires scala.library;
    //requires scala.reflect;
    requires spring.beans;
    requires spring.boot.autoconfigure;
    requires spring.cloud.commons;
    requires spring.context;
    //requires spring.web;
    //requires spring.webmvc;
    requires model;
    requires common.constants;
    requires ml.common;
    requires ml.spark;
    requires pipeline.model;
    requires pipeline.common;
    requires common.util;
    requires spring.boot;
    requires coredb;
    requires coredb.spark;
    requires org.slf4j;
    //equires curator.client;
    //requires curator.framework;
    //requires redis.clients.jedis;
    //requires jakarta.cdi;
    //requires jakarta.interceptor;
    requires iclij.common.config;
    requires iclij.common.service;
}
