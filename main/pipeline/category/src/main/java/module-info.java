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
    requires org.slf4j;
    requires stockutil;
    requires model;
    requires common.model;
    requires iclij.common.config;
    requires common.config;
    requires common.util;
}
