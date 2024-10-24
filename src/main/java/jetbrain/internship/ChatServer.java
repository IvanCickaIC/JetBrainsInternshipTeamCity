package jetbrain.internship;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class ChatServer {
    private static Set<ClientHandler> clientHandlers = new HashSet<>();

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Port number is required to start the server.");
            return;
        }

        int port = Integer.parseInt(args[0]);

        System.out.println("Starting chat server on port " + port);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected");
                ClientHandler clientHandler = new ClientHandler(socket);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static synchronized void addClientHandler(ClientHandler clientHandler) {
        clientHandlers.add(clientHandler);
        System.out.println("Client added: " + clientHandler.getUsername());
    }

    public static synchronized void broadcastMessage(String message, ClientHandler sender) {
        for (ClientHandler clientHandler : clientHandlers) {
            if (clientHandler != sender) {
                clientHandler.sendMessage(message);
            }
        }
    }

    public static synchronized void removeClientHandler(ClientHandler clientHandler) {
        clientHandlers.remove(clientHandler);
        System.out.println("Client removed: " + clientHandler.getUsername());
    }
}

class ClientHandler implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String username;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }
    public String getUsername() {
        return username; // Add this method to retrieve username
    }
    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            username = in.readLine(); // First message from client is the username
            System.out.println(username + " has joined the chat.");

            ChatServer.addClientHandler(this);
            ChatServer.broadcastMessage(username + " has joined the chat.", this);

            String message;
            while ((message = in.readLine()) != null) {
                String timeStampedMessage = "[" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "] " + username + ": " + message;
                System.out.println(timeStampedMessage);
                ChatServer.broadcastMessage(timeStampedMessage, this);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // Remove the client from the handler set when they disconnect
                ChatServer.removeClientHandler(this);
                System.out.println(username + " has left the chat.");
            }
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }

}
