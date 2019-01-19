/**
 * 
 */
/**
 * @author roart
 *
 */
module evolution.chromosome {
    exports roart.evaluation;

    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires common.config;
    requires common.constants;
    requires evolution.model;
    requires slf4j.api;
    requires evolution.gene;
    requires model;
    requires aggregate;
    requires pipeline.model;
}
