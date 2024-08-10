package org.example.client;

import org.example.client.util.IOUtil;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

class Client {

    static final String EXIT_COMMAND = "//exit";

    private static final String SERVER_IP_ADDRESS = "localhost";
    private static final int SERVER_PORT = 8080;

    private final Socket socket;

    private volatile boolean connectionIsRunningStatus = true;

    Client(Socket socket) {
        this.socket = socket;
    }

    public static void main(String[] args) {

        try (Socket socket = new Socket(SERVER_IP_ADDRESS, SERVER_PORT)) {

            new Client(socket).startClient();

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    boolean isConnectionRunning() {
        return this.connectionIsRunningStatus;
    }

    synchronized void closeClientConnection() {
        this.connectionIsRunningStatus = false;
    }

    void sendMessage(DataOutputStream output, String message) throws IOException {
        output.writeUTF(message);
    }

    String receiveMessage(DataInputStream input) throws IOException {
        return input.readUTF();
    }

    void startClient() {

        try (DataInputStream input = new DataInputStream(this.socket.getInputStream());
             DataOutputStream output = new DataOutputStream(this.socket.getOutputStream())) {

            if (initializeClientUsername(input, output)) {

                Thread receiver = new Thread(new MessageReceiver(this, input));
                Thread sender = new Thread(new MessageSender(this, output));

                receiver.start();
                sender.start();

                try {
                    sender.join();
                    receiver.join();
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                }

            } else {
                System.out.println("Error initializing client.");
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
        } finally {
            IOUtil.closeClientInput();
        }
    }

    private boolean initializeClientUsername(DataInputStream input, DataOutputStream output) {

        try {

            String fromServer = receiveMessage(input);
            System.out.println(fromServer);
            sendMessage(output, IOUtil.getClientInput());
            return true;

        } catch (IOException ignored) {}
        IOUtil.closeClientInput();
        return false;
    }
}