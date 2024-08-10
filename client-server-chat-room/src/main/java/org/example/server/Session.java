package org.example.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

class Session implements Runnable {

    private final Server server;
    private final Socket socket;
    private final DataInputStream input;
    private final DataOutputStream output;

    private String clientUserName;
    private boolean isTerminateSession = false;

    Session(Server server, Socket socket) throws IOException {
       this.server = server;
       this.socket = socket;
       this.input = new DataInputStream(this.socket.getInputStream());
       this.output = new DataOutputStream(this.socket.getOutputStream());
   }

   private boolean initializeNewClientSession() {
       try {

           sendMessage(MessageHandler.createNewUserName());
           this.clientUserName = receiveMessage();
           this.server.addClientSession(this);
           sendMessage(MessageHandler.welcomeMessage(this.clientUserName));
           sendMessage(MessageHandler.notifyClientAboutCommandInputs());
           this.server.broadcastMessage(MessageHandler.broadcastNewUserInChatRoom(this.clientUserName));
           return true;

       } catch (IOException ignored) {}

       return false;
   }

   void sendMessage(String message) throws IOException {
       this.output.writeUTF(message);
    }

    private String receiveMessage() throws IOException {
        return this.input.readUTF();
    }

    String getClientUserName() {
        return this.clientUserName;
    }

    @Override
    public void run() {

        if (initializeNewClientSession()) {

            try {

                String messageFromClient;
                while (!this.isTerminateSession) {
                    messageFromClient = receiveMessage();
                    handleMessageFromClient(messageFromClient);
                }

            } catch (IOException e) {
                System.err.println(MessageHandler.undefinedErrorMessage(e));

            } finally {

                this.server.removeClientSession(this);
                System.out.println(MessageHandler.clientSessionEnded(this.clientUserName));
                terminateSession();
            }

        } else {
            System.err.println(MessageHandler.creatingNewUserError());
            terminateSession();
        }
    }

    private void handleMessageFromClient(String messageFromClient) throws IOException {

        switch (messageFromClient) {

            case MessageHandler.MENU_COMMAND:
                sendMessage(MessageHandler.menuOptions());
                break;

            case MessageHandler.LIST_COMMAND:
                sendMessage(this.server.listActiveClientConnectionsByUserName());
                break;

            case MessageHandler.EXIT_COMMAND:
                this.isTerminateSession = true;
                sendMessage(MessageHandler.confirmingShutDownCommandFromClient());
                this.server.broadcastMessage(MessageHandler.broadcastUserLeftChatRoom(this.clientUserName));
                break;

            default:
                this.server.broadcastMessage(this.clientUserName + ": " + messageFromClient);
                System.out.println(MessageHandler.clientSentMessageConfirmation(this.clientUserName));
                break;
        }
    }

    private void terminateSession() {

       try {

           this.socket.close();
           this.input.close();
           this.output.close();

       } catch (IOException e) {
           throw new RuntimeException("Error closing session: " + e.getMessage());
       }
    }
}
