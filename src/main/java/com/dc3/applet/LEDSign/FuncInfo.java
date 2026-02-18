//------------------------------------------------------------------------------
//  FuncInfo.java   -- LED Sign V3.1
//
//  Contains the following classes:
//	FuncInfo	a class (struct) to hold all the
//				information for any function.
//------------------------------------------------------------------------------

package com.dc3.applet.LEDSign;


import java.net.*;
import java.util.*;

public class FuncInfo
{
	public LEDFunction func;
	public int delay;
	public int startspace, endspace;
	public int times, remaining;
	public boolean centered;
	public String color;
	public String text;
	public String store;
	public String target;
	public String script;
	public URL url;
	public int retIndex;  // Index in script list for DO/REPEAT loop returns
}


