// Deedee.java
// Fredrik Kilander, DSV
// 30-jan-2009/FK Replaced f.show() (deprecated) with f.setVisible();
// 07-feb-2008/FK Code smarted up a bit.
// 18-nov-2004/FK Adapted for PRIS course.
// 2001-03-28/FK

package dsv.pis.gotag.deedee;

import java.io.*;
import java.lang.*;
import java.util.Random;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import net.jini.core.lookup.*;
import net.jini.core.entry.*;
import net.jini.lookup.*;
import net.jini.lookup.entry.*;

import dsv.pis.gotag.util.*;
import dsv.pis.gotag.bailiff.BailiffInterface;

/**
 * Deedee is a messenger agent. She looks for Bailiffs (execution services)
 * registered to a particular user (started with the -user switch).
 * When she finds one, she migrates there and pops a dialogue with her message.
 */
public class Deedee implements Serializable {

  /**
   * The service discovery manager helps us locating Jini lookup servers
   * and to query them for the services they contain.
   */
  protected transient ServiceDiscoveryManager SDM;

  /**
   * The bailiff template allows us to search for bailiffs using a
   * template description of them.
   */
  protected ServiceTemplate bailiffTemplate;

  /**
   * The (system-local) name of the user for which the message is intended.
   */
  protected String toUser;

  /**
   * The message intended for the user.
   */
  protected String message;

  /**
   * The Java system time when the message times out.
   */
  protected long expires;

  /**
   * As long as the runFlop is true the main loops keeps running.
   */
  protected boolean runFlop = true;

  /**
   * The fully qualified name of the bailiff interface class.
   */
  protected String bfName = "dsv.pis.gotag.bailiff.BailiffInterface";

  /**
   * Creates a new Deedee.
   * @param targetUser The recipient's (system-local user-) name.
   * @param mesg The message text.
   * @param expires The Java system expiration time of the message.
   */
  public Deedee (String targetUser, String msg, long expires)
    throws java.lang.ClassNotFoundException
  {
    // Copy from method arguments to instance fields.
    toUser = targetUser;
    message = msg;
    this.expires = expires;
    // And create a template to match services with.
    bailiffTemplate =
      new ServiceTemplate (null,
			   new Class [] {java.lang.Class.forName (bfName)},
			   new Entry [] {new Location (null, null, toUser)});
  }

  /**
   * Version of sleep wrapped in a try clause.
   * @param ms The number of milliseconds to sleep.
   */
  protected void snooze (long ms) {
    try {
      Thread.currentThread ().sleep (ms);
    }
    catch (java.lang.InterruptedException e) {}
  }

  /**
   * The entry point and main program of Deedee.
   */
  public void topLevel () throws java.io.IOException {

    // Holds name of the local host computer.
    String host = null;

    // Try to look up the name of the local host computer.
    try {
      host =
	java.net.InetAddress.getLocalHost ().getHostName ().toLowerCase ();
    }
    catch (java.net.UnknownHostException e) {
      return;
    }

    // Get a service discovery manager.
    SDM = new ServiceDiscoveryManager (null, null);

    // Create a user interface frame.
    JFrame f = new JFrame ("Deedee -> [" + toUser + "]");

    // If the interface frame is closed, we terminate as well.
    f.addWindowListener (new WindowAdapter () {
      public void windowClosing (WindowEvent e) {
	runFlop = false;
      }
    });

    // Instantiate a Deedee GUI object.
    DeedeeGUI dgui = new DeedeeGUI ();

    // Put it in the interface frame.
    f.getContentPane ().add ("Center", dgui);

    dgui.init ();
    dgui.start ();

    f.pack ();
    f.setSize (new Dimension (192, 192));
    //    f.show ();  // 30-jan-2009 deprecated, replaced by f.setVisible()
    f.setVisible (true);

    // While we are still running...

    while (runFlop) {

      // Has the message expiration time been reached?
      if (System.currentTimeMillis () < expires) {
	dgui.showGivingUp ();	// Emote the GUI
	runFlop = false;	// Our exit condition
	continue;		// To top of loop
      }

      dgui.showLookup ();	// Emote the GUI

      // Lookup the next Bailiff service object.
      ServiceItem svcItem;
      svcItem = SDM.lookup (bailiffTemplate, null);

      // If none is  found...
      if (svcItem == null) {
	dgui.showWaiting ();	// Emote the GUI
	snooze (60000);		// Sleep a while
	continue;		// Try again
      }


      boolean isArrived = false;
      dgui.showDeliberation ();	// Emote the GUI
      Entry [] atts = svcItem.attributeSets; // Get service attributes

      for (int i = 0; i < atts.length; i++) { // For each attribute
	if (atts[i] instanceof Location) { // If it is Location ..
	  Location loc = (Location) atts[i];
	  isArrived = loc.floor.equalsIgnoreCase (host); // This host?
	  if (isArrived == true) {
	    break;
	  }
	}
      }

      // At this point, if isArrived is true, we have found a bailiff
      // registered to our current host.

      if (isArrived == false) {
	if (svcItem.service instanceof BailiffInterface) {
	  BailiffInterface bfi = (BailiffInterface) svcItem.service;
	  dgui.showPrepareJump ();
	  try {
	    bfi.migrate (this, "topLevel", new Object [] {});
	    dgui.showGone ();
	    runFlop = false;
	  }
	  catch (java.lang.NoSuchMethodException e) {
	    dgui.showPain ();
	  }
	  catch (java.rmi.RemoteException e) {
	    dgui.showPain ();
	  }
	}
      }
      else {
	dgui.showMessage (message);
	runFlop = false;
      }
  
    }// while runFlop is true

    dgui.stop ();
    SDM.terminate ();
    f.setVisible (false);
  } // topLevel

  /**
   * Main program for Deedee
   */
  public static void main (String [] argv)
    throws
      java.io.IOException,
      java.lang.ClassNotFoundException
  {
    Deedee de = new Deedee ("ralph",
			    "This is a message for Ralph!",
			    300000);
    de.topLevel ();
    System.exit (0);
  }
}
