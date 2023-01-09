package com.example.evotehybrid.repositories;

import com.example.evotehybrid.client.Login;
import com.example.evotehybrid.models.Voter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoterRepository extends CrudRepository<Voter, Long> {

    Voter findByWalletId(String secret);
}
