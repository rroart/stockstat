package roart.common.springdata.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import roart.common.springdata.model.Meta;
import roart.common.springdata.model.Stock;
import roart.common.springdata.model.TimingBL;

@Repository
public interface SpringTimingBLRepository  extends CrudRepository<TimingBL, Long>{
}
