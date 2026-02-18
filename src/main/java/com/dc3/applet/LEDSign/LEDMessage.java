//------------------------------------------------------------------------------
//  LEDMessage.java	 -- LED Sign V3.1
//------------------------------------------------------------------------------

package com.dc3.applet.LEDSign;

import java.io.*;
import java.util.*;

class LEDMessage
{
	int letcol[];
	boolean msg[][];
	FuncInfo fi;
	int h,w;
	int WIDTH,HEIGHT,TOTAL;
	Letters let;
	Index index;

	public LEDMessage(int height, int width, Letters l)
	{
		h = height;
		w = width;
		HEIGHT = 5*h;
		WIDTH = 5*w;
		let = l;
	}

	void setmsg(FuncInfo f)
	{
		int a,b;
		int i,j,k;
		int p;
		int len;
		char c;

		fi = f;

		len = 0;
		for(i=0;i<fi.text.length();i++)
		{
			len += (let.getLetter(fi.text.charAt(i))).width+1;
		}

		if(fi.centered && len <= w)
		{
			a = w;
			a = a - len;
			a = a/2;
			fi.startspace = a;
			fi.endspace = a;
			if(a*2 < w)
				fi.startspace++;
		}

		TOTAL = len+fi.startspace+fi.endspace;

		msg = new boolean[TOTAL][h];

		for(i = 0; i < TOTAL; i++)
			for(j = 0; j < h; j++)
				msg[i][j] = false;

		letcol = new int[TOTAL];

		for(i=0;i<TOTAL;i++)
			letcol[i] = 1;

		p = fi.startspace;
		c = 'r';

		for(i=0;i<fi.text.length();i++)
		{
			index = let.getLetter(fi.text.charAt(i));

			if(fi.color.length() > 0)
				try
				{
					c = fi.color.charAt(i);
				}
				catch(IndexOutOfBoundsException e)
				{
					System.out.println("Out of bounds in LEDMessage.setmsg");
				}

			k = index.width;
			for(a=0;a<k;a++)
			{
				for(b=0;b<h;b++)
				{
					try
					{
						msg[p+a][b] = index.letter[a][b];
					}
					catch(IndexOutOfBoundsException e)
					{
					}

					if(c == 'r')
						letcol[p+a] = 1;
					else if(c == 'g')
						letcol[p+a] = 2;
					else if(c == 'b')
						letcol[p+a] = 3;
					else if(c == 'y')
						letcol[p+a] = 4;
					else if(c == 'o')
						letcol[p+a] = 5;
					else if(c == 'p')
						letcol[p+a] = 6;
					else if(c == 'w')
						letcol[p+a] = 7;
					else if(c == 'c')
						letcol[p+a] = 8;
				}
			}
			p += index.width+1;
		}
	}

	boolean getLED(int x, int y)
	{
		if(x >= 0 && x < TOTAL && y >= 0 && y < h)
			return msg[x][y];
		else
			return false;
	}

	int getColor(int x)
	{
		if(x >= 0 && x < TOTAL)
			return letcol[x];
		else
			return 1;
	}

	int length()
	{
		return TOTAL;
	}

	boolean inRange(int x)
	{
        return x >= 0 && x < TOTAL;
	}
}
