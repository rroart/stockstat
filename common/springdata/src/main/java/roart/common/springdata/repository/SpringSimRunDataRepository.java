package roart.common.springdata.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import roart.common.springdata.model.SimData;
import roart.common.springdata.model.SimRunData;

@Repository
public interface SpringSimRunDataRepository  extends CrudRepository<SimRunData, String>{
}
