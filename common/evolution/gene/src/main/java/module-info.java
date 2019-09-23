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
    requires common.config;
    requires jackson.annotations;
    requires evolution.model;
    requires common.util;
    requires slf4j.api;
    requires common.constants;
}
