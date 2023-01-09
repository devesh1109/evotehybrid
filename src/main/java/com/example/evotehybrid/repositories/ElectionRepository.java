package com.example.evotehybrid.repositories;

import com.example.evotehybrid.models.Election;
import org.springframework.data.repository.CrudRepository;

public interface ElectionRepository extends CrudRepository<Election, Long> {

}
