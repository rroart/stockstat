/**
 * 
 */
/**
 * @author roart
 *
 */
module iclij.common.service {
    exports roart.iclij.service;
    exports roart.iclij.service.util;
    exports roart.iclij.model.component;

    requires iclij.common.config;
    requires common.config;
    requires common.util;
    requires org.slf4j;
    requires common.model;
    requires common.constants;
    requires common.communication.factory;
    requires org.apache.commons.lang3;
    requires iclij.common.model;
    requires spring.web;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires common.communication.model;
    requires common.cache;
    requires common.inmemory.factory;
    requires common.inmemory.model;
    requires common.webflux;
    requires common.queueutil;
    requires common.filesystem.client;
    requires curator.client;
    requires curator.framework;
    requires pipeline.model;
    requires model;
    requires spring.beans;
    requires spring.context;
    requires common.queue;
    requires dbdao;
}
