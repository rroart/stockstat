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
    requires org.slf4j;
    requires coredb.hibernate;
    requires stockutil;
    requires common.constants;
    requires common.model;
    requires common.cache;
    requires common.util;
}
