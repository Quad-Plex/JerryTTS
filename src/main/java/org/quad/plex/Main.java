package org.quad.plex;

import java.awt.event.*;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import javax.sound.sampled.*;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.embed.swing.JFXPanel;

import marytts.LocalMaryInterface;
import marytts.MaryInterface;
import marytts.exceptions.MaryConfigurationException;
import marytts.exceptions.SynthesisException;

import static java.lang.Thread.sleep;

public class Main extends KeyAdapter {
    private static final String punctuation = ".,:-!?";
    private final MaryInterface mary = new LocalMaryInterface();

    private static MediaPlayer mediaPlayer;
    private float volume = 0.69F;
    private float rate = 1.0F;

    //This panel needs to be instantiated in order for the JavaFX library to initialize
    private static final JFXPanel fxPanel = new JFXPanel();

    public Main() throws MaryConfigurationException {
        org.quad.plex.guiConfigurator guiConfigurator = new guiConfigurator(this);
        guiConfigurator.createFrame();
        speak("Shitty T T S version 0.4 20.69 initialized.");
    }

    public static void main(String[] args) throws Exception {
        new Main();
    }

    void gracefulShutdown() {
        // Exit gracefully
        mary.setLocale(Locale.US);
        speak("Goodbye!");
        try {
            sleep(1600);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
        System.exit(0);
    }

    void speak(String input) {
        // Check if the input string already ends with a punctuation mark
        if (!punctuation.contains(input.subSequence(input.length()-1, input.length()))) {
            // If not, add a period to the end of the string
            input = input + ".";
        }

        String finalInput = input;
        new Thread(() -> {
            try {
                // Generate audio data for the input text
                AudioInputStream audioInputStream = mary.generateAudio(finalInput);

                File wavFile = new File("temp\\output.wav");
                wavFile.getParentFile().mkdir();
                AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, wavFile);
                Media media = new Media(wavFile.toURI().toASCIIString());

                mediaPlayer = new MediaPlayer(media);
                mediaPlayer.setVolume(volume);
                mediaPlayer.setRate(rate);
                mediaPlayer.play();
            } catch (SynthesisException ex) {
                System.err.println("Error speaking text: " + ex.getMessage());
                ex.printStackTrace();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    public void setVolume(int value) {
        volume = (float) value / 100;
        mediaPlayer.setVolume(volume);
    }

    public void setSpeed(int value) {
        rate = (float) value / 100;
        mediaPlayer.setRate(rate);
    }

    public MaryInterface getMaryInstance() {
        return mary;
    }
}
