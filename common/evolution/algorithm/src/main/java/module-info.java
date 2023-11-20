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

    requires common.constants;
    requires common.util;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires evolution.config;
    requires evolution.model;
    requires myexecutor;
    requires org.apache.commons.lang3;
    requires org.slf4j;
}
