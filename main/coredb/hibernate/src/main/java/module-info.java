/**
 * 
 */
/**
 * @author roart
 *
 */
module coredb.hibernate {
    exports roart.db.hibernate;

    requires iclij.common.config;
    requires common.config;
    requires common.model;
    requires common.util;
    requires coredb;
    requires db;
    requires org.slf4j;
    requires java.sql;
    requires common.constants;
}
