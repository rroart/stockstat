/**
 * 
 */
/**
 * @author roart
 *
 */
module iclij.evolutioniclijconfigmap {
    exports roart.evolution.iclijconfigmap.genetics.gene.impl;
    exports roart.evolution.iclijconfigmap.jenetics.gene.impl;
    exports roart.evolution.iclijconfigmap.common.gene.impl;
    requires iclij.common.config;
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
}
