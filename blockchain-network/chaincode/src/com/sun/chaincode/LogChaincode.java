package com.sun.chaincode;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.model.Log;
import org.hyperledger.fabric.shim.ChaincodeBase;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyModification;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LogChaincode extends ChaincodeBase {

  private ObjectMapper objectMapper = new ObjectMapper();

  /**
   * Initializes the chaincode
   *
   * @param stub ChaincodeStub provided by Hyperledger Fabric
   * @return A success response
   */
  @Override
  public Response init(ChaincodeStub stub) {
    return newSuccessResponse();
  }

  /**
   * Invokes chaincode function
   *
   * @param stub ChaincodeStub provided by Hyperledger Fabric
   * @return
   */
  @Override
  public Response invoke(ChaincodeStub stub) {
    String function = stub.getFunction();
    List<String> params = stub.getParameters();
    if (function.equals("createLog")) return createLog(stub, params);
    else if (function.equals("getLog")) return getLog(stub, params);
    else if (function.equals("getLogHistory")) return getLogHistory(stub, params);
    else if (function.equals("getBlockchainHistory")) return getBlockchainHistory(stub, params);
    else if (function.equals("deleteLog")) return deleteLog(stub, params);
    return newErrorResponse("Unsupported method");
  }

  /**
   * Create a log
   *
   * @param stub ChaincodeStub provided by Hyperledger Fabric
   * @param args List of arguments containing function name and arguments to the function
   * @return A success response if the log is created successfully / return error response
   */
  private Response createLog(ChaincodeStub stub, List<String> args) {
    if (args.size() != 4) return newErrorResponse("Incorrect number of arguments, expecting 3");
    String application = args.get(0);
    String event = args.get(1);
    String message = args.get(2);
    String timestamp = args.get(3);
    Log log = new Log(application, event, message, timestamp);
    try {
      stub.putState(application, this.objectMapper.writeValueAsBytes(log));
      return newSuccessResponse("Log created successfully");
    } catch (Throwable e) {
      return newErrorResponse(e.getMessage());
    }
  }

  /**
   * Gets a log
   *
   * @param stub ChaincodeStub provided by Hyperledger Fabric
   * @param args List of arguments containing function name and arguments to the function
   * @return A success response if the log is created successfully / return error response
   */
  private Response getLog(ChaincodeStub stub, List<String> args) {
    if (args.size() != 1) return newErrorResponse("Incorrect number of arguments, expecting 1");
    try {
      String log = stub.getStringState(args.get(0));
      return newSuccessResponse(objectMapper.writeValueAsBytes(log));
    } catch (Throwable e) {
      return newErrorResponse(e.getMessage());
    }
  }

  /**
   * Gets history of a log
   *
   * @param stub ChaincodeStub provided by Hyperledger Fabric
   * @param args List of arguments containing function name and arguments to the function
   * @return A success response if the log is created successfully / return error response
   */
  private Response getLogHistory(ChaincodeStub stub, List<String> args) {
    if (args.size() != 1) return newErrorResponse("Incorrect number of arguments, expecting 1");
    QueryResultsIterator<KeyModification> logHistory = stub.getHistoryForKey(args.get(0));
    List<String> logs = new ArrayList<>();
    logHistory.forEach(keyModification ->
            logs.add(keyModification.getStringValue())
    );
    try {
      return newSuccessResponse(objectMapper.writeValueAsBytes(logs));
    } catch (Throwable e) {
      return newErrorResponse(e.getMessage());
    }
  }

  /**
   * Create a log
   *
   * @param stub ChaincodeStub provided by Hyperledger Fabric
   * @param args List of arguments containing function name and arguments to the function
   * @return A success response if the log is created successfully / return error response
   */
  private Response getBlockchainHistory(ChaincodeStub stub, List<String> args) {
    if (args.size() != 1) return newErrorResponse("Incorrect number of arguments, expected 1 got " + args.size());
    QueryResultsIterator<KeyModification> blockchainHistory = stub.getHistoryForKey(args.get(0));
    try {
      return newSuccessResponse(objectMapper.writeValueAsBytes(blockchainHistory));
    } catch (IOException e) {
      return newErrorResponse(e.getMessage());
    }
  }

  /**
   * @param stub
   * @param args
   * @return
   */
  private Response deleteLog(ChaincodeStub stub, List<String> args) {
    if (args.size() != 1) return newErrorResponse("Incorrect number of arguments, expected 1 got " + args.size());
    stub.delState(args.get(0));
    return newSuccessResponse("Deleted successfully");
  }

  public static void main(String[] args) {
    new LogChaincode().start(args);
  }
}


