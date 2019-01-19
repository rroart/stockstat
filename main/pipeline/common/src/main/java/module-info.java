/**
 * 
 */
/**
 * @author roart
 *
 */
module pipeline.common {
    exports roart.aggregatorindicator;

    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires common.config;
    requires common.constants;
    requires common.util;
    requires commons.math3;
    requires dbdao;
    requires model;
    requires org.apache.commons.lang3;
    requires pipeline.model;
    requires slf4j.api;
    requires stockutil;
    requires talib;
    requires pipeline.indicator;
}