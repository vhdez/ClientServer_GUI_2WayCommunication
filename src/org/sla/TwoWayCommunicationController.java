package org.sla;

import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class TwoWayCommunicationController {
    public TextField IPAddressText;
    public TextField portText;
    public Button    startButton;
    public TextField statusText;
    public TextField yourNameText;
    public TextField sendText;
    public Button    sendButton;
    public ListView  allReceivedText;

    // Each Program has only 1 inQueue for incoming data and 1 outQueue for outgoing data
    //     There can be many different Streams where incoming data is read from
    //              BUT all their data is put() into inQueue
    //     There can be many different Streams where outgoing data will be written to
    //              BUT all their data is got() from outQueue
    private SynchronizedQueue inQueue;
    private SynchronizedQueue outQueue;

    private boolean serverMode;
    static boolean connected;

    public void initialize() {
        inQueue = new SynchronizedQueue();
        outQueue = new SynchronizedQueue();
        connected = false;

        // Create and start the GUI updater thread
        GUIUpdater updater = new GUIUpdater(inQueue, allReceivedText, yourNameText);
        Thread updaterThread = new Thread(updater);
        updaterThread.start();
    }

    void setServerMode() {
        serverMode = true;
        startButton.setText("Start");
        try {
            // display the computer's IP address
            IPAddressText.setText(InetAddress.getLocalHost().getHostAddress());
        } catch (Exception ex) {
            ex.printStackTrace();
            statusText.setText("Server start: getLocalHost failed. Exiting....");
        }
    }

    void setClientMode() {
        serverMode = false;
        startButton.setText("Connect");
        // display the IP address for the local computer
        IPAddressText.setText("127.0.0.1");
    }

    public void startButtonPressed() {
        // If we're already connected, start button should be disabled
        if (connected) {
            // don't do anything else; the threads will stop and everything will be cleaned up by them.
            return;
        }

        // We can't start network connection if Port number is unknown
        if (portText.getText().isEmpty()) {
            // user did not enter a Port number, so we can't connect.
            statusText.setText("Type a port number BEFORE connecting.");
            return;
        }

        // We're gonna start network connection!
        connected = true;
        startButton.setDisable(true);

        if (serverMode) {

            // We're a server: create a thread for listening for connecting clients
            ConnectToNewClients connectToNewClients = new ConnectToNewClients(Integer.parseInt(portText.getText()), inQueue, outQueue, statusText, yourNameText);
            Thread connectThread = new Thread(connectToNewClients);
            connectThread.start();

        } else {

            // We're a client: connect to a server
            try {
                Socket socketClientSide = new Socket(IPAddressText.getText(), Integer.parseInt(portText.getText()));
                statusText.setText("Connected to server at IP address " + IPAddressText.getText() + " on port " + portText.getText());

                // The socketClientSide provides 2 separate streams for 2-way communication
                //   the InputStream is for communication FROM server TO client
                //   the OutputStream is for communication TO server FROM client
                // Create data reader and writer from those stream (NOTE: ObjectOutputStream MUST be created FIRST)

                // Every client prepares for communication with its server by creating 2 new threads:
                //   Thread 1: handles communication TO server FROM client
                CommunicationOut communicationOut = new CommunicationOut(socketClientSide, new ObjectOutputStream(socketClientSide.getOutputStream()), outQueue, statusText);
                Thread communicationOutThread = new Thread(communicationOut);
                communicationOutThread.start();

                //   Thread 2: handles communication FROM server TO client
                CommunicationIn communicationIn = new CommunicationIn(socketClientSide, new ObjectInputStream(socketClientSide.getInputStream()), inQueue, null, statusText, yourNameText);
                Thread communicationInThread = new Thread(communicationIn);
                communicationInThread.start();

            } catch (Exception ex) {
                ex.printStackTrace();
                statusText.setText("Client start: networking failed. Exiting....");
            }

            // We connected!
        }
    }

    public void sendButtonPressed() {
        // send puts message (sender+text) into the outQueue
        Message message = new Message(yourNameText.getText(), sendText.getText());

        boolean putSucceeded = outQueue.put(message);
        while (!putSucceeded) {
            Thread.currentThread().yield();
            putSucceeded = outQueue.put(message);
        }

        // text has been sent, so clear it from GUI
        sendText.setText("");
    }

}
