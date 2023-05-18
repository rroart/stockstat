/**
 * 
 */
/**
 * @author roart
 *
 */
open module dbdao {
    exports roart.db.dao;
    exports roart.db.dao.util;

    requires common.config;
    requires iclij.common.config;
    requires coredb;
    requires coredb.spark;
    requires model;
    requires pipeline.model;
    requires org.slf4j;
    requires coredb.hibernate;
    requires stockutil;
    requires common.constants;
    requires common.model;
    requires common.cache;
    requires common.util;
    requires spring.beans;
    requires spring.context;
    requires spring.data.commons;
    requires spring.jdbc;
    requires spring.boot;
    requires spring.tx;
    requires spring.data.relational;
    requires coredb.spring;
}
