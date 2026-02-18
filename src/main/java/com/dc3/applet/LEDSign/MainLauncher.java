package com.dc3.applet.LEDSign;

import javax.swing.*;
import java.awt.*;
import java.io.FileInputStream;
import java.io.InputStream;
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

            LED applet = new LED();

            // Prepare stub and default parameters (from example.html)
            AppletStubImpl stub = new AppletStubImpl(applet);

            // Default values if not provided
            stub.setParameter("ledsize", finalProps.getProperty("ledsize", "2"));
            stub.setParameter("script", finalProps.getProperty("script", "example.led"));
            stub.setParameter("border", finalProps.getProperty("border", "2"));
            stub.setParameter("font", finalProps.getProperty("font", "default.font"));
            stub.setParameter("wth", finalProps.getProperty("wth", "200"));
            stub.setParameter("ht", finalProps.getProperty("ht", "9"));

            // Apply any additional properties to the stub
            for (String name : finalProps.stringPropertyNames()) {
                stub.setParameter(name, finalProps.getProperty(name));
            }

            applet.setStub(stub);

            frame.getContentPane().add(applet, BorderLayout.CENTER);

            // Call lifecycle
            try { applet.init(); } catch (Throwable t) { t.printStackTrace(); }
            try { applet.start(); } catch (Throwable t) { t.printStackTrace(); }

            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try { applet.stop(); } catch (Throwable ignored) {}
                try { applet.destroy(); } catch (Throwable ignored) {}
            }));
        });
    }
}
