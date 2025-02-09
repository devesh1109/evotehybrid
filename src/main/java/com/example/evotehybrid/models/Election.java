package com.example.evotehybrid.models;


import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "election")
public class Election {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;
    private String name;
    private String constituency;
    private String year;
    private String country;
    private Long startDate;
    private Long endDate;
    private String candidates;
    private String result;
}

