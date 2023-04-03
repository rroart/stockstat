package roart.common.springdata.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import roart.common.springdata.model.IncDec;
import roart.common.springdata.model.Meta;
import roart.common.springdata.model.Stock;

@Repository
public interface SpringIncDecRepository  extends CrudRepository<IncDec, String>{
}
