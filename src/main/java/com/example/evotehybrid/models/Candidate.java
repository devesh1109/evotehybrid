package com.example.evotehybrid.models;


import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Table(name = "candidate")
public class Candidate {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;
    private String name;
    private String constituency;
    private String kycUuid;
    private String walletId;
    private String votingCampaign;
    private String isApproved;
    private String electionId;
}
