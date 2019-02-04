package org.sla;

import javafx.scene.control.TextField;

public class GUIUpdater implements Runnable {
    private SynchronizedQueue inputQueue;
    private TextField receiveText;

    // GUIUpdater tries to get data from the Programs inputQueue and updates the GUI when data arrives

    GUIUpdater(SynchronizedQueue q, TextField text) {
        inputQueue = q;
        receiveText = text;
    }

    public void run() {
        Thread.currentThread().setName("GUIUpdater Thread");

        while (!Thread.interrupted()) {
            // Try to get a string from the inputQueue
            String nextString = (String)inputQueue.get();
            while (nextString == null) {
                Thread.currentThread().yield();
                nextString = (String)inputQueue.get();
            }
            // Got a string... update the GUI with it
            receiveText.setText(nextString);
        }
    }
}

