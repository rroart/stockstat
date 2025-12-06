/**
 * 
 */
/**
 * @author roart
 *
 */
open module springdata {
    exports roart.common.springdata.model;
    exports roart.common.springdata.repository;

    requires common.constants;
    requires common.model;
    requires model;
    requires common.util;
    //requires org.slf4j;
    requires org.slf4j;
    requires java.naming;
	requires org.apache.commons.lang3;
    requires java.sql;
    requires spring.context;
    requires spring.beans;
    requires spring.data.commons;
    requires spring.jdbc;
    requires spring.boot;
    requires spring.tx;
    requires spring.data.relational;
    requires spring.boot.jdbc;
}
