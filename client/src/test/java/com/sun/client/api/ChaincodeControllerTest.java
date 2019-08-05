package com.sun.client.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.client.client.AppClient;
import com.sun.client.model.Log;
import com.sun.client.util.AppUtils;
import org.hyperledger.fabric.protos.peer.FabricProposalResponse;
import org.hyperledger.fabric.sdk.ChaincodeResponse;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(PowerMockRunner.class)
@PrepareForTest({FabricProposalResponse.ProposalResponse.class, FabricProposalResponse.Response.class, AppUtils.class, AppClient.class})
public class ChaincodeControllerTest {

  private MockMvc mockMvc;
  private AppClient client;

  @Before
  public void setUp() throws Exception {
    mockMvc = MockMvcBuilders.standaloneSetup(ChaincodeController.class).build();
    client = mock(AppClient.class);
    // Mock classes with static methods
    PowerMockito.mockStatic(AppUtils.class);
    PowerMockito.mockStatic(AppClient.class);
    when(AppClient.getInstance()).thenReturn(client);
  }

  @Test
  public void testGetCurrentLog() throws Exception {
    ProposalResponse response = mock(ProposalResponse.class);
    Log mockLog = new Log("test-application", "test-event", "test-message");
    List<Log> mockMessages = Arrays.asList(mockLog);
    Collection<ProposalResponse> responses = Arrays.asList(response);
    when(client.queryChainCode(anyString(), anyString())).thenReturn(responses);
    when(AppUtils.convertToLog(any(Collection.class), any(boolean.class))).thenReturn(mockMessages);
    // Perform get request
    MvcResult result = mockMvc.perform(get("/api/logs/" + mockLog.getApplication())).andReturn();
    assertEquals(HttpStatus.OK.value(),result.getResponse().getStatus());
    assertEquals(new ObjectMapper().writeValueAsString(mockMessages),result.getResponse().getContentAsString());
  }

  @Test
  public void testGetAllLogs() throws Exception{
    ProposalResponse response = mock(ProposalResponse.class);
    Log mockLog1 = new Log("test-application1", "test-event1", "test-message1");
    Log mockLog2 = new Log("test-application1", "test-event2", "test-message2");
    List<Log> mockMessages = Arrays.asList(mockLog1,mockLog2);
    Collection<ProposalResponse> responses = Arrays.asList(response);
    when(client.queryChainCode(anyString(), anyString())).thenReturn(responses);
    when(AppUtils.convertToLog(any(Collection.class), any(boolean.class))).thenReturn(mockMessages);
    // Perform get request
    MvcResult result = mockMvc.perform(get("/api/logs/history/" + mockLog1.getApplication())).andReturn();
    assertEquals(HttpStatus.OK.value(),result.getResponse().getStatus());
    assertEquals(new ObjectMapper().writeValueAsString(mockMessages),result.getResponse().getContentAsString());
  }

  @Test
  public void testCreateLog() throws Exception{
    ProposalResponse response = mock(ProposalResponse.class);
    Collection<ProposalResponse> responses = Arrays.asList(response);
    Log mockLog = new Log("new-test-application","new-test-event","new-test-event");
    when(response.getStatus()).thenReturn(ProposalResponse.Status.SUCCESS);
    when(client.invokeChainCode(anyString(),anyString(),anyString(),anyString(),anyString())).thenReturn(responses);
    String mockLogAsJson = new ObjectMapper().writeValueAsString(mockLog);
    // Perform post request
    MvcResult result = mockMvc.perform(post("/api/logs").content(mockLogAsJson).contentType(MediaType.APPLICATION_JSON)).andReturn();
    assertEquals(HttpStatus.CREATED.value(),result.getResponse().getStatus());

  }
}