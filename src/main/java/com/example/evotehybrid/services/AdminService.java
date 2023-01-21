package com.example.evotehybrid.services;

import com.example.evotehybrid.client.Login;
import com.example.evotehybrid.hfgateway.CreateConnection;
import com.example.evotehybrid.models.Admin;
import com.example.evotehybrid.models.Ballot;
import com.example.evotehybrid.models.Election;
import com.example.evotehybrid.models.Voter;
import com.example.evotehybrid.repositories.AdminRepository;
import com.example.evotehybrid.repositories.BallotRepository;
import com.example.evotehybrid.repositories.ElectionRepository;
import com.example.evotehybrid.repositories.VoterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {
    @Autowired
    AdminRepository adminRepository;

    @Autowired
    ElectionRepository electionRepository;

    @Autowired
    VoterRepository voterRepository;

    @Autowired
    BallotRepository ballotRepository;

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
                List<Voter> voters = voterRepository.findByConstituency(election1.getConstituency());
                for (Voter voter : voters) {
                    // create ballot for voters
                    Ballot ballot = new Ballot();
                    ballot.setConstituency(election1.getConstituency());
                    ballot.setVoterId(voter.getId());
                    ballot.setElectionId(election1.getId());
                    ballot.setCandidates(election1.getCandidates());
                    ballot = ballotRepository.save(ballot);
                    boolean isBallotCreated = CreateConnection.createBallot(ballot);
                    if (!isBallotCreated) {
                        // try again once more
                        CreateConnection.createBallot(ballot);
                    }
                }
                return election1;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new Election();
    }

    public String declareResults(Election election) {
        List<Ballot> allBallotOfThisElection = ballotRepository.findByElectionId(election.getId());
        // create map of candidates
        Map<String, Long> candidateMap = new HashMap<>();
        for (String candidate : election.getCandidates().split(",")) {
            candidateMap.put(candidate, 0l);
        }
        // update counts
        for (Ballot ballot : allBallotOfThisElection) {
            candidateMap.put(ballot.getVote(), (Long) candidateMap.getOrDefault(ballot.getVote(), 0l) + 1);
        }
        Long max = 0l;
        String winner = "";
        for (String candidate : candidateMap.keySet()) {
            if (candidateMap.get(candidate) > max) {
                max = candidateMap.get(candidate);
                winner = candidate;
            }
        }
        // update result in election
        election.setResult("Winner of election " + election.getName() + " is " + winner + " with " + max + " votes.");
        electionRepository.save(election);
        return election.getResult();
    }
}
