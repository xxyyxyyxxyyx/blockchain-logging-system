package com.sun.chaincode;

import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyModification;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import static org.hyperledger.fabric.shim.Chaincode.Response;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LogChaincodeTest {

  private ChaincodeStub chaincodeStub;
  private LogChaincode logChaincode;

  @BeforeEach
  public void init() {
    this.chaincodeStub = mock(ChaincodeStub.class);
    this.logChaincode = new LogChaincode();
  }

  @Test
  public void testInit() {
    Response response = logChaincode.init(chaincodeStub);
    assertEquals(Response.Status.SUCCESS, response.getStatus());
  }

  @Nested
  class InvokeTest {
    @Test
    public void testInvokeCreateLog() {
      when(chaincodeStub.getFunction()).thenReturn("createLog");
      List<String> list = new ArrayList<>();
      list.add("test-application");
      list.add("test-event");
      list.add("test-message");
      when(chaincodeStub.getParameters()).thenReturn(list);
      Response response = logChaincode.invoke(chaincodeStub);
      assertEquals(Response.Status.SUCCESS, response.getStatus());
    }

    @Test
    public void testInvokeGetLog() {
      String application = "test-application";
      String methodName = "getLog";
      when(chaincodeStub.getFunction()).thenReturn(methodName);
      when(chaincodeStub.getParameters()).thenReturn(Arrays.asList(application));
      when(chaincodeStub.getStringState(anyString())).thenReturn("test-log");
      Response response = logChaincode.invoke(chaincodeStub);
      assertEquals(Response.Status.SUCCESS, response.getStatus());
    }

    @Test
    public void testInvokeGetLogHistory() {
      String applicationName = "test-application";
      String functionName = "getLogHistory";
      KeyModification keyModification = mock(KeyModification.class);
      QueryResultsIterator<KeyModification> resultsIterator = mock(QueryResultsIterator.class);
      Iterator<KeyModification> iterator = mock(Iterator.class);
      when(chaincodeStub.getParameters()).thenReturn(Arrays.asList(applicationName));
      when(chaincodeStub.getFunction()).thenReturn(functionName);
      when(chaincodeStub.getHistoryForKey(anyString())).thenReturn(resultsIterator);
      Mockito.doCallRealMethod().when(resultsIterator).forEach(any(Consumer.class));
      when(resultsIterator.iterator()).thenReturn(iterator);
      when(iterator.hasNext()).thenReturn(true).thenReturn(false);
      when(iterator.next()).thenReturn(keyModification);
      when(keyModification.getStringValue()).thenReturn("test-history-data");
      Response response = logChaincode.invoke(chaincodeStub);
      assertEquals(Response.Status.SUCCESS, response.getStatus());
    }
  }

}