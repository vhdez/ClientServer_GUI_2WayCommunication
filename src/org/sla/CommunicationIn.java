package org.sla;

import javafx.scene.control.TextField;

import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class CommunicationIn implements Runnable {
    private InputStream inStream;
    private SynchronizedQueue inQueue;
    private SynchronizedQueue outQueue;
    private TextField statusText;
    private boolean serverMode;

    // CommunicationIn reads from a Socket and puts data into the Program's inQueue

    CommunicationIn(InputStream in, SynchronizedQueue inQ, SynchronizedQueue outQ, TextField status) {
        inStream = in;
        // CommunicationIn puts data read from the socket into the inQueue
        inQueue = inQ;
        // Only the server needs the outQueue from CommunicationIn
        outQueue = outQ;
        statusText = status;
        serverMode = (outQ != null);
    }

    @Override
    public void run() {
        Thread.currentThread().setName("CommunicationIn Thread");
        System.out.println("CommunicationIn thread running");

        try {
            // Read all incoming communication
            // This reader reads from 1 socket's input stream
            BufferedReader reader = new BufferedReader(new InputStreamReader(inStream));
            String message;
            while ((message = reader.readLine()) != null) {
                // Received incoming data!!!
                System.out.println("CommunicationIn RECEIVED: \"" + message + "\"");
                statusText.setText("RECEIVED: \"" + message + "\"");

                // Now put that incoming data on the InputQueue so that the GUI will see it
                boolean putSucceeded = inQueue.put(message);
                while (!putSucceeded) {
                    Thread.currentThread().yield();
                    putSucceeded = inQueue.put(message);
                }
                System.out.println("CommunicationIn PUT into InputQueue: \"" + message + "\"");
                statusText.setText("PUT into InputQueue: \"" + message + "\"");

                // IF SERVER: also put that incoming data on the OutputQueue so ALL clients see it
                if (serverMode && MainServer.multicastMode) {
                    putSucceeded = outQueue.put(message);
                    while (!putSucceeded) {
                        Thread.currentThread().yield();
                        putSucceeded = outQueue.put(message);
                    }
                    System.out.println("CommunicationIn MULTICAST into OutputQueue: \"" + message + "\"");
                    statusText.setText("MULTICAST into OutputQueue: \"" + message + "\"");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            statusText.setText("CommunicationIn: networking failed. Exiting....");
        }

        System.out.println("CommunicationIn thread DONE");
    }
}
