package com.sun.client.client;

import com.sun.client.user.AppUser;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class AppClient {
  private final static Logger Logger = LoggerFactory.getLogger(AppClient.class);
  private static AppClient appClient;

  private AppUser user;
  private Channel channel;
  private HFClient client;
  // 4 peers for the network
  private Peer peer0;
  private Peer peer1;
  private Peer peer2;
  private Peer peer3;

  private AppClient() {

  }

  // Making the class a singleton
  public static AppClient getInstance() throws Exception {
    if (appClient == null) {
      appClient = new AppClient();
    }
    return appClient;
  }

  /**
   * Creates a channel called mychannel
   * @throws Exception
   */
  public void createChannel() throws Exception {
    // Creating orderers and peers for the channel
    Orderer orderer = client.newOrderer("orderer.example.com", "grpc://localhost:7050");
    Peer peer0 = client.newPeer("peer0.org1.example.com", "grpc://localhost:7051");
    this.peer0 = peer0;
    Peer peer1 = client.newPeer("peer1.org1.example.com", "grpc://localhost:7056");
    this.peer1 = peer1;
    Peer peer2 = client.newPeer("peer2.org1.example.com", "grpc://localhost:8051");
    this.peer2 = peer2;
    Peer peer3 = client.newPeer("peer3.org1.example.com", "grpc://localhost:8056");
    this.peer3 = peer3;
    // Create channel configuration file from channel.tx
    ChannelConfiguration channelConfiguration = new ChannelConfiguration(new File("config/channel.tx"));
    byte[] channelConfigurationSignatures = client.getChannelConfigurationSignature(channelConfiguration, user);
    // Create the channel
    Channel mychannel = client.newChannel("mychannel", orderer, channelConfiguration,
            channelConfigurationSignatures);
    // Adding peers to channel
    mychannel.joinPeer(peer0);
    mychannel.joinPeer(peer1);
    mychannel.joinPeer(peer2);
    mychannel.joinPeer(peer3);
    // Initialize the channel
    Channel channel = mychannel.initialize();
    this.channel = channel;
  }

  /**
   *  Install chaincode named logchaincode
   * @throws Exception
   */
  public void installChainCode() throws Exception {
    InstallProposalRequest installRequest = client.newInstallProposalRequest();
    ChaincodeID chaincodeID = ChaincodeID.newBuilder().setName("logchaincode").setVersion("1").build();
    // Peers to install the chain codes on
    List<Peer> installNodes = Arrays.asList(
            peer0,peer1,peer2,peer3
    );
    installRequest.setChaincodeID(chaincodeID);
    installRequest.setChaincodeLanguage(TransactionRequest.Type.JAVA);
    installRequest.setUserContext(client.getUserContext());
    installRequest.setChaincodeSourceLocation(new File("chaincode"));
    installRequest.setChaincodeVersion("1");
    Collection<ProposalResponse> responses = client.sendInstallProposal(installRequest, installNodes);
    for (ProposalResponse response : responses) {
      Logger.info("Chaincode {} installation on {} {}", chaincodeID.getName(), response.getPeer().getName(), response.getStatus());
    }
  }

  /**
   * Instantiates the installed chaincode
   * @throws Exception
   */
  public void instantiateChainCode() throws Exception {
    ChaincodeID chaincodeID = ChaincodeID.newBuilder().setName("logchaincode").setVersion("1").build();
    InstantiateProposalRequest instantiateRequest = client.newInstantiationProposalRequest();
    instantiateRequest.setProposalWaitTime(1000000);
    instantiateRequest.setChaincodeID(chaincodeID);
    instantiateRequest.setChaincodeLanguage(TransactionRequest.Type.JAVA);
    instantiateRequest.setFcn("init");
    instantiateRequest.setArgs("mock application", "mock event", "mock message");
    Collection<ProposalResponse> responses = channel.sendInstantiationProposal(instantiateRequest);
    Collection<ProposalResponse> successful = new LinkedList<>();
    for (ProposalResponse response : responses) {
      if (response.isVerified() && response.getStatus() == ProposalResponse.Status.SUCCESS) {
        successful.add(response);
        Logger.info("Instantiation proposal on {} {} ", response.getPeer().getName(), response.getStatus());
      }
    }
    CompletableFuture<BlockEvent.TransactionEvent> completableFuture = channel.sendTransaction(successful, client.getUserContext());
    // Wait for response from peers
    while (!completableFuture.isDone()) Thread.sleep(100);
    Logger.info("Chaincode instantiation SUCCESSFUL");
  }

  /**
   * Removes peers from the network channel
   * @param numberOfPeers Number of peers to remove from channel
   * @return The number of existing peers
   * @throws InvalidArgumentException
   */
  public int removePeer(int numberOfPeers) throws InvalidArgumentException {
    List<Peer> peers = Arrays.asList(peer3,peer2,peer1,peer0);
    for (int i=0;i<numberOfPeers;i++){
      channel.removePeer(peers.get(i));
    }
    return channel.getPeers().size();
  }
  /**
   *  Queries a chaincode function
   * @param functionName The name of the chaincode function to query
   * @param args The arguments to be passed to the chaincode query
   * @return Collection of successful responses from the chaincode
   * @throws Exception
   */
  public Collection<ProposalResponse> queryChainCode(String functionName, String... args) throws Exception {
    QueryByChaincodeRequest queryProposalRequest = client.newQueryProposalRequest();
    ChaincodeID chaincodeID = ChaincodeID.newBuilder().setName("logchaincode").setVersion("1").build();
    queryProposalRequest.setChaincodeID(chaincodeID);
    queryProposalRequest.setChaincodeLanguage(TransactionRequest.Type.JAVA);
    queryProposalRequest.setFcn(functionName);
    queryProposalRequest.setProposalWaitTime(1000);
    queryProposalRequest.setArgs(args);
    Collection<ProposalResponse> responses = channel.queryByChaincode(queryProposalRequest);
    // Selecting only successful responses from peers
    Collection<ProposalResponse> successfulResponses = new LinkedList<>();
    for (ProposalResponse response : responses) {
      if (response.isVerified() && response.getStatus() == ProposalResponse.Status.SUCCESS) {
        successfulResponses.add(response);
        Logger.info("Chaincode query from {} {}", response.getPeer().getName(), response.getStatus());
      }
    }

    return successfulResponses;
  }

  /**
   *  Invokes a chaincode function
   * @param functionName The name of the chaincode function to invoke
   * @param args The arguments to be passed to the chaincode invoke function
   * @return Collection of successful responses from the chaincode
   * @throws InvalidArgumentException
   * @throws ProposalException
   */
  public Collection<ProposalResponse> invokeChainCode(String functionName, String... args) throws InvalidArgumentException, ProposalException {
    // Setup transaction request
    TransactionProposalRequest transactionProposalRequest = client.newTransactionProposalRequest();
    ChaincodeID chaincodeID = ChaincodeID.newBuilder().setName("logchaincode").setVersion("1").build();
    transactionProposalRequest.setChaincodeID(chaincodeID);
    transactionProposalRequest.setProposalWaitTime(1000);
    transactionProposalRequest.setChaincodeLanguage(TransactionRequest.Type.JAVA);
    transactionProposalRequest.setFcn(functionName);
    transactionProposalRequest.setArgs(args);
    Collection<ProposalResponse> proposalResponses = channel.sendTransactionProposal(transactionProposalRequest);
    // Selecting only successful responses from peers
    Collection<ProposalResponse> successfulResponse = new LinkedList<>();
    for (ProposalResponse response : proposalResponses) {
      if (response.isVerified() && response.getStatus() == ProposalResponse.Status.SUCCESS) {
        successfulResponse.add(response);
        Logger.info("Chaincode inovaction on {} {}",response.getPeer().getName(), response.getStatus());

      }
    }
    channel.sendTransaction(successfulResponse);
    return successfulResponse;
  }

  /**
   * Getters and Setters
   */
  public AppUser getUser() {
    return user;
  }

  public void setUser(AppUser user) throws InvalidArgumentException {
    this.user = user;
    client.setUserContext(user);
  }

  public Channel getChannel() {
    return channel;
  }

  public void setChannel(Channel channel) {
    this.channel = channel;
  }

  public HFClient getClient() {
    return client;
  }

  public void setClient(HFClient client) {
    this.client = client;
  }
}
