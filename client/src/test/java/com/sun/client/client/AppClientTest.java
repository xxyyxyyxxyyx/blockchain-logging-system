package com.sun.client.client;

import com.sun.client.user.AppUser;
import com.sun.client.util.AppUtils;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.util.ResourceUtils;
import sun.misc.BASE64Decoder;

import java.io.File;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest({AppClient.class, AppUtils.class})
public class AppClientTest {

  private AppClient appClient;
  private HFClient client;
  private Channel channel;

  @Before
  public void setUp() throws Exception {
    this.appClient = AppClient.getInstance();
    this.client = HFClient.createNewInstance();
    client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
    this.channel = mock(Channel.class);
    appClient.setClient(client);
    appClient.setChannel(channel);
  }


  @Test
  public void testQueryChaincode() throws Exception {
    ProposalResponse mockResponse = mock(ProposalResponse.class);
    Peer mockPeer = mock(Peer.class);
    Collection<ProposalResponse> responses = Arrays.asList(mockResponse);
    when(channel.queryByChaincode(any(QueryByChaincodeRequest.class))).thenReturn(responses);
    when(mockResponse.getMessage()).thenReturn("mock-message");
    when(mockResponse.getStatus()).thenReturn(ProposalResponse.Status.SUCCESS);
    when(mockResponse.isVerified()).thenReturn(true);
    when(mockResponse.getPeer()).thenReturn(mockPeer);
    when(mockPeer.getName()).thenReturn("test-peer");
    // Calling method under test
    Collection<ProposalResponse> successfulResponses = appClient.queryChainCode("test-function","test-arguments");
    ProposalResponse successfulResponse = successfulResponses.iterator().next();
    assertEquals("mock-message",successfulResponse.getMessage());
    assertEquals(ProposalResponse.Status.SUCCESS,successfulResponse.getStatus());

  }

  @Test
  public void testInvokeChaincode() throws InvalidArgumentException, ProposalException {
    ProposalResponse mockResponse = mock(ProposalResponse.class);
    Peer mockPeer = mock(Peer.class);
    Collection<ProposalResponse> responses = Arrays.asList(mockResponse);
    when(channel.sendTransactionProposal(any(TransactionProposalRequest.class))).thenReturn(responses);
    when(mockResponse.getMessage()).thenReturn("mock-message");
    when(mockResponse.getStatus()).thenReturn(ProposalResponse.Status.SUCCESS);
    when(mockResponse.isVerified()).thenReturn(true);
    when(mockResponse.getPeer()).thenReturn(mockPeer);
    when(mockPeer.getName()).thenReturn("test-peer");
    // Calling method under test
    Collection<ProposalResponse> successfulResponses = appClient.invokeChainCode("test-function","test-arguments");
    ProposalResponse successfulResponse = successfulResponses.iterator().next();
    assertEquals("mock-message",successfulResponse.getMessage());
    assertEquals(ProposalResponse.Status.SUCCESS,successfulResponse.getStatus());

  }

}