/**
 * 
 */
/**
 * @author roart
 *
 */
module iclij.evolve {
    requires slf4j.api;
    requires iclij.common.service;
    requires common.communication.model;
    requires iclij.common.config;
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
    requires spring.context;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires com.fasterxml.jackson.core;
}
