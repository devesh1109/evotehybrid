package com.example.evotehybrid.services;

import com.example.evotehybrid.client.Login;
import com.example.evotehybrid.hfgateway.CreateConnection;
import com.example.evotehybrid.models.Admin;
import com.example.evotehybrid.models.Election;
import com.example.evotehybrid.repositories.AdminRepository;
import com.example.evotehybrid.repositories.ElectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class AdminService {
    @Autowired
    AdminRepository adminRepository;

    @Autowired
    ElectionRepository electionRepository;
    public boolean authenticate(Login login) {
        Admin admin = adminRepository.findByWalletId(login.getUserSecret());
        if (null != admin) {
            boolean exists = CreateConnection.validateIdentity(admin.getKycUuid(), login.getType());
            if (exists) {
                return true;
            }
        }
        return false;
    }

    public Election createElection(Election election) {
        Election election1 = electionRepository.save(election);
        try {
            boolean isCreated = CreateConnection.createElection(election1);
            if (isCreated) {
                return election1;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new Election();
    }
}
