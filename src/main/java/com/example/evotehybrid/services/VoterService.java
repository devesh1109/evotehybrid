package com.example.evotehybrid.services;

import com.example.evotehybrid.client.Login;
import com.example.evotehybrid.hfgateway.CreateConnection;
import com.example.evotehybrid.models.Ballot;
import com.example.evotehybrid.models.Voter;
import com.example.evotehybrid.repositories.BallotRepository;
import com.example.evotehybrid.repositories.VoterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class VoterService {

    @Autowired
    VoterRepository voterRepository;

    @Autowired
    BallotRepository ballotRepository;

    public List<Voter> getAllVoters(){
        List<Voter> voters = new ArrayList<>();
        voterRepository.findAll().forEach(voters::add);
        return voters;
    }

    public Voter registerVoter(Voter voter) {
        String response;
        response = CreateConnection.registerAndEnrollUser(voter.getKycUuid(), "voter");
        voter.setWalletId(response);
        voterRepository.save(voter);
        return voter;
    }

    public boolean authenticate(Login login) {
        Voter voter = voterRepository.findByWalletId(login.getUserSecret());
        if (null != voter) {
            boolean exists = CreateConnection.validateIdentity(voter.getKycUuid(), login.getType());
            if (exists) {
                return true;
            }
        }
        return false;
    }

    public boolean vote(Ballot ballot) {
        try {
            Ballot ballot1 = ballotRepository.findById(ballot.getId()).get();
            boolean isVoted = CreateConnection.updateBallot(ballot);
            if (isVoted) {
                ballot1.setVote(ballot.getVote());
                ballotRepository.save(ballot1);
                return true;
            }

        } catch (Exception ex) {
        }
        return false;
    }
}
