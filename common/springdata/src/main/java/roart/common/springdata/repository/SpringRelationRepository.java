package roart.common.springdata.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import roart.common.springdata.model.Meta;
import roart.common.springdata.model.Relation;
import roart.common.springdata.model.Stock;

public @Repository
interface SpringRelationRepository  extends CrudRepository<Relation, String>{
}
