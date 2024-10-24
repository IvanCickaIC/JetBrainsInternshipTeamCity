package jetbrain.internship;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatRegistryServer {
    private static final Set<Integer> activePorts = new HashSet<>();

    public static void main(String[] args) {
        int registryPort = 5000;  // Registry server listens on this port

        try (ServerSocket serverSocket = new ServerSocket(registryPort)) {
            System.out.println("Chat Registry Server is running on port " + registryPort);

            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(new RegistryHandler(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized void registerPort(int port) {
        activePorts.add(port);
    }

    public static synchronized Set<Integer> getActivePorts() {
        return new HashSet<>(activePorts);
    }
}

class RegistryHandler implements Runnable {
    private final Socket socket;

    public RegistryHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            String request = in.readLine();
            if (request.startsWith("REGISTER")) {
                // Register new chat port
                int port = Integer.parseInt(request.split(" ")[1]);
                ChatRegistryServer.registerPort(port);
                out.println("Port " + port + " registered.");
            } else if (request.equals("GET_PORTS")) {
                // Send list of active ports
                Set<Integer> activePorts = ChatRegistryServer.getActivePorts();
                out.println(activePorts);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
