package org.quad.plex;

import javafx.stage.Stage;
import marytts.LocalMaryInterface;
import marytts.MaryInterface;
import marytts.exceptions.MaryConfigurationException;
import marytts.exceptions.SynthesisException;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

import static java.lang.Thread.sleep;

public class TTSUtils {

    public static boolean STOP = false;
    private static final String punctuation = ".,:-!?";
    private static final MaryInterface mary;

    static {
        try {
            mary = new LocalMaryInterface();
        } catch (MaryConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    private static Sonic sonic;
    private float volume = 0.69F;
    private float pitch = 1.0F;
    private float speed = 1.0F;

    void speak(String input) {
        // Check if there is any input to speak, otherwise return
        if (input.isEmpty()) {
            return;
        } else if (!punctuation.contains(input.subSequence(input.length()-1, input.length()))) {
            //If there is input, check if it ends in punctuation, if it doesn't, add a period
            //this causes MaryTTS to behave more predictably when speaking as it sees a finished sentence
            input = input + ".";
        }

        float rate = 1.0f;
        boolean emulateChordPitch = false;
        int quality = 0;

        String finalInput = input;
        new Thread(() -> {
            try {
                // Generate audio data for the input text
                AudioInputStream speechStream = mary.generateAudio(finalInput);

                AudioFormat format = speechStream.getFormat();
                int sampleRate = (int)format.getSampleRate();
                int numChannels = format.getChannels();
                SourceDataLine.Info info = new DataLine.Info(SourceDataLine.class, format,
                        ((int)speechStream.getFrameLength()*format.getFrameSize()));
                SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
                line.open(speechStream.getFormat());
                line.start();
                runSonic(speechStream, line, speed, this.pitch, rate, volume, emulateChordPitch, quality,
                        sampleRate, numChannels);
                line.drain();
                line.stop();
            } catch (SynthesisException ex) {
                System.err.println("Error speaking text: " + ex.getMessage());
                ex.printStackTrace();
            } catch (LineUnavailableException | IOException e) {
                throw new RuntimeException(e);
            } catch (Exception e) {
                System.out.println("General exception occured while speaking: " + e.getMessage());
            }
        }).start();
    }

    // Run sonic.
    private void runSonic(
            AudioInputStream audioStream,
            SourceDataLine line,
            float speed,
            float pitch,
            float rate,
            float volume,
            boolean emulateChordPitch,
            int quality,
            int sampleRate,
            int numChannels) throws IOException
    {
        sonic = new Sonic(sampleRate, numChannels);
        int bufferSize = line.getBufferSize();
        byte[] inBuffer = new byte[bufferSize];
        byte[] outBuffer = new byte[bufferSize];
        int numRead, numWritten;

        sonic.setSpeed(speed);
        sonic.setPitch(pitch);
        sonic.setRate(rate);
        sonic.setVolume(volume);
        sonic.setChordPitch(emulateChordPitch);
        sonic.setQuality(quality);
        TTSApplication.running.set(true);
        do {
            if (STOP || !TTSApplication.running.get()) { TTSApplication.running.set(false); STOP=false; return; }
            numRead = audioStream.read(inBuffer, 0, bufferSize);
            if(numRead <= 0) {
                sonic.flushStream();
            } else {
                sonic.writeBytesToStream(inBuffer, numRead);
            }
            do {
                numWritten = sonic.readBytesFromStream(outBuffer, bufferSize);
                if(numWritten > 0) {
                    line.write(outBuffer, 0, numWritten);
                }
            } while(numWritten > 0);
        } while(numRead > 0);
        TTSApplication.running.set(false);
    }

    void gracefulShutdown(Stage ttsStage) {
        // Exit gracefully
        File wavFile = new File("temp\\export.wav");
        if (wavFile.exists()) {
            boolean success = wavFile.delete();
            if (!success) {
                System.err.println("Failed to delete exported .wav file!");
            }
        }

        mary.setLocale(Locale.US);
        speak("Have a shitty day motherfucker!");
        try {
            sleep(1500);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
        ttsStage.close();
        STOP = true;
    }

    public void setVolume(float value) {
        volume = value / 100;
        sonic.setVolume(volume);
    }

    public void setSpeed(float value) {
        speed = value / 100;
        sonic.setSpeed(speed);
    }

    public void setPitch(float value) {
        pitch = value / 100;
        sonic.setPitch(pitch);
    }

    public static MaryInterface getMaryInstance() {
        return mary;
    }
}
