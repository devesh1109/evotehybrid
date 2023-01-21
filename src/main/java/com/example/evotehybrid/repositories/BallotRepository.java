package com.example.evotehybrid.repositories;

import com.example.evotehybrid.models.Ballot;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BallotRepository extends CrudRepository<Ballot, Long> {
    List<Ballot> findByElectionId(Long electionId);
}
