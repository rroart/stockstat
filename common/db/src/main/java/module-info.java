/**
 * 
 */
/**
 * @author roart
 *
 */
module db {
    exports roart.db.model;
    exports roart.db.thread;

    requires common.constants;
    requires common.util;
    requires java.persistence;
    requires org.hibernate.orm.core;
    requires slf4j.api;
    requires java.naming;
    requires java.transaction;
}
