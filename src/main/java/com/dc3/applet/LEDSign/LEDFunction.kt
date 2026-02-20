package com.dc3.applet.LEDSign

/**
 * Enumeration of LED sign transition functions.
 * Maps function names to their numeric codes for script parsing and execution.
 */
enum class LEDFunction(
    /**
     * Get the numeric code for this function.
     * @return the function code
     */
    val code: Int,
    /**
     * Get the script name for this function.
     * @return the function name as it appears in LED scripts
     */
    val scriptName: String
) {
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

    companion object {
        /**
         * Look up a function by its script name.
         * @param scriptName the name as it appears in LED scripts
         * @return the corresponding LEDFunction, or null if not found
         */
        fun fromScriptName(scriptName: String?): LEDFunction? {
            if (scriptName == null) {
                return null
            }
            for (func in LEDFunction.entries) {
                if (func.scriptName.equals(scriptName, ignoreCase = true) ) {
                    return func
                }
            }
            return null
        }

        /**
         * Look up a function by its numeric code.
         * @param code the function code
         * @return the corresponding LEDFunction, or null if not found
         */
        fun fromCode(code: Int): LEDFunction? {
            for (func in LEDFunction.entries) {
                if (func.code == code) {
                    return func
                }
            }
            return null
        }
    }
}

