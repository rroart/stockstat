package roart.common.springdata.repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import roart.model.StockItem;

@Repository
@Component
public class StockRepository {

    //@Autowired
    static JdbcTemplate jdbcTemplate;
    
    //@Autowired
    static DataSource dataSource;
    
    public DataSource getDataSource() {
        System.out.println("DDDDDDDDDDDDDDDDDDDDDDDDDD");
        return DataSourceBuilder.create()
          .driverClassName("org.postgresql.Driver")
          .url("jdbc:postgresql://localhost:5432/stockstatdev")
          .username("stockstat")
          .password("password")
          .build();     
    }

    public List<StockItem> getAll(String mymarket) throws Exception {
        if (jdbcTemplate == null) {
        dataSource = getDataSource();
        jdbcTemplate = new JdbcTemplate(dataSource);
        }
        System.out.println("ds"+ dataSource);
        String sql = "select * from stock where marketid = ?";
        return jdbcTemplate.query(sql, new Object[]{mymarket}, new StockRowMapper());
    }

    /*
    @Transactional
    public void save() {
        String query;
        int r = jdbcTemplate.update(query, new PreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps) throws SQLException {
                
            }
        });
    }
    */
    
    /*
    public static List<Stock> getAll(String mymarket) throws Exception {
        java.sql.Connection conn = java.sql.DriverManager.getConnection(System.getProperty("connection.url"));
        PreparedStatement st = conn.prepareStatement("select *from Stock where marketid = ?");
        st.setString(1, mymarket);
        ResultSet rs = st.executeQuery();
        while (rs.next()) {
            String dbid = rs.getString("dbid");
            String marketid = rs.getString("marketid");
            String id = rs.getString("id");
            String isin = rs.getString("isin");
            String name = rs.getString("name");
            Date date = rs.getDate("date");
            Double indexvalue = rs.getDouble("indexvalue");
            Double indexvaluelow = rs.getDouble("indexvaluelow");
            Double indexvaluehigh = rs.getDouble("indexvaluehigh");
            Double indexvalueopen = rs.getDouble("indexvalueopen");
            Double price = rs.getDouble("price");
            Double pricelow = rs.getDouble("pricelow");
            Double pricehigh = rs.getDouble("pricehigh");
            Double priceopen = rs.getDouble("priceopen");
            Long volume = rs.getLong("volume");
            String currency = rs.getString("currency");
            Double period1 = rs.getDouble("period1");
            Double period2 = rs.getDouble("period2");
            Double period3 = rs.getDouble("period3");
            Double period4 = rs.getDouble("period4");
            Double period5 = rs.getDouble("period5");
            Double period6 = rs.getDouble("period6");
            Double period7 = rs.getDouble("period7");
            Double period8 = rs.getDouble("period8");
            Double period9 = rs.getDouble("period9");
            // \([A-Za-z]+\) \([A-Za-z]+\) → \1 \2 = rs.get\1("\2")
            StockItem stockItem = new StockItem(dbid, marketid, id, isin, name, date, indexvalue, indexvaluelow, indexvaluehigh, indexvalueopen, price, pricelow, pricehigh, priceopen, volume, currency, period1, period2, period3, period4, period5, period6, period7, period8, period9);
            stockitems.add(stockItem);
        }
        HibernateUtil hu = new HibernateUtil(false);
        SelectionQuery<Stock> query = hu.createQuery("from Stock where marketid = :mymarket").setParameter("mymarket",  mymarket);
        return hu.get(query);
    }
    */

}
