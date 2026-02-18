//tabs=4
//-----------------------------------------------------------------------------
//  LED.java  -- LED Sign V3.1
//
//  The main for the LED Sign applet. This applet mimics
//  an LED sign that you typically see displaying messages
//  at airport terminals and the such.
//
//  Revisions:
//	V3.1	Converted to package
//			Modified 11-Aug-96 by Robert B. Denny
//
//	V3.0:...
//-----------------------------------------------------------------------------

package com.dc3.applet.LEDSign;

import java.awt.*;
import java.io.*;
import java.net.*;

// Just a small struct
// used in randomizing the pixels in the "Pixel" function
class Pixelize
{
	int x;
	int y;
}

//-----------------------------------------------------------------------------
// The java.applet.Applet
//-----------------------------------------------------------------------------
public class LED extends java.applet.Applet implements Runnable
{
	// my #defines
	int WIDTH = 400;
	int HEIGHT = 30;
	String appletName = "LED Sign V3.1";

	Script scr; 					// The class that takes care of the script
	FuncInfo fi;					// All the info for any funtion/transition
	Letters let; 					// The class that contains all the letters
	int ledsize; 					// The size of the LEDs

	Color highlight; 				// Color used for highlight on large LEDs
	Color colors[]; 				// The array of possible colors
	LEDMessage msg; 				// The class that takes care of the message to be displayed
	Color bhilite; 					// Normal border hilite color
	Color bcolor; 					// Normal border color
	Color bshadow; 					// Normal border shadow color
	Color h_bhilite; 				// Border hilite if message has URL
	Color h_bcolor; 				// Border color if message has URL
	Color h_bshadow; 				// Border shadow if message has URL
	boolean smooth_leds = true; 		// true if want real circular LEDs in larger sizes

	// Replace 'Thread led' with safer naming and a running flag
	private Thread ledThread = null;              // the thread that runs the applet
	private volatile boolean running = false;     // controls the run loop

	String scrpt,endspace,fnt; 		// "command line" arguments
	String text; 					// the current message
	String currurl = appletName; 	// The current url that are set in the script
	URL currURL = null;
	URL lastURL = null;
	String target = new String("");
	int place;
	int border; 					// The border width
	int offset; 					// The offset for the sign from the upper left
	int w,h; 						// Width & Height in LEDs
	int swidth; 					// The width of the space character.  Settable in the HTML command line.
	boolean beginning = false;
	boolean init = false; 			// used to make sure "getinfo" is called only once.
	boolean inapplet; 			// Is the mouse cursor in the applet?  (used to display status messages)
	boolean done = false; 			// Is the transition done?
	Image pixmapimg,offimg,tmpimg; 	// The pixmaps!!
	Graphics pixmap,offmap,tmpmap; 	// Graphics for the pixmaps
	Pixelize pix[]; 				// Array of "pixels" used during the Pixel transition

	//--------------------------------------------------------------------------
	// PRIVATE void getAttrs() - Get the command arguments from the HTML
	//--------------------------------------------------------------------------
	private void getAttrs()
			throws RuntimeException 				// Not really needed, but...
	{
		String s;
		int r,g,b;
		Graphics gr;

		if(getParameter("script") !=	null) {
			scrpt = new String(getParameter("script"));
		} else {
			throw(new RuntimeException("No script specified in HTML."));
		}

		if(getParameter("font") !=  null)
		{
			fnt = new String(getParameter("font"));
		} else {
			throw(new RuntimeException("No font specified in HTML."));
		}

		if(getParameter("spacewidth") !=  null)
		{
			swidth = (new Integer(new String(getParameter("spacewidth")))).intValue();
		}
		else
			swidth = 3;

		if(getParameter("ledsize") !=  null)
		{
			ledsize = new Integer(new String(getParameter("ledsize"))).intValue();

			// A little error trapping
			if(ledsize < 1)
				ledsize = 1;
			else if(ledsize > 4)
				ledsize = 4;

			ledsize++; 	  // The user enters 1-4, the applet needs 2-5
		}
		else
			ledsize = 4;

		if(getParameter("ht") != null)
		{
			HEIGHT = ledsize*(new Integer(new String(getParameter("ht")))).intValue();
			h = HEIGHT/ledsize;
		}
		else
		{
			System.out.println("LED Sign Warning: parameter \"ht\" not specified");
			HEIGHT = ledsize*9;
			h = 9;
		}

		if(getParameter("wth") != null)
		{
			WIDTH = ledsize*(new Integer(new String(getParameter("wth")))).intValue();
			if(WIDTH/ledsize%2 == 1)
				WIDTH += ledsize;  // It must be even!!!

			w = WIDTH/ledsize;
		}
		else
		{
			System.out.println("LED Sign Warning: parameter \"wth\" not specified");
			WIDTH = 60*ledsize;
			w = 60;
		}

		if(getParameter("border") != null)
		{
			border = new Integer(new String(getParameter("border"))).intValue();
		}
		else
			border = 0;

		if(getParameter("bordercolor") != null)
		{
			// User specified border color!!
			s = new String(getParameter("bordercolor"));
			s = s.trim();
			r = new Integer(s.substring(0,s.indexOf(","))).intValue();
			s = s.substring(s.indexOf(",")+1);
			g = new Integer(s.substring(0,s.indexOf(","))).intValue();
			s = s.substring(s.indexOf(",")+1);
			b = new Integer(s).intValue();

			bhilite = new Color(r+40<256?r+40:255, g+40<256?g+40:255, b+40<256?b+40:255);
			bcolor = new Color(r,g,b);
			bshadow = new Color(r-40>=0?r-40:0, g-40>=0?g-40:0, b-40>=0?b-40:0);
		}
		else
		{
			bhilite = Color.white;
			bcolor = Color.lightGray;
			bshadow = Color.darkGray;
		}

		if(getParameter("hot_bordercolor") != null)
		{
			s = new String(getParameter("hot_bordercolor"));
			s = s.trim();
			r = new Integer(s.substring(0,s.indexOf(","))).intValue();
			s = s.substring(s.indexOf(",")+1);
			g = new Integer(s.substring(0,s.indexOf(","))).intValue();
			s = s.substring(s.indexOf(",")+1);
			b = new Integer(s).intValue();

			h_bhilite = new Color(r+40<256?r+40:255, g+40<256?g+40:255, b+40<256?b+40:255);
			h_bcolor = new Color(r,g,b);
			h_bshadow = new Color(r-40>=0?r-40:0, g-40>=0?g-40:0, b-40>=0?b-40:0);
		}
		else
		{
			h_bhilite = new Color(128, 128, 255);
			h_bcolor = new Color(0, 0, 255);
			h_bshadow = new Color(0,0, 128);
		}

		if(getParameter("smooth_leds") != null)
		{
			s = new String(getParameter("smooth_leds"));
			s = s.trim();
			if(s.equals("true")) {
				smooth_leds = true;
			} else {
				smooth_leds = false;
			}
		}
		else
		{
			smooth_leds = true;
		}

	} // end getAttrs()

	//--------------------------------------------------------------------------
	// PUBLIC init() - The initialization entry for the applet
	//--------------------------------------------------------------------------
	public void init()
	{
		boolean ok = true;

		highlight = new Color(100,100,100);
		colors = new Color[9];
		colors[0] = new Color(64,64,64);
		colors[1] = new Color(255,64,64);
		colors[2] = new Color(130,255,0);
		colors[3] = new Color(0,130,255);
		colors[4] = new Color(255,255,0);
		colors[5] = new Color(255,160,0);
		colors[6] = new Color(255,0,255);
		colors[7] = new Color(255,255,255);
		colors[8] = new Color(0,255,255);

		try {
			getAttrs();
		} catch(RuntimeException e) {
			currurl = new String("LED Sign Error: " + e.getMessage());
			System.out.println(currurl);
			stop();
		}
		offset = 3*border;
		beginning = true;
		init = true;
	}

	//--------------------------------------------------------------------------
	// PRIVATE void runInit() - Second-phase initialization
	//--------------------------------------------------------------------------
	private void runInit() throws RuntimeException
	{
		Rectangle r1, r2;

		pix = new Pixelize[1];
		let = new Letters(getDocumentBase(),fnt,swidth);
		if(HEIGHT != let.height()*ledsize)
		{
			System.out.println("LED Sign Warning: parameter \"ht\" should be set to " + let.height() + ".");
		}

		msg = new LEDMessage(h,w,let);
		scr = new Script(getDocumentBase(),scrpt);
		fi = new FuncInfo();
		nextFunc();

		r1 = bounds();
		resize(WIDTH+2*(offset),HEIGHT+2*(offset));
		r2 = bounds();
		if((r1.width != r2.width) || (r1.height != r2.height))
			System.out.println("Applet resized to w=" + r2.width + " h=" + r2.height);
		init = false;

	}

	//--------------------------------------------------------------------------
	// Start the applet running and thread the process
	//--------------------------------------------------------------------------
	public void start()
	{
		if (ledThread == null)
		{
			running = true;
			ledThread = new Thread(this, "LEDSign-Thread");
			ledThread.setDaemon(true);
			ledThread.start();
		}
	}

	//////////////////////////////////////////////////////////////////
	// Stop the thread - perform a cooperative shutdown
	public void stop()
	{
		if (ledThread != null)
		{
			// Request shutdown
			running = false;
			// Interrupt the thread to wake it if sleeping
			ledThread.interrupt();

			// If stop() is called from a different thread, join briefly
			if (Thread.currentThread() != ledThread)
			{
				try { ledThread.join(500); } catch (InterruptedException ignored) { }
				ledThread = null;
			}
			else
			{
				// If called from the running thread, just return and let run() exit
			}
		}
	}

	//--------------------------------------------------------------------------
	// The run loop
	//--------------------------------------------------------------------------
	public void run()
	{
		try {
			if(init) {
				try {
					runInit();
				} catch(RuntimeException e) {
					currurl = new String("LED Sign Error: " + e.getMessage());
					System.out.println(currurl);
					showStatus(currurl);
					// request cooperative shutdown
					running = false;
					// avoid calling stop() here because stop() may try to join this thread
					return;
				}
			}

			while(running)
			{
				repaint();

				try {
					Thread.sleep(fi.delay);
				} catch (InterruptedException e) {
					// Interrupted: if running flag was cleared, exit loop
					if(!running) break;
				}

				if(done)
				{
					try {
						nextFunc();
					} catch(RuntimeException e) {
						currurl = "LED Sign error: " + e.getMessage();
						System.out.println(currurl);
						showStatus(currurl);
						running = false;
						break;
					}

					if(fi == null) {
						currurl = "Script finished";
						System.out.println(currurl);
						showStatus(currurl);
						running = false;
						break;
					}
					done = false;
				}
			}
		} finally {
			// Clean up thread reference on exit
			if (Thread.currentThread() == ledThread)
				ledThread = null;
		}
	}

	//--------------------------------------------------------------------------
	// The HTML tag parameter information
	//--------------------------------------------------------------------------
	public String[][] getParameterInfo() {
		String[][] info = {
			{"script         ","URL        ", "LED script to use (Required)"},
			{"font           ","URL        ", "Font to use (Required)"},
			{"spacewidth     ","int        ", "Width of space in columns, default=3 )"},
			{"wth            ","int        ", "Width of live display (cols, default=60)"},
			{"ht             ","int        ", "Height of live display (rows, default=9)"},
			{"border         ","int        ", "Width of display border (pix, default=0)"},
			{"bordercolor    ","int,int,int", "Color of border (n,n,n default=lightGray)"},
			{"hot_bordercolor","int,int,int", "Color of hot border (n,n,n default=blue)"},
			{"ledsize        ","int        ", "Diameter of LEDs pixels (1-4), default=3)"},
			{"smooth_leds    ","boolean    ", "True circles for larger size LEDs (default=false)"}
		};
		return info;
	}

	//--------------------------------------------------------------------------
	// The "about" stuff.
	//--------------------------------------------------------------------------
	public String getAppletInfo() {
		return(appletName + " by Darrick Brown and Bob Denny");
	}

	//--------------------------------------------------------------------------
	// Trap for a mouse click on the applet to check to see if they
	// want to go to another page.
	//--------------------------------------------------------------------------
	public boolean mouseDown(java.awt.Event evt, int x, int y)
	{
		if (currURL != null)
		{
			if(target.length() > 0)  {
				getAppletContext().showDocument(currURL,target);
			} else {
				getAppletContext().showDocument(currURL);
			}
		}

		return true;
	}

	//--------------------------------------------------------------------------
	// If the mouse cursor enters the applet, then display something
	// in the status bar of the browser.
	//--------------------------------------------------------------------------
	public boolean mouseEnter(java.awt.Event evt, int x, int y)
	{
		inapplet = true;

		showStatus(currurl);

		return true;
	}

	//--------------------------------------------------------------------------
	// If the mouse cursor exits the applet, then clear the status
	// bar.
	//--------------------------------------------------------------------------
	public boolean mouseExit(java.awt.Event evt, int x, int y)
	{
		inapplet = false;

		showStatus(" ");

		return true;
	}

	//--------------------------------------------------------------------------
	// PRIVATE void nextVunc() - Set the next function
	//--------------------------------------------------------------------------
	private void nextFunc()
	{
		int i,j;
		Pixelize temp;
		int rand;

		fi = scr.nextFunc();

		if(fi == null) {
			return;
		}

		fi = scr.parseLine(fi);

		msg.setmsg(fi);

		if(fi.url != null)
		{
			currurl = fi.url.toString();
			currURL = fi.url;
			target = fi.target;
		}
		else
		{
			currurl = appletName;
			currURL = null;
			target = new String("");
		}

		if(inapplet)
		{
			showStatus(currurl);
		}

		switch(fi.func)
		{
			case 0:
				place = 0;
				break;
			case 1:
				place = 0;
				break;
			case 2:
				place = 0;
				break;
			case 3:
				place = msg.length()-1;
				break;
			case 4:
				place = 0;
				break;
			case 5:
				place = h-1;
				break;
			case 6:
				place = 0;

				pix = new Pixelize[w*h];

				for(i=0;i<w;i++)
				{
					for(j=0;j<h;j++)
					{
						pix[h*i+j] = new Pixelize();
						pix[h*i+j].x = i;
						pix[h*i+j].y = j;
					}
				}

				for(i=0;i<WIDTH/ledsize*h;i++)
				{
					rand = (int)(Math.random()*(double)(WIDTH/ledsize)*(double)h);
					temp = pix[i];
					pix[i] = pix[rand];
					pix[rand] = temp;
				}
				break;
			case 7:
				place = fi.times*2;
				break;
			case 8:
				place = 0;
				break;
			case 9:
				place = 0;
				break;
			case 10:
				place = 0;
				break;
			case 11:
				place = w;
				break;
			case 12:
				place = h-1;
				break;
			case 13:
				place = 0;
				break;
		}
	}

	//--------------------------------------------------------------------------
	// Draw a pretty little LED
	//--------------------------------------------------------------------------
	private void drawLED(int x, int y, boolean on, int col, Graphics gr)
	{
		if(on)
		{
			gr.setColor(colors[col]);
		}
		else
		{
			gr.setColor(colors[0]);
		}

		switch(ledsize)
		{
			case 2:
				gr.drawLine(x,y,x,y);
				break;

			case 3:
				gr.fillRect(x,y,2,2);
				break;

			case 4:
				if(smooth_leds) {
					gr.fillOval(x, y, 3, 3);
				} else {
					gr.drawLine(x,y+1,x+2,y+1);
					gr.drawLine(x+1,y,x+1,y+2);
				}
				break;

			case 5:
				if(smooth_leds) {
					gr.fillOval(x, y, 4, 4);
				} else {
					gr.fillRect(x+1,y,2,4);
					gr.fillRect(x,y+1,4,2);
				}
				break;
		}

		if(ledsize == 5 && !on && !smooth_leds)
		{
			gr.setColor(highlight);
			gr.drawLine(x+1,y+1,x+1,y+1);
		}
	}

	//--------------------------------------------------------------------------
	// PRIVATE void draw3DRect() - Draw a 3D rect with variable line width
	//--------------------------------------------------------------------------
	private void draw3DRect(Graphics gr, int x, int y, int lx, int ly,
							int width, boolean raised)
	{
		int i;

		for(i=0; i<width; i++)
		{
			Color c1 = (currURL != null) ? h_bhilite : bhilite;
			Color c2 = (currURL != null) ? h_bshadow : bshadow;

			if(raised)
				gr.setColor(c1);
			else
				gr.setColor(c2);

			gr.drawLine(x+i,y+i,lx-i,y+i);
			gr.drawLine(x+i,y+i,x+i,ly-i);

			if(raised)
				gr.setColor(c2);
			else
				gr.setColor(c1);

			gr.drawLine(lx-i,y+i,lx-i,ly-i);
			gr.drawLine(x+i,ly-i,lx-i,ly-i);
		}
	}

	//--------------------------------------------------------------------------
	// PRIVATE void drawWideRect() - Draw a rectangle with variable line width
	//--------------------------------------------------------------------------
	private void drawWideRect(Graphics gr, int x, int y, int lx, int ly, int width)
	{
		int i;

		gr.setColor((currURL != null) ? h_bcolor : bcolor);
		for(i=0; i<width; i++)
		{
			gr.drawLine(x+i,y+i,lx-i,y+i);
			gr.drawLine(x+i,y+i,x+i,ly-i);
			gr.drawLine(lx-i,y+i,lx-i,ly-i);
			gr.drawLine(x+i,ly-i,lx-i,ly-i);
	  }
	}

	//--------------------------------------------------------------------------
	// PRIVATE void drawFrame() - Draw the frame
	//--------------------------------------------------------------------------
	private void drawFrame(Graphics gr)
	{
		if(border > 0) {
			draw3DRect(gr,0,0,WIDTH+2*offset-1,HEIGHT+2*offset-1,border,true);
			drawWideRect(gr,border,border,WIDTH+5*border-1,HEIGHT+5*border-1, border);
			draw3DRect(gr,2*border,2*border,WIDTH+4*border-1,HEIGHT+4*border-1,border,false);
		}
	}

	//--------------------------------------------------------------------------
	//--------------------------------------------------------------------------
	public void paint(Graphics gr)
	{
		int i,j;
		int p,p2;

		if(ledThread != null)
		{
			drawFrame(gr);

			if(beginning)
			{
				offimg = createImage(WIDTH, HEIGHT);
				offmap = offimg.getGraphics();
				offmap.setColor(Color.black);
				offmap.fillRect(0,0,WIDTH,HEIGHT);

				for(i=0;i<HEIGHT;i+=ledsize)
					for(j=0;j<WIDTH;j+=ledsize)
					{
						drawLED(j,i,false,1,offmap);
					}

				gr.drawImage(offimg,offset,offset, this);

				pixmapimg = createImage(WIDTH, HEIGHT);
				tmpimg = createImage(WIDTH, HEIGHT);

				pixmap = pixmapimg.getGraphics();
				tmpmap = tmpimg.getGraphics();

				pixmap.setColor(Color.black);
				pixmap.fillRect(0,0,WIDTH,HEIGHT);

				for(i=0;i<HEIGHT;i+=ledsize)
					for(j=0;j<WIDTH;j+=ledsize)
					{
						drawLED(j,i,false,1,pixmap);
					}

				beginning = false;
			}
			else
			{
				gr.drawImage(pixmapimg,offset,offset, this);
			}
		}
	}

	//--------------------------------------------------------------------------
	// This procedure contains all the different transitions
	//--------------------------------------------------------------------------
	public void update(Graphics gr)
	{
		int i,j;
		int count;

		if(done)
			return;

		if( (ledThread != null) && (fi != null) && (pixmap != null) && (offmap != null) && (tmpmap != null))
		{
			if(((currURL != null) && (lastURL == null)) ||
					((currURL == null) && (lastURL != null)))
			{
				drawFrame(gr);
				lastURL = currURL;
			}

			switch(fi.func)
			{
				case 0:
					if(fi.text == null)
					{
						gr.drawImage(offimg,offset,offset, this);
					}
					else
					{
						for(i=0;i<w;i++)
							for(j=0;j<h;j++)
								drawLED(i*ledsize,j*ledsize,msg.getLED(i,j),msg.getColor(i),pixmap);

						gr.drawImage(pixmapimg,offset,offset, this);
					}

					done = true;

					break;

				case 1:
					done = true;

					break;

				case 2:
					pixmap.copyArea(ledsize,0,WIDTH-ledsize,HEIGHT,-ledsize,0);

					for(i=0;i<HEIGHT;i+=ledsize)
						drawLED(WIDTH-ledsize,i,msg.getLED(place,i/ledsize),msg.getColor(place),pixmap);

					gr.drawImage(pixmapimg,offset,offset, this);

					place++;

					if(!msg.inRange(place))
						done = true;

					break;

				case 3:
					pixmap.copyArea(0,0,WIDTH-ledsize,HEIGHT,ledsize,0);

					for(i=0;i<HEIGHT;i+=ledsize)
						drawLED(0,i,msg.getLED(place,i/ledsize),msg.getColor(place),pixmap);

					gr.drawImage(pixmapimg,offset,offset, this);

					place--;

					if(place < 0)
						done = true;

					break;

				case 4:
					pixmap.copyArea(0,ledsize,WIDTH,HEIGHT-ledsize,0,-ledsize);

					for(i=0;i<WIDTH;i+=ledsize)
						if(msg.inRange(i/ledsize))
							drawLED(i,HEIGHT-ledsize,msg.getLED(i/ledsize,place),msg.getColor(i/ledsize),pixmap);
						else
							drawLED(i,HEIGHT-ledsize,false,1,pixmap);

					gr.drawImage(pixmapimg,offset,offset, this);

					place++;

					if(place >= h)
						done = true;

					break;

				case 5:
					pixmap.copyArea(0,0,WIDTH,HEIGHT-ledsize,0,ledsize);

					for(i=0;i<WIDTH;i+=ledsize)
						if(msg.inRange(i/ledsize))
						{
							drawLED(i,0,msg.getLED(i/ledsize,place),msg.getColor(i/ledsize),pixmap);
						}
						else
						{
							drawLED(i,0,false,1,pixmap);
						}

					gr.drawImage(pixmapimg,offset,offset, this);

					place--;

					if(place < 0)
						done = true;

					break;

				case 6:
					i = place + fi.times;
					while(place < WIDTH/ledsize*h && place < i)
					{
						if(msg.inRange(pix[place].x))
						{
							drawLED(pix[place].x*ledsize,pix[place].y*ledsize,msg.getLED(pix[place].x,pix[place].y),msg.getColor(pix[place].x),pixmap);
						}
						else
						{
							drawLED(pix[place].x*ledsize,pix[place].y*ledsize,false,1,pixmap);
						}

						place++;
					}
					gr.drawImage(pixmapimg,offset,offset, this);

					if(place >= w*h)
						done = true;

					break;

				case 7:
					if(place%2 == 0)
						gr.drawImage(offimg,offset,offset, this);
					else
						gr.drawImage(pixmapimg,offset,offset, this);

					place--;

					if(place == 0)
						done = true;

					break;

				case 8:
					if(msg.inRange(place))
						for(i=0;i<h;i++)
							drawLED(place*ledsize,i*ledsize,msg.getLED(place,i),msg.getColor(place),pixmap);
					else
						for(i=0;i<h;i++)
							drawLED(place*ledsize,i*ledsize,false,1,pixmap);

					gr.drawImage(pixmapimg,offset,offset, this);

					place++;

					if(place >= w)
						done = true;

					break;

				case 9:
					if(w >= place*2)
					{
						pixmap.copyArea(WIDTH/2,0,WIDTH/2-ledsize,HEIGHT,ledsize,0);
						for(i=0;i<h;i++)
							if(msg.inRange(w-place))
								drawLED(WIDTH/2,i*ledsize,msg.getLED(w-place,i),msg.getColor(w-place),pixmap);
							else
								drawLED(WIDTH/2,i*ledsize,false,1,pixmap);
					}

					if(place < w/2)
					{
						pixmap.copyArea(ledsize,0,WIDTH/2-ledsize,HEIGHT,-ledsize,0);
						for(i=0;i<h;i++)
							if(msg.inRange(place))
								drawLED(WIDTH/2-ledsize,i*ledsize,msg.getLED(place,i),msg.getColor(place),pixmap);
							else
								drawLED(WIDTH/2-ledsize,i*ledsize,false,1,pixmap);
					}

					gr.drawImage(pixmapimg,offset,offset, this);

					place++;

					if(place >= w/2 && place*2 > w)
						done = true;

					break;

				case 10:
					if(w >= place+w/2)
					{
						for(i=0;i<h;i++)
							if(msg.inRange(w/2+place+1))
								drawLED(WIDTH/2+place*ledsize+ledsize,i*ledsize,msg.getLED(w/2+place+1,i),msg.getColor(w/2+place+1),pixmap);
							else
								drawLED(WIDTH/2+place*ledsize+ledsize,i*ledsize,false,1,pixmap);
					}

					if(place < w/2)
					{
						for(i=0;i<h;i++)
							if(msg.inRange(w/2-place))
								drawLED(WIDTH/2-place*ledsize,i*ledsize,msg.getLED(w/2-place,i),msg.getColor(w/2-place),pixmap);
							else
								drawLED(WIDTH/2-place*ledsize,i*ledsize,false,1,pixmap);
					}

					gr.drawImage(pixmapimg,offset,offset, this);

					place++;

					if(w < w/2+place && place >= w/2)
						done = true;

					break;

				case 11:
					if(msg.inRange(place))
						for(i=0;i<h;i++)
							drawLED(place*ledsize,i*ledsize,msg.getLED(place,i),msg.getColor(place),pixmap);
					else
						for(i=0;i<h;i++)
							drawLED(place*ledsize,i*ledsize,false,1,pixmap);

					gr.drawImage(pixmapimg,offset,offset, this);

					place--;

					if(place == 0)
						done = true;

					break;

				case 12:
					for(i=0;i<w;i++)
					{
						if(msg.inRange(i))
							drawLED(i*ledsize,place*ledsize,msg.getLED(i,place),msg.getColor(i),pixmap);
						else
							drawLED(i*ledsize,place*ledsize,false,1,pixmap);
					}

					gr.drawImage(pixmapimg,offset,offset, this);

					place--;

					if(place < 0)
						done = true;

					break;

				case 13:
					for(i=0;i<w;i++)
					{
						if(msg.inRange(i))
							drawLED(i*ledsize,place*ledsize,msg.getLED(i,place),msg.getColor(i),pixmap);
						else
							drawLED(i*ledsize,place*ledsize,false,1,pixmap);
					}

					gr.drawImage(pixmapimg,offset,offset, this);

					place++;

					if(place >= h)
						done = true;

					break;
			}
		}

		return;

	}
}
