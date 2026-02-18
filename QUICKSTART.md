# Quick Start Guide

## Build & Run in 30 Seconds

### Windows (PowerShell)
```powershell
cd S:\Java\src\LEDSIGN3
.\gradlew run
```

### Linux/Mac
```bash
cd LEDSIGN3
./gradlew run
```

The app will launch a window displaying a scrolling LED sign.

## Customize Parameters

### Option 1: Command-Line Arguments
```powershell
.\gradlew run --args="script=custom.led font=tiny.font ledsize=3"
```

### Option 2: Properties File
Create `ledsign.properties`:
```properties
script=my_script.led
ledsize=2
wth=300
ht=15
```

Then run:
```powershell
.\gradlew run
```

## Build JAR

```powershell
.\gradlew clean build
java -jar build\libs\ledsign3-1.0.0.jar
```

## Key Parameters

| Parameter | Example | Notes |
|-----------|---------|-------|
| `script` | `example.led` | Script file to run |
| `font` | `default.font` | Font file |
| `ledsize` | `1`, `2`, `3`, `4` | LED size (1=tiny, 4=huge) |
| `wth` | `200` | Width in character columns |
| `ht` | `9` | Height in character rows |

See `README.md` for full documentation.

