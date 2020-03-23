/**
 * 
 */
/**
 * @author roart
 *
 */
module coredb {
    exports roart.db.common;

    requires common.config;
    requires common.model;
    requires model;
    requires slf4j.api;
    requires pipeline.model;
}
