package org.sla;

import javafx.scene.control.TextField;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;

public class CommunicationOut implements Runnable {
    private OutputStream outStream;
    private ArrayList<OutputStream> outStreams;
    private SynchronizedQueue outQueue;
    private TextField statusText;
    private boolean serverMode;

    // CommunicationOut gets data from the Program's outQueue and writes it to 1 or many Sockets

    CommunicationOut(OutputStream out, SynchronizedQueue outQ, TextField status) {
        outStream = out;
        outQueue = outQ;
        statusText = status;
        serverMode = false;
    }

    CommunicationOut(ArrayList<OutputStream> outs, SynchronizedQueue outQ, TextField status) {
        outStreams = outs;
        outQueue = outQ;
        statusText = status;
        serverMode = true;
    }

    public void run() {
        Thread.currentThread().setName("CommunicationOut Thread");
        System.out.println("CommunicationOut thread running");

        while (!Thread.interrupted()) {
            // keep getting from output Queue until it has data
            String nextMessage = (String) outQueue.get();
            while (nextMessage == null) {
                Thread.currentThread().yield();
                nextMessage = (String) outQueue.get();
            }
            System.out.println("CommunicationOut GOT: \"" + nextMessage + "\"");

            // write that data to 1 or many sockets
            try {
                if (serverMode) {
                    int clientCount = 0;
                    Iterator<OutputStream> allClients = outStreams.iterator();
                    while (allClients.hasNext()) {
                        OutputStream nextClient = allClients.next();
                        // This writer writes to 1 socket's input stream
                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(nextClient));
                        writer.write(nextMessage + "\n");
                        writer.flush();
                        System.out.println("CommunicationOut Client " + clientCount + ": \"" + nextMessage + "\"");
                        clientCount = clientCount + 1;
                    }
                } else {
                    // This writer writes to 1 socket's input stream
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outStream));
                    writer.write(nextMessage + "\n");
                    writer.flush();
                }

                statusText.setText("SENT: \"" + nextMessage + "\"");
                System.out.println("CommunicationOut SENT: \"" + nextMessage + "\"");

            } catch (Exception ex) {
                ex.printStackTrace();
                statusText.setText("CommunicationOut: networking failed. Exiting....");
            }
        }

    }
}
