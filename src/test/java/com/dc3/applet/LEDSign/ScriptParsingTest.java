package com.dc3.applet.LEDSign;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.InputStream;

public class ScriptParsingTest {

    @Test
    void parseSimpleScript_shouldReturnAppearFunc() throws Exception {
        // Copy resource test.led to a temp directory so Script can open it via URL
        try (InputStream in = this.getClass().getResourceAsStream("/com/dc3/applet/LEDSign/test.led")) {
            assertNotNull(in, "Test script resource not found");

            Path tmpDir = Files.createTempDirectory("ledsign-test");
            tmpDir.toFile().deleteOnExit();
            Path tmpFile = tmpDir.resolve("test.led");
            Files.copy(in, tmpFile);
            tmpFile.toFile().deleteOnExit();

            URL dirUrl = tmpDir.toUri().toURL();

            Script script = new Script(dirUrl, "test.led");

            FuncInfo fi = script.nextFunc();
            assertNotNull(fi);
            assertEquals(LEDFunction.APPEAR, fi.func);
            assertEquals("Hello", fi.text.trim());
        }
    }
}
