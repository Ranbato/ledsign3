// ...existing code...
package com.dc3.applet.LEDSign;

public class Index
{
	public byte ch;
	public int width;
	public boolean[][] letter;

	Index(byte b, int w, int h)
	{
		letter = new boolean[w][h];
		width = w;
		ch = b;
	}
}


