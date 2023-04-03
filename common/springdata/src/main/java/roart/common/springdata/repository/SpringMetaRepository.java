package roart.common.springdata.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import roart.common.springdata.model.Meta;

public interface SpringMetaRepository extends CrudRepository<Meta, String>{
}
