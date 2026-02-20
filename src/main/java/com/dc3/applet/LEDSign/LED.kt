//tabs=4
//-----------------------------------------------------------------------------
//  LED.java  -- LED Sign V3.1
//
//  The main for the LED Sign applet. This applet mimics
//  an LED sign that you typically see displaying messages
//  at airport terminals and the such.
//
//  Revisions:
//	V3.1	Converted to package
//			Modified 11-Aug-96 by Robert B. Denny
//
//	V3.0:...
//-----------------------------------------------------------------------------
package com.dc3.applet.LEDSign

import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.net.URI
import java.net.URL
import javax.swing.JPanel
import kotlin.concurrent.Volatile
import kotlin.math.max

// Just a small struct
// used in randomizing the pixels in the "Pixel" function
class Pixelize {
    var x: Int = 0
    var y: Int = 0
}

//-----------------------------------------------------------------------------
// Converted to Swing JPanel (formerly java.applet.Applet)
//-----------------------------------------------------------------------------
class LED : JPanel(), Runnable {
    // my #defines
    var WIDTH: Int = 400
    var HEIGHT: Int = 30
    var appletName: String = "LED Sign V3.1"

    var scr: Script? = null // The class that takes care of the script
    var fi: FuncInfo? = null // All the info for any funtion/transition
    var let: Letters? = null // The class that contains all the letters
    var ledsize: Int = 0 // The size of the LEDs

    var highlight: Color? = null // Color used for highlight on large LEDs
    lateinit var colors: Array<Color?> // The array of possible colors
    var msg: LEDMessage? = null // The class that takes care of the message to be displayed
    var bhilite: Color? = null // Normal border hilite color
    var bcolor: Color? = null // Normal border color
    var bshadow: Color? = null // Normal border shadow color
    var h_bhilite: Color? = null // Border hilite if message has URL
    var h_bcolor: Color? = null // Border color if message has URL
    var h_bshadow: Color? = null // Border shadow if message has URL
    var smooth_leds: Boolean = true // true if want real circular LEDs in larger sizes

    // Replace 'Thread led' with safer naming and a running flag
    private var ledThread: Thread? = null // the thread that runs the application

    @Volatile
    private var running = false // controls the run loop

    // Configuration for Swing-based application
    private var documentBase: URL? = null // Base URL for resources
    var scrpt: String? = null
    var endspace: String? = null
    var fnt: String? = null // Script and font parameters
    var text: String? = null // the current message
    var currurl: String? = appletName // The current url that are set in the script
    var currURL: URL? = null
    var lastURL: URL? = null
    var target: String? = ""
    var place: Int = 0
    private var border: Int = 0 // The border width
    var offset: Int = 0 // The offset for the sign from the upper left
    var w: Int = 0
    var h: Int = 0 // Width & Height in LEDs
    var swidth: Int = 0 // The width of the space character.  Settable in the HTML command line.
    var beginning: Boolean = false
    var init: Boolean = false // used to make sure "getinfo" is called only once.
    var inapplet: Boolean = false // Is the mouse cursor in the panel?  (used to display status messages)
    var done: Boolean = false // Is the transition done?
    var pixmapimg: Image? = null
    var offimg: Image? = null
    var tmpimg: Image? = null // The pixmaps!!
    var pixmap: Graphics? = null
    var offmap: Graphics? = null
    var tmpmap: Graphics? = null // Graphics for the pixmaps
    lateinit var pix: Array<Pixelize?> // Array of "pixels" used during the Pixel transition

    //--------------------------------------------------------------------------
    // Configuration Setters for Swing-based Application
    //--------------------------------------------------------------------------
    fun setDocumentBase(url: URL) {
        this.documentBase = url
    }

    fun setScript(scriptName: String) {
        this.scrpt = scriptName
    }

    fun setFont(fontName: String) {
        this.fnt = fontName
    }

    fun setLedSize(size: Int) {
        ledsize = size
        // A little error trapping
        if (ledsize < 1) ledsize = 1
        else if (ledsize > 4) ledsize = 4
        ledsize++ // The user enters 1-4, the applet needs 2-5
    }

    fun setSpaceWidth(width: Int) {
        this.swidth = width
    }

    fun setWidthHeight(ledWidth: Int, ledHeight: Int) {
        this.w = ledWidth
        this.h = ledHeight
        this.WIDTH = ledsize * ledWidth
        this.HEIGHT = ledsize * ledHeight
        if (WIDTH % 2 == 1) WIDTH += ledsize // It must be even!!!
    }

    fun setBorder(borderWidth: Int) {
        this.border = borderWidth
    }

    fun setBorderColors(r: Int, g: Int, b: Int) {
        bhilite = Color(
            if (r + 40 < 256) r + 40 else 255,
            if (g + 40 < 256) g + 40 else 255,
            if (b + 40 < 256) b + 40 else 255
        )
        bcolor = Color(r, g, b)
        bshadow = Color(max(r - 40, 0), max(g - 40, 0), max(b - 40, 0))
    }

    fun setHotBorderColors(r: Int, g: Int, b: Int) {
        h_bhilite = Color(
            if (r + 40 < 256) r + 40 else 255,
            if (g + 40 < 256) g + 40 else 255,
            if (b + 40 < 256) b + 40 else 255
        )
        h_bcolor = Color(r, g, b)
        h_bshadow = Color(max(r - 40, 0), max(g - 40, 0), max(b - 40, 0))
    }

    fun setSmoothLeds(smooth: Boolean) {
        this.smooth_leds = smooth
    }

    //--------------------------------------------------------------------------
    // PUBLIC init() - The initialization entry for the application
    //--------------------------------------------------------------------------
    fun init() {

        // Validate required fields
        if (scrpt == null || scrpt!!.isEmpty()) {
            throw RuntimeException("No script specified.")
        }
        if (fnt == null || fnt!!.isEmpty()) {
            throw RuntimeException("No font specified.")
        }
        if (documentBase == null) {
            throw RuntimeException("Document base URL not set.")
        }

        highlight = Color(100, 100, 100)
        colors = arrayOfNulls<Color>(9)
        colors[0] = Color(64, 64, 64)
        colors[1] = Color(255, 64, 64)
        colors[2] = Color(130, 255, 0)
        colors[3] = Color(0, 130, 255)
        colors[4] = Color(255, 255, 0)
        colors[5] = Color(255, 160, 0)
        colors[6] = Color(255, 0, 255)
        colors[7] = Color(255, 255, 255)
        colors[8] = Color(0, 255, 255)

        // Set default border colors if not already set
        if (bhilite == null) {
            bhilite = Color.white
            bcolor = Color.lightGray
            bshadow = Color.darkGray
        }
        if (h_bhilite == null) {
            h_bhilite = Color(128, 128, 255)
            h_bcolor = Color(0, 0, 255)
            h_bshadow = Color(0, 0, 128)
        }

        offset = 3 * border
        beginning = true
        init = true

        // Add mouse listener for Swing
        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(evt: MouseEvent?) {
                handleMouseClick()
            }

            override fun mouseEntered(evt: MouseEvent?) {
                inapplet = true
                showStatus(currurl)
            }

            override fun mouseExited(evt: MouseEvent?) {
                inapplet = false
                showStatus(" ")
            }
        })
    }

    //--------------------------------------------------------------------------
    // PRIVATE void runInit() - Second-phase initialization
    //--------------------------------------------------------------------------
    @Throws(RuntimeException::class)
    private fun runInit() {
        val r1: Rectangle?
        val r2: Rectangle?

        pix = arrayOfNulls<Pixelize>(1)
        let = Letters(documentBase, fnt!!, swidth)
        if (HEIGHT != let!!.height() * ledsize) {
            println("LED Sign Warning: parameter \"ht\" should be set to " + let!!.height() + ".")
        }

        msg = LEDMessage(h, w, let!!)
        scr = Script(documentBase, scrpt)
        fi = FuncInfo()
        nextFunc()

        r1 = bounds
        setSize(WIDTH + 2 * (offset), HEIGHT + 2 * (offset))
        preferredSize = Dimension(WIDTH + 2 * (offset), HEIGHT + 2 * (offset))
        r2 = bounds
        if ((r1.width != r2.width) || (r1.height != r2.height)) println("Panel resized to w=" + r2.width + " h=" + r2.height)
        init = false
    }

    //--------------------------------------------------------------------------
    // Start the applet running and thread the process
    //--------------------------------------------------------------------------
    fun start() {
        if (ledThread == null) {
            running = true
            ledThread = Thread(this, "LEDSign-Thread")
            ledThread!!.setDaemon(true)
            ledThread!!.start()
        }
    }

    /** /////////////////////////////////////////////////////////////// */ // Stop the thread - perform a cooperative shutdown
    fun stop() {
        if (ledThread != null) {
            // Request shutdown
            running = false
            // Interrupt the thread to wake it if sleeping
            ledThread!!.interrupt()

            // If stop() is called from a different thread, join briefly
            if (Thread.currentThread() !== ledThread) {
                try {
                    ledThread!!.join(500)
                } catch (ignored: InterruptedException) {
                }
                ledThread = null
            } else {
                // If called from the running thread, just return and let run() exit
            }
        }
    }

    //--------------------------------------------------------------------------
    // The run loop
    //--------------------------------------------------------------------------
    override fun run() {
        try {
            if (init) {
                try {
                    runInit()
                } catch (e: RuntimeException) {
                    currurl = "LED Sign Error: " + e.message
                    println(currurl)
                    showStatus(currurl)
                    // request cooperative shutdown
                    running = false
                    // avoid calling stop() here because stop() may try to join this thread
                    return
                }
            }

            while (running) {
                updateDisplay()
                repaint()

                try {
                    Thread.sleep(fi!!.delay.toLong())
                } catch (e: InterruptedException) {
                    // Interrupted: if running flag was cleared, exit loop
                    if (!running) break
                }

                if (done) {
                    try {
                        nextFunc()
                    } catch (e: RuntimeException) {
                        currurl = "LED Sign error: " + e.message
                        println(currurl)
                        showStatus(currurl)
                        running = false
                        break
                    }

                    if (fi == null) {
                        currurl = "Script finished"
                        println(currurl)
                        showStatus(currurl)
                        running = false
                        break
                    }
                    done = false
                }
            }
        } finally {
            // Clean up thread reference on exit
            if (Thread.currentThread() === ledThread) ledThread = null
        }
    }

    val parameterInfo: Array<Array<String?>?>
        //--------------------------------------------------------------------------
        get() {
            val info = arrayOf<Array<String?>?>(
                arrayOf<String?>("script         ", "URL        ", "LED script to use (Required)"),
                arrayOf<String?>("font           ", "URL        ", "Font to use (Required)"),
                arrayOf<String?>(
                    "spacewidth     ",
                    "int        ",
                    "Width of space in columns, default=3 )"
                ),
                arrayOf<String?>(
                    "wth            ",
                    "int        ",
                    "Width of live display (cols, default=60)"
                ),
                arrayOf<String?>(
                    "ht             ",
                    "int        ",
                    "Height of live display (rows, default=9)"
                ),
                arrayOf<String?>(
                    "border         ",
                    "int        ",
                    "Width of display border (pix, default=0)"
                ),
                arrayOf<String?>(
                    "bordercolor    ",
                    "int,int,int",
                    "Color of border (n,n,n default=lightGray)"
                ),
                arrayOf<String?>(
                    "hot_bordercolor",
                    "int,int,int",
                    "Color of hot border (n,n,n default=blue)"
                ),
                arrayOf<String?>(
                    "ledsize        ",
                    "int        ",
                    "Diameter of LEDs pixels (1-4), default=3)"
                ),
                arrayOf<String?>(
                    "smooth_leds    ",
                    "boolean    ",
                    "True circles for larger size LEDs (default=false)"
                )
            )
            return info
        }

    //--------------------------------------------------------------------------
    // PRIVATE void handleMouseClick() - Handle mouse click on the panel
    //--------------------------------------------------------------------------
    private fun handleMouseClick() {
        if (currURL != null) {
            try {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().browse(URI(currURL.toString()))
                }
            } catch (e: Exception) {
                System.err.println("Failed to open URL: " + e.message)
            }
        }
    }

    //--------------------------------------------------------------------------
    // PRIVATE void showStatus() - Display status message (console output)
    //--------------------------------------------------------------------------
    private fun showStatus(status: String?) {
        println("Status: " + status)
    }

    //--------------------------------------------------------------------------
    // PRIVATE void nextVunc() - Set the next function
    //--------------------------------------------------------------------------
    private fun nextFunc() {
        var i: Int
        var j: Int
        var temp: Pixelize?
        var rand: Int

        fi = scr!!.nextFunc()

        if (fi == null) {
            return
        }

        fi = scr!!.parseLine(fi!!)

        msg!!.setmsg(fi!!)

        if (fi!!.url != null) {
            currurl = fi!!.url.toString()
            currURL = fi!!.url
            target = fi!!.target
        } else {
            currurl = appletName
            currURL = null
            target = ""
        }

        if (inapplet) {
            showStatus(currurl)
        }

        when (fi!!.func) {
            LEDFunction.APPEAR, LEDFunction.OVER_RIGHT, LEDFunction.SCROLL_CENTER, LEDFunction.OVER_CENTER, LEDFunction.OVER_DOWN, LEDFunction.SLEEP, LEDFunction.SCROLL_LEFT, LEDFunction.SCROLL_UP -> place =
                0

            LEDFunction.SCROLL_RIGHT -> place = msg!!.length() - 1
            LEDFunction.SCROLL_DOWN, LEDFunction.OVER_UP -> place = h - 1
            LEDFunction.PIXEL -> {
                place = 0

                pix = arrayOfNulls<Pixelize>(w * h)

                i = 0
                while (i < w) {
                    j = 0
                    while (j < h) {
                        pix[h * i + j] = Pixelize()
                        pix[h * i + j]!!.x = i
                        pix[h * i + j]!!.y = j
                        j++
                    }
                    i++
                }

                i = 0
                while (i < WIDTH / ledsize * h) {
                    rand = (Math.random() * (WIDTH / ledsize).toDouble() * h.toDouble()).toInt()
                    temp = pix[i]
                    pix[i] = pix[rand]
                    pix[rand] = temp
                    i++
                }
            }

            LEDFunction.BLINK -> place = fi!!.times * 2
            LEDFunction.OVER_LEFT -> place = w
            else -> {}
        }
    }

    //--------------------------------------------------------------------------
    // Draw a pretty little LED
    //--------------------------------------------------------------------------
    private fun drawLED(x: Int, y: Int, on: Boolean, col: Int, gr: Graphics) {
        if (on) {
            gr.color = colors[col]
        } else {
            gr.color = colors[0]
        }

        when (ledsize) {
            2 -> gr.drawLine(x, y, x, y)
            3 -> gr.fillRect(x, y, 2, 2)
            4 -> if (smooth_leds) {
                gr.fillOval(x, y, 3, 3)
            } else {
                gr.drawLine(x, y + 1, x + 2, y + 1)
                gr.drawLine(x + 1, y, x + 1, y + 2)
            }

            5 -> if (smooth_leds) {
                gr.fillOval(x, y, 4, 4)
            } else {
                gr.fillRect(x + 1, y, 2, 4)
                gr.fillRect(x, y + 1, 4, 2)
            }
        }

        if (ledsize == 5 && !on && !smooth_leds) {
            gr.color = highlight
            gr.drawLine(x + 1, y + 1, x + 1, y + 1)
        }
    }

    //--------------------------------------------------------------------------
    // PRIVATE void draw3DRect() - Draw a 3D rect with variable line width
    //--------------------------------------------------------------------------
    private fun draw3DRect(
        gr: Graphics, x: Int, y: Int, lx: Int, ly: Int,
        width: Int, raised: Boolean
    ) {
        var i: Int

        i = 0
        while (i < width) {
            val c1 = if (currURL != null) h_bhilite else bhilite
            val c2 = if (currURL != null) h_bshadow else bshadow

            if (raised) gr.color = c1
            else gr.color = c2

            gr.drawLine(x + i, y + i, lx - i, y + i)
            gr.drawLine(x + i, y + i, x + i, ly - i)

            if (raised) gr.color = c2
            else gr.color = c1

            gr.drawLine(lx - i, y + i, lx - i, ly - i)
            gr.drawLine(x + i, ly - i, lx - i, ly - i)
            i++
        }
    }

    //--------------------------------------------------------------------------
    // PRIVATE void drawWideRect() - Draw a rectangle with variable line width
    //--------------------------------------------------------------------------
    private fun drawWideRect(gr: Graphics, x: Int, y: Int, lx: Int, ly: Int, width: Int) {
        var i: Int

        gr.color = if (currURL != null) h_bcolor else bcolor
        i = 0
        while (i < width) {
            gr.drawLine(x + i, y + i, lx - i, y + i)
            gr.drawLine(x + i, y + i, x + i, ly - i)
            gr.drawLine(lx - i, y + i, lx - i, ly - i)
            gr.drawLine(x + i, ly - i, lx - i, ly - i)
            i++
        }
    }

    //--------------------------------------------------------------------------
    // PRIVATE void drawFrame() - Draw the frame
    //--------------------------------------------------------------------------
    private fun drawFrame(gr: Graphics) {
        if (border > 0) {
            draw3DRect(gr, 0, 0, WIDTH + 2 * offset - 1, HEIGHT + 2 * offset - 1, border, true)
            drawWideRect(gr, border, border, WIDTH + 5 * border - 1, HEIGHT + 5 * border - 1, border)
            draw3DRect(gr, 2 * border, 2 * border, WIDTH + 4 * border - 1, HEIGHT + 4 * border - 1, border, false)
        }
    }

    //--------------------------------------------------------------------------
    // Swing paintComponent override
    //--------------------------------------------------------------------------
    override fun paintComponent(gr: Graphics) {
        super.paintComponent(gr)

        var i: Int
        var j: Int

        if (ledThread != null) {
            drawFrame(gr)

            if (beginning) {
                offimg = createImage(WIDTH, HEIGHT)
                offmap = offimg!!.graphics
                offmap!!.color = Color.black
                offmap!!.fillRect(0, 0, WIDTH, HEIGHT)

                i = 0
                while (i < HEIGHT) {
                    j = 0
                    while (j < WIDTH) {
                        drawLED(j, i, false, 1, offmap!!)
                        j += ledsize
                    }
                    i += ledsize
                }

                gr.drawImage(offimg, offset, offset, this)

                pixmapimg = createImage(WIDTH, HEIGHT)
                tmpimg = createImage(WIDTH, HEIGHT)

                pixmap = pixmapimg!!.graphics
                tmpmap = tmpimg!!.graphics

                pixmap!!.color = Color.black
                pixmap!!.fillRect(0, 0, WIDTH, HEIGHT)

                i = 0
                while (i < HEIGHT) {
                    j = 0
                    while (j < WIDTH) {
                        drawLED(j, i, false, 1, pixmap!!)
                        j += ledsize
                    }
                    i += ledsize
                }

                beginning = false
            } else {
                gr.drawImage(pixmapimg, offset, offset, this)
            }
        }
    }

    //--------------------------------------------------------------------------
    // updateDisplay() - Render the animation frame (called from run loop)
    //--------------------------------------------------------------------------
    fun updateDisplay() {
        var i: Int
        var j: Int

        if (done) return

        if ((ledThread != null) && (fi != null) && (pixmap != null) && (offmap != null) && (tmpmap != null)) {
            val gr = getGraphics()
            if (gr != null) {
                if (((currURL != null) && (lastURL == null)) ||
                    ((currURL == null) && (lastURL != null))
                ) {
                    drawFrame(gr)
                    lastURL = currURL
                }

                when (fi!!.func) {
                    LEDFunction.APPEAR -> {
                        if (fi!!.text == null) {
                            gr.drawImage(offimg, offset, offset, this)
                        } else {
                            i = 0
                            while (i < w) {
                                j = 0
                                while (j < h) {
                                    drawLED(i * ledsize, j * ledsize, msg!!.getLED(i, j), msg!!.getColor(i), pixmap!!)
                                    j++
                                }
                                i++
                            }

                            gr.drawImage(pixmapimg, offset, offset, this)
                        }

                        done = true
                    }

                    LEDFunction.SLEEP -> done = true

                    LEDFunction.SCROLL_LEFT -> {
                        pixmap!!.copyArea(ledsize, 0, WIDTH - ledsize, HEIGHT, -ledsize, 0)

                        i = 0
                        while (i < HEIGHT) {
                            drawLED(
                                WIDTH - ledsize,
                                i,
                                msg!!.getLED(place, i / ledsize),
                                msg!!.getColor(place),
                                pixmap!!
                            )
                            i += ledsize
                        }

                        gr.drawImage(pixmapimg, offset, offset, this)

                        place++

                        if (!msg!!.inRange(place)) done = true
                    }

                    LEDFunction.SCROLL_RIGHT -> {
                        pixmap!!.copyArea(0, 0, WIDTH - ledsize, HEIGHT, ledsize, 0)

                        i = 0
                        while (i < HEIGHT) {
                            drawLED(0, i, msg!!.getLED(place, i / ledsize), msg!!.getColor(place), pixmap!!)
                            i += ledsize
                        }

                        gr.drawImage(pixmapimg, offset, offset, this)

                        place--

                        if (place < 0) done = true
                    }

                    LEDFunction.SCROLL_UP -> {
                        pixmap!!.copyArea(0, ledsize, WIDTH, HEIGHT - ledsize, 0, -ledsize)

                        i = 0
                        while (i < WIDTH) {
                            if (msg!!.inRange(i / ledsize)) drawLED(
                                i,
                                HEIGHT - ledsize,
                                msg!!.getLED(i / ledsize, place),
                                msg!!.getColor(i / ledsize),
                                pixmap!!
                            )
                            else drawLED(i, HEIGHT - ledsize, false, 1, pixmap!!)
                            i += ledsize
                        }

                        gr.drawImage(pixmapimg, offset, offset, this)

                        place++

                        if (place >= h) done = true
                    }

                    LEDFunction.SCROLL_DOWN -> {
                        pixmap!!.copyArea(0, 0, WIDTH, HEIGHT - ledsize, 0, ledsize)

                        i = 0
                        while (i < WIDTH) {
                            if (msg!!.inRange(i / ledsize)) {
                                drawLED(i, 0, msg!!.getLED(i / ledsize, place), msg!!.getColor(i / ledsize), pixmap!!)
                            } else {
                                drawLED(i, 0, false, 1, pixmap!!)
                            }
                            i += ledsize
                        }

                        gr.drawImage(pixmapimg, offset, offset, this)

                        place--

                        if (place < 0) done = true
                    }

                    LEDFunction.PIXEL -> {
                        i = place + fi!!.times
                        while (place < WIDTH / ledsize * h && place < i) {
                            if (msg!!.inRange(pix[place]!!.x)) {
                                drawLED(
                                    pix[place]!!.x * ledsize,
                                    pix[place]!!.y * ledsize,
                                    msg!!.getLED(pix[place]!!.x, pix[place]!!.y),
                                    msg!!.getColor(pix[place]!!.x),
                                    pixmap!!
                                )
                            } else {
                                drawLED(pix[place]!!.x * ledsize, pix[place]!!.y * ledsize, false, 1, pixmap!!)
                            }

                            place++
                        }
                        gr.drawImage(pixmapimg, offset, offset, this)

                        if (place >= w * h) done = true
                    }

                    LEDFunction.BLINK -> {
                        if (place % 2 == 0) gr.drawImage(offimg, offset, offset, this)
                        else gr.drawImage(pixmapimg, offset, offset, this)

                        place--

                        if (place == 0) done = true
                    }

                    LEDFunction.OVER_RIGHT -> {
                        if (msg!!.inRange(place)) {
                            i = 0
                            while (i < h) {
                                drawLED(
                                    place * ledsize,
                                    i * ledsize,
                                    msg!!.getLED(place, i),
                                    msg!!.getColor(place),
                                    pixmap!!
                                )
                                i++
                            }
                        } else {
                            i = 0
                            while (i < h) {
                                drawLED(place * ledsize, i * ledsize, false, 1, pixmap!!)
                                i++
                            }
                        }

                        gr.drawImage(pixmapimg, offset, offset, this)

                        place++

                        if (place >= w) done = true
                    }

                    LEDFunction.SCROLL_CENTER -> {
                        if (w >= place * 2) {
                            pixmap!!.copyArea(WIDTH / 2, 0, WIDTH / 2 - ledsize, HEIGHT, ledsize, 0)
                            i = 0
                            while (i < h) {
                                if (msg!!.inRange(w - place)) drawLED(
                                    WIDTH / 2,
                                    i * ledsize,
                                    msg!!.getLED(w - place, i),
                                    msg!!.getColor(w - place),
                                    pixmap!!
                                )
                                else drawLED(WIDTH / 2, i * ledsize, false, 1, pixmap!!)
                                i++
                            }
                        }

                        if (place < w / 2) {
                            pixmap!!.copyArea(ledsize, 0, WIDTH / 2 - ledsize, HEIGHT, -ledsize, 0)
                            i = 0
                            while (i < h) {
                                if (msg!!.inRange(place)) drawLED(
                                    WIDTH / 2 - ledsize,
                                    i * ledsize,
                                    msg!!.getLED(place, i),
                                    msg!!.getColor(place),
                                    pixmap!!
                                )
                                else drawLED(WIDTH / 2 - ledsize, i * ledsize, false, 1, pixmap!!)
                                i++
                            }
                        }

                        gr.drawImage(pixmapimg, offset, offset, this)

                        place++

                        if (place >= w / 2 && place * 2 > w) done = true
                    }

                    LEDFunction.OVER_CENTER -> {
                        if (w >= place + w / 2) {
                            i = 0
                            while (i < h) {
                                if (msg!!.inRange(w / 2 + place + 1)) drawLED(
                                    WIDTH / 2 + place * ledsize + ledsize,
                                    i * ledsize,
                                    msg!!.getLED(w / 2 + place + 1, i),
                                    msg!!.getColor(w / 2 + place + 1),
                                    pixmap!!
                                )
                                else drawLED(WIDTH / 2 + place * ledsize + ledsize, i * ledsize, false, 1, pixmap!!)
                                i++
                            }
                        }

                        if (place < w / 2) {
                            i = 0
                            while (i < h) {
                                if (msg!!.inRange(w / 2 - place)) drawLED(
                                    WIDTH / 2 - place * ledsize,
                                    i * ledsize,
                                    msg!!.getLED(w / 2 - place, i),
                                    msg!!.getColor(w / 2 - place),
                                    pixmap!!
                                )
                                else drawLED(WIDTH / 2 - place * ledsize, i * ledsize, false, 1, pixmap!!)
                                i++
                            }
                        }

                        gr.drawImage(pixmapimg, offset, offset, this)

                        place++

                        if (w < w / 2 + place && place >= w / 2) done = true
                    }

                    LEDFunction.OVER_LEFT -> {
                        if (msg!!.inRange(place)) {
                            i = 0
                            while (i < h) {
                                drawLED(
                                    place * ledsize,
                                    i * ledsize,
                                    msg!!.getLED(place, i),
                                    msg!!.getColor(place),
                                    pixmap!!
                                )
                                i++
                            }
                        } else {
                            i = 0
                            while (i < h) {
                                drawLED(place * ledsize, i * ledsize, false, 1, pixmap!!)
                                i++
                            }
                        }

                        gr.drawImage(pixmapimg, offset, offset, this)

                        place--

                        if (place == 0) done = true
                    }

                    LEDFunction.OVER_UP -> {
                        i = 0
                        while (i < w) {
                            if (msg!!.inRange(i)) drawLED(
                                i * ledsize,
                                place * ledsize,
                                msg!!.getLED(i, place),
                                msg!!.getColor(i),
                                pixmap!!
                            )
                            else drawLED(i * ledsize, place * ledsize, false, 1, pixmap!!)
                            i++
                        }

                        gr.drawImage(pixmapimg, offset, offset, this)

                        place--

                        if (place < 0) done = true
                    }

                    LEDFunction.OVER_DOWN -> {
                        i = 0
                        while (i < w) {
                            if (msg!!.inRange(i)) drawLED(
                                i * ledsize,
                                place * ledsize,
                                msg!!.getLED(i, place),
                                msg!!.getColor(i),
                                pixmap!!
                            )
                            else drawLED(i * ledsize, place * ledsize, false, 1, pixmap!!)
                            i++
                        }

                        gr.drawImage(pixmapimg, offset, offset, this)

                        place++

                        if (place >= h) done = true
                    }

                    else -> {}
                }
                gr.dispose()
            }
        }
    }
}
