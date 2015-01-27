// Commandline.java
// Fredrik Kilander, DSV
// 18-nov-2004/FK Adapted for Pis course.
// 2000-10-11/FK

package dsv.pis.gotag.util;

import java.lang.*;
import java.io.*;

/**
 * The Commandline class contains static utility routines for parsing
 * the commandline.
 */
public class Commandline {

  public static String [] parseArgs (PrintStream ps,
				     String [] argv,
				     CmdlnOption [] opts) {
    int i;
    int j;
    int restCount = 0;
    String [] restArgs = new String[argv.length];

    for (i = 0; i < argv.length;) {
      boolean found = false;
      for (j = 0; j < opts.length; j++) {
	if (opts[j].keyMatches (argv[i])) {
	  found = true;
	  break;
	}
      }

      if (found == true) {
	if (opts[j].expectsArg ()) {
	  if (i == (argv.length - 1)) {
	    if (ps != null) {
	      ps.println ("Argument expected for option: "
			  + argv[i]);
	    }
	    return null;
	  }
	  else {
	    i++;
	    opts[j].setValue (argv[i]);
	  }
	}
	else if (opts[j].allowsArg ()) {
	  opts[j].setValue (null); // Tentatively set null
	  if (i < argv.length - 1) {
	    if (argv[i+1].charAt (0) != '-') {
	      i++;
	      opts[j].setValue (argv[i]); // Replace with probable arg.
	    }
	  }
	}
	else {
	  opts[j].setValue (null);
	}
	i++;
      }
      else if (argv[i].charAt (0) == '-') {
	if (ps != null) {
	  ps.println ("Unknown option: " + argv[i]);
	}
	return null;
      }
      else {
	restArgs [restCount++] = argv[i++];
      }
    }

    boolean errFlag = false;

    for (j = 0; j < opts.length; j++) {
      if ((opts[j].getIsOptional () == false)
	  &&
	  (opts[j].getIsSet () == false)){
	errFlag = true;
	if (ps != null) {
	  ps.println ("Mandatory option "
		      + opts[j].getKey () + " is missing.");
	}
      }
    }

    if (errFlag == true) {
      return null;
    }

    String [] rtns = new String [restCount];
    System.arraycopy (restArgs, 0, rtns, 0, restCount);
    return rtns;
  }
}
