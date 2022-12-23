package org.quad.plex;

import javafx.application.Platform;
import javafx.stage.Stage;
import marytts.LocalMaryInterface;
import marytts.MaryInterface;
import marytts.exceptions.MaryConfigurationException;
import marytts.exceptions.SynthesisException;
import org.apache.commons.lang.ArrayUtils;
import ws.schild.jave.*;

import javax.sound.sampled.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

import static java.lang.Thread.sleep;

public class TTSUtils {

    static boolean REVERSE_AUDIO = false;
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

    static Sonic sonic;
    private static float speed = 1.0F;
    private static float pitch = 1.0F;
    private static final float RATE = 1.0f;
    private static float volume = 0.69F;
    private static boolean emulateChordPitch = false;
    private static boolean playContinuously = false;
    private static boolean exportAsMp3 = true;

    static void speak(String input) {
        // Check if there is any input to speak, otherwise return
        input = sanitizeInput(input);
        if (input == null) return;

        String finalInput = input;
        new Thread(() -> {
            TTSApplication.running.set(true);
            SourceDataLine line = null;
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

                byte[] audioData = convertStreamToByteArray(speechStream);

                if (REVERSE_AUDIO) {
                    reverseAudioData(audioData);
                }

                audioData = trimAudioData(audioData);

                do {
                    AudioInputStream loopStream = new AudioInputStream(new ByteArrayInputStream(audioData), targetFormat, audioData.length / targetFormat.getFrameSize());
                    SourceDataLine.Info info = new DataLine.Info(SourceDataLine.class, targetFormat);
                    line = (SourceDataLine) AudioSystem.getLine(info);

                    line.open(targetFormat);
                    line.start();

                    runSonic(loopStream, line,
                            (int) targetFormat.getSampleRate(),
                            targetFormat.getChannels(),
                            0,
                            false);

                    line.drain();
                    line.stop();
                } while (playContinuously && TTSApplication.running.get());
            } catch (SynthesisException ex) {
                System.err.println("Error speaking text: " + ex.getMessage());
                ex.printStackTrace();
            } catch (LineUnavailableException | IOException e) {
                throw new RuntimeException(e);
            } catch (Exception e) {
                System.out.println("General exception occurred while speaking: " + e.getMessage());
                TTSApplication.error.set(true);
            } finally {
                TTSApplication.running.set(false);
                assert line != null;
                line.drain();
                line.stop();
            }
        }).start();
    }

    static void reverseAudioData(byte[] audioData) {
        //First flip around all the individual frames in the byte stream
        //then read the array from the back, which leaves the frames themselves intact
        for (int i = 0; i < audioData.length; i += 2) {
            byte temp = audioData[i];
            audioData[i] = audioData[i + 1];
            audioData[i + 1] = temp;
        }
        ArrayUtils.reverse(audioData);
    }

    static byte[] convertStreamToByteArray(AudioInputStream speechStream) throws IOException {
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
        return audioData;
    }

    static String sanitizeInput(String input) {
        if (input.isEmpty()) {
            return null;
        } else if (!punctuation.contains(input.subSequence(input.length()-1, input.length()))) {
            //If there is input, check if it ends in punctuation, if it doesn't, add a period
            //this causes MaryTTS to behave more predictably when speaking as it sees a finished sentence
            input = input + ".";
        }
        if(Objects.equals(mary.getLocale().getDisplayName(), Locale.forLanguageTag("ru").getDisplayName())){
            input = CyrillicLatinConverter.latinToCyrillic(input);
        }
        return input;
    }

    static byte[] trimAudioData(byte[] audioData) {
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
        if (playContinuously) {
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
        return Arrays.copyOfRange(audioData, start - 1, end + 1);
    }

    // Run sonic.
    static byte[] runSonic(
            AudioInputStream audioStream,
            SourceDataLine line,
            int sampleRate,
            int numChannels,
            int quality,
            boolean export) throws IOException
    {
        sonic = new Sonic(sampleRate, numChannels);
        byte[] inBuffer = new byte[sampleRate];
        byte[] outBuffer = new byte[sampleRate];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int numRead, numWritten;

        sonic.setSpeed(TTSUtils.speed);
        sonic.setPitch(TTSUtils.pitch);
        sonic.setRate(TTSUtils.RATE);
        sonic.setVolume(TTSUtils.volume);
        sonic.setChordPitch(TTSUtils.emulateChordPitch);
        sonic.setQuality(quality);
        do {
            if (STOP || !TTSApplication.running.get()) {
                STOP = false;
                TTSApplication.running.set(false);
                break;
            }
            numRead = audioStream.read(inBuffer, 0, sampleRate);
            if(numRead <= 0) {
                sonic.flushStream();
            } else {
                sonic.writeBytesToStream(inBuffer, numRead);
            }
            do {
                numWritten = sonic.readBytesFromStream(outBuffer, sampleRate);
                if(numWritten > 0) {
                    if (!export) {
                        line.write(outBuffer, 0, numWritten);
                    } else {
                        // Append the data read from the stream to the 'exportData' array
                        baos.write(outBuffer, 0, numWritten);
                    }
                }
            } while(numWritten > 0);
        } while(numRead > 0);
        return baos.toByteArray();
    }

    static void gracefulShutdown(Stage ttsStage) {
        // Exit gracefully
        STOP = true;
        playContinuously = false;
        File wavFile = new File("temp\\export.wav");
        File mp3File = new File("temp\\export.mp3");
        if (wavFile.exists()) {
            boolean success = wavFile.delete();
            if (!success) {
                System.err.println("Failed to delete exported .wav file!");
            }
        }
        if (mp3File.exists()) {
            boolean success = mp3File.delete();
            if (!success) {
                System.err.println("Failed to delete exported .mp3 file!");
            }
        }

        mary.setLocale(Locale.US);
        speak("Goodbye!");
        try {
            sleep(690);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
        ttsStage.close();
    }

    static void exportAudioToClipboard(String input) {
        try {
            input = TTSUtils.sanitizeInput(input);
            if (input == null) {
                Platform.runLater(() -> TTSApplication.createModalPopupWindow(TTSApplication.popupWindowState.NOINPUT));
                return;
            }


            // Generate the audio data for the given text
            AudioInputStream audio = mary.generateAudio(input);

            AudioFormat sourceFormat = audio.getFormat();
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
                audio = AudioSystem.getAudioInputStream(targetFormat, audio);
            }

            byte[] audioData = TTSUtils.convertStreamToByteArray(audio);

            audioData = TTSUtils.trimAudioData(audioData);

            if (TTSUtils.REVERSE_AUDIO) {
                TTSUtils.reverseAudioData(audioData);
            }

            AudioInputStream audioStream = new AudioInputStream(new ByteArrayInputStream(audioData),targetFormat, audioData.length / targetFormat.getFrameSize());

            TTSUtils.setStop(false);
            TTSApplication.running.set(true);
            byte[] exportData = runSonic(audioStream, null,
                    (int) targetFormat.getSampleRate(),
                    targetFormat.getChannels(),
                    1,
                    true);
            TTSApplication.running.set(false);

            AudioInputStream exportStream = new AudioInputStream(new ByteArrayInputStream(exportData),targetFormat, exportData.length / targetFormat.getFrameSize());

            // Write the audio data to a file in the WAV format
            File exportedFile = new File("temp\\export.wav");
            AudioSystem.write(exportStream, AudioFileFormat.Type.WAVE, exportedFile);

            if (exportAsMp3) {
                // Set up the audio attributes for the conversion
                AudioAttributes audioAttributes = new AudioAttributes();
                audioAttributes.setCodec("libmp3lame");
                audioAttributes.setBitRate(128000);
                audioAttributes.setChannels(2);
                audioAttributes.setSamplingRate(44100);

                // Set up the encoding attributes
                EncodingAttributes attrs = new EncodingAttributes();
                attrs.setFormat("mp3");
                attrs.setAudioAttributes(audioAttributes);

                // Create a new encoder
                FFMPEGLocator locator = new LocalFFMPEGLocator(new File("ffmpeg\\ffmpeg.exe"));
                Encoder encoder = new Encoder(locator);
                File targetFile = new File("temp\\export.mp3");
                MultimediaObject source = new MultimediaObject(exportedFile, locator);

                // Perform the conversion
                try {
                    encoder.encode(source, targetFile, attrs);
                    System.out.println("Conversion complete!");
                } catch (EncoderException e) {
                    System.out.println("An error occurred: " + e.getMessage());
                }
                if (exportedFile.exists()) {
                    boolean success = exportedFile.delete();
                    if (!success) {
                        System.err.println("Failed to delete exported .wav file!");
                    }
                }
                exportedFile = targetFile;
            }

            // Get the system clipboard and set the contents to the file that we just created
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            Transferable transferable = new FileTransferable(exportedFile);
            clipboard.setContents(transferable, null);
            Platform.runLater(() -> TTSApplication.createModalPopupWindow(TTSApplication.popupWindowState.SUCCESS));
        } catch (Exception ex) {
            ex.printStackTrace();
            Platform.runLater(() -> TTSApplication.createModalPopupWindow(TTSApplication.popupWindowState.EXCEPTION));
        }
    }

    public static void setVolume(float value) {
        volume = value / 100;
        sonic.setVolume(value / 100);
    }

    public static void setSpeed(float value) {
        //we don't want the user to be able to set speed to 0, so we cap it to 2 behind the scenes
        float speed = (value == 0.0F) ? 2 : value;
        speed = speed / 100;
        TTSUtils.speed = speed;
        sonic.setSpeed(speed);
    }

    public static void setPitch(float value) {
        //we don't want the user to be able to set pitch to 0, so we cap it to 1 behind the scenes
        float pitch = (value == 0.0F) ? 1 : value;
        pitch = pitch / 100;
        TTSUtils.pitch = pitch;
        sonic.setPitch(pitch);
    }

    public static void setChordPitchEnabled(boolean enabled) {
        sonic.setChordPitch(enabled);
        emulateChordPitch = enabled;
    }

    public static void setPlayContinuously(boolean enabled) {
        playContinuously = enabled;
    }

    public static void setReverseAudio(boolean enabled) {
        REVERSE_AUDIO = enabled;
    }

    public static void setStop(boolean stop) {
        STOP = stop;
    }

    public static void setExportAsMp3(boolean enabled) {
        exportAsMp3 = enabled;
    }

    public static MaryInterface getMaryInstance() {
        return mary;
    }
}
