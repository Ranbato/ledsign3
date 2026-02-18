//------------------------------------------------------------------------------
//  Letters.java	 -- LED Sign V3.1
//
//  This class parses the font file and stores
//  each letter in an array of boolean (on/off).
//  It takes care of all the storage and
//  retrieval of letters data structure.
//------------------------------------------------------------------------------

package com.dc3.applet.LEDSign;

import java.awt.*;
import java.io.*;
import java.net.*;

//////////////////////////////////////////////////////////////////
// The Letters Class
//////////////////////////////////////////////////////////////////
public class Letters
{
	int HEIGHT,TOTAL;
	String let;
	String path;
	URL url;
	URLConnection urlc;
	DataInputStream dis;
	int w,h,num,place,len,space,swidth;
	Index index[];

	public Letters(URL url, String URLfile, int width) throws RuntimeException
	{
		try {
			urlc = (new URL(url,URLfile)).openConnection();
			dis = new DataInputStream(urlc.getInputStream());
			path = URLfile;
			swidth = width;
		} catch(IOException e) {
			throw(new RuntimeException("Failed to connect to host for font"));
		}

		if(urlc.getContentType().equals("text/html"))
			throw(new RuntimeException("Font not found (or inaccessible) on host"));

		try {
			initLetters();
		} catch(IOException e) {
			throw(new RuntimeException("Failed to read font data"));
		}
	}

	public int height()
	{
		return HEIGHT;
	}

	void initLetters() throws IOException
	{
		int a,b,c;
		byte ch;
		int i,j,k;
		String s;
		boolean done;
		int width;

		w = 5;
		h = 5;
		num = 100;

		done = false;
		while(!done)
		{
			s = dis.readLine();
			if(!s.startsWith("!!"))
			{
				h = (new Integer(s.trim())).intValue();
				HEIGHT = h;
				done = true;
			}
		}

		done = false;
		while(!done)
		{
			s = dis.readLine();
			if(!s.startsWith("!!"))
			{
				w = (new Integer(s.trim())).intValue();
				done = true;
			}
		}

		done = false;
		while(!done)
		{
			s = dis.readLine();
			if(!s.startsWith("!!"))
			{
				num = (new Integer(s.trim())).intValue();
				done = true;
			}
		}

		index = new Index[num+1];

		for(i=0;i<num;i++)
		{
			ch = 2;
			width = 10;

			done = false;
			while(!done)
			{
				s = dis.readLine();
				if(!s.startsWith("!!"))
				{
					ch = (byte)s.charAt(0);
					done = true;
				}
			}
			done = false;
			while(!done)
			{
				s = dis.readLine();
				if(!s.startsWith("!!"))
				{
					width = (new Integer(s.trim())).intValue();
					done = true;
				}
			}

			index[i] = new Index(ch,width,h);

			for(j=0;j<h;j++)
			{
				done = false;
				s = "";
				while(!done)
				{
					s = dis.readLine();

					if(s.length() > 0)
					{
						if(!s.startsWith("!!"))
						{
							done = true;
						}
					}
					else
					{
						s = " ";
						done = true;
					}
				}

				for(k=0;k<index[i].width;k++)
				{
					if(k>=s.length())
					{
						index[i].letter[k][j] = false;
					}
					else
					{
						if(s.charAt(k) == '#')
							index[i].letter[k][j] = true;
						else
							index[i].letter[k][j] = false;
					}
				}
			}
		}

		index[num] = new Index((byte)32,swidth,h);

		dis.close();
	}

	public Index getLetter(char c)
	{
		int j;

		if(c == (char)(32))
		{
			j = num;
		}
		else
		{
			j = 0;
			while(c != index[j].ch && j < num)
				j++;
		}

		return index[j];
	}
}
