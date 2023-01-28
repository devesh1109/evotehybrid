package com.example.evotehybrid.controllers;

import com.example.evotehybrid.client.Login;
import com.example.evotehybrid.models.Ballot;
import com.example.evotehybrid.models.Voter;
import com.example.evotehybrid.services.VoterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Objects;


@RestController("/voter")
public class VoterHandler {

    @Autowired
    VoterService voterService;

    @PostMapping("/voter/register")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Voter registerVoter(@RequestBody Voter voter){
        Voter voter1 = voterService.registerVoter(voter);
        return voter1;
    }

    @PostMapping("/voter/vote")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public HashMap<String, Object> vote(@RequestBody Login login, @RequestBody Ballot ballot){
        Boolean isAuthenticated = voterService.authenticate(login);
        HashMap<String, Object> result = new HashMap<>();
        if (isAuthenticated) {
            boolean isVoted = voterService.vote(ballot);
            if (isVoted) {
                result.put("status", "success");
                result.put("isVoted", true);
            } else {
                result.put("status", "error");
                result.put("isVoted", "false - try again");
            }
        } else {
            result.put("status", "error");
            result.put("isAuthenticated", false);
        }
        return result;
    }

    @PostMapping("/voter/authenticate")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public HashMap<String, Object> authenticate(@RequestBody Login login){
        Boolean isAuthenticated = voterService.authenticate(login);
        HashMap<String, Object> result = new HashMap<>();
        result.put("isAuthenticated", isAuthenticated);
        result.put("actions", new String[]{"viewCandidate", "viewBallot", "voteUpdateBallot", "viewResults"});
        return result;
    }
}
