/**
 * 
 */
/**
 * @author roart
 *
 */
module iclij.evolutioniclijconfigmap {
    exports roart.evolution.iclijconfigmap.jenetics.gene.impl;
    requires iclij.common.config;
    requires evolution.gene;
    requires common.model;
    requires common.util;
    requires io.jenetics.base;
    requires tools.jackson.core;
    requires tools.jackson.databind;
    requires org.slf4j;
    requires common.constants;
    requires iclij.common.model;
    requires common.config;
}
