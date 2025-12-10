/**
 * 
 */
/**
 * @author roart
 *
 */
module iclij.evolutionfitness {
    exports roart.iclij.evolution.fitness.impl;
	requires iclij.evolutionmarketfilter;
	requires iclij.common.component;
	requires iclij.common.componentdata;
	requires iclij.common.model;
	requires iclij.common.config;
	requires iclij.common.filter;
	requires iclij.common.service;
	requires iclij.common.util;
	requires iclij.common.verifyprofit;
	requires iclij.evolutioniclijconfigmap;
	requires common.config;
	requires common.constants;
	requires org.slf4j;
	requires evolution.model;
	requires evolution.gene;
	requires tools.jackson.core;
	requires tools.jackson.databind;
	requires commons.math3;
	requires org.apache.commons.lang3;
	requires common.util;
	requires iclij.evolutionchromosome;
    requires io.jenetics.base;
    requires common.model;
    requires common.inmemory.model;
    requires common.io;
    requires common.inmemory.factory;
    requires pipeline.util;
}
