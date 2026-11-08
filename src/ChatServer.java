import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer {

    private static final int PORT = 5000;

    private static final Set<ClientHandler> clients = ConcurrentHashMap.newKeySet();

    private static final List<String> messages = new CopyOnWriteArrayList<>();

    private static final Set<String> webUsers = ConcurrentHashMap.newKeySet();

    public static void main(String[] args) {

        System.out.println("Chat server starting...");
        startWebServer();

        try (ServerSocket server = new ServerSocket(PORT)) {

            System.out.println("Server running on port " + PORT);

            while (true) {

                Socket socket = server.accept();

                System.out.println("New client connected!");

                ClientHandler handler = new ClientHandler(socket, clients);

                clients.add(handler);
                handler.start();
            }

        } catch (Exception e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }

    public static void broadcast(String message) {

        if (!message.startsWith("USERS:")) {
            messages.add(message);
        }

        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public static void updateUsers() {

        StringBuilder users = new StringBuilder("USERS:");

        for (ClientHandler client : clients) {

            if (client.getUsername() != null) {
                users.append(client.getUsername()).append(",");
            }
        }

        broadcast(users.toString());
    }

    public static void startWebServer() {

        try {

            HttpServer webServer = HttpServer.create(
                    new InetSocketAddress(8081), 0);

            webServer.createContext("/messages", exchange -> {

                String response = String.join("\n", messages);

                byte[] data = response.getBytes(StandardCharsets.UTF_8);

                exchange.getResponseHeaders().set(
                        "Access-Control-Allow-Origin", "*");

                exchange.getResponseHeaders().set(
                        "Content-Type", "text/plain; charset=UTF-8");

                exchange.sendResponseHeaders(200, data.length);

                try (OutputStream output = exchange.getResponseBody()) {
                    output.write(data);
                }
            });

            webServer.createContext("/send", exchange -> {

                String body = new String(
                        exchange.getRequestBody().readAllBytes(),
                        StandardCharsets.UTF_8);

                String[] parts = body.split("&", 2);

                String username = URLDecoder.decode(
                        parts[0].split("=", 2)[1],
                        StandardCharsets.UTF_8);

                String message = URLDecoder.decode(
                        parts[1].split("=", 2)[1],
                        StandardCharsets.UTF_8);

                String chatMessage = username + ": " + message;

                broadcast(chatMessage);

                byte[] response = "Message sent".getBytes(StandardCharsets.UTF_8);

                exchange.getResponseHeaders().set(
                        "Access-Control-Allow-Origin", "*");

                exchange.sendResponseHeaders(200, response.length);

                try (OutputStream output = exchange.getResponseBody()) {
                    output.write(response);
                }
            });

            webServer.start();

            System.out.println("Chat HTTP server running on port 8081");

        } catch (Exception e) {

            System.out.println("Web server error: " + e.getMessage());
        }
    }

}
