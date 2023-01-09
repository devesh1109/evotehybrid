package com.example.evotehybrid.repositories;

import com.example.evotehybrid.models.Candidate;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CandidateRepository extends CrudRepository<Candidate, Long> {

    Candidate findByWalletId(String secret);
}
