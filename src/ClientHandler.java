import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ClientHandler extends Thread {
  private Socket client;

  public ClientHandler(Socket client){
    this.client = client;
  }

  public void run(){
    try{
      BufferedReader input = new BufferedReader(new InputStreamReader(client.getInputStream()));

      String msg;

      while((msg = input.readLine()) != null){
        System.out.println("Client says: " + msg);
      }

      client.close();
    } catch (Exception e){
      System.out.println("Client Disconnected");
    }
  }
}
