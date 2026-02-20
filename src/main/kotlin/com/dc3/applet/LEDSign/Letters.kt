//------------------------------------------------------------------------------
//  Letters.java	 -- LED Sign V3.1
//
//  This class parses the font file and stores
//  each letter in an array of boolean (on/off).
//  It takes care of all the storage and
//  retrieval of letters data structure.
//------------------------------------------------------------------------------
package com.dc3.applet.LEDSign

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import java.net.URLConnection

/**/////////////////////////////////////////////////////////////// */ // The Letters Class
/**/////////////////////////////////////////////////////////////// */
class Letters
    (url: URL?, URLfile: String, width: Int) {
    var HEIGHT: Int = 0
    var path: String? = null
    var urlc: URLConnection? = null
    var dis: BufferedReader? = null
    var w: Int = 0
    var h: Int = 0
    var num: Int = 0
    var swidth: Int = 0
    var index: Array<Index?>? = null

    init {
        try {
            urlc = (URL(url, URLfile)).openConnection()
            dis = BufferedReader(InputStreamReader(urlc!!.getInputStream()))
            path = URLfile
            swidth = width
        } catch (e: IOException) {
            throw (RuntimeException("Failed to connect to host for font"))
        }

        if (urlc!!.contentType == "text/html") throw (RuntimeException("Font not found (or inaccessible) on host"))

        try {
            initLetters()
        } catch (e: IOException) {
            throw (RuntimeException("Failed to read font data"))
        }
    }

    fun height(): Int {
        return HEIGHT
    }

    @Throws(IOException::class)
    fun initLetters() {
        var ch: Byte
        var j: Int
        var k: Int
        var s: String
        var done: Boolean
        var width: Int

        w = 5
        h = 5
        num = 100

        done = false
        while (!done) {
            s = dis!!.readLine()
            if (!s.startsWith("!!")) {
                h = s.trim().toIntOrNull() ?: 0
                HEIGHT = h
                done = true
            }
        }

        done = false
        while (!done) {
            s = dis!!.readLine()
            if (!s.startsWith("!!")) {
                w = s.trim().toIntOrNull() ?: 0
                done = true
            }
        }

        done = false
        while (!done) {
            s = dis!!.readLine()
            if (!s.startsWith("!!")) {
                num = s.trim().toIntOrNull() ?: 0
                done = true
            }
        }

        index = arrayOfNulls<Index>(num + 1)

        var i = 0
        while (i < num) {
            ch = 2
            width = 10

            done = false
            while (!done) {
                s = dis!!.readLine()
                if (!s.startsWith("!!")) {
                    ch = s.get(0).code.toByte()
                    done = true
                }
            }
            done = false
            while (!done) {
                s = dis!!.readLine()
                if (!s.startsWith("!!")) {
                    width = s.trim().toIntOrNull() ?: 0
                    done = true
                }
            }

            index!![i] = Index(ch, width, h)

            j = 0
            while (j < h) {
                done = false
                s = ""
                while (!done) {
                    s = dis!!.readLine()

                    if (s.length > 0) {
                        if (!s.startsWith("!!")) {
                            done = true
                        }
                    } else {
                        s = " "
                        done = true
                    }
                }

                k = 0
                while (k < index!![i]!!.width) {
                    if (k >= s.length) {
                        index!![i]!!.letter!![k]!![j] = false
                    } else {
                        index!![i]!!.letter!![k]!![j] = s.get(k) == '#'
                    }
                    k++
                }
                j++
            }
            i++
        }

        index!![num] = Index(32.toByte(), swidth, h)

        dis!!.close()
    }

    fun getLetter(c: Char): Index? {
        var j: Int

        if (c == (32).toChar()) {
            j = num
        } else {
            j = 0
            while (c.code.toByte() != index!![j]!!.ch && j < num) j++
        }

        return index!![j]
    }
}
