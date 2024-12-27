/**
 * 
 */
/**
 * @author roart
 *
 */
module evolution.gene {
    exports roart.gene;
    exports roart.gene.impl;
    exports roart.gene.ml.impl;
    exports roart.evolution.marketfilter.genetics.gene.impl;
    exports roart.evolution.marketfilter.common.gene.impl;
    
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires iclij.common.config;
    requires common.config;
    requires common.util;
    requires org.slf4j;
    requires common.constants;
    requires com.fasterxml.jackson.annotation;
}
