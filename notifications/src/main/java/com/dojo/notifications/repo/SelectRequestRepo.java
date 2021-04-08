package com.dojo.notifications.repo;

import com.dojo.notifications.model.request.SelectRequest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SelectRequestRepo extends CrudRepository<SelectRequest,Integer> {
}
