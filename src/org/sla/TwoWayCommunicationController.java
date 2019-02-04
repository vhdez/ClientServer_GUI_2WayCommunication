package org.sla;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import java.net.Socket;

public class TwoWayCommunicationController {
    public TextField IPAddressText;
    public TextField portText;
    public Button    startButton;
    public TextField statusText;
    public TextField sendText;
    public Button    sendButton;
    public TextField receiveText;

    // Each Program has only 1 inQueue for incoming data and 1 outQueue for outgoing data
    //     There can be many different Streams where incoming data is read from
    //     There can be many different Streams where outgoing data will be written to
    private SynchronizedQueue inQueue;
    private SynchronizedQueue outQueue;

    private boolean serverMode;

    public void initialize() {
        inQueue = new SynchronizedQueue();
        outQueue = new SynchronizedQueue();

        // Create and start the GUI updater thread
        GUIUpdater updater = new GUIUpdater(inQueue, receiveText);
        Thread updaterThread = new Thread(updater);
        updaterThread.start();
    }

    void setServerMode() {
        serverMode = true;
        startButton.setText("Listen");
    }

    void setClientMode() {
        serverMode = false;
        startButton.setText("Connect");
        IPAddressText.setText("127.0.0.1");
    }

    public void startButtonPressed() {
        if (serverMode) {

            // We're a server: create a thread for listening for connecting clients
            ConnectToNewClients connectToNewClients = new ConnectToNewClients(Integer.parseInt(portText.getText()), inQueue, outQueue, statusText);
            Thread connectThread = new Thread(connectToNewClients);
            connectThread.start();

        } else {

            // We're a client: connect to a server
            try {
                Socket serverSocket = new Socket(IPAddressText.getText(), Integer.parseInt(portText.getText()));
                statusText.setText("Connected to server at IP address " + IPAddressText.getText() + " on port " + portText.getText());

                // The serverSocket provides 2 separate streams for 2-way communication
                //   the InputStream is for communication FROM server TO client
                //   the OutputStream is for communication TO server FROM client

                // Every client prepares for communication with its server by creating 2 new threads:
                //   Thread 1: handles communication FROM server TO client
                CommunicationIn communicationIn = new CommunicationIn(serverSocket.getInputStream(), inQueue, null, statusText);
                Thread communicationInThread = new Thread(communicationIn);
                communicationInThread.start();
                //   Thread 2: handles communication TO server FROM client
                CommunicationOut communicationOut = new CommunicationOut(serverSocket.getOutputStream(), outQueue, statusText);
                Thread communicationOutThread = new Thread(communicationOut);
                communicationOutThread.start();

            } catch (Exception ex) {
                ex.printStackTrace();
                statusText.setText("Client start: networking failed. Exiting....");
            }
        }
    }

    public void sendButtonPressed() {
        // send just puts data into the outQueue
        boolean putSucceeded = outQueue.put(sendText.getText());
        while (!putSucceeded) {
            Thread.currentThread().yield();
            putSucceeded = outQueue.put(sendText.getText());
        }
    }


}
