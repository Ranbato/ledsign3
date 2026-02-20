package com.dc3.applet.LEDSign;

import javax.swing.*;
import java.awt.*;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class MainLauncher {
    public static void main(String[] args) {
        // Load defaults from properties file if present, then apply CLI key=value pairs
        Properties props = new Properties();
        try {
            if (Files.exists(Paths.get("ledsign.properties"))) {
                try (InputStream in = new FileInputStream("ledsign.properties")) {
                    props.load(in);
                }
            }
        } catch (Exception e) {
            System.out.println("Warning: failed to read ledsign.properties: " + e.getMessage());
        }

        // Parse CLI arguments of form key=value
        for (String a : args) {
            if (a.contains("=")) {
                String k = a.substring(0, a.indexOf('='));
                String v = a.substring(a.indexOf('=') + 1);
                props.setProperty(k, v);
            }
        }

        final Properties finalProps = props;

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("LEDSign");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            LED ledPanel = new LED();

            // Set configuration from properties using public setters
            try {
                // Determine document base (for resource loading)
                URL documentBase = Paths.get(System.getProperty("user.dir"),
                    "src", "main", "resources", "com", "dc3", "applet", "LEDSign").toUri().toURL();
                ledPanel.setDocumentBase(documentBase);
            } catch (Exception e) {
                try {
                    // Fallback to classpath resources
                    URL res = LED.class.getResource("/com/dc3/applet/LEDSign/");
                    if (res != null) {
                        ledPanel.setDocumentBase(res);
                    }
                } catch (Exception ex) {
                    System.err.println("Failed to determine document base: " + ex.getMessage());
                }
            }

            // Set required parameters
            ledPanel.setScript(finalProps.getProperty("script", "example.led"));
            ledPanel.setFont(finalProps.getProperty("font", "default.font"));

            // Set optional parameters with defaults
            int ledSize = Integer.parseInt(finalProps.getProperty("ledsize", "2"));
            ledPanel.setLedSize(ledSize);

            int border = Integer.parseInt(finalProps.getProperty("border", "2"));
            ledPanel.setBorder(border);

            int width = Integer.parseInt(finalProps.getProperty("wth", "200"));
            int height = Integer.parseInt(finalProps.getProperty("ht", "9"));
            ledPanel.setWidthHeight(width, height);

            int spaceWidth = Integer.parseInt(finalProps.getProperty("spacewidth", "3"));
            ledPanel.setSpaceWidth(spaceWidth);

            // Set border colors if provided
            String borderColor = finalProps.getProperty("bordercolor");
            if (borderColor != null && !borderColor.isEmpty()) {
                try {
                    String[] parts = borderColor.split(",");
                    if (parts.length == 3) {
                        int r = Integer.parseInt(parts[0].trim());
                        int g = Integer.parseInt(parts[1].trim());
                        int b = Integer.parseInt(parts[2].trim());
                        ledPanel.setBorderColors(r, g, b);
                    }
                } catch (Exception e) {
                    System.out.println("Warning: Invalid bordercolor format: " + borderColor);
                }
            }

            // Set hot border colors if provided
            String hotBorderColor = finalProps.getProperty("hot_bordercolor");
            if (hotBorderColor != null && !hotBorderColor.isEmpty()) {
                try {
                    String[] parts = hotBorderColor.split(",");
                    if (parts.length == 3) {
                        int r = Integer.parseInt(parts[0].trim());
                        int g = Integer.parseInt(parts[1].trim());
                        int b = Integer.parseInt(parts[2].trim());
                        ledPanel.setHotBorderColors(r, g, b);
                    }
                } catch (Exception e) {
                    System.out.println("Warning: Invalid hot_bordercolor format: " + hotBorderColor);
                }
            }

            // Set smooth LEDs if provided
            String smoothLeds = finalProps.getProperty("smooth_leds", "true");
            ledPanel.setSmoothLeds(smoothLeds.equalsIgnoreCase("true"));

            // Add to frame
            frame.getContentPane().add(ledPanel, BorderLayout.CENTER);

            // Call lifecycle methods
            try {
                ledPanel.init();
            } catch (Throwable t) {
                System.err.println("Error during init: ");
                t.printStackTrace();
                System.exit(1);
            }
            try {
                ledPanel.start();
            } catch (Throwable t) {
                System.err.println("Error during start: ");
                t.printStackTrace();
                System.exit(1);
            }

            // Set frame properties and display
            ledPanel.setPreferredSize(new Dimension(
                Integer.parseInt(finalProps.getProperty("wth", "200")) * (Integer.parseInt(finalProps.getProperty("ledsize", "2")) + 1) + 2 * 3 * Integer.parseInt(finalProps.getProperty("border", "2")),
                Integer.parseInt(finalProps.getProperty("ht", "9")) * (Integer.parseInt(finalProps.getProperty("ledsize", "2")) + 1) + 2 * 3 * Integer.parseInt(finalProps.getProperty("border", "2"))
            ));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            // Add shutdown hook to cleanly stop the LED thread
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    ledPanel.stop();
                } catch (Throwable ignored) {}
            }));
        });
    }
}
