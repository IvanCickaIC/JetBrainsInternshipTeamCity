package jetbrain.internship;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatClient {
    private static String username;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your username: ");
        username = scanner.nextLine();

        while(true){
            System.out.print("Do you want to create a new chat or join an existing one? (new/join): ");
            String choice = scanner.nextLine();

            if (choice.equalsIgnoreCase("new")) {
                System.out.print("Enter port to create new chat: ");
                int port = scanner.nextInt();
                scanner.nextLine(); // Consume the newline
                new Thread(() -> ChatServer.main(new String[]{String.valueOf(port)})).start(); // Start the server in a new thread

                // Register the new chat room with the registry server
                registerChatPort(port);

                // Give the server a moment to start before connecting
                try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }

                connectToChat(port); // Client connects to the new server
                break;
            } else if (choice.equalsIgnoreCase("join")) {
                List<Integer> availablePorts = getAvailablePorts();
                if (availablePorts.isEmpty()) {
                    System.out.println("No active chats available try to create a new one.");
                }else{
                    System.out.println("Available chat ports: " + availablePorts);
                    System.out.print("Enter the port of the chat to join: ");
                    int portToJoin = scanner.nextInt();
                    scanner.nextLine(); // Consume newline

                    connectToChat(portToJoin); // Client joins existing chat
                    break;
                }
            }else{
                System.out.println("Invalid choice try again.");
            }
        }
    }

    private static void connectToChat(int port) {
        try (Socket socket = new Socket("localhost", port);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            out.println(username); // Send username to server

            // Thread for listening to incoming messages
            new Thread(() -> {
                String response;
                try {
                    while ((response = in.readLine()) != null) {
                        System.out.println(response); // Display server messages
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            // Main thread to send messages
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String message = scanner.nextLine();
                out.println(message); // Send to server
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void registerChatPort(int port) {
        try (Socket socket = new Socket("localhost", 5000);  // Registry server's port
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            out.println("REGISTER " + port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<Integer> getAvailablePorts() {
        List<Integer> availablePorts = new ArrayList<>();
        try (Socket socket = new Socket("localhost", 5000);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            out.println("GET_PORTS");
            String response = in.readLine();

            if (response == null || response.trim().isEmpty()) {
                System.out.println("No available ports.");
                return availablePorts;
            }

            String[] ports = response.replace("[", "").replace("]", "").split(",");
                for (String port : ports) {
                    if (!port.trim().isEmpty()) {
                        availablePorts.add(Integer.parseInt(port.trim()));
                    }
                }


        } catch (IOException e) {
            e.printStackTrace();
        }
        return availablePorts;
    }
}
