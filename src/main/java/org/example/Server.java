package org.example;

import com.google.common.primitives.Bytes;
import org.example.exception.BadRequestException;
import org.example.exception.DeadlineExceedException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class Server {

  public static final byte[] CRLFCRLF = {'\r', '\n', '\r', '\n'};
  private final int port = 9999;
  private final int soTimeout = 30 * 1000;
  private final int readTimeout = 60 * 1000;
  private final int bufferSize = 4096;

  public void start() {
    // ServerSocket
    // Socket
    // ctrl + alt + v - переменная
    // ctrl + alt + f - поле
    // ctrl + alt + c - static field
    try (
        final ServerSocket serverSocket = new ServerSocket(port);
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

  private void handleClient(final Socket socket) throws IOException {
    socket.setSoTimeout(soTimeout);

    try (
        socket;
        final OutputStream out = socket.getOutputStream();
        final InputStream in = socket.getInputStream()
    ) {
      System.out.println(socket.getInetAddress());

      final String message = readMessage(in);
      System.out.println("message = " + message);

      // Ctrl + Alt + L - форматирование
      final String response = "HTTP/1.1 200 OK\r\n" +
          "Connection: close\r\n" +
          "Content-Length: 2\r\n" +
          "\r\n" +
          "OK";

      out.write(response.getBytes(StandardCharsets.UTF_8));
    }
  }

  private String readMessage(final InputStream in) throws IOException {
    final byte[] buffer = new byte[bufferSize];
    int offset = 0;
    int length = buffer.length;
    // deadline

    // System.currentTimeMillis() + 60 * 1000;
    final Instant deadline = Instant.now().plusMillis(readTimeout);
    // внутренний цикл чтения команды
    while (true) {
      if (Instant.now().isAfter(deadline)) {
        throw new DeadlineExceedException();
      }

      final int read = in.read(buffer, offset, length); // read - сколько байт было прочитано
      offset += read; // offset = offset + read;
      length = buffer.length - offset;

      final int headersEndIndex = Bytes.indexOf(buffer, CRLFCRLF);
      if (headersEndIndex != -1) {
        break;
      }

      if (read == -1) {
        throw new BadRequestException("CRLFCRLF not found, no more data");
      }

      if (length == 0) {
        throw new BadRequestException("CRLFCRLF not found");
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
