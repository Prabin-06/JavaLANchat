
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer {

    private static final int PORT = 5000;

    private static final Set<ClientHandler> clients =
            ConcurrentHashMap.newKeySet();

    public static void main(String[] args) {

        System.out.println("Chat server starting...");

        try (ServerSocket server = new ServerSocket(PORT)) {

            System.out.println("Server running on port " + PORT);

            while (true) {

                Socket socket = server.accept();

                System.out.println("New client connected!");

                ClientHandler handler =
                        new ClientHandler(socket, clients);

                clients.add(handler);
                handler.start();
            }

        } catch (Exception e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }

    public static void broadcast(
            String message,
            ClientHandler sender,
            Set<ClientHandler> clients) {

        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }
}
