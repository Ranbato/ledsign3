package com.dc3.applet.LEDSign

import com.dc3.applet.LEDSign.LEDFunction.Companion.fromCode
import com.dc3.applet.LEDSign.LEDFunction.Companion.fromScriptName
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class LEDFunctionTest {
    @Test
    fun fromScriptName_shouldMapKnownNames() {
        Assertions.assertEquals(LEDFunction.APPEAR, fromScriptName(LEDFunction.APPEAR.scriptName))
        Assertions.assertEquals(LEDFunction.SLEEP, fromScriptName(LEDFunction.SLEEP.scriptName))
        Assertions.assertEquals(LEDFunction.SCROLL_LEFT, fromScriptName(LEDFunction.SCROLL_LEFT.scriptName))
        Assertions.assertEquals(LEDFunction.PIXEL, fromScriptName(LEDFunction.PIXEL.scriptName))
        Assertions.assertEquals(LEDFunction.DO, fromScriptName(LEDFunction.DO.scriptName))
        Assertions.assertEquals(LEDFunction.REPEAT, fromScriptName(LEDFunction.REPEAT.scriptName))
    }

    @Test
    fun fromScriptName_shouldReturnNullForUnknown() {
        Assertions.assertNull(fromScriptName("NotAFunction"))
    }

    @Test
    fun fromCode_shouldMapKnownCodes() {
        Assertions.assertEquals(LEDFunction.APPEAR, fromCode(0))
        Assertions.assertEquals(LEDFunction.RELOAD, fromCode(99))
        Assertions.assertEquals(LEDFunction.CHAIN, fromCode(100))
    }
}
