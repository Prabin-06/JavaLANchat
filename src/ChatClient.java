
import javax.swing.*;
import java.awt.*;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ChatClient extends JFrame {

    private JTextArea chatArea;
    private JTextField usernameField;
    private JTextField serverField;
    private JTextField messageField;

    private JButton connectButton;
    private JButton sendButton;

    private Socket socket;
    private PrintWriter output;
    private BufferedReader input;

    private String username;
    private volatile boolean connected = false;

    public ChatClient() {

        setTitle("Java LAN Chat");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout(8, 8));

        JLabel title = new JLabel("Java LAN Chat");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(45, 55, 70));
        header.add(title);

        JPanel topPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        usernameField = new JTextField();
        serverField = new JTextField("10.88.249.221");

        connectButton = new JButton("Connect");

        topPanel.add(new JLabel("Username:"));
        topPanel.add(usernameField);
        topPanel.add(new JLabel("Server IP:"));
        topPanel.add(serverField);

        JPanel connectionPanel = new JPanel(new BorderLayout(5, 5));
        connectionPanel.add(topPanel, BorderLayout.CENTER);
        connectionPanel.add(connectButton, BorderLayout.SOUTH);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setFont(new Font("Arial", Font.PLAIN, 14));
        chatArea.setBackground(new Color(245, 246, 248));
        chatArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        JPanel bottomPanel = new JPanel(new BorderLayout(8, 8));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        messageField = new JTextField();
        messageField.setFont(new Font("Arial", Font.PLAIN, 14));

        sendButton = new JButton("Send");
        sendButton.setBackground(new Color(45, 110, 180));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);

        messageField.setEnabled(false);
        sendButton.setEnabled(false);

        bottomPanel.add(messageField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(connectionPanel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        connectButton.addActionListener(e -> connectToServer());
        sendButton.addActionListener(e -> sendMessage());
        messageField.addActionListener(e -> sendMessage());

        setVisible(true);
    }

    private void connectToServer() {

        username = usernameField.getText().trim();
        String serverIP = serverField.getText().trim();

        if (username.isEmpty() || serverIP.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this, "Enter username and server IP.");
            return;
        }

        connectButton.setEnabled(false);

        Thread connectionThread = new Thread(() -> {

            try {

                socket = new Socket(serverIP, 5000);

                output = new PrintWriter(
                        socket.getOutputStream(), true);

                input = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));

                output.println(username);

                connected = true;

                SwingUtilities.invokeLater(() -> {
                    usernameField.setEnabled(false);
                    serverField.setEnabled(false);
                    messageField.setEnabled(true);
                    sendButton.setEnabled(true);

                    chatArea.append("Connected to server!\n");
                });

                listenForMessages();

            } catch (Exception e) {

                SwingUtilities.invokeLater(() -> {
                    connectButton.setEnabled(true);

                    JOptionPane.showMessageDialog(
                            this, "Connection failed: " + e.getMessage());
                });
            }
        });

        connectionThread.setDaemon(true);
        connectionThread.start();
    }

    private void listenForMessages() {

        try {

            String message;

            while ((message = input.readLine()) != null) {

                String received = message;

                SwingUtilities.invokeLater(() -> {

                    String time = LocalTime.now().format(
                            DateTimeFormatter.ofPattern("hh:mm a"));

                    chatArea.append("[" + time + "] " + received + "\n");
                });
            }

        } catch (Exception e) {

        } finally {

            connected = false;

            SwingUtilities.invokeLater(() -> {
                messageField.setEnabled(false);
                sendButton.setEnabled(false);
                chatArea.append("Disconnected from server.\n");
            });
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

        SwingUtilities.invokeLater(() -> new ChatClient());
    }
}
