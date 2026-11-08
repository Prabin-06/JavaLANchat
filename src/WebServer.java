
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.File;
import java.io.OutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;

public class WebServer {

  public static void main(String[] args) throws IOException {

    HttpServer server = HttpServer.create(
        new InetSocketAddress(8080), 0);

    server.createContext("/", exchange -> {

      String path = exchange.getRequestURI().getPath();

      if (path.equals("/")) {
        path = "/index.html";
      }

      File file = new File("../web", path.substring(1));

      if (!file.exists() || !file.isFile()) {

        String message = "404 - File not found";

        exchange.sendResponseHeaders(404, message.length());

        try (OutputStream output = exchange.getResponseBody()) {
          output.write(message.getBytes());
        }

        return;
      }

      String contentType = "text/plain";

      if (path.endsWith(".html")) {
        contentType = "text/html";
      } else if (path.endsWith(".css")) {
        contentType = "text/css";
      } else if (path.endsWith(".js")) {
        contentType = "application/javascript";
      }

      byte[] content = Files.readAllBytes(file.toPath());

      exchange.getResponseHeaders().set(
          "Content-Type", contentType + "; charset=UTF-8");

      exchange.sendResponseHeaders(200, content.length);

      try (OutputStream output = exchange.getResponseBody()) {
        output.write(content);
      }
    });

    server.setExecutor(null);
    server.start();

    System.out.println("Web server running on port 8080");
    System.out.println("Open http://localhost:8080");
  }
}
