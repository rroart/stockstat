/**
 * 
 */
/**
 * @author roart
 *
 */
module pipeline.indicator {
    exports roart.indicator.impl;
    exports roart.indicator.util;
    exports roart.pipeline.impl;
    
    requires common.config;
    requires common.constants;
    requires commons.math3;
    requires dbdao;
    requires model;
    requires pipeline.model;
    requires org.apache.commons.lang3;
    requires slf4j.api;
    requires talib;
    requires coredb;
    requires stockutil;
    requires ml.common;
    requires com.fasterxml.jackson.databind;
    requires common.util;
}