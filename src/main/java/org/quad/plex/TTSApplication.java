package org.quad.plex;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import marytts.MaryInterface;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.net.URL;
import java.util.Locale;

public class TTSApplication extends Application {
    private MaryInterface mary;

    private static TTSUtils ttsUtils;
    private static final URL infoIconUrl = TTSApplication.class.getResource("/info.png");

    public static BooleanProperty running = new SimpleBooleanProperty();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage ttsStage) {
        ttsUtils = new TTSUtils();
        mary = TTSUtils.getMaryInstance();

        Label title = new javafx.scene.control.Label("Shitty Text to Speech Program go brrr");
        title.setFont(new Font("Verdana", 15));
        title.setStyle("-fx-font-weight: bold");
        HBox titleBox = new HBox();
        titleBox.setAlignment(Pos.CENTER);
        titleBox.getChildren().add(title);

        // Add a text input field
        TextArea textArea = new javafx.scene.control.TextArea("Text to speak here");
        textArea.setWrapText(true);
        textArea.setStyle("-fx-border-color: black;");
        HBox textBox = new HBox();
        textBox.setAlignment(Pos.CENTER);
        textBox.setPadding(new Insets(10,10,10,10));
        textBox.getChildren().add(textArea);

        int volumeSetting = 69;
        Slider volumeSlider = new Slider(0, 200, volumeSetting);
        volumeSlider.setOrientation(Orientation.VERTICAL);
        volumeSlider.setPrefHeight(500);
        volumeSlider.setMajorTickUnit(25);
        volumeSlider.setMinorTickCount(1);
        volumeSlider.setShowTickMarks(true);
        volumeSlider.setShowTickLabels(true);

        int speedSetting = 100;
        Slider speedSlider = new Slider(1, 500, speedSetting);
        speedSlider.setOrientation(Orientation.VERTICAL);
        speedSlider.setPrefHeight(500);
        volumeSlider.setMajorTickUnit(50);
        volumeSlider.setMinorTickCount(10);
        volumeSlider.setShowTickMarks(true);
        volumeSlider.setShowTickLabels(true);

        int pitchSetting = 100;
        Slider pitchSlider = new Slider(1, 500, pitchSetting);
        pitchSlider.setOrientation(Orientation.VERTICAL);
        pitchSlider.setPrefHeight(500);
        volumeSlider.setMajorTickUnit(50);
        volumeSlider.setMinorTickCount(10);
        volumeSlider.setShowTickMarks(true);
        volumeSlider.setShowTickLabels(true);

        Label volumeLabel = new javafx.scene.control.Label("Volume");
        Label speedLabel = new javafx.scene.control.Label("Speed");
        Label pitchLabel = new javafx.scene.control.Label("Pitch");

        VBox volumeBox = new VBox();
        volumeBox.setSpacing(10);
        volumeBox.setPadding(new Insets(10,10,10,10));
        volumeBox.getChildren().add(volumeSlider);
        volumeBox.getChildren().add(volumeLabel);

        //create a panel for the speed slider and label
        VBox speedBox = new VBox();
        speedBox.setSpacing(10);
        speedBox.setPadding(new Insets(10,10,10,10));
        speedBox.getChildren().add(speedSlider);
        speedBox.getChildren().add(speedLabel);

        //create a panel for the rate slider and label
        VBox pitchBox = new VBox();
        pitchBox.setSpacing(10);
        pitchBox.setPadding(new Insets(10,10,10,10));
        pitchBox.getChildren().add(pitchSlider);
        pitchBox.getChildren().add(pitchLabel);

        HBox sliderBox = new HBox();
        sliderBox.getChildren().add(volumeBox);
        sliderBox.getChildren().add(speedBox);
        sliderBox.getChildren().add(pitchBox);

        Button speakButton = new Button("Speak!");
        speakButton.setPrefSize(75, 30);

        Button exportButton = new Button("Export");
        exportButton.setPrefSize(75, 30);

        Locale[] languages = mary.getAvailableLocales().toArray(new Locale[0]);
        ComboBox<Locale> languageComboBox = new ComboBox<>(FXCollections.observableArrayList(languages));
        languageComboBox.setPrefSize(80, 30);
        languageComboBox.getSelectionModel().select(1);

        String[] voices = mary.getAvailableVoices(mary.getLocale()).toArray(new String[0]);
        ComboBox<String> voiceComboBox = new ComboBox<>(FXCollections.observableArrayList(voices));
        voiceComboBox.setPrefSize(120, 30);
        voiceComboBox.getSelectionModel().select(0);

        HBox buttonBox = new HBox();
        buttonBox.setSpacing(10);
        buttonBox.setPadding(new Insets(0,0,10,10));
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().add(speakButton);
        buttonBox.getChildren().add(exportButton);
        buttonBox.getChildren().add(languageComboBox);
        buttonBox.getChildren().add(voiceComboBox);

        BorderPane root = new BorderPane();
        root.setTop(titleBox);
        root.setCenter(textBox);
        root.setRight(sliderBox);
        root.setBottom(buttonBox);

        Scene scene = new Scene(root);
        ttsStage.setTitle("Shitty AI-Generated TTS v0.420.69");
        ttsStage.setHeight(420);
        ttsStage.setWidth(500);
        ttsStage.setResizable(false);
        ttsStage.centerOnScreen();
        ttsStage.setScene(scene);
        ttsStage.show();

        volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> ttsUtils.setVolume(newValue.intValue()));

        speedSlider.valueProperty().addListener((observable, oldValue, newValue) -> ttsUtils.setSpeed(newValue.intValue()));

        pitchSlider.valueProperty().addListener((observable, oldValue, newValue) -> ttsUtils.setPitch(newValue.intValue()));

        languageComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            mary.setLocale(newValue);
            updateVoiceSelection(voiceComboBox);
            languageComboBox.hide();
        });

        voiceComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                mary.setVoice(newValue);
            }
            voiceComboBox.hide();
        });

        running.addListener((observable, oldValue, newValue) -> {
            if(newValue.equals(true))
                Platform.runLater(() -> speakButton.setText("Running..."));
            else
                Platform.runLater(() -> speakButton.setText("Speak!"));
        });

        // Add an action listener to the button
        speakButton.setOnAction(e -> {
            if (!running.get()) {
                speakButton.setText("Running...");
                String input = textArea.getText();
                ttsUtils.speak(input);
            } else {
                speakButton.setText("Speak!");
                TTSUtils.STOP = true;
            }
        });

        exportButton.setOnAction(e -> {
            String input = textArea.getText();
            try {
                // Generate the audio data for the given text
                AudioInputStream audio = mary.generateAudio(input);

                // Write the audio data to a file in the WAV format
                File wavFile = new File("temp\\export.wav");
                AudioSystem.write(audio, AudioFileFormat.Type.WAVE, wavFile);

                // Get the system clipboard and set the contents to the file that we just created
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                Transferable transferable = new FileTransferable(wavFile);
                clipboard.setContents(transferable, null);

                //Show a toast message
                createPopupWindow();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        textArea.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER && event.isControlDown() && !event.isShiftDown()) {
                String input = textArea.getText();
                ttsUtils.speak(input);
            } else if (event.getCode() == KeyCode.ESCAPE) {
                ttsUtils.gracefulShutdown(ttsStage);
            }
        });

        ttsStage.setOnCloseRequest(e -> {
            e.consume();
            ttsUtils.gracefulShutdown(ttsStage);
        });

        ttsUtils.speak("Shitty T T S version 0.4 20.69 initialized.");
    }

    private void createPopupWindow() {
        // Create a new Stage for the popup window
        Stage popupWindow = new Stage();
        popupWindow.setAlwaysOnTop(true);
        assert infoIconUrl != null;
        popupWindow.getIcons().add(new Image(infoIconUrl.toString()));

        // Set the window's title
        popupWindow.setTitle("Info");

        // Set the window's size
        popupWindow.setWidth(230);
        popupWindow.setHeight(130);

        // Create the message label
        Label messageLabel = new Label("Successfully copied speech to clipboard!");
        messageLabel.setMaxWidth(130);
        messageLabel.setWrapText(true);
        messageLabel.setPadding(new Insets(10, 10, 10, 10)); // Add some padding around the label
        messageLabel.setTextAlignment(TextAlignment.CENTER); // Center the label's text

        // Create the close button
        Button closeButton = new Button("Close");
        closeButton.setAlignment(Pos.CENTER_RIGHT);
        closeButton.setOnAction(event -> popupWindow.close()); // Close the window when the button is clicked

        // Create the icon
        ImageView infoIcon = new ImageView(new Image(infoIconUrl.toString()));
        infoIcon.setFitWidth(50);
        infoIcon.setFitHeight(50);

        // Create the layout
        VBox closeButtonBox = new VBox();
        closeButtonBox.getChildren().addAll(messageLabel, closeButton);
        closeButtonBox.setAlignment(Pos.BOTTOM_RIGHT);
        closeButtonBox.setPadding(new Insets(0,30,10,0));

        HBox messageBox = new HBox();
        messageBox.getChildren().addAll(infoIcon, closeButtonBox);
        messageBox.setAlignment(Pos.CENTER); // Center the layout
        messageBox.setPadding(new Insets(10, 10, 10, 10)); // Add some padding around the layout

        BorderPane popupRoot = new BorderPane();
        popupRoot.setLeft(messageBox);
        popupRoot.setRight(closeButtonBox);

        // Set the scene for the window and show it
        Scene popupScene = new Scene(popupRoot);
        popupWindow.setScene(popupScene);
        popupWindow.show();
    }


    private void updateVoiceSelection(ComboBox<String> voiceComboBox) {
        // Create a new ObservableList with the updated contents
        String[] newVoices = mary.getAvailableVoices(mary.getLocale()).toArray(new String[0]);
        ObservableList<String> list = FXCollections.observableArrayList(newVoices);

        // Set the items of the ComboBox to the new ObservableList
        voiceComboBox.setItems(list);
        voiceComboBox.getSelectionModel().select(0);
        voiceComboBox.hide();
    }

}