package com.example.evotehybrid.services;

import com.example.evotehybrid.client.Login;
import com.example.evotehybrid.hfgateway.CreateConnection;
import com.example.evotehybrid.models.Candidate;
import com.example.evotehybrid.models.Voter;
import com.example.evotehybrid.repositories.CandidateRepository;
import com.example.evotehybrid.repositories.VoterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
@Service
public class CandidateService {
    @Autowired
    CandidateRepository candidateRepository;

    public List<Candidate> getAllVoters(){
        List<Candidate> candidates = new ArrayList<>();
        candidateRepository.findAll().forEach(candidates::add);
        return candidates;
    }

    public boolean authenticate(Login login) {
        Candidate candidate = candidateRepository.findByWalletId(login.getUserSecret());
        if (null != candidate) {
            boolean exists = CreateConnection.validateIdentity(candidate.getKycUuid(), login.getType());
            if (exists) {
                return true;
            }
        }
        return false;
    }

    public Candidate registerCandidate(Candidate candidate) {
        String response;
        response = CreateConnection.registerAndEnrollUser(candidate.getKycUuid(), "candidate");
        candidate.setWalletId(response);
        candidateRepository.save(candidate);
        return candidate;
    }
}
