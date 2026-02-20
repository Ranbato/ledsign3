//------------------------------------------------------------------------------
//  FuncInfo.java   -- LED Sign V3.1
//
//  Contains the following classes:
//	FuncInfo	a class (struct) to hold all the
//				information for any function.
//------------------------------------------------------------------------------
package com.dc3.applet.LEDSign

import java.net.URL


class FuncInfo {
    
	var func: LEDFunction? = null
    
	var delay: Int = 0
    
	var startspace: Int = 0
    
	var endspace: Int = 0
    
	var times: Int = 0
    
	var remaining: Int = 0
    
	var centered: Boolean = false
    
	var color: String? = null
    
	var text: String? = null
    
	var store: String? = null
    
	var target: String? = null
    
	var script: String? = null
    
	var url: URL? = null
    
	var retIndex: Int = 0 // Index in script list for DO/REPEAT loop returns
}


