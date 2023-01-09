package com.example.evotehybrid.models;


import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
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
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}

