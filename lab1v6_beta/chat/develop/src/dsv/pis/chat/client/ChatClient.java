// ChatClient.java
// Fredrik Kilander, DSV SU/KTH
// 26-mar-2004/FK Small fixes
// 25-mar-2004/FK Given to its own package.
// 18-mar-2004/FK First version

package dsv.pis.chat.client;


// Standard JDK

import java.io.*;
import java.lang.*;
import java.rmi.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

// Jini




import net.jini.core.entry.*;
import net.jini.core.event.*;
import net.jini.core.lookup.*;
import net.jini.lookup.*;
import net.jini.lookup.entry.*;
import dsv.pis.chat.server.ChatServerInterface;
import dsv.pis.chat.server.ChatNotification;

/**
 * This class implements the ChatClient application.
 */
public class ChatClient
  extends
    java.rmi.server.UnicastRemoteObject	// Since we accept remote calls
  implements
    ServiceDiscoveryListener,	// So we can receive service notifications
    RemoteEventListener		// So we can receive chat notifications
{
  /**
   * Holds the Jini ServiceItems of found ChatServers.
   */
  protected Vector servers = new Vector ();

  /**
   * Maps from serviceID to ServiceItem.
   */
  protected Hashtable serverIDs = new Hashtable ();

  /**
   * Refers to the service object of the currently connected chat-service.
   */
  protected ChatServerInterface myServer = null;

  /**
   * The name the user has choosen to present herself with.
   */
  protected String myName = null;

  /**
   * Helper object (actually, a kind of service) that automates the finding
   * of interesting Jini services.
   */
  protected ServiceDiscoveryManager sdm;

  /**
   * Refers to an object obtained from the service discovery manager to
   * locate a specific kind of service.
   */
  protected LookupCache luc;

  /**
   * Refers to a matching template used to identify interesting services.
   */
  protected ServiceTemplate chatServiceTemplate;

  /**
   * The class name of the interface implemented by services we want to find.
   */
  protected static final String csi =
    "dsv.pis.chat.server.ChatServerInterface";

  /**
   * Information string for the user. Printed by the help command.
   */
  protected static final String versionString = "fk-4.3.26.2";

  /**
   * Creates a new ChatClient instance.
   */
  public ChatClient ()
    throws
      java.io.IOException,
      java.lang.ClassNotFoundException,
      java.rmi.RemoteException
  {
    // Create a service template so the lookup cache will have something
    // to use for matching found services. There are three ways of matching
    // against a service; the service id, an array of service types
    // (interface classes) or an array of attributes (Entry). Here we
    // just use the interface implemented by the service.

    chatServiceTemplate =
      new ServiceTemplate (null,
			   new Class [] {java.lang.Class.forName (csi)},
			   null);

    // Next we create a service discovery manager to manage all interaction
    // with Jini lookup servers for us.
    sdm = new ServiceDiscoveryManager (null, null);

    // Then we ask the SDM for a lookup cache to manage lookups against
    // chat-services found. The arguments are the template for matching,
    // and ourselves so the cache can notify us via the interface
    // ServiceDiscoveryListener (which we implement below).
    luc = sdm.createLookupCache (chatServiceTemplate, null, this);
  }

  // The next three methods, serviceAdded, serviceChanged and serviceRemoved,
  // can be called at any time by the lookup cache object. It will execute
  // in its own thread, talking to Jini lookup servers looking for
  // services that match the service template we gave it. When it has some
  // news, it reports in.

  // In interface ServiceDiscoveryListener

  /**
   * The lookup cache calls this method when it has found a new service.
   * @param e The discovery event.
   */
  public void serviceAdded (ServiceDiscoveryEvent e) {
    ServiceItem sit = e.getPostEventServiceItem ();
    if (sit.service instanceof ChatServerInterface) {
      servers.add (sit);
      serverIDs.put (sit.serviceID, sit);
      System.out.println ("[Added server " + sit.toString () + "]");
    }
  }

  // In interface ServiceDiscoveryListener

  /**
   * The lookup cache calls this method when a found service has changed
   * its registration.
   * @param e The discovery event.
   */
  public void serviceChanged (ServiceDiscoveryEvent e) {
    ServiceItem preSit = e.getPreEventServiceItem ();
    ServiceItem postSit = e.getPostEventServiceItem ();
    if (postSit.service instanceof ChatServerInterface) {
      servers.remove (serverIDs.get (preSit.serviceID));
      servers.add (postSit);
      serverIDs.put (postSit.serviceID, postSit);
      System.out.println ("[Changed server " + postSit.toString () + "]");
    }
  }

  // In interface ServiceDiscoveryListener

  /**
   * The lookup cache calls this method when a found service has removed
   * its registration.
   * @param e The discovery event.
   */
  public void serviceRemoved (ServiceDiscoveryEvent e) {
    ServiceItem sit = e.getPreEventServiceItem ();
    if (sit.service instanceof ChatServerInterface) {
      servers.remove (serverIDs.get (sit.serviceID));
      System.out.println ("[Removed server " + sit.toString () + "]");
    }
  }

  // In interface RemoteEventListener

  /**
   * The ChatServer we are registered with (connected to) calls this method
   * to notify us of a new chat message.
   * @param rev  The remote event that is the notification.
   */
  public void notify (RemoteEvent rev)
    throws
      net.jini.core.event.UnknownEventException,
      java.rmi.RemoteException
  {
    if (rev instanceof ChatNotification) {
      ChatNotification chat = (ChatNotification) rev;
      System.out.println (chat.getSequenceNumber () + " : " +
			  chat.getText ());
    }
  }

  /**
   * This method disconnects the chat client from a chat server. It
   * unregisters the client and supresses any exceptions generated by the
   * communication.
   * @param server The chat server to disconnect from.
   */
  public void disconnect (ChatServerInterface server)
  {
    if (server != null) {
      try {
	String serverName = server.getName ();
	server.unregister (this);
	System.out.println ("[Disconnected from " + serverName + "]");
      }
      catch (java.rmi.RemoteException rex) {}
    }
  }

  /**
   * This method implements the '.disconnect' user command.
   */
  public void userDisconnect () {
    if (myServer != null) {
      disconnect (myServer);
      myServer = null;
    }
    else {
      System.out.println ("[Client is not currently connected]");
    }
  }

  /**
   * This method implements the '.connect' user command. If a servername
   * pattern is supplied, the known chat services are scanned for names
   * in which the pattern is case-insensitive substring. If no pattern
   * is supplied, the connection attempt is directed at the first known
   * server (regardless whether it is answering or not.
   * @param serverNamePattern The substring to match against the server name.
   * @return The index of the found server in the servers Vector.
   */
  public int connectToChat (String serverNamePattern)
  {
    // See if we know any servers at all.

    if (0 < servers.size ()) {
      String serverName = null;

      // Start iterating over the known servers.
      for (int i = 0; i < servers.size (); i++) {

	// Access the service item object (which is what we got from the
	// lookup cache).
	ServiceItem sit = (ServiceItem) servers.elementAt (i);

	// Since we matched for class in the template, this cast is
	// relatively safe. If there was any risk of the service object
	// not being an instance of ChatServerInterface, we would have to
	// test it first using the instanceof operator.
	ChatServerInterface server = (ChatServerInterface) sit.service;

	// Assume that we won't find a matching service.
	boolean accept = false;

	// Do we have a search string?
	if (serverNamePattern == null) {

	  // No, so trivially accept this server.
	  accept = true;

	  // Dig out the name so we can show the user the name of the
	  // server we have selected.
	  for (int j = 0; j < sit.attributeSets.length; j++) {

	    // Access the attributes of the service. We assume that they
	    // exist because that in the contract between the service and
	    // the client.
	    Entry e = sit.attributeSets[j];

	    // Is it a Name attribute?
	    if (e instanceof Name) {
	      serverName = ((Name) e).name;
	      break;
	    }
	  }
	}
	else {
	  for (int j = 0; j < sit.attributeSets.length; j++) {
	    Entry e = sit.attributeSets[j];
	    if (e instanceof Name) {
	      serverName = ((Name) e).name;
	      if (-1 < serverName.toLowerCase ().indexOf (serverNamePattern)) {
		accept = true;
		break;
	      }
	    }
	  }
	}


	if (accept) {
	  ChatServerInterface newServer = null;
	  System.out.print ("[Connecting to " + serverName + "...");
	  System.out.flush ();

	  try {
		if(myName == null)
			myName = "anonymous";
	    server.register (this, myName);
	    newServer = server;
	    System.out.println ("ok]");
	  }
	  catch (java.rmi.RemoteException rex) {
	    System.out.println ("failed]");
	  }

	  if (newServer != null) {
	    if (myServer != null) {
	      disconnect (myServer);
	    }
	    myServer = newServer;
	    return i;
	  }
	}
      }

      System.out.println ("[No servers matching " +
			  serverNamePattern +
			  " found]");

    }
    else {
      System.out.println ("[No servers around]");
    }

    return -1;
  }


  /**
   * This method implements the '.name' user command. It sets the name
   * the user has choosen for herself on the chat. If the name is null
   * or the empty string, the &quot;user.name&quot; system property is
   * used as a substitute.
   *
   * @param newName  The user's name.
   */
  public void setName (String newName) {

    myName = newName;

    if (myName != null) {
      myName = myName.trim ();
      if (myName.length () == 0) {
	myName = null;
      }
    }

    if (myName == null) {
      myName = System.getProperty ("user.name");
    }
    
    try {
    	if(myServer != null)
    		myServer.changeName(this, myName);
	} catch (RemoteException e) {
		System.out.println("Server could not change my name to " + myName);
	}
  }

  /**
   * This method implements the send command which is implicit in the
   * command interpreter (the input line does not start with a period).
   * @param text  The text to send to the currently connected server.
   */
  public void sendToChat (String text) {
    if (myServer != null) {
      try {
	myServer.say (text);
      }
      catch (java.rmi.RemoteException rex) {
	System.out.println ("[Sending to server failed]");
      }
    }
  }

  /**
   * This method implements the '.list' and '.purge' user commands.
   * All known servers are listed and a call attempt is made with each.
   * Non-reachable servers are listed and if the purge parameter is true,
   * also removed from the list of known servers. Note that intermittent
   * network failures (not uncommon for wireless and mobile users) may
   * cause a service to appear down when it really is not.
   *
   * @param purge  False to just list the servers, true to remove the
   *               service objects of services that does not respond.
   */
  public void listServers (boolean purge) {
    if (servers.isEmpty ()) {
      System.out.println ("[No servers found]");
      return;
    }

    for (int i = 0; i < servers.size (); i++) {

      ServiceItem sit = (ServiceItem) servers.elementAt (i);
      ChatServerInterface chat = (ChatServerInterface) sit.service;
      String remoteName = null;
      String localName = null;

      for (int j = 0; j < sit.attributeSets.length; j++) {
	Entry e = sit.attributeSets[j];
	if (e instanceof Name) {
	  localName = ((Name) e).name;
	  break;
	}
      }

      if (localName == null) {
	localName = sit.service.toString ();
      }

      System.out.print ("[");
      try {
	remoteName = chat.getName ();
	System.out.print (remoteName);
	if (chat == myServer) {
	  System.out.print (": connected");
	}
      }
      catch (java.rmi.RemoteException rex) {
	System.out.print (localName + ": not responding");
	if (purge) {
	  servers.remove (i);
	  System.out.print (": PURGED");
	  i = i - 1;		// Compensate for for-loop increment
	}
      }

      System.out.println ("]");
    }

  }

  /**
   * This array holds the strings of the user command help text.
   */
  protected String [] cmdHelp = {
    "Commands (can be abbreviated):",
    "list              List the currently known chat servers",
    "purge             As list, but also forget non-responding servers",
    "name <name>       Set the username presented by the chat client",
    "connect <string>  Connect to a server with a matching string",
    "disconnect        Break the connection to the server",
    "quit              Exit the client",
    "help              This text",
    "users             List connected users"
  };

  /**
   * Implements the '.help' user command.
   * @param argv Reserved for future used (e.g. '.help purge').
   */
  protected void showHelp (String [] argv) {
    System.out.println ("[" + versionString + "]");
    for (int i = 0; i < cmdHelp.length; i++) {
      System.out.println ("[" + cmdHelp[i] + "]");
    }
  }
  
  protected void showRegisteredUsers() {
	  try {
		  if(myServer == null)
			  System.out.println("You are not connected...");
		  else {
			  System.out.println("User name\t\t\ttime (h:m:s)");
			  System.out.println("--------------------------------------------");
			  List<String> users = myServer.registeredUsers();
			  for(String u : users) {
				  System.out.println(u);
			  }
		  }
	} catch (RemoteException e) {
		e.printStackTrace();
	}
  }

  /**
   * Creates a new string which is the concatenation of the elements
   * in a string array, joined around a given delimiter string.
   *
   * @param sa         The string array to join together.
   * @param firstIndex The index of the first element in sa to consider.
   * @param delim      Delimiter string between elements in sa, or null.
   *
   * @return The concatenated result or at least the empty string.
   */
  protected String stringJoin (String [] sa, int firstIndex, String delim) {
    String rtn = "";

    if (sa != null) {
      if (firstIndex < sa.length) {
	rtn = sa[firstIndex];
	for (int i = firstIndex + 1; i < sa.length; i++) {
	  rtn += ((delim == null) ? "" : delim) + sa[i];
	}
      }
    }

    return rtn;
  }

  /**
   * The user command interpreter. Commands are read from standard input,
   * parsed and dispatched methods that either alter the client or sends
   * the text to the ChatServer (when connected).
   */
  public void readLoop () {
    boolean halted = false;
    BufferedReader d = new BufferedReader(new InputStreamReader(System.in));

    System.out.println ("[Output from the client is in braces]");
    System.out.println ("[Commands start with '.' (period). Try .help]");
    System.out.println ("[When connected, type text and hit return to send]");

    while (!halted) {
      System.out.print ("Client> ");
      System.out.flush ();
      String buf = null;

      try {
	buf = d.readLine ();
      }
      catch (java.io.IOException iox) {
	iox.printStackTrace ();
	System.out.println ("\nI/O error in command interface.");
	halted = true;
	continue;
      }

      if (buf == null) {	// EOF in command input.
	halted = true;
	continue;
      }

      // Trim away leading and trailing space from the raw input.

      String arg = buf.trim ();

      // Check if the input starts with a period.

      if (arg.startsWith (".")) {

	// Get a reference to the other side of the leading period.
	String cmd = arg.substring (1);

	// Split the string into fragments, separated by whitespace.
	String [] cmdv = cmd.split ("\\s");

	// Unfortunately enough, the split method does not collapse
	// consecutive whitespace, but regards them as separators of
	// empty strings. So we need to get rid of those.
	ArrayList realWords = new ArrayList ();

	// Iterate over the output of the split and add any non-empty
	// string to the realWords arraylist.
	for (int i = 0; i < cmdv.length; i++) {
	  if (0 < cmdv[i].length ()) {
	    realWords.add (cmdv[i]);
	  }
	}

	// Then recompose the real words into a string array again.
	// (This is not strictly necessary; we could work with the
	// arrayList below, but when the problem was detected the
	// code was already written in terms of a string array.)
	String [] argv =
	  (String []) realWords.toArray (new String [realWords.size ()]);

	// We treat the first word as a command verb and makes it lowercase
	// for easier matching.
	String verb = argv[0].toLowerCase ();

	// We will accept any leading abbreviation and this is fine while
	// the number of commands is so small that their first character
	// is sufficiently distinctive.

	if ("quit".startsWith (verb)) {
	  halted = true;
	}
	else if ("connect".startsWith (verb)) {
	  connectToChat (stringJoin (argv, 1, " "));
	}
	else if ("disconnect".startsWith (verb)) {
	  userDisconnect ();
	}
	else if ("list".startsWith (verb)) {
	  listServers (false);
	}
	else if ("purge".startsWith (verb)) {
	  listServers (true);
	}
	else if ("name".startsWith (verb)) {
	  setName (stringJoin (argv, 1, " "));
	}
	else if ("help".startsWith (verb)) {
	  showHelp (argv);
	}
	else if("users".startsWith(verb)) {
		showRegisteredUsers();
	}
	else {
	  System.out.println ("[" + verb + ": unknown command]");
	}
      }
      else if (0 < arg.length ()) {
	if (myServer != null) {
	  if (myName == null) {
	    setName (myName);
	  }
	  sendToChat (myName + ": " + arg);
	}
	else {
	  System.out.println ("[Client is not connected!]");
	}

      }
    } // while not halted

    System.out.println ("[Quitting, please wait...]");

    disconnect (myServer);

    // Shut down the service discovery manager.

    sdm.terminate ();
  }

  // The main method.

  public static void main (String [] argv)
    throws
      java.io.IOException,
      java.lang.ClassNotFoundException,
      java.rmi.RemoteException
  {
    System.setSecurityManager (new RMISecurityManager ());

    // System.out.println(System.getProperty("java.rmi.server.codebase"));

    ChatClient cc = new ChatClient ();
    cc.readLoop ();
  }
}
