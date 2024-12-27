/**
 * 
 */
/**
 * @author roart
 *
 */
module evolution.model {
    exports roart.evolution.fitness;
    exports roart.evolution.chromosome;
    exports roart.evolution.chromosome.impl;
    exports roart.evolution.species;
    exports roart.iclij.evolution.chromosome.impl;
    exports roart.iclij.evolution.marketfilter.chromosome.impl;
    
    requires common.config;
    requires common.util;
    requires iclij.common.config;
    requires evolution.config;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires org.slf4j;
    requires evolution.gene;
}
