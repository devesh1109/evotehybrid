package com.example.evotehybrid.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Table(name = "ballot")
@AllArgsConstructor @NoArgsConstructor
public class Ballot {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    private String constituency;
    private Long voterId;
    private Long electionId;
    private String candidates;
    private String vote;

    public Ballot(String constituency, Long id, Long id1, String candidates) {
    }
}
