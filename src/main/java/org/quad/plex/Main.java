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

    private int volumeSetting = 15;
    private int speedSetting = 1;
    private static final String punctuation = ".,:-!?";
    private final MaryInterface mary = new LocalMaryInterface();

    public Main() throws MaryConfigurationException {
        org.quad.plex.guiConfigurator guiConfigurator = new guiConfigurator(this);
        guiConfigurator.createFrame();
        speak("Shitty T T S version 0.4 20.69 initialized.", volumeSetting, speedSetting);
    }

    public static void main(String[] args) throws Exception {
        new Main();
    }

    void gracefulShutdown() {
        // Exit gracefully
        mary.setLocale(Locale.US);
        speak("Goodbye!", volumeSetting, speedSetting);
        try {
            sleep(1600);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
        System.exit(0);
    }

    void speak(String input, float volume, float speed) {
        if (volume == 0) return;

        // Check if the input string already ends with a punctuation mark
        if (!punctuation.contains(input.subSequence(input.length()-1, input.length()))) {
            // If not, add a period to the end of the string
            input = input + ".";
        }

        String finalInput = input;
        new Thread(() -> {
            try {
                // Generate audio data for the input text
                AudioInputStream audio = mary.generateAudio(finalInput);

                // Get the audio format
                AudioFormat format = audio.getFormat();

                // Calculate the new sample rate based on the desired speed
                float newSampleRate = format.getSampleRate() * speed;

                // Create a new audio format with the modified sample rate
                AudioFormat newFormat = new AudioFormat(format.getEncoding(), newSampleRate, format.getSampleSizeInBits(),
                        format.getChannels(), format.getFrameSize(), newSampleRate,
                        format.isBigEndian());

                // Create a new audio input stream with the modified audio format
                AudioInputStream newAudio = AudioSystem.getAudioInputStream(newFormat, audio);

                // Create a source data line
                SourceDataLine line = AudioSystem.getSourceDataLine(newFormat);
                line.open(newFormat);
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
                    numBytesRead = newAudio.read(data, 0, data.length);
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

    public int getSpeedSetting() {
        return speedSetting;
    }

    public void setSpeedSetting(int value) {
        speedSetting = value;
    }
}
