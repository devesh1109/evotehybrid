package com.example.evotehybrid;

import com.example.evotehybrid.hfgateway.CreateConnection;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
public class EvotehybridApplication {
	final private static Path APPLICATION_WALLET_DIRECTORY = Paths.get("/home/devilscar/Downloads/evotehybrid/src/main/" +
			"resources/wallets/Org1");
	final private static Path NETWORK_DIRECTORY = Paths.get("/home/devilscar/Downloads/evotehybrid/src/main/" +
			"resources/RhinoDualOrg1GatewayConnection.json");

	public static void main(String[] args) {
		SpringApplication.run(EvotehybridApplication.class, args);
		CreateConnection.registerAndEnrollAdmin();
	}
}
