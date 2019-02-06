package org.sla;

import javafx.application.Platform;
import javafx.scene.control.TextField;

public class GUIUpdater implements Runnable {
    private SynchronizedQueue inputQueue;
    private TextField receivedText;
    private TextField yourNameText;

    // GUIUpdater tries to get data from the Programs inputQueue and updates the GUI when data arrives

    GUIUpdater(SynchronizedQueue q, TextField text, TextField name) {
        inputQueue = q;
        receivedText = text;
        yourNameText = name;
    }

    public void run() {
        Thread.currentThread().setName("GUIUpdater Thread");

        while (!Thread.interrupted()) {
            // Try to get 2 strings from the inputQueue
            String sender = (String)inputQueue.get();
            while (sender == null) {
                Thread.currentThread().yield();
                sender = (String)inputQueue.get();
            }
            String finalSender = sender;

            String message = (String)inputQueue.get();
            while (message == null) {
                Thread.currentThread().yield();
                message = (String)inputQueue.get();
            }
            String finalMessage = message;

            if (!sender.equals(yourNameText.getText())) {
                // Got a string... update the GUI with it
                Platform.runLater(() -> receivedText.setText(finalSender + ": " + finalMessage));
            }
        }
    }
}
