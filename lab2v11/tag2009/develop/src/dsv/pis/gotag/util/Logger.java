// Logger.java
// Fredrik Kilander, DSV
// 18-nov-2004/FK Adapted for PRIS course. This is redundant since Java 1.4.
// 2000-10-11/FK First compiled version
// 2000-10-09/FK First version

// We should introduce an option to maintain the size of the logfile.

package dsv.pis.gotag.util;

import java.io.*;
import java.util.*;
import java.text.*;

/**
 * The Logger class is a utility which takes care of log information.
 * The information submitted to the log is treated in three different ways:
 * A timestamp is prepended to each entry.
 * Output may (or may not) be reflected on a file.
 * Output may (or may not) be reflected on a PrintWriter submitted by the
 * application (System.out comes to mind as a natural choice).
 * Writing to a Logger (in theory) never fails, although it may be
 * difficult for the Logger to make a record of that fact.
 */
public class Logger {
  protected PrintWriter pwAux = null;
  protected PrintWriter pwLog = null;
  
  protected File currentLogFile = null;

  protected DateFormat dtf = null;

  protected void init () {
    dtf = DateFormat.getDateTimeInstance ();
  }

  /**
   * Creates a new Logger without default (none) output streams.
   */
  public Logger () {
    init ();
  }

  /**
   * Creates a new Logger with logging to the supplied filename.
   * @param logFileName The path to the logfile.
   * @param append True if the log should be appended to an existing file.
   */
  public Logger (String logFileName, boolean append) {
    init ();
    setLogFileName (logFileName, append);
  }

  /**
   * Creates a new Logger with output to new file.
   * The logfile name is
   * composed from the application name and a five-digit random number.
   * The extension is <pre>.log</pre>.
   * @param logFileDir The directory in which to place the log file.
   * @param appName The application name to be used as prefix in the
   * log file name. For example, &quot;MyTest&quot; may result in the
   * log file name &quot;MyText95734.log&quot;.
   */
  public Logger (String logFileDir, String appName) {
    init ();
    setLogFile (logFileDir, appName);
  }

  /**
   * Sets the logfile to use.
   * @param logFileName The path to the logfile.
   * @param append If true, the log is appended to the file.
   */
  public synchronized void setLogFileName (String logFileName,
					   boolean append) {
    setFile (new File (logFileName), append);
  }

  /**
   * Composes a name for the logfile.
   * The logfile name is
   * composed from the application name and a five-digit random number.
   * The extension is <pre>.log</pre>.
   * @param logFileDir The directory in which to place the log file.
   * @param appName The application name to be used as prefix in the
   * log file name. For example, &quot;MyTest&quot; may result in the
   * log file name &quot;MyText95734.log&quot;.
   */
  public synchronized void setLogFile (String logFileDir, String appName) {
    String lfd;
    if (logFileDir == null) {
      lfd = "";
    }
    else {
      lfd = logFileDir;
      while (lfd.endsWith (File.separator)) {
	lfd = lfd.substring (0, lfd.length ());
      }
    }
    Random rnd = new Random (System.currentTimeMillis ());
    for (int i = 0; i < 10000; i++) {
      File candidate = new File (lfd
				 + File.separator
				 + appName
				 + rnd.nextInt (100000)
				 + ".log");
      if (candidate.exists () == false) {
	setFile (candidate, false);
	break;
      }
    }
  }

  /**
   * Sets the file to be used as the logfile.
   * @param f The File object to use as log file.
   * @param append If true, the log is appended to the file.
   */
  public synchronized void setFile (File f, boolean append) {
    currentLogFile = f;
    try {
      pwLog = new PrintWriter (new FileWriter (f.getCanonicalPath (), append));
    }
    catch (java.io.IOException e) {
      pwLog = null;
    }
  }

  /**
   * Returns the current logfile or null if none is assigned.
   */
  public File getCurrentLogFile () {
    return currentLogFile;
  }

  /**
   * Sets the auxilliary output stream.
   * @param aux The auxilliary output stream.
   */
  public synchronized void setAuxWriter (PrintWriter aux) {
    pwAux = aux;
  }

  /**
   * Returns the PrintWriter used for the log or null if none is set.
   * @return The PrintWriter object used for the log.
   */
  public PrintWriter getLogWriter () {
    return pwLog;
  }

  /**
   * Returns the auxilliary PrintWriter or null if not set.
   * @return The auxilliary PrintWriter or null if not set.
   */
  public PrintWriter getAuxWriter () {
    return pwAux;
  }

  /**
   * Makes a log entry. A timestamp and a colon
   * are prepended to the string. A newline is printed after it.
   * The entry is sent to the logfile (if set) and the auxilliary
   * PrintWriter (if set). Both streams are flushed (if set).
   * @param e The string to enter into the log.
   */
  public synchronized void entry (String e) {
    String ts = null;
    if ((pwLog != null) || (pwAux != null)) {
      ts = dtf.format (new Date ()) + ":" + e;
      if (pwLog != null) {
	pwLog.println (ts);
	pwLog.flush ();
      }
      if (pwAux != null) {
	pwAux.println (ts);
	pwAux.flush ();
      }
    }
  }

  /**
   * Makes a log entry for an exception.
   * @param t The Throwable instance to log.
   */
  public synchronized void entry (Throwable t) {
    String ts = null;
    if ((pwLog != null) || (pwAux != null)) {
      ts = dtf.format (new Date ()) + ": EXCEPTION";
      if (pwLog != null) {
	pwLog.println (ts);
	t.printStackTrace (pwLog);
      }
      if (pwAux != null) {
	pwAux.println (ts);
	t.printStackTrace (pwAux);
      }
    }
  }

  /**
   * Closes the logfile (if set). The auxilliary PrintWriter is never
   * closed by the Logger.
   */
  public synchronized void close () {
    if (pwLog != null) {
      pwLog.close ();
    }
  }
}
