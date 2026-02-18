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

package com.dc3.applet.LEDSign;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.DayOfWeek;

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

public class Script
{
	private linkList list;
	linkList ptr,start;
	String scrpt;
	URL documentURL;
	boolean finished = false;

	public Script(URL url, String s) throws RuntimeException
	{
		scrpt = s;
		documentURL = url;
		initScript();
	}

	private String getParam(String s, String sub)
	{
		int i,j;
		String tmp;

		i = s.indexOf(sub);
		j = s.indexOf("text");

		if(j == -1 || i <= j)
		{
			if(i == -1)
				return null;
			else
			{
				tmp = s.substring(i);
				i = tmp.indexOf("=");
				if(i == -1)
				{
					System.out.println("Error in '"+sub+"' parameter in "+s);
					return null;
				}
				else
				{
					i++;
					if(sub.compareTo("text") == 0)
						tmp = tmp.substring(i);
					else
					{
						tmp = tmp.substring(i);
						if(tmp.indexOf(" ") != -1)
							tmp = tmp.substring(0,tmp.indexOf(" "));
					}
					tmp.trim();
					return tmp;
				}
			}
		}
		else
			return null;

	}

	private FuncInfo getFunc(String s)
	{
		int i;
		String tmp;
		FuncInfo fi = new FuncInfo();

		fi.func = -1;
		fi.delay = 40;
		fi.startspace = 10;
		fi.endspace = 20;
		fi.times = -1;
		fi.remaining = 0;
		fi.centered = false;
		fi.color = new String("");
		fi.text = new String("No text specified");
		fi.url = null;
		fi.target = new String("");
		fi.script = null;
		fi.ret = null;

		s = s.trim();

		String tmp2;
		tmp2 = getParam(s,"delay");
		if(tmp2 != null)
			fi.delay = (new Integer(tmp2)).intValue();

		tmp2 = getParam(s,"clear");
		if(tmp2 != null && tmp2.compareTo("true") == 0)
		{
			fi.centered = true;
			fi.text = new String("");
		}
		else
		{
			tmp2 = getParam(s,"center");
			if(tmp2 != null && tmp2.compareTo("true") == 0)
				fi.centered = true;
			else
			{
				fi.centered = false;
				tmp2 = getParam(s,"startspace");
				if(tmp2 != null)
					fi.startspace = (new Integer(tmp2)).intValue();

				tmp2 = getParam(s,"endspace");
				if(tmp2 != null)
					fi.endspace = (new Integer(tmp2)).intValue();
			}

			tmp2 = getParam(s,"text");
			if(tmp2 != null)
				fi.text = tmp2;
		}

		tmp2 = getParam(s,"times");
		if(tmp2 != null)
		{
			fi.times = (new Integer(tmp2)).intValue();
			fi.remaining = fi.times;
		}

		tmp2 = getParam(s,"pixels");
		if(tmp2 != null)
		{
			fi.times = (new Integer(tmp2)).intValue();
			fi.remaining = fi.times;
		}

		tmp2 = getParam(s,"URL");
		if(tmp2 != null)
		{
			if(tmp2.indexOf(',') != -1)
			{
				fi.target = tmp2.substring(tmp2.indexOf(',') + 1);
				tmp2 = tmp2.substring(0,tmp2.indexOf(','));
			}

			try
			{
				fi.url = new URL(tmp2);
			}
			catch(MalformedURLException e)
			{
				System.out.println("Bad URL: "+tmp2);
				fi.url = null;
			}
		}

		fi.script = getParam(s,"script");

		i = s.indexOf(" ");
		if(i != -1)
			tmp = s.substring(0,i);
		else
			tmp = s;

		if(tmp.compareTo("Appear") == 0)
		{
			fi.func = 0;
		}
		else if(tmp.compareTo("Sleep") == 0)
		{
			fi.func = 1;
		}
		else if(tmp.compareTo("ScrollLeft") == 0)
		{
			fi.func = 2;
		}
		else if(tmp.compareTo("ScrollRight") == 0)
		{
			fi.func = 3;
		}
		else if(tmp.compareTo("ScrollUp") == 0)
		{
			fi.func = 4;
		}
		else if(tmp.compareTo("ScrollDown") == 0)
		{
			fi.func = 5;
		}
		else if(tmp.compareTo("Pixel") == 0)
		{
			fi.func = 6;
			if(fi.delay < 1)
				fi.delay = 1;
			if(fi.times < 1)
				fi.times = 15;
		}
		else if(tmp.compareTo("Blink") == 0)
		{
			fi.func = 7;
			if(fi.times < 1)
				fi.times = 2;
		}
		else if(tmp.compareTo("OverRight") == 0)
		{
			fi.func = 8;
		}
		else if(tmp.compareTo("ScrollCenter") == 0)
		{
			fi.func = 9;
		}
		else if(tmp.compareTo("OverCenter") == 0)
		{
			fi.func = 10;
		}
		else if(tmp.compareTo("OverLeft") == 0)
		{
			fi.func = 11;
		}
		else if(tmp.compareTo("OverUp") == 0)
		{
			fi.func = 12;
		}
		else if(tmp.compareTo("OverDown") == 0)
		{
			fi.func = 13;
		}
		else if(tmp.compareTo("Do") == 0)
		{
			fi.func = 97;
		}
		else if(tmp.compareTo("Repeat") == 0)
		{
			fi.func = 98;
		}
		else if(tmp.compareTo("Reload") == 0)
		{
			fi.func = 99;
		}
		else if(tmp.compareTo("Chain") == 0)
		{
			fi.func = 100;
		}

		fi.store = fi.text;

		return fi;
	}

	public FuncInfo nextFunc()
	{
		FuncInfo fi;

		fi = ptr.fi;
		if(fi == null)
		{
			return(null);
		}
		ptr = ptr.next;

		switch(fi.func)
		{
			case 97:
				fi = nextFunc();
				break;
			case 98:
				if(fi.times >= 0)
				{
					fi.remaining--;
					if(fi.remaining <= 0)
					{
						fi.remaining = fi.times;
						fi = nextFunc();
					}
					else
					{
						ptr = fi.ret;
						fi = nextFunc();
					}
				}
				else
				{
					ptr = fi.ret;
					fi = nextFunc();
				}
				break;
			case 100:
				scrpt = fi.script;
			case 99:
				initScript();
				fi = nextFunc();
				break;
		}

		return fi;
	}

	private boolean isColor(char t)
	{
		return(t == 'r' || t == 'g' || t == 'b' || t == 'y' || t == 'o' ||
				t == 'p' || t == 'w' || t == 'c');
	}

	public FuncInfo parseLine(FuncInfo fi)
	{
		String tmp;
		String time;
		String month[] = {"Jan","Feb","Mar","Apr","May","Jun",
								"Jul","Aug","Sept","Oct","Nov","Dec"};
		String Month[] = {"January","February","March","April","May","June",
								"July","August","September","October","November","December"};
		String day[] = {"Sun","Mon","Tues","Wed","Thur","Fri","Sat"};
		String Day[] = {"Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"};
		String ddmmyy;
		int min;
		int pm;
		// Replace legacy java.util.Date with java.time.LocalDateTime
		LocalDateTime date = LocalDateTime.now();
		int a,b;
		int i;
		char c;
		String t;

		tmp = fi.store;
		fi.color = "";

		if(fi.func == 0 || (fi.func >= 2 && fi.func <= 97))
		{
			c = 'r';
			b = 0;
			while(b < tmp.length())
			{
				if(tmp.charAt(b) == '\\')
				{
					b++;
					if(tmp.charAt(b) == '{')
					{
						t = tmp.substring(b+1);
						tmp = tmp.substring(0,b-1).concat(t.substring(t.indexOf('}')+1));
						t = t.substring(0,t.indexOf('}'));
						b -= 1;
					}
					else
					{
						t = tmp.substring(b,b+1);
						tmp = (tmp.substring(0,b-1)).concat(tmp.substring(b+1));
						b -= 1;
					}

					if(t.length() == 1 && isColor(t.charAt(0)))
					{
						c = t.charAt(0);
					}
					else if(t.compareTo("tt") == 0)
					{
						if(date.getHour() >= 12)
							pm = 1;
						else
							pm = 0;

						if(pm == 1)
						{
							a = date.getHour();
							if(a == 12)
								time	= String.valueOf(12);
							else
								time = String.valueOf(date.getHour()-12);
						}
						else
						{
							a = date.getHour();
							if(a == 0)
								time = String.valueOf(12);
							else
								time = String.valueOf(a);
						}

						time = time.concat(":");

						min = date.getMinute();
						if(min >= 10)
							time = time.concat(String.valueOf(min));
						else
						{
							time = time.concat("0");
							time = time.concat(String.valueOf(min));
						}

						if(pm == 1)
							time = time.concat(" pm");
						else
							time = time.concat(" am");

						tmp = ((tmp.substring(0,b)).concat(time)).concat(tmp.substring(b));

						b += time.length();

						for(i = 0; i < time.length(); i++)
							fi.color = (fi.color).concat((new Character(c)).toString());

					} // End time
					else if(t.compareTo("dd") == 0 || t.compareTo("DD") == 0)    // day name
					{
						// LocalDateTime.getDayOfWeek returns MONDAY..SUNDAY
						int dow = date.getDayOfWeek().getValue() % 7; // maps Sunday->0, Monday->1
						if(t.compareTo("dd") == 0)
							ddmmyy = day[dow];
						else
							ddmmyy = Day[dow];

						for(i = 0; i < ddmmyy.length(); i++)
							fi.color = (fi.color).concat((new Character(c)).toString());

						tmp = ((tmp.substring(0,b)).concat(ddmmyy)).concat(tmp.substring(b));
						b += ddmmyy.length();
					}
					else if(t.compareTo("dn") == 0)
					{
						ddmmyy = String.valueOf(date.getDayOfMonth());

						for(i = 0; i < ddmmyy.length(); i++)
							fi.color = (fi.color).concat((new Character(c)).toString());

						tmp = ((tmp.substring(0,b)).concat(ddmmyy)).concat(tmp.substring(b));
						b += ddmmyy.length();
					}
					else if(t.compareTo("mm") == 0 || t.compareTo("MM") == 0)
					{
						int monthIndex = date.getMonthValue() - 1; // 0-based index
						if(monthIndex < 0 || monthIndex > 11) monthIndex = 0;
						if(t.compareTo("mm") == 0)
							ddmmyy = month[monthIndex];
						else
							ddmmyy = Month[monthIndex];

						for(i = 0; i < ddmmyy.length(); i++)
							fi.color = (fi.color).concat((new Character(c)).toString());

						tmp = ((tmp.substring(0,b)).concat(ddmmyy)).concat(tmp.substring(b));
						b += ddmmyy.length();
					}
					else if(t.compareTo("mn") == 0)
					{
						ddmmyy = String.valueOf(date.getMonthValue());

						for(i = 0; i < ddmmyy.length(); i++)
							fi.color = (fi.color).concat((new Character(c)).toString());

						tmp = ((tmp.substring(0,b)).concat(ddmmyy)).concat(tmp.substring(b));
						b += ddmmyy.length();
					}
					else if(t.compareTo("yy") == 0 || t.compareTo("YY") == 0)
					{
						if(t.compareTo("YY") == 0)
							ddmmyy = String.valueOf(date.getYear());
						else
							ddmmyy = String.valueOf(date.getYear()%100);

						for(i = 0; i < ddmmyy.length(); i++)
							fi.color = (fi.color).concat((new Character(c)).toString());

						tmp = ((tmp.substring(0,b)).concat(ddmmyy)).concat(tmp.substring(b));
						b += ddmmyy.length();

					}
					else if(t.compareTo("\\") == 0)
					{
						tmp = (tmp.substring(0,b)).concat(tmp.substring(b+1));
						b--;
					}
					else
					{
						System.out.println("Backslash (\\) error in text line: "+ fi.store);
					}

				}
				else
				{
					b++;
					fi.color = fi.color.concat((new Character(c)).toString());
				}

			}    // END - for(...)

		} // END - if(fi.func == ...)

		fi.text = tmp;

		return fi;

	}

	private void initScript() throws RuntimeException
	{
		URLConnection urlc;
		DataInputStream dis;
		URL url;
		String line;
		int listlen;
		int dos;
		int a;

		try {
			url = new URL(documentURL,scrpt);
			urlc = url.openConnection();
			dis = new DataInputStream(urlc.getInputStream());
		} catch(Exception e) {
			throw(new RuntimeException("Failed to connect to host for script"));
		}

		try
		{
			list = new linkList();
			start = list;
			ptr = list;
			listlen = 0;
			dos = 0;
			while((line = dis.readLine()) != null)
			{
				line = line.trim();
				if(!(line.startsWith("!!")) && (line.length() != 0))
				{
					listlen++;
					ptr.fi = getFunc(line);
					if(ptr.fi.func == 97)
						dos++;
					ptr.next = new linkList();
					ptr = ptr.next;
				}
			}

			ptr = start;
			linkList stack[] = new linkList[dos];
			dos = 0;
			for(a=0;a<listlen;a++)
			{
				if(ptr.fi.func == 97)
				{
					stack[dos] = new linkList();
					stack[dos] = ptr;
					dos++;
				}
				else if(ptr.fi.func == 98)
				{
					if(dos > 0)
					{
						dos--;
						ptr.fi.ret = stack[dos];
					}
					else
					{
						System.out.println("Repeat error in line : Repeat times="+ptr.fi.times);
						System.out.println(" 	 Mismatched Do/Repeats?");
					}
				}
				ptr = ptr.next;
			}

			ptr = start;

			dis.close();
		}
		catch (IOException e)
		{
			throw(new RuntimeException("Error reading from script"));
		}
	}
}
