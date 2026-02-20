package com.dc3.applet.LEDSign

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.nio.file.Files

class ScriptParsingTest {
    @Test
    @Throws(Exception::class)
    fun parseSimpleScript_shouldReturnAppearFunc() {
        // Copy resource test.led to a temp directory so Script can open it via URL
        this.javaClass.getResourceAsStream("/com/dc3/applet/LEDSign/test.led").use { `in` ->
            Assertions.assertNotNull(`in`, "Test script resource not found")
            val tmpDir = Files.createTempDirectory("ledsign-test")
            tmpDir.toFile().deleteOnExit()
            val tmpFile = tmpDir.resolve("test.led")
            Files.copy(`in`, tmpFile)
            tmpFile.toFile().deleteOnExit()

            val dirUrl = tmpDir.toUri().toURL()

            val script = Script(dirUrl, "test.led")

            val fi = script.nextFunc()
            Assertions.assertNotNull(fi)
            Assertions.assertEquals(LEDFunction.APPEAR, fi!!.func)
            Assertions.assertEquals("Hello", fi.text!!.trim { it <= ' ' })
        }
    }
}
