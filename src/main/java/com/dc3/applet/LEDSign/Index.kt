// ...existing code...
package com.dc3.applet.LEDSign

class Index
internal constructor( var ch: Byte,  var width: Int, h: Int) {
    
	var letter: Array<BooleanArray?>? = Array(width) { BooleanArray(h) }
}


