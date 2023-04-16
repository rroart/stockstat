package roart.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;

import java.util.HashMap;
import java.util.List;

import roart.common.config.MyMyConfig;

@SpringBootTest
@Import(DbDaoUtil.class)
public class ServiceControllerTestBenchmark {

    @Autowired
    private DbDaoUtil dbDao;
    
    //@Test
    public void test() throws Exception {
        System.out.println("S" + dbDao);
        List<String> markets = dbDao.getMarkets();
        int[][] bench = new int[markets.size()][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < markets.size(); j++) {
                long time0 = System.currentTimeMillis();
                dbDao.getAll(markets.get(j), i);
                int abench = (int) ((System.currentTimeMillis() - time0) / 1000);
                bench[j][i] = abench;
            }
        }
        for (int j = 0; j < markets.size(); j++) {
            System.out.print(markets.get(j));
            for (int i = 0; i < 3; i++) {
                System.out.print(" " + bench[j][i]);
            }
            System.out.println("");
        }
    }
    
    @Test
    public void t() throws Exception {
        dbDao.bla2();
    }
}

