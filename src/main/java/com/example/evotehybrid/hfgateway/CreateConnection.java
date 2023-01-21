package com.example.evotehybrid.hfgateway;

import com.example.evotehybrid.configs.Config;
import com.example.evotehybrid.models.*;
import com.example.evotehybrid.util.Util;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import org.h2.util.StringUtils;
import org.hyperledger.fabric.gateway.*;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric.sdk.security.CryptoSuiteFactory;
import org.hyperledger.fabric_ca.sdk.EnrollmentRequest;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.RegistrationRequest;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Properties;
import java.util.StringJoiner;

public class CreateConnection {
    static {
        System.setProperty("org.hyperledger.fabric.sdk.service_discovery.as_localhost", "true");
    }
    final private static Path NETWORK_DIRECTORY = Paths.get("/home/devilscar/Code/fabric/fabric-samples/evotehybrid/src/main/" +
            "resources/RhinoDualOrg1GatewayConnection.json");
    final private static Path DATA_NETWORK_DIRECTORY = Paths.get("/home/devilscar/Code/fabric/fabric-samples/evotehybrid/src/main/" +
            "resources/RhinodataOrg1GatewayConnection.json");

    public static Gateway connect(Wallet wallet, String label) throws Exception{
        // Load a file system based wallet for managing identities.
        Path walletPath = Paths.get("wallet");
        if (null == wallet) {
            wallet = Wallets.newFileSystemWallet(walletPath);
        }
        if (StringUtils.isNullOrEmpty(label)) {
            return null;
        }
        // load a CCP
        Gateway.Builder builder = Gateway.createBuilder();
        builder.identity(wallet, label).networkConfig(NETWORK_DIRECTORY).discovery(true);
        return builder.connect();
    }

    public static Gateway connectCdnGateway(Wallet wallet, String label) throws Exception{
        // Load a file system based wallet for managing identities.
        Path walletPath = Paths.get("wallet/data_wallet");
        if (null == wallet) {
            wallet = Wallets.newFileSystemWallet(walletPath);
        }
        if (StringUtils.isNullOrEmpty(label)) {
            return null;
        }
        // load a CCP
        Gateway.Builder builder = Gateway.createBuilder();
        builder.identity(wallet, label).networkConfig(DATA_NETWORK_DIRECTORY).discovery(true);
        return builder.connect();
    }

    public static String registerAndEnrollAdmin() {
        try {
            // Create a CA client for interacting with the CA.
            Properties props = new Properties();
            props.put("pemFile",
                    "/home/devilscar/Code/fabric/fabric-samples/test-network/organizations/peerOrganizations/org1.example.com/ca/ca.org1.example.com-cert.pem");
            props.put("allowAllHostNames", "true");
            HFCAClient caClient = HFCAClient.createNewInstance("https://localhost:7054", props);
            CryptoSuite cryptoSuite = CryptoSuiteFactory.getDefault().getCryptoSuite();
            caClient.setCryptoSuite(cryptoSuite);

            // Create a wallet for managing identities
            Wallet wallet = Wallets.newFileSystemWallet(Paths.get("wallet"));

            // Check to see if we've already enrolled the admin user.
            if (wallet.get("admin") != null) {
                System.out.println("An identity for the admin user \"admin\" already exists in the wallet");
                return null;
            }

            // Enroll the admin user, and import the new identity into the wallet.
            final EnrollmentRequest enrollmentRequestTLS = new EnrollmentRequest();
            enrollmentRequestTLS.addHost("localhost");
            enrollmentRequestTLS.setProfile("tls");
            Enrollment enrollment = caClient.enroll("admin", "adminpw", enrollmentRequestTLS);
            Identity user = Identities.newX509Identity(Config.ORG1_MSP, enrollment);
            wallet.put("admin", user);
            Admin admin = new Admin();
            admin.setId(1l);
            admin.setName("admin");
            admin.setKycUuid("661319691231123");
            admin.setWalletId(enrollment.getKey().toString());
            uploadAdminToDataFabric(admin);
            System.out.println("Successfully enrolled user \"admin\" and imported it into the wallet");
            return enrollment.getKey().toString();
        } catch (Exception e) {
            System.err.println(e);
        }
        return null;
    }

    private static boolean uploadAdminToDataFabric(Admin admin) throws IOException {
        Wallet wallet = Wallets.newFileSystemWallet(Paths.get("wallet/data_wallet"));
        final String ORG_1_ADMIN = "Org1 Admin";
        try(Gateway gw = connectCdnGateway(wallet, ORG_1_ADMIN)) {
            Network network = gw.getNetwork("mychannel");
            Contract contract = network.getContract("AdminContract");
            String adminLabel = "admin_" + admin.getId();
            byte[] createAdminResult = contract.createTransaction("createAdmin")
                    .submit(adminLabel, admin.getName(), admin.getKycUuid(), admin.getWalletId());
            System.out.println(new String(createAdminResult, StandardCharsets.UTF_8));

            // Evaluate transactions that query state from the ledger.
            byte[] queryAdmin = contract.evaluateTransaction("readAdmin", adminLabel);
            System.out.println(new String(queryAdmin, StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    public static String registerAndEnrollUser(String kycUuid, String type) {
        try {
            String userName = type + "_" + kycUuid;
            // Create a CA client for interacting with the CA.
            Properties props = new Properties();
            props.put("pemFile",
                    "/home/devilscar/Code/fabric/fabric-samples/test-network/organizations/peerOrganizations/org1.example.com/ca/ca.org1.example.com-cert.pem");
            props.put("allowAllHostNames", "true");
            HFCAClient caClient = HFCAClient.createNewInstance("https://localhost:7054", props);
            CryptoSuite cryptoSuite = CryptoSuiteFactory.getDefault().getCryptoSuite();
            caClient.setCryptoSuite(cryptoSuite);

            // Create a wallet for managing identities
            Wallet wallet = Wallets.newFileSystemWallet(Paths.get("wallet"));

            // Check to see if we've already enrolled the admin user.
            X509Identity userIdentity = (X509Identity) wallet.get(userName);
            if (userIdentity != null) {
                System.out.println("An identity for the user "+ userName + " already exists in the wallet");
                return userIdentity.getPrivateKey().toString();
            }

            X509Identity adminIdentity = (X509Identity)wallet.get("admin");
            if (adminIdentity == null) {
                registerAndEnrollAdmin();
            }
            UserContext adminUserContext = Util.getAdminUserContext(adminIdentity);

            // Register the user, enroll the user, and import the new identity into the wallet.
            RegistrationRequest registrationRequest = new RegistrationRequest(userName);
            registrationRequest.setAffiliation(Config.ORG1_AFFILIATION);
            registrationRequest.setEnrollmentID(userName);
            String enrollmentSecret = caClient.register(registrationRequest, adminUserContext);
            Enrollment enrollment = caClient.enroll(userName, enrollmentSecret);
            Identity user = Identities.newX509Identity(Config.ORG1_MSP, enrollment);
            wallet.put(userName, user);
            System.out.println("Successfully enrolled user " + userName + " and imported it into the wallet");
            return enrollmentSecret;
        } catch (Exception e) {
            System.err.println(e);
        }
        return null;
    }

    public static boolean validateIdentity(String kycUuid, String type) {
        Wallet wallet = null;
        try {
            String userName = type + "_" + kycUuid;
            wallet = Wallets.newFileSystemWallet(Paths.get("wallet"));
            X509Identity userIdentity = (X509Identity) wallet.get(userName);
            if (userIdentity != null) {
                System.out.println("An identity for the user "+ userName + " already exists in the wallet");
                return true;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    public static boolean createElection(Election election1) throws IOException {
        Wallet wallet = Wallets.newFileSystemWallet(Paths.get("wallet"));
        // Check to see if we've already enrolled the admin user.
        if (wallet.get("admin") == null) {
            System.out.println("An identity for the admin user \"admin\" already exists in the wallet");
            return false;
        }

        String electionId = "election_" + election1.getId();
        try (Gateway gateway = connect(wallet, "admin")) {

            // Obtain a smart contract deployed on the network.
            Network network = gateway.getNetwork("mychannel");
            Contract contract = network.getContract("evoterContract");
            byte[] createVoterResult = contract.createTransaction("createElection")
                    .submit(electionId, election1.getName(), String.valueOf(election1.getStartDate()),
                            String.valueOf(election1.getEndDate()), election1.getConstituency(), election1.getCandidates());
            System.out.println(new String(createVoterResult, StandardCharsets.UTF_8));

            // Evaluate transactions that query state from the ledger.
            byte[] queryAllVoters = contract.evaluateTransaction("readElection", electionId);
            System.out.println(new String(queryAllVoters, StandardCharsets.UTF_8));
            return true;
        } catch (ContractException | IOException | InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    public static boolean createBallot(Ballot ballot) throws IOException {
        Wallet wallet = Wallets.newFileSystemWallet(Paths.get("wallet"));
        // Check to see if we've already enrolled the admin user.
        if (wallet.get("admin") == null) {
            System.out.println("An identity for the admin user \"admin\" already exists in the wallet");
            return false;
        }

        String ballotId = "ballot_" + ballot.getElectionId() + "_" + ballot.getVoterId();
        try (Gateway gateway = connect(wallet, "admin")) {

            // Obtain a smart contract deployed on the network.
            Network network = gateway.getNetwork("mychannel");
            Contract contract = network.getContract("evoterContract");
//            String ballotId, String electionId, String voterId, String candidates,
//                    String vote
            byte[] createVoterResult = contract.createTransaction("createBallot")
                    .submit(ballotId, String.valueOf(ballot.getElectionId()),
                            String.valueOf(ballot.getVoterId()),
                            ballot.getCandidates(), "");
            System.out.println(new String(createVoterResult, StandardCharsets.UTF_8));

            // Evaluate transactions that query state from the ledger.
            byte[] queryAllVoters = contract.evaluateTransaction("readBallot", ballotId);
            System.out.println(new String(queryAllVoters, StandardCharsets.UTF_8));
            return true;
        } catch (ContractException | IOException | InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    public static boolean updateBallot(Ballot ballot) throws IOException {
        Wallet wallet = Wallets.newFileSystemWallet(Paths.get("wallet"));
        // Check to see if we've already enrolled the admin user.
        if (wallet.get("admin") == null) {
            System.out.println("An identity for the admin user \"admin\" already exists in the wallet");
            return false;
        }

        String ballotId = "ballot_" + ballot.getElectionId() + "_" + ballot.getVoterId();
        try (Gateway gateway = connect(wallet, "admin")) {

            // Obtain a smart contract deployed on the network.
            Network network = gateway.getNetwork("mychannel");
            Contract contract = network.getContract("evoterContract");
//            String ballotId, String electionId, String voterId, String candidates,
//                    String vote
            byte[] createVoterResult = contract.createTransaction("updateBallot")
                    .submit(ballotId, String.valueOf(ballot.getElectionId()),
                            String.valueOf(ballot.getVoterId()),
                            ballot.getCandidates(), ballot.getVote());
            System.out.println(new String(createVoterResult, StandardCharsets.UTF_8));

            // Evaluate transactions that query state from the ledger.
            byte[] queryAllVoters = contract.evaluateTransaction("readBallot", ballotId);
            System.out.println(new String(queryAllVoters, StandardCharsets.UTF_8));
            return true;
        } catch (ContractException | IOException | InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return false;
    }


    /*public static String registerTransaction(String kycUuid, String type) throws IOException {
        WalletStore walletStore = new FileSystemWalletStore(APPLICATION_WALLET_DIRECTORY);
        WalletImpl walletImpl = new WalletImpl(walletStore);
        String privateKeyUser = null;

        try {
            String userName = type + "_" + kycUuid;
            Identity adminIdentity = walletImpl.get(Config.ADMIN);
            Properties props = new Properties();
            props.put("pemFile",
                    "/home/devilscar/Downloads/evotehybrid/src/main/resources/wallets/Org1/Org1 CA Admin.pem");
            props.put("allowAllHostNames", "true");
            HFCAClient caClient = HFCAClient.createNewInstance(CA_URL, props);
            CryptoSuite cryptoSuite = CryptoSuiteFactory.getDefault().getCryptoSuite();
            caClient.setCryptoSuite(cryptoSuite);
            if (null != adminIdentity) {


            }
            Gateway gw = Gateway.createBuilder().identity(walletImpl.get(Config.ADMIN)).networkConfig(NETWORK_DIRECTORY)
                    .connect();
            Network network = gw.getNetwork("mychannel");
            // Enroll Admin to Org1MSP
            UserContext adminUserContext = new UserContext();
            adminUserContext.setName("Org1 CA Admin");
            adminUserContext.setAffiliation(Config.ORG1);
            adminUserContext.setMspId(Config.ORG1_MSP);
            adminUserContext.setEnrollment(((GatewayImpl) gw).getClient().getUserContext().getEnrollment());
            String secret = caClient.register(new RegistrationRequest("Devesh", Config.ORG1), adminUserContext);
            UserContext userContext = Util.readUserContext(Config.ORG1, userName);
            if (null == userContext) {

//                UserContext userContext1 = new UserContext();
//                userContext1.setName(userName);
//                userContext1.setAffiliation(Config.ORG1);
//                userContext1.setMspId(Config.ORG1_MSP);
//                String secret = caClient.registerUser(userName, Config.ORG1);
//                userContext1 = caClient.enrollUser(userContext1, secret);
//                X509IdentityImpl userIdentity = (X509IdentityImpl) Identities.newX509Identity(Config.ORG1_MSP, userContext1.getEnrollment());
//                walletImpl.put(userName, userIdentity);
//                privateKeyUser = userIdentity.getPrivateKey().toString();
            }

        } catch (InvalidArgumentException e) {
            throw new RuntimeException(e);
        } catch (org.hyperledger.fabric.sdk.exception.InvalidArgumentException e) {
            throw new RuntimeException(e);
        } catch (CryptoException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return privateKeyUser;
//        try (Gateway gateway = builder.connect()) {
//
//            // Obtain a smart contract deployed on the network.
//            Network network = gateway.getNetwork("mychannel");
//            Contract contract = network.getContract("voterRegisterContract");
//
//            // Submit transactions that store state to the ledger.
//            byte[] createVoterResult = contract.createTransaction("createVoter")
//                    .submit("1", "Devesh");
//            System.out.println(new String(createVoterResult, StandardCharsets.UTF_8));
//
//            // Evaluate transactions that query state from the ledger.
//            byte[] queryAllVoters = contract.evaluateTransaction("readVoter", "1");
//            System.out.println(new String(queryAllVoters, StandardCharsets.UTF_8));
//
//        } catch (ContractException | TimeoutException | InterruptedException e) {
//            e.printStackTrace();
//        }
//        return true;
    }*/
}
