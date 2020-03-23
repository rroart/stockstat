/**
 * 
 */
/**
 * @author roart
 *
 */
module coredb.hibernate {
    exports roart.db.hibernate;

    requires common.config;
    requires common.model;
    requires common.util;
    requires coredb;
    requires db;
    requires model;
    requires slf4j.api;
    requires pipeline.model;
}
