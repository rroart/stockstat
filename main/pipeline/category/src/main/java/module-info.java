/**
 * 
 */
/**
 * @author roart
 *
 */
module pipeline.category {
    exports roart.category.impl;

    requires common.constants;
    requires pipeline.indicator;
    requires pipeline.model;
    requires pipeline.predictor;
    requires slf4j.api;
    requires stockutil;
    requires model;
    requires common.model;
    requires common.config;
}