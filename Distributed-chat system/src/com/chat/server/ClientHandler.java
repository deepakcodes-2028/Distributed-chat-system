package com.chat.server;

import com.chat.model.Message;
import com.chat.model.MessageType;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;
    private ChatServer server;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String username;

    public ClientHandler(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            while (true) {
                Message message = (Message) in.readObject();
                if (message == null) break;

                handleMessage(message);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Connection closed for user: " + username);
        } finally {
            if (username != null) {
                server.removeClient(username);
                server.broadcast(new Message(MessageType.DISCONNECT, username, username + " has left the chat."));
            }
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleMessage(Message message) {
        switch (message.getType()) {
            case CONNECT:
                this.username = message.getSender();
                server.addClient(username, this);
                System.out.println("User connected: " + username);
                break;
            case PUBLIC_KEY_EXCHANGE:
                server.registerPublicKey(message.getSender(), message.getContent());
                server.broadcast(message); // Broadcast public key so everyone can talk to this user
                break;
            case CHAT_MESSAGE:
            case SESSION_KEY_EXCHANGE:
                // End-to-End: Server just forwards to receiver
                if (message.getReceiver() != null) {
                    server.forwardMessage(message);
                } else {
                    // Broadcast or error
                    server.broadcast(message);
                }
                break;
            case DISCONNECT:
                // Handle disconnect
                break;
            default:
                break;
        }
    }

    public void sendMessage(Message message) {
        try {
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getUsername() {
        return username;
    }
}
