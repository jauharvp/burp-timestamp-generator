package com.example; 

import burp.*; // Burp Suite imports
import java.awt.*; // For GUI components
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import javax.swing.*; // For Swing components

public class BurpExtender implements IBurpExtender, ITab {

    private IBurpExtenderCallbacks callbacks;
    private JPanel panel;
    private JLabel unixResult; // Store reference to the timestamp label

    @Override
    public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks) {
        this.callbacks = callbacks;
        callbacks.setExtensionName("Time Stamp Generator");

        // Use FlowLayout for a more compact arrangement
        panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(
            BorderFactory.createTitledBorder("Unix Timestamp Converter")
        );

        // Button to generate current Unix timestamp
        JButton unixButton = new JButton("Gen Timestamp");
        unixButton.setPreferredSize(new Dimension(120, 25)); // Set a smaller preferred size
        unixResult = new JLabel("Timestamp: ");
        unixResult.setPreferredSize(new Dimension(150, 25)); // Set a smaller preferred size

        unixButton.addActionListener(e -> {
            long timestamp = Instant.now().getEpochSecond();
            unixResult.setText("Timestamp: " + timestamp);
        });

        // Date picker using JSpinner for time selection
        JLabel dateLabel = new JLabel("Select Date: ");
        JSpinner datePicker = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(
            datePicker,
            "yyyy-MM-dd"
        );
        datePicker.setEditor(dateEditor);
        datePicker.setValue(new java.util.Date()); // Set current date
        datePicker.setPreferredSize(new Dimension(120, 25)); // Smaller size for date picker

        JLabel timeLabel = new JLabel("Select Time: ");
        String[] hours = new String[24];
        String[] minutes = new String[60];
        for (int i = 0; i < 24; i++) {
            hours[i] = String.format("%02d", i); // Formatting to 2 digits
        }
        for (int i = 0; i < 60; i++) {
            minutes[i] = String.format("%02d", i); // Formatting to 2 digits
        }

        JComboBox<String> hourComboBox = new JComboBox<>(hours);
        hourComboBox.setPreferredSize(new Dimension(50, 25)); // Smaller size for hour combo
        JComboBox<String> minuteComboBox = new JComboBox<>(minutes);
        minuteComboBox.setPreferredSize(new Dimension(50, 25)); // Smaller size for minute combo

        // Set current hour and minute as default values
        LocalDateTime now = LocalDateTime.now();
        hourComboBox.setSelectedItem(String.format("%02d", now.getHour()));
        minuteComboBox.setSelectedItem(String.format("%02d", now.getMinute()));

        JLabel conversionResult = new JLabel("Unix Timestamp: ");
        conversionResult.setPreferredSize(new Dimension(150, 25)); // Set a smaller preferred size
        JButton convertButton = new JButton("Convert");
        convertButton.setPreferredSize(new Dimension(80, 25)); // Set a smaller preferred size

        convertButton.addActionListener(e -> {
            try {
                // Get selected date and time
                LocalDateTime localDateTime = LocalDateTime.ofInstant(
                    ((java.util.Date) datePicker.getValue()).toInstant(),
                    ZoneOffset.UTC
                );
                int hour = Integer.parseInt(
                    (String) hourComboBox.getSelectedItem()
                );
                int minute = Integer.parseInt(
                    (String) minuteComboBox.getSelectedItem()
                );
                localDateTime = localDateTime.withHour(hour).withMinute(minute);

                long timestamp = localDateTime.toEpochSecond(ZoneOffset.UTC);
                conversionResult.setText("Unix Timestamp: " + timestamp);
            } catch (Exception ex) {
                conversionResult.setText("Invalid input!");
            }
        });

        // Button to copy the generated Unix timestamp
        JButton copyButton = new JButton("Copy Timestamp");
        copyButton.setPreferredSize(new Dimension(120, 25)); // Set a smaller preferred size
        copyButton.addActionListener(e -> {
            // Extract the timestamp from the unixResult label
            String text = unixResult
                .getText()
                .replace("Timestamp: ", "")
                .trim(); // Extract the timestamp and trim whitespace

            if (!text.isEmpty()) {
                // Create a selection object for the clipboard
                StringSelection selection = new StringSelection(text);
                Clipboard clipboard = Toolkit.getDefaultToolkit()
                    .getSystemClipboard();
                clipboard.setContents(selection, null); // Set the clipboard contents

                // Show confirmation message
                JOptionPane.showMessageDialog(
                    panel,
                    "Timestamp copied to clipboard!",
                    "Copied",
                    JOptionPane.INFORMATION_MESSAGE
                );
            } else {
                // Show warning message if there is no timestamp to copy
                JOptionPane.showMessageDialog(
                    panel,
                    "No timestamp to copy!",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE
                );
            }
        });

        // Add components to the panel
        panel.add(unixButton);
        panel.add(unixResult);
        panel.add(dateLabel);
        panel.add(datePicker);
        panel.add(timeLabel);
        panel.add(hourComboBox);
        panel.add(new JLabel(" : ")); // Add a separator
        panel.add(minuteComboBox);
        panel.add(convertButton);
        panel.add(conversionResult);
        panel.add(copyButton); // Add the copy button to the panel

        // Add the UI panel to Burp Suite
        callbacks.addSuiteTab(this);
    }

    @Override
    public String getTabCaption() {
        return "Time Stamp Generator";
    }

    @Override
    public Component getUiComponent() {
        return panel;
    }
}
