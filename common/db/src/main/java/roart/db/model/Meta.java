package roart.db.model;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
//import org.hibernate.annotations.Index;





import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "Meta")
@org.hibernate.annotations.Table(appliesTo = "Meta")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Meta implements Serializable /*,Comparable<Meta>*/ {

	@Id
@Column
private String marketid;
private String period1;
@Column
private String period2;
@Column
private String period3;
@Column
private String period4;
@Column
private String period5;
@Column
private String period6;
@Column
private String period7;
@Column
private String period8;
@Column
private String period9;

public String getMarketid() {
    return marketid;
}

public void setMarketid(String marketid) {
    this.marketid = marketid;
}

public String getPeriod1() {
	return period1;
}

public void setPeriod1(String period1) {
	this.period1 = period1;
}

public String getPeriod2() {
    return period2;
}

public void setPeriod2(String period2) {
    this.period2 = period2;
}

public String getPeriod3() {
    return period3;
}

public void setPeriod3(String period3) {
    this.period3 = period3;
}

public String getPeriod4() {
    return period4;
}

public void setPeriod4(String period4) {
    this.period4 = period4;
}

public String getPeriod5() {
    return period5;
}

public void setPeriod5(String period5) {
    this.period5 = period5;
}

public String getPeriod6() {
    return period6;
}

public void setPeriod6(String period6) {
    this.period6 = period6;
}

public String getPeriod7() {
    return period7;
}

public void setPeriod7(String period7) {
    this.period7 = period7;
}

public String getPeriod8() {
    return period8;
}

public void setPeriod8(String period8) {
    this.period8 = period8;
}

public String getPeriod9() {
    return period9;
}

public void setPeriod9(String period9) {
    this.period9 = period9;
}

@Transient
	public static Meta ensureExistence(String id) throws Exception {
        Meta fi = getById(id);
        if (fi == null) {
            fi = new Meta();
            fi.setMarketid(id);
            HibernateUtil.currentSession().save(fi);
        }
        return fi;
    }

    @Transient
        public static Meta getById(String id) throws Exception {
        //return (Meta) HibernateUtil.getHibernateSession().createQuery("from Meta where dbid = :dbid").setParameter("dbid", dbid).uniqueResult();
        // this is slower:                                                  
        return (Meta) HibernateUtil.getHibernateSession().get(Meta.class, id);                                           
    }

    @Transient
        public static List<Meta> getAll() throws Exception {
        return HibernateUtil.convert(HibernateUtil.currentSession().createQuery("from Meta").list(), Meta.class);
    }

    @Transient
       public static List<String> getMarkets() throws Exception {
        return (List<String>) HibernateUtil.getHibernateSession().createQuery("select distinct (marketid) from Meta").list() ;
    }

    @Transient
        public static List<Meta> getAll(String mymarket) throws Exception {
        return (List<Meta>) HibernateUtil.convert(HibernateUtil.currentSession().createQuery("from Meta where marketid = :mymarket").setParameter("mymarket",  mymarket).list(), Meta.class);
    }

    /*
    @Transient
    public int compareTo(Meta arg0) {
        // TODO Auto-generated method stub
        return 0;
    }
    */
    
    //@Transient

}
