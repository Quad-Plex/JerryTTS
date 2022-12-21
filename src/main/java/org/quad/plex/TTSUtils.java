package org.quad.plex;

import javafx.stage.Stage;
import marytts.LocalMaryInterface;
import marytts.MaryInterface;
import marytts.exceptions.MaryConfigurationException;
import marytts.exceptions.SynthesisException;
import org.apache.commons.lang.ArrayUtils;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

import static java.lang.Thread.sleep;

public class TTSUtils {

    private static boolean REVERSE_AUDIO = false;
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
    private static boolean EMULATE_CHORD_PITCH = false;
    private static boolean PLAY_CONTINUOUSLY = false;
    private static final int QUALITY = 0;



    static void speak(String input) {
        // Check if there is any input to speak, otherwise return
        if (input.isEmpty()) {
            return;
        } else if (!punctuation.contains(input.subSequence(input.length()-1, input.length()))) {
            //If there is input, check if it ends in punctuation, if it doesn't, add a period
            //this causes MaryTTS to behave more predictably when speaking as it sees a finished sentence
            input = input + ".";
        }
        if(Objects.equals(mary.getLocale().getDisplayName(), Locale.forLanguageTag("ru").getDisplayName())){
            input = CyrillicLatinConverter.latinToCyrillic(input);
        }

        String finalInput = input;
        new Thread(() -> {
            try {
                // Generate audio data for the input text
                AudioInputStream speechStream = mary.generateAudio(finalInput);

                AudioFormat sourceFormat = speechStream.getFormat();
                AudioFormat targetFormat = new AudioFormat(
                        AudioFormat.Encoding.PCM_SIGNED,
                        sourceFormat.getSampleRate(),
                        16,  // sample size in bits
                        sourceFormat.getChannels(),
                        sourceFormat.getChannels() * 2,  // frame size
                        sourceFormat.getSampleRate(),
                        false  // little-endian
                );

                if (AudioSystem.isConversionSupported(targetFormat, sourceFormat)) {
                    speechStream = AudioSystem.getAudioInputStream(targetFormat, speechStream);
                }

                byte[] audioData;
                try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    byte[] buffer = new byte[1024];
                    int read;
                    while ((read = speechStream.read(buffer, 0, buffer.length)) != -1) {
                        baos.write(buffer, 0, read);
                    }
                    audioData = baos.toByteArray();
                }
                // Close the stream
                speechStream.close();

                if (REVERSE_AUDIO) {
                    ArrayUtils.reverse(audioData);
                }

                // Trim all leading and trailing silence
                // This allows Repeating speech to repeat without pause
                // We detect the first 12 samples in the start and end of the byte array that are >125 in value
                // We use these cutoffs as the new start and end points. The audio at these extreme ends in the stream
                // isn't audible, so it just results in a more concise audiostream containing our speech
                int start = 0;
                int end = audioData.length - 1;
                int numHitsStart = 0;
                int numHitsEnd = 0;
                int numCutoff = 1;
                if (PLAY_CONTINUOUSLY) {
                    numCutoff = 5;
                }
                while (start < audioData.length && numHitsStart < numCutoff) {
                    if (audioData[start] > 125) {
                        numHitsStart++;
                    }
                    start++;
                }
                while (end >= 0 && numHitsEnd < numCutoff * 2) {
                    if (audioData[end] > 125) {
                        numHitsEnd++;
                    }
                    end--;
                }
                audioData = Arrays.copyOfRange(audioData, start - 1, end + 1);

                do {
                    AudioInputStream loopStream = new AudioInputStream(new ByteArrayInputStream(audioData),targetFormat, audioData.length / targetFormat.getFrameSize());
                    SourceDataLine.Info info = new DataLine.Info(SourceDataLine.class, targetFormat);
                    SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);

                    line.open(targetFormat);
                    line.start();

                    runSonic(loopStream, line,
                            (int) targetFormat.getSampleRate(), targetFormat.getChannels());

                    line.drain();
                    line.stop();
                } while(PLAY_CONTINUOUSLY && TTSApplication.running.get());
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
    private static void runSonic(
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
            if (STOP || !TTSApplication.running.get()) { TTSApplication.running.set(false); return; }
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

    static void gracefulShutdown(Stage ttsStage) {
        // Exit gracefully
        STOP = true;
        PLAY_CONTINUOUSLY = false;
        File wavFile = new File("temp\\export.wav");
        if (wavFile.exists()) {
            boolean success = wavFile.delete();
            if (!success) {
                System.err.println("Failed to delete exported .wav file!");
            }
        }

        mary.setLocale(Locale.US);
        speak("Goodbye!");
        try {
            sleep(500);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
        ttsStage.close();
        STOP = true;
    }

    public static void setVolume(float value) {
        VOLUME = value / 100;
        SONIC.setVolume(value / 100);
    }

    public static void setSpeed(float value) {
        //we don't want the user to be able to set speed to 0, so we cap it to 2 behind the scenes
        float speed = (value == 0.0F) ? 2 : value;
        speed = speed / 100;
        SPEED = speed;
        SONIC.setSpeed(speed);
    }

    public static void setPitch(float value) {
        //we don't want the user to be able to set pitch to 0, so we cap it to 1 behind the scenes
        float pitch = (value == 0.0F) ? 1 : value;
        pitch = pitch / 100;
        PITCH = pitch;
        SONIC.setPitch(pitch);
    }

    public static void setChordPitchEnabled(boolean enabled) {
        SONIC.setChordPitch(enabled);
        EMULATE_CHORD_PITCH = enabled;
    }

    public static void setPlayContinuously(boolean enabled) {
        PLAY_CONTINUOUSLY = enabled;
    }

    public static void setReverseAudio(boolean enabled) {
        REVERSE_AUDIO = enabled;
    }

    public static void setStop(boolean stop) {
        STOP = stop;
    }

    public static MaryInterface getMaryInstance() {
        return mary;
    }
}
