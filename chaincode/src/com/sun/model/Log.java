package com.sun.model;

public class Log {
  // Timestamp for log creation
  private String timestamp;
  // Log creating application application
  private String application;
  // Log triggering event
  private String event;
  // Log message
  private String message;

  public Log(String application, String event, String message, String timestamp) {
    this.application = application;
    this.event = event;
    this.message = message;
    this.timestamp = timestamp;
  }

//  public Log(String application, String event, String message, String timestamp) {
//    this.timestamp = timestamp;
//    this.application = application;
//    this.event = event;
//    this.message = message;
//  }

  public String getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(String timestamp) {
    this.timestamp = timestamp;
  }


  public String getEvent() {
    return event;
  }

  public void setEvent(String event) {
    this.event = event;
  }

  public String getApplication() {
    return application;
  }

  public void setApplication(String application) {
    this.application = application;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}

