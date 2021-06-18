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
    requires iclij.evolutionchromosome;
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
    requires java.base;
    requires evolution.model;
    requires evolution.chromosome;
    requires common.config;
    requires iclij.common.model;
    requires iclij.common.util;
    requires guava;
    requires common.inmemory.factory;
    requires common.inmemory.model;
    requires evolution.gene;
    requires db;
}
