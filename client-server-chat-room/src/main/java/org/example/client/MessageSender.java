package org.example.client;

import org.example.client.util.IOUtil;

import java.io.DataOutputStream;
import java.io.IOException;

class MessageSender implements Runnable {

    private final Client client;
    private final DataOutputStream output;

    MessageSender(Client client, DataOutputStream output) {
        this.client = client;
        this.output = output;
    }

    @Override
    public void run() {

        String messageToSend;

        while (this.client.isConnectionRunning()) {
            try {

                messageToSend = IOUtil.getClientInput();
                if (messageToSend.equals(Client.EXIT_COMMAND)) {
                    this.client.closeClientConnection();
                }
                this.client.sendMessage(this.output, messageToSend);

            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
