package com.example.evotehybrid.client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor @NoArgsConstructor
@Getter @Setter
public class Login {

    private String userName;
    private String userSecret;
    private String type;
}
