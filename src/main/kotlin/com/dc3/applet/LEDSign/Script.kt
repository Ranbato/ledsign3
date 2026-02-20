//------------------------------------------------------------------------------
//  Script.java	-- LED Sign V3.1
//
//  Contains the following class:
//	  Script 	 -- The class that manages the script
//					including parsing, storage, and
//				retrieval.
//
//  Revisions:
//	V3.1:	Converted to package
//			Modified 11-Aug-96 by Robert B. Denny
//
//	V3.0: ...
//------------------------------------------------------------------------------
package com.dc3.applet.LEDSign

import com.dc3.applet.LEDSign.LEDFunction.Companion.fromScriptName
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.MalformedURLException
import java.net.URL
import java.net.URLConnection
import java.time.LocalDateTime
import java.util.*

//=============================================================================
// Function		  Code
// -------- 		  ----
// Appear				0
// Sleep 				1
// ScrollLeft			2
// ScrollRight			3
// ScrollUp 			4
// ScrollDown			5
// Pixel 				6
// Blink 				7
// OverRight			8
// ScrollCenter			9
// OverCenter			10
// OverLeft 			11
// OverUp				12
// OverDown 			13
// Do 					97
// Repeat				98
// Reload				99
// Chain 				100
//=============================================================================
class Script
    (var documentURL: URL?, var scrpt: String?) {
    private var scriptList: LinkedList<FuncInfo>? = null
    private var currentIndex = 0 // Current position in the script

    init {
        initScript()
    }

    private fun getParam(command: String, subCommand: String): String? {
        var subCommandPos: Int
        val j: Int
        var tmp: String?

        subCommandPos = command.indexOf(subCommand)
        j = command.indexOf("text")

        if (j == -1 || subCommandPos <= j) {
            if (subCommandPos == -1) return null
            else {
                tmp = command.substring(subCommandPos)
                subCommandPos = tmp.indexOf("=")
                if (subCommandPos == -1) {
                    println("Error in '" + subCommand + "' parameter in " + command)
                    return null
                } else {
                    subCommandPos++
                    if (subCommand.compareTo("text") == 0) tmp = tmp.substring(subCommandPos)
                    else {
                        tmp = tmp.substring(subCommandPos)
                        if (tmp.contains(" ")) tmp = tmp.substring(0, tmp.indexOf(" "))
                    }
                    tmp.trim ()
                    return tmp
                }
            }
        } else return null
    }

    private fun getFunc(s: String): FuncInfo {
        var s = s
        val fi = FuncInfo()

        fi.func = null
        fi.delay = 40
        fi.startspace = 10
        fi.endspace = 20
        fi.times = -1
        fi.remaining = 0
        fi.centered = false
        fi.color = ""
        fi.text = "No text specified"
        fi.url = null
        fi.target = ""
        fi.script = null
        fi.retIndex = -1 // No return point by default

        s = s.trim ()

        var tmp2: String?
        tmp2 = getParam(s, "delay")
        if (tmp2 != null) fi.delay = tmp2.toInt()

        tmp2 = getParam(s, "clear")
        if (tmp2 != null && tmp2.compareTo("true") == 0) {
            fi.centered = true
            fi.text = ""
        } else {
            tmp2 = getParam(s, "center")
            if (tmp2 != null && tmp2.compareTo("true") == 0) fi.centered = true
            else {
                fi.centered = false
                tmp2 = getParam(s, "startspace")
                if (tmp2 != null) fi.startspace = tmp2.toInt()

                tmp2 = getParam(s, "endspace")
                if (tmp2 != null) fi.endspace = tmp2.toInt()
            }

            tmp2 = getParam(s, "text")
            if (tmp2 != null) fi.text = tmp2
        }

        tmp2 = getParam(s, "times")
        if (tmp2 != null) {
            fi.times = tmp2.toInt()
            fi.remaining = fi.times
        }

        tmp2 = getParam(s, "pixels")
        if (tmp2 != null) {
            fi.times = tmp2.toInt()
            fi.remaining = fi.times
        }

        tmp2 = getParam(s, "URL")
        if (tmp2 != null) {
            if (tmp2.indexOf(',') != -1) {
                fi.target = tmp2.substring(tmp2.indexOf(',') + 1)
                tmp2 = tmp2.substring(0, tmp2.indexOf(','))
            }

            try {
                fi.url = URL(tmp2)
            } catch (e: MalformedURLException) {
                println("Bad URL: " + tmp2)
                fi.url = null
            }
        }

        fi.script = getParam(s, "script")

        val i = s.indexOf(" ")
        val funcName: String?
        if (i != -1) funcName = s.substring(0, i)
        else funcName = s

        // Parse function name using enum
        fi.func = fromScriptName(funcName)

        // Apply function-specific defaults
        if (fi.func == LEDFunction.PIXEL) {
            if (fi.delay < 1) fi.delay = 1
            if (fi.times < 1) fi.times = 15
        } else if (fi.func == LEDFunction.BLINK) {
            if (fi.times < 1) fi.times = 2
        }

        fi.store = fi.text

        return fi
    }

    fun nextFunc(): FuncInfo? {
        var fi: FuncInfo?

        if (currentIndex >= scriptList!!.size) {
            return null
        }

        fi = scriptList!!.get(currentIndex)
        currentIndex++

        when (fi.func) {
            LEDFunction.DO -> fi = nextFunc()
            LEDFunction.REPEAT -> if (fi.times >= 0) {
                fi.remaining--
                if (fi.remaining <= 0) {
                    fi.remaining = fi.times
                    fi = nextFunc()
                } else {
                    currentIndex = fi.retIndex
                    fi = nextFunc()
                }
            } else {
                currentIndex = fi.retIndex
                fi = nextFunc()
            }

            LEDFunction.CHAIN -> {
                scrpt = fi.script
                initScript()
                fi = nextFunc()
            }

            LEDFunction.RELOAD -> {
                initScript()
                fi = nextFunc()
            }

            else -> {}
        }

        return fi
    }

    private fun isColor(t: Char): Boolean {
        return (t == 'r' || t == 'g' || t == 'b' || t == 'y' || t == 'o' || t == 'p' || t == 'w' || t == 'c')
    }

    fun parseLine(fi: FuncInfo): FuncInfo {
        var tmp: String?
        var time: String?
        val month: Array<String?> = arrayOf<String?>(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sept", "Oct", "Nov", "Dec"
        )
        val Month: Array<String?> = arrayOf<String?>(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        val day: Array<String?> = arrayOf<String?>("Sun", "Mon", "Tues", "Wed", "Thur", "Fri", "Sat")
        val Day: Array<String?> =
            arrayOf<String?>("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
        var ddmmyy: String?
        var min: Int
        var pm: Int
        // Use modern java.time.LocalDateTime
        val date = LocalDateTime.now()
        var a: Int
        var b: Int
        var i: Int
        var c: Char
        var t: String?

        tmp = fi.store
        fi.color = ""

        if (fi.func == LEDFunction.APPEAR || (fi.func != null && fi.func!!.code >= 2 && fi.func!!.code <= 97)) {
            c = 'r'
            b = 0
            while (b < tmp!!.length) {
                if (tmp.get(b) == '\\') {
                    b++
                    if (tmp.get(b) == '{') {
                        t = tmp.substring(b + 1)
                        tmp = tmp.substring(0, b - 1) + t.substring(t.indexOf('}') + 1)
                        t = t.substring(0, t.indexOf('}'))
                        b -= 1
                    } else {
                        t = tmp.substring(b, b + 1)
                        tmp = (tmp.substring(0, b - 1)) + tmp.substring(b + 1)
                        b -= 1
                    }

                    if (t.length == 1 && isColor(t.get(0))) {
                        c = t.get(0)
                    } else if (t.compareTo("tt") == 0) {
                        if (date.hour >= 12) pm = 1
                        else pm = 0

                        if (pm == 1) {
                            a = date.hour
                            if (a == 12) time = 12.toString()
                            else time = (date.hour - 12).toString()
                        } else {
                            a = date.hour
                            if (a == 0) time = 12.toString()
                            else time = a.toString()
                        }

                        time = time + ":"

                        min = date.minute
                        if (min >= 10) time = time + min.toString()
                        else {
                            time = time + "0"
                            time = time + min.toString()
                        }

                        if (pm == 1) time = time + " pm"
                        else time = time + " am"

                        tmp = ((tmp.substring(0, b)) + time) + tmp.substring(b)

                        b += time.length

                        i = 0
                        while (i < time.length) {
                            fi.color = (fi.color) + (c).toString()
                            i++
                        }
                    } // End time
                    else if (t.compareTo("dd") == 0 || t.compareTo("DD") == 0)  // day name
                    {
                        // LocalDateTime.getDayOfWeek returns MONDAY..SUNDAY
                        val dow = date.dayOfWeek.value % 7 // maps Sunday->0, Monday->1
                        if (t.compareTo("dd") == 0) ddmmyy = day[dow]
                        else ddmmyy = Day[dow]

                        i = 0
                        while (i < ddmmyy!!.length) {
                            fi.color += (c).toString()
                            i++
                        }

                        tmp = ((tmp.substring(0, b)) + ddmmyy) + tmp.substring(b)
                        b += ddmmyy.length
                    } else if (t.compareTo("dn") == 0) {
                        ddmmyy = date.dayOfMonth.toString()

                        i = 0
                        while (i < ddmmyy.length) {
                            fi.color = (fi.color) + (c).toString()
                            i++
                        }

                        tmp = ((tmp.substring(0, b)) + ddmmyy) + tmp.substring(b)
                        b += ddmmyy.length
                    } else if (t.compareTo("mm") == 0 || t.compareTo("MM") == 0) {
                        var monthIndex = date.monthValue - 1 // 0-based index
                        if (monthIndex < 0 || monthIndex > 11) monthIndex = 0
                        if (t.compareTo("mm") == 0) ddmmyy = month[monthIndex]
                        else ddmmyy = Month[monthIndex]

                        i = 0
                        while (i < ddmmyy!!.length) {
                            fi.color += (c).toString()
                            i++
                        }

                        tmp = ((tmp.substring(0, b)) + ddmmyy) + tmp.substring(b)
                        b += ddmmyy.length
                    } else if (t.compareTo("mn") == 0) {
                        ddmmyy = date.monthValue.toString()

                        i = 0
                        while (i < ddmmyy.length) {
                            fi.color += (c).toString()
                            i++
                        }

                        tmp = ((tmp.substring(0, b)) + ddmmyy) + tmp.substring(b)
                        b += ddmmyy.length
                    } else if (t.compareTo("yy") == 0 || t.compareTo("YY") == 0) {
                        if (t.compareTo("YY") == 0) ddmmyy = date.year.toString()
                        else ddmmyy = (date.year % 100).toString()

                        i = 0
                        while (i < ddmmyy.length) {
                            fi.color = (fi.color) + (c).toString()
                            i++
                        }

                        tmp = ((tmp.substring(0, b)) + ddmmyy) + tmp.substring(b)
                        b += ddmmyy.length
                    } else if (t.compareTo("\\") == 0) {
                        tmp = (tmp.substring(0, b)) + tmp.substring(b + 1)
                        b--
                    } else {
                        println("Backslash (\\) error in text line: " + fi.store)
                    }
                } else {
                    b++
                    fi.color = fi.color + (c).toString()
                }
            } // END - while(...)
        } // END - if(fi.func == ...)


        fi.text = tmp

        return fi
    }

    @Throws(RuntimeException::class)
    private fun initScript() {
        val urlc: URLConnection
        val dis: BufferedReader?
        val url: URL?
        var line: String?
        var listlen: Int
        var dos: Int

        try {
            url = URL(documentURL, scrpt)
            urlc = url.openConnection()
            dis = BufferedReader(InputStreamReader(urlc.getInputStream()))
        } catch (e: Exception) {
            throw (RuntimeException("Failed to connect to host for script"))
        }

        try {
            scriptList = LinkedList<FuncInfo>()
            currentIndex = 0
            listlen = 0
            dos = 0
            while ((dis.readLine().also { line = it }) != null) {
                line = line!!.trim ()
                if (!(line.startsWith("!!")) && (!line.isEmpty())) {
                    listlen++
                    val fi = getFunc(line)
                    scriptList!!.add(fi)
                    if (fi.func == LEDFunction.DO) dos++
                }
            }

            // Build mapping of DO/REPEAT loops using indices
            val doStack: MutableList<Int?> = ArrayList<Int?>()
            dos = 0
            for (idx in scriptList!!.indices) {
                val fi = scriptList!!.get(idx)
                if (fi.func == LEDFunction.DO) {
                    doStack.add(idx)
                    dos++
                } else if (fi.func == LEDFunction.REPEAT) {
                    if (dos > 0) {
                        dos--
                        fi.retIndex = doStack.get(dos)!!
                    } else {
                        println("Repeat error in line : Repeat times=" + fi.times)
                        println(" 	 Mismatched Do/Repeats?")
                    }
                }
            }

            dis.close()
        } catch (e: IOException) {
            throw (RuntimeException("Error reading from script"))
        }
    }
}
