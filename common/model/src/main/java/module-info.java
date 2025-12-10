/**
 * 
 */
/**
 * @author roart
 *
 */
module common.model {
    exports roart.common.model;
    exports roart.common.model.util;
    exports roart.common.pipeline.model;
    exports roart.common.pipeline.data;
    exports roart.result.model;
    exports roart.service.model;
    exports roart.iclij.model;
    exports roart.simulate.model;
    exports roart.evolution.chromosome;
    exports roart.evolution.chromosome.impl;
    exports roart.evolution.iclijconfigmap.genetics.gene.impl;
    exports roart.iclij.evolution.chromosome.impl;
    exports roart.iclij.evolution.marketfilter.chromosome.impl;
    exports roart.evolution.species;
    exports roart.evolution.fitness;
    exports roart.evolution.iclijconfigmap.common.gene.impl;
    
    requires common.constants;
    requires common.util;
    requires common.config;
    requires iclij.common.config;
    requires evolution.config;
    requires evolution.gene;
    requires com.fasterxml.jackson.annotation;
    requires tools.jackson.core;
    requires tools.jackson.databind;
    requires org.slf4j;
    requires org.apache.commons.lang3;
}
