import java.net.Socket;
import java.io.PrintWriter;
import java.util.Scanner;

public class ChatClient {
  public static void main(String[] args) throws Exception{
    Socket socket = new Socket("localhost", 5000);

    System.out.println("Connected to chat server");

    PrintWriter output = new PrintWriter(socket.getOutputStream(), true);

    Scanner sc = new Scanner(System.in);

    while (true) {
      System.out.println("Enter your message (Exit to quit): ");
      String msg = sc.nextLine();

      if (msg.equalsIgnoreCase("exit")) {
        break;
      }

      output.println(msg);
    }

    socket.close();
  }
}
