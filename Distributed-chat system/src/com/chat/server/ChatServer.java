package com.chat.server;

import com.chat.model.Message;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {
    private final int port;
    private final Map<String, ClientHandler> clients = new ConcurrentHashMap<>();
    private final Map<String, String> publicKeys = new ConcurrentHashMap<>();
    private final ExecutorService pool = Executors.newCachedThreadPool();

    public ChatServer(int port) {
        this.port = port;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Chat Server started on port " + port);
            while (!serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket, this);
                pool.execute(handler);
            }
        } catch (IOException e) {
            System.err.println("Server encountered an error: " + e.getMessage());
        }
    }

    public void addClient(String username, ClientHandler handler) {
        clients.put(username, handler);
        // In a real distributed system, we would notify other server nodes here
    }

    public void removeClient(String username) {
        clients.remove(username);
        publicKeys.remove(username);
    }

    public void registerPublicKey(String username, String publicKey) {
        publicKeys.put(username, publicKey);
        System.out.println("Public key registered for: " + username);
    }

    public void forwardMessage(Message message) {
        ClientHandler receiver = clients.get(message.getReceiver());
        if (receiver != null) {
            receiver.sendMessage(message);
        } else {
            // If not found locally, search in peer nodes (Distributed Simulation)
            System.out.println("Receiver " + message.getReceiver() + " not found locally. Forwarding to peers...");
            // Simulated forwarding logic
        }
    }

    public void broadcast(Message message) {
        for (ClientHandler client : clients.values()) {
            // Forward everything except to the sender (unless it's a specific system message)
            if (message.getSender() == null || !client.getUsername().equals(message.getSender())) {
                client.sendMessage(message);
            }
        }
    }

    public static void main(String[] args) {
        int port = (args.length > 0) ? Integer.parseInt(args[0]) : 9000;
        new ChatServer(port).start();
    }
}
