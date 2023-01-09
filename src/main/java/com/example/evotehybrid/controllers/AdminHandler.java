package com.example.evotehybrid.controllers;

import com.example.evotehybrid.client.Login;
import com.example.evotehybrid.models.Candidate;
import com.example.evotehybrid.models.Election;
import com.example.evotehybrid.services.AdminService;
import com.example.evotehybrid.services.CandidateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;


@RestController("/admin")
public class AdminHandler {
    String[] validActions = {"createElection", "createBallots", "verifyBallots", "removeCandidate", "removeVoter",
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
    public HashMap<String, Object> createElection(@RequestBody Election election){
        Election election1 = adminService.createElection(election);
        HashMap<String, Object> result = new HashMap<>();
        result.put("result", "success");
        result.put("election", election1);
        return result;
    }
}
