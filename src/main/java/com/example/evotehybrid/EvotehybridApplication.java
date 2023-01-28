package com.example.evotehybrid;

import com.example.evotehybrid.hfgateway.CreateConnection;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
public class EvotehybridApplication {
	public static void main(String[] args) {
		SpringApplication.run(EvotehybridApplication.class, args);
		CreateConnection.registerAndEnrollAdmin();
	}
}
