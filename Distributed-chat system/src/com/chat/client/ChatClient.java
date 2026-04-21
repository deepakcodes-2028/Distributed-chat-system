package com.chat.client;

import com.chat.encryption.AESUtil;
import com.chat.encryption.RSAUtil;
import com.chat.model.Message;
import com.chat.model.MessageType;

import javax.crypto.SecretKey;
import java.io.*;
import java.net.Socket;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class ChatClient {
    private String host;
    private int port;
    private String username;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private RSAUtil rsa;
    private Map<String, SecretKey> sessionKeys = new ConcurrentHashMap<>();
    private Map<String, String> peerPublicKeys = new ConcurrentHashMap<>();

    public ChatClient(String host, int port, String username) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.rsa = new RSAUtil();
    }

    public void start() {
        try {
            socket = new Socket(host, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            // 1. Connect and Send Public Key
            sendMessage(new Message(MessageType.CONNECT, username, "Hello"));
            sendMessage(new Message(MessageType.PUBLIC_KEY_EXCHANGE, username, RSAUtil.encodeKey(rsa.getPublicKey())));

            // 2. Start Message Listener Thread
            new Thread(this::listen).start();

            // 3. User Input Loop
            try (Scanner scanner = new Scanner(System.in)) {
                System.out.println("Welcome " + username + "! Type 'msg <user> <message>' to chat or 'exit' to quit.");
                
                while (true) {
                    String input = scanner.nextLine();
                    if (input.equalsIgnoreCase("exit")) break;

                    if (input.startsWith("msg ")) {
                        String[] parts = input.split(" ", 3);
                        if (parts.length < 3) continue;
                        String target = parts[1];
                        String text = parts[2];
                        sendEncryptedMessage(target, text);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (socket != null) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void listen() {
        try {
            while (true) {
                Message msg = (Message) in.readObject();
                handleIncomingMessage(msg);
            }
        } catch (Exception e) {
            System.out.println("Disconnected from server.");
        }
    }

    private void handleIncomingMessage(Message msg) {
        try {
            switch (msg.getType()) {
                case PUBLIC_KEY_EXCHANGE:
                    peerPublicKeys.put(msg.getSender(), msg.getContent());
                    break;
                case SESSION_KEY_EXCHANGE:
                    // Decrypt the session key sent by peer using our private key
                    byte[] encryptedKey = Base64.getDecoder().decode(msg.getEncryptedSessionKey());
                    byte[] decryptedKey = rsa.decrypt(encryptedKey);
                    SecretKey sessionKey = AESUtil.decodeKey(Base64.getEncoder().encodeToString(decryptedKey));
                    sessionKeys.put(msg.getSender(), sessionKey);
                    System.out.println("[System] Secure channel established with " + msg.getSender());
                    break;
                case CHAT_MESSAGE:
                    SecretKey key = sessionKeys.get(msg.getSender());
                    if (key != null) {
                        String decryptedText = AESUtil.decrypt(msg.getContent(), key);
                        System.out.println("[" + msg.getSender() + " (E2EE)]: " + decryptedText);
                    } else {
                        System.out.println("[" + msg.getSender() + " (Unsecured)]: " + msg.getContent());
                    }
                    break;
                case DISCONNECT:
                    System.out.println("[System]: " + msg.getContent());
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            System.err.println("Error handling message: " + e.getMessage());
        }
    }

    private void sendEncryptedMessage(String target, String text) {
        try {
            SecretKey sessionKey = sessionKeys.get(target);

            // If no session key exists, initiate key exchange
            if (sessionKey == null) {
                // In this simplified version, we assume we need to get their public key first
                // For demonstration, we'll wait for it or assume it's there if broadcasted
                // Real implementation would request it from server
                
                // Let's create a new AES key for this session
                sessionKey = AESUtil.generateKey();
                sessionKeys.put(target, sessionKey);

                // Encrypt AES key with Target's Public Key (Simulated retrieval)
                // For this project, we'll send a request to server or use cached one
                String targetPubKeyStr = peerPublicKeys.get(target);
                if (targetPubKeyStr == null) {
                    System.out.println("Searching for " + target + "'s public key...");
                    // In this version, users broadcast their public keys on connection
                    // If not found, we can't do E2EE yet.
                    System.out.println("Error: No public key found for " + target);
                    return;
                }

                PublicKey targetPubKey = RSAUtil.decodePublicKey(targetPubKeyStr);
                byte[] encryptedAESKey = RSAUtil.encrypt(sessionKey.getEncoded(), targetPubKey);

                Message keyEx = new Message(MessageType.SESSION_KEY_EXCHANGE, username, target, "Key Exchange");
                keyEx.setEncryptedSessionKey(Base64.getEncoder().encodeToString(encryptedAESKey));
                sendMessage(keyEx);
            }

            // Encrypt message content with AES
            String encryptedText = AESUtil.encrypt(text, sessionKey);
            sendMessage(new Message(MessageType.CHAT_MESSAGE, username, target, encryptedText));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(Message msg) {
        try {
            out.writeObject(msg);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try (Scanner sc = new Scanner(System.in)) {
            System.out.print("Enter username: ");
            String name = sc.nextLine();
            new ChatClient("localhost", 9000, name).start();
        }
    }
}
