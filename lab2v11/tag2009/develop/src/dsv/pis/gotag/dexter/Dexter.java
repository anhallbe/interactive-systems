// Dexter.java
// Bailiff excerciser and demo.
// Fredrik Kilander, DSV
// 30-jan-2009/FK Replaced f.show() (deprecated) with f.setVisible();
// 07-feb-2008/FK Code smarted up a bit.
// 18-nov-2004/FK Adapted for PRIS course.
// 2000-12-18/FK Runs for the first time.
// 2000-12-13/FK

package dsv.pis.gotag.dexter;

import java.io.*;
import java.lang.*;
import java.util.Random;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import net.jini.core.lookup.*;
import net.jini.lookup.*;

import dsv.pis.gotag.util.*;
import dsv.pis.gotag.bailiff.BailiffInterface;

/**
 * Dexter jumps around randomly among the Bailiffs. He is can be used
 * test that the system is operating, or as a template for more
 * evolved agents.
 */
public class Dexter implements Serializable
{
  /**
   * The string name of the Bailiff service interface, used when
   * querying the Jini lookup server.
   */
  protected static final String bfi =
    "dsv.pis.gotag.bailiff.BailiffInterface";

  /**
   * The debug flag controls the amount of diagnostic info we put out.
   */
  protected boolean debug = false;

  /**
   * The noFace flag disables the graphical frame when true.
   */
  protected boolean noFace = false;

  /**
   * Dexter uses a ServiceDiscoveryManager to find Bailiffs.
   * The SDM is not serializable so it must recreated on each new Bailiff.
   * That is why it is marked as transient.
   */
  protected transient ServiceDiscoveryManager SDM;

  /**
   * This service template is created in Dexter's constructor and used
   * in the topLevel method to find Bailiffs. The service
   * template IS serializable so Dexter only needs to instantiate it once.
   */
  protected ServiceTemplate bailiffTemplate;

  /**
   * Outputs a diagnostic message on standard output. This will be on
   * the host of the launching JVM before Dexter moves. Once he has migrated
   * to another Bailiff, the text will appear on the console of that Bailiff.
   * @param msg The message to print.
   */
  protected void debugMsg (String msg) {
    if (debug) System.out.println (msg);
  }

  /**
   * This creates a new Dexter. All the constructor needs to do is to
   * instantiate the service template.
   * @param debug True if this instance is being debugged.
   * @throws ClassNotFoundException Thrown if the class for the Bailiff
   * service interface could not be found.
   */
  public Dexter (boolean debug, boolean noFace)
    throws
      java.lang.ClassNotFoundException
  {
    if (this.debug == false) this.debug = debug;

    this.noFace = noFace;

    // This service template is used to query the Jini lookup server
    // for services which implement the BailiffInterface. The string
    // name of that interface is passed in the bfi argument. At this
    // point we only create and configure the service template, no
    // query has yet been issued.

    bailiffTemplate =
      new ServiceTemplate
	(null,
	 new Class [] {java.lang.Class.forName (bfi)},
	 null);
  }

  /**
   * Sleep snugly and safely not bothered by interrupts.
   * @param ms  The number of milliseconds to sleep.
   */
  protected void snooze (long ms) {
    try {
      Thread.currentThread ().sleep (ms);
    }
    catch (java.lang.InterruptedException e) {}
  }

  /**
   * This is Dexter's main program once he is on his way. In short, he
   * gets himself a service discovery manager and asks it about Bailiffs.
   * If the list is long enough, he then selects one randomly and pings it.
   * If the ping returned without a remote exception, Dexter then tries
   * to migrate to that Bailiff. If the ping or the migrates fails, Dexter
   * gives up on that Bailiff and tries another.
   */
  public void topLevel ()
    throws
      java.io.IOException
  {
    Random rnd = new Random ();

    // Create a Jini service discovery manager to help us interact with
    // the Jini lookup service.
    SDM = new ServiceDiscoveryManager (null, null);

    DexterFace dexFace = null;
    JFrame f = null;

    if (!noFace) {
      // Create a small GUI for this Dexter instance.
      f = new JFrame ("Dexter");
      f.addWindowListener (new WindowAdapter () {
	  public void windowClosing (WindowEvent e) {System.exit (0);}
	});
      dexFace = new DexterFace ();
      f.getContentPane ().add ("Center", dexFace);
      dexFace.init ();
      f.pack ();
      f.setSize (new Dimension (256, 192));
      f.setVisible (true);
      dexFace.startAnimation ();
    }

    for (;;) {

      ServiceItem [] svcItems;

      long retryInterval = 0;

      // The restraint sleep is just there so we don't get hyperactive
      // and confuse the slow human beings.

      debugMsg ("Entering restraint sleep.");

      snooze (5000);

      debugMsg ("Leaving restraint sleep.");

      // Enter a loop in which Dexter tries to find some Bailiffs.

      do {

	if (0 < retryInterval) {
	  debugMsg ("No Bailiffs detected - sleeping.");
	  snooze (retryInterval);
	  debugMsg ("Waking up.");
	}

	// Put our query, expressed as a service template, to the Jini
	// service discovery manager. 

	svcItems = SDM.lookup (bailiffTemplate, 8, null);
	retryInterval = 20 * 1000;

	// If no lookup servers are found, go back up to the beginning
	// of the loop, sleep a bit and then try again.
      } while (svcItems.length == 0);
      
      // We have the Bailiffs.

      debugMsg ("Found " + svcItems.length + " Bailiffs.");

      // Now enter a loop in which we try to ping and migrate to them.

      int nofItems = svcItems.length;

      // While we still have at least one Bailiff service to try...

      while (0 < nofItems) {

	// Select one Bailiff randomly.

	int idx = 0;
	if (1 < nofItems) {
	  idx = rnd.nextInt (nofItems);
	}

	boolean accepted = false;	    // Assume it will fail
	Object obj = svcItems[idx].service; // Get the service object
	BailiffInterface bfi = null;

	// Try to ping the selected Bailiff.

	debugMsg ("Trying to ping...");

	try {
	  if (obj instanceof BailiffInterface) {
	    bfi = (BailiffInterface) obj;
	    String response = bfi.ping (); // Ping it
	    debugMsg (response);
	    accepted = true;	// Oh, it worked!
	  }
	}
	catch (java.rmi.RemoteException e) { // Ping failed
	  if (debug) {
	    e.printStackTrace ();
	  }
	}

	debugMsg (accepted ? "Accepted." : "Not accepted.");

	// If the ping failed, delete that Bailiff from the array and
	// try another. The current (idx) entry in the list of service items
	// is replaced by the last item in the list, and the list length
	// is decremented by one.

	if (accepted == false) { 
	  svcItems[idx] = svcItems[nofItems - 1];
	  nofItems -= 1;
	  continue;		// Back to top of while-loop.
	}
	else {

	  debugMsg ("Trying to jump...");

	  // This is the spot where Dexter tries to migrate.

	  try {
		  System.out.println("Jumping..");
	    bfi.migrate (this, "topLevel", new Object [] {});
	    System.out.println("Jumped to new VM");
	    SDM.terminate ();	// SUCCESS
	    if (!noFace) {
	      dexFace.stopAnimation ();
	      f.setVisible (false);
	    }
	    return;		// SUCCESS
	  }
	  catch (java.rmi.RemoteException e) { // FAILURE
	    if (debug) { e.printStackTrace (); }
	  }
	  catch (java.lang.NoSuchMethodException e) { // FAILURE
	    if (debug) { e.printStackTrace (); }
	  }

	  debugMsg ("Didn't make the jump...");

	}
      }	// while there are candidates left

      debugMsg ("They were all bad.");

    } // for ever // go back up and try to find more Bailiffs
  }

  /**
   * The main program of Dexter. It is only used when a Dexter is launched.
   */
  public static void main (String [] argv)
    throws
      java.lang.ClassNotFoundException,
      java.io.IOException
  {
    CmdlnOption helpOption  = new CmdlnOption ("-help");
    CmdlnOption debugOption = new CmdlnOption ("-debug");
    CmdlnOption noFaceOption = new CmdlnOption ("-noface");

    CmdlnOption [] opts =
      new CmdlnOption [] {helpOption, debugOption, noFaceOption};

    String [] restArgs = Commandline.parseArgs (System.out, argv, opts);

    if (restArgs == null) {
      System.exit (1);
    }

    if (helpOption.getIsSet () == true) {
      System.out.println ("Usage: [-help]|[-debug][-noface]");
      System.out.println ("where -help shows this message");
      System.out.println ("      -debug turns on debugging.");
      System.out.println ("      -noface disables the GUI.");
      System.exit (0);
    }

    boolean debug = debugOption.getIsSet ();
    boolean noFace = noFaceOption.getIsSet ();

    // We will try without it first
    // System.setSecurityManager (new RMISecurityManager ());
    Dexter dx = new Dexter (debug, noFace);
    dx.topLevel ();
    System.exit (0);
  }
}
