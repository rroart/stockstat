package roart.common.springdata.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import roart.common.springdata.model.Memory;
import roart.common.springdata.model.Meta;
import roart.common.springdata.model.Stock;

@Repository
public interface SpringMemoryRepository  extends CrudRepository<Memory, String>{
}
