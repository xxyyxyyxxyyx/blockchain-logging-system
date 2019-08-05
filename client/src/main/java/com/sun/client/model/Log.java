package com.sun.client.model;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Log {
  // Log creating source application
  private String application;
  // Log triggering event
  private String event;
  // Log message
  private String message;
  private String timestamp;

  public Log() {
  }

  public Log(String application, String event, String message) {
    this.application = application;
    this.event = event;
    this.message = message;
  }

  public String getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(String timestamp) {
    this.timestamp = timestamp;
  }

  public String getApplication() {
    return application;
  }

  public void setApplication(String application) {
    this.application = application;
  }

  public String getEvent() {
    return event;
  }

  public void setEvent(String event) {
    this.event = event;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
