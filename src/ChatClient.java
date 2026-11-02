
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class ChatClient extends JFrame {

    private JTextArea chatArea;
    private JTextField messageField;
    private JTextField usernameField;

    private JButton connectButton;
    private JButton sendButton;

    private Socket socket;
    private PrintWriter output;
    private BufferedReader input;

    private volatile boolean connected = false;

    public ChatClient() {

        setTitle("Java LAN Chat");
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout(10, 10));

        // Top panel
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));

        usernameField = new JTextField();
        usernameField.setToolTipText("Enter username");

        connectButton = new JButton("Connect");

        topPanel.add(new JLabel("Username: "), BorderLayout.WEST);
        topPanel.add(usernameField, BorderLayout.CENTER);
        topPanel.add(connectButton, BorderLayout.EAST);

        // Chat area
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(chatArea);

        // Bottom panel
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));

        messageField = new JTextField();
        sendButton = new JButton("Send");

        sendButton.setEnabled(false);
        messageField.setEnabled(false);

        bottomPanel.add(messageField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // Connect button
        connectButton.addActionListener(e -> connectToServer());

        // Send button
        sendButton.addActionListener(e -> sendMessage());

        // Enter key sends message
        messageField.addActionListener(e -> sendMessage());

        setVisible(true);
    }

    private void connectToServer() {

        String username = usernameField.getText().trim();

        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this, "Please enter a username.");
            return;
        }

        try {

            socket = new Socket("localhost", 5000);

            output = new PrintWriter(
                    socket.getOutputStream(), true);

            input = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            output.println(username);

            connected = true;

            usernameField.setEnabled(false);
            connectButton.setEnabled(false);
            messageField.setEnabled(true);
            sendButton.setEnabled(true);

            chatArea.append("Connected to server!\n");

            Thread listener = new Thread(() -> {

                try {

                    String message;

                    while ((message = input.readLine()) != null) {

                        String received = message;

                        SwingUtilities.invokeLater(() ->
                                chatArea.append(received + "\n"));
                    }

                } catch (Exception e) {

                    SwingUtilities.invokeLater(() ->
                            chatArea.append("Connection lost.\n"));

                } finally {

                    connected = false;

                    SwingUtilities.invokeLater(() -> {
                        messageField.setEnabled(false);
                        sendButton.setEnabled(false);
                    });
                }
            });

            listener.setDaemon(true);
            listener.start();

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to connect to server: " + e.getMessage());
        }
    }

    private void sendMessage() {

        if (!connected) {
            return;
        }

        String message = messageField.getText().trim();

        if (!message.isEmpty()) {

            output.println(message);

            messageField.setText("");
        }
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() ->
                new ChatClient());
    }
}
