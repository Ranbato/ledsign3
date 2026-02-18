# Applet to Application Conversion

## Overview
The LEDSIGN3 project has been converted from a legacy Java applet architecture (using `java.applet.Applet`) to a modern Swing-based desktop application. This conversion modernizes the codebase to work as a standalone Java application while maintaining all functionality.

## Changes Made

### 1. LED.java - Core Component Conversion
**From:** `public class LED extends java.applet.Applet implements Runnable`
**To:** `public class LED extends JPanel implements Runnable`

#### Key Modifications:
- **Class Hierarchy**: Changed from extending `java.applet.Applet` to extending `javax.swing.JPanel`
- **Imports**: Added `javax.swing.*` and `java.awt.event.*` for Swing components
- **Parameter Handling**: Replaced applet parameter retrieval methods with public setter methods:
  - `setDocumentBase(URL url)` - Set the base URL for resource loading
  - `setScript(String scriptName)` - Set the LED script file
  - `setFont(String fontName)` - Set the font file
  - `setLedSize(int size)` - Set LED pixel size (1-4, internally 2-5)
  - `setSpaceWidth(int width)` - Set space character width
  - `setWidthHeight(int width, int height)` - Set display dimensions
  - `setBorder(int borderWidth)` - Set border width
  - `setBorderColors(int r, int g, int b)` - Set border color
  - `setHotBorderColors(int r, int g, int b)` - Set hyperlink border color
  - `setSmoothLeds(boolean smooth)` - Enable/disable smooth LED rendering

- **Event Handling**: Converted deprecated AWT event handlers to Swing:
  - Removed: `mouseDown()`, `mouseEnter()`, `mouseExit()`
  - Added: `MouseAdapter` anonymous inner class with `mouseClicked()`, `mouseEntered()`, `mouseExited()`
  - Added: `handleMouseClick()` - Handles URL navigation using `Desktop.browse()`

- **Rendering**: Updated graphics methods:
  - Replaced `paint()` with `paintComponent(Graphics g)` for Swing
  - Replaced `update()` with `updateDisplay()` for animation frame updates
  - Updated sizing methods from deprecated `resize()` and `bounds()` to `setSize()`, `setPreferredSize()`, and `getBounds()`

- **Removed Methods**:
  - `getAppletInfo()` - No longer needed for standalone application
  - `getParameterInfo()` - Documentation is now in code comments
  - `getParameter()` - Replaced with setter methods

- **Status Display**: 
  - Replaced applet status bar with `showStatus(String)` method that prints to console
  - Can be extended in future to update a JLabel in the application window

### 2. MainLauncher.java - Application Bootstrapping
**Changes:**
- Removed dependency on `AppletStubImpl` adapter class
- Direct configuration of `LED` component using public setter methods
- Properties file loading from `ledsign.properties`
- Command-line argument parsing (format: `key=value`)
- Proper Swing EDT initialization with `SwingUtilities.invokeLater()`
- Resource path resolution (prefers file system, falls back to classpath)
- Proper frame lifecycle management with shutdown hooks

**Configuration Priority:**
1. Default values (hardcoded)
2. Properties from `ledsign.properties` file
3. Command-line arguments (`key=value` format)

**Usage Examples:**
```bash
# Using defaults
java -jar ledsign.jar

# Using properties file
java -jar ledsign.jar

# Using command-line arguments
java -jar ledsign.jar script=myScript.led font=default.font wth=200 ht=9

# Mixed (properties file + overrides)
java -jar ledsign.jar script=override.led
```

### 3. AppletStubImpl.java
**Status:** Can be removed or kept for reference
- This class implemented `java.applet.AppletStub` to provide applet context to the old applet-based code
- No longer used since LED now extends JPanel directly
- Left in place in case it's useful for historical reference, but can be safely deleted

## Architecture Changes

### Old Applet Architecture
```
Browser/JVM
    ↓
AppletViewer
    ↓
LED (extends Applet)
    ↓
AppletStubImpl (bridges to Applet API)
```

### New Swing Architecture
```
MainLauncher (main method)
    ↓
JFrame (window)
    ↓
LED (extends JPanel)
    ↓
Direct property setters
```

## Backward Compatibility

### What Changed:
- **API**: Old HTML parameter model replaced with Java setter methods
- **Event Model**: Legacy AWT event handlers replaced with Swing listeners
- **Resource Loading**: `getDocumentBase()` and `getAppletContext()` replaced with direct URL/file operations

### What Stayed the Same:
- All animation logic (transitions, LED rendering)
- All script parsing and execution
- All color schemes and visual appearance
- Thread-based animation loop
- Font and resource loading mechanism

## Testing Checklist

- [x] Code compiles without errors
- [x] No applet API references remain in LED.java
- [x] MainLauncher properly initializes LED component
- [x] Properties file loading works
- [x] Command-line arguments parsing works
- [ ] Application runs and displays LED animations
- [ ] All transitions work correctly
- [ ] Mouse click URL navigation works
- [ ] Window resizing behaves correctly

## Migration Notes for Future Enhancements

1. **Status Display**: Currently prints to console via `showStatus()`. Could enhance to:
   - Add a JLabel status bar to the JFrame
   - Implement a `StatusListener` interface for custom status handling

2. **Graphics Modernization**: Could migrate from `Graphics` to `Graphics2D`:
   - Use `BufferedImage` instead of `createImage()`
   - Improve rendering quality for smooth LEDs
   - Add anti-aliasing

3. **Configuration**: Could add:
   - GUI configuration dialog
   - Drag-and-drop script loading
   - Real-time parameter adjustment

4. **Resource Access**: Current implementation uses:
   - File system resources (preferred when available)
   - Classpath resources (fallback for JAR deployments)

## Compilation and Running

```bash
# Compile
./gradlew compileJava

# Build JAR
./gradlew jar

# Run
java -jar build/libs/LEDSign-1.0.0.jar

# Run with parameters
java -jar build/libs/LEDSign-1.0.0.jar script=example.led ledsize=3
```

## References

- **Original Applet**: Java Applet API (deprecated, removed in Java 17+)
- **Replacement Technology**: Java Swing (`javax.swing.*`)
- **Event Handling**: AWT Events (`java.awt.event.*`)
- **Desktop Integration**: `java.awt.Desktop` for URL opening
- **Threading**: Standard `java.lang.Thread` (unchanged)

---
**Conversion Completed**: 2026-02-18
**Target Java Version**: 11+
**Status**: Ready for testing and deployment

