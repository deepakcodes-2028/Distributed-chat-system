# Distributed Chat System with End-to-End Encryption

A **Java-based distributed chat application** that allows multiple users to communicate with each other securely using **end-to-end encryption (E2EE)**. Messages are encrypted so that only the intended recipient can read them — not even the server.

---

## Table of Contents

1. [Description](#description)
2. [Features](#features)
3. [Technologies Used](#technologies-used)
4. [Project Structure](#project-structure)
5. [Installation Steps](#installation-steps)
6. [How to Run the Project](#how-to-run-the-project)
7. [Usage Instructions](#usage-instructions)
8. [How Encryption Works](#how-encryption-works)
9. [Future Scope](#future-scope)
10. [Contributing](#contributing)
11. [License](#license)

---

## Description

This project is a **multi-user chat system** built in Java. Here is what makes it special:

- Users connect to a central **Chat Server** over a network
- When two users want to chat they exchange **encryption keys** automatically in the background
- All messages are **encrypted before being sent** and **decrypted only by the receiver**
- The server simply forwards messages — it cannot read the actual content

This simulates how real-world secure messaging apps like Signal or WhatsApp protect your privacy.

---

## Features

- **Multi-user support** — Multiple clients can connect to the server at the same time
- **End-to-End Encryption (E2EE)** — Messages are encrypted so only the receiver can read them
- **RSA Key Exchange** — Each user generates a public/private RSA key pair on startup
- **AES Session Keys** — Fast and secure AES-256 encryption for actual messages
- **Real-time messaging** — Messages are delivered instantly using socket programming
- **Distributed architecture simulation** — The server can forward messages to peer nodes if the receiver is not found locally
- **Clean disconnect handling** — Users can type `exit` to leave gracefully
- **Thread-safe design** — Multiple clients handled concurrently without conflicts

---

## Technologies Used

| Technology                              | Purpose                                               |
| --------------------------------------- | ----------------------------------------------------- |
| **Java 8+**                             | Core programming language                             |
| **Java Sockets**                        | Network communication between client and server       |
| **Java Threads**                        | Handle multiple clients at the same time              |
| **RSA Encryption**                      | Secure key exchange between users                     |
| **AES-256 Encryption**                  | Fast encryption of chat messages                      |
| **ObjectOutputStream / ObjectInputStream** | Sending Java objects over the network              |
| **ConcurrentHashMap**                   | Thread-safe storage of connected clients and keys     |
| **ExecutorService (Thread Pool)**       | Efficient thread management on the server             |

---

## Project Structure

Here is what each file and folder does:

```
Distributed-chat system/
│
├── src/
│   └── com/
│       └── chat/
│           │
│           ├── server/
│           │   ├── ChatServer.java       Starts the server and accepts client connections
│           │   └── ClientHandler.java    Manages each individual client on a separate thread
│           │
│           ├── client/
│           │   └── ChatClient.java       Connects to server and sends and receives messages
│           │
│           ├── model/
│           │   ├── Message.java          Defines the Message object sent over the network
│           │   └── MessageType.java      Enum for message types (CONNECT, CHAT, KEY_EXCHANGE, etc.)
│           │
│           └── encryption/
│               ├── AESUtil.java          Generates AES keys and encrypts and decrypts messages
│               └── RSAUtil.java          Generates RSA key pairs and encrypts and decrypts AES keys
│
├── bin/                                  Compiled class files go here
├── out/                                  IDE output directory
└── README.md                             This file
```

---

## Installation Steps

Follow these steps carefully one by one. No prior experience needed.

### Step 1 — Make Sure Java is Installed

Open your **Command Prompt** on Windows or **Terminal** on Mac/Linux and type:

```bash
java -version
```

You should see something like:

```
java version "17.0.x" ...
```

If Java is **not installed** download it from:
https://www.oracle.com/java/technologies/downloads/

This project requires **Java 8 or higher**.

---

### Step 2 — Get the Project Files

**Option A — Clone using Git (recommended)**

```bash
git clone https://github.com/your-username/distributed-chat-system.git
```

Then move into the project folder:

```bash
cd distributed-chat-system
```

**Option B — Download as ZIP**

1. Click the green **Code** button on GitHub
2. Click **Download ZIP**
3. Extract the ZIP file to a folder on your computer

---

### Step 3 — Open the Project in an IDE

This project works best in **IntelliJ IDEA** or **Eclipse**.

- **IntelliJ IDEA** (recommended, free Community Edition): https://www.jetbrains.com/idea/download/
- Open the project by selecting **File > Open** and choosing the `Distributed-chat system` folder

---

### Step 4 — Compile the Project

**Using an IDE (IntelliJ or Eclipse)**

Simply open the project and the IDE will compile it automatically.

**Using Command Line (Manual)**

Navigate to the project root and compile all files:

```bash
cd "Distributed-chat system"

javac -d bin src/com/chat/model/*.java src/com/chat/encryption/*.java src/com/chat/server/*.java src/com/chat/client/*.java
```

This puts all the compiled `.class` files into the `bin/` folder.

---

## How to Run the Project

You need to open **at least 2 terminal windows** — one for the server and one or more for each client.

### Step 1 — Start the Server

In your **first terminal** run:

```bash
java -cp bin com.chat.server.ChatServer
```

You should see:

```
Chat Server started on port 9000
```

The server listens on **port 9000** by default. You can change this by passing a port number:

```bash
java -cp bin com.chat.server.ChatServer 8080
```

---

### Step 2 — Start Client 1

Open a **second terminal** and run:

```bash
java -cp bin com.chat.client.ChatClient
```

You will be asked to enter a username:

```
Enter username: Alice
```

Type `Alice` and press **Enter**.

---

### Step 3 — Start Client 2

Open a **third terminal** and run the same command:

```bash
java -cp bin com.chat.client.ChatClient
```

Enter a different username:

```
Enter username: Bob
```

---

## Usage Instructions

Once both clients are connected you can start chatting.

### Sending a Message

Use this format in the client terminal:

```
msg <username> <your message>
```

**Example**

In Alice's terminal type:

```
msg Bob Hello Bob! How are you?
```

Bob will see:

```
[Alice (E2EE)]: Hello Bob! How are you?
```

The **(E2EE)** tag means the message was securely encrypted.

---

### First Message and Key Exchange

The very first time Alice messages Bob the following happens automatically:

1. Alice generates a random **AES session key**
2. Alice encrypts this key using **Bob's RSA public key**
3. The encrypted key is sent to Bob through the server
4. Bob decrypts the AES key using his **RSA private key**
5. From now on all messages between Alice and Bob use this AES key

You will see a system message like:

```
[System] Secure channel established with Alice
```

---

### Exiting the Chat

To leave type:

```
exit
```

---

### Message Types

| Type                   | What It Means                              |
| ---------------------- | ------------------------------------------ |
| `CONNECT`              | A new user has joined the server           |
| `PUBLIC_KEY_EXCHANGE`  | Sharing RSA public key with others         |
| `SESSION_KEY_EXCHANGE` | Sending an encrypted AES session key       |
| `CHAT_MESSAGE`         | An actual encrypted chat message           |
| `DISCONNECT`           | A user has left the chat                   |

---

## How Encryption Works

Think of it like a **padlock and key**:

1. **RSA** — Each person gets a padlock (public key) and a private key that only they have
2. When Alice wants to chat with Bob she creates a **secret AES key** (like a shared locker code)
3. She locks this AES key inside **Bob's padlock** (encrypts using Bob's public key)
4. **Only Bob** can open it with his private key
5. Now both Alice and Bob have the same secret AES key and use it to encrypt and decrypt all messages

Even if someone intercepts the messages on the network they will only see random encrypted data.

---

## Future Scope

Here are some ideas to make this project even better:

- **Group Chat** — Support messaging in chat rooms with multiple users
- **GUI Interface** — Build a graphical user interface using Java Swing or JavaFX
- **File Transfer** — Allow users to send images and files securely
- **Message History** — Store chat logs in a database such as SQLite or MySQL
- **Multiple Servers** — True distributed setup with actual peer-to-peer server nodes
- **User Authentication** — Login with a username and password
- **Offline Messages** — Store messages for users who are temporarily disconnected
- **Read Receipts** — Notify the sender when their message has been read
- **Mobile App** — Build an Android or iOS frontend that connects to this Java backend

---

## Contributing

Contributions are welcome. Here is how to get started:

### Step 1 — Fork the Repository

Click the **Fork** button at the top right of this GitHub page. This creates your own copy of the project.

### Step 2 — Clone Your Fork

```bash
git clone https://github.com/your-username/distributed-chat-system.git
cd distributed-chat-system
```

### Step 3 — Create a New Branch

Always work on a **new branch** not directly on `main`:

```bash
git checkout -b feature/your-feature-name
```

Example:

```bash
git checkout -b feature/group-chat
```

### Step 4 — Make Your Changes

Edit the code, add new features, or fix bugs.

### Step 5 — Commit Your Changes

```bash
git add .
git commit -m "Add: brief description of what you did"
```

### Step 6 — Push and Create a Pull Request

```bash
git push origin feature/your-feature-name
```

Then go to GitHub and click **Compare and pull request** to submit your changes for review.

---

### Contribution Rules

- Keep your code **clean and well-commented**
- Follow the **existing package structure** (`com.chat.*`)
- Test your changes before submitting
- One feature or fix per pull request — keep it focused

---

## License

This project is open-source and available under the [MIT License](LICENSE).

---

## Author

Made with care as a Java project demonstrating distributed systems and cryptography concepts.

---

**Note:** This project is for educational purposes. In a production environment additional security measures such as certificate-based authentication, TLS transport layer security and more robust key management would be required.

