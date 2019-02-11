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

    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires common.config;
    requires jackson.annotations;
    requires evolution.model;
}
