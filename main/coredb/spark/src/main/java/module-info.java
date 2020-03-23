/**
 * 
 */
/**
 * @author roart
 *
 */
module coredb.spark {
    exports roart.db.spark;

    requires common.config;
    requires common.constants;
    requires common.model;
    requires common.util;
    requires commons.lang;
    requires coredb;
    requires model;
    requires pipeline.model;
    requires scala.library;
    requires slf4j.api;
    requires spark;
    requires stockstat.shadow.spark;
}
