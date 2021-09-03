/**
 * 
 */
/**
 * @author roart
 *
 */
module iclij.evolutionchromosome {
    exports roart.iclij.evolution.marketfilter.chromosome.impl;
    exports roart.iclij.evolution.chromosome.impl;
    exports roart.iclij.evolution.chromosome.winner;
    requires iclij.common.config;
    requires iclij.common.componentdata;
    requires iclij.evolutionmarketfilter;
    requires iclij.evolutioniclijconfigmap;
    requires evolution.gene;
    requires evolution.model;
    requires common.util;
    requires io.jenetics.base;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires slf4j.api;
    requires common.constants;
    requires iclij.common.model;
    requires common.config;
    requires org.apache.commons.lang3;
}
