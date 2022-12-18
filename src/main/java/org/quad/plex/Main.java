package org.quad.plex;

import java.awt.event.*;
import java.io.IOException;
import java.util.Locale;

import javax.sound.sampled.*;

import marytts.LocalMaryInterface;
import marytts.MaryInterface;
import marytts.exceptions.MaryConfigurationException;
import marytts.exceptions.SynthesisException;

import static java.lang.Thread.sleep;

public class Main extends KeyAdapter {

    private final org.quad.plex.guiConfigurator guiConfigurator = new guiConfigurator(this);
    private int volumeSetting = 15;
    private final MaryInterface mary;

    public static void main(String[] args) throws Exception {
        Main main = new Main();
        main.run();
    }

    public Main() throws MaryConfigurationException {
        // Initialize the MaryTTS library
        this.mary = new LocalMaryInterface();
        System.out.println(mary.getAvailableVoices());
    }

    public void run() {
        guiConfigurator.createFrame();
        speak("Shitty T T S version 0.4 20.69 initialized.", volumeSetting);
    }

    void gracefulShutdown() {
        // Exit gracefully
        mary.setLocale(Locale.US);
        speak("Have a shitty day motherfucker!", volumeSetting);
        try {
            sleep(1600);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
        System.exit(0);
    }

    void speak(String input, float volume) {
        if (volume == 0) return;
        // Check if the input string already ends with a punctuation mark
        String punctuation = ".,:-!?";
        if (!punctuation.contains(input.subSequence(input.length()-1, input.length()))) {
            // If not, add a period to the end of the string
            input = input + ".";
        }

        String finalInput = input;
        new Thread(() -> {
            try {
                // Generate audio data for the input text
                AudioInputStream audio = mary.generateAudio(finalInput);

                // Create a source data line
                SourceDataLine line = AudioSystem.getSourceDataLine(audio.getFormat());
                line.open(audio.getFormat());
                line.start();

                // Set the volume of the source data line
                Control gainControl = line.getControl(FloatControl.Type.MASTER_GAIN);
                if (gainControl instanceof FloatControl floatControl) {
                    floatControl.setValue(volume-24);
                }

                // Write the audio data to the source data line
                int numBytesRead = 0;
                byte[] data = new byte[line.getBufferSize() / 5];
                while (numBytesRead != -1) {
                    numBytesRead = audio.read(data, 0, data.length);
                    if (numBytesRead >= 0) {
                        line.write(data, 0, numBytesRead);
                    }
                }

                // Close the source data line
                line.drain();
                line.close();
            } catch (SynthesisException ex) {
                System.err.println("Error speaking text: " + ex.getMessage());
                ex.printStackTrace();
            } catch (LineUnavailableException | IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    public int getVolumeSetting() {
        return volumeSetting;
    }

    public void setVolumeSetting(int value) {
        volumeSetting = value;
    }

    public MaryInterface getMaryInstance() {
        return mary;
    }
}
