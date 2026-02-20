package com.dc3.applet.LEDSign;

/**
 * Enumeration of LED sign transition functions.
 * Maps function names to their numeric codes for script parsing and execution.
 */
public enum LEDFunction {
    APPEAR(0, "Appear"),
    SLEEP(1, "Sleep"),
    SCROLL_LEFT(2, "ScrollLeft"),
    SCROLL_RIGHT(3, "ScrollRight"),
    SCROLL_UP(4, "ScrollUp"),
    SCROLL_DOWN(5, "ScrollDown"),
    PIXEL(6, "Pixel"),
    BLINK(7, "Blink"),
    OVER_RIGHT(8, "OverRight"),
    SCROLL_CENTER(9, "ScrollCenter"),
    OVER_CENTER(10, "OverCenter"),
    OVER_LEFT(11, "OverLeft"),
    OVER_UP(12, "OverUp"),
    OVER_DOWN(13, "OverDown"),
    DO(97, "Do"),
    REPEAT(98, "Repeat"),
    RELOAD(99, "Reload"),
    CHAIN(100, "Chain");

    private final int code;
    private final String name;

    LEDFunction(int code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * Get the numeric code for this function.
     * @return the function code
     */
    public int getCode() {
        return code;
    }

    /**
     * Get the script name for this function.
     * @return the function name as it appears in LED scripts
     */
    public String getScriptName() {
        return name;
    }

    /**
     * Look up a function by its script name.
     * @param scriptName the name as it appears in LED scripts
     * @return the corresponding LEDFunction, or null if not found
     */
    public static LEDFunction fromScriptName(String scriptName) {
        if (scriptName == null) {
            return null;
        }
        for (LEDFunction func : LEDFunction.values()) {
            if (func.name.equals(scriptName)) {
                return func;
            }
        }
        return null;
    }

    /**
     * Look up a function by its numeric code.
     * @param code the function code
     * @return the corresponding LEDFunction, or null if not found
     */
    public static LEDFunction fromCode(int code) {
        for (LEDFunction func : LEDFunction.values()) {
            if (func.code == code) {
                return func;
            }
        }
        return null;
    }
}

