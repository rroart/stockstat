/**
 * 
 */
/**
 * @author roart
 *
 */
module db {
    exports roart.db.model;

    requires java.persistence;
    requires org.hibernate.orm.core;
    requires slf4j.api;
    requires java.naming;
    requires java.transaction;
}
