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
import javafx.util.StringConverter;
import marytts.MaryInterface;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.net.URL;
import java.util.*;

public class TTSApplication extends Application {
    private MaryInterface mary;

    private static TTSUtils ttsUtils;
    private static final URL infoIconUrl = TTSApplication.class.getResource("/info.png");
    private static final URL mainIconUrl = TTSApplication.class.getResource("/icon.png");

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
        textArea.setFont(new Font("Verdana", 13));
        textArea.setStyle("-fx-border-color: black;");
        HBox textBox = new HBox();
        textBox.setAlignment(Pos.CENTER);
        textBox.setPadding(new Insets(10,10,10,10));
        textBox.getChildren().add(textArea);

        int volumeSetting = 69;
        Slider volumeSlider = new Slider(0, 250, volumeSetting);
        volumeSlider.setOrientation(Orientation.VERTICAL);
        volumeSlider.setPadding(new Insets(40,10,30,0));
        volumeSlider.setScaleY(1.3);
        volumeSlider.setScaleX(1.3);
        volumeSlider.setSnapToTicks(true);
        volumeSlider.setPrefHeight(500);
        volumeSlider.setMajorTickUnit(25);
        volumeSlider.setMinorTickCount(1);
        volumeSlider.setShowTickMarks(true);
        volumeSlider.setShowTickLabels(true);

        // create a StringConverter to convert the slider values to percentage labels
        StringConverter<Double> percentageConverter = new StringConverter<>() {
            @Override
            public String toString(Double value) {
                // format the label as a percentage
                return String.format("%d%%", value.intValue());
            }
            @Override
            public Double fromString(String string) { return null; }
        };

        // set the label formatter for the major tick marks
        volumeSlider.setLabelFormatter(percentageConverter);

        int speedSetting = 100;
        Slider speedSlider = new Slider(0, 500, speedSetting);
        speedSlider.setOrientation(Orientation.VERTICAL);
        speedSlider.setPadding(new Insets(40,10,30,10));
        speedSlider.setScaleY(1.3);
        speedSlider.setScaleX(1.3);
        speedSlider.setSnapToTicks(true);
        speedSlider.setPrefHeight(500);
        speedSlider.setMajorTickUnit(50);
        speedSlider.setMinorTickCount(10);
        speedSlider.setShowTickMarks(true);
        speedSlider.setShowTickLabels(true);

        // create a StringConverter to convert the slider values to "x.xx" labels
        StringConverter<Double> multiplierConverter = new StringConverter<>() {
            @Override
            public String toString(Double value) {
                // format the label as "x.xx"
                return String.format("%.2fx", value / 100);
            }

            @Override
            public Double fromString(String string) { return null; }
        };
        // set the label formatter for the major tick marks
        speedSlider.setLabelFormatter(multiplierConverter);

        int pitchSetting = 100;
        Slider pitchSlider = new Slider(0, 500, pitchSetting);
        pitchSlider.setOrientation(Orientation.VERTICAL);
        pitchSlider.setPadding(new Insets(40,20,30,10));
        pitchSlider.setScaleY(1.3);
        pitchSlider.setScaleX(1.3);
        pitchSlider.setSnapToTicks(true);
        pitchSlider.setPrefHeight(500);
        pitchSlider.setMajorTickUnit(50);
        pitchSlider.setMinorTickCount(10);
        pitchSlider.setShowTickMarks(true);
        pitchSlider.setShowTickLabels(true);
        pitchSlider.setLabelFormatter(multiplierConverter);

        Label volumeLabel = new javafx.scene.control.Label("Volume");
        volumeLabel.setFont(new Font("Verdana", 13));
        Label speedLabel = new javafx.scene.control.Label("Speed");
        speedLabel.setFont(new Font("Verdana", 13));
        speedLabel.setPadding(new Insets(0,0,0,10));
        Label pitchLabel = new javafx.scene.control.Label("Pitch");
        pitchLabel.setPadding(new Insets(0,0,0,10));
        pitchLabel.setFont(new Font("Verdana", 13));

        VBox volumeBox = new VBox();
        volumeBox.setSpacing(10);
        volumeBox.setPadding(new Insets(0,0,10,10));
        volumeBox.getChildren().add(volumeSlider);
        volumeBox.getChildren().add(volumeLabel);

        //create a panel for the speed slider and label
        VBox speedBox = new VBox();
        speedBox.setSpacing(10);
        speedBox.setPadding(new Insets(0,0,10,10));
        speedBox.getChildren().add(speedSlider);
        speedBox.getChildren().add(speedLabel);

        //create a panel for the rate slider and label
        VBox pitchBox = new VBox();
        pitchBox.setSpacing(10);
        pitchBox.setPadding(new Insets(0,0,10,10));
        pitchBox.getChildren().add(pitchSlider);
        pitchBox.getChildren().add(pitchLabel);

        HBox sliderBox = new HBox();
        sliderBox.getChildren().add(volumeBox);
        sliderBox.getChildren().add(speedBox);
        sliderBox.getChildren().add(pitchBox);

        Button speakButton = new Button("Speak!");
        speakButton.setPrefSize(85, 32);
        speakButton.setFont(new Font("Verdana", 12));
        VBox speakButtonBox = new VBox();
        speakButtonBox.getChildren().add(speakButton);
        speakButtonBox.setPadding(new Insets(16,0,0,0));

        Button exportButton = new Button("Export");
        exportButton.setPrefSize(85, 32);
        exportButton.setFont(new Font("Verdana", 12));
        VBox exportButtonBox = new VBox();
        exportButtonBox.getChildren().add(exportButton);
        exportButtonBox.setPadding(new Insets(16,0,0,0));

        Locale[] languages = mary.getAvailableLocales().toArray(new Locale[0]);
        Arrays.sort(languages, Comparator.comparing(Locale::getDisplayName));

        // create a map that maps the display names to the Locale objects
        Map<String, Locale> displayNameToLocaleMap = new HashMap<>();
        for (Locale locale : languages) {
            displayNameToLocaleMap.put(locale.getDisplayName(), locale);
        }

        ComboBox<String> languageComboBox = new ComboBox<>(FXCollections.observableArrayList(displayNameToLocaleMap.keySet()));
        languageComboBox.setPrefSize(125, 32);
        languageComboBox.setStyle("-fx-font-family: Verdana; -fx-font-size: 12;");
        languageComboBox.getSelectionModel().select(1);
        Label languageLabel = new Label();
        languageLabel.setText("Language:");
        languageLabel.setFont(new Font("Verdana", 10));
        languageLabel.setPadding(new Insets(0,0,4,0));
        VBox languageBox = new VBox();
        languageBox.getChildren().addAll(languageLabel, languageComboBox);

        String[] voices = mary.getAvailableVoices(mary.getLocale()).toArray(new String[0]);
        ComboBox<String> voiceComboBox = new ComboBox<>(FXCollections.observableArrayList(voices));
        voiceComboBox.setPrefSize(135, 32);
        voiceComboBox.setStyle("-fx-font-family: Verdana; -fx-font-size: 12;");
        voiceComboBox.getSelectionModel().select(0);
        Label voiceLabel = new Label();
        voiceLabel.setText("Voice:");
        voiceLabel.setFont(new Font("Verdana", 10));
        voiceLabel.setPadding(new Insets(0,0,4,0));
        VBox voiceBox = new VBox();
        voiceBox.getChildren().addAll(voiceLabel, voiceComboBox);

        CheckBox chordCheckBox = new CheckBox("Chord Pitch");
        chordCheckBox.setWrapText(true);
        chordCheckBox.setPadding(new Insets(15,0,0,0));
        // Add an action listener to the checkbox
        chordCheckBox.setOnAction(event -> ttsUtils.setChordPitchEnabled(chordCheckBox.isSelected()));

        HBox buttonBox = new HBox();
        buttonBox.setSpacing(10);
        buttonBox.setPadding(new Insets(0,0,10,10));
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(speakButtonBox, exportButtonBox, languageBox, voiceBox, chordCheckBox);

        BorderPane root = new BorderPane();
        root.setTop(titleBox);
        root.setCenter(textBox);
        root.setRight(sliderBox);
        root.setBottom(buttonBox);

        Scene scene = new Scene(root);
        ttsStage.setTitle("Shitty AI-Generated TTS v0.420.69");
        assert mainIconUrl != null;
        ttsStage.getIcons().add(new Image(mainIconUrl.toString()));
        ttsStage.setHeight(420);
        ttsStage.setWidth(530);
        ttsStage.setResizable(false);
        ttsStage.centerOnScreen();
        ttsStage.setScene(scene);
        ttsStage.show();

        volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> ttsUtils.setVolume(newValue.intValue()));

        speedSlider.valueProperty().addListener((observable, oldValue, newValue) -> ttsUtils.setSpeed(newValue.intValue()));

        pitchSlider.valueProperty().addListener((observable, oldValue, newValue) -> ttsUtils.setPitch(newValue.intValue()));

        languageComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            // get the Locale object for the selected display name
            Locale selectedLocale = displayNameToLocaleMap.get(newValue);
            mary.setLocale(selectedLocale);
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
                String input = textArea.getText();
                ttsUtils.speak(input);
            } else {
                TTSUtils.STOP = true;
                //calling speak again here is just a workaround. Sometimes the audio playback gets stuck when using the
                //sliders while audio is playing. Just setting the 'STOP' variable won't stop the playback in this case,
                //as something in the Sonic thread gets stuck. Calling speak again wakes this tread up, so that it can
                //recognize that the STOP value is set, and terminate the Stream normally
                ttsUtils.speak("stopping");
            }
        });

        exportButton.setOnAction(e -> {
            String input = textArea.getText();
            exportAudioToClipboard(input);
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

        //shitty workaround; the comboboxes don't close automatically the first time they're used
        //to select an item. Closing one of them once clears this behavior, for some reason,
        //so we just open and close one here, which can't even be seen when the program opens
        languageComboBox.show();
        languageComboBox.hide();
    }

    private void exportAudioToClipboard(String input) {
        try {
            if (input.isEmpty()) {
                createPopupWindow(false);
                return;
            }
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
            createPopupWindow(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void createPopupWindow(boolean success) {
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

        Label messageLabel;
        // Create the message label
        if (success) {
            messageLabel = new Label("Successfully copied speech to clipboard!");
        } else {
            messageLabel = new Label("Enter some text to be synthesized first!");
        }
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