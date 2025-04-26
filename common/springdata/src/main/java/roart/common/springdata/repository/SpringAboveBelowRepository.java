package roart.common.springdata.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import roart.common.springdata.model.AboveBelow;
import roart.common.springdata.model.Stock;

import org.springframework.stereotype.Component;

@Repository
public interface SpringAboveBelowRepository extends CrudRepository<AboveBelow, String>{
    //@Query("SELECT m FROM Movie m WHERE m.title LIKE %:title%")
    //List<StockDTO> searchByTitleLike(@Param("title") String title);
    List<Stock> findByMarketid(String marketid);
}
