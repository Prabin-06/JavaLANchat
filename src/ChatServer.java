import java.net.ServerSocket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

public class ChatServer {
  public static void main(String[] args) throws Exception{
    ServerSocket server = new ServerSocket(5000);

    System.out.println("Chat server started on port 5000");
    while (true) {
      Socket client = server.accept();

      System.out.println("Client connected");

      ClientHandler handler = new ClientHandler(client);
      handler.start();
    }
  }
}
