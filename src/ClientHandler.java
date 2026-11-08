
import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.Set;

public class ClientHandler extends Thread {

    private Socket socket;
    private Set<ClientHandler> clients;
    private PrintWriter output;
    private String username;

    public ClientHandler(
            Socket socket,
            Set<ClientHandler> clients) {

        this.socket = socket;
        this.clients = clients;
    }

    public void run() {

        try {

            BufferedReader input = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            output = new PrintWriter(
                    socket.getOutputStream(), true);

            username = input.readLine();

            if (username == null || username.isBlank()) {
                return;
            }

            System.out.println(username + " joined the chat!");

            broadcast(username + " joined the chat!");

            // Update the online users list
            ChatServer.updateUsers();

            String message;

            while ((message = input.readLine()) != null) {

                System.out.println(username + ": " + message);

                broadcast(username + ": " + message);
            }

        } catch (IOException e) {

            System.out.println(username + " disconnected.");

        } finally {

            clients.remove(this);

            if (username != null) {

                broadcast(username + " left the chat!");

                // Update the online users list
                ChatServer.updateUsers();
            }

            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Error closing connection.");
            }
        }
    }

    public void sendMessage(String message) {

        if (output != null) {
            output.println(message);
        }
    }

    public String getUsername() {
        return username;
    }

    private void broadcast(String message) {

        ChatServer.broadcast(message);
    }
}
