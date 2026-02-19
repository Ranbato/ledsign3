# LEDSign3 - Gradle Java Application
Vibe coding practice project!

A modernized Gradle build of the classic LEDSign3 applet project, converted from a legacy Java applet to a standalone desktop application.

## Overview

LEDSign3 displays scrolling text on a simulated LED sign with various transition effects (scroll, blink, pixel, etc.). Originally written as a Java applet in the mid-1990s, this project has been modernized to run as a standard desktop application using Gradle and Java 11+.

## Project Structure

```
LEDSIGN3/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/dc3/applet/LEDSign/
│       │       ├── LED.java              (main applet class)
│       │       ├── MainLauncher.java     (desktop entry point)
│       │       ├── AppletStubImpl.java    (applet parameter provider)
│       │       ├── Script.java           (script parser)
│       │       ├── Letters.java          (font loader)
│       │       ├── LEDMessage.java       (message renderer)
│       │       ├── FuncInfo.java         (function info)
│       │       ├── Index.java            (letter index)
│       │       └── linkList.java         (linked list for scripts)
│       └── resources/
│           └── com/dc3/applet/LEDSign/
│               ├── example.led           (example script)
│               ├── default.font          (default font file)
│               └── fonts/                (additional fonts, if provided)
├── build.gradle                         (Gradle build config)
├── settings.gradle                      (Gradle project config)
├── gradlew & gradlew.bat                (Gradle wrapper)
├── ledsign.properties                   (optional: runtime configuration)
└── README.md                            (this file)
```

## Building

### With Gradle Wrapper (Recommended)

```bash
cd LEDSIGN3
.\gradlew clean build
```

### With Installed Gradle

```bash
gradle clean build
```

## Running

### Default Run (Uses Example Script)

```bash
.\gradlew run
```

This launches the app with default parameters:
- **script**: `example.led`
- **font**: `default.font`
- **ledsize**: `2`
- **wth**: `200` (width in pixels)
- **ht**: `9` (height in pixels)
- **border**: `2`

### With Command-Line Parameters

```bash
.\gradlew run --args="script=custom.led font=tiny.font ledsize=3"
```

Parameters are passed as `key=value` pairs and override defaults.

### With Properties File

Create a `ledsign.properties` file in the working directory:

```properties
script=demo.led
font=lucida.font
ledsize=3
wth=300
ht=15
border=1
```

Then run:

```bash
.\gradlew run
```

The app will read parameters from the properties file and use them for applet configuration.

### Running the Built JAR

```bash
.\gradlew clean build
java -jar build/libs/ledsign3-1.0.0.jar
```

Or with parameters:

```bash
java -jar build/libs/ledsign3-1.0.0.jar script=test.led ledsize=2
```

## Available Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `script` | String | `example.led` | Script file (relative to resources or current dir) |
| `font` | String | `default.font` | Font file name |
| `ledsize` | Int (1-4) | `2` | LED pixel size: 1=small, 4=large |
| `wth` | Int | `200` | Display width in character columns |
| `ht` | Int | `9` | Display height in character rows |
| `border` | Int | `2` | Border width in pixels |
| `bordercolor` | String (R,G,B) | `lightGray` | Border color (e.g., `128,128,128`) |
| `hot_bordercolor` | String (R,G,B) | `blue` | Border color for clickable links |
| `smooth_leds` | Boolean | `true` | Smooth/circular LEDs for sizes 3 and 4 |
| `spacewidth` | Int | `3` | Width of space character in pixels |

## Script Format

Create LED sign scripts with commands like:

```
Do
   ScrollUp delay=30 center=true text=Hello World
   Sleep delay=1000
   ScrollLeft delay=25 text=Scrolling Text
   Sleep delay=1000
Repeat times=-1
```

Available transitions:
- `Appear` — Show text immediately
- `Sleep` — Pause (use `delay` parameter)
- `ScrollLeft`, `ScrollRight`, `ScrollUp`, `ScrollDown` — Scroll text
- `Pixel` — Reveal pixel-by-pixel
- `Blink` — Flash on/off
- `OverLeft`, `OverRight`, `OverUp`, `OverDown` — Overlay transitions
- `ScrollCenter`, `OverCenter` — Center transitions

### Script Tags

Use backslash tags in text for colors and dates:

- `\r` — Red, `\g` — Green, `\b` — Blue, `\y` — Yellow, `\o` — Orange, `\p` — Purple, `\w` — White, `\c` — Cyan
- `\tt` — Current time (e.g., `3:45 pm`)
- `\dd` — Day of week short (e.g., `Mon`), `\DD` — Long (e.g., `Monday`)
- `\dn` — Day of month
- `\mm` — Month short (e.g., `Jan`), `\MM` — Long (e.g., `January`)
- `\mn` — Month number (1-12)
- `\yy` — Year (2-digit), `\YY` — Full year (4-digit)

Example: `"Today is \DD, \MM \dn, \YY at \tt"` → `"Today is Monday, January 18, 2026 at 3:45 pm"`

## Modernizations

This version includes:

1. **Safe Thread Handling**: Replaced deprecated `Thread.stop()` with cooperative shutdown using a `volatile boolean running` flag and `interrupt()`/`join()`.

2. **Modern Date/Time**: Replaced deprecated `java.util.Date` with `java.time.LocalDateTime` in `Script.java`.

3. **Gradle Build**: Standard Gradle layout (`src/main/java`, `src/main/resources`) with proper dependency management and cross-platform wrapper.

4. **Configurable Launcher**: `MainLauncher` accepts parameters via:
   - `ledsign.properties` file
   - Command-line `key=value` arguments
   - Hardcoded defaults

5. **AppletStub Implementation**: Full desktop applet support without a browser:
   - Parameter resolution from properties/CLI
   - Document/code base URLs pointing to resources
   - Browser integration for URL clicks (via `Desktop.browse()`)

## Character Encoding

The project uses `Cp1252` (Windows-1252) encoding to preserve legacy copyright symbols and accented characters in source comments. This is configured in `build.gradle`:

```groovy
tasks.withType(JavaCompile) {
    options.encoding = 'Cp1252'
}
```

## Requirements

- **Java**: 11 or later (configured via Gradle toolchain)
- **Gradle**: Included via wrapper (no separate install needed)

## Notes

- The original `com/` folder (legacy source layout) is ignored by `.gitignore`. All active sources are in `src/main/java/`.
- Resources (fonts, scripts) are included in the JAR under `com/dc3/applet/LEDSign/` in the classpath. When running from the project root, the app prefers `src/main/resources/...` for development.
- The desktop launcher does not require HTML or a browser.
- Window size is determined by applet parameters and can be resized.

## Troubleshooting

**"Font not found" error**:
- Ensure the font file exists in `src/main/resources/com/dc3/applet/LEDSign/` (during development) or is bundled in the JAR.
- Check the `font` parameter spelling.

**"No script specified" error**:
- Set the `script` parameter via properties file or CLI args.
- Default is `example.led`; create it if missing.

**UI doesn't appear**:
- The window may be off-screen or very small. Check terminal for error messages.
- Adjust `wth` and `ht` parameters to ensure visible size.

## License & Attribution

Original LEDSign code by Darrick Brown, with V3.1 enhancements by Robert B. Denny.

Modernization (Gradle, Java 11+, safe threading, date/time) performed in 2026.

## Example Usage

```bash
# Run with example script (default)
.\gradlew run

# Run with custom script and font
.\gradlew run --args="script=demo.led font=tiny.font ledsize=3"

# Create a properties file and run
echo "script=my_script.led" > ledsign.properties
echo "ledsize=2" >> ledsign.properties
.\gradlew run

# Build and run as JAR
.\gradlew clean build
java -jar build/libs/ledsign3-1.0.0.jar script=custom.led
```

Enjoy your LED sign!

