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
    requires tools.jackson.core;
    requires tools.jackson.databind;
    requires evolution.config;
    requires evolution.model;
    requires myexecutor;
    requires org.apache.commons.lang3;
    requires org.slf4j;
    requires common.model;
}
