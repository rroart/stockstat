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

    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires iclij.common.config;
    requires common.config;
    requires evolution.model;
    requires common.util;
    requires org.slf4j;
    requires common.constants;
    requires com.fasterxml.jackson.annotation;
}
