/**
 * 
 */
/**
 * @author roart
 *
 */
module pipeline.predictor {
    exports roart.predictor.impl;

    requires common.constants;
    requires commons.math3;
    requires ml.common;
    requires mldao;
    requires org.apache.commons.lang3;
    requires pipeline.model;
    requires slf4j.api;
    requires stockutil;
    requires common.config;
    requires common.util;
    requires model;
    requires common.model;
    requires pipeline.indicator;
    requires com.fasterxml.jackson.databind;
}