/**
 * 
 */
/**
 * @author roart
 *
 */
module evolution.algorithm {
    exports roart.evolution.algorithm;
    exports roart.evolution.algorithm.impl;

    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires evolution.config;
    requires evolution.model;
    requires myexecutor;
    requires slf4j.api;
}