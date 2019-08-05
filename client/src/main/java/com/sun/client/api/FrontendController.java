package com.sun.client.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FrontendController {

  /**
   * Serves the static index page
   * @return name of the index file
   */
  @GetMapping("/")
  public String index(){
    return "index";
  }
}
