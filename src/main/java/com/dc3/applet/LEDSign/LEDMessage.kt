//------------------------------------------------------------------------------
//  LEDMessage.java	 -- LED Sign V3.1
//------------------------------------------------------------------------------
package com.dc3.applet.LEDSign

class LEDMessage
    (var h: Int, var w: Int, var let: Letters) {
    var letcol: IntArray? = null
    var msg: Array<BooleanArray?>? = null
    var fi: FuncInfo? = null
    var WIDTH: Int
    var HEIGHT: Int
    var TOTAL: Int = 0
    var index: Index? = null

    init {
        HEIGHT = 5 * h
        WIDTH = 5 * w
    }

    fun setmsg(f: FuncInfo) {
        var a: Int
        var b: Int
        var i: Int
        var j: Int
        var k: Int
        var p: Int
        var len: Int
        var c: Char

        fi = f

        len = 0
        i = 0
        while (i < fi!!.text!!.length) {
            len += (let.getLetter(fi!!.text!!.get(i)))?.width?.plus(1) ?: 0
            i++
        }

        if (fi!!.centered && len <= w) {
            a = w
            a = a - len
            a = a / 2
            fi!!.startspace = a
            fi!!.endspace = a
            if (a * 2 < w) fi!!.startspace++
        }

        TOTAL = len + fi!!.startspace + fi!!.endspace

        msg = Array<BooleanArray?>(TOTAL) { BooleanArray(h) }

        i = 0
        while (i < TOTAL) {
            j = 0
            while (j < h) {
                msg!![i]!![j] = false
                j++
            }
            i++
        }

        letcol = IntArray(TOTAL)

        i = 0
        while (i < TOTAL) {
            letcol!![i] = 1
            i++
        }

        p = fi!!.startspace
        c = 'r'

        i = 0
        while (i < fi!!.text!!.length) {
            index = let.getLetter(fi!!.text!!.get(i))

            if (!fi!!.color!!.isEmpty()) try {
                c = fi!!.color!!.get(i)
            } catch (e: IndexOutOfBoundsException) {
                println("Out of bounds in LEDMessage.setmsg")
            }

            k = index!!.width
            a = 0
            while (a < k) {
                b = 0
                while (b < h) {
                    try {
                        msg!![p + a]!![b] = index!!.letter!![a]!![b]
                    } catch (e: IndexOutOfBoundsException) {
                    }

                    if (c == 'r') letcol!![p + a] = 1
                    else if (c == 'g') letcol!![p + a] = 2
                    else if (c == 'b') letcol!![p + a] = 3
                    else if (c == 'y') letcol!![p + a] = 4
                    else if (c == 'o') letcol!![p + a] = 5
                    else if (c == 'p') letcol!![p + a] = 6
                    else if (c == 'w') letcol!![p + a] = 7
                    else if (c == 'c') letcol!![p + a] = 8
                    b++
                }
                a++
            }
            p += index!!.width + 1
            i++
        }
    }

    fun getLED(x: Int, y: Int): Boolean {
        if (x >= 0 && x < TOTAL && y >= 0 && y < h) return msg!![x]!![y]
        else return false
    }

    fun getColor(x: Int): Int {
        if (x >= 0 && x < TOTAL) return letcol!![x]
        else return 1
    }

    fun length(): Int {
        return TOTAL
    }

    fun inRange(x: Int): Boolean {
        return x >= 0 && x < TOTAL
    }
}
