package com.dc3.applet.LEDSign;

import java.applet.Applet;
import java.applet.AppletContext;
import java.applet.AppletStub;
import java.applet.AudioClip;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class AppletStubImpl implements AppletStub {
    private final Map<String, String> params = new HashMap<>();
    private final java.applet.Applet applet;

    public AppletStubImpl(java.applet.Applet applet) {
        this.applet = applet;
    }

    public void setParameter(String name, String value) {
        params.put(name, value);
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public URL getDocumentBase() {
        // 1) If running from project root, prefer the resources folder on disk
        try {
            Path p = Paths.get(System.getProperty("user.dir"), "src", "main", "resources", "com", "dc3", "applet", "LEDSign");
            if (Files.exists(p)) {
                return p.toUri().toURL();
            }
        } catch (Exception ignored) {}

        // 2) Fallback to classpath resource directory so resources bundled in the jar are found
        URL res = AppletStubImpl.class.getResource("/com/dc3/applet/LEDSign/");
        if (res != null) return res;
        try {
            return new URL("file:" + System.getProperty("user.dir") + "/");
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public URL getCodeBase() {
        return getDocumentBase();
    }

    @Override
    public String getParameter(String name) {
        return params.get(name);
    }

    @Override
    public AppletContext getAppletContext() {
        return new AppletContext() {
            @Override
            public AudioClip getAudioClip(URL url) { return null; }
            @Override
            public Image getImage(URL url) { return null; }
            @Override
            public Applet getApplet(String name) { return null; }
            @Override
            public Enumeration<Applet> getApplets() { return null; }
            @Override
            public void showDocument(URL url) {
                try {
                    if (java.awt.Desktop.isDesktopSupported()) {
                        java.awt.Desktop.getDesktop().browse(new URI(url.toString()));
                    }
                } catch (Exception ignored) { }
            }
            @Override
            public void showDocument(URL url, String target) { showDocument(url); }
            @Override
            public void showStatus(String status) { System.out.println("Status: " + status); }
            @Override
            public void setStream(String key, InputStream stream) throws IOException {}
            @Override
            public InputStream getStream(String key) { return null; }
            @Override
            public java.util.Iterator<String> getStreamKeys() { return null; }
        };
    }

    @Override
    public void appletResize(int width, int height) {
        applet.resize(width, height);
    }
}
