package com.example.evotehybrid.repositories;

import com.example.evotehybrid.models.Admin;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepository extends CrudRepository<Admin, Long> {

    Admin findByWalletId(String secret);
}
