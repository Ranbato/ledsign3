package com.dc3.applet.LEDSign

import java.awt.BorderLayout
import java.awt.Dimension
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import javax.swing.JFrame
import javax.swing.SwingUtilities

object MainLauncher {
    @JvmStatic
    fun main(args: Array<String>) {
        // Load defaults from properties file if present, then apply CLI key=value pairs
        val props = Properties()
        try {
            if (Files.exists(Paths.get("ledsign.properties"))) {
                FileInputStream("ledsign.properties").use { `in` ->
                    props.load(`in`)
                }
            }
        } catch (e: Exception) {
            println("Warning: failed to read ledsign.properties: " + e.message)
        }

        // Parse CLI arguments of form key=value
        for (a in args) {
            if (a.contains("=")) {
                val k = a.substring(0, a.indexOf('='))
                val v = a.substring(a.indexOf('=') + 1)
                props.setProperty(k, v)
            }
        }

        val finalProps = props

        SwingUtilities.invokeLater(Runnable {
            val frame = JFrame("LEDSign")
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)

            val ledPanel = LED()

            // Set configuration from properties using public setters
            try {
                // Determine document base (for resource loading)
                val documentBase = Paths.get(
                    System.getProperty("user.dir"),
                    "src", "main", "resources", "com", "dc3", "applet", "LEDSign"
                ).toUri().toURL()
                ledPanel.setDocumentBase(documentBase)
            } catch (e: Exception) {
                try {
                    // Fallback to classpath resources
                    val res = LED::class.java.getResource("/com/dc3/applet/LEDSign/")
                    if (res != null) {
                        ledPanel.setDocumentBase(res)
                    }
                } catch (ex: Exception) {
                    System.err.println("Failed to determine document base: " + ex.message)
                }
            }

            // Set required parameters
            ledPanel.setScript(finalProps.getProperty("script", "example.led"))
            ledPanel.setFont(finalProps.getProperty("font", "default.font"))

            // Set optional parameters with defaults
            val ledSize = finalProps.getProperty("ledsize", "2").toInt()
            ledPanel.setLedSize(ledSize)

            val border = finalProps.getProperty("border", "2").toInt()
            ledPanel.setBorder(border)

            val width = finalProps.getProperty("wth", "200").toInt()
            val height = finalProps.getProperty("ht", "9").toInt()
            ledPanel.setWidthHeight(width, height)

            val spaceWidth = finalProps.getProperty("spacewidth", "3").toInt()
            ledPanel.setSpaceWidth(spaceWidth)

            // Set border colors if provided
            val borderColor = finalProps.getProperty("bordercolor")
            if (borderColor != null && !borderColor.isEmpty()) {
                try {
                    val parts: Array<String?> =
                        borderColor.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    if (parts.size == 3) {
                        val r = parts[0]!!.trim { it <= ' ' }.toInt()
                        val g = parts[1]!!.trim { it <= ' ' }.toInt()
                        val b = parts[2]!!.trim { it <= ' ' }.toInt()
                        ledPanel.setBorderColors(r, g, b)
                    }
                } catch (e: Exception) {
                    println("Warning: Invalid bordercolor format: " + borderColor)
                }
            }

            // Set hot border colors if provided
            val hotBorderColor = finalProps.getProperty("hot_bordercolor")
            if (hotBorderColor != null && !hotBorderColor.isEmpty()) {
                try {
                    val parts: Array<String?> =
                        hotBorderColor.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    if (parts.size == 3) {
                        val r = parts[0]!!.trim { it <= ' ' }.toInt()
                        val g = parts[1]!!.trim { it <= ' ' }.toInt()
                        val b = parts[2]!!.trim { it <= ' ' }.toInt()
                        ledPanel.setHotBorderColors(r, g, b)
                    }
                } catch (e: Exception) {
                    println("Warning: Invalid hot_bordercolor format: " + hotBorderColor)
                }
            }

            // Set smooth LEDs if provided
            val smoothLeds = finalProps.getProperty("smooth_leds", "true")
            ledPanel.setSmoothLeds(smoothLeds.equals("true", ignoreCase = true))

            // Add to frame
            frame.contentPane.add(ledPanel, BorderLayout.CENTER)

            // Call lifecycle methods
            try {
                ledPanel.init()
            } catch (t: Throwable) {
                System.err.println("Error during init: ")
                t.printStackTrace()
                System.exit(1)
            }
            try {
                ledPanel.start()
            } catch (t: Throwable) {
                System.err.println("Error during start: ")
                t.printStackTrace()
                System.exit(1)
            }

            // Set frame properties and display
            ledPanel.preferredSize = Dimension(
                finalProps.getProperty("wth", "200").toInt() * (finalProps.getProperty("ledsize", "2")
                    .toInt() + 1) + 2 * 3 * finalProps.getProperty("border", "2").toInt(),
                finalProps.getProperty("ht", "9").toInt() * (finalProps.getProperty("ledsize", "2")
                    .toInt() + 1) + 2 * 3 * finalProps.getProperty("border", "2").toInt()
            )
            frame.pack()
            frame.setLocationRelativeTo(null)
            frame.isVisible = true

            // Add shutdown hook to cleanly stop the LED thread
            Runtime.getRuntime().addShutdownHook(Thread(Runnable {
                try {
                    ledPanel.stop()
                } catch (ignored: Throwable) {
                }
            }))
        })
    }
}
