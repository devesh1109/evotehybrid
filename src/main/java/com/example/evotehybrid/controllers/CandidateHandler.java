package com.example.evotehybrid.controllers;

import com.example.evotehybrid.client.Login;
import com.example.evotehybrid.models.Candidate;
import com.example.evotehybrid.services.CandidateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;


@RestController("/candidate")
public class CandidateHandler {

    @Autowired
    CandidateService candidateService;

    @PostMapping("/candidate/register")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Candidate registerCandidate(@RequestBody Candidate candidate){
        Candidate oneCandidate = new Candidate();
        Candidate candidate1 = candidateService.registerCandidate(candidate);
        return candidate1;
    }

    @PostMapping("/candidate/authenticate")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public HashMap<String, Object> authenticate(@RequestBody Login login){
        Boolean isAuthenticated = candidateService.authenticate(login);
        HashMap<String, Object> result = new HashMap<>();
        result.put("isAuthenticated", isAuthenticated);
        result.put("actions", new String[]{"viewAndUpdateCampaign", "viewResults"});
        return result;
    }
}
