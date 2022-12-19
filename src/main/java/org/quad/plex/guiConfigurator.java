package org.quad.plex;

import marytts.MaryInterface;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.io.File;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Locale;

public class guiConfigurator {
    private final Main main;
    private final MaryInterface mary;

    public guiConfigurator(Main main) {
        this.main = main;
        this.mary = main.getMaryInstance();
    }

    void createFrame() {
        // Create a window and add a key listener
        JFrame frame = new JFrame("Shitty AI-Generated TTS v0.420.69");
        frame.setSize(500, 350); // Set the size to be a little bigger
        frame.setMinimumSize(new Dimension(500, 350));
        frame.setLocationRelativeTo(null); // Center the window on the screen
        frame.setLayout(new BorderLayout());

        JLabel title = new JLabel("Shitty Text to Speech Program go brrr");
        title.setHorizontalAlignment(0);
        title.setBorder(BorderFactory.createEmptyBorder(5,0,0,0));

        // Add a text input field and a volume slider to the panel
        JTextArea textArea = new JTextArea("Text to speak here");
        textArea.setLineWrap(true);
        Border border = BorderFactory.createLineBorder(Color.BLACK);
        textArea.setBorder(BorderFactory.createCompoundBorder(border, BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        //create a dummy panel for the textarea to give it an extra border
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BorderLayout());
        textPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        textPanel.add(textArea);

        int volumeSetting = 69;
        JSlider volumeSlider = new JSlider(JSlider.VERTICAL, 0, 100, volumeSetting);
        volumeSlider.setMajorTickSpacing(10);
        volumeSlider.setMinorTickSpacing(1);
        volumeSlider.setPaintTicks(true);
        volumeSlider.setPaintLabels(true);

        int speedSetting = 100;
        JSlider speedSlider = new JSlider(JSlider.VERTICAL, 0, 300, speedSetting);
        Dictionary<Integer, JLabel> labels = new Hashtable<>();
        labels.put(0, new JLabel("0x"));
        labels.put(50, new JLabel("0.5x"));
        labels.put(100, new JLabel("1x"));
        labels.put(150, new JLabel("1.5x"));
        labels.put(200, new JLabel("2x"));
        labels.put(250, new JLabel("2.5x"));
        labels.put(300, new JLabel("3x"));
        speedSlider.setLabelTable(labels);
        speedSlider.setMajorTickSpacing(50);
        speedSlider.setMinorTickSpacing(10);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);

        JLabel volumeLabel = new JLabel("Volume");
        volumeLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JLabel speedLabel = new JLabel("Speed");
        speedLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        //create a panel for the volume slider and label
        JPanel volumePanel = new JPanel();
        volumePanel.setLayout(new BorderLayout());
        volumePanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 20));
        volumePanel.add(volumeSlider, BorderLayout.NORTH);
        volumePanel.add(volumeLabel, BorderLayout.SOUTH);

        //create a panel for the speed slider and label
        JPanel speedPanel = new JPanel();
        speedPanel.setLayout(new BorderLayout());
        speedPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 20));
        speedPanel.add(speedSlider, BorderLayout.NORTH);
        speedPanel.add(speedLabel, BorderLayout.SOUTH);

        JPanel sliderPanel = new JPanel();
        sliderPanel.setLayout(new FlowLayout());
        sliderPanel.add(volumePanel);
        sliderPanel.add(speedPanel);

        // Add the buttons to the frame
        JButton speakButton = new JButton("Speak!");
        speakButton.setPreferredSize(new Dimension(75, 30)); // Make the button a little bigger

        JButton exportButton = new JButton("Export");
        exportButton.setPreferredSize(new Dimension(75, 30));

        JRadioButton englishButton = new JRadioButton("English");
        englishButton.setActionCommand("english");
        englishButton.setSelected(true); // English is the default selected locale

        JRadioButton germanButton = new JRadioButton("German");
        germanButton.setActionCommand("german");

        // Add the buttons to a ButtonGroup to ensure that only one button can be selected at a time
        ButtonGroup languagueButtonGroup = new ButtonGroup();
        languagueButtonGroup.add(englishButton);
        languagueButtonGroup.add(germanButton);

        String[] voices = mary.getAvailableVoices(mary.getLocale()).toArray(new String[0]);
        JComboBox<String> voiceComboBox = new JComboBox<>(voices);
        voiceComboBox.setSelectedIndex(0);

        //create a panel for the buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        buttonPanel.add(speakButton);
        buttonPanel.add(exportButton);
        buttonPanel.add(englishButton);
        buttonPanel.add(germanButton);
        buttonPanel.add(voiceComboBox);

        //add the panels to the frame
        frame.add(title, BorderLayout.NORTH);
        frame.add(textPanel, BorderLayout.CENTER);
        frame.add(sliderPanel, BorderLayout.EAST);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        volumeSlider.addChangeListener(e -> {
            JSlider src = (JSlider) e.getSource();
            main.setVolume(src.getValue());
        });

        speedSlider.addChangeListener(e -> {
            JSlider src = (JSlider) e.getSource();
            main.setSpeed(src.getValue());
        });

        englishButton.addActionListener(e -> {
            mary.setLocale(Locale.US);
            updateVoiceSelection(voiceComboBox);
        });

        germanButton.addActionListener(e -> {
            mary.setLocale(Locale.GERMAN);
            updateVoiceSelection(voiceComboBox);
        });

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
                if (wavFile.exists()) {
                    boolean success = wavFile.delete();
                    if (!success) {
                        System.err.println("Failed to delete exported .wav file!");
                    }
                }

                //Show a toast message
                JPanel messagePanel = new JPanel();
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
                    main.speak(input);
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    main.gracefulShutdown();
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
            main.speak(input);
        });

        voiceComboBox.addActionListener(e -> {
            String selectedVoice = (String) voiceComboBox.getSelectedItem();
            mary.setVoice(selectedVoice);
        });

        // Add a window listener to handle the window closing event
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                main.gracefulShutdown();
            }
        });

        frame.setVisible(true); // Call setVisible() after all the components have been added
    }

    private void updateVoiceSelection(JComboBox<String> voiceComboBox) {
        // Create a new ComboBoxModel with the updated contents
        String[] newVoices = mary.getAvailableVoices(mary.getLocale()).toArray(new String[0]);
        ComboBoxModel<String> model = new DefaultComboBoxModel<>(newVoices);

        // Set the model of the JComboBox to the new ComboBoxModel
        voiceComboBox.setModel(model);
    }
}