package org.example;

public class Main {
  public static void main(String[] args) {
    final Server server = new Server();
    server.setPort(10_000);
    server.start();
  }
}
