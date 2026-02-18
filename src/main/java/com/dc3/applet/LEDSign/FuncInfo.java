//------------------------------------------------------------------------------
//  FuncInfo.java   -- LED Sign V3.1
//
//  Contains the following classes:
//	FuncInfo	a class (struct) to hold all the
//				information for any function.
//------------------------------------------------------------------------------

package com.dc3.applet.LEDSign;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.net.*;

public class FuncInfo
{
	public int func;
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
	public linkList ret;
}
