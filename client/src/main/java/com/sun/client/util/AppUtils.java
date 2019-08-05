package com.sun.client.util;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.sun.client.model.Log;
import com.sun.client.user.AppUser;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.identity.X509Enrollment;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import sun.misc.BASE64Decoder;

import java.io.*;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class AppUtils {
  /**
   * Creates an admin enrollment from private key and certificate files
   * @return Enrollment with admin provileges
   * @throws IOException
   * @throws NoSuchAlgorithmException
   * @throws InvalidKeySpecException
   */
  public static Enrollment createAdminPeerEnrollment()throws IOException,NoSuchAlgorithmException,InvalidKeySpecException{
    // Load the private key folder
    File pkFolder = new File("/root/my/crypto-config/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp/keystore");
    File[] pkFiles = pkFolder.listFiles();
    // Load certificate folder
    File certFolder = new File("/root/my/crypto-config/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp/admincerts");
    File[] certFiles = certFolder.listFiles();
    byte[] bytes = Files.readAllBytes(pkFiles[0].toPath());
    // Convert private key file to Privatekey
    String privateKeyString = new String(bytes);
    privateKeyString = privateKeyString.replace("-----BEGIN PRIVATE KEY-----", "");
    privateKeyString = privateKeyString.replace("-----END PRIVATE KEY-----", "");
    BASE64Decoder b64 = new BASE64Decoder();
    byte[] decoded = b64.decodeBuffer(privateKeyString);
    PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
    KeyFactory kf = KeyFactory.getInstance("EC");
    PrivateKey privateKey = kf.generatePrivate(spec);
    String certificate = new String(Files.readAllBytes(certFiles[0].toPath()));
    // Creating enrollment with private key and certificate
    X509Enrollment enrollment = new X509Enrollment(privateKey, certificate);
    return enrollment;
  }

  /**
   * Converts the query response from peer nodes into logs entries
   * @param responses Responses from query to peer nodes
   * @param isManyEntries Boolean flag for determining if the response contains an array of data entries
   * @return List of log entries
   * @throws Exception
   */
  public static List<Log> convertToLog(Collection<ProposalResponse> responses, boolean isManyEntries){
    List<Log> logs = new ArrayList<>();
    // Get response from only one peer node
    ProposalResponse response = responses.iterator().next();
    String payload = response.getProposalResponse().getResponse().getPayload().toStringUtf8();
    // Replace redundant characters from response data for json conversion
    payload = payload.replace("\\", "").replace("\"{", "{").replace("}\"", "}").replace(",\"\",",",").replace(",\"\"","").replace("\"\"","");
    Gson gson = new Gson();
    StringReader stringReader = new StringReader(payload);
    JsonReader jsonReader = new JsonReader(stringReader);
    // If the response contains array of data then create an array of logs
    if (isManyEntries) return Arrays.asList(gson.fromJson(jsonReader, Log[].class));
    // Create a single log from response data
    Log log = gson.fromJson(jsonReader, Log.class);
    logs.add(log);
    return logs;
  }

}
