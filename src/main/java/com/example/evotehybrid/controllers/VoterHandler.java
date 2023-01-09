package com.example.evotehybrid.controllers;

import com.example.evotehybrid.client.Login;
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
