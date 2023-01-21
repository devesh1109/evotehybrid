package com.example.evotehybrid.controllers;

import com.example.evotehybrid.client.Login;
import com.example.evotehybrid.models.Candidate;
import com.example.evotehybrid.models.Election;
import com.example.evotehybrid.services.AdminService;
import com.example.evotehybrid.services.CandidateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;


@RestController("/admin")
public class AdminHandler {
    String[] validActions = {"createElection", "removeCandidate", "removeVoter",
                            "announceResults", "approveCandidate"};

    @Autowired
    AdminService adminService;

    @PostMapping("/admin/authenticate")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public HashMap<String, Object> authenticate(@RequestBody Login login){
        Boolean isAuthenticated = adminService.authenticate(login);
        HashMap<String, Object> result = new HashMap<>();
        result.put("isAuthenticated", isAuthenticated);
        result.put("actions", validActions);
        return result;
    }

    @PostMapping("/admin/election/create")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public HashMap<String, Object> createElection(@RequestBody Election election, Login login){
        Boolean isAuthenticated = adminService.authenticate(login);
        HashMap<String, Object> result = new HashMap<>();
        if (isAuthenticated) {
            Election election1 = adminService.createElection(election);
            result.put("result", "success");
            result.put("election", election1);
        } else {
            result.put("isAuthenticated", false);
            result.put("result", "error");
        }
        return result;
    }

    @PostMapping("/admin/election/count")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public HashMap<String, Object> declareResults(@RequestBody Election election, Login login){
        Boolean isAuthenticated = adminService.authenticate(login);
        HashMap<String, Object> result = new HashMap<>();
        if (isAuthenticated) {
            String results = adminService.declareResults(election);
            result.put("result", "success");
            result.put("electionResults", results);
        } else {
            result.put("isAuthenticated", false);
            result.put("result", "error");
        }
        return result;
    }
}
