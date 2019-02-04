package org.sla;

import javafx.scene.control.TextField;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ConnectToNewClients implements Runnable {
    private int connectionPort;
    private SynchronizedQueue inQueue;
    private SynchronizedQueue outQueue;
    private ArrayList<OutputStream> clientOutputStreams;
    private TextField statusText;

    // ConnectToNewClients is server's code that listens on ServerSocket's port for connecting clients
    // When a new client's connection is accepted:
    //    1. a CommunicationIn thread is created to read data from that client (via inQueue)
    //    2. a CommunicationOut thread is created to write data to that client (via outQueue)
    //    3. (if multicast): collect outputStreams together to outQueue writes data to ALL clients

    ConnectToNewClients(int port, SynchronizedQueue inQ, SynchronizedQueue outQ, TextField status) {
        connectionPort = port;
        inQueue = inQ;
        outQueue = outQ;
        statusText = status;
        if (MainServer.multicastMode) {
            clientOutputStreams = new ArrayList<OutputStream>();
        }
    }

    public void run() {
        Thread.currentThread().setName("ConnectToNewClients Thread");
        System.out.println("ConnectToNewClients thread running");

        try {
            // ONLY server handles connections on a thread
            // Every time a separate client connects, the server creates 2 new threads:
            //   1 thread for communication FROM that new client TO server
            //   1 thread for communication TO that client FROM server

            // Start listening for client connections
            statusText.setText("Listening on port " + connectionPort);
            ServerSocket connectionSocket = new ServerSocket(connectionPort);

            while (!Thread.interrupted()) {
                // Wait until a client tries to connect
                Socket clientSocket = connectionSocket.accept();
                statusText.setText("Client has connected!");

                // EACH SEPARATE client gives the server 1 extra clientSocket
                // The clientSocket provides 2 separate streams for 2-way communication
                //   the InputStream is for communication FROM client TO server
                //   the OutputStream is for communication TO client FROM server

                // The server prepares for communication with EACH client by creating 2 new threads:
                //   Thread 1: handles communication FROM that client TO server
                CommunicationIn communicationIn = new CommunicationIn(clientSocket.getInputStream(), inQueue, outQueue, statusText);
                Thread communicationInThread = new Thread(communicationIn);
                communicationInThread.start();
                //   Thread 2: handles communication TO that client FROM server
                //   if multicast is enabled, communicationOut sends data TO ALL clients FROM server
                CommunicationOut communicationOut;
                if (MainServer.multicastMode) {
                    // collect all output streams to clients, so that server can multicast to all clients
                    clientOutputStreams.add(clientSocket.getOutputStream());
                    communicationOut = new CommunicationOut(clientOutputStreams, outQueue, statusText);
                } else {
                    communicationOut = new CommunicationOut(clientSocket.getOutputStream(), outQueue, statusText);
                }
                Thread communicationOutThread = new Thread(communicationOut);
                communicationOutThread.start();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Server ConnectToNewClients: networking failed.  Exiting...");
        }
    }
}
