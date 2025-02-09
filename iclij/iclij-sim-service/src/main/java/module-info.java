/**
 * 
 */
/**
 * @author roart
 *
 */
open module iclij.sim.service {
    exports roart.sim;
    
    requires iclij.common.service;
    requires common.communication.model;
    requires iclij.common.config;
    requires iclij.common.simulate;
    requires common.constants;
    requires com.fasterxml.jackson.databind;
    requires common.communication.factory;
    requires org.apache.commons.lang3;
    requires common.util;
    requires spring.web;
    requires spring.boot.autoconfigure;
    requires spring.boot;
    requires spring.cloud.commons;
    requires spring.beans;
    requires myexecutor;
    requires common.cache;
    requires common.controller;
    requires common.queue;
    requires spring.context;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires com.fasterxml.jackson.core;
    requires evolution.model;
    requires iclij.evolutioniclijconfigmap;
    requires iclij.common.util;
    //requires guava;
    requires iclij.common.constants;
    requires common.inmemory.factory;
    requires common.inmemory.model;
    requires common.filesystem.client;
    requires iclij.common.model;
    requires db;
    requires commons.math3;
    //requires com.google.common;
    requires org.slf4j;
    requires common.config;
    requires curator.client;
    requires curator.framework;
    requires dbdao;
    requires common.model;
    requires spring.data.jdbc;
    requires common.queueutil;
}
