/**
 * 
 */
/**
 * @author roart
 *
 */
module evolution.chromosome {
    exports roart.evolution.chromosome.impl;

    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires common.config;
    requires iclij.common.config;
    requires common.constants;
    requires evolution.model;
    requires org.slf4j;
    requires evolution.gene;
    requires model;
    requires aggregate;
    requires pipeline.model;
    requires common.model;
    requires pipeline.predictor;
    requires com.fasterxml.jackson.annotation;
}
