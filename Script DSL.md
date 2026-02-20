# The LED Script

### Syntax

The script syntax for LED Sign V3.1 is 100% backward compatible with previous version of LED Sign. 

The syntax for the LED script is extremely simple. Each command must be on one line; they cannot be split onto multiple lines. Only one command can be on a line. A line that starts with a "!!" is a comment. Here is an example of a script:
````
!! Just a simple script
Do
!! Move "Hello" up onto the sign
ScrollUp delay=30 center=true text=Hello
Sleep delay=1000

!! Scroll "This is a test" left onto the sign
ScrollLeft delay=30 startspace=20 endspace=80 text=This is a test
Sleep delay=300

!! Repeat infinitely
Repeat times=-1
````
You can write a script with the same text editor that you use to write your HTML. Programs like Windows Notepad or Macintosh teachtext will work fine.

### The Options

The different options for each function/transition are as follows:

*   clear=true
*   delay=<non-neg int>
*   startspace=<non-neg int>
*   endspace=<non-neg int>
*   center=true
*   times=<-1 | non-neg int> (-1 specifies infinity)
*   pixels=<pos int>
*   URL=<URL string>
*   text=<string>

### The Functions/Transitions

The different functions/transitions available in LED Sign are as follows:

*   Appear - Cause text to appear on the sign
*   Sleep - A delay
*   ScrollUp - Scroll whatever is on the sign and the new message on going from bottom to top.
*   ScrollDown - Scroll whatever is on the sign and the new message on going from top to bottom.
*   ScrollLeft - Scroll whatever is on the sign and the new message on going from right to left.
*   ScrollRight - Scroll whatever is on the sign and the new message on going from left to right.
*   ScrollCenter - Scroll whatever is on the sign and the new message originating from the center.
*   OverUp - same as ScrollUp except it overwrites whatever is on the sign.
*   OverDown - same as ScrollDown except it overwrites whatever is on the sign.
*   OverLeft - same as ScrollLeft except it overwrites whatever is on the sign.
*   OverRight - same as ScrollRight except it overwrites whatever is on the sign.
*   OverCenter - same as ScrollCenter except it overwrites whatever is on the sign.
*   Pixel - Pixel in a new message
*   Blink - Cause what ever is on the sign to blink
*   Do - begin a "Repeat" block
*   Repeat - end a "Repeat" block
*   Reload - reload the script. Probably the most powerful feature of LED Sign. Whenever LED Sign gets the Reload command, it will reload the script over the URL. Useful if information is updated frequently (like stock quotes). An example use of reload:

          Do
                [Display some messages/information]
          
          !! display those messages 5 times
          Repeat times=5
          !! Now reload the script to get any possible changes
          Reload


### Usage

*   Appear \[clear=true\] \[spacing\] \[URL=<URL string>\] \[text=<String>\]
*   Sleep \[URL=<URL string>\] \[delay=<non-neg int>\] (defaults to 1000)
*   ScrollUp \[clear=true\] \[URL=<URL string>\] \[delay=<non-neg int>\] \[spacing\] \[text=<String>\]
*   ScrollDown \[clear=true\] \[URL=<URL string>\] \[delay=<non-neg int>\] \[spacing\] \[text=<String>\]
*   ScrollLeft \[clear=true\] \[URL=<URL string>\] \[delay=<non-neg int>\] \[spacing\] \[text=<String>\]
*   ScrollRight \[clear=true\] \[URL=<URL string>\] \[delay=<non-neg int>\] \[spacing\] \[text=<String>\]
*   ScrollCenter \[clear=true\] \[URL=<URL string>\] \[delay=<non-neg int>\] \[spacing\] \[text=<String>\]
*   OverUp \[clear=true\] \[URL=<URL string>\] \[delay=<non-neg int>\] \[spacing\] \[text=<String>\]
*   OverDown \[clear=true\] \[URL=<URL string>\] \[delay=<non-neg int>\] \[spacing\] \[text=<String>\]
*   OverLeft \[clear=true\] \[URL=<URL string>\] \[delay=<non-neg int>\] \[spacing\] \[text=<String>\]
*   OverRight \[clear=true\] \[URL=<URL string>\] \[delay=<non-neg int>\] \[spacing\] \[text=<String>\]
*   OverCenter \[clear=true\] \[URL=<URL string>\] \[delay=<non-neg int>\] \[spacing\] \[text=<String>\]
*   Pixel \[clear=true\] \[URL=<URL string>\] \[delay=<non-neg int>\] \[pixels=<pos int>\] \[spacing\] \[text=<String>\]
*   Blink \[delay=<non-neg int>\] \[URL=<URL string>\] \[times=<pos int>\]
*   Do (no parameters)
*   Repeat times=<-1 | non-neg int> (the "times" parameter is required)
*   Reload (no parameters)

\[spacing\] = \[center=true\] | \[startspace=<non-neg int>\] \[endspace=<non-neg int>\]

### Text Color

In LED Sign you can make the "on" LEDs eight different colors:

*   Red (default)
*   Orange
*   Yellow
*   Green
*   Cyan
*   Blue
*   Purple
*   White

The way you specify a color is with a '\\' followed by 'r', 'o', 'y', 'g', 'c', 'b', 'p', or 'w'. Everything after a color specification will be that color until another color is specified or the end of the line is reached. Each line starts out with the default of red. The text specified as:

text=This \\ois \\ya \\gtest

will appear on the sign as "This is a test" but with "Test" being red, "is" being orange, "a" being yellow and "test" being green.

### Variable tags!!!

LED Sign also has the capability of displaying the current time/date. The tags are specified in the text just like the colors. Here are the time/date tags:

*   tt - time
*   dd - day abbreviated (eg Fri)
*   DD - day (eg Friday)
*   dn - day number 1-31
*   mm - month abbreviated (eg Aug)
*   MM - month (eg August)
*   mn - month number 1-12
*   yy - last two digits of the year (eg 95)
*   YY - the year (eg 1995)

The way the time/date tags are used is by preceding them by a backslash ('\\') and enclosing them in brackets ({}). Here is an exaple:

     ScrollLeft text=The current time is \{tt}.  Today is \{dd} \{mm} \{dn}, \{YY}.

The line above in a script would print somthing like "The current time is 6:19 pm. Today is Sun Oct 29, 1995." on the sign.

### URL connections!
#### Currently non-functional though the visual is there.
~~You can specify a URL to go to if the user clicks on LED Sign while it is displaying a certain message. To do this all you need to do is specify the URL on the function line in the script. Here's an example:~~

     ScrollLeft URL=http://java.sun.com text=Java is cool!
     ScrollLeft URL=http://java.sun.com endspace=40 text=Click on this sign now to go to Sun!

~~The URL tag is also valid for the "Sleep" function. So you can be displaying a message and sleeping, and they can still click on the sign and have it take them to the URL. Example:~~

     Appear center=true URL=http://www.sun.com text=Sun MicroSystems
     Sleep URL=http://www.sun.com delay=4000
     !! Wait a long time to give them a chance to click!

### Notes

The "text" parameter **must** be the last parameter, because the string after the "text=" is your message. This makes it easy to parse (ie no nested quote problem) and easy for the user. What you see is what you get!!!

If the "clear=true" parameter is given to one of the transitions, it will cause the sign to clear (turn off LEDs) in the manner of the transition. i.e. "ScrollDown clear=true" would clear the sign by scrolling whatever is on the sign down off the sign.

If the "center=true" option is set, then the message will be centered on the sign **if** it fits on the sign. It doesn't make sense to center a message if it is longer than the sign. So, if your message isn't centering, then it is probably too long. The "startspace" and "endspace" parameters are ignored when the "center=true" parameter is given.

The script is much like HTML in how it handles function tags and parameters. It will ignore anything it does not recognize. However, there are some ways to stop the applet. For example, "delay=fifty" is incorrect because "delay" requires an integer and would thus cause the applet to stop. There is some script error output. If you are having trouble with LED Sign running your script, check your java console (under options menu in netscape); any error output is printed there.

- - -

The LED Sign Java applet is written and Copyright 1995 by ~~[Darrick Brown](http://www.cs.hope.edu/~dbrown/)~~ dead site