package com.sun.client.api;

import com.sun.client.client.AppClient;
import com.sun.client.model.Log;
import com.sun.client.util.AppUtils;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@RestController()
@RequestMapping("/api")
@CrossOrigin
public class ChaincodeController {
  /**
   * Api endpoint for querying the current log for the given application
   *
   * @param application Application name to get the logs of
   * @return Json array of the current log and Http response code 200
   * @throws Exception
   */
  @GetMapping("/logs/{application}")
  public ResponseEntity<List<Log>> getCurrentLog(@PathVariable String application) throws Exception {
    AppClient client = AppClient.getInstance();
    Collection<ProposalResponse> responses = client.queryChainCode("getLog", application);
    List<Log> messages = AppUtils.convertToLog(responses, false);
    return new ResponseEntity<>(messages, HttpStatus.OK);
  }

  /**
   * Api endpoint for querying all the logs of the given application
   *
   * @param application Application name to get the log history of
   * @return Json array of the history logs and Http response code 200
   * @throws Exception
   */
  @GetMapping("/logs/history/{application}")
  public ResponseEntity<List<Log>> getAllLogs(@PathVariable String application) throws Exception {
    AppClient client = AppClient.getInstance();
    Collection<ProposalResponse> responses = client.queryChainCode("getLogHistory", application);
    List<Log> messages = AppUtils.convertToLog(responses, true);
    return new ResponseEntity<>(messages, HttpStatus.OK);
  }

  /**
   * Api endpoint for creating a new log
   * @param log The new log to be created
   * @return Http code 201 if successful else returns Http code 500
   * @throws Exception
   */
  @PostMapping(value = "/logs", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> createLog(@RequestBody Log log) throws Exception {
    AppClient client = AppClient.getInstance();
    String currentTimeStamp = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss").format(new Date());
    Collection<ProposalResponse> responses = client.invokeChainCode("createLog", log.getApplication(), log.getEvent(), log.getMessage(),currentTimeStamp);
    if (responses.iterator().next().getStatus() == ProposalResponse.Status.SUCCESS)
      return new ResponseEntity<Object>(HttpStatus.CREATED);
    else return new ResponseEntity<Object>(HttpStatus.INTERNAL_SERVER_ERROR);
  }

  /**
   * Api endpoint for deleting the log history of the given application
   * @param application Name of the application whose history is to be deleted
   * @return
   * @throws Exception
   */
  @DeleteMapping("/logs/{application}")
  public ResponseEntity<Object> deleteLog(@PathVariable String application) throws Exception {
    AppClient client = AppClient.getInstance();
    client.invokeChainCode("deleteLog", application);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  /**
   * Removes peers from network
   * @param number Number of peers to remove
   * @return Number of peers remaining/ Error message
   * @throws Exception
   */
  @DeleteMapping("/peers/{number}")
  public String removePeers(@PathVariable int number) throws Exception {
    AppClient client = AppClient.getInstance();
    if (client.getChannel().getPeers().size() > number + 1){
      int remainingPeers = client.removePeer(number);
      return "Number of remaining Peer nodes is "+remainingPeers;
    }
    return "The network should have at least one peer node";
  }


  // Experimental
  @GetMapping("/logs/{application}/blockchain")
  public ResponseEntity<Object> getBlockchainHistory(@PathVariable String application) throws Exception {
    AppClient client = AppClient.getInstance();
    Collection<ProposalResponse> responses = client.queryChainCode("getBlockchainHistory", application);
    List<Log> messages = AppUtils.convertToLog(responses, true);
    return new ResponseEntity<>(messages, HttpStatus.OK);
  }




}
