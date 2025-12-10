/**
 * 
 */
/**
 * @author roart
 *
 */
module pipeline.common {
    exports roart.aggregatorindicator;

    requires tools.jackson.core;
    requires tools.jackson.databind;
    requires iclij.common.config;
    requires common.config;
    requires common.constants;
    requires common.util;
    requires commons.math3;
    requires model;
    requires org.apache.commons.lang3;
    requires pipeline.model;
    requires org.slf4j;
    requires stockutil;
    requires talib;
    requires pipeline.indicator;
    requires common.model;
    requires common.inmemory.model;
}
