/**
 * 
 */
/**
 * @author roart
 *
 */
module dbdao {
    exports roart.db.dao;
    exports roart.db.dao.util;

    requires common.config;
    requires coredb;
    requires coredb.spark;
    requires db;
    requires model;
    requires pipeline.model;
    requires slf4j.api;
    requires coredb.hibernate;
    requires stocketl;
    requires stockutil;
    requires common.constants;
    requires common.model;
    requires common.cache;
}
