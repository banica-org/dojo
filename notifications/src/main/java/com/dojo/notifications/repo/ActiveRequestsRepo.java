package com.dojo.notifications.repo;

import com.dojo.notifications.model.contest.ActiveContestRequests;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActiveRequestsRepo extends CrudRepository<ActiveContestRequests, Integer> {
}
