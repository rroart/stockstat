package roart.common.springdata.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import roart.common.springdata.model.Config;
import roart.common.springdata.model.Meta;
import roart.common.springdata.model.Stock;

@Repository
public interface SpringConfigRepository  extends CrudRepository<Config, String>{
}
