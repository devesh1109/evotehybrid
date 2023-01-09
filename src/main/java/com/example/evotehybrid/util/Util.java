package com.example.evotehybrid.util;

import com.example.evotehybrid.configs.Config;
import com.example.evotehybrid.models.UserContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.hyperledger.fabric.gateway.Identities;
import org.hyperledger.fabric.gateway.X509Identity;
import org.hyperledger.fabric.sdk.Enrollment;

import java.io.*;
import java.security.PrivateKey;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Balaji Kadambi
 *
 */

public class Util {
    public static void writeUserContext(UserContext userContext) throws Exception {
        String directoryPath = "users/" + userContext.getAffiliation();
        String filePath = directoryPath + "/" + userContext.getName() + ".ser";
        File directory = new File(directoryPath);
        if (!directory.exists())
            directory.mkdirs();

        FileOutputStream file = new FileOutputStream(filePath);
        ObjectOutputStream out = new ObjectOutputStream(file);

        // Method for serialization of object
        out.writeObject(userContext);

        out.close();
        file.close();
    }

    /**
     * Deserialize user
     *
     * @param affiliation
     * @param username
     * @return
     * @throws Exception
     */
    public static UserContext readUserContext(String affiliation, String username) throws Exception {
        String filePath = "users/" + affiliation + "/" + username + ".ser";
        File file = new File(filePath);
        if (file.exists()) {
            // Reading the object from a file
            FileInputStream fileStream = new FileInputStream(filePath);
            ObjectInputStream in = new ObjectInputStream(fileStream);

            // Method for deserialization of object
            UserContext uContext = (UserContext) in.readObject();

            in.close();
            fileStream.close();
            return uContext;
        }

        return null;
    }

    public static void cleanUp() {
        String directoryPath = "users";
        File directory = new File(directoryPath);
        deleteDirectory(directory);
    }

    public static boolean deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            File[] children = dir.listFiles();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDirectory(children[i]);
                if (!success) {
                    return false;
                }
            }
        }

        // either file or an empty directory
        Logger.getLogger(Util.class.getName()).log(Level.INFO, "Deleting - " + dir.getName());
        return dir.delete();
    }

    public static UserContext getAdminUserContext(X509Identity admin) {
        UserContext adminContext = new UserContext();
        adminContext.setName("admin");
        adminContext.setMspId(Config.ORG1_MSP);
        adminContext.setEnrollment(new Enrollment() {
            @Override
            public PrivateKey getKey() {
                return admin.getPrivateKey();
            }

            @Override
            public String getCert() {
                return Identities.toPemString(admin.getCertificate());
            }
        });
        adminContext.setAffiliation(Config.ORG1);
        return adminContext;
    }

    public static String classToJsonString(Object object) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            String string = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
            return string;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}