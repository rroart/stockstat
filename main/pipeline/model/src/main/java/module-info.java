/**
 * 
 */
/**
 * @author roart
 *
 */
module pipeline.model {
    exports roart.indicator;
    exports roart.category;
    exports roart.pipeline.data;
    exports roart.pipeline;
    exports roart.pipeline.common.aggregate;
    exports roart.pipeline.common;
    exports roart.pipeline.common.predictor;

    requires common.config;
    requires common.constants;
    requires common.model;
    requires common.util;
    requires commons.lang;
    requires commons.math3;
    requires model;
    requires scala.library;
    requires slf4j.api;
    requires org.apache.commons.lang3;
}