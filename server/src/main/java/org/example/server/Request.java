package org.example.server;

import lombok.Data;

@Data
public class Request {
  private String method;
  private String path;
  // ignore http version
  // TODO: add other parts
  //  1. Headers
  //  2. Body
}
