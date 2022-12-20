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

    static Sonic SONIC;
    private static float SPEED = 1.0F;
    private static float PITCH = 1.0F;
    private static final float RATE = 1.0f;
    private static float VOLUME = 0.69F;
    private static final boolean EMULATE_CHORD_PITCH = false;
    private static final int QUALITY = 0;



    void speak(String input) {
        // Check if there is any input to speak, otherwise return
        if (input.isEmpty()) {
            return;
        } else if (!punctuation.contains(input.subSequence(input.length()-1, input.length()))) {
            //If there is input, check if it ends in punctuation, if it doesn't, add a period
            //this causes MaryTTS to behave more predictably when speaking as it sees a finished sentence
            input = input + ".";
        }

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
                runSonic(speechStream, line,
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
            TTSApplication.running.set(false);
        }).start();
    }

    // Run sonic.
    private void runSonic(
            AudioInputStream audioStream,
            SourceDataLine line,
            int sampleRate,
            int numChannels) throws IOException
    {
        SONIC = new Sonic(sampleRate, numChannels);
        int bufferSize = line.getBufferSize();
        byte[] inBuffer = new byte[bufferSize];
        byte[] outBuffer = new byte[bufferSize];
        int numRead, numWritten;

        SONIC.setSpeed(TTSUtils.SPEED);
        SONIC.setPitch(TTSUtils.PITCH);
        SONIC.setRate(TTSUtils.RATE);
        SONIC.setVolume(TTSUtils.VOLUME);
        SONIC.setChordPitch(TTSUtils.EMULATE_CHORD_PITCH);
        SONIC.setQuality(TTSUtils.QUALITY);
        TTSApplication.running.set(true);
        do {
            if (STOP || !TTSApplication.running.get()) { TTSApplication.running.set(false); STOP=false; return; }
            numRead = audioStream.read(inBuffer, 0, bufferSize);
            if(numRead <= 0) {
                SONIC.flushStream();
            } else {
                SONIC.writeBytesToStream(inBuffer, numRead);
            }
            do {
                numWritten = SONIC.readBytesFromStream(outBuffer, bufferSize);
                if(numWritten > 0) {
                    line.write(outBuffer, 0, numWritten);
                }
            } while(numWritten > 0);
        } while(numRead > 0);
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
        TTSUtils.VOLUME = value / 100;
        SONIC.setVolume(value / 100);
    }

    public void setSpeed(float value) {
        //we don't want the user to be able to set speed to 0, so we cap it to 1 behind the scenes
        float speed = (value == 0.0F) ? 1 : value;
        speed = speed / 100;
        TTSUtils.SPEED = speed;
        SONIC.setSpeed(speed);
    }

    public void setPitch(float value) {
        //we don't want the user to be able to set pitch to 0, so we cap it to 1 behind the scenes
        float pitch = (value == 0.0F) ? 1 : value;
        pitch = pitch / 100;
        TTSUtils.PITCH = pitch;
        SONIC.setPitch(pitch);
    }

    public static MaryInterface getMaryInstance() {
        return mary;
    }
}
