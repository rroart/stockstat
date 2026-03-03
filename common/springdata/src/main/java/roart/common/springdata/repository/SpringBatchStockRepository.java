package roart.common.springdata.repository;

import roart.common.springdata.model.Stock;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

@Repository
@Component
public interface SpringBatchStockRepository extends PagingAndSortingRepository<Stock, String>{

    Slice<Stock> findAllByMarketid(String marketid, Pageable pageable);

    //List<Stock> findByMarketid(String marketid);
}
