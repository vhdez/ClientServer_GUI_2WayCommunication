package org.sla;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

public class GUIUpdater implements Runnable {
    private SynchronizedQueue inputQueue;
    private ListView allReceivedText;
    private TextField yourNameText;

    // GUIUpdater tries to get data from the Programs inputQueue and updates the GUI when data arrives

    GUIUpdater(SynchronizedQueue q, ListView allTexts, TextField name) {
        inputQueue = q;
        allReceivedText = allTexts;
        yourNameText = name;
    }

    public void run() {
        Thread.currentThread().setName("GUIUpdater Thread");

        while (!Thread.interrupted()) {
            // Try to get a Message from the inputQueue
            Message message = (Message)inputQueue.get();
            while (message == null) {
                Thread.currentThread().yield();
                message = (Message)inputQueue.get();
            }
            Message finalMessage = message; // needed for Platform.runLater()

            if (!finalMessage.sender().equals(yourNameText.getText())) {
                // Got a message from another client... prepend the chat with it
                Platform.runLater(() -> allReceivedText.getItems().add(0, new Label(finalMessage.sender() + " says \"" + finalMessage.data() + "\"")));
            }
        }
    }
}
