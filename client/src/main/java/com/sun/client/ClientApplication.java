package com.sun.client;

import com.sun.client.client.AppClient;
import com.sun.client.user.AppUser;
import com.sun.client.util.AppUtils;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Scanner;

@SpringBootApplication
public class ClientApplication {
    private final static Logger LOGGER = LoggerFactory.getLogger(ClientApplication.class);

    public static void main(String[] args) throws Exception {
        SpringApplication.run(ClientApplication.class, args);
        Setup();
    }

    /**
     * Static method for network setup
     * @throws Exception
     */
    public static void Setup() throws Exception{
        AppClient client = AppClient.getInstance();
        HFClient hfClient = HFClient.createNewInstance();
        hfClient.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
        client.setClient(hfClient);
        AppUser admin = new AppUser("admin","org1","Org1MSP",AppUtils.createAdminPeerEnrollment());
        client.setUser(admin);
        // Creating a channel
        client.createChannel();
        // Installing chaincode
        client.installChainCode();
        // Instantiating the installed chaincode
        client.instantiateChainCode();
    }


}