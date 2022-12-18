package org.quad.plex;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

import javax.sound.sampled.*;
import javax.swing.*;

import marytts.LocalMaryInterface;
import marytts.MaryInterface;
import marytts.exceptions.MaryConfigurationException;
import marytts.exceptions.SynthesisException;

import javax.swing.border.Border;

import static java.lang.Thread.sleep;

public class Main extends KeyAdapter {

    private int volumeSetting = 15;
    private final MaryInterface mary;

    public static void main(String[] args) throws Exception {
        Main main = new Main();
        main.run();
    }

    public Main() throws MaryConfigurationException {
        // Initialize the MaryTTS library
        mary = new LocalMaryInterface();
        System.out.println(mary.getAvailableVoices());
    }

    public void run() {
        createFrame();
        speak("Shitty T T S version 0.4 20.69 initialized.", volumeSetting);
    }

    private void createFrame() {
        // Create a window and add a key listener
        JFrame frame = new JFrame("Shitty AI-Generated TTS v0.420.69");
        frame.setSize(420, 320); // Set the size to be a little bigger
        frame.setMinimumSize(new Dimension(420, 320));
        frame.setLocationRelativeTo(null); // Center the window on the screen
        frame.setLayout(new BorderLayout());

        JLabel title = new JLabel("Shitty Text to Speech Program go brrr");
        title.setHorizontalAlignment(0);

        // Add a text input field and a volume slider to the panel
        JTextArea textArea = new JTextArea("Text to speak here");
        textArea.setLineWrap(true);
        Border border = BorderFactory.createLineBorder(Color.BLACK);
        textArea.setBorder(BorderFactory.createCompoundBorder(border, BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        //create a dummy panel for the textarea to give it an extra border
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BorderLayout());
        textPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        textPanel.add(textArea);

        JSlider volumeSlider = new JSlider(JSlider.VERTICAL, 0, 30, volumeSetting);
        volumeSlider.setMajorTickSpacing(5);
        volumeSlider.setMinorTickSpacing(1);
        volumeSlider.setPaintTicks(true);
        volumeSlider.setPaintLabels(true);

        JLabel volumeLabel = new JLabel("Volume");
        volumeLabel.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));

        //create a panel for the volume slider and label
        JPanel volumePanel = new JPanel();
        volumePanel.setLayout(new BorderLayout());
        volumePanel.setBorder(BorderFactory.createEmptyBorder(0,10,0,20));
        volumePanel.add(volumeSlider, BorderLayout.NORTH);
        volumePanel.add(volumeLabel, BorderLayout.SOUTH);

        // Add the buttons to the frame
        JButton speakButton = new JButton("Speak!");
        speakButton.setPreferredSize(new Dimension(100, 30)); // Make the button a little bigger

        JButton exportButton = new JButton("Export");
        exportButton.setPreferredSize(new Dimension(100, 30));

        JRadioButton englishButton = new JRadioButton("English");
        englishButton.setActionCommand("english");
        englishButton.setSelected(true); // English is the default selected locale

        JRadioButton germanButton = new JRadioButton("German");
        germanButton.setActionCommand("german");

        // Add the buttons to a ButtonGroup to ensure that only one button can be selected at a time
        ButtonGroup group = new ButtonGroup();
        group.add(englishButton);
        group.add(germanButton);

        //create a panel for the buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));
        buttonPanel.add(speakButton);
        buttonPanel.add(exportButton);
        buttonPanel.add(englishButton);
        buttonPanel.add(germanButton);

        //add the panels to the frame
        frame.add(title, BorderLayout.NORTH);
        frame.add(textPanel, BorderLayout.CENTER);
        frame.add(volumePanel, BorderLayout.EAST);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        volumeSlider.addChangeListener(e -> {
            JSlider src = (JSlider) e.getSource();
            if (src.getValueIsAdjusting()) return;
            volumeSetting = src.getValue();
        });

        englishButton.addActionListener(e -> mary.setLocale(Locale.US));

        germanButton.addActionListener(e -> mary.setLocale(Locale.GERMAN));

        exportButton.addActionListener(e -> {
            String input = textArea.getText();
            try {
                // Generate the audio data for the given text
                AudioInputStream audio = mary.generateAudio(input);

                // Write the audio data to a file in the WAV format
                File wavFile = new File("output.wav");
                AudioSystem.write(audio, AudioFileFormat.Type.WAVE, wavFile);

                // Get the system clipboard and set the contents to the file that we just created
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                Transferable transferable = new FileTransferable(wavFile);
                clipboard.setContents(transferable, null);
                JPanel messagePanel = new JPanel();

                //Show a toast message
                messagePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
                JLabel messageLabel = new JLabel("Successfully copied speech to clipboard");
                messagePanel.add(messageLabel);
                JOptionPane.showMessageDialog(null, messagePanel, "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // Add a key listener to the text field to listen for the enter key
        textArea.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && !e.isShiftDown()) {
                    String input = textArea.getText();
                    speak(input, volumeSetting);
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    gracefulShutdown();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                // Not needed for this example
            }

            @Override
            public void keyTyped(KeyEvent e) {
                // Not needed for this example
            }
        });

        // Add an action listener to the button
        speakButton.addActionListener(e -> {
            String input = textArea.getText();
            speak(input, volumeSetting);
        });

        // Add a window listener to handle the window closing event
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                gracefulShutdown();
            }
        });

        frame.setVisible(true); // Call setVisible() after all the components have been added
    }

    private void gracefulShutdown() {
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

    private void speak(String input, float volume) {
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
}
