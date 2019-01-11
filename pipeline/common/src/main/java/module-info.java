/**
 * 
 */
/**
 * @author roart
 *
 */
module pipeline {
    exports roart.pipeline.common.aggregate;
    exports roart.pipeline.common;
    exports roart.pipeline.common.predictor;

    requires common.config;
    requires common.constants;
    requires common.model;
    requires model;
    requires scala.library;
    requires slf4j.api;
}