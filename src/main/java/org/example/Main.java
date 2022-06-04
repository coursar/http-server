package org.example;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

public class Main {
  public static void main(String[] args) {
    // ServerSocket
    // Socket
    try (
        final ServerSocket serverSocket = new ServerSocket(9999);
    ) {
      while (true) {
        // блокирующий вызов
        try {
          final Socket socket = serverSocket.accept();
          handleClient(socket);
        } catch (Exception e) {
          // если произошла любая проблема с клиентом
          e.printStackTrace();
        }
      }
    } catch (IOException e) {
      // если не удалось запустить сервер
      e.printStackTrace();
    }
  }

  private static void handleClient(final Socket socket) throws IOException {
    try (
        socket;
        final OutputStream out = socket.getOutputStream();
        final InputStream in = socket.getInputStream();
    ) {
      System.out.println(socket.getInetAddress());
      out.write("Enter command\n".getBytes(StandardCharsets.UTF_8));

      final String message = readMessage(in);
      System.out.println("message = " + message);

      // hardcoded known commands
      switch (message) {
        case "time": // if (message.equals("time") {...}
          final Instant now = Instant.now();
          out.write(now.toString().getBytes(StandardCharsets.UTF_8));
          break;
        case "shutdown":  // else if (message.equals("shutdown") {...}
          out.write("Ok, shutdown server".getBytes(StandardCharsets.UTF_8));
          System.exit(0); // danger, finally не срабатывает
          break;
        default: // else {...}
          out.write("Unknown command\n".getBytes(StandardCharsets.UTF_8));
      }
    }
  }

  private static String readMessage(final InputStream in) throws IOException {
    final byte[] buffer = new byte[4096];
    int offset = 0;
    int length = buffer.length;
    // внутренний цикл чтения команды
    while (true) {
      final int read = in.read(buffer, offset, length); // read - сколько байт было прочитано
      offset += read; // offset = offset + read;
      length = buffer.length - offset;

      final byte lastByte = buffer[offset - 1];
      // если клиент прислал \n (Enter), значит он закончил вводить сообщение
      if (lastByte == '\n') { // 13 - \r, 10 - \n
        break;
      }
    }
    final String message = new String(
        buffer,
        0,
        buffer.length - length,
        StandardCharsets.UTF_8
    ).trim();
    return message;
  }
}
