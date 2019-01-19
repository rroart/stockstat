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
    exports roart.evolution.species;

    requires evolution.config;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires slf4j.api;
}