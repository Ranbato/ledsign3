package com.dc3.applet.LEDSign;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LEDFunctionTest {

    @Test
    void fromScriptName_shouldMapKnownNames() {
        assertEquals(LEDFunction.APPEAR, LEDFunction.fromScriptName(LEDFunction.APPEAR.getScriptName()));
        assertEquals(LEDFunction.SLEEP, LEDFunction.fromScriptName(LEDFunction.SLEEP.getScriptName()));
        assertEquals(LEDFunction.SCROLL_LEFT, LEDFunction.fromScriptName(LEDFunction.SCROLL_LEFT.getScriptName()));
        assertEquals(LEDFunction.PIXEL, LEDFunction.fromScriptName(LEDFunction.PIXEL.getScriptName()));
        assertEquals(LEDFunction.DO, LEDFunction.fromScriptName(LEDFunction.DO.getScriptName()));
        assertEquals(LEDFunction.REPEAT, LEDFunction.fromScriptName(LEDFunction.REPEAT.getScriptName()));
    }

    @Test
    void fromScriptName_shouldReturnNullForUnknown() {
        assertNull(LEDFunction.fromScriptName("NotAFunction"));
    }

    @Test
    void fromCode_shouldMapKnownCodes() {
        assertEquals(LEDFunction.APPEAR, LEDFunction.fromCode(0));
        assertEquals(LEDFunction.RELOAD, LEDFunction.fromCode(99));
        assertEquals(LEDFunction.CHAIN, LEDFunction.fromCode(100));
    }
}
